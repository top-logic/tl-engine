/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines;

/**
 * Intersection status for two items.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public enum IntersectionStatus {

	/**
	 * No intersection.
	 */
	NONE,

	/**
	 * An item is partial contained in the other item.
	 */
	PARTIAL,

	/**
	 * An item is strict contained in the other item.
	 */
	FULL

}