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
 * A {@link VerticalUpAligner} which aligns from right to left.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class VerticalRightUpAligner extends VerticalUpAligner {

	/**
	 * Creates a {@link VerticalRightUpAligner}.
	 */
	public VerticalRightUpAligner(LayoutGraph graph, LayoutDirection direction) {
		super(graph, direction);
	}

	@Override
	public void verticalAlign(Map<Integer, DefaultAlternatingLayer> ordering, Set<LayoutEdge> markedEdges) {
		initPositions(ordering);

		int amountOfLayers = ordering.size();

		for (int i = amountOfLayers - 1; i >= 1; i--) {
			DefaultAlternatingLayer layer = ordering.get(i);
			DefaultAlternatingLayer lowerLayer = ordering.get(i + 1);

			List<LayoutNode> nodes = layer.getNodes();

			int nodesSize = nodes.size();

			int r = Integer.MAX_VALUE;

			for (int k = nodesSize - 1; k >= 0; k--) {
				List<LayoutNode> lowerNodes = new LinkedList<>(lowerLayer.getNodes());
				LayoutNode currentNode = nodes.get(k);

				if (!currentNode.isSourceDummy()) {
					getBottomNodes(lowerNodes, currentNode);

					int lowerNodesSize = lowerNodes.size();

					if (lowerNodesSize == 0) {
						continue;
					}

					List<Integer> medianIndices = getMedianIndices(lowerNodes, true);

					for (int medianIndex : medianIndices) {
						if (getAligns().get(currentNode) == currentNode) {
							LayoutNode median = lowerNodes.get(medianIndex);

							Collection<LayoutEdge> edges =
								LayoutGraphUtil.getEdges(getDirection(), getGraph(), currentNode, median);

							if (r > getPositions().get(median) && !markedEdges.containsAll(edges)) {
								alignNode(currentNode, median);

								r = getPositions().get(median);
							}
						}
					}
				} else {
					List<LayoutNode> bottomNodes =
						new LinkedList<>(LayoutGraphUtil.getBottomNodes(getDirection(), currentNode));

					LayoutNode target = bottomNodes.get(0);

					alignNode(currentNode, target);

					r = getPositions().get(target);
				}
			}
		}
	}

}
