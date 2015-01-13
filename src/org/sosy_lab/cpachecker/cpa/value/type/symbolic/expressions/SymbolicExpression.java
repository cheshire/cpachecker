/*
 * CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2014  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.cpa.value.type.symbolic.expressions;

import org.sosy_lab.cpachecker.cfa.types.Type;
import org.sosy_lab.cpachecker.cfa.types.c.CType;
import org.sosy_lab.cpachecker.cpa.constraints.constraint.Constraint;
import org.sosy_lab.cpachecker.cpa.value.type.NumericValue;
import org.sosy_lab.cpachecker.cpa.value.type.symbolic.SymbolicValue;
import org.sosy_lab.cpachecker.cpa.value.type.symbolic.SymbolicValueVisitor;

/**
 * An expression used to describe one side of a {@link Constraint}.
 */
public abstract class SymbolicExpression implements SymbolicValue {

  /**
   * Accepts the given {@link SymbolicExpressionVisitor}.
   *
   * @param pVisitor the visitor to accept
   * @param <VisitorReturnT> the return type of the visitor's specific <code>visit</code> method
   * @return the value returned by the visitor's <code>visit</code> method
   */
  public abstract <VisitorReturnT> VisitorReturnT accept(SymbolicValueVisitor<VisitorReturnT> pVisitor);

  /**
   * Returns the expression type of this <code>ConstraintExpression</code>.
   *
   * @return the expression type of this <code>ConstraintExpression</code>
   */
  public abstract Type getType();

  /**
   * Returns a copy of this <code>ConstraintExpression</code> object with the given expression type.
   *
   * @param pType the expression type of the returned object
   */
  public abstract SymbolicExpression copyWithType(Type pType);

  /**
   * Returns whether this <code>ConstraintExpression</code> is always true and does only contain explicit values.
   *
   * @return <code>true</code> if this <code>ConstraintExpression</code> is always true and does only contain explicit
   *    values, <code>false</code> otherwise
   */
  public abstract boolean isTrivial();

  @Override
  public boolean isNumericValue() {
    return false;
  }

  @Override
  public boolean isUnknown() {
    return false;
  }

  @Override
  public boolean isExplicitlyKnown() {
    return false;
  }

  @Override
  public NumericValue asNumericValue() {
    throw new UnsupportedOperationException("Symbolic expressions can't be expressed as numeric values");
  }

  @Override
  public Long asLong(CType type) {
    throw new UnsupportedOperationException("Symbolic expressions can't be expressed as numeric values");
  }
}