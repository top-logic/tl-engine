/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceStarter;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.util.sched.Scheduler;

/**
 * Tests for {@link com.top_logic.util.sched.Scheduler.Module} class.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestSchedulerModule extends BasicTestCase {

	/**
	 * Restarting the {@link Scheduler} without restarting the {@link KnowledgeBase}.
	 */
	public void testMultipleStartupAndShutdown() throws Exception {
		assertFalse("Test for starting does not work with active scheduler.", Scheduler.Module.INSTANCE.isActive());
		ServiceStarter serviceStarter = new ServiceStarter(Scheduler.Module.INSTANCE);

		serviceStarter.startService();
		assertTrue(Scheduler.Module.INSTANCE.isActive());
		serviceStarter.stopService();
		assertFalse(Scheduler.Module.INSTANCE.isActive());

		serviceStarter.startService();
		assertTrue(Scheduler.Module.INSTANCE.isActive());
		serviceStarter.stopService();
		assertFalse(Scheduler.Module.INSTANCE.isActive());

		serviceStarter.startService();
		assertTrue(Scheduler.Module.INSTANCE.isActive());
		serviceStarter.stopService();
		assertFalse(Scheduler.Module.INSTANCE.isActive());
	}

	/**
	 * The suite containing all the test cases in this class.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestSchedulerModule.class);
	}

}
