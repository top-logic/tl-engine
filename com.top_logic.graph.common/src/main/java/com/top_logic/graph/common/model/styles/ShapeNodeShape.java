/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles;

/**
 * This enumeration predefines some often used shapes that can be used together with
 * {@link ShapeNodeStyle} instances
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ShapeNodeShape {

	/**
	 * A rectangular shape
	 */
	RECTANGLE,

	/**
	 * A rectangular shape with rounded edges
	 */
	ROUND_RECTANGLE,

	/**
	 * An elliptical shape
	 */
	ELLIPSE,

	/**
	 * A triangular shape that points to the top
	 */
	TRIANGLE,

	/**
	 * A triangular shape that points to the bottom
	 */
	TRIANGLE2,

	/**
	 * A rectangle that is sheared in the horizontal direction to the right
	 */
	SHEARED_RECTANGLE,

	/**
	 * A rectangle that is sheared in the horizontal direction to the left
	 */
	SHEARED_RECTANGLE2,

	/**
	 * A trapezoid shape that is smaller at the bottom
	 */
	TRAPEZ,

	/**
	 * A trapezoid shape that is smaller at the top
	 */
	TRAPEZ2,

	/**
	 * A 5-star shape
	 */
	STAR5,

	/**
	 * A 6-star shape
	 */
	STAR6,

	/**
	 * An 8-star shape
	 */
	STAR8,

	/**
	 * An arrow like shape that points to the right
	 */
	FAT_ARROW,

	/**
	 * An arrow like shape that points to the left
	 */
	FAT_ARROW2,

	/**
	 * A symmetric parallelogram shape that has sloped edges
	 */
	DIAMOND,

	/**
	 * A regular eight-sided shape,
	 */
	OCTAGON,

	/**
	 * A regular six-sided shape,
	 */
	HEXAGON,

	;
}
