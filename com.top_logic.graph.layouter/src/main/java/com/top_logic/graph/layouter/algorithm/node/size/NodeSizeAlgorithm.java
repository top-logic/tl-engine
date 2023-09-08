/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.size;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Algorithm to size a {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface NodeSizeAlgorithm {
	/**
	 * @param node
	 *        {@link LayoutNode} that should be sized.
	 */
	public void size(LayoutNode node);
}
