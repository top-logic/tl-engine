/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal;

import com.top_logic.graph.layouter.algorithm.GraphLayoutAlgorithm;
import com.top_logic.graph.layouter.model.LayoutGraph;

/**
 * Assigner for the horizontal coordinates of an layouted graph.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class HorizontalCoordinatesAssigner extends GraphLayoutAlgorithm
		implements HorizontalCoordinateAlgorithm {

	/**
	 * Creates an {@link HorizontalCoordinatesAssigner} for the given graph.
	 */
	public HorizontalCoordinatesAssigner(LayoutGraph graph) {
		super(graph);
	}

}
