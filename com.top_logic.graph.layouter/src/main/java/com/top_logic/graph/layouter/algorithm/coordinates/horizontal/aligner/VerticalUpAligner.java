/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner;

import java.util.List;
import java.util.Set;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * General {@link VerticalAligner} which aligns from bottom to top.
 * 
 * @see VerticalAligner
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class VerticalUpAligner extends VerticalAligner {

	/**
	 * Creates a {@link VerticalUpAligner}.
	 */
	public VerticalUpAligner(LayoutGraph graph, LayoutDirection direction) {
		super(graph, direction);
	}

	protected void getBottomNodes(List<LayoutNode> lowerNodes, LayoutNode currentNode) {
		Set<LayoutNode> bottomNodes = LayoutGraphUtil.getBottomNodes(getDirection(), currentNode);

		lowerNodes.removeIf(node -> !bottomNodes.contains(node));
	}

}
