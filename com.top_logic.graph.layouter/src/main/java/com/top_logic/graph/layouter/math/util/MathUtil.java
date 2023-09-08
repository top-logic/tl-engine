/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.math.util;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Util methods for mathematics.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class MathUtil {

	/**
	 * The greatest common divisor between a and b.
	 */
	public static int gcd(int a, int b) {
		return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).intValue();
	}

	/**
	 * The greatest common divisor between several numbers.
	 */
	public static int gcd(int... numbers) {
		return Arrays.stream(numbers).reduce(0, (x, y) -> gcd(x, y));
	}

	/**
	 * The lowest common multiple between several numbers.
	 */
	public static int lcm(int... numbers) {
		return Arrays.stream(numbers).reduce(1, (x, y) -> x * (y / gcd(x, y)));
	}

	/**
	 * Upper rounded number to a multiple of base.
	 */
	public static double roundUpperMultiple(double number, int base) {
		return base * (Math.ceil(Math.abs(number / base)));
	}

	/**
	 * Lower rounded number to a multiple of base.
	 */
	public static double roundLowerMultiple(int number, int base) {
		return base * (Math.floor(Math.abs((double) number / base)));
	}
}
