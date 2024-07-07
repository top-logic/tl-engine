/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.math;

/**
 * Immutable vector in two-dimensional space.
 * 
 * @see #vec(double, double)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Vec {

	/**
	 * Creates a {@link Vec} with the given X and Y coordinates.
	 */
	public static Vec vec(double x, double y) {
		return new Vec(x, y);
	}

	private final double _x;

	private final double _y;

	/**
	 * Creates a {@link Vec}.
	 */
	private Vec(double x, double y) {
		_x = x;
		_y = y;
	}

	/**
	 * The X coordinate.
	 */
	public double getX() {
		return _x;
	}

	/**
	 * The Y coordinate.
	 */
	public double getY() {
		return _y;
	}

	/**
	 * Adds the given {@link Vec} to this {@link Vec} and returns the result.
	 */
	public Vec plus(Vec other) {
		return plus(other.getX(), other.getY());
	}

	/**
	 * Adds the vector defined by the given coordinates to this {@link Vec} and returns the result.
	 */
	public Vec plus(double x, double y) {
		return vec(getX() + x, getY() + y);
	}

	/**
	 * Subtracts the given {@link Vec} from this {@link Vec} and returns the result.
	 */
	public Vec minus(Vec other) {
		return minus(other.getX(), other.getY());
	}

	/**
	 * Subtracts the vector defined by the given coordinates from this {@link Vec} and returns the
	 * result.
	 */
	public Vec minus(double x, double y) {
		return vec(getX() - x, getY() - y);
	}

	/**
	 * The length of this {@link Vec} using Euclidean norm.
	 */
	public double length() {
		return Math.sqrt(_x * _x + _y * _y);
	}

}
