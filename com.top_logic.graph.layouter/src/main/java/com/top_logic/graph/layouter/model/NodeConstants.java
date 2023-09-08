/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model;

import com.top_logic.graph.layouter.GraphConstants;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Configuration constants for a {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface NodeConstants {
	/**
	 * Minimum node width.
	 */
	public static final double MINIMUM_WIDTH = 4 * GraphConstants.SCALE;

	/**
	 * Minimum node height.
	 */
	public static final double MINIMUM_NODE_HEIGHT = GraphConstants.SCALE;
}
