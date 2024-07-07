/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.math;

import static com.top_logic.graphic.blocks.math.Vec.*;

/**
 * Description of a rectangular region in Euclidean space.
 * 
 * @see #rect(double, double, double, double)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Rect {

	/**
	 * Creates a {@link Rect} from the given coordinate pairs.
	 *
	 * @param x1
	 *        See {@link #getX1()}.
	 * @param y1
	 *        See {@link #getY1()}.
	 * @param x2
	 *        See {@link #getX2()}.
	 * @param y2
	 *        See {@link #getY2()}.
	 */
	public static Rect rect(double x1, double y1, double x2, double y2) {
		assert x1 <= x2;
		assert y1 <= y2;
		return new Rect(x1, y1, x2, y2);
	}

	private final double _x1;

	private final double _y1;

	private final double _x2;

	private final double _y2;

	/**
	 * Creates a {@link Rect}.
	 */
	private Rect(double x1, double y1, double x2, double y2) {
		_x1 = x1;
		_y1 = y1;
		_x2 = x2;
		_y2 = y2;
	}

	/**
	 * X coordinate of the top-left corner.
	 */
	public double getX1() {
		return _x1;
	}

	/**
	 * Y coordinate of the top-left corner.
	 */
	public double getY1() {
		return _y1;
	}

	/**
	 * X coordinate of the bottom-right corner.
	 */
	public double getX2() {
		return _x2;
	}

	/**
	 * Y coordinate of the bottom-right corner.
	 */
	public double getY2() {
		return _y2;
	}

	/**
	 * Vector pointing to the top-left corner.
	 */
	public Vec getTopLeft() {
		return vec(_x1, _y1);
	}

	/**
	 * {@link Vec} pointing to the bottom right corner.
	 */
	public Vec getBottomRight() {
		return vec(_x2, _y2);
	}

	/**
	 * Whether this {@link Rect} and the other one has some common points.
	 */
	public boolean intersects(Rect other) {
		return Math.max(getX1(), other.getX1()) <= Math.min(getX2(), other.getX2()) &&
			Math.max(getY1(), other.getY1()) <= Math.min(getY2(), other.getY2());
	}

	/**
	 * Whether this {@link Rect} contains the point pointed to by the given {@link Vec}.
	 */
	public boolean contains(Vec v) {
		return contains(v.getX(), v.getY());
	}

	/**
	 * Whether this {@link Rect} contains the point described by the given coordinate pair.
	 */
	public boolean contains(double x, double y) {
		return getX1() <= x && x <= getX2() &&
			getY1() <= y && y <= getY2();
	}

}
