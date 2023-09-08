/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.col.ListComputation;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Test case for {@link ListComputation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestListComputation extends BasicTestCase {

	public void testCompute() {
		List<? extends String> future = new ListComputation<String>() {
			@Override
			public List<? extends String> run() {
				return list("Hello world!".split(" "));
			}
		}.start();

		assertEquals(list("Hello", "world!"), future);
	}
	
	public void testContext() {
		String contextId = contextId();
		assertNotNull(contextId);

		List<? extends String> future = new ListComputation<String>() {
			@Override
			public List<? extends String> run() {
				return list(contextId());
			}
		}.start();

		assertEquals(list(contextId), future);
	}

	static String contextId() {
		return ThreadContextManager.getSubSession().getContextId();
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestListComputation.class, SchedulerService.Module.INSTANCE));
	}
}
