/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.crossing;

import java.util.List;

import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;

/**
 * Algorithm to reduce the crossing between two layers of an layered {@link LayoutGraph}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface LayerCrossingReductionAlgorithm {

	/**
	 * Reduces the crossings. The given {@code fixedLayer} stays the same and a new ordering for the
	 * {@code freeLayer} is computed and returned.
	 */
	public DefaultAlternatingLayer getMinCrossingLayer(DefaultAlternatingLayer fixedLayer, List<LayoutNode> freeLayer);
}
