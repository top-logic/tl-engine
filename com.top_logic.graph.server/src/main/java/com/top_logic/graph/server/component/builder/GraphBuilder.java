/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.component.builder;

import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.Node;

/**
 * Partial algorithm for building a {@link GraphModel} in an application object context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphBuilder extends GraphChangeHandler {

	/**
	 * Builds {@link Node}s or {@link Edge}s in the given {@link GraphModel}.
	 * 
	 * @param graph
	 *        The graph to build.
	 */
	void populateGraph(GraphModel graph);

}