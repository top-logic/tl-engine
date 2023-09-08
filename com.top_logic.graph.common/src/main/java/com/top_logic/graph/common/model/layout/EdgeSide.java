/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.layout;

/**
 * Placement specifier for labels.
 * 
 * @see EdgeSegmentLabelLayout
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum EdgeSide {

	/**
	 * On the path of the edge.
	 */
	ON,

	/**
	 * On the left hand side of the edge path if looking from the source node into the direction of
	 * the target node.
	 */
	LEFT,

	/**
	 * On the right hand side of the edge path if looking from the source node into the direction of
	 * the target node.
	 */
	RIGHT,

	/**
	 * Above the edge (in geometric sense).
	 */
	ABOVE,

	/**
	 * Below the edge (in geometric sense).
	 */
	BELOW,

	;
}
