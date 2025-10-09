/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * TL-Script functions wrapping Math methods.
 *
 * <p>
 * Provides mathematical functions for TL-Script usage by delegating to Math.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ScriptPrefix("math")
public class MathFuncions extends TLScriptFunctions {

	/**
	 * Returns the absolute value of a double value.
	 *
	 * @param a The value whose absolute value is to be determined.
	 * @return The absolute value of the argument.
	 */
	@Label("Absolute value of a number")
	@SideEffectFree
	public static double abs(@Mandatory double a) {
		return Math.abs(a);
	}

	/**
	 * Returns the arc cosine of a value.
	 *
	 * @param a
	 *        The value whose arc cosine is to be returned.
	 * @return The arc cosine of the argument in radians. The returned angle is in the range
	 *         <code>0.0</code> through <code>pi</code>.
	 */
	@Label("Arc cosine function")
	@SideEffectFree
	public static double acos(@Mandatory double a) {
		return Math.acos(a);
	}

	/**
	 * Returns the arc sine of a value.
	 *
	 * @param a
	 *        The value whose arc sine is to be returned.
	 * @return The arc sine of the argument in radians. The returned angle is in the range
	 *         <code>-pi/2</code> through <code>pi/2</code>.
	 */
	@Label("Arc sine function")
	@SideEffectFree
	public static double asin(@Mandatory double a) {
		return Math.asin(a);
	}

	/**
	 * Returns the arc tangent of a value.
	 *
	 * @param a
	 *        The value whose arc tangent is to be returned.
	 * @return The arc tangent of the argument in radians. The returned angle is in the range
	 *         <code>-pi/2</code> through <code>pi/2</code>.
	 */
	@Label("Arc tangent function")
	@SideEffectFree
	public static double atan(@Mandatory double a) {
		return Math.atan(a);
	}

	/**
	 * Converts rectangular coordinates (x, y) to polar (r, theta).
	 *
	 * @param y
	 *        The ordinate coordinate.
	 * @param x
	 *        The abscissa coordinate.
	 * @return The theta component of the point (r, theta) in polar coordinates.
	 */
	@Label("Arc tangent of y/x")
	@SideEffectFree
	public static double atan2(@Mandatory double y, @Mandatory double x) {
		return Math.atan2(y, x);
	}

	/**
	 * Returns the cube root of a double value.
	 *
	 * @param a
	 *        The value whose cube root is to be returned.
	 * @return The cube root of <code>a</code>.
	 */
	@Label("Cube root")
	@SideEffectFree
	public static double cbrt(@Mandatory double a) {
		return Math.cbrt(a);
	}

	/**
	 * 
	 * Returns the first floating-point argument with the sign of the second floating-point
	 * argument.
	 *
	 * @param magnitude
	 *        Determines the absolute value (or magnitude) of the result.
	 * @param sign
	 *        Determines the sign (positive or negative) of the result.
	 * @return The value of <code>magnitude</code> with the sign of <code>sign</code>.
	 */
	@Label("Copy Sign")
	@SideEffectFree
	public static double copySign(@Mandatory double magnitude, @Mandatory double sign) {
		return Math.copySign(magnitude, sign);
	}

	/**
	 * Returns the trigonometric cosine of an angle.
	 *
	 * @param a
	 *        The angle in radians.
	 * @return The cosine of the angle.
	 */
	@Label("Cosine function")
	@SideEffectFree
	public static double cos(@Mandatory double a) {
		return Math.cos(a);
	}

	/**
	 * Returns the hyperbolic cosine of a double value.
	 *
	 * @param x
	 *        The number whose hyperbolic cosine is to be returned.
	 * @return The hyperbolic cosine of <code>x</code>.
	 */
	@Label("Hyperbolic cosine")
	@SideEffectFree
	public static double cosh(@Mandatory double x) {
		return Math.cosh(x);
	}

	/**
	 * Returns Euler's number e raised to the power of a double value.
	 *
	 * @param a
	 *        The exponent to raise e to.
	 * @return The value <code>e^a</code>.
	 */
	@Label("Exponential function")
	@SideEffectFree
	public static double exp(@Mandatory double a) {
		return Math.exp(a);
	}

	/**
	 * Returns Euler's number e raised to the power of a double value minus 1.
	 *
	 * @param x
	 *        The exponent to raise e to.
	 * @return The value <code>e^x - 1</code>.
	 */
	@Label("Exponential minus one")
	@SideEffectFree
	public static double expm1(@Mandatory double x) {
		return Math.expm1(x);
	}

	/**
	 * Returns the unbiased exponent used in the representation of a double.
	 *
	 * @param d
	 *        The double value whose unbiased exponent is to be returned.
	 * @return The unbiased exponent of the argument
	 */
	@Label("Get Binary Exponent")
	@SideEffectFree
	public static double getExponent(@Mandatory double d) {
		return Math.getExponent(d);
	}

	/**
	 * Returns the positive difference between two arguments (Euclidean distance).
	 *
	 * @param x
	 *        The first value.
	 * @param y
	 *        The second value.
	 * @return The Euclidean distance <code>sqrt(x^2 + y^2)</code>.
	 */
	@Label("Euclidean distance")
	@SideEffectFree
	public static double hypot(@Mandatory double x, @Mandatory double y) {
		return Math.hypot(x, y);
	}

	/**
	 * Returns the remainder of two floating-point arguments as prescribed by the IEEE 754 standard.
	 *
	 * @param f1
	 *        The dividend (the number being divided).
	 * @param f2
	 *        The divisor (the number dividing <code>f1</code>}).
	 * @return The IEEE 754 standard remainder of <code>f1</code> and <code>f2</code>.
	 */
	@Label("IEEE 754 Remainder")
	@SideEffectFree
	public static double IEEEremainder(@Mandatory double f1, @Mandatory double f2) {
		return Math.IEEEremainder(f1, f2);
	}

	/**
	 * Returns the natural logarithm (base e) of a double value.
	 *
	 * @param a
	 *        The value whose natural logarithm is to be returned.
	 * @return The natural logarithm of <code>a</code>.
	 */
	@Label("Natural logarithm")
	@SideEffectFree
	public static double log(@Mandatory double a) {
		return Math.log(a);
	}

	/**
	 * Returns the base 10 logarithm of a double value.
	 *
	 * @param a
	 *        The value whose base 10 logarithm is to be returned.
	 * @return The base 10 logarithm of <code>a</code>.
	 */
	@Label("Base 10 logarithm")
	@SideEffectFree
	public static double log10(@Mandatory double a) {
		return Math.log10(a);
	}

	/**
	 * Returns the natural logarithm of the sum of the argument and 1.
	 *
	 * @param x
	 *        The value to add 1 to.
	 * @return The value <code>ln(x + 1)</code>.
	 */
	@Label("Natural logarithm of x + 1")
	@SideEffectFree
	public static double log1p(@Mandatory double x) {
		return Math.log1p(x);
	}

	/**
	 * Returns the next floating-point value adjacent to the first argument in the direction of the
	 * second argument.
	 *
	 * @param start
	 *        The starting floating-point value.
	 * @param direction
	 *        The value indicating the direction.
	 * @return The adjacent floating-point value.
	 */
	@Label("Next floating-point value")
	@SideEffectFree
	public static double nextAfter(@Mandatory double start, @Mandatory double direction) {
		return Math.nextAfter(start, direction);
	}

	/**
	 * Returns the floating-point value adjacent to the argument in the direction of positive
	 * infinity.
	 *
	 * @param d
	 *        The starting floating-point value.
	 * @return The adjacent floating-point value toward positive infinity.
	 */
	@Label("Next value toward positive infinity")
	@SideEffectFree
	public static double nextUp(@Mandatory double d) {
		return Math.nextUp(d);
	}

	/**
	 * Returns the floating-point value adjacent to the argument in the direction of negative
	 * infinity.
	 *
	 * @param d
	 *        The starting floating-point value.
	 * @return The adjacent floating-point value toward negative infinity.
	 */
	@Label("Next value toward negative infinity")
	@SideEffectFree
	public static double nextDown(@Mandatory double d) {
		return Math.nextDown(d);
	}

	/**
	 * Returns the value of the first argument raised to the power of the second argument.
	 *
	 * @param a
	 *        The base.
	 * @param b
	 *        The exponent.
	 * @return The value <code>a^b</code>.
	 */
	@Label("Power function")
	@SideEffectFree
	public static double pow(@Mandatory double a, @Mandatory double b) {
		return Math.pow(a, b);
	}

	/**
	 * Returns a double value with a positive sign, greater than or equal to <code>0.0</code> and
	 * less than <code>1.0</code>.
	 *
	 * @return A pseudorandom double greater than or equal to <code>0.0</code> and less than
	 *         <code>1.0</code>.
	 */
	@Label("Random")
	public static double random() {
		return Math.random();
	}

	/**
	 * Returns the double value that is closest in value to the argument and is equal to a
	 * mathematical integer.
	 *
	 * @param a
	 *        The value to be rounded.
	 * @return The closest double value to the argument that is equal to a mathematical integer.
	 */
	@Label("Round to nearest integer")
	@SideEffectFree
	public static double rint(@Mandatory double a) {
		return Math.rint(a);
	}

	/**
	 * Returns the signum function of the argument.
	 *
	 * @param d
	 *        The value whose signum is to be returned.
	 * @return The signum function of the argument.
	 */
	@Label("Signum function")
	@SideEffectFree
	public static double signum(@Mandatory double d) {
		return Math.signum(d);
	}

	/**
	 * Returns the trigonometric sine of an angle.
	 *
	 * @param a
	 *        The angle in radians.
	 * @return The sine of the angle.
	 */
	@Label("Sine function")
	@SideEffectFree
	public static double sin(@Mandatory double a) {
		return Math.sin(a);
	}

	/**
	 * Returns the hyperbolic sine of a double value.
	 *
	 * @param x
	 *        The number whose hyperbolic sine is to be returned.
	 * @return The hyperbolic sine of <code>x</code>.
	 */
	@Label("Hyperbolic sine")
	@SideEffectFree
	public static double sinh(@Mandatory double x) {
		return Math.sinh(x);
	}

	/**
	 * Returns the correctly rounded positive square root of a double value.
	 *
	 * @param a
	 *        The value whose square root is to be determined.
	 * @return The positive square root of <code>a</code>.
	 */
	@Label("Square root")
	@SideEffectFree
	public static double sqrt(@Mandatory double a) {
		return Math.sqrt(a);
	}

	/**
	 * Returns the trigonometric tangent of an angle.
	 *
	 * @param a
	 *        The angle in radians.
	 * @return The tangent of the angle.
	 */
	@Label("Tangent function")
	@SideEffectFree
	public static double tan(@Mandatory double a) {
		return Math.tan(a);
	}

	/**
	 * Returns the hyperbolic tangent of a double value.
	 *
	 * @param x
	 *        The number whose hyperbolic tangent is to be returned.
	 * @return The hyperbolic tangent of <code>x</code>.
	 */
	@Label("Hyperbolic tangent")
	@SideEffectFree
	public static double tanh(@Mandatory double x) {
		return Math.tanh(x);
	}

	/**
	 * Converts an angle measured in radians to an approximatelyequivalent angle measured in
	 * degrees. The conversion from radians to degrees is generally inexact.
	 *
	 * @param angrad
	 *        An angle, in radians
	 * @return The measurement of the angle <code>angrad</code> in degrees
	 */
	@Label("Convert Radians to Degrees")
	@SideEffectFree
	public static double toDegrees(@Mandatory double angrad) {
		return Math.toDegrees(angrad);
	}

	/**
	 * Converts an angle measured in degrees to an approximatelyequivalent angle measured in
	 * radians. The conversion from degrees to radians is generally inexact.
	 *
	 * @param angdeg
	 *        An angle, in degrees
	 * @return the measurement of the angle <code>angdeg</code> radians.
	 */
	@Label("Convert Degrees to Radians")
	@SideEffectFree
	public static double toRadians(@Mandatory double angdeg) {
		return Math.toRadians(angdeg);
	}

	/**
	 * Returns the ulp size of the argument. An ulp, unit in the last place, of a double value is
	 * the positive distance between this floating-point value and the double value next larger in
	 * magnitude.
	 *
	 * @param d
	 *        The value whose ulp is to be returned.
	 * @return The size of an ulp of the argument.
	 */
	@Label("Unit of least precision")
	@SideEffectFree
	public static double ulp(@Mandatory double d) {
		return Math.ulp(d);
	}

	/**
	 * Returns the value of pi.
	 *
	 * @return The value of pi (3.141592653589793).
	 */
	@Label("Value of pi")
	@SideEffectFree
	public static double pi() {
		return Math.PI;
	}

	/**
	 * Returns the value of e.
	 *
	 * @return The value of e (2.718281828459045).
	 */
	@Label("Value of e")
	@SideEffectFree
	public static double e() {
		return Math.E;
	}

}