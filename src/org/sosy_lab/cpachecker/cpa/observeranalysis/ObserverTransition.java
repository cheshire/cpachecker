/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2010  Dirk Beyer
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
package org.sosy_lab.cpachecker.cpa.observeranalysis;

import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;

import org.sosy_lab.cpachecker.cfa.objectmodel.CFAEdge;

import com.google.common.collect.ImmutableList;

import org.sosy_lab.cpachecker.cpa.observeranalysis.ObserverBoolExpr.MaybeBoolean;

/**
 * A transition in the observer automaton implements one of the {@link PATTERN_MATCHING_METHODS}.
 * This determines if the transition matches on a certain {@link CFAEdge}.
 * @author rhein
 */
class ObserverTransition {

  // The order of triggers, assertions and (more importantly) actions is preserved by the parser.
  private final List<ObserverBoolExpr> triggers;
  private final List<ObserverBoolExpr> assertions;
  private final List<ObserverActionExpr> actions;

  /**
   * When the parser instances this class it can not assign a followstate because
   * that state might not be created (forward-reference).
   * Only the name is known in the beginning and the followstate relation must be
   * resolved by calling setFollowState() when all States are known.
   */
  private final String followStateName;
  private ObserverInternalState followState = null;

  public ObserverTransition(List<ObserverBoolExpr> pTriggers, List<ObserverBoolExpr> pAssertions, List<ObserverActionExpr> pActions,
      String pFollowStateName) {
    this.triggers = ImmutableList.copyOf(pTriggers);
    this.assertions = ImmutableList.copyOf(pAssertions);
    this.actions = ImmutableList.copyOf(pActions);
    this.followStateName = pFollowStateName;
  }

  public ObserverTransition(List<ObserverBoolExpr> pTriggers,
      List<ObserverBoolExpr> pAssertions, List<ObserverActionExpr> pActions,
      ObserverInternalState pFollowState) {
    this.triggers = ImmutableList.copyOf(pTriggers);
    this.assertions = ImmutableList.copyOf(pAssertions);
    this.actions = ImmutableList.copyOf(pActions);
    this.followState = pFollowState;
    this.followStateName = pFollowState.getName();
  }

  /**
   * Resolves the follow-state relation for this transition.
   */
  public void setFollowState(List<ObserverInternalState> pAllStates) throws InvalidAutomatonException {
    if (this.followState == null) {
      for (ObserverInternalState s : pAllStates) {
        if (s.getName().equals(followStateName)) {
          this.followState = s;
          return;
        }
      }
      throw new InvalidAutomatonException("No Follow-State with name " + followStateName + " found.");
    }
  }

  /** Writes a representation of this transition (as edge) in DOT file format to the argument {@link PrintStream}.
   */
  void writeTransitionToDotFile(int sourceStateId, PrintStream out) {
    out.println(sourceStateId + " -> " + followState.getStateId() + " [label=\"" /*+ pattern */ + "\"]");
  }

  /** Determines if this Transition matches on the current State of the CPA.
   * This might return a <code>MaybeBoolean.MAYBE</code> value if the method cannot determine if the transition matches.
   * In this case more information (e.g. more AbstractElements of other CPAs) are needed.
   */
  public MaybeBoolean match(ObserverExpressionArguments pArgs) {
    for (ObserverBoolExpr trigger : triggers) {
      MaybeBoolean triggerValue = trigger.eval(pArgs);
      
      // Why this condition ? Why not MaybeBoolean.MAYBE ? 
      // rhein: MAYBE and FALSE have to be handled identically (immediate abort of the trigger checks) 
      if (triggerValue != MaybeBoolean.TRUE) {
        return triggerValue;
      }
    }
    return MaybeBoolean.TRUE;
  }

  /**
   * Checks if all assertions of this transition are fulfilled
   * in the current configuration of the automaton this method is called.
   */
  public MaybeBoolean assertionsHold(ObserverExpressionArguments pArgs) {
    for (ObserverBoolExpr assertion : assertions) {
      MaybeBoolean assertionValue = assertion.eval(pArgs);
      if (assertionValue == MaybeBoolean.MAYBE || assertionValue == MaybeBoolean.FALSE) {
        return assertionValue; // LazyEvaluation
      }
    }
    return MaybeBoolean.TRUE;
  }

  /**
   * Executes all actions of this transition in the order which is defined in the automaton definition file.
   */
  public void executeActions(ObserverExpressionArguments pArgs) {
    for (ObserverActionExpr action : actions) {
      action.execute(pArgs);
    }
    if (pArgs.getLogMessage() != null && pArgs.getLogMessage().length() > 0) {
      pArgs.getLogger().log(Level.INFO, pArgs.getLogMessage());
      pArgs.clearLogMessage();
    }
  }

  /**
   * returns null if setFollowState() was not called or no followState with appropriate name was found.
   */
  public ObserverInternalState getFollowState() {
    return followState;
  }
  
  @Override
  public String toString() {
    return this.triggers.toString();
  }
}
