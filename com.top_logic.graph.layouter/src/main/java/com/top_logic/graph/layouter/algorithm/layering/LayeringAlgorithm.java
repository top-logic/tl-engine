/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.layering;

import java.util.Map;

import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayer;

/**
 * Computes a layering for an acyclic {@link LayoutGraph}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface LayeringAlgorithm {

	/**
	 * Layering with unordered {@link LayoutNode}s.
	 */
	public Map<Integer, UnorderedNodeLayer> getLayering();
}
