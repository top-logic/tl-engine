/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.edge.routing;

import com.top_logic.graph.layouter.GraphConstants;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;

/**
 * Configuration constants for the routing of {@link LayoutEdge}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface EdgeRoutingConstants {

	/**
	 * Distance between two horizontal lines.
	 */
	public final static int MIN_HORIZONTAL_LINE_DISTANCE = GraphConstants.SCALE;

	/**
	 * Offset to the lower layer.
	 */
	public final static int LAYER_OFFSET_BOTTOM = GraphConstants.SCALE * 3;

	/**
	 * Offset to the upper layer.
	 */
	public final static int LAYER_OFFSET_TOP = GraphConstants.SCALE * 3;
}
