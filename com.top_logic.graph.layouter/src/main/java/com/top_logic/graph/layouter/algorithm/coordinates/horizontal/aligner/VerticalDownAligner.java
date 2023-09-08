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
 * General {@link VerticalAligner} which aligns from top to down.
 * 
 * @see VerticalAligner
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class VerticalDownAligner extends VerticalAligner {

	/**
	 * Creates a {@link VerticalDownAligner}.
	 */
	public VerticalDownAligner(LayoutGraph graph, LayoutDirection direction) {
		super(graph, direction);
	}

	protected void getTopNodes(List<LayoutNode> upperNodes, LayoutNode currentNode) {
		Set<LayoutNode> topNodes = LayoutGraphUtil.getTopNodes(getDirection(), currentNode);

		upperNodes.removeIf(node -> !topNodes.contains(node));
	}

}
