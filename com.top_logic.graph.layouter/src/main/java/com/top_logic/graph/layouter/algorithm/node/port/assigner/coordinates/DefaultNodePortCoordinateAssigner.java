/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.port.assigner.coordinates;

import java.util.List;

import com.top_logic.graph.layouter.GraphConstants;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.algorithm.node.port.assigner.NodePortAssignAlgorithm;
import com.top_logic.graph.layouter.math.util.MathUtil;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Assign {@link NodePort} coordinates.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultNodePortCoordinateAssigner implements NodePortAssignAlgorithm {
	/**
	 * Singleton instance.
	 */
	public static final DefaultNodePortCoordinateAssigner INSTANCE = new DefaultNodePortCoordinateAssigner();

	private DefaultNodePortCoordinateAssigner() {
		// Singleton.
	}

	/**
	 * Assign all {@link NodePort}s of the given graph.
	 */
	public void assignNodePorts(LayoutContext context, LayoutGraph graph) {
		for (LayoutNode node : graph.nodes()) {
			assignNodePorts(context, node);
		}
	}

	@Override
	public void assignNodePorts(LayoutContext context, LayoutNode node) {
		setIncomingNodePortCoordinates(context, node);
		setOutgoingNodePortCoordinates(context, node);
	}

	private void setIncomingNodePortCoordinates(LayoutContext context, LayoutNode node) {
		List<NodePort> incomingPorts = node.getIncomingPorts();

		double x = node.getX();

		if (!node.isDummy()) {
			x += getIncomingNodePortsOffset(context, node, incomingPorts);
		}

		for (NodePort port : incomingPorts) {
			port.setX(x);

			x += LayoutGraphUtil.getIncomingPortGridLabelWidth(context, port, GraphConstants.SCALE,
				GraphConstants.SCALE);
		}
	}

	private double getIncomingNodePortsOffset(LayoutContext context, LayoutNode node, List<NodePort> incomingPorts) {
		double x = 0;

		if (!incomingPorts.isEmpty()) {
			double width = LayoutGraphUtil.getIncomingPortsGridWidth(context, incomingPorts);

			if (LayoutGraphUtil.getIncomingPortLabelWidth(context, incomingPorts.get(incomingPorts.size() - 1),
				0) == 0) {
				width -= GraphConstants.SCALE;
			}

			x = MathUtil.roundUpperMultiple((int) ((node.getWidth() - width) / 2.), GraphConstants.SCALE);
		}

		return x;
	}

	private void setOutgoingNodePortCoordinates(LayoutContext context, LayoutNode node) {
		List<NodePort> outgoingPorts = node.getOutgoingPorts();

		double x = node.getX();

		if (!node.isDummy()) {
			x += getOutgoingNodePortsOffset(context, node, outgoingPorts);
		}

		for (NodePort port : outgoingPorts) {
			port.setX(x);

			x += LayoutGraphUtil.getOutgoingPortGridLabelWidth(context, port, GraphConstants.SCALE,
				GraphConstants.SCALE);
		}
	}

	private double getOutgoingNodePortsOffset(LayoutContext context, LayoutNode node, List<NodePort> outgoingPorts) {
		double x = 0;

		if (!outgoingPorts.isEmpty()) {
			double width = LayoutGraphUtil.getOutgoingPortsGridWidth(context, outgoingPorts);

			if (LayoutGraphUtil.getOutgoingPortLabelWidth(context, outgoingPorts.get(outgoingPorts.size() - 1),
				0) == 0) {
				width -= GraphConstants.SCALE;
			}

			x = MathUtil.roundUpperMultiple((int) ((node.getWidth() - width) / 2.), GraphConstants.SCALE);
		}

		return x;
	}
}
