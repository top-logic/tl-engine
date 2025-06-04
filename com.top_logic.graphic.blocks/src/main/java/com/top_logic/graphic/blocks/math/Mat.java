/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.math;

import java.util.Objects;

/**
 * A 2D transformation matrix.
 * 
 * <pre>
 * <code>
 * [a c e]
 * [b d f]
 * [0 0 1]
 * </code>
 * </pre>
 */
public class Mat {

	/**
	 * The identity transformation.
	 */
	public static final Mat IDENTITY = Mat.scale(1, 1);

	/**
	 * Mirror along the Y axis.
	 */
	public static final Mat FLIP_X = Mat.scale(-1, 1);

	/**
	 * Mirror along the X axis.
	 */
	public static final Mat FLIP_Y = Mat.scale(1, -1);

	/**
	 * Rotate 90 degree around the origin counter-clockwise.
	 */
	public static final Mat ROT_90 = Mat.mat(0, 1, -1, 0, 0, 0);

	/**
	 * Rotate 180 degree around the origin.
	 */
	public static final Mat ROT_180 = ROT_90.apply(ROT_90);

	/**
	 * Rotate 270 degree around the origin counter-clockwise.
	 */
	public static final Mat ROT_270 = ROT_180.apply(ROT_90);

	/**
	 * Mirror along the Y axis and then rotate 90 degree around the origin counter-clockwise.
	 */
	public static final Mat FLIP_X_ROT_90 = ROT_90.apply(FLIP_X);

	/**
	 * Mirror along the Y axis and then rotate 270 degree around the origin counter-clockwise.
	 */
	public static final Mat FLIP_X_ROT_270 = ROT_270.apply(FLIP_X);

	private static final double EPSILON = 1E-10;

	/**
	 * Creates a translation transform.
	 */
	public static Mat translate(double dx, double dy) {
		return mat(1, 0, 0, 1, dx, dy);
	}

	/**
	 * Creates a scaling transform.
	 */
	public static Mat scale(double sx, double sy) {
		return mat(sx, 0, 0, sy, 0, 0);
	}

	/**
	 * Creates a matrix.
	 * 
	 * <pre>
	 * <code>
	 * [a c e]
	 * [b d f]
	 * [0 0 1]
	 * </code>
	 * </pre>
	 */
	public static Mat mat(double a, double b, double c, double d, double e, double f) {
		return new Mat(a, b, c, d, e, f);
	}

	private final double _a;

	private final double _b;

	private final double _c;

	private final double _d;

	private final double _e;

	private final double _f;

	/**
	 * Creates a {@link Mat}.
	 */
	private Mat(double a, double b, double c, double d, double e, double f) {
		super();
		_a = a;
		_b = b;
		_c = c;
		_d = d;
		_e = e;
		_f = f;
	}

	/**
	 * @see Mat
	 */
	public double a() {
		return _a;
	}

	/**
	 * @see Mat
	 */
	public double b() {
		return _b;
	}

	/**
	 * @see Mat
	 */
	public double c() {
		return _c;
	}

	/**
	 * @see Mat
	 */
	public double d() {
		return _d;
	}

	/**
	 * @see Mat
	 */
	public double e() {
		return _e;
	}

	/**
	 * @see Mat
	 */
	public double f() {
		return _f;
	}

	/**
	 * <pre>
	 * <code>
	 * [a c e] [x]
	 * [b d f] [y]
	 * [0 0 1] [1]
	 * </code>
	 * </pre>
	 */
	public Vec apply(Vec v) {
		double x = v.getX();
		double y = v.getY();
		double rx = _a * x + _c * y + _e;
		double ry = _b * x + _d * y + _f;
		return Vec.vec(rx, ry);
	}

	/**
	 * <pre>
	 * <code>
	 * [a c e] [ma mc me]
	 * [b d f] [mb md mf]
	 * [0 0 1] [ 0  0  1]
	 * </code>
	 * </pre>
	 */
	public Mat apply(Mat m) {
		double ma = m._a;
		double mb = m._b;
		double mc = m._c;
		double md = m._d;
		double me = m._e;
		double mf = m._f;

		double ra = _a * ma + _c * mb;
		double rc = _a * mc + _c * md;
		double re = _a * me + _c * mf + _e;

		double rb = _b * ma + _d * mb;
		double rd = _b * mc + _d * md;
		double rf = _b * me + _d * mf + _f;

		return Mat.mat(ra, rb, rc, rd, re, rf);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_a, _b, _c, _d, _e, _f);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mat other = (Mat) obj;
		return equals(_a, other._a)
			&& equals(_b, other._b)
			&& equals(_c, other._c)
			&& equals(_d, other._d)
			&& equals(_e, other._e)
			&& equals(_f, other._f);
	}

	static boolean equals(double a, double b) {
		return Math.abs(a - b) < EPSILON;
	}

	@Override
	public String toString() {
		return 
			"[" + _a + " " + _c + " " + _e + "]\n" +
			"[" + _b + " " + _d + " " + _f + "]\n" +
			"[" + 0.0 + " " + 0.0 + " " + 1.0 + "]";
	}

}
