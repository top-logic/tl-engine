/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.layout;

/**
 * Enumeration of the possible label positions
 * 
 * @see InteriorLabelLayout#getPosition()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum InteriorLayoutPosition {

	/**
	 * Encodes a position at the top side of the node interior
	 */
	NORTH,

	/**
	 * Encodes a position at the right side of the node interior
	 */
	EAST,

	/**
	 * Encodes a position at the bottom side of the node interior
	 */
	SOUTH,

	/**
	 * Encodes a position at the left side of the node interior
	 */
	WEST,

	/**
	 * Encodes a position at the upper right corner of the node interior
	 */
	NORTH_EAST,

	/**
	 * Encodes a position at the lower right corner of the node interior
	 */
	SOUTH_EAST,

	/**
	 * Encodes a position at the upper left corner of the node interior
	 */
	NORTH_WEST,

	/**
	 * Encodes a position at the lower left corner of the node interior
	 */
	SOUTH_WEST,

	/**
	 * Encodes a position at the center of the node interior
	 */
	CENTER,

	;
}
