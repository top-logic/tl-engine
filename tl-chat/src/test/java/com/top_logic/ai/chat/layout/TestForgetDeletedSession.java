/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.model.ModelService;

/**
 * That {@link ChatSessionManager#forgetSession(String)} lets go of a chat that was deleted.
 *
 * <p>
 * The window holds the open chat in memory - its messages, its conversation memory, its task store
 * - and keeps its id as the active one. Deleting that chat elsewhere (a row command, another tab,
 * the REST endpoint) leaves the window pointing at a session that no longer exists, and the next
 * message would be written into it. {@code AgentViewRefreshService} calls this for every window of
 * the account when the store reports a deletion.
 * </p>
 */
public class TestForgetDeletedSession extends BasicTestCase {

	/** Made once: no test here changes the account. */
	private static Person owner;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("forget-session-owner");
		}
	}

	/** The open chat is let go of: no longer active, and no longer cached. */
	public void testTheOpenChatIsLetGoOf() throws Exception {
		TLObject session = ChatHistoryService.createSession("Offener Chat", owner);
		HistoryUtils.updateSessionRevision();
		String id = idOf(session);

		ChatSessionManager manager = new ChatSessionManager();
		assertTrue("The test needs the chat to be the open one.", manager.switchSession(id, owner));
		assertEquals(id, manager.getActiveSessionId());

		assertTrue("The manager held it.", manager.forgetSession(id));

		assertNull("Nothing may still be open.", manager.getActiveSessionId());
		assertNull("...and nothing may still be written as its owner.", manager.getActiveOwner());
	}

	/** A chat the window never opened, and a nonsense id, change nothing. */
	public void testAnUnheldSessionIsANoOp() throws Exception {
		ChatSessionManager manager = new ChatSessionManager();

		assertFalse(manager.forgetSession("999999999"));
		assertFalse(manager.forgetSession(""));
		assertFalse(manager.forgetSession(null));
	}

	/** Deleting some other chat leaves the open one open. */
	public void testAnotherChatDoesNotCloseThisOne() throws Exception {
		TLObject open = ChatHistoryService.createSession("Bleibt offen", owner);
		TLObject other = ChatHistoryService.createSession("Wird gelöscht", owner);
		HistoryUtils.updateSessionRevision();

		ChatSessionManager manager = new ChatSessionManager();
		assertTrue(manager.switchSession(idOf(open), owner));

		assertFalse("The other chat was never held here.", manager.forgetSession(idOf(other)));
		assertEquals("The open chat stays open.", idOf(open), manager.getActiveSessionId());
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/** Creates the test suite: KB + PersonManager (for a real owner) around the store. */
	public static Test suite() {
		Test test = new TestSuite(TestForgetDeletedSession.class);
		test = ServiceTestSetup.createSetup(test,
			ModelService.Module.INSTANCE,
			AgentExecutionStore.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}
}
