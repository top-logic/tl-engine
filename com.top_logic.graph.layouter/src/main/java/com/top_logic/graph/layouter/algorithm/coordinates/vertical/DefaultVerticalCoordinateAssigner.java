/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.vertical;

import java.util.List;
import java.util.Map;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Simple vertical coordinate assigner. The vertical coordinate of a node is determined by its
 * layer, layer step size and the max height of all nodes in this layer.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultVerticalCoordinateAssigner implements VerticalCoordinateAlgorithm {

	/**
	 * Singleton instance.
	 */
	public static final DefaultVerticalCoordinateAssigner INSTANCE = new DefaultVerticalCoordinateAssigner();

	private DefaultVerticalCoordinateAssigner() {
		// Singleton.
	}

	@Override
	public void setVerticalNodeCoordinates(Map<Integer, DefaultAlternatingLayer> ordering) {
		double currentY = VerticalCoordinatesConstants.FIRST_LAYER_COORDINATE;

		for (int layer : ordering.keySet()) {
			currentY = setVerticalCoordinates(ordering, currentY, layer);
		}
	}

	private double setVerticalCoordinates(Map<Integer, DefaultAlternatingLayer> ordering, double currentY, int layer) {
		List<LayoutNode> layerNodes = getLayerNodes(ordering, layer);
		
		layerNodes.stream().forEach(node -> {
			node.setY(currentY);
		});

		return currentY + LayoutGraphUtil.findMaxNodeHeight(layerNodes) + VerticalCoordinatesConstants.LAYER_DISTANCE;
	}

	private List<LayoutNode> getLayerNodes(Map<Integer, DefaultAlternatingLayer> ordering, int layer) {
		return ordering.get(layer).getNodes();
	}

}
