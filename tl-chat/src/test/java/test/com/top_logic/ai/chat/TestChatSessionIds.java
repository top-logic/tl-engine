/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.view.ChatSessionIds;
import com.top_logic.ai.chat.view.SecurityCheckedTestCase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * The {@link TLObject} a selection channel holds round-trips to the id the chat's commands
 * use.
 */
public class TestChatSessionIds extends SecurityCheckedTestCase {

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
		assertEquals(annasChat, ChatHistoryService.findAccessibleSession(id, anna, SimpleBoundCommandGroup.READ));
		assertEquals(annasChat, ChatSessionIds.accessibleSession(id, anna, SimpleBoundCommandGroup.READ));
		assertEquals(id, ChatSessionIds.selectedId(annasChat, anna));
		assertEquals(id, ChatSessionIds.idOf(ChatSessionIds.accessibleSession(id, anna, SimpleBoundCommandGroup.READ)));
	}

	/** Another user, or nobody, must not see the id. */
	public void testForeignAndMissingAreEmpty() {
		assertEquals("", ChatSessionIds.selectedId(annasChat, ben));
		assertEquals("", ChatSessionIds.selectedId(annasChat, null));
		assertNull(ChatSessionIds.accessibleSession(ChatSessionIds.idOf(annasChat), ben, SimpleBoundCommandGroup.READ));
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSessionIds.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
