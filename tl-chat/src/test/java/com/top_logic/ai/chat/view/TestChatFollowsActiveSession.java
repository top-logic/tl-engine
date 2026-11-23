/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.ChatSendReply;
import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.ScriptedBackend;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver.Conversation;
import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.event.AgentEvent;
import com.top_logic.ai.core.agent.event.AgentEventListener;
import com.top_logic.ai.core.cost.Money;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A {@code <chat-conversation>} opens a chat the window switches to on the server - one started by
 * {@code chatStart} - by writing it onto its {@code input} channel, while the browser's own picks
 * and sends are reported by the browser and not written by the server (#30445).
 */
public class TestChatFollowsActiveSession extends BasicTestCase {

	private static final String SELECTION = "chatSession";

	private static final String WITH_INPUT = "input='" + SELECTION + "'";

	private static final long TIMEOUT_MILLIS = 60_000;

	private static Person anna;

	private static Person ben;

	private ChatCommandDriver _driver;

	private ViewChannel _channel;

	/** How often {@link #_channel} told its listeners of a write. */
	private final AtomicInteger _writes = new AtomicInteger();

	private TaskProgressTracker _sending;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-follows-her-chats");
			ben = TestPerson.createPerson("ben-has-chats-too");
		}
		_driver = new ChatCommandDriver("followWindow", "follow-" + getName());
		_channel = new DefaultViewChannel(SELECTION);
		_channel.addListener((sender, oldValue, newValue) -> _writes.incrementAndGet());
		_driver.registerChannel(SELECTION, _channel);
	}

	@Override
	protected void tearDown() throws Exception {
		if (_sending != null) {
			awaitIdle(_sending);
		}
		ScriptedBackend.reset();
		_driver.close();
		super.tearDown();
	}

	/** A switch on the server writes the configured channel, and the browser is told to open it. */
	public void testServerSwitchWritesTheConfiguredChannel() throws Exception {
		TLObject started = chat(anna, "started by a command");
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			assertTrue(tracker(conversation).setActiveSessionId(idOf(started), anna));

			assertSame(started, _channel.get());
			assertEquals(idOf(started), conversation.state(SessionSelection.SELECTED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** Without a configured {@code input}, the conversation's own channel follows the switch. */
	public void testServerSwitchWritesTheInternalChannel() throws Exception {
		TLObject started = chat(anna, "started without a channel");
		Conversation conversation = _driver.conversation(anna);
		try {
			assertEquals("", conversation.state(SessionSelection.SELECTED));

			assertTrue(tracker(conversation).setActiveSessionId(idOf(started), anna));

			assertEquals(idOf(started), conversation.state(SessionSelection.SELECTED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A switch to the session the channel already holds writes nothing again. */
	public void testTheSameSessionIsNotWrittenAgain() throws Exception {
		TLObject open = chat(anna, "already open");
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			_channel.set(open);
			assertEquals(1, _writes.get());

			assertTrue(tracker(conversation).setActiveSessionId(idOf(open), anna));
			assertTrue(tracker(conversation).setActiveSessionId(idOf(open), anna));

			assertEquals("No further write of the channel.", 1, _writes.get());
			assertSame(open, _channel.get());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A session of another account is not written, although the tracker was switched to it. */
	public void testAnotherAccountsSessionIsNotWritten() throws Exception {
		TLObject foreign = chat(ben, "Ben's own");
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			tracker(conversation).setActiveSessionId(idOf(foreign), ben);

			assertNull(_channel.get());
			assertEquals(0, _writes.get());
			assertEquals("", conversation.state(SessionSelection.SELECTED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A hidden conversation writes nothing, and opens the chat when it is displayed again. */
	public void testAHiddenConversationCatchesUpWhenDisplayed() throws Exception {
		TLObject started = chat(anna, "started while hidden");
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			conversation.detach();

			assertTrue(tracker(conversation).setActiveSessionId(idOf(started), anna));
			assertNull("A hidden conversation does not write.", _channel.get());

			conversation.attach();
			assertSame(started, _channel.get());
			assertEquals(idOf(started), conversation.state(SessionSelection.SELECTED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/**
	 * A conversation displayed again, while the window did not switch, keeps what its channel holds
	 * now - the user may have started a fresh conversation in the meantime.
	 */
	public void testADisplayWithoutSwitchKeepsTheChannel() throws Exception {
		TLObject open = chat(anna, "open before hiding");
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			assertTrue(tracker(conversation).setActiveSessionId(idOf(open), anna));
			_channel.set(null);
			conversation.detach();

			conversation.attach();

			assertNull(_channel.get());
		} finally {
			conversation.cleanupTree();
		}
	}

	/** A host that refuses the session and writes its own wins, and the browser is told so. */
	public void testARefusingHostWins() throws Exception {
		TLObject started = chat(anna, "refused by the page");
		TLObject own = chat(anna, "the page's own");
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			_channel.addListener((sender, oldValue, newValue) -> {
				if (newValue != null && newValue != own) {
					sender.set(own);
				}
			});

			assertTrue(tracker(conversation).setActiveSessionId(idOf(started), anna));

			assertSame(own, _channel.get());
			assertEquals(idOf(own), conversation.state(SessionSelection.SELECTED));
			assertEquals(Integer.valueOf(1), conversation.state(SessionSelection.CORRECTIONS));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** The browser's own pick is reported by the browser: the server does not write the channel. */
	public void testThePickOfTheBrowserIsNotWritten() throws Exception {
		TLObject picked = chat(anna, "picked in the list");
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			_driver.command(conversation, ChatControl.CMD_SELECT_SESSION,
				Map.of(ChatControl.ARG_SESSION_ID, idOf(picked)));

			assertEquals(idOf(picked), tracker(conversation).getActiveSessionId());
			assertNull(_channel.get());
			assertEquals(0, _writes.get());
		} finally {
			conversation.cleanupTree();
		}
	}

	/**
	 * The browser's first send creates a chat: the server does not write it onto the channel before
	 * the browser knows it - the browser would open it as another chat, in the middle of its turn.
	 */
	public void testTheSendOfTheBrowserIsNotWritten() throws Exception {
		ScriptedBackend.install(session -> new AnsweringBackend());
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			_sending = tracker(conversation);
			Map<String, Object> reply = _driver.command(conversation, ChatControl.CMD_SEND,
				Map.of(ChatControl.ARG_TEXT, "Hello", ChatControl.ARG_SESSION_ID, ""));
			assertEquals(reply.toString(), ChatControl.STATUS_PROCESSING, reply.get(ChatSendReply.STATUS__PROP));

			assertNotNull("The send created a chat.", _sending.getActiveSessionId());
			assertNull(_channel.get());
			assertEquals(0, _writes.get());
			assertEquals("", conversation.state(SessionSelection.SELECTED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/**
	 * A conversation first displayed after the window switched to a chat - a chat started before the
	 * tab was shown, or the page reloaded - shows that chat.
	 */
	public void testTheFirstDisplayShowsTheActiveChat() throws Exception {
		TLObject started = chat(anna, "started before the first display");
		TaskProgressTracker window =
			ChatSessionRegistry.tracker(TLContextManager.getSubSession(), _driver.chatId(anna), anna);
		assertTrue(window.setActiveSessionId(idOf(started), anna));

		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			assertSame(window, tracker(conversation));
			assertSame(started, _channel.get());
			assertEquals(idOf(started), conversation.state(SessionSelection.SELECTED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** In a window without an open chat, a conversation starts fresh: the channel stays empty. */
	public void testTheFirstDisplayWithoutActiveChatIsFresh() throws Exception {
		Conversation conversation = _driver.conversation(anna, WITH_INPUT, "");
		try {
			assertNull(tracker(conversation).getActiveSessionId());
			assertNull(_channel.get());
			assertEquals(0, _writes.get());
			assertEquals("", conversation.state(SessionSelection.SELECTED));
		} finally {
			conversation.cleanupTree();
		}
	}

	/** Answers every message at once, without a model. */
	private static final class AnsweringBackend implements ConversationBackend {

		@Override
		public String agentName() {
			return "test-backend";
		}

		@Override
		public BackendFeatures features() {
			return BackendFeatures.NONE;
		}

		@Override
		public String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) {
			String runId = "follow-" + System.nanoTime();
			TokenUsage usage = new TokenUsage(1, 1, 0, 0, 0);
			sink.onEvent(new AgentEvent.RunStarted(runId, 0, Instant.now(), null));
			sink.onEvent(new AgentEvent.TurnStarted(runId, 1, Instant.now()));
			sink.onEvent(new AgentEvent.TextDelta(runId, 1, Instant.now(), "Hi."));
			sink.onEvent(new AgentEvent.TurnCompleted(runId, 1, Instant.now(), usage, List.of()));
			sink.onEvent(new AgentEvent.RunCompleted(runId, 1, Instant.now(), StopReason.COMPLETE, usage,
				new Money(0, Money.USD)));
			return "Hi.";
		}
	}

	/** Waits until no run of the window's chat is in flight, so that none outlives its test. */
	private static void awaitIdle(TaskProgressTracker window) throws InterruptedException {
		String id = window.getActiveSessionId();
		if (id == null) {
			return;
		}
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (window.hasActiveProcessingStateFor(id)) {
			assertTrue("The run did not end.", System.currentTimeMillis() < deadline);
			Thread.sleep(20);
		}
	}

	private static TaskProgressTracker tracker(ChatControl control) {
		return ChatCommandDriver.tracker(control);
	}

	private static TLObject chat(Person owner, String title) {
		TLObject session = ChatHistoryService.createSession(title, owner);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	private static String idOf(TLObject session) {
		return ChatSessionIds.idOf(session);
	}

	/** The suite, on the services a chat request and a backend's run pass through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatFollowsActiveSession.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}
}
