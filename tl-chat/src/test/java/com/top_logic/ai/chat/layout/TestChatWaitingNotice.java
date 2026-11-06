/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.RunStateFrame;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.api.messages.RunWaitingEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.limits.CapacityConfig;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A send that finds every chat worker busy tells its window that it waits, and where, and takes the
 * notice down when its run starts or is stopped (#30053).
 */
public class TestChatWaitingNotice extends BasicTestCase {

	private static final String AGENT = "TestChatPlanner";

	private static final long TIMEOUT_SECONDS = 60;

	/** What the window's handler records for a run starting. */
	private static final String STARTED = "started";

	private Person _owner;

	private Person _previousPerson;

	private RunLimitSettings.Overridden _limits;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
		_limits = RunLimitSettings.override(oneChatWorker());
		ChatRunExecutor.resetForTest();
	}

	@Override
	protected void tearDown() throws Exception {
		_limits.close();
		ChatRunExecutor.resetForTest();
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * With {@code chat-workers="1"}, a send while the first run works is told it waits at place 1;
	 * when the first run ends, the notice is taken down and the second run starts.
	 */
	public void testSecondSendWaitsForTheFirstRun() throws Exception {
		CountDownLatch firstWorks = new CountDownLatch(1);
		CountDownLatch releaseFirst = new CountDownLatch(1);
		AtomicBoolean firstCall = new AtomicBoolean(true);
		ScriptedModel.install((request, turn) -> {
			if (firstCall.getAndSet(false)) {
				firstWorks.countDown();
				await(releaseFirst);
			}
			return List.<ContentPart> of(new TextContent("Done."));
		});

		Window first = new Window(newChat("first"));
		Window second = new Window(newChat("second"));
		try {
			ChatProcessor.AIProcessingState firstRun = first.send();
			assertTrue("The first run did not start.", firstWorks.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			assertTrue("A run that starts at once is not announced as waiting: " + first._seen,
				first.waiting().isEmpty());

			ChatProcessor.AIProcessingState secondRun = second.send();
			List<RunWaitingEvent> waiting = second.waiting();
			assertEquals("The second send waits: " + second._seen, 1, waiting.size());
			assertEquals(1, waiting.get(0).getPosition());
			assertEquals(second._sessionId, waiting.get(0).getSessionId());
			assertTrue(waiting.get(0).getText(), waiting.get(0).getText().contains("1"));
			assertEquals("A window that connects now finds the notice in its run state.", 1,
				RunStateFrame.of(second._tracker, _owner).getWaiting().getPosition());
			assertFalse("The waiting run has not started.", second._seen.contains(STARTED));

			releaseFirst.countDown();
			firstRun._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			secondRun._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

			List<Object> seen = second._seen;
			int cleared = indexOfPosition(seen, 0);
			assertTrue("The notice was taken down: " + seen, cleared >= 0);
			assertTrue("The run started after the notice was taken down: " + seen,
				seen.indexOf(STARTED) > cleared);
			assertNull("Nothing waits any more.", secondRun.getWaiting());
		} finally {
			releaseFirst.countDown();
			first.close();
			second.close();
		}
	}

	/** A send stopped while it waits takes its notice down at once, before any worker is free. */
	public void testStoppingAWaitingSendTakesTheNoticeDown() throws Exception {
		CountDownLatch firstWorks = new CountDownLatch(1);
		CountDownLatch releaseFirst = new CountDownLatch(1);
		AtomicBoolean firstCall = new AtomicBoolean(true);
		ScriptedModel.install((request, turn) -> {
			if (firstCall.getAndSet(false)) {
				firstWorks.countDown();
				await(releaseFirst);
			}
			return List.<ContentPart> of(new TextContent("Done."));
		});

		Window first = new Window(newChat("first"));
		Window second = new Window(newChat("second"));
		try {
			ChatProcessor.AIProcessingState firstRun = first.send();
			assertTrue("The first run did not start.", firstWorks.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

			ChatProcessor.AIProcessingState secondRun = second.send();
			assertEquals(1, second.waiting().size());

			second._tracker.cancelRun(secondRun);
			List<RunWaitingEvent> waiting = second.waiting();
			assertEquals("The stop took the notice down: " + second._seen, 2, waiting.size());
			assertEquals(0, waiting.get(1).getPosition());
			assertNull(secondRun.getWaiting());

			releaseFirst.countDown();
			firstRun._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			secondRun._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertEquals("Nothing more is said about the stopped send's place.", 2, second.waiting().size());
		} finally {
			releaseFirst.countDown();
			first.close();
			second.close();
		}
	}

	private static int indexOfPosition(List<Object> seen, int position) {
		for (int i = 0; i < seen.size(); i++) {
			if (seen.get(i) instanceof RunWaitingEvent event && event.getPosition() == position) {
				return i;
			}
		}
		return -1;
	}

	private String newChat(String title) {
		String id = ChatHistoryService.createSession(title, _owner).tHandle().getObjectName().toExternalForm();
		HistoryUtils.updateSessionRevision();
		return id;
	}

	private static RunLimitsConfig oneChatWorker() {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(CapacityConfig.CHAT_WORKERS), Integer.valueOf(1));
		return config;
	}

	private static void await(CountDownLatch latch) {
		try {
			assertTrue("The test did not release the run.", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new AssertionError(ex);
		}
	}

	/** A window showing one chat, with its event stream recording what this test asks about. */
	private final class Window implements ChatEventHandler {

		final TaskProgressTracker _tracker = new TaskProgressTracker();

		final String _sessionId;

		/** The waiting announcements and run starts, in the order they arrived. */
		final List<Object> _seen = new CopyOnWriteArrayList<>();

		Window(String sessionId) {
			_sessionId = sessionId;
			assertTrue(_tracker.setActiveSessionId(sessionId, _owner));
			ChatEventHandlerRegistry.register(this);
		}

		ChatProcessor.AIProcessingState send() {
			ChatProcessor.SendResult sent = ChatProcessor.sendMessage(_tracker, TLContextManager.getSubSession(),
				_owner.getName(), "Go.", null, new ChatProcessor.ChatRunOptions(AGENT, null, false, List.of()));
			assertNotNull("The send was refused.", sent.getTaskId());
			return _tracker.getProcessingState(sent.getTaskId());
		}

		List<RunWaitingEvent> waiting() {
			return _seen.stream().filter(RunWaitingEvent.class::isInstance).map(RunWaitingEvent.class::cast).toList();
		}

		void close() {
			ChatEventHandlerRegistry.unregister(this);
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _tracker;
		}

		@Override
		public Person getAccount() {
			return _owner;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public void onEvent(AgentEvent event) {
			if (event instanceof AgentEvent.RunStarted) {
				_seen.add(STARTED);
			}
		}

		@Override
		public void sendRunWaiting(RunWaitingEvent event) {
			_seen.add(event);
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

	/** The suite, on the services a chat run passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatWaitingNotice.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
