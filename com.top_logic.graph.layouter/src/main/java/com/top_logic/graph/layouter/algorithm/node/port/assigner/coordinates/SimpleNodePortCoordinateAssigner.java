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
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;

/**
 * Simple {@link NodePortAssignAlgorithm} that distributes ports evenly across the node width
 * without considering label widths. Each port occupies one grid unit.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F&ouml;rster</a>
 */
public class SimpleNodePortCoordinateAssigner implements NodePortAssignAlgorithm {

	/**
	 * Singleton instance.
	 */
	public static final SimpleNodePortCoordinateAssigner INSTANCE = new SimpleNodePortCoordinateAssigner();

	private SimpleNodePortCoordinateAssigner() {
		// Singleton.
	}

	@Override
	public void assignNodePorts(LayoutContext context, LayoutNode node) {
		setPortCoordinates(node, node.getIncomingPorts());
		setPortCoordinates(node, node.getOutgoingPorts());
	}

	private void setPortCoordinates(LayoutNode node, List<NodePort> ports) {
		if (ports.isEmpty()) {
			return;
		}

		double portsWidth = ports.size() * GraphConstants.SCALE;
		double offset = node.isDummy() ? 0
			: MathUtil.roundUpperMultiple((int) ((node.getWidth() - portsWidth) / 2.), GraphConstants.SCALE);

		double x = node.getX() + offset;

		for (NodePort port : ports) {
			port.setX(x);
			x += GraphConstants.SCALE;
		}
	}

}
