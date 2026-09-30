/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.administration;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.administration.MaintenanceWindowTimer;

/**
 * Test for {@link MaintenanceWindowTimer}.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestMaintenanceWindowTimer extends BasicTestCase {

	private static final long DELAY_MILLIS = 60_000;

	/** Slack for the time the test itself needs between the two clock readings. */
	private static final long TOLERANCE_MILLIS = 5_000;

	/**
	 * The announced finish time must be available as soon as the timer exists, without waiting for
	 * its thread to run.
	 *
	 * <p>
	 * The maintenance window is announced immediately after {@link Thread#start()}, and a listener
	 * reacting to that announcement asks for the finish time right away - typically before the new
	 * thread has executed its first statement. While the finish time was computed inside the
	 * thread, such a listener saw "no timer running" and could not show a count-down; only a later
	 * reader (the classic UI, which polls) ever got a usable value.
	 * </p>
	 */
	public void testFinishTimeKnownBeforeThreadRuns() {
		long before = System.currentTimeMillis();
		MaintenanceWindowTimer timer = new MaintenanceWindowTimer(DELAY_MILLIS);
		long finishedTime = timer.getFinishedTime();

		assertTrue("The finish time must be known without starting the thread, but was " + finishedTime,
			finishedTime > 0);
		assertTrue("The finish time must not lie before the announced delay has elapsed.",
			finishedTime >= before + DELAY_MILLIS);
		assertTrue("The finish time must not lie noticeably beyond the announced delay.",
			finishedTime <= before + DELAY_MILLIS + TOLERANCE_MILLIS);
	}

	/**
	 * The remaining time is likewise available without starting the thread, and is at most the
	 * configured delay.
	 */
	public void testTimeLeftKnownBeforeThreadRuns() {
		MaintenanceWindowTimer timer = new MaintenanceWindowTimer(DELAY_MILLIS);
		long timeLeft = timer.getTimeLeft();

		assertTrue("The remaining time must not exceed the configured delay, but was " + timeLeft,
			timeLeft <= DELAY_MILLIS);
		assertTrue("Nearly the whole delay must still be left, but only " + timeLeft + " ms were.",
			timeLeft > DELAY_MILLIS - TOLERANCE_MILLIS);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestMaintenanceWindowTimer}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestMaintenanceWindowTimer.class);
	}
}
