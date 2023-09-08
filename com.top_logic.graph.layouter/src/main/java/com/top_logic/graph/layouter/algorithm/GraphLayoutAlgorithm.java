/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm;

import com.top_logic.graph.layouter.model.LayoutGraph;

/**
 * Abstract layout graph algorithm which is applied on a {@link LayoutGraph}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class GraphLayoutAlgorithm extends GraphAlgorithm<LayoutGraph> {

	/**
	 * Creates a layout graph algorithm for the given graph.
	 * 
	 * @param graph
	 *        {@link LayoutGraph} on which the algorithm is applied.
	 */
	public GraphLayoutAlgorithm(LayoutGraph graph) {
		super(graph);
	}

}
