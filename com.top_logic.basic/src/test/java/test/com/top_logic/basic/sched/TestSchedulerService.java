/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sched;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.sched.SchedulerService;

/**
 * Test case for {@link SchedulerService}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSchedulerService extends BasicTestCase {

	public void testScheduleHappens() throws InterruptedException, ExecutionException {
		final Object expectedResult = new Object();

		Callable<Object> runnable = new Callable<>() {
			@Override
			public Object call() {
				return expectedResult;
			}
		};

		ScheduledFuture<Object> future = SchedulerService.getInstance().schedule(runnable, 10, TimeUnit.MICROSECONDS);

		assertSame("Scheduled Callable returned wrong result.", expectedResult, future.get());
		assertTrue("Scheduled Callable is not done after ScheduledFuture.get() returned.", future.isDone());
		assertFalse("Scheduled Callable has been canceled.", future.isCancelled());
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestSchedulerService.class,
			SchedulerService.Module.INSTANCE));
	}

}
