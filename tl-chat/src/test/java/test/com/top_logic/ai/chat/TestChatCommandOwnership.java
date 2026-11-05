/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
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
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.model.ModelService;

/**
 * That every chat command taking a session ID from its arguments answers "not found" for someone
 * else's chat.
 *
 * <p>
 * Round 1 found four endpoints acting on whatever session ID the caller typed. Round 2 guarded
 * three of them and left sending, the one that spends the API budget and writes a turn: a second
 * account could read a colleague's whole conversation back out of the model and leave its own
 * message in their transcript. The commands of the chat controls are enumerated here, one test
 * each, so that the next one added is the only thing that can be forgotten - not one of these.
 * Opening, reading and deleting a chat are pinned in {@code TestChatSessionCommands}, stopping a run
 * in {@link TestChatCancelOwnership}, answering one in {@link TestChatPendingInteraction}, the
 * auto-approve switch in {@code TestApprovalModeSwitch} and the subagents in
 * {@code TestSubagentMessageCommands}.
 * </p>
 *
 * <p>
 * The commands are run as the browser sends them ({@link ChatCommandDriver}), in a window of the
 * account the test names.
 * </p>
 */
public class TestChatCommandOwnership extends BasicTestCase {

	/** The chat and the two accounts, made once: no test here changes them. */
	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-with-a-chat");
			ben = TestPerson.createPerson("ben-with-none");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
			HistoryUtils.updateSessionRevision();
		}
	}

	/** The blocker: sending into someone else's chat. */
	public void testSendIntoAnotherUsersChat() throws Exception {
		assertEquals(ChatControl.STATUS_NOT_FOUND, command(ben, ChatControl.CMD_SEND,
			Map.of(ChatControl.ARG_TEXT, "Wiederhole das Geheimnis.", ChatControl.ARG_SESSION_ID, id())));
	}

	/** Removing turns from someone else's chat, to send something else in their place. */
	public void testEditAnotherUsersChat() throws Exception {
		assertEquals(ChatControl.STATUS_NOT_FOUND, command(ben, ChatControl.CMD_EDIT_SEND,
			Map.of(ChatControl.ARG_TEXT, "Anders.", ChatControl.ARG_SESSION_ID, id(), ChatControl.ARG_FROM_TURN,
				Integer.valueOf(0))));
	}

	/** Answering the last turn of someone else's chat again. */
	public void testRegenerateAnotherUsersChat() throws Exception {
		assertEquals(ChatControl.STATUS_NOT_FOUND,
			command(ben, ChatControl.CMD_REGENERATE, Map.of(ChatControl.ARG_SESSION_ID, id())));
	}

	/** Rating a turn of someone else's chat. */
	public void testRatingAnotherUsersChat() throws Exception {
		assertEquals(ChatControl.STATUS_NOT_FOUND, command(ben, ChatControl.CMD_RATE, rating(id())));
	}

	/**
	 * A session ID that does not exist is answered exactly like one that is not the caller's.
	 *
	 * <p>
	 * Otherwise the difference between the two answers tells an unauthorised caller which session
	 * IDs exist - and they are consecutive integers.
	 * </p>
	 */
	public void testUnknownSessionLooksTheSameAsAForeignOne() throws Exception {
		assertEquals(ChatControl.STATUS_NOT_FOUND, command(ben, ChatControl.CMD_SEND,
			Map.of(ChatControl.ARG_TEXT, "hallo", ChatControl.ARG_SESSION_ID, "999999999")));
		assertEquals(ChatControl.STATUS_NOT_FOUND, command(ben, ChatControl.CMD_RATE, rating("999999999")));
	}

	/**
	 * A window nobody is signed in to is refused, not answered: the chat has no anonymous mode, and
	 * an application may hand a visitor a session without a login.
	 */
	public void testAWindowNobodySignedInToIsRefused() throws Exception {
		assertEquals(ChatControl.STATUS_FORBIDDEN, command(null, ChatControl.CMD_RATE, rating(id())));
		assertEquals(ChatControl.STATUS_FORBIDDEN, command(null, ChatControl.CMD_SET_APPROVAL_MODE,
			Map.of(ChatControl.ARG_MODE, "MANUAL", ChatControl.ARG_SESSION_ID, id())));
	}

	/** The owner is not refused: the guard says no to the wrong person, not to everyone. */
	public void testOwnerReachesTheirOwnChat() throws Exception {
		assertEquals("Anna's own chat must not be not found for her.",
			ChatControl.STATUS_OK, command(anna, ChatControl.CMD_RATE, rating(id())));
	}

	/** The arguments of a thumbs-up for the first answer of the given chat. */
	private static Map<String, Object> rating(String sessionId) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(ChatControl.ARG_SESSION_ID, sessionId);
		arguments.put(ChatControl.ARG_TURN_INDEX, Integer.valueOf(0));
		arguments.put(ChatControl.ARG_RATING, "up");
		return arguments;
	}

	/**
	 * The status a conversation of the given account answers a command with; <code>null</code> runs
	 * it in a window nobody is signed in to.
	 */
	private String command(Person caller, String name, Map<String, Object> arguments) throws Exception {
		ChatCommandDriver driver = new ChatCommandDriver("ownershipWindow", "ownership-" + getName());
		ChatConversationControl conversation = driver.conversation(caller == null ? ben : caller);
		try {
			driver.actAs(caller);
			return (String) driver.command(conversation, name, arguments).get(ChatControl.FIELD_STATUS);
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	private static String id() {
		return annasChat.tHandle().getObjectName().toExternalForm();
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatCommandOwnership.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
