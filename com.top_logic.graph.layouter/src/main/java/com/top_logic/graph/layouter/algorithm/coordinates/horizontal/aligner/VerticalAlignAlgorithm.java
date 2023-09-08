/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal.aligner;

import java.util.Map;
import java.util.Set;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;

/**
 * Aligns the nodes of a layered graph in blocks. Each block is 'connected' and contains at most one
 * node per layer.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface VerticalAlignAlgorithm {

	/**
	 * Align the givenordered layered graph.
	 */
	public void verticalAlign(Map<Integer, DefaultAlternatingLayer> ordering, Set<LayoutEdge> markedEdges);
}
