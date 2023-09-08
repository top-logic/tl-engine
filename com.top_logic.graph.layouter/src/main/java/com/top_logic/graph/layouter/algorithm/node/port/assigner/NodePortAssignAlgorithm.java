/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.port.assigner;

import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;

/**
 * Algorithm to assign {@link NodePort}s for a given {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface NodePortAssignAlgorithm {
	/**
	 * @param node
	 *        {@link LayoutNode} where {@link NodePort}s should be assigned.
	 */
	public void assignNodePorts(LayoutContext context, LayoutNode node);
}
