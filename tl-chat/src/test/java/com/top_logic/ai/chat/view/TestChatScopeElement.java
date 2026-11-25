/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A {@code <chat>} is a scope around exactly one element: the chat elements inside it join its
 * conversation, and the element itself shows up in its place unchanged.
 */
public class TestChatScopeElement extends BasicTestCase {

	private static final String WINDOW = "scopeWindow";

	private LiveWindow _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_window = LiveWindow.open(PersonManager.getManager().getRoot(), WINDOW, TLContextManager.getSubSession());
	}

	@Override
	protected void tearDown() throws Exception {
		if (_window != null) {
			_window.close();
			_window = null;
		}
		super.tearDown();
	}

	/**
	 * A chat element nested in a layout element inside the {@code <chat>} joins the conversation of
	 * the {@code <chat>}; one naming a {@code chat-id} of its own keeps it.
	 */
	public void testContentJoinsTheConversation() throws Exception {
		IReactControl control = create("<chat %s chat-id='x'>"
			+ "<stack><chat-sources/><chat-sources chat-id='own'/></stack>"
			+ "</chat>");
		try {
			assertTrue("The control of the <chat> is the control of its content: " + control,
				control instanceof ReactStackControl);
			List<ReactControl> sections = ((ReactStackControl) control).displayedChildren();
			assertEquals(2, sections.size());
			assertEquals("x", chatId(sections.get(0)));
			assertEquals("An explicit chat-id is kept inside a <chat>.", "own", chatId(sections.get(1)));
		} finally {
			((ReactControl) control).cleanupTree();
		}
	}

	/** Directly inside the {@code <chat>}, the content control is the chat element's own control. */
	public void testChatElementAsContent() throws Exception {
		IReactControl control = create("<chat %s chat-id='x'><chat-sources/></chat>");
		try {
			assertTrue("No wrapper around the content: " + control, control instanceof ChatSectionControl);
			assertEquals("x", chatId((ReactControl) control));
		} finally {
			((ReactControl) control).cleanupTree();
		}
	}

	/** Two elements in a {@code <chat>} are a configuration error that points to a layout element. */
	public void testSeveralElementsAreAnError() throws Exception {
		assertContentError("<chat %s chat-id='x'><chat-sources/><chat-sources/></chat>");
	}

	/** An empty {@code <chat>} is a configuration error as well: there is nothing to scope. */
	public void testEmptyIsAnError() throws Exception {
		assertContentError("<chat %s chat-id='x'/>");
	}

	private static void assertContentError(String xml) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(parse(xml));
		assertTrue("A <chat> must hold exactly one element: " + xml, log.hasErrors());
		assertTrue("The error points to a layout element: " + log.getError(),
			log.getError().contains("split-panel"));
	}

	private IReactControl create(String xml) throws Exception {
		ReactWindowRegistry registry = _window.registry();
		DefaultViewContext view = new DefaultViewContext(
			new DefaultReactContext("/app", WINDOW, registry.getOrCreateQueue(WINDOW), registry));
		UIElement element = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(parse(xml));
		return element.createControl(view);
	}

	private static String chatId(ReactControl control) throws Exception {
		Map<?, ?> state = (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		return (String) state.get(ChatControl.STATE_CHAT_ID);
	}

	private static ChatScopeElement.Config parse(String xml) throws Exception {
		String attributes = "xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ChatScopeElement.Config.class.getName() + "'";
		return (ChatScopeElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent(String.format(xml, attributes)));
	}

	/** The services a chat element control needs, with a person manager for the window's login. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatScopeElement.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ChatWindowService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}
