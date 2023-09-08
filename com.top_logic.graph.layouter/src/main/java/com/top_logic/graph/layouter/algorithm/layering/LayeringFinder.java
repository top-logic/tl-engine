/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.layering;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.graph.layouter.algorithm.GraphLayoutAlgorithm;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayer;

/**
 * Layering finder for the given {@link LayoutGraph}.
 * 
 * @see LayeringAlgorithm
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class LayeringFinder extends GraphLayoutAlgorithm implements LayeringAlgorithm {

	/**
	 * Layering of the graph.
	 */
	protected Map<Integer, UnorderedNodeLayer> _layering = new LinkedHashMap<>();

	/**
	 * Creates a {@link LayeringFinder} for the given {@link LayoutGraph}.
	 */
	public LayeringFinder(LayoutGraph graph) {
		super(graph);
	}

}
