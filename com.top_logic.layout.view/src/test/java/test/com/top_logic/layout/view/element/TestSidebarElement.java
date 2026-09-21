/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.sidebar.CommandItem;
import com.top_logic.layout.react.control.sidebar.GroupItem;
import com.top_logic.layout.react.control.sidebar.HeaderItem;
import com.top_logic.layout.react.control.sidebar.NavigationItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SeparatorItem;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.SidebarElement;
import com.top_logic.layout.view.navigation.MountPath;
import com.top_logic.layout.view.navigation.MountStep;
import com.top_logic.layout.view.navigation.ViewMounts;
import com.top_logic.layout.view.security.SecurityScopeService;

/**
 * Tests the items a {@code <sidebar>} offers: the captions, groups, navigation items and command
 * items it is written with, and what each of them reports to the client.
 *
 * <p>
 * A group gathers items without becoming a place of its own: what a navigation item inside it leads
 * to is displayed where the sidebar displays everything, so the way there names the sidebar and the
 * item, and nothing in between. What an item shows, on the other hand, follows the application
 * while the sidebar stands: a badge follows the channel it displays, and a command item follows the
 * executability of the command it hosts. Above and below the items the rail carries content of its
 * own - a header and a footer, each with a shape of its own for the folded rail - which is shown for
 * as long as the sidebar stands.
 * </p>
 */
public class TestSidebarElement extends BasicTestCase {

	/** The view the sidebar under test is written in. */
	private static final String FIXTURE = "sidebar-items.view.xml";

	/** The view the navigation item inside the group leads to. */
	private static final String CONTENT_VIEW = "sidebar-item-content.view.xml";

	/** A view whose group is guarded by a scope the session has no role on. */
	private static final String DENIED_FIXTURE = "sidebar-denied-group.view.xml";

	/** A view whose sidebar is written with content above and below its items. */
	private static final String CHROME_FIXTURE = "sidebar-chrome.view.xml";

	/** Name of the channel the badge and the command items read. */
	private static final String COUNT_CHANNEL = "count";

	/** The state field holding the serialized sidebar items. */
	private static final String ITEMS = "items";

	/** The state field holding the content shown above the items. */
	private static final String HEADER_CONTENT = "headerContent";

	/** The state field holding the content shown above the items while the rail is folded. */
	private static final String HEADER_COLLAPSED_CONTENT = "headerCollapsedContent";

	/** The state field holding the content shown below the items. */
	private static final String FOOTER_CONTENT = "footerContent";

	/** The state field holding the content shown below the items while the rail is folded. */
	private static final String FOOTER_COLLAPSED_CONTENT = "footerCollapsedContent";

	/** The wire name of an item's id. */
	private static final String ID = "id";

	/** The wire name of an item's kind. */
	private static final String TYPE = "type";

	/** The wire name of the badge of a navigation item. */
	private static final String BADGE = "badge";

	/** The wire name of an item withheld from the sidebar. */
	private static final String HIDDEN = "hidden";

	/** The wire name of an item shown out of reach. */
	private static final String DISABLED = "disabled";

	/** The wire name of the unfolded state of a group. */
	private static final String EXPANDED = "expanded";

	/** The wire name of the items a group gathers. */
	private static final String CHILDREN = "children";

	private ViewChannel _count;

	private ViewContext _context;

	private FixtureViews _views;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_views = new FixtureViews(TestSidebarElement.class);
		_count = new DefaultViewChannel(COUNT_CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(COUNT_CHANNEL, _count);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_count = null;
		_views = null;

		super.tearDown();
	}

	/**
	 * Tests that the way to a view a navigation item inside a group leads to names the sidebar and
	 * the item alone: a group gathers items, it does not display anything itself.
	 */
	public void testAGroupAddsNoStepOnTheWay() {
		ViewMounts mounts = ViewMounts.scan(FIXTURE, new FixtureViews(TestSidebarElement.class));

		List<MountPath> paths = mounts.getMounts(CONTENT_VIEW);
		assertEquals("The view is reached through the one item that references it.", 1, paths.size());

		List<MountStep> steps = paths.get(0).steps();
		assertEquals("Only the sidebar chooses what it displays: " + steps, 1, steps.size());
		assertEquals("The item is addressed at the sidebar, not at the group it stands in.",
			SidebarElement.class, steps.get(0).container().getClass());
		assertEquals("inner", steps.get(0).key());
	}

	/**
	 * Tests that every kind of item the sidebar is written with reaches the client, in the order it
	 * is written in and with what distinguishes it.
	 */
	public void testTheItemsReachTheClient() {
		List<Map<String, Object>> items = items(sidebar(FIXTURE));

		assertEquals("Caption, group, navigation item, separator and two command items: " + items,
			6, items.size());
		assertItem(items.get(0), HeaderItem.TYPE_HEADER, "section");

		Map<String, Object> group = items.get(1);
		assertItem(group, GroupItem.TYPE_GROUP, "g");
		assertEquals("The group is written folded away.", Boolean.FALSE, group.get(EXPANDED));

		List<Map<String, Object>> children = children(group);
		assertEquals("The group gathers the two items written inside it: " + children, 2, children.size());
		assertItem(children.get(0), NavigationItem.TYPE_NAV, "inner");
		assertNull("A channel holding nothing is worth no badge.", children.get(0).get(BADGE));
		assertItem(children.get(1), HeaderItem.TYPE_HEADER, "innerSection");

		Map<String, Object> top = items.get(2);
		assertItem(top, NavigationItem.TYPE_NAV, "top");
		assertEquals("The item is written as one the sidebar does not display.",
			Boolean.TRUE, top.get(HIDDEN));

		assertEquals(SeparatorItem.TYPE_SEPARATOR, items.get(3).get(TYPE));
		assertItem(items.get(4), CommandItem.TYPE_COMMAND, "cmdHidden");
		assertItem(items.get(5), CommandItem.TYPE_COMMAND, "cmdDisabled");
	}

	/**
	 * Tests that the badge of a navigation item says what its channel holds, and disappears when the
	 * channel holds nothing again.
	 */
	public void testTheBadgeFollowsItsChannel() {
		ReactSidebarControl sidebar = sidebar(FIXTURE);

		_count.set(Integer.valueOf(3));

		assertEquals("The badge names what the channel holds.", "3", item(sidebar, "inner").get(BADGE));

		_count.set(null);

		assertNull("Nothing to announce leaves no badge behind.", item(sidebar, "inner").get(BADGE));
	}

	/**
	 * Tests that a command item follows the executability of the command it hosts: it is withheld
	 * or shown out of reach while the rules reject the input, and takes its place back when they
	 * accept it.
	 */
	public void testTheCommandItemsFollowTheirExecutability() {
		ReactSidebarControl sidebar = sidebar(FIXTURE);

		assertEquals("A command whose rules hide it is not offered.",
			Boolean.TRUE, item(sidebar, "cmdHidden").get(HIDDEN));
		assertEquals("A command whose rules disable it is shown out of reach.",
			Boolean.TRUE, item(sidebar, "cmdDisabled").get(DISABLED));

		_count.set(Integer.valueOf(3));

		assertNull("The input the rules accept brings the item back.", item(sidebar, "cmdHidden").get(HIDDEN));
		assertNull("The input the rules accept puts the item within reach.",
			item(sidebar, "cmdDisabled").get(DISABLED));

		_count.set(null);

		assertEquals("The item is withheld again once its input is gone.",
			Boolean.TRUE, item(sidebar, "cmdHidden").get(HIDDEN));
		assertEquals("The item is out of reach again once its input is gone.",
			Boolean.TRUE, item(sidebar, "cmdDisabled").get(DISABLED));
	}

	/**
	 * Tests that a group the session may not see is left out with everything it gathers, rather
	 * than shown empty.
	 */
	public void testAGuardedGroupIsOmitted() {
		List<Map<String, Object>> items = items(sidebar(DENIED_FIXTURE));

		assertEquals("Only the unguarded item is offered: " + items, 1, items.size());
		assertEquals("open", items.get(0).get(ID));
	}

	/**
	 * Tests that the content written above and below the items reaches the client as part of the
	 * sidebar, in both the unfolded and the folded shape of the rail.
	 */
	public void testTheChromeReachesTheClient() {
		Map<?, ?> state = state(sidebar(CHROME_FIXTURE));

		assertNotNull("The rail shows what is written above its items.", state.get(HEADER_CONTENT));
		assertNotNull("The folded rail shows what is written for it above the items.",
			state.get(HEADER_COLLAPSED_CONTENT));
		assertNotNull("The rail shows what is written below its items.", state.get(FOOTER_CONTENT));
		assertNotNull("The folded rail shows what is written for it below the items.",
			state.get(FOOTER_COLLAPSED_CONTENT));
	}

	/**
	 * Tests that a sidebar written with items alone offers no chrome around them.
	 */
	public void testASidebarWithoutChromeShowsNone() {
		Map<?, ?> state = state(sidebar(FIXTURE));

		assertFalse("Nothing is written above the items: " + state, state.containsKey(HEADER_CONTENT));
		assertFalse("Nothing is written above the items of the folded rail: " + state,
			state.containsKey(HEADER_COLLAPSED_CONTENT));
		assertFalse("Nothing is written below the items: " + state, state.containsKey(FOOTER_CONTENT));
		assertFalse("Nothing is written below the items of the folded rail: " + state,
			state.containsKey(FOOTER_COLLAPSED_CONTENT));
	}

	/**
	 * Tests that a view written into the rail's footer is displayed as soon as the sidebar is: the
	 * way there names no item, because the sidebar does not choose between its chrome and anything
	 * else.
	 */
	public void testTheChromeAddsNoStepOnTheWay() {
		ViewMounts mounts = ViewMounts.scan(CHROME_FIXTURE, new FixtureViews(TestSidebarElement.class));

		List<MountPath> paths = mounts.getMounts(CONTENT_VIEW);
		assertEquals("The view is reached through the footer that references it.", 1, paths.size());
		assertEquals("The footer is shown whenever the sidebar is: " + paths.get(0).steps(),
			List.of(), paths.get(0).steps());
	}

	/**
	 * The attached sidebar control of the given view, as a session displays it.
	 */
	private ReactSidebarControl sidebar(String viewRef) {
		SidebarElement element = sidebarElement(view(viewRef));
		ReactSidebarControl result = (ReactSidebarControl) element.createControl(_context);
		result.attach();
		return result;
	}

	private ViewElement view(String viewRef) {
		try {
			return _views.getView(viewRef);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a readable view: " + viewRef, ex);
		}
	}

	/**
	 * The sidebar written as the content of the given view.
	 */
	private static SidebarElement sidebarElement(ViewElement view) {
		ChildGroup group = view.getChildGroups().get(0);
		UIElement content = ((ChildGroup.Elements) group).children().get(0);
		return (SidebarElement) content;
	}

	private static void assertItem(Map<String, Object> item, String type, String id) {
		assertEquals("Kind of " + item, type, item.get(TYPE));
		assertEquals("Id of " + item, id, item.get(ID));
	}

	/**
	 * The state of the item with the given id, searched in the groups as well.
	 */
	private static Map<String, Object> item(ReactSidebarControl sidebar, String id) {
		Map<String, Object> found = find(items(sidebar), id);
		if (found == null) {
			throw new AssertionError("The sidebar does not report an item '" + id + "' to the client.");
		}
		return found;
	}

	private static Map<String, Object> find(List<Map<String, Object>> items, String id) {
		for (Map<String, Object> item : items) {
			if (id.equals(item.get(ID))) {
				return item;
			}
			if (GroupItem.TYPE_GROUP.equals(item.get(TYPE))) {
				Map<String, Object> found = find(children(item), id);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private static List<Map<String, Object>> children(Map<String, Object> group) {
		return maps((List<?>) group.get(CHILDREN));
	}

	/**
	 * The items of the given sidebar, as the client holds them.
	 */
	private static List<Map<String, Object>> items(ReactSidebarControl sidebar) {
		return maps((List<?>) state(sidebar).get(ITEMS));
	}

	/**
	 * The state of the given sidebar, as the client holds it.
	 */
	private static Map<?, ?> state(ReactSidebarControl sidebar) {
		try {
			return (Map<?, ?>) JSON.fromString(sidebar.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static List<Map<String, Object>> maps(List<?> values) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object value : values) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) value;
			result.add(map);
		}
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The guarded group asks the {@link SecurityScopeService} whether the session may see
	 *           it, and that service materializes its scopes in the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestSidebarElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SecurityScopeService.Module.INSTANCE));
	}

}
