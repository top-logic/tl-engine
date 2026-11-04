/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContextManager;

/**
 * The chat of a window states the account that is signed in now, not the one it was first
 * rendered for (#30109).
 *
 * <p>
 * A login in a window keeps its sub-session while the control tree is built anew. A binding that
 * wrote down the visitor's anonymity kept showing the sign-in note to the account that had just
 * signed in. A binding serves the account it was made for, so the account that signed in gets a
 * binding of its own; the state is read from the acting account on every render.
 * </p>
 */
public class TestChatLogin extends BasicTestCase {

	private Function<String, ChatPreferencesDto> _previousSource;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_previousSource = ChatViewBinding.preferenceSource;
		ChatViewBinding.preferenceSource = chatId -> ChatPreferencesDto.create();
		_previousPerson = window().getPerson();
	}

	@Override
	protected void tearDown() throws Exception {
		window().setPerson(_previousPerson);
		ChatViewBinding.preferenceSource = _previousSource;
		super.tearDown();
	}

	/** A chat first rendered for a visitor states the account that signed in afterwards. */
	public void testChatOfTheVisitorFollowsTheLogin() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		window().setPerson(null);
		ChatViewBinding visitor = ChatViewBinding.bind(scope, "main");
		Map<String, Object> before = render(visitor);
		assertEquals("A visitor sees the sign-in note.", Boolean.FALSE,
			before.get(ChatViewBinding.STATE_AUTHENTICATED));

		Person root = PersonManager.getManager().getRoot();
		window().setPerson(root);
		assertEquals("A reload of the visitor's tree shows the composer.", Boolean.TRUE,
			render(visitor).get(ChatViewBinding.STATE_AUTHENTICATED));
		ChatViewBinding signedIn = ChatViewBinding.bind(scope, "main");
		assertNotSame("The account that signed in gets a binding of its own.", visitor, signedIn);
		Map<String, Object> after = render(signedIn);
		assertEquals("The account that signed in gets the composer.", Boolean.TRUE,
			after.get(ChatViewBinding.STATE_AUTHENTICATED));
		assertEquals(root.getName(), after.get(ChatViewBinding.STATE_USER_NAME));
	}

	/** A chat first rendered for an account states that the visitor is signed out after a logout. */
	public void testChatOfTheAccountFollowsTheLogout() {
		TypedAnnotatable scope = new LazyTypedAnnotatable();
		window().setPerson(PersonManager.getManager().getRoot());
		ChatViewBinding binding = ChatViewBinding.bind(scope, "main");
		assertEquals(Boolean.TRUE, render(binding).get(ChatViewBinding.STATE_AUTHENTICATED));

		window().setPerson(null);
		assertEquals("A signed-out window shows the sign-in note again.", Boolean.FALSE,
			render(ChatViewBinding.bind(scope, "main")).get(ChatViewBinding.STATE_AUTHENTICATED));
	}

	private static TLSubSessionContext window() {
		return TLContextManager.getSubSession();
	}

	private static Map<String, Object> render(ChatViewBinding binding) {
		Map<String, Object> state = new HashMap<>();
		binding.writeRenderState(state::put);
		return state;
	}

	/** Test suite. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestChatLogin.class));
	}

}
