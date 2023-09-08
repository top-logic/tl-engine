/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import junit.framework.TestCase;

import com.top_logic.basic.util.StopWatch;

/**
 * Test case for {@link StopWatch}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestStopWatch extends TestCase {
	
	private static final long NANO = 1L;
	private static final long MICRO = 1000L * NANO;
	private static final long MILLI = 1000L * MICRO;
	private static final long SECOND = 1000L * MILLI;

	public void testInit() throws InterruptedException {
		StopWatch watch = new StopWatch();
		Thread.sleep(1);
		long t1 = watch.getElapsedNanos();
		Thread.sleep(1);
		long t2 = watch.getElapsedNanos();
		assertEquals(0L, t1);
		assertEquals(0L, t2);
	}

	public void testRunning() throws InterruptedException {
		StopWatch watch = StopWatch.createStartedWatch();
		long t1 = watch.getElapsedNanos();
		Thread.sleep(1);
		long t2 = watch.getElapsedNanos();
		Thread.sleep(1);
		long t3 = watch.getElapsedNanos();
		assertTrue(t2 > t1);
		assertTrue(t3 > t2);
	}

	public void testStop() throws InterruptedException {
		StopWatch watch = StopWatch.createStartedWatch();
		Thread.sleep(1);
		watch.stop();
		long t1 = watch.getElapsedNanos();
		Thread.sleep(1);
		long t2 = watch.getElapsedNanos();
		assertTrue(t1 > 0);
		assertTrue(t2 == t1);
		
		assertTrue(t1 < 1L * SECOND);
	}

	public void testResetRunning() throws InterruptedException {
		StopWatch watch = StopWatch.createStartedWatch();
		Thread.sleep(1);
		long t1 = watch.getElapsedNanos();
		watch.reset();
		Thread.sleep(1);
		long t2 = watch.getElapsedNanos();
		assertTrue(t1 > 0);
		assertTrue(t2 == 0);
	}

	public void testResetStopped() throws InterruptedException {
		StopWatch watch = StopWatch.createStartedWatch();
		Thread.sleep(1);
		long t1 = watch.getElapsedNanos();
		watch.stop();
		watch.reset();
		Thread.sleep(1);
		long t2 = watch.getElapsedNanos();
		assertTrue(t1 > 0);
		assertTrue(t2 == 0);
	}
	
}
