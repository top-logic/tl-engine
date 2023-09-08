/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.port.orderer;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;

/**
 * Algorithm to order {@link NodePort} for a {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface NodePortOrderingAlgorithm {
	/**
	 * Order {@link NodePort}s for the given {@link LayoutNode}.
	 */
	public void orderNodePorts(LayoutNode node);
}
