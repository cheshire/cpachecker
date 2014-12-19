/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2011  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.guardededgeautomaton.progress.product;

import java.util.ArrayList;
import java.util.List;

import org.sosy_lab.common.Triple;
import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.interfaces.Precision;
import org.sosy_lab.cpachecker.core.interfaces.PrecisionAdjustment;
import org.sosy_lab.cpachecker.core.reachedset.UnmodifiableReachedSet;
import org.sosy_lab.cpachecker.cpa.composite.CompositePrecision;
import org.sosy_lab.cpachecker.cpa.guardededgeautomaton.productautomaton.ProductAutomatonElement;
import org.sosy_lab.cpachecker.cpa.guardededgeautomaton.progress.ProgressPrecisionAdjustment;
import org.sosy_lab.cpachecker.exceptions.CPAException;

public class ProgressProductAutomatonPrecisionAdjustment implements
    PrecisionAdjustment {

  public static final ProgressProductAutomatonPrecisionAdjustment INSTANCE = new ProgressProductAutomatonPrecisionAdjustment();

  private static final ProgressPrecisionAdjustment mSubPrecisionAdjustment = new ProgressPrecisionAdjustment();

  @Override
  public PrecisionAdjustmentResult prec(
      AbstractState pElement, Precision pPrecision,
      UnmodifiableReachedSet pElements, AbstractState fullState) throws CPAException {

    ProductAutomatonElement lElement = (ProductAutomatonElement)pElement;

    CompositePrecision lPrecision = (CompositePrecision)pPrecision;

    // TODO implement precision adjustment for composite elements
    // Reuse of CompositePrecisionAdjustment
    Action lAction;

    if (lElement.isTarget()) {
      lAction = Action.BREAK;
    }
    else {
      lAction = Action.CONTINUE;
    }

    List<AbstractState> lAdjustedElements = new ArrayList<>(lElement.getNumberOfStates());
    List<Precision> lAdjustedPrecisions = new ArrayList<>(lElement.getNumberOfStates());

    for (int lIndex = 0; lIndex < lElement.getNumberOfStates(); lIndex++) {
      AbstractState lSubelement = lElement.get(lIndex);
      Precision lSubprecision = lPrecision.get(lIndex);

      // TODO make use of reached set? or reimplement such that we adjust precision at this level?
      //Triple<AbstractState, Precision, Action> lAdjustment = mSubPrecisionAdjustment.prec(lSubelement, lSubprecision, null);
      PrecisionAdjustmentResult lAdjustment = mSubPrecisionAdjustment.prec(lSubelement, lSubprecision, null, fullState);

      if (lAdjustment.action().equals(Action.BREAK)) {
        lAction = Action.BREAK;
      }

      lAdjustedElements.add(lAdjustment.abstractState());
      lAdjustedPrecisions.add(lAdjustment.precision());
    }

    // TODO do we always throw away the wrapper ... can we optimize something?
    ProductAutomatonElement lAdjustedElement = ProductAutomatonElement.createElement(lAdjustedElements);
    CompositePrecision lAdjustedPrecision = new CompositePrecision(lAdjustedPrecisions);

    return PrecisionAdjustmentResult.create(lAdjustedElement, lAdjustedPrecision, lAction);
  }

}