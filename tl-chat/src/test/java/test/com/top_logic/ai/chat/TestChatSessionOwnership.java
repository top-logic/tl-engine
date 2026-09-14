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
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.model.ModelService;

/**
 * That a chat belongs to one person, on every path that can reach it by its ID.
 *
 * <p>
 * Chat session IDs are small sequential integers that arrive in a request body, so "the session the
 * caller named" and "the caller's session" are two different things, and every path that confuses
 * them hands one user another user's conversation. That has now happened twice: round 1 left it
 * open on four endpoints, round 2 closed three of them and left {@code POST /chat/send}, where a
 * second account could read a colleague's whole chat out of the model and write a turn into their
 * transcript.
 * </p>
 *
 * <p>
 * These tests do not go through the servlet - they go through what the servlet calls, which is
 * where the guard now lives: loading a conversation into a window, and writing one back. A handler
 * that forgets to ask gets nothing from either.
 * </p>
 */
public class TestChatSessionOwnership extends BasicTestCase {

	/**
	 * Two accounts and one chat, made once for the whole suite.
	 *
	 * <p>
	 * Accounts are not created and dropped per test: an account that owns a chat cannot be deleted
	 * while the chat still refers to it, and the fixture is read-only anyway - no test here changes
	 * either of them.
	 * </p>
	 */
	private static Person anna;

	private static Person ben;

	private static TLObject annasChat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-owns-chats");
			ben = TestPerson.createPerson("ben-owns-none");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
		}
		assertNotNull("The fixture needs a persisted chat.", annasChat);
	}

	/** The owner finds their own chat. */
	public void testOwnerFindsTheirChat() {
		assertEquals(annasChat, ChatHistoryService.findOwnedSessionById(id(annasChat), anna));
	}

	/**
	 * Another signed-in user finds nothing, and cannot tell that from the chat not existing.
	 *
	 * <p>
	 * Both answer {@code null}, which is what lets the handlers answer 404 either way: which of the
	 * two it is, is not the caller's business - and telling them apart would turn the endpoint into
	 * an oracle for which session IDs exist.
	 * </p>
	 */
	public void testAnotherUserFindsNothing() {
		assertNull("Ben must not reach Anna's chat.",
			ChatHistoryService.findOwnedSessionById(id(annasChat), ben));
		assertNull("An unknown ID answers exactly the same way.",
			ChatHistoryService.findOwnedSessionById("999999999", ben));
	}

	/** Nobody is not a user: an unauthenticated request owns nothing. */
	public void testNobodyOwnsNothing() {
		assertNull(ChatHistoryService.findOwnedSessionById(id(annasChat), null));
		assertNull(ChatHistoryService.findOwnedSessionById(null, anna));
		assertNull(ChatHistoryService.findOwnedSessionById("", anna));
	}

	/**
	 * The exploit, at the level it was exploited.
	 *
	 * <p>
	 * {@code POST /chat/send} handed the body's session ID to the session manager, which loaded the
	 * conversation into the caller's memory - that is how ben read anna's chat back out of the
	 * model. The manager now refuses, so even a handler that forgot to ask leaves the caller with
	 * the session they had.
	 * </p>
	 */
	public void testSwitchingToAnotherUsersChatIsRefused() {
		ChatSessionManager manager = new ChatSessionManager();

		assertFalse("Ben must not be able to open Anna's chat.",
			manager.switchSession(id(annasChat), ben));
		assertNull("Nothing was activated.", manager.getActiveSessionId());
		assertNull(manager.getActiveOwner());

		assertFalse("Nor may an unauthenticated caller.", manager.switchSession(id(annasChat), null));
		assertNull(manager.getActiveSessionId());
	}

	/** The owner's switch works, and records whose session is active. */
	public void testSwitchingToOwnChatWorks() {
		ChatSessionManager manager = new ChatSessionManager();

		assertTrue(manager.switchSession(id(annasChat), anna));
		assertEquals(id(annasChat), manager.getActiveSessionId());
		assertEquals(anna, manager.getActiveOwner());
	}

	/**
	 * A run is recorded against a chat only if it is the acting person's.
	 *
	 * <p>
	 * This is the write half of the same defect: the turn ben sent landed in anna's persisted
	 * transcript. Answering {@code null} here means the run is not recorded at all, which is the
	 * right outcome - a turn belongs in its own user's history or in none.
	 * </p>
	 */
	public void testRunIsNotRecordedIntoAnotherUsersChat() {
		assertNotNull("Anna's own run is recorded.",
			ChatProcessor.resolveAgentSession(id(annasChat), anna));
		assertNull("Ben's run must not append to Anna's transcript.",
			ChatProcessor.resolveAgentSession(id(annasChat), ben));
		assertNull("Neither may an unauthenticated one.",
			ChatProcessor.resolveAgentSession(id(annasChat), null));
	}

	/** The session ID exactly as the servlet hands it to the client and reads it back. */
	private static String id(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatSessionOwnership.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
