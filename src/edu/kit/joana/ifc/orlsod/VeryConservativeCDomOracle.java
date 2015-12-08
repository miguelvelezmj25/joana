package edu.kit.joana.ifc.orlsod;

import edu.kit.joana.ifc.sdg.graph.SDGNode;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.CFG;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.VirtualNode;

/**
 * This cdom oracle chooses the start node of the given cfg as cdom of
 * any two nodes. When plugged into an ORLSOD algorithm, this yields the
 * RLSOD algorithm.
 * @author Martin Mohr&lt;martin.mohr@kit.edu&gt;
 *
 */
public class VeryConservativeCDomOracle implements ICDomOracle {
	private CFG icfg;

	public VeryConservativeCDomOracle(CFG icfg) {
		this.icfg = icfg;
	}

	@Override
	public VirtualNode cdom(SDGNode n1, int threadN1, SDGNode n2, int threadN2) {
		return new VirtualNode(icfg.getRoot(), icfg.getRoot().getThreadNumbers()[0]);
	}
}
