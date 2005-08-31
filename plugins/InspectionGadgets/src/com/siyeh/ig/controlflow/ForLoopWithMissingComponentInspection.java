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
package com.siyeh.ig.controlflow;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.StatementInspection;
import com.siyeh.ig.StatementInspectionVisitor;
import com.siyeh.InspectionGadgetsBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

public class ForLoopWithMissingComponentInspection extends StatementInspection {

  public String getGroupDisplayName() {
    return GroupNames.CONTROL_FLOW_GROUP_NAME;
  }

  public String buildErrorString(PsiElement location) {
    final PsiJavaToken forToken = (PsiJavaToken)location;
    final PsiForStatement forStatement = (PsiForStatement)forToken.getParent();
    boolean hasInitializer = false;
    boolean hasCondition = false;
    boolean hasUpdate = false;
    int count = 0;
    if (!hasInitializer(forStatement)) {
      hasInitializer = true;
      count++;
    }
    if (!hasCondition(forStatement)) {
      hasCondition = true;
      count++;
    }
    if (!hasUpdate(forStatement)) {
      hasUpdate = true;
      count++;
    }
    if (count == 1) {
      if (hasInitializer){
        return InspectionGadgetsBundle.message("for.loop.with.missing.component.problem.descriptor1");
      } else if (hasCondition){
        return InspectionGadgetsBundle.message("for.loop.with.missing.component.problem.descriptor2");
      } else {
        return InspectionGadgetsBundle.message("for.loop.with.missing.component.problem.descriptor3");
      }
    }
    else if (count == 2) {
      if (hasInitializer){
        if (hasCondition){
          return InspectionGadgetsBundle.message("for.loop.with.missing.component.problem.descriptor4");
        } else {
          return InspectionGadgetsBundle.message("for.loop.with.missing.component.problem.descriptor5");
        }
      } else {
        return InspectionGadgetsBundle.message("for.loop.with.missing.component.problem.descriptor6");
      }
    }
    else {
      return InspectionGadgetsBundle.message("for.loop.with.missing.component.problem.descriptor7");
    }

  }

  public BaseInspectionVisitor buildVisitor() {
    return new ForLoopWithMissingComponentVisitor();
  }

  private static class ForLoopWithMissingComponentVisitor extends StatementInspectionVisitor {

    public void visitForStatement(@NotNull PsiForStatement statement) {
      super.visitForStatement(statement);

      if (hasCondition(statement)
          && hasInitializer(statement)
          && hasUpdate(statement)) {
        return;
      }
      registerStatementError(statement);
    }
  }

  private static boolean hasCondition(PsiForStatement statement) {
    return statement.getCondition() != null;
  }

  private static boolean hasInitializer(PsiForStatement statement) {
    final PsiStatement initialization = statement.getInitialization();
    return initialization != null && !(initialization instanceof PsiEmptyStatement);
  }

  private static boolean hasUpdate(PsiForStatement statement) {
    final PsiStatement update = statement.getUpdate();
    return update != null && !(update instanceof PsiEmptyStatement);
  }
}
