/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.AvatarState;
import com.top_logic.layout.react.state.Badge;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.state.TabBarState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.element.WithBadge;

/**
 * Tests that a button, a tab of a tab bar, an avatar and an item of a bottom bar declared {@link WithBadge} send the badge
 * of their channel to the client, in the role and with the name declared, and follow the channel.
 */
public class TestBadgedElements extends BasicTestCase {

	/** The view the elements under test are written in. */
	private static final String FIXTURE = "badges.view.xml";

	/** Name of the channel the badges read. */
	private static final String COUNT_CHANNEL = "count";

	/** The state field of a bottom bar holding its items. */
	private static final String BOTTOM_BAR_ITEMS = "items";

	/** The wire name of the badge of an item of a bottom bar. */
	private static final String BOTTOM_BAR_BADGE = "badge";

	/** Name of the channel the avatar shows. */
	private static final String PERSON_CHANNEL = "person";

	private ViewChannel _count;

	private ViewContext _context;

	private List<UIElement> _elements;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_count = new DefaultViewChannel(COUNT_CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(COUNT_CHANNEL, _count);
		_context.registerChannel(PERSON_CHANNEL, new DefaultViewChannel(PERSON_CHANNEL));
		ChildGroup group = new FixtureViews(TestBadgedElements.class).getView(FIXTURE).getChildGroups().get(0);
		UIElement stack = ((ChildGroup.Elements) group).children().get(0);
		_elements = ((ChildGroup.Elements) stack.getChildGroups().get(0)).children();
	}

	@Override
	protected void tearDown() throws Exception {
		_elements = null;
		_context = null;
		_count = null;

		super.tearDown();
	}

	/**
	 * Tests that the button of a command declaring a badge shows it in the declared role, and
	 * follows the channel.
	 */
	public void testTheButtonShowsTheBadgeOfItsCommand() {
		_count.set(Integer.valueOf(3));
		ReactControl button = control(0);

		assertEquals(Map.of(Badge.CONTENT__PROP, "3", Badge.ROLE__PROP, "warning"),
			state(button).get(ButtonState.BADGE__PROP));

		_count.set(Integer.valueOf(0));
		assertNull("A count dropping to zero removes the badge.", state(button).get(ButtonState.BADGE__PROP));

		_count.set(Boolean.TRUE);
		assertEquals(Map.of(Badge.DOT__PROP, Boolean.TRUE, Badge.ROLE__PROP, "warning"),
			state(button).get(ButtonState.BADGE__PROP));
	}

	/**
	 * Tests that a tab declaring a badge shows it with its accessible name, and the tab without
	 * one shows none.
	 */
	public void testATabShowsItsBadge() {
		_count.set(Integer.valueOf(150));
		ReactControl tabBar = control(1);

		List<?> tabs = (List<?>) state(tabBar).get(TabBarState.TABS__PROP);
		assertEquals(Map.of(Badge.CONTENT__PROP, "99+", Badge.LABEL__PROP, "New entries"),
			((Map<?, ?>) tabs.get(0)).get(TabBarState.Tab.BADGE__PROP));
		assertFalse("A tab without a badge sends none.", ((Map<?, ?>) tabs.get(1)).containsKey(TabBarState.Tab.BADGE__PROP));

		_count.set(null);
		tabs = (List<?>) state(tabBar).get(TabBarState.TABS__PROP);
		assertFalse("No value leaves no badge.", ((Map<?, ?>) tabs.get(0)).containsKey(TabBarState.Tab.BADGE__PROP));
	}

	/**
	 * Tests that an avatar declaring a badge shows it in the declared role, and follows the channel.
	 */
	public void testTheAvatarShowsItsBadge() {
		ReactControl avatar = control(2);
		assertNull("An empty channel shows no badge.", state(avatar).get(AvatarState.BADGE__PROP));

		_count.set("neu");
		assertEquals(Map.of(Badge.CONTENT__PROP, "neu", Badge.ROLE__PROP, "success"),
			state(avatar).get(AvatarState.BADGE__PROP));
	}

	/**
	 * Tests that an item of a bottom bar declaring a badge shows it in the declared role, and
	 * follows the channel.
	 */
	public void testABottomBarItemShowsItsBadge() {
		_count.set(Integer.valueOf(4));
		ReactControl bottomBar = control(3);

		List<?> items = (List<?>) state(bottomBar).get(BOTTOM_BAR_ITEMS);
		assertEquals(Map.of(Badge.CONTENT__PROP, "4", Badge.ROLE__PROP, "info"),
			((Map<?, ?>) items.get(0)).get(BOTTOM_BAR_BADGE));
		assertFalse("An item without a badge sends none.", ((Map<?, ?>) items.get(1)).containsKey(BOTTOM_BAR_BADGE));

		_count.set(Integer.valueOf(250));
		items = (List<?>) state(bottomBar).get(BOTTOM_BAR_ITEMS);
		assertEquals(Map.of(Badge.CONTENT__PROP, "99+", Badge.ROLE__PROP, "info"),
			((Map<?, ?>) items.get(0)).get(BOTTOM_BAR_BADGE));
	}

	/**
	 * The control of the element at the given position of the view, attached as a displayed control
	 * is.
	 */
	private ReactControl control(int index) {
		IReactControl control = _elements.get(index).createControl(_context);
		ReactControl result = (ReactControl) control;
		result.attach();
		return result;
	}

	/**
	 * The state of the given control, as the client holds it.
	 */
	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestBadgedElements.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}

}
