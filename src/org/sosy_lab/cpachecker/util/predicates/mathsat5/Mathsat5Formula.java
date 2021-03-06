/*
 *  CPAchecker is a tool for configurable software verification.
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
package org.sosy_lab.cpachecker.util.predicates.mathsat5;

import org.sosy_lab.cpachecker.util.predicates.interfaces.BitvectorFormula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.BooleanFormula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.FloatingPointFormula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.Formula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.NumeralFormula.IntegerFormula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.NumeralFormula.RationalFormula;

abstract class Mathsat5Formula implements Formula {

  private final long msatTerm;

  public Mathsat5Formula(long term) {
    this.msatTerm = term;
  }

  @Override
  public String toString() {
    return Mathsat5NativeApi.msat_term_repr(msatTerm);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Mathsat5Formula)) {return false;}
    return msatTerm == ((Mathsat5Formula)o).msatTerm;
  }

  @Override
  public int hashCode() {
    return (int) msatTerm;
  }

  long getTerm() {
    return msatTerm;
  }
}

class Mathsat5BitvectorFormula extends Mathsat5Formula implements BitvectorFormula {
  public Mathsat5BitvectorFormula(long pTerm) {
    super(pTerm);
  }
}

class Mathsat5FloatingPointFormula extends Mathsat5Formula implements FloatingPointFormula {
  public Mathsat5FloatingPointFormula(long pTerm) {
    super(pTerm);
  }
}

class Mathsat5IntegerFormula extends Mathsat5Formula implements IntegerFormula {
  public Mathsat5IntegerFormula(long pTerm) {
    super(pTerm);
  }
}

class Mathsat5RationalFormula extends Mathsat5Formula implements RationalFormula {
  public Mathsat5RationalFormula(long pTerm) {
    super(pTerm);
  }
}

class Mathsat5BooleanFormula extends Mathsat5Formula implements BooleanFormula {
  public Mathsat5BooleanFormula(long pTerm) {
    super(pTerm);
  }
}
