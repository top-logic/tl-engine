/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.layout.WelcomeName;
import com.top_logic.ai.chat.view.ChatCommandDriver.Conversation;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A login in a window keeps the window's sub-session and changes its person; the chat of that
 * window follows the person.
 *
 * <p>
 * A page opened by a visitor, signed in to through the header's login dialog in the same window,
 * kept the chat's sign-in note, and the user's names were missing, because the window's binding of
 * the conversation held what the first render had found. And the conversation of one person must
 * not be the next one's after a change of the account in the window.
 * </p>
 */
public class TestChatLoginInWindow extends BasicTestCase {

	private static Person alice;

	private static Person bob;

	private ChatCommandDriver _driver;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (alice == null) {
			alice = TestPerson.createPerson("login-in-window-alice");
			bob = TestPerson.createPerson("login-in-window-bob");
		}
		_driver = new ChatCommandDriver("login-window-" + getName(), "login-" + getName());
	}

	@Override
	protected void tearDown() throws Exception {
		_driver.close();
		super.tearDown();
	}

	/** The render after the login states the user, also of the controls the visitor was shown. */
	public void testTheRenderAfterALoginStatesTheUser() throws Exception {
		Conversation visitor = _driver.window(null);
		assertEquals(Boolean.FALSE, visitor.state(ChatViewBinding.STATE_AUTHENTICATED));

		_driver.actAs(alice);
		visitor.render();
		assertEquals("A reload of the visitor's tree shows the composer.", Boolean.TRUE,
			visitor.state(ChatViewBinding.STATE_AUTHENTICATED));
		assertEquals(alice.getName(), visitor.state(ChatViewBinding.STATE_USER_NAME));

		Conversation signedIn = _driver.window(alice);
		assertEquals("The tree built for the user shows the composer.", Boolean.TRUE,
			signedIn.state(ChatViewBinding.STATE_AUTHENTICATED));
		assertEquals(alice.getName(), signedIn.state(ChatViewBinding.STATE_USER_NAME));
		assertEquals(WelcomeName.of(alice), signedIn.state(ChatViewBinding.STATE_WELCOME_NAME));
		assertSame(alice, ChatCommandDriver.tracker(signedIn).getAccount());
		visitor.cleanupTree();
		signedIn.cleanupTree();
	}

	/** After a change of the account in the window, the conversation and its commands are the new account's. */
	public void testTheNextAccountGetsAChatOfItsOwn() throws Exception {
		Conversation first = _driver.window(alice);
		String chatId = ChatHistoryService.createSession("Alice's", alice).tHandle().getObjectName().toExternalForm();
		HistoryUtils.updateSessionRevision();
		assertEquals(Boolean.TRUE, select(first, chatId).get(ChatControl.FIELD_OPENED));
		TaskProgressTracker alicesTracker = ChatCommandDriver.tracker(first);

		Conversation second = _driver.window(bob);
		TaskProgressTracker bobsTracker = ChatCommandDriver.tracker(second);
		assertNotSame("The chat Alice had open in the window is not Bob's.", alicesTracker, bobsTracker);
		assertSame(bob, bobsTracker.getAccount());
		assertNull(bobsTracker.getActiveSessionId());
		assertSame("The window's conversation is Bob's now.", bobsTracker,
			ChatSessionRegistry.tracker(TLContextManager.getSubSession(), second.binding().conversation()));
		assertEquals(bob.getName(), second.state(ChatViewBinding.STATE_USER_NAME));
		assertEquals("Bob cannot open Alice's chat.", Boolean.FALSE, select(second, chatId).get(ChatControl.FIELD_OPENED));
		first.cleanupTree();
		second.cleanupTree();
	}

	private Map<String, Object> select(ChatControl control, String chatId) {
		return _driver.command(control, ChatControl.CMD_SELECT_SESSION, Map.of(ChatControl.ARG_SESSION_ID, chatId));
	}

	/** Test suite. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatLoginInWindow.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
