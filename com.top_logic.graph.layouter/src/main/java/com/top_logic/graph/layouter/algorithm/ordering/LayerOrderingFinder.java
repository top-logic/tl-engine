/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.ordering;

import java.util.Set;

import com.top_logic.graph.layouter.algorithm.GraphLayoutAlgorithm;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Finds an {@link LayoutNode} ordering for the given layered {@link LayoutGraph}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class LayerOrderingFinder extends GraphLayoutAlgorithm implements LayerOrderingAlgorithm {

	/**
	 * Creates a {@link LayerOrderingFinder} for the given {@link LayoutGraph}.
	 */
	public LayerOrderingFinder(LayoutGraph graph) {
		super(graph);
	}

	/**
	 * All non segment {@link LayoutEdge}s that cross a {@link LayoutEdge} segment.
	 */
	public abstract Set<LayoutEdge> getCrossingType1Edges();

}
