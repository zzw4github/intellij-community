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
package com.siyeh.ig.classlayout;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.psiutils.UtilityClassUtil;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

public class UtilityClassWithPublicConstructorInspection extends ClassInspection {

  public String getGroupDisplayName() {
    return GroupNames.CLASSLAYOUT_GROUP_NAME;
  }

  protected InspectionGadgetsFix buildFix(PsiElement location) {
    final PsiClass psiClass = (PsiClass)location.getParent();
    assert psiClass != null;
    if (psiClass.getConstructors().length > 1) {
      return new UtilityClassWithPublicConstructorFix(true);
    }
    else {
      return new UtilityClassWithPublicConstructorFix(false);
    }
  }

  private static class UtilityClassWithPublicConstructorFix
    extends InspectionGadgetsFix {
    private final boolean m_multipleConstructors;

    UtilityClassWithPublicConstructorFix(boolean multipleConstructors) {
      super();
      m_multipleConstructors = multipleConstructors;
    }

    public String getName() {
        return InspectionGadgetsBundle.message("utility.class.with.public.constructor.make.quickfix", m_multipleConstructors ? 1 : 2);
    }

    public void doFix(Project project, ProblemDescriptor descriptor)
      throws IncorrectOperationException {
      final PsiElement classNameIdentifer = descriptor.getPsiElement();
      final PsiClass psiClass = (PsiClass)classNameIdentifer.getParent();
      assert psiClass != null;
      final PsiMethod[] constructors = psiClass.getConstructors();
      for (PsiMethod constructor : constructors) {
        final PsiModifierList modifierList = constructor
          .getModifierList();
        modifierList.setModifierProperty(PsiModifier.PRIVATE, true);
      }
    }
  }

  public BaseInspectionVisitor buildVisitor() {
    return new StaticClassWithPublicConstructorVisitor();
  }

  private static class StaticClassWithPublicConstructorVisitor
    extends BaseInspectionVisitor {
    public void visitClass(@NotNull PsiClass aClass) {
      // no call to super, so that it doesn't drill down to inner classes
      if (!UtilityClassUtil.isUtilityClass(aClass)) {
        return;
      }

      if (!hasPublicConstructor(aClass)) {
        return;
      }
      registerClassError(aClass);
    }
  }

  private static boolean hasPublicConstructor(PsiClass aClass) {
    final PsiMethod[] methods = aClass.getMethods();
    for (final PsiMethod method : methods) {
      if (method.isConstructor() && method
        .hasModifierProperty(PsiModifier.PUBLIC)) {
        return true;
      }
    }
    return false;
  }
}
