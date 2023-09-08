/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.edge.routing;

import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;

/**
 * Algorithm to route {@link LayoutEdge} of an {@link LayoutGraph}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface EdgeRoutingAlgorithm {

	/**
	 * Routes {@link LayoutEdge} for the given {@link LayoutGraph}.
	 */
	void route(LayoutGraph graph);

}
