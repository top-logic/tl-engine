/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver;
import com.top_logic.ai.chat.view.ChatControl;
import com.top_logic.ai.chat.view.ChatConversationControl;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.model.ModelService;

/**
 * The chat's "auto-approve" switch is a setting of the conversation (#658).
 *
 * <p>
 * The switch set the mode on the window's tracker only. The conversation stored the mode only when a
 * turn was saved, and every window that opened the chat read the stored mode back - so a chat whose
 * last turn ran with auto-approve came back with it on after the user had turned it off, and the next
 * tool call ran without asking. Turning auto-approve off is how a user has every write confirmed; it
 * must not be undone by anything but the switch.
 * </p>
 */
public class TestApprovalModeSwitch extends BasicTestCase {

	private static Person anna;

	private static Person ben;

	/** The window the switch is turned in: a conversation of Anna's. */
	private ChatCommandDriver _driver;

	private ChatConversationControl _conversation;

	private TaskProgressTracker _window;

	private TLObject _chat;

	private final List<ChatEventHandler> _handlers = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-switches-approval");
			ben = TestPerson.createPerson("ben-switches-approval");
		}
		_chat = ChatHistoryService.createSession("Mit Freigabe", anna);
		HistoryUtils.updateSessionRevision();

		// The window the commands come from.
		_driver = new ChatCommandDriver("approvalWindow", "approval-" + getName());
		_conversation = _driver.conversation(anna);
		_window = ChatCommandDriver.tracker(_conversation);

		// A turn ran with auto-approve on, and was saved with it.
		ranATurn(_window, ApprovalMode.AUTO_APPROVE);
	}

	@Override
	protected void tearDown() throws Exception {
		for (ChatEventHandler handler : _handlers) {
			ChatEventHandlerRegistry.unregister(handler);
		}
		_conversation.cleanupTree();
		_driver.close();
		super.tearDown();
	}

	/** Turning auto-approve off reaches every window that opens the chat afterwards: a new login. */
	public void testSwitchingOffIsStoredAtOnce() throws Exception {
		assertEquals(ChatControl.STATUS_OK, switchTo(ApprovalMode.MANUAL, anna));

		assertEquals(ApprovalMode.MANUAL, modeANewWindowOpens());
	}

	/**
	 * A window that opened the chat before the switch was turned off still holds the old mode in its
	 * tracker. Saving its metadata - a rating, a turn, moving to another chat - must not turn
	 * auto-approve back on.
	 */
	public void testAnOlderWindowDoesNotTurnItBackOn() throws Exception {
		TaskProgressTracker older = new TaskProgressTracker();
		older.setActiveSessionId(id(), anna);
		assertEquals(ApprovalMode.AUTO_APPROVE, older.getApprovalMode());

		switchTo(ApprovalMode.MANUAL, anna);
		older.getSessionManager().persistActiveSession();

		assertEquals(ApprovalMode.MANUAL, modeANewWindowOpens());
	}

	/**
	 * A window of the account that shows the chat is told the new mode at once, so its switch does
	 * not go on claiming a mode its runs no longer have, and its next request reads the chat again.
	 */
	public void testAWindowShowingTheChatIsTold() throws Exception {
		TaskProgressTracker other = new TaskProgressTracker();
		other.setActiveSessionId(id(), anna);
		Listening handler = listen(other, anna);

		switchTo(ApprovalMode.MANUAL, anna);

		assertEquals(List.of(ApprovalMode.MANUAL), handler._modes);
		other.setActiveSessionId(id(), anna);
		assertEquals("Its next request runs with the mode the chat now has.",
			ApprovalMode.MANUAL, other.getApprovalMode());
	}

	/**
	 * Opening a chat answers with its mode, which is what the browser's switch shows. A window that
	 * was opened before - showing the mode of its page load - would otherwise show the switch off
	 * while the chat's runs approve by themselves.
	 */
	public void testOpeningAChatAnswersItsMode() throws Exception {
		TaskProgressTracker newWindow = new TaskProgressTracker();
		assertEquals(ApprovalMode.MANUAL, newWindow.getApprovalMode());

		// What the open does - the selectSession command of a chat control - and what is read then.
		assertTrue(newWindow.getSessionManager().switchSession(id(), anna));
		String opened = DtoMapper.toJson(ChatTranscripts.sessionDetail(newWindow, anna, id()));

		assertTrue("The chat is opened with the mode it stores: " + opened,
			opened.contains("\"approvalMode\":\"AUTO_APPROVE\""));
	}

	/**
	 * A run of the chat that works right now reads the switch at every approval, from the chat it
	 * was started with - which is another window's copy when another window started it (#943).
	 */
	public void testARunStartedInAnotherWindowTakesTheSwitch() throws Exception {
		TaskProgressTracker runWindow = new TaskProgressTracker();
		runWindow.setActiveSessionId(id(), anna);
		AIProcessingState run = new AIProcessingState();
		String taskId = UUID.randomUUID().toString();
		run.setCurrentTaskId(taskId);
		run.setSessionId(id());
		run.setChatState(runWindow.activeChat());
		run.startProcessing();
		PendingInteractionRegistry.register(taskId, anna, runWindow, run);
		try {
			assertEquals(ApprovalMode.AUTO_APPROVE, run.getChatState().approvalMode());

			switchTo(ApprovalMode.MANUAL, anna);
			assertEquals("Switched off in this window, the run in the other one asks again.",
				ApprovalMode.MANUAL, run.getChatState().approvalMode());

			switchTo(ApprovalMode.AUTO_APPROVE, anna);
			assertEquals("Switched on again, it stops asking.",
				ApprovalMode.AUTO_APPROVE, run.getChatState().approvalMode());
		} finally {
			PendingInteractionRegistry.unregister(taskId);
		}
	}

	/** Somebody else's chat is not switched, and the answer says nothing about it existing. */
	public void testSomeoneElsesChatIsNotSwitched() throws Exception {
		assertEquals(ChatControl.STATUS_NOT_FOUND, switchTo(ApprovalMode.MANUAL, ben));

		assertEquals(ApprovalMode.AUTO_APPROVE, modeANewWindowOpens());
	}

	/**
	 * A chat the server opens without a mode runs manual (#808); the browser names the person's
	 * choice with the send that creates a chat (see
	 * {@code TestChatRunAccess#testAChatIsCreatedWithTheModeItsSendNames}).
	 */
	public void testANewChatStartsManual() throws Exception {
		assertEquals(ApprovalMode.AUTO_APPROVE, _window.getApprovalMode());

		TLObject created = ChatHistoryService.createSession("Neu", anna);
		HistoryUtils.updateSessionRevision();
		_window.setActiveSessionId(idOf(created), anna);
		assertEquals(ApprovalMode.MANUAL, _window.getApprovalMode());

		_window.getSessionManager().persistActiveSession();
		HistoryUtils.updateSessionRevision();
		assertTrue("The first save stores the new chat manual: " + ChatHistoryService.getMetadataJson(created),
			ChatHistoryService.getMetadataJson(created).contains("\"approvalMode\":\"MANUAL\""));
		TaskProgressTracker fresh = new TaskProgressTracker();
		fresh.setActiveSessionId(idOf(created), anna);
		assertEquals(ApprovalMode.MANUAL, fresh.getApprovalMode());
	}

	/**
	 * The switch belongs to the person: a switch in a conversation that has no chat yet is stored as
	 * the person's choice, which a new chat starts with.
	 */
	public void testASwitchWithoutAChatStoresThePersonsChoice() throws Exception {
		assertEquals(ChatControl.STATUS_OK, command(anna, Map.of(ChatControl.ARG_MODE, ApprovalMode.AUTO_APPROVE.name())));
		assertEquals(ApprovalMode.AUTO_APPROVE, ApprovalChoice.load());

		switchTo(ApprovalMode.MANUAL, anna);
		assertEquals("A switch in a chat is the person's choice as well.", ApprovalMode.MANUAL,
			ApprovalChoice.load());
	}

	/**
	 * A turn runs with the position the switch shows at the send. A switch request still on its way
	 * - or one that failed - left the server on the old mode: the turn then waited for a click while
	 * the browser answered by itself.
	 */
	public void testASendTakesTheModeTheSwitchShows() throws Exception {
		_driver.command(_conversation, ChatControl.CMD_SEND, Map.of(ChatControl.ARG_TEXT, "Hallo",
			ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_APPROVAL_MODE, ApprovalMode.MANUAL.name()));

		assertEquals(ApprovalMode.MANUAL, _window.getApprovalMode());
		assertEquals(ApprovalMode.MANUAL, modeANewWindowOpens());
	}

	// --- Fixture ---

	private void ranATurn(TaskProgressTracker window, ApprovalMode mode) {
		window.setActiveSessionId(id(), anna);
		window.setApprovalMode(mode);
		window.getSessionManager().persistActiveSession();
		HistoryUtils.updateSessionRevision();
	}

	private ApprovalMode modeANewWindowOpens() {
		HistoryUtils.updateSessionRevision();
		TaskProgressTracker fresh = new TaskProgressTracker();
		fresh.setActiveSessionId(id(), anna);
		return fresh.getApprovalMode();
	}

	/** The status the switch to the given mode of the test's chat, turned by the given account, got. */
	private String switchTo(ApprovalMode mode, Person caller) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(ChatControl.ARG_MODE, mode.name());
		arguments.put(ChatControl.ARG_SESSION_ID, id());
		String status = command(caller, arguments);
		HistoryUtils.updateSessionRevision();
		return status;
	}

	/** The status a switch with the given arguments, turned by the given account, got. */
	private String command(Person caller, Map<String, Object> arguments) {
		_driver.actAs(caller);
		return (String) _driver.command(_conversation, ChatControl.CMD_SET_APPROVAL_MODE, arguments)
			.get(ChatControl.FIELD_STATUS);
	}

	private Listening listen(TaskProgressTracker tracker, Person account) {
		Listening handler = new Listening(tracker, account);
		ChatEventHandlerRegistry.register(handler);
		_handlers.add(handler);
		return handler;
	}

	private String id() {
		return idOf(_chat);
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/** A chat UI of another window, recording the modes it is told. */
	private static final class Listening implements ChatEventHandler {

		private final TaskProgressTracker _tracker;

		private final Person _account;

		final List<ApprovalMode> _modes = new ArrayList<>();

		Listening(TaskProgressTracker tracker, Person account) {
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
			// Not what this test is about.
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			// Not what this test is about.
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			// Not what this test is about.
		}

		@Override
		public void sendApprovalMode(ApprovalMode mode) {
			_modes.add(mode);
		}
	}

	/** The knowledge base, the chat model and a login. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestApprovalModeSwitch.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
