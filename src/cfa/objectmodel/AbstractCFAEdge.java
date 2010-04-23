/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker. 
 *
 *  Copyright (C) 2007-2009  Dirk Beyer and Erkan Keremoglu.
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
 *    http://www.cs.sfu.ca/~dbeyer/CPAchecker/
 */
package cfa.objectmodel;

import java.util.logging.Level;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import com.google.common.base.Preconditions;

import cpa.common.CPAchecker;


public abstract class AbstractCFAEdge implements CFAEdge
{
    private final CFANode predecessor;
    private final CFANode successor;

    private final String rawStatement;
    
    private final int lineNumber;

    public AbstractCFAEdge(String rawStatement, int lineNumber, CFANode predecessor, CFANode successor) {
      Preconditions.checkNotNull(rawStatement);
      Preconditions.checkNotNull(predecessor);
      Preconditions.checkNotNull(successor);
      this.predecessor = predecessor;
      this.successor = successor;
      this.rawStatement = rawStatement;
      this.lineNumber = lineNumber;
    }

    /**
     * This method registers adds this edge to the leaving and entering edges
     * of its predecessor and successor respectively.
     */
    public void addToCFA() {
      CFANode predecessor = getPredecessor();
      CFANode successor = getSuccessor();

      if (predecessor.hasJumpEdgeLeaving()) {
        assert predecessor.getNumLeavingEdges() == 1;
        
        // the code following a jump statement is only reachable if there is a label
        if (!(successor instanceof CFALabelNode) || isJumpEdge()) {
          CPAchecker.logger.log(Level.INFO, "Dead code detected after line " + predecessor.getLineNumber() + ": " + this.getRawStatement());
        }
      
        // don't add this edge to the CFA
        
      } else {
        if (this.isJumpEdge()) {
          
          for (int i = predecessor.getNumLeavingEdges()-1; i >= 0; i--) {
            CFAEdge otherEdge = predecessor.getLeavingEdge(i);
            CFANode otherEdgeSuccessor = otherEdge.getSuccessor();
            
            if (!(otherEdgeSuccessor instanceof CFALabelNode
                  || otherEdge.getRawStatement().isEmpty())) {
              // don't log if the dead code begins with a blank edge, this is most often a false positive
              CPAchecker.logger.log(Level.INFO, "Dead code detected after line " + predecessor.getLineNumber() + ": " + otherEdge.getRawStatement());
            }
            
            predecessor.removeLeavingEdge(otherEdge);
            otherEdgeSuccessor.removeEnteringEdge(otherEdge);
          }
        }
        
        predecessor.addLeavingEdge(this);
        successor.addEnteringEdge(this);
      }
    }

    public CFANode getPredecessor ()
    {
        return predecessor;
    }

    public CFANode getSuccessor ()
    {
        return successor;
    }

    public String getRawStatement ()
    {
        return rawStatement;
    }

    public IASTNode getRawAST() {
      return null;
    }
    
    public boolean isJumpEdge ()
    {
        return false;
    }

    @Override
    public int getLineNumber() {
      return lineNumber;
    }
    
    @Override
    public int hashCode() {
      return 31 * predecessor.hashCode() + successor.hashCode();
    }
    
    @Override
    public boolean equals (Object other)
    {
        if (!(other instanceof AbstractCFAEdge))
            return false;

        AbstractCFAEdge otherEdge = (AbstractCFAEdge) other;

        if ((otherEdge.predecessor != this.predecessor) ||
            (otherEdge.successor != this.successor))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "(" + getPredecessor() + " -{" +
                getRawStatement().replaceAll("\n", " ") +
               "}-> " + getSuccessor() + ")";
    }
}
