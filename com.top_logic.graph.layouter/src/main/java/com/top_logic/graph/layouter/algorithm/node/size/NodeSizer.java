/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.size;

import java.util.function.Function;

import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * General sizer for a {@link LayoutNode}. Sets the width and height of a node.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NodeSizer implements NodeSizeAlgorithm {

	private Function<LayoutNode, Double> _widthSizer;

	private Function<LayoutNode, Double> _heightSizer;

	/**
	 * Creates a {@link NodeSizer} with the given width and height sizer.
	 */
	public NodeSizer(Function<LayoutNode, Double> widthSizer, Function<LayoutNode, Double> heightSizer) {
		_widthSizer = widthSizer;
		_heightSizer = heightSizer;
	}

	@Override
	public void size(LayoutNode node) {
		node.setWidth(_widthSizer.apply(node));

		node.setHeight(_heightSizer.apply(node));
	}

	/**
	 * Size all {@link LayoutNode} for the given graph.
	 */
	public void size(LayoutGraph graph) {
		for (LayoutNode node : graph.nodes()) {
			size(node);
		}
	}

}
