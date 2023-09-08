/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * A {@link VerticalDownAligner} which aligns from left to right.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class VerticalLeftDownAligner extends VerticalDownAligner {

	/**
	 * Creates a {@link VerticalLeftDownAligner}.
	 */
	public VerticalLeftDownAligner(LayoutGraph graph, LayoutDirection direction) {
		super(graph, direction);
	}

	@Override
	public void verticalAlign(Map<Integer, DefaultAlternatingLayer> ordering, Set<LayoutEdge> markedEdges) {
		initPositions(ordering);

		int amountOfLayers = ordering.size();

		for (int i = 2; i <= amountOfLayers; i++) {
			DefaultAlternatingLayer upperLayer = ordering.get(i - 1);
			DefaultAlternatingLayer layer = ordering.get(i);

			List<LayoutNode> nodes = layer.getNodes();

			int nodesSize = nodes.size();

			int r = Integer.MIN_VALUE;

			for (int k = 0; k < nodesSize; k++) {
				List<LayoutNode> upperNodes = new LinkedList<>(upperLayer.getNodes());
				LayoutNode currentNode = nodes.get(k);

				if (!currentNode.isTargetDummy()) {
					getTopNodes(upperNodes, currentNode);

					int upperNodesSize = upperNodes.size();

					if (upperNodesSize == 0) {
						continue;
					}

					List<Integer> medianIndices = getMedianIndices(upperNodes, false);

					for (int medianIndex : medianIndices) {
						if (getAligns().get(currentNode) == currentNode) {
							LayoutNode median = upperNodes.get(medianIndex);

							Collection<LayoutEdge> edges = LayoutGraphUtil.getEdges(getDirection(), getGraph(), median, currentNode);

							if (r < getPositions().get(median) && !markedEdges.containsAll(edges)) {
								alignNode(currentNode, median);

								r = getPositions().get(median);
							}
						}
					}
				} else {
					List<LayoutNode> topNodes = new LinkedList<>(LayoutGraphUtil.getTopNodes(getDirection(), currentNode));

					LayoutNode sourceNode = topNodes.get(0);

					alignNode(currentNode, sourceNode);

					r = getPositions().get(sourceNode);
				}
			}
		}
	}

}
