/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal;

import java.util.Map;
import java.util.Set;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;

/**
 * Algorithm to set the horizontal coordinates for the given layering.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface HorizontalCoordinateAlgorithm {
	/**
	 * Sets the horizontal coordinates of all nodes of the given layering.
	 * 
	 * @param ordering
	 *        Ordered layering of a graph.
	 * @param type1Edges
	 *        Edges that could be neglected for the horizontal coordinate assignment.
	 */
	void setHorizontalNodeCoordinates(Map<Integer, DefaultAlternatingLayer> ordering, Set<LayoutEdge> type1Edges);
}
