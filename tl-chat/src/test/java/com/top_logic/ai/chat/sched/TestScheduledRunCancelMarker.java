/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.sched;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ScriptedModel;
import com.top_logic.ai.chat.sched.TestScheduledAgentRun.ProbeTask;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.RunCancellationBinding;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.scripting.AIScriptFunctions;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * A scheduled run of an {@link AgentTask} stops at the cancel marker of its execution, which
 * {@code aiAgentRunCancel} and the stop command of the agent activity monitor set, and ends as
 * {@code CANCELLED} (#30483). A run that nobody stops is still reported as a success.
 *
 * <p>
 * The marker is set while the model call of the first turn is in flight. The scripted model holds
 * that call until the run is cancelled, or until the run had more than enough time to read the
 * marker. The scheduler service runs, because the run reads its marker on a timer of the scheduler
 * while a model call is in flight.
 * </p>
 *
 * <p>
 * The helpers that hold the model call and read the run's status are the same as in
 * {@link com.top_logic.ai.chat.layout.TestChatRunCancelMarker}: each test stays in the package of
 * the class it drives, and a shared helper class for a few lines is not worth it.
 * </p>
 */
public class TestScheduledRunCancelMarker extends BasicTestCase {

	private static final long TIMEOUT_SECONDS = 60;

	/** How long the model call waits for the stop. The run reads its marker every second. */
	private static final long STOP_SECONDS = 10;

	private Person _owner;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/** {@code aiAgentRunCancel} on the session of a running scheduled run stops the run. */
	public void testMarkerStopsTheRun() throws Exception {
		CountDownLatch modelWorks = new CountDownLatch(1);
		AtomicBoolean askedAgain = new AtomicBoolean();
		ScriptedModel.install((request, turn) -> {
			if (turn > 0) {
				askedAgain.set(true);
				return List.<ContentPart> of(new TextContent("Done."));
			}
			modelWorks.countDown();
			awaitStop();
			return List.<ContentPart> of(
				new ToolCall("call-skill", "loadSkill", "{\"skillId\": \"TL_taskManagement\"}"));
		});

		ProbeTask task = new ProbeTask(new DefaultInstantiationContext(TestScheduledRunCancelMarker.class),
			config("<task %s name='CancelMarker' agent='TestChatPlanner'><prompt>Work on it.</prompt></task>"));
		Thread sitting = new Thread(
			() -> ThreadContext.inSystemContext(TestScheduledRunCancelMarker.class, task::drive),
			"TestScheduledRunCancelMarker-sitting");
		sitting.setDaemon(true);
		sitting.start();

		AgentSession session;
		try {
			assertTrue("The run did not reach its model call.", modelWorks.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			HistoryUtils.updateSessionRevision();
			session = (AgentSession) ChatHistoryService.findOwnedSessionById(task.tracker().getActiveSessionId(),
				_owner);
			assertNotNull("The sitting was not recorded as a session.", session);
			assertTrue("aiAgentRunCancel found no running execution of the sitting.",
				AIScriptFunctions.agentRunCancel(session));
		} finally {
			sitting.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
		}
		assertFalse("The sitting did not end.", sitting.isAlive());
		assertEquals("The run did not stop at its cancel marker.", "CANCELLED", statusOfLastRun(session));
		assertFalse("The model was asked again after the cancel marker was set.", askedAgain.get());
		assertEquals("The task must report the cancelled run as cancelled.", ResultType.CANCELED,
			TestScheduledAgentRun.resultType(task));
	}

	/** A scheduled run whose marker is not set ends normally, and the task reports a success. */
	public void testRunWithoutMarkerSucceeds() throws Exception {
		ScriptedModel.install((request, turn) -> List.<ContentPart> of(new TextContent("Done.")));

		ProbeTask task = new ProbeTask(new DefaultInstantiationContext(TestScheduledRunCancelMarker.class),
			config("<task %s name='NoCancelMarker' agent='TestChatPlanner'><prompt>Work on it.</prompt></task>"));
		task.drive();

		HistoryUtils.updateSessionRevision();
		AgentSession session =
			(AgentSession) ChatHistoryService.findOwnedSessionById(task.tracker().getActiveSessionId(), _owner);
		assertNotNull("The sitting was not recorded as a session.", session);
		assertEquals("COMPLETE", statusOfLastRun(session));
		assertEquals(ResultType.SUCCESS, TestScheduledAgentRun.resultType(task));
	}

	/**
	 * Holds the model call on the agent thread until its run is cancelled, or until the run had time
	 * to read its marker several times.
	 */
	private static void awaitStop() {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(STOP_SECONDS);
		while (!RunCancellationBinding.isRunCancelled() && System.nanoTime() < deadline) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private static String statusOfLastRun(AgentSession session) {
		HistoryUtils.updateSessionRevision();
		List<AgentExecution> executions = AgentExecutionStore.getInstance().executionsOf(session);
		assertFalse("No run was recorded.", executions.isEmpty());
		return executions.get(executions.size() - 1).getStatus().getName();
	}

	private static AgentTask.Config config(String xml) throws Exception {
		String attributes = "xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + AgentTask.Config.class.getName() + "'";
		return (AgentTask.Config) TypedConfiguration.parse(
			CharacterContents.newContent(String.format(xml, attributes)));
	}

	/** The suite, on the services a chat run passes through, and the scheduler. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestScheduledRunCancelMarker.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}
}
