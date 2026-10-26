/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.history.ChatHistoryService.RecordedRun;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The figures the chat shows about a run answered by a {@link ConversationBackend}: the context
 * window is the one the backend reports, not the one of the framework's agent, and the cost and
 * the start the run reported are found again when the conversation is read back.
 */
public class TestBackendRunFigures extends BasicTestCase {

	private static final long TIMEOUT_MILLIS = 60_000;

	/** How long the backend prepares before its run starts, as a process start does. */
	private static final long PREPARATION_MILLIS = 300;

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	private Person _owner;

	private Person _previousPerson;

	private AgentSession _session;

	private TaskProgressTracker _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
		_session = (AgentSession) ChatHistoryService.createSession("Figures", _owner);
		HistoryUtils.updateSessionRevision();
		_window = new TaskProgressTracker();
		assertTrue(_window.setActiveSessionId(_session.tHandle().getObjectName().toExternalForm(), _owner));
	}

	@Override
	protected void tearDown() throws Exception {
		ScriptedBackend.reset();
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * A chat a backend answers is measured against the window the backend reports, zero while it
	 * reports none; a chat of the framework's agent against its memory.
	 */
	public void testContextWindowOfABackend() {
		assertNull("The framework's agent answers.", ChatSSEHandler.backendContextWindow(_window));

		ScriptedBackend.install(session -> session.equals(_session) ? backend(0) : null);
		assertEquals(Integer.valueOf(0), ChatSSEHandler.backendContextWindow(_window));

		ScriptedBackend.install(session -> session.equals(_session) ? backend(200_000) : null);
		assertEquals(Integer.valueOf(200_000), ChatSSEHandler.backendContextWindow(_window));
	}

	/** The cost a run reported is read back with the turn the run ended with. */
	public void testCostIsReadBack() throws Exception {
		ScriptedBackend.install(session -> session.equals(_session) ? backend(0) : null);
		Map<String, RecordedRun> runs = sendAndReadBack();
		assertEquals(1, runs.size());
		Money cost = runs.values().iterator().next().cost();
		assertEquals(0.0025, cost.amount(), 1e-9);
		assertEquals(Money.USD, cost.currency());
		assertTrue("The cost belongs to a recorded turn.",
			ChatHistoryService.loadConversationJson(_session).contains(runs.keySet().iterator().next()));
	}

	/**
	 * A run is read back with the moment it reported its start, not the moment its turn started:
	 * what the backend did before - starting a process - is no part of the run's duration.
	 */
	public void testStartIsReadBack() throws Exception {
		ScriptedBackend.install(session -> session.equals(_session) ? backend(0) : null);
		long sentAt = System.currentTimeMillis();
		Map<String, RecordedRun> runs = sendAndReadBack();
		assertEquals(1, runs.size());
		RecordedRun run = runs.values().iterator().next();
		assertNotNull("No start was recorded.", run.startedAt());
		assertNotNull("No end was recorded.", run.endedAt());
		assertTrue("The run started before the backend reported its start.",
			run.startedAt().getTime() >= sentAt + PREPARATION_MILLIS);
		assertTrue("The run ended before it started.", run.endedAt().getTime() >= run.startedAt().getTime());
	}

	private Map<String, RecordedRun> sendAndReadBack() throws InterruptedException {
		ChatProcessor.SendResult sent = ChatProcessor.sendMessage(_window, TLContextManager.getSubSession(),
			_owner.getName(), "What does it cost?", null, ChatProcessor.ChatRunOptions.DEFAULT);
		assertNotNull("The send was refused.", sent.getTaskId());
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (true) {
			HistoryUtils.updateSessionRevision();
			Map<String, RecordedRun> runs = ChatHistoryService.recordedRuns(_session);
			if (!runs.isEmpty() && runs.values().iterator().next().endedAt() != null
				&& !ChatProcessor.isProcessing(_window)) {
				return runs;
			}
			assertTrue("No run was recorded.", System.currentTimeMillis() < deadline);
			TimeUnit.MILLISECONDS.sleep(20);
		}
	}

	private static ConversationBackend backend(int contextWindow) {
		return new ConversationBackend() {
			@Override
			public String agentName() {
				return "test-figures-backend";
			}

			@Override
			public BackendFeatures features() {
				return BackendFeatures.NONE;
			}

			@Override
			public int contextWindow() {
				return contextWindow;
			}

			@Override
			public String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) {
				String runId = "figures-" + System.nanoTime();
				try {
					Thread.sleep(PREPARATION_MILLIS);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return null;
				}
				sink.onEvent(new AgentEvent.RunStarted(runId, 0, Instant.now(), "test-model"));
				sink.onEvent(new AgentEvent.TurnStarted(runId, 1, Instant.now()));
				sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), "Little."));
				sink.onEvent(new AgentEvent.TurnCompleted(runId, 1, Instant.now(), USAGE,
					List.of(new TextContent("Little."))));
				sink.onEvent(new AgentEvent.RunCompleted(runId, 1, Instant.now(), StopReason.COMPLETE, USAGE,
					new Money(0.0025, Money.USD)));
				return "Little.";
			}
		};
	}

	/** The suite, on the services a chat run passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestBackendRunFigures.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}
}
