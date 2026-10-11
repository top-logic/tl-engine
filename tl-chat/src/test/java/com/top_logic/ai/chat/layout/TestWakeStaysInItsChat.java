/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.lang.ref.WeakReference;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A lead turn started for chat A's subagents runs in chat A, also when the window switches to chat B
 * while the turn is being started.
 */
public class TestWakeStaysInItsChat extends BasicTestCase {

	private static Person owner;

	private static String chatA;

	private static String chatB;

	private SwitchingWindow _window;

	private Handler _handler;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("wake-stays-in-its-chat");
			chatA = idOf(ChatHistoryService.createSession("A", owner));
			chatB = idOf(ChatHistoryService.createSession("B", owner));
			HistoryUtils.updateSessionRevision();
		}
		_window = new SwitchingWindow();
		_window.setLastSend(new ChatProcessor.LastSend(new WeakReference<>(TLContextManager.getSubSession()),
			ChatProcessor.ChatRunOptions.DEFAULT, List.of()));
		_handler = new Handler(_window);
		ChatEventHandlerRegistry.register(_handler);
	}

	@Override
	protected void tearDown() throws Exception {
		ChatEventHandlerRegistry.unregister(_handler);
		super.tearDown();
	}

	/**
	 * The user opens chat B just as a turn for chat A's news is started: no turn of A is started in
	 * B's log - the window does not show A any more, and the news waits for A.
	 */
	public void testASwitchWhileTheTurnStartsLeavesTheOtherChatAlone() {
		ChatSessionManager chats = _window.getSessionManager();
		assertTrue(chats.switchSession(chatB, owner));
		ConversationLog logB = chats.getOrCreateConversationLog();
		int turnsOfB = logB.turns().size();
		assertTrue(chats.switchSession(chatA, owner));

		_window._switchTo = chatB;
		boolean started = ChatProcessor.sendWake(_window, chatA);

		assertEquals("The window shows chat B now.", chatB, _window.getActiveSessionId());
		assertFalse("A turn of chat A is not started in the window that left it.", started);
		assertNull("Nothing runs for chat A.", _window.getProcessingStateForSession(chatA));
		assertEquals("Chat B got no turn.", turnsOfB, logB.turns().size());
	}

	/** Without a switch, the turn starts in the chat it is for. */
	public void testTheTurnRunsInItsChat() {
		ChatSessionManager chats = _window.getSessionManager();
		assertTrue(chats.switchSession(chatA, owner));
		ConversationLog logA = chats.getOrCreateConversationLog();

		assertTrue(ChatProcessor.sendWake(_window, chatA));

		ChatProcessor.AIProcessingState wake = _window.getProcessingStateForSession(chatA);
		assertNotNull(wake);
		assertSame(logA, wake.getConversationLog());
	}

	/** A run of chat A reads chat A's subagents, also after the window switched to chat B. */
	public void testARunReadsTheSubagentsOfItsOwnChat() {
		ChatSessionManager chats = _window.getSessionManager();
		assertTrue(chats.switchSession(chatA, owner));
		SubagentJobRegistry ofA = chats.getActiveSubagentRegistry();
		assertTrue(chats.switchSession(chatB, owner));
		SubagentJobRegistry ofB = chats.getActiveSubagentRegistry();
		assertNotSame(ofA, ofB);

		assertSame(ofA, chats.getSubagentRegistry(chatA));
		assertSame(ofB, chats.getSubagentRegistry(chatB));
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/**
	 * A window whose user opens another chat at the moment a turn is started: the first time the send
	 * lock is taken, the window switches.
	 */
	private static final class SwitchingWindow extends TaskProgressTracker {

		String _switchTo;

		@Override
		Object sendLock() {
			String target = _switchTo;
			if (target != null) {
				_switchTo = null;
				assertTrue(getSessionManager().switchSession(target, owner));
			}
			return super.sendLock();
		}
	}

	/** The window's open event stream; a lead turn is started only for a window somebody watches. */
	private static final class Handler implements ChatEventHandler {

		private final TaskProgressTracker _window;

		Handler(TaskProgressTracker window) {
			_window = window;
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _window;
		}

		@Override
		public Person getAccount() {
			return owner;
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

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestWakeStaysInItsChat.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
