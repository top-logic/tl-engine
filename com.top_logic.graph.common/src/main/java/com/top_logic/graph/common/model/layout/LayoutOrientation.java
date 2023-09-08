/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.layout;

/**
 * Preferred orientation of graph layouts.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum LayoutOrientation {
	/**
	 * Draw edges from top to bottom.
	 */
	TOP_TO_BOTTOM,

	/**
	 * Draw edges from left to right.
	 */
	LEFT_TO_RIGHT,

	/**
	 * Draw edges from right to left.
	 */
	RIGHT_TO_LEFT,

	/**
	 * Draw edges from bottom to top.
	 */
	BOTTOM_TO_TOP;
}
