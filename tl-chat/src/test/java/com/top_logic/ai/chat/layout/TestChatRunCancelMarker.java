/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.RunCancellationBinding;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.agent.event.AgentEvent;
import com.top_logic.ai.core.agent.subagent.SubagentJob;
import com.top_logic.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.ai.core.agent.subagent.SubagentResult;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.scripting.AIScriptFunctions;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A chat run stops at the cancel marker of its execution, which {@code aiAgentRunCancel} and the
 * stop command of the agent activity monitor set, and ends as {@code CANCELLED} (#30483). The
 * background subagents of the chat stop with it.
 *
 * <p>
 * The marker is set while the model call of the first turn is in flight, where a stop from a script
 * finds a busy run. The scripted model holds that call until the run is cancelled, or until the run
 * had more than enough time to read the marker. The scheduler service runs, because the run reads
 * its marker on a timer of the scheduler while a model call is in flight.
 * </p>
 */
public class TestChatRunCancelMarker extends BasicTestCase {

	private static final String AGENT = "TestChatPlanner";

	private static final long TIMEOUT_SECONDS = 60;

	/** How long the model call waits for the stop. The run reads its marker every second. */
	private static final long STOP_SECONDS = 10;

	private Person _owner;

	private Person _previousPerson;

	private Watcher _watcher;

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
		if (_watcher != null) {
			ChatEventHandlerRegistry.unregister(_watcher);
		}
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/** {@code aiAgentRunCancel} on the session of a running chat stops the run. */
	public void testMarkerStopsTheRun() throws Exception {
		AgentSession session = (AgentSession) ChatHistoryService.createSession("Cancel marker", _owner);
		HistoryUtils.updateSessionRevision();
		TaskProgressTracker window = new TaskProgressTracker();
		assertTrue(window.setActiveSessionId(session.tHandle().getObjectName().toExternalForm(), _owner));

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

		ChatProcessor.SendResult sent = ChatProcessor.sendMessage(window, TLContextManager.getSubSession(),
			_owner.getName(), "Work on it.", null, new ChatProcessor.ChatRunOptions(AGENT, null, false, List.of()));
		assertNotNull("The send was refused.", sent.getTaskId());
		ChatProcessor.AIProcessingState run = window.getProcessingState(sent.getTaskId());
		assertTrue("The run did not reach its model call.", modelWorks.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

		HistoryUtils.updateSessionRevision();
		assertTrue("aiAgentRunCancel found no running execution of the chat.",
			AIScriptFunctions.agentRunCancel(session));

		run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		assertNull("A stop is no error: " + run.getError(), run.getError());
		assertEquals("The run did not stop at its cancel marker.", "CANCELLED", statusOfLastRun(session));
		assertFalse("The model was asked again after the cancel marker was set.", askedAgain.get());
	}

	/**
	 * A stop by the cancel marker stops the background subagents of the chat, and starts no turn of
	 * the lead for their news afterwards, as the stop button does.
	 */
	public void testMarkerStopsTheSubagentsOfTheChat() throws Exception {
		AgentSession session = (AgentSession) ChatHistoryService.createSession("Cancel marker, subagents", _owner);
		HistoryUtils.updateSessionRevision();
		TaskProgressTracker window = new TaskProgressTracker();
		String sessionId = session.tHandle().getObjectName().toExternalForm();
		assertTrue(window.setActiveSessionId(sessionId, _owner));
		// Somebody watches the window, so that a turn of the lead could be started for news.
		_watcher = new Watcher(window, _owner);
		ChatEventHandlerRegistry.register(_watcher);
		SubagentJobRegistry subagents = window.getSessionManager().getSubagentRegistry(sessionId);

		SubagentJob working = subagents.register("worker", "Work long.", new CancellationToken());
		CountDownLatch workerRuns = new CountDownLatch(1);
		subagents.spawn(working, job -> {
			workerRuns.countDown();
			if (job.cancellationToken().sleepCancellably(TimeUnit.SECONDS.toMillis(STOP_SECONDS), 10)) {
				// What the spawner does with a cancelled run.
				throw new CancellationException("Subagent cancelled.");
			}
			return result("Worked long.");
		});
		assertTrue("The subagent did not start.", workerRuns.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

		CountDownLatch modelWorks = new CountDownLatch(1);
		AtomicInteger modelCalls = new AtomicInteger();
		ScriptedModel.install((request, turn) -> {
			if (modelCalls.incrementAndGet() > 1) {
				return List.<ContentPart> of(new TextContent("Woken for the news."));
			}
			// A subagent ends while the lead works: its result waits for a turn of the lead.
			SubagentJob quick = subagents.register("worker", "Answer fast.", new CancellationToken());
			subagents.spawn(quick, job -> result("Found it."));
			modelWorks.countDown();
			awaitStop();
			return List.<ContentPart> of(
				new ToolCall("call-skill", "loadSkill", "{\"skillId\": \"TL_taskManagement\"}"));
		});

		ChatProcessor.SendResult sent = ChatProcessor.sendMessage(window, TLContextManager.getSubSession(),
			_owner.getName(), "Work on it.", null, new ChatProcessor.ChatRunOptions(AGENT, null, false, List.of()));
		assertNotNull("The send was refused.", sent.getTaskId());
		ChatProcessor.AIProcessingState run = window.getProcessingState(sent.getTaskId());
		assertTrue("The run did not reach its model call.", modelWorks.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

		HistoryUtils.updateSessionRevision();
		assertTrue("aiAgentRunCancel found no running execution of the chat.",
			AIScriptFunctions.agentRunCancel(session));
		run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		assertEquals("The run did not stop at its cancel marker.", "CANCELLED", statusOfLastRun(session));

		assertTrue("A subagent of the chat goes on after the stop.", working.cancellationToken().isCancelled());
		// Longer than the waker collects news before it starts a turn of the lead.
		Thread.sleep(2 * SubagentWaker.DEBOUNCE_MS + 1000);
		assertEquals("A turn of the lead started for the news of its subagents after the stop.", 1,
			modelCalls.get());
	}

	private static SubagentResult result(String text) {
		return new SubagentResult(text, TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
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

	/** The open event stream of a window; a turn of the lead is started only for a watched window. */
	private static final class Watcher implements ChatEventHandler {

		private final TaskProgressTracker _tracker;

		private final Person _account;

		Watcher(TaskProgressTracker tracker, Person account) {
			_tracker = tracker;
			_account = account;
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _tracker;
		}

		@Override
		public Person getAccount() {
			return _account;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public void onEvent(AgentEvent event) {
			// Not what this test asks about.
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			// Not what this test asks about.
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			// Not what this test asks about.
		}
	}

	private static String statusOfLastRun(AgentSession session) {
		HistoryUtils.updateSessionRevision();
		List<AgentExecution> executions = AgentExecutionStore.getInstance().executionsOf(session);
		assertFalse("No run was recorded.", executions.isEmpty());
		return executions.get(executions.size() - 1).getStatus().getName();
	}

	/** The suite, on the services a chat run passes through, and the scheduler. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatRunCancelMarker.class),
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
