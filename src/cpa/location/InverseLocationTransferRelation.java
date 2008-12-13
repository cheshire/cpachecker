package cpa.location;

import java.util.ArrayList;
import java.util.List;

import cfa.objectmodel.CFAEdge;
import cfa.objectmodel.CFANode;
import cfa.objectmodel.c.CallToReturnEdge;

import exceptions.CPATransferException;
import cpa.common.interfaces.AbstractDomain;
import cpa.common.interfaces.AbstractElement;
import cpa.common.interfaces.AbstractElementWithLocation;
import cpa.common.interfaces.TransferRelation;

/**
 * @author holzera
 *
 */
public class InverseLocationTransferRelation implements TransferRelation
{
    private final LocationDomain locationDomain;

    public InverseLocationTransferRelation (LocationDomain locationDomain)
    {
        this.locationDomain = locationDomain;
    }

    public AbstractDomain getAbstractDomain ()
    {
        return locationDomain;
    }

    public AbstractElement getAbstractSuccessor (AbstractElement element, CFAEdge cfaEdge) throws CPATransferException
    {
        LocationElement inputElement = (LocationElement) element;
        CFANode node = inputElement.getLocationNode();

        int numEnteringEdges = node.getNumEnteringEdges();

        for (int edgeIdx = 0; edgeIdx < numEnteringEdges; edgeIdx++) {
        	CFAEdge testEdge = node.getEnteringEdge(edgeIdx);

        	if (testEdge == cfaEdge) {
        		return new LocationElement(testEdge.getPredecessor());
        	}
        }

        if (node.getEnteringSummaryEdge() != null) {
        	CallToReturnEdge summaryEdge = node.getEnteringSummaryEdge();
        	return new LocationElement(summaryEdge.getPredecessor());
        }

        return locationDomain.getBottomElement();
    }

    public List<AbstractElementWithLocation> getAllAbstractSuccessors (AbstractElementWithLocation element) throws CPATransferException
    {
        CFANode node = element.getLocationNode();

        List<AbstractElementWithLocation> allSuccessors = new ArrayList<AbstractElementWithLocation> ();
        int numEnteringEdges = node.getNumEnteringEdges ();

        for (int edgeIdx = 0; edgeIdx < numEnteringEdges; edgeIdx++)
        {
            CFAEdge tempEdge = node.getEnteringEdge(edgeIdx);
            allSuccessors.add (new LocationElement(tempEdge.getPredecessor()));
        }

        return allSuccessors;
    }
}
