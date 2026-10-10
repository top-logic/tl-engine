/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.top_logic.ai.chat.history.ChatScriptFunctions;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.util.model.ModelService;

/**
 * A background subagent ends long after the lead's turn that started it, and that turn was written
 * to the database when it ended - with the subagent still at work. Every window but the one that
 * started it read the conversation from there, and showed the finished subagent as running for good.
 *
 * <p>
 * The run is set up the way {@code ChatProcessor} sets one up and the subagent's end is recorded the
 * way {@code SubagentSpawner} records it: starting a real agent would need a provider and a model to
 * ask about something the log and the store decide on their own.
 * </p>
 */
public class TestSubagentEndInEveryWindow extends BasicTestCase {

	private static Person owner;

	/** The window the lead turn was started in. */
	private TaskProgressTracker _windowA;

	/** A second window that opened the chat while the subagent was at work. */
	private TaskProgressTracker _windowB;

	private ConversationLogListener _listener;

	private String _chat;

	private Watcher _watcherB;

	private AgentExecution _lead;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("mia-with-two-windows");
		}
		TLObject chat = ChatHistoryService.createSession("Zwei Fenster", owner);
		HistoryUtils.updateSessionRevision();
		_chat = chat.tHandle().getObjectName().toExternalForm();

		_windowA = new TaskProgressTracker();
		assertTrue(_windowA.getSessionManager().switchSession(_chat, owner));
		ConversationLog log = _windowA.getSessionManager().getOrCreateConversationLog();
		_listener = new ConversationLogListener(log);
		_listener.setParentListener(ChatProcessor.streamForwarder(_windowA, owner, _chat, _listener));

		// The lead's run: it starts a subagent and ends, writing its transcript, while the subagent
		// works on.
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentExecution lead = store.beginExecution((AgentSession) chat, "lead-run-" + UUID.randomUUID(),
			"Albert", "model", null, null);
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(),
			"Zähle die Anmeldungen im Hintergrund.")));
		_listener.recordSubagentStarted("sa-1", "Counter", "Anmeldungen zählen", "call-1", false, lead);
		_lead = lead;
		log.completeTurn();
		store.finishExecution(lead, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, TokenUsage.EMPTY,
			ConversationLogSerializer.toJson(List.of(turn)));
		HistoryUtils.updateSessionRevision();

		_windowB = new TaskProgressTracker();
		assertTrue(_windowB.getSessionManager().switchSession(_chat, owner));
		assertEquals("RUNNING", subagentPart(_windowB).status());
		_watcherB = new Watcher(_windowB, owner);
		ChatEventHandlerRegistry.register(_watcherB);
	}

	@Override
	protected void tearDown() throws Exception {
		ChatEventHandlerRegistry.unregister(_watcherB);
		super.tearDown();
	}

	/** A window opened after the end reads it from the database. */
	public void testANewWindowSeesTheEnd() {
		subagentFinished();

		TaskProgressTracker windowC = new TaskProgressTracker();
		assertTrue(windowC.getSessionManager().switchSession(_chat, owner));
		Part.SubagentPart part = subagentPart(windowC);
		assertEquals("DONE", part.status());
		assertEquals("Anmeldungen: 12", part.summary());
		assertEquals("Its transcript can be opened.", "child-run-1", part.childRunId());
	}

	/** A window that had the chat open reads it back with its next request. */
	public void testAnOpenWindowSeesTheEnd() {
		subagentFinished();

		assertTrue(_windowB.getSessionManager().switchSession(_chat, owner));
		assertEquals("DONE", subagentPart(_windowB).status());
	}

	/** A window showing the chat is told at once, with where the subagent's card stands. */
	public void testAnOpenWindowIsToldWhereTheCardIs() {
		subagentFinished();

		assertEquals(1, _watcherB._ended.size());
		AgentEvent.SubagentCompleted end = _watcherB._ended.get(0);
		assertEquals("sa-1", end.subagentId());
		assertEquals("call-1", end.parentToolCallId());
		assertEquals("child-run-1", end.childRunId());
	}

	/** The window that started it holds the very log the end was written into, and keeps it. */
	public void testTheStartingWindowKeepsItsCopy() {
		ConversationLog before = _windowA.getSessionManager().getOrCreateConversationLog();

		subagentFinished();

		assertTrue(_windowA.getSessionManager().switchSession(_chat, owner));
		assertSame(before, _windowA.getSessionManager().getOrCreateConversationLog());
		assertEquals("DONE", subagentPart(_windowA).status());
	}

	/**
	 * A window shows the chat as the subagent ends: the chat list does not report the end as news,
	 * because the user saw it.
	 */
	public void testAWindowShowingTheChatSeesTheEnd() throws Exception {
		childRunEnded();
		subagentFinished();

		assertNull(ChatScriptFunctions.activity(chat()));
	}

	/** No window shows the chat as the subagent ends: the chat list reports it until it is opened. */
	public void testNoWindowShowingTheChatMakesTheEndNews() throws Exception {
		ChatEventHandlerRegistry.unregister(_watcherB);
		childRunEnded();
		subagentFinished();

		assertEquals(ChatScriptFunctions.NEWS, ChatScriptFunctions.activity(chat()));
	}

	/** The subagent's own run, recorded under the lead's, ended - a moment after the chat was shown. */
	private void childRunEnded() throws InterruptedException {
		Thread.sleep(5);
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentExecution child = store.beginExecution(chat(), "child-run-1", "Counter", "model", null, _lead);
		store.finishExecution(child, ExecutionStatus.COMPLETE, StopReason.COMPLETE, null, TokenUsage.EMPTY, null,
			null);
		HistoryUtils.updateSessionRevision();
	}

	private AgentSession chat() {
		return (AgentSession) ChatHistoryService.findOwnedSessionById(_chat, owner);
	}

	private void subagentFinished() {
		_listener.recordSubagentCompleted("sa-1",
			new SubagentResult("Anmeldungen: 12", TokenUsage.EMPTY, 1000L, 2, StopReason.COMPLETE),
			"child-run-1");
		// The next request of a window moves its view to the newest commit, as the servlet does.
		HistoryUtils.updateSessionRevision();
	}

	private static Part.SubagentPart subagentPart(TaskProgressTracker window) {
		List<Part.SubagentPart> found = new ArrayList<>();
		for (Turn turn : window.getSessionManager().getOrCreateConversationLog().turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.SubagentPart subagent) {
					found.add(subagent);
				}
			}
		}
		assertEquals(1, found.size());
		return found.get(0);
	}

	/** A chat UI of a window, noting the subagent ends it is told about. */
	private static final class Watcher implements ChatEventHandler {

		private final TaskProgressTracker _window;

		private final Person _account;

		final List<AgentEvent.SubagentCompleted> _ended = new ArrayList<>();

		Watcher(TaskProgressTracker window, Person account) {
			_window = window;
			_account = account;
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _window;
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
			if (event instanceof AgentEvent.SubagentCompleted completed) {
				_ended.add(completed);
			}
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
				new TestSuite(TestSubagentEndInEveryWindow.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
