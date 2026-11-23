/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.sched.AgentTask;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.ai.core.agent.approval.ApprovalMode;
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

	private static final String TASK_NAME = "DailyEventBriefing";

	/** The task of the demo whose run is about one object. */
	private static final String SUBJECT_TASK_NAME = "NextEventBriefing";

	/** The demo's scheduled agents are disabled. */
	public void testTheDemoTasksAreShippedDisabled() throws Exception {
		for (String name : new String[] { TASK_NAME, SUBJECT_TASK_NAME }) {
			assertFalse("Every run of " + name + " costs a real model call; a demo must not start one by itself.",
				demoTask(name).isEnabled());
		}
	}

	/**
	 * The second example names the object its run is about.
	 *
	 * <p>
	 * Only read here, not compiled: the expression names a type of the demo's model, which this test
	 * does not boot.
	 * </p>
	 */
	public void testTheSubjectTaskNamesItsSubject() throws Exception {
		AgentTask.Config config = demoTask(SUBJECT_TASK_NAME);

		assertNotNull("The example is about one event.", config.getSubjectExpression());
		assertEquals("root", config.getRunAs());
		assertTrue("The sitting is about the event, so it is recorded.", config.isRecordSession());
		assertNotNull("There has to be something to ask.", config.getPrompt());
	}

	/** What the example says: an entry-point agent, root, auto-approval, a recorded session. */
	public void testTheDemoTaskIsTheWorkedExample() throws Exception {
		AgentTask.Config config = demoTask(TASK_NAME);

		assertNull("The example runs whichever agent the application makes its entry point.",
			config.getAgent());
		assertEquals("root", config.getRunAs());
		assertEquals(ApprovalMode.AUTO_APPROVE, config.getApprovalMode());
		assertTrue("The transcript is what the README tells the reader to go and look at.",
			config.isRecordSession());
		assertEquals("Tagesbericht Veranstaltungen", config.getSessionTitle());
		assertNotNull("There has to be something to ask.", config.getPrompt());
		assertEquals("The example is scheduled, not run-once.", 1, config.getSchedules().size());
	}

	/** It instantiates: the class resolves, and the prompt configuration is accepted. */
	public void testTheDemoTaskInstantiates() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		Task task = new DefaultInstantiationContext(log).getInstance(demoTask(TASK_NAME));

		assertEquals(String.join(" | ", log.getErrors()), 0, log.getErrors().size());
		assertTrue("Expected an AgentTask, got: " + task, task instanceof AgentTask);
		assertEquals(TASK_NAME, task.getName());
	}

	/** A scheduled agent of the demo, read from the assembled application configuration. */
	private static AgentTask.Config demoTask(String name) throws Exception {
		SchedulerConfig scheduler =
			(SchedulerConfig) ApplicationConfig.getInstance().getServiceConfiguration(Scheduler.class);
		Task.Config<?> task = scheduler.getTasks().get(name);

		assertNotNull("The demo's scheduler configuration does not declare '" + name
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
