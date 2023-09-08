/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic.timer;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.basic.timer.TimerControl;

/**
 * Test for {@link com.top_logic.layout.basic.timer.UIExecutorService}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestUIExecutorService extends BasicTestCase {

	private static final Runnable DO_NOTHING_RUNNABLE = new Runnable() {

		@Override
		public void run() {
			// Do nothing
		}

	};

	private static final Callable<Object> DO_NOTHING_CALLABLE = new Callable<>() {

		@Override
		public Object call() throws Exception {
			// Do nothing
			return null;
		}
	};

	private ScheduledExecutorService _executor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_executor = new TimerControl().getExecutor();
	}

	public void testDelayConversion() {
		TimeUnit unit = TimeUnit.MINUTES;
		int delay = 500;
		assertDelay(_executor.schedule(DO_NOTHING_RUNNABLE, delay, unit), delay, unit);
		assertDelay(_executor.schedule(DO_NOTHING_CALLABLE, delay, unit), delay, unit);
		assertDelay(_executor.scheduleAtFixedRate(DO_NOTHING_RUNNABLE, delay, delay, unit), delay, unit);
		assertDelay(_executor.scheduleWithFixedDelay(DO_NOTHING_RUNNABLE, delay, delay, unit), delay, unit);
	}

	private void assertDelay(ScheduledFuture<?> schedule, int delay, TimeUnit unit) {
		try {
			long remainingDelay = schedule.getDelay(TimeUnit.MILLISECONDS);
			/* remaining delay may be decreased between starting schedule and fetching delay.
			 * Therefore check can only be an estimated guess. */
			long epsilon = 3;
			assertTrue(TimeUnit.MILLISECONDS.convert(delay, unit) - remainingDelay <= epsilon);
		} finally {
			schedule.cancel(true);
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestUIExecutorService}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestUIExecutorService.class);
	}

}
