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
package com.siyeh.ig.serialization;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTypeParameter;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.psiutils.SerializationUtils;
import com.siyeh.ig.ui.SingleCheckboxOptionsPanel;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SerializableHasSerializationMethodsInspection extends ClassInspection {

  /**
   * @noinspection PublicField
   */
  public boolean m_ignoreSerializableDueToInheritance = true;

  public String getGroupDisplayName() {
    return GroupNames.SERIALIZATION_GROUP_NAME;
  }

  public String buildErrorString(PsiElement location) {
    final PsiClass aClass = (PsiClass)location.getParent();
    assert aClass != null;
    final boolean hasReadObject = SerializationUtils.hasReadObject(aClass);
    final boolean hasWriteObject = SerializationUtils.hasWriteObject(aClass);

    if (!hasReadObject && !hasWriteObject) {
      return InspectionGadgetsBundle.message("serializable.has.serialization.methods.problem.descriptor");
    }
    else if (hasReadObject) {
      return InspectionGadgetsBundle.message("serializable.has.serialization.methods.problem.descriptor1");
    }
    else {
      return InspectionGadgetsBundle.message("serializable.has.serialization.methods.problem.descriptor2");
    }
  }

  public JComponent createOptionsPanel() {
    return new SingleCheckboxOptionsPanel(InspectionGadgetsBundle.message("serializable.has.serialization.methods.ignore.option"),
                                          this, "m_ignoreSerializableDueToInheritance");
  }

  public BaseInspectionVisitor buildVisitor() {
    return new SerializableDefinesMethodsVisitor();
  }

  private class SerializableDefinesMethodsVisitor extends BaseInspectionVisitor {


    public void visitClass(@NotNull PsiClass aClass) {
      // no call to super, so it doesn't drill down
      if (aClass.isInterface() || aClass.isAnnotationType() ||
          aClass.isEnum()) {
        return;
      }
      if (aClass instanceof PsiTypeParameter ||
          aClass instanceof PsiAnonymousClass) {
        return;
      }
      if (m_ignoreSerializableDueToInheritance) {
        if (!SerializationUtils.isDirectlySerializable(aClass)) {
          return;
        }
      }
      else {
        if (!SerializationUtils.isSerializable(aClass)) {
          return;
        }
      }
      final boolean hasReadObject = SerializationUtils.hasReadObject(aClass);
      final boolean hasWriteObject = SerializationUtils.hasWriteObject(aClass);

      if (hasWriteObject && hasReadObject) {
        return;
      }
      registerClassError(aClass);
    }

  }
}
