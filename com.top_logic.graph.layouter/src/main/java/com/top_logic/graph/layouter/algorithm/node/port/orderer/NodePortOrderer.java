/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.port.orderer;

import java.util.Comparator;
import java.util.List;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Order {@link NodePort}s for {@link LayoutNode}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NodePortOrderer implements NodePortOrderingAlgorithm {

	private Comparator<NodePort> _topPortComparator;

	private Comparator<NodePort> _bottomPortComparator;

	private LayoutDirection _direction;

	/**
	 * Creates an {@link NodePortOrderer} with the given {@link Comparator}s for incoming and
	 * outgoing {@link NodePort}s.
	 */
	public NodePortOrderer(LayoutDirection direction, Comparator<NodePort> topPortComparator,
			Comparator<NodePort> bottomPortComparator) {
		_direction = direction;

		_topPortComparator = topPortComparator;
		_bottomPortComparator = bottomPortComparator;
	}

	@Override
	public void orderNodePorts(LayoutNode node) {
		orderTopNodePorts(node);
		orderBottomNodePorts(node);
	}

	/**
	 * Order {@link NodePort}s for the given {@link LayoutGraph}, i.e. for all {@link LayoutNode}s.
	 */
	public void orderNodePorts(LayoutGraph graph) {
		for (LayoutNode node : graph.nodes()) {
			orderNodePorts(node);
		}
	}

	private void orderBottomNodePorts(LayoutNode node) {
		List<NodePort> bottomPorts = LayoutGraphUtil.getBottomNodePorts(_direction, node);

		bottomPorts.sort(_bottomPortComparator);

		LayoutGraphUtil.setBottomNodePorts(_direction, node, bottomPorts);
	}

	private void orderTopNodePorts(LayoutNode node) {
		List<NodePort> topPorts = LayoutGraphUtil.getTopNodePorts(_direction, node);

		topPorts.sort(_topPortComparator);

		LayoutGraphUtil.setTopNodePorts(_direction, node, topPorts);
	}
}
