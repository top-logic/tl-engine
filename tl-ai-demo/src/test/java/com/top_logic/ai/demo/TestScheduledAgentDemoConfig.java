/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.sched.AgentTask;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.Scheduler.SchedulerConfig;
import com.top_logic.util.sched.task.Task;

/**
 * That the demo's own scheduled agent is a configuration the application can actually start with
 * (issue #384).
 *
 * <p>
 * {@code WEB-INF/autoconf/com.top_logic.util.sched.Scheduler.config.xml} is the worked example an
 * application copies from, and it is the one piece of this feature nothing else exercises: the task
 * is shipped disabled, so a running demo never instantiates it and a typo in it would be found by
 * whoever enabled it first. This reads it out of the assembled application configuration - the same
 * load path the running app uses - and instantiates it.
 * </p>
 */
public class TestScheduledAgentDemoConfig extends TestCase {

	private static final String TASK_NAME = "DailyInvoiceSummary";

	/** The demo ships exactly one scheduled agent, and it is disabled. */
	public void testTheDemoTaskIsShippedDisabled() throws Exception {
		AgentTask.Config config = demoTask();

		assertFalse("Every run of it costs a real model call; a demo must not start one by itself.",
			config.isEnabled());
		assertFalse(config.isRunOnStartup());
	}

	/** What the example says: an entry-point agent, root, auto-approval, a recorded session. */
	public void testTheDemoTaskIsTheWorkedExample() throws Exception {
		AgentTask.Config config = demoTask();

		assertNull("The example runs whichever agent the application makes its entry point.",
			config.getAgent());
		assertEquals("root", config.getRunAs());
		assertEquals(ApprovalMode.AUTO_APPROVE, config.getApprovalMode());
		assertTrue("The transcript is what the README tells the reader to go and look at.",
			config.isRecordSession());
		assertEquals("Tageszusammenfassung der Belege", config.getSessionTitle());
		assertNotNull("There has to be something to ask.", config.getPrompt());
		assertEquals("The example is scheduled, not run-once.", 1, config.getSchedules().size());
	}

	/** It instantiates: the class resolves, and the prompt configuration is accepted. */
	public void testTheDemoTaskInstantiates() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		Task task = new DefaultInstantiationContext(log).getInstance(demoTask());

		assertEquals(String.join(" | ", log.getErrors()), 0, log.getErrors().size());
		assertTrue("Expected an AgentTask, got: " + task, task instanceof AgentTask);
		assertEquals(TASK_NAME, task.getName());
	}

	/** The demo's scheduled agent, read from the assembled application configuration. */
	private static AgentTask.Config demoTask() throws Exception {
		SchedulerConfig scheduler =
			(SchedulerConfig) ApplicationConfig.getInstance().getServiceConfiguration(Scheduler.class);
		Task.Config<?> task = scheduler.getTasks().get(TASK_NAME);

		assertNotNull("The demo's scheduler configuration does not declare '" + TASK_NAME
			+ "'. Declared: " + scheduler.getTasks().keySet(), task);
		assertTrue("Expected an AgentTask configuration, got: " + task.getImplementationClass(),
			task instanceof AgentTask.Config);
		return (AgentTask.Config) task;
	}

	/** Loads the assembled application configuration around the test. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestScheduledAgentDemoConfig.class);
	}
}
