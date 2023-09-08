/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.StopWatch;

/**
 * Benchmark for determining the trade-off for
 * {@link CollectionUtil#toContainsChecker(int, Collection)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BenchmarkCollectionUtilToContainsChecker {

	private static Random _random = new Random();

	/**
	 * Runs the benchmark.
	 */
	public static void main(String[] args) {
		// Warm up.
		benchmark(100, 100);
		benchmark(100, 100);
		benchmark(100, 100);

		for (int size = 64; size < 1000000; size *= 2) {
			int count = 2;
			while (!benchmark(size, count)) {
				count *= 2;
			}
			System.err.println("Tradeoff reached: for size " + size + " at least " + count + " test are required ("
				+ size * count + " operations).");
		}
	}

	private static boolean benchmark(int size, int count) {
		List<String> list = randomStrings(size);
		List<String> testList = randomStrings(count);
		long regularTime = benchmark(size, count, list, testList, false);
		long optimizedTime = benchmark(size, count, list, testList, true);

		return optimizedTime < regularTime;
	}

	private static long benchmark(int size, int count, List<?> checkList, List<?> testList, boolean optimize) {
		StopWatch watch = StopWatch.createStartedWatch();

		Collection<?> checker;
		if (optimize) {
			checker = CollectionUtil.toContainsChecker(/* enforce optimization */Integer.MAX_VALUE, checkList);
		} else {
			checker = checkList;
		}
		int matches = 0;
		for (Object s : testList) {
			if (checker.contains(s)) {
				matches++;
			}
		}
		watch.stop();
		long elapsedNanos = watch.getElapsedNanos();
		System.out.println(count + " " + (optimize ? "optimized" : "unoptimized")
			+ " contains tests in list with size " + size + " took " + elapsedNanos / 1000.0
			+ "µs (" + matches + " matches)");

		return elapsedNanos;
	}

	private static List<String> randomStrings(int count) {
		ArrayList<String> result = new ArrayList<>(count);
		for (int n = 0; n < count; n++) {
			result.add(BasicTestCase.randomString(_random, 30, true, true, false, false));
		}
		return result;
	}

}
