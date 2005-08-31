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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiSuperMethodUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.MethodInspection;
import com.siyeh.ig.fixes.RemoveModifierFix;
import org.jetbrains.annotations.NotNull;

public class ProtectedMemberInFinalClassInspection extends MethodInspection {

  public String getGroupDisplayName() {
    return GroupNames.CLASSLAYOUT_GROUP_NAME;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new ProtectedMemberInFinalClassVisitor();
  }

  public InspectionGadgetsFix buildFix(PsiElement location) {
    return new RemoveModifierFix(location);
  }

  private static class ProtectedMemberInFinalClassVisitor extends BaseInspectionVisitor {

    public void visitMethod(@NotNull PsiMethod method) {
      //no call to super, so we don't drill into anonymous classes
      if (!method.hasModifierProperty(PsiModifier.PROTECTED)) {
        return;
      }
      final PsiClass containingClass = method.getContainingClass();
      if (containingClass == null) {
        return;
      }
      if (!containingClass.hasModifierProperty(PsiModifier.FINAL)) {
        return;
      }
      if (methodOverrides(method)) {
        return;
      }
      registerModifierError(PsiModifier.PROTECTED, method);

    }


    private static boolean methodOverrides(PsiMethod meth) {
      final PsiMethod[] superMethods = PsiSuperMethodUtil.findSuperMethods(meth);
      return superMethods != null && superMethods.length != 0;
    }

    public void visitField(@NotNull PsiField field) {
      //no call to super, so we don't drill into anonymous classes
      if (!field.hasModifierProperty(PsiModifier.PROTECTED)) {
        return;
      }
      final PsiClass containingClass = field.getContainingClass();
      if (containingClass == null) {
        return;
      }
      if (!containingClass.hasModifierProperty(PsiModifier.FINAL)) {
        return;
      }
      registerModifierError(PsiModifier.PROTECTED, field);
    }

  }
}
