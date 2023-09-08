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
 * {@link LongestPathLayeringFinder} for a given {@link LayoutGraph}. The source {@link LayoutNode}s
 * are on top and the sinks on the bottom.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LongestPathFromSourceLayeringFinder extends LongestPathLayeringFinder {

	/**
	 * Creates a {@link LongestPathFromSourceLayeringFinder} for the given {@link LayoutGraph}.
	 */
	public LongestPathFromSourceLayeringFinder(LayoutGraph graph) {
		super(graph);
	}

	@Override
	void sparseNormalizeLayering(LayoutGraph graph) {
		for (LayoutNode source : new LinkedHashSet<>(graph.nodes())) {
			for (LayoutEdge edge : new LinkedHashSet<>(source.outgoingEdges())) {
				LayoutNode target = edge.target();

				int targetLayer = _nodeLayerMapping.get(target);
				int sourceLayer = _nodeLayerMapping.get(source);

				if (1 < targetLayer - sourceLayer) {
					if (targetLayer == sourceLayer + 2) {
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
		LayoutNode sourceDummyNode = graph.add(graph.newNode(NodeType.P_DUMMY_NODE));
		LayoutNode targetDummyNode = graph.add(graph.newNode(NodeType.Q_DUMMY_NODE));

		assignNode(sourceDummyNode, _nodeLayerMapping.get(source) + 1);
		assignNode(targetDummyNode, _nodeLayerMapping.get(target) - 1);

		LayoutGraphUtil.createEdge(graph, source, sourceDummyNode, edge);
		createSegment(graph, sourceDummyNode, targetDummyNode, edge);
		LayoutGraphUtil.createEdge(graph, targetDummyNode, target, edge);
	}

	private void addDummyNode(LayoutGraph graph, LayoutNode source, LayoutNode target, LayoutEdge edge) {
		LayoutNode dummyNode = graph.add(graph.newNode(NodeType.R_DUMMY_NODE));

		assignNode(dummyNode, _nodeLayerMapping.get(source) + 1);

		LayoutGraphUtil.createEdge(graph, source, dummyNode, edge);
		LayoutGraphUtil.createEdge(graph, dummyNode, target, edge);
	}

	@Override
	boolean isAssignable(LayoutNode node) {
		return _allAssignedVertices.containsAll(node.incoming());
	}

	@Override
	void initializeLayering(LinkedHashSet<LayoutNode> nodes) {
		_currentLayer = LayerConstants.FIRST_LAYER;

		LinkedHashSet<LayoutNode> sources = LayoutGraphUtil.getSources(nodes);

		assignNodes(sources, _currentLayer);

		_possibleAssignableVertices = getPossibleAssignableVertices(sources);
	}

	@Override
	Set<LayoutNode> getNotAssignedSourceNodes(LayoutNode node) {
		Filter<? super LayoutNode> filterNotMarkedNodes = FilterFactory.not(new FilterMarkedNode(_allAssignedVertices));

		return LayoutGraphUtil.getFilteredNodes(filterNotMarkedNodes, node.outgoing());
	}

}
