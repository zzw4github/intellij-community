/*
 * Copyright 2003-2005 Dave Griffith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.style;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.ImportUtils;
import com.siyeh.ig.ui.SingleCheckboxOptionsPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class UnnecessaryFullyQualifiedNameInspection extends ClassInspection {

  @SuppressWarnings("PublicField")
  public boolean m_ignoreJavadoc = false;
  private final UnnecessaryFullyQualifiedNameFix fix =
    new UnnecessaryFullyQualifiedNameFix();

  public String getGroupDisplayName() {
    return GroupNames.STYLE_GROUP_NAME;
  }

  public JComponent createOptionsPanel() {
    return new SingleCheckboxOptionsPanel(
      "Ignore fully qualified names in javadoc",
      this,
      "m_ignoreJavadoc");
  }

  public BaseInspectionVisitor buildVisitor() {
    return new UnnecessaryFullyQualifiedNameVisitor();
  }

  public InspectionGadgetsFix buildFix(PsiElement location) {
    return fix;
  }

  private static class UnnecessaryFullyQualifiedNameFix
    extends InspectionGadgetsFix {
    public String getName() {
      return "Replace with import";
    }

    public void doFix(Project project, ProblemDescriptor descriptor)
      throws IncorrectOperationException {
      final CodeStyleSettingsManager settingsManager =
        CodeStyleSettingsManager.getInstance(project);
      final CodeStyleSettings settings =
        settingsManager.getCurrentSettings();
      final boolean oldUseFQNamesInJavadoc =
        settings.USE_FQ_CLASS_NAMES_IN_JAVADOC;
      final boolean oldUseFQNames = settings.USE_FQ_CLASS_NAMES;
      try {
        settings.USE_FQ_CLASS_NAMES_IN_JAVADOC = false;
        settings.USE_FQ_CLASS_NAMES = false;
        final PsiJavaCodeReferenceElement reference =
          (PsiJavaCodeReferenceElement)descriptor
            .getPsiElement();
        final PsiManager psiManager = reference.getManager();
        final CodeStyleManager styleManager =
          psiManager.getCodeStyleManager();
        styleManager.shortenClassReferences(reference);
      }
      finally {
        settings.USE_FQ_CLASS_NAMES_IN_JAVADOC = oldUseFQNamesInJavadoc;
        settings.USE_FQ_CLASS_NAMES = oldUseFQNames;
      }
    }
  }

  private class UnnecessaryFullyQualifiedNameVisitor
    extends BaseInspectionVisitor {
    private boolean m_inClass = false;

    public void visitClass(@NotNull PsiClass aClass) {
      final boolean wasInClass = m_inClass;
      if (!m_inClass) {

        m_inClass = true;
        super.visitClass(aClass);
      }
      m_inClass = wasInClass;
    }

    public void visitReferenceElement(
      PsiJavaCodeReferenceElement reference) {
      super.visitReferenceElement(reference);
      final String text = reference.getText();
      if (text.indexOf((int)'.') < 0) {
        return;
      }
      if (m_ignoreJavadoc) {
        final PsiElement containingComment =
          PsiTreeUtil.getParentOfType(reference,
                                      PsiDocComment.class);
        if (containingComment != null) {
          return;
        }
      }
      final PsiElement psiElement = reference.resolve();
      if (!(psiElement instanceof PsiClass)) {
        return;
      }
      final PsiReferenceParameterList typeParameters =
        reference.getParameterList();
      if (typeParameters == null) {
        return;
      }
      typeParameters.accept(this);
      final PsiClass aClass = (PsiClass)psiElement;
      final PsiClass outerClass =
        ClassUtils.getOutermostContainingClass(aClass);
      final String fqName = outerClass.getQualifiedName();
      if (!text.startsWith(fqName)) {
        return;
      }
      PsiClass containingClass =
        ClassUtils.getContainingClass(reference);
      while (containingClass != null) {
        final String containingClassName = containingClass.getName();
        if (text.endsWith('.' + containingClassName)) {
          return;
        }
        containingClass = ClassUtils.getContainingClass(containingClass);
      }
      final PsiJavaFile file = (PsiJavaFile)reference.getContainingFile();
      if (!ImportUtils.nameCanBeImported(text, file)) {
        return;
      }
      registerError(reference);
    }
  }
}
