/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.coordinates.horizontal;

import com.top_logic.graph.layouter.GraphConstants;

/**
 * Configuration constants for the horizontal coordinate assignment.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface HorizontalCoordinatesConstants {
	/**
	 * Horizontal start coordinate.
	 */
	public static final double START_X_COORDINATE = 0;

	/**
	 * If the horizontal coordinate for the given node is not defined.
	 */
	public static final int X_UNDEFINED = -1;

	/**
	 * Minimal horizontal distance between two nodes.
	 */
	public static final double MIN_DISTANCE = GraphConstants.SCALE;
}
