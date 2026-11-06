/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.messages.ChatSendReply;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver;
import com.top_logic.ai.chat.view.ChatControl;
import com.top_logic.ai.chat.view.ChatConversationControl;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That a window showing two conversations has a {@link TaskProgressTracker} for each, and that a
 * command acts on the conversation of the control it is sent to.
 *
 * <p>
 * A window can show more than one conversation - a page's chat beside a {@code <chat-dock>}'s, a
 * chat in a dialog over the one on the page. The window used to have one tracker: the two
 * conversations shared one active session and one processing state, the second one to send took
 * the first one's event stream, and the first waited for an answer that never arrived. Each
 * conversation now has a tracker of its own, and every command of the browser goes to a chat control
 * of one conversation ({@link ChatControl}), which acts on that conversation's tracker. A command to
 * a control the window does not hold is not the chat's to answer: the engine's command channel
 * reloads such a page as a stale UI.
 * </p>
 */
public class TestChatConversationRouting extends BasicTestCase {

	private static final String PAGE = "chat";

	private static final String DOCK = "dock";

	private static Person user;

	private TLSubSessionContext _subSession;

	private String _windowId;

	/** The conversations a test sent commands to, closed after it. */
	private final List<Shown> _shown = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("user-with-two-conversations");
		}
		_subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the trackers on.", _subSession);
		_subSession.setPerson(user);
		// The name of the window the conversations are rendered in.
		_windowId = "test-window-" + System.identityHashCode(this);
	}

	@Override
	protected void tearDown() throws Exception {
		for (Shown shown : _shown) {
			shown.close();
		}
		_shown.clear();
		super.tearDown();
	}

	/** Two conversations rendered in one window are two trackers, not one. */
	public void testEachConversationOfAWindowHasItsOwnTracker() {
		TaskProgressTracker page = register(PAGE);
		TaskProgressTracker dock = register(DOCK);

		assertNotSame("The page's chat and the dock's must not share one tracker.", page, dock);
		assertSame(page, ChatSessionRegistry.tracker(_subSession, PAGE));
		assertSame(dock, ChatSessionRegistry.tracker(_subSession, DOCK));
		assertTrue("Both conversations belong to the window.",
			ChatSessionRegistry.trackers(_subSession).containsAll(List.of(page, dock)));
	}

	/**
	 * A lookup that names no conversation finds the window's unnamed one, which is what a host that
	 * shows one chat per window registers.
	 */
	public void testALookupNamingNoConversationGetsTheUnnamedOne() {
		TaskProgressTracker unnamed = register(null);

		assertSame(unnamed, ChatSessionRegistry.tracker(_subSession, ""));
		assertSame(unnamed, ChatSessionRegistry.tracker(_subSession, "   "));
	}

	/** A conversation this window never rendered resolves to nothing. */
	public void testAConversationNobodyRenderedIsUnknown() {
		register(PAGE);

		assertNull(ChatSessionRegistry.tracker(_subSession, "never-rendered"));
	}

	/**
	 * A command acts on the conversation of the control it is sent to, and on no other.
	 *
	 * <p>
	 * The auto-approve switch is the probe because it writes one field of the addressed tracker,
	 * besides the chat it names. With one tracker per window, switching the dock to auto-approve
	 * switched the page's chat with it - which is the same collision that let the two take each
	 * other's runs.
	 * </p>
	 */
	public void testACommandActsOnItsOwnConversationOnly() throws Exception {
		Shown page = show(PAGE);
		Shown dock = show(DOCK);
		assertNotSame(page.tracker(), dock.tracker());
		page.tracker().setApprovalMode(ApprovalMode.MANUAL);
		dock.tracker().setApprovalMode(ApprovalMode.MANUAL);
		String chatId = idOf(ChatHistoryService.createSession("Im Dock", user));
		HistoryUtils.updateSessionRevision();
		// The switch sits in a conversation that shows the chat; it does not open the chat itself.
		assertTrue(dock.tracker().setActiveSessionId(chatId, user));

		assertEquals(ChatControl.STATUS_OK, dock.command(ChatControl.CMD_SET_APPROVAL_MODE, switchTo(chatId)));

		assertEquals("The dock's conversation was the one addressed.",
			ApprovalMode.AUTO_APPROVE, dock.tracker().getApprovalMode());
		assertEquals("The page's conversation was not addressed and must not have changed.",
			ApprovalMode.MANUAL, page.tracker().getApprovalMode());
	}

	/**
	 * Rating an answer and switching auto-approve act on the chat they name, and leave the chat the
	 * conversation shows where it is.
	 *
	 * <p>
	 * Both used to switch the conversation to the named chat before they wrote, so a rating or a
	 * switch sent for a chat the conversation had moved away from pulled the window back to it.
	 * </p>
	 */
	public void testActingOnAnotherChatKeepsTheShownOne() throws Exception {
		Shown conversation = show(PAGE);
		TaskProgressTracker page = conversation.tracker();
		String shown = idOf(ChatHistoryService.createSession("Gezeigt", user));
		String cached = idOf(ChatHistoryService.createSession("Vorher gezeigt", user));
		String unheld = idOf(ChatHistoryService.createSession("Nie geladen", user));
		HistoryUtils.updateSessionRevision();
		// The window held "cached" before it moved on to "shown".
		assertTrue(page.setActiveSessionId(cached, user));
		assertTrue(page.setActiveSessionId(shown, user));
		page.setApprovalMode(ApprovalMode.MANUAL);

		assertEquals(ChatControl.STATUS_OK, conversation.command(ChatControl.CMD_SET_APPROVAL_MODE, switchTo(cached)));
		assertEquals(ChatControl.STATUS_OK, conversation.command(ChatControl.CMD_SET_APPROVAL_MODE, switchTo(unheld)));
		Map<String, Object> rating = new HashMap<>();
		rating.put(ChatControl.ARG_SESSION_ID, unheld);
		rating.put(ChatControl.ARG_TURN_INDEX, Integer.valueOf(0));
		rating.put(ChatControl.ARG_RATING, "up");
		assertEquals(ChatControl.STATUS_OK, conversation.command(ChatControl.CMD_RATE, rating));
		HistoryUtils.updateSessionRevision();

		assertEquals("The window still shows the chat it showed.", shown, page.getActiveSessionId());
		assertEquals("The shown chat's mode is untouched.", ApprovalMode.MANUAL, page.getApprovalMode());
		assertEquals("The window's copy of the switched chat follows the switch.",
			ApprovalMode.AUTO_APPROVE, page.getSessionManager().getChatState(cached).approvalMode());
		assertNull("A chat the window did not hold is not loaded into it.",
			page.getSessionManager().getChatState(unheld));
		String stored = ChatHistoryService.getMetadataJson(ChatHistoryService.findOwnedSessionById(unheld, user));
		assertTrue("The rating is stored with the chat it names: " + stored, stored.contains("\"rating\":\"up\""));
		assertTrue("The rating keeps the stored mode: " + stored, stored.contains("\"approvalMode\":\"AUTO_APPROVE\""));
	}

	private static Map<String, Object> switchTo(String chatId) {
		return Map.of(ChatControl.ARG_MODE, ApprovalMode.AUTO_APPROVE.name(), ChatControl.ARG_SESSION_ID, chatId);
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	private TaskProgressTracker register(String conversation) {
		TaskProgressTracker tracker = new TaskProgressTracker();
		ChatSessionRegistry.register(_subSession, conversation, tracker);
		return tracker;
	}

	/** A conversation of the test's window, rendered and displayed for the user. */
	private Shown show(String chatId) throws Exception {
		Shown shown = new Shown(_windowId, "routing-" + getName() + "-" + chatId);
		_shown.add(shown);
		return shown;
	}

	/** One conversation of the window and the driver that sends its commands. */
	private static final class Shown {

		private final ChatCommandDriver _driver;

		private final ChatConversationControl _conversation;

		Shown(String window, String chatId) throws Exception {
			_driver = new ChatCommandDriver(window, chatId);
			_conversation = _driver.conversation(user);
		}

		TaskProgressTracker tracker() {
			return ChatCommandDriver.tracker(_conversation);
		}

		/** The status the command answered with. */
		String command(String name, Map<String, Object> arguments) {
			return (String) _driver.command(_conversation, name, arguments).get(ChatSendReply.STATUS__PROP);
		}

		void close() {
			_conversation.cleanupTree();
			_driver.close();
		}
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatConversationRouting.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
