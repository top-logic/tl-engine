/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.view.ChatSessionIds;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.model.ModelService;

/**
 * The {@link TLObject} a selection channel holds round-trips to the id {@code /chat/*} already
 * uses.
 */
public class TestChatSessionIds extends BasicTestCase {

	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-chat-session-ids");
			ben = TestPerson.createPerson("ben-chat-session-ids");
			annasChat = ChatHistoryService.createSession("Annas mapped chat", anna);
		}
		assertNotNull("The fixture needs a persisted chat.", annasChat);
	}

	/** Owner, id and object agree both ways. */
	public void testRoundTrip() {
		String id = ChatSessionIds.idOf(annasChat);
		assertFalse("The servlet id is not empty.", id.isEmpty());
		assertEquals(annasChat, ChatHistoryService.findOwnedSessionById(id, anna));
		assertEquals(annasChat, ChatSessionIds.ownedSession(id, anna));
		assertEquals(id, ChatSessionIds.selectedId(annasChat, anna));
		assertEquals(id, ChatSessionIds.idOf(ChatSessionIds.ownedSession(id, anna)));
	}

	/** Another user, or nobody, must not see the id. */
	public void testForeignAndMissingAreEmpty() {
		assertEquals("", ChatSessionIds.selectedId(annasChat, ben));
		assertEquals("", ChatSessionIds.selectedId(annasChat, null));
		assertNull(ChatSessionIds.ownedSession(ChatSessionIds.idOf(annasChat), ben));
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSessionIds.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
