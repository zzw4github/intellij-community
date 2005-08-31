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
import com.intellij.psi.jsp.JspFile;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import org.jetbrains.annotations.NotNull;

public class EmptyClassInspection extends ClassInspection {

  public String getGroupDisplayName() {
    return GroupNames.CLASSLAYOUT_GROUP_NAME;
  }

  public BaseInspectionVisitor buildVisitor() {
    return new EmptyClassVisitor();
  }

  private static class EmptyClassVisitor extends BaseInspectionVisitor {

    public void visitClass(@NotNull PsiClass aClass) {
      //don't call super, to prevent drilldown

      if (aClass.getContainingFile() instanceof JspFile) {
        return;
      }
      if (aClass.isInterface() || aClass.isEnum() || aClass.isAnnotationType()) {
        return;
      }
      if (aClass instanceof PsiTypeParameter ||
          aClass instanceof PsiAnonymousClass) {
        return;
      }
      final PsiMethod[] constructors = aClass.getConstructors();
      if (constructors != null && constructors.length > 0) {
        return;
      }
      final PsiMethod[] methods = aClass.getMethods();
      if (methods != null && methods.length > 0) {
        return;
      }
      final PsiField[] fields = aClass.getFields();
      if (fields != null && fields.length > 0) {
        return;
      }
      registerClassError(aClass);
    }
  }
}