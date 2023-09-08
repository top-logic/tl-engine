/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.layering;

import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.LayoutGraph.NodeType;
import com.top_logic.graph.layouter.model.filter.FilterMarkedNode;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * {@link LongestPathLayeringFinder} for a given {@link LayoutGraph}. The sink {@link LayoutNode}s
 * are on top and the sources on the bottom.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LongestPathFromSinkLayeringFinder extends LongestPathLayeringFinder {

	/**
	 * Creates a {@link LongestPathFromSinkLayeringFinder} for the given {@link LayoutGraph}.
	 */
	public LongestPathFromSinkLayeringFinder(LayoutGraph graph) {
		super(graph);
	}

	@Override
	void sparseNormalizeLayering(LayoutGraph graph) {
		for (LayoutNode source : new LinkedHashSet<>(graph.nodes())) {
			for (LayoutEdge edge : new LinkedHashSet<>(source.outgoingEdges())) {
				LayoutNode target = edge.target();

				int targetLayer = _nodeLayerMapping.get(target);
				int sourceLayer = _nodeLayerMapping.get(source);

				if (1 < sourceLayer - targetLayer) {
					if (targetLayer + 2 == sourceLayer) {
						addDummyNode(graph, source, target, edge);
					} else {
						addDummySegment(graph, source, target, edge);
					}

					edge.remove();
				}
			}
		}
	}

	private void addDummySegment(LayoutGraph graph, LayoutNode source, LayoutNode target, LayoutEdge edge) {
		LayoutNode newNode = graph.newNode(NodeType.Q_DUMMY_NODE);
		LayoutNode sourceDummyNode = graph.add(newNode);
		LayoutNode newNode2 = graph.newNode(NodeType.P_DUMMY_NODE);
		LayoutNode targetDummyNode = graph.add(newNode2);

		assignNode(sourceDummyNode, _nodeLayerMapping.get(source) - 1);
		assignNode(targetDummyNode, _nodeLayerMapping.get(target) + 1);

		LayoutGraphUtil.createEdge(graph, source, sourceDummyNode, edge);
		createSegment(graph, sourceDummyNode, targetDummyNode, edge);
		LayoutGraphUtil.createEdge(graph, targetDummyNode, target, edge);
	}

	private void addDummyNode(LayoutGraph graph, LayoutNode source, LayoutNode target, LayoutEdge edge) {
		LayoutNode newNode = graph.newNode(NodeType.R_DUMMY_NODE);
		LayoutNode dummyNode = graph.add(newNode);

		assignNode(dummyNode, _nodeLayerMapping.get(source) - 1);

		LayoutGraphUtil.createEdge(graph, source, dummyNode, edge);
		LayoutGraphUtil.createEdge(graph, dummyNode, target, edge);
	}

	@Override
	boolean isAssignable(LayoutNode node) {
		return _allAssignedVertices.containsAll(node.outgoing());
	}

	@Override
	void initializeLayering(LinkedHashSet<LayoutNode> nodes) {
		_currentLayer = LayerConstants.FIRST_LAYER;

		LinkedHashSet<LayoutNode> sinks = LayoutGraphUtil.getSinks(nodes);

		assignNodes(sinks, _currentLayer);

		_possibleAssignableVertices = getPossibleAssignableVertices(sinks);
	}

	@Override
	Set<LayoutNode> getNotAssignedSourceNodes(LayoutNode node) {
		Filter<? super LayoutNode> filterNotMarkedNodes = FilterFactory.not(new FilterMarkedNode(_allAssignedVertices));

		return LayoutGraphUtil.getFilteredNodes(filterNotMarkedNodes, node.incoming());
	}

}
