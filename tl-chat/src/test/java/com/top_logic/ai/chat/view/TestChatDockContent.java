/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A conversation in a closed {@code <chat-dock>} offers the agent nothing, although the dock keeps
 * it displayed: the registrations of one conversation are those of the place the user sees it in.
 *
 * <p>
 * The dock and a page show the same conversation (the default chat id), each with a channel of its
 * own under the name {@link #PANE}, the way a dock embedding the shipped chat page and that page
 * have.
 * </p>
 */
public class TestChatDockContent extends BasicTestCase {

	/** The channel both conversations offer to the agent. */
	private static final String PANE = "paneObject";

	private static Person user;

	private LiveWindow _window;

	private Person _previousAccount;

	private ViewChannel _pageChannel;

	private ViewChannel _dockChannel;

	private ChatConversationControl _page;

	private ChatDockControl _dock;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_previousAccount = subSession().getPerson();
		subSession().setPerson(PersonManager.getManager().getRoot());
		if (user == null) {
			user = TestPerson.createPerson("docker");
		}
		String window = "dockWindow";
		_window = LiveWindow.open(user, window, subSession());
		ReactWindowRegistry registry = _window.registry();

		DefaultViewContext pageView = new DefaultViewContext(
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry));
		_pageChannel = new DefaultViewChannel(PANE);
		pageView.registerChannel(PANE, _pageChannel);
		_page = new ChatConversationControl(pageView, (ChatConversationElement.Config) parse(
			"<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
				+ " present='" + PANE + "'/>"));

		// The dock's channels are its own, as those of a view embedded with <view-ref>.
		DefaultViewContext dockView = new DefaultViewContext(
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry));
		_dockChannel = new DefaultViewChannel(PANE);
		dockView.registerChannel(PANE, _dockChannel);
		UIElement dockElement = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(
			(ChatDockElement.Config) parse(
				"<chat-dock xmlns:config='http://www.top-logic.com/ns/config/6.0'"
					+ " config:interface='" + ChatDockElement.Config.class.getName() + "'>"
					+ "<chat-conversation present='" + PANE + "'/>"
					+ "</chat-dock>"));
		_dock = (ChatDockControl) dockElement.createControl(dockView);
	}

	@Override
	protected void tearDown() throws Exception {
		_page.cleanupTree();
		_dock.cleanupTree();
		if (_window != null) {
			_window.close();
			_window = null;
		}
		subSession().setPerson(_previousAccount);
		super.tearDown();
	}

	/**
	 * A page showing the conversation beside a closed dock: the agent presents on the page's
	 * channel, and the dock's conversation offers nothing although it is displayed.
	 */
	public void testAClosedDockOffersNothing() {
		assertFalse("A dock is closed unless configured open.", _dock.isOpen());
		_dock.attach();
		assertTrue("The closed dock's content stays displayed.", dockConversation().isAttached());
		assertEquals("A conversation in a closed dock offers nothing.", Set.of(), presentableNames());

		_page.attach();
		assertSame("The page is where the agent presents.", _pageChannel, binding().presentable(PANE));

		_page.detach();
		assertEquals("A closed dock on another page offers nothing either.", Set.of(), presentableNames());
	}

	/**
	 * A dock opened on a page that does not show the conversation offers what its conversation
	 * offers, and closing it withdraws that again.
	 */
	public void testOpeningTheDockOffersItsConversation() {
		_dock.attach();

		_dock.setOpen(Map.of(ChatDockControl.ARG_OPEN, Boolean.TRUE));
		assertTrue(_dock.isOpen());
		assertSame("The open dock is where the agent presents.", _dockChannel, binding().presentable(PANE));

		_dock.setOpen(Map.of(ChatDockControl.ARG_OPEN, Boolean.FALSE));
		assertFalse(_dock.isOpen());
		assertTrue("Closing hides the content, it does not drop it.", dockConversation().isAttached());
		assertEquals("Closing the dock withdraws what its conversation offers.", Set.of(), presentableNames());

		_dock.setOpen(Map.of(ChatDockControl.ARG_OPEN, Boolean.TRUE));
		assertSame("Opened again.", _dockChannel, binding().presentable(PANE));

		_dock.detach();
		assertEquals("An open dock that is not displayed offers nothing.", Set.of(), presentableNames());
	}

	/** The conversation the dock frames. */
	private ChatConversationControl dockConversation() {
		List<ReactControl> children = _dock.displayedChildren();
		assertEquals(1, children.size());
		assertTrue("The dock's content is its conversation: " + children,
			children.get(0) instanceof ChatConversationControl);
		return (ChatConversationControl) children.get(0);
	}

	private static ChatViewBinding binding() {
		return ChatViewBinding.bind(subSession(), ChatScope.DEFAULT_CHAT_ID);
	}

	private static Set<String> presentableNames() {
		return ChatViewBinding.presentableNames(subSession(), null);
	}

	private static ConfigurationItem parse(String xml) throws Exception {
		return TypedConfiguration.parse(CharacterContents.newContent(xml));
	}

	private static TLSubSessionContext subSession() {
		return TLContextManager.getSubSession();
	}

	/**
	 * The services a chat conversation control needs, with a person manager for the window's login.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatDockContent.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ChatWindowService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}
