/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.reflect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.StopWatch;

/**
 * Tests whether it is faster to always call {@link Class#forName(String)}, or cache these calls.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@DeactivatedTest("This test should only be used on-demand to avoid using unnecessarily time in the build.")
public class TestClassCaching extends TestCase {

	private static final boolean PRINT_RESULTS = false;

	private static final int INNER_LOOP_ITERATIONS = 1_000_000;

	/**
	 * How much time should be accumulated, before the test ends.
	 * <p>
	 * In nanoseconds, as {@link System#nanoTime()} is used.
	 * </p>
	 */
	private static final long MIN_TIME = 10_000_000_000L;

	private final Map<String, Class<?>> _cache = new ConcurrentHashMap<>();

	/** Tests that the cache for Class.forName() is faster than the method itself. */
	public void testCacheIsFaster() {
		String exampleClassName = StringServices.class.getName();
		long sumCache = 0;
		long sumNoCache = 0;
		int dummyValue = 0;
		int outerCounter = 0;
		if (PRINT_RESULTS) {
			System.out.println("Running until " + StopWatch.toStringNanos(MIN_TIME) + " have been reached.");
		}
		do {
			outerCounter += 1;
			for (int i = 0; i < INNER_LOOP_ITERATIONS; i++) {
				long start = System.nanoTime();
				Class<?> classObject = resolveUncached(exampleClassName);
				long end = System.nanoTime();
				sumNoCache += (end - start);
				/* Use the object, to prevent that the class resolution is optimized away. */
				dummyValue += classObject.hashCode();
			}
			for (int i = 0; i < INNER_LOOP_ITERATIONS; i++) {
				long start = System.nanoTime();
				Class<?> classObject = resolveCached(exampleClassName);
				long end = System.nanoTime();
				sumCache += (end - start);
				/* Use the object, to prevent that the class resolution is optimized away. */
				dummyValue += classObject.hashCode();
			}
			printResults(sumCache, sumNoCache, outerCounter);
		} while ((sumCache < MIN_TIME) && (sumNoCache < MIN_TIME));
		if (sumCache >= sumNoCache) {
			fail("Class.forName(String) is faster than the cache.");
		}
		/* Use the dummy value, to prevent it from being optimized away. */
		if (dummyValue == sumCache && dummyValue == sumNoCache) {
			System.out.println("This will probably never be printed.");
		}
		_cache.clear();
	}

	private void printResults(long sumCache, long sumNoCache, int outerCounter) {
		if (PRINT_RESULTS) {
			System.out.println("Calls:    " + (outerCounter * INNER_LOOP_ITERATIONS));
			System.out.println("Uncached: " + StopWatch.toStringNanos(sumNoCache));
			System.out.println("Cache:    " + StopWatch.toStringNanos(sumCache));
			System.out.println();
		}
	}

	private Class<?> resolveCached(String qualifiedName) {
		return _cache.computeIfAbsent(qualifiedName, this::resolveUncached);
	}

	private Class<?> resolveUncached(String qualifiedName) {
		try {
			return Class.forName(qualifiedName);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

}
