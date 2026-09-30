/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.sidebar;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.sidebar.GroupItem;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.react.control.sidebar.ToggleGroupArguments;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link ReactSidebarControl} restores the expansion state of its groups at every
 * depth.
 *
 * <p>
 * A group may hold a group of its own, and the user collapses and expands the inner ones just as
 * the outer ones; the state is kept for whoever persists it and handed back to the sidebar when it
 * is built. Applied to the top level alone, an inner group is saved but never restored: it snaps
 * back to its configured default on the next page load, and again on every refresh of the item
 * list.
 * </p>
 */
public class TestSidebarGroupState extends TestCase {

	/** The state field holding the serialized sidebar items. */
	private static final String ITEMS = "items";

	/** The state field holding a group's child items. */
	private static final String CHILDREN = "children";

	/** The state field holding a group's expansion state. */
	private static final String EXPANDED = "expanded";

	/** The state field holding an item's id. */
	private static final String ID = "id";

	private static final String OUTER = "outer";

	private static final String INNER = "inner";

	private static final String SIBLING = "sibling";

	/** A group nested in a group starts out in the state that was persisted for it. */
	public void testANestedGroupIsRestored() {
		ReactSidebarControl sidebar = sidebar(Map.of(INNER, Boolean.FALSE));

		assertEquals("The state persisted for the inner group was not applied: it is restored only "
			+ "for the groups at the top level.", Boolean.FALSE, expanded(sidebar, OUTER, INNER));
	}

	/** A group nothing was persisted for keeps the state it is configured with. */
	public void testAnUnmentionedNestedGroupKeepsItsDefault() {
		ReactSidebarControl sidebar = sidebar(Map.of(INNER, Boolean.FALSE));

		assertEquals("A group the persisted state does not name keeps its configured default.",
			Boolean.TRUE, expanded(sidebar, OUTER, SIBLING));
		assertEquals(Boolean.TRUE, expanded(sidebar, OUTER));
	}

	/**
	 * What the user chose for a nested group is what a later refresh of the item list sends.
	 */
	public void testARefreshKeepsTheNestedGroupState() {
		ReactSidebarControl sidebar = sidebar(Map.of());

		sidebar.executeCommand(ReactSidebarControl.TOGGLE_GROUP_COMMAND,
			Map.of(ToggleGroupArguments.ITEM_ID, INNER, ToggleGroupArguments.EXPANDED, Boolean.FALSE));
		sidebar.refreshItems();

		assertEquals("The inner group was collapsed by the user, but the refreshed item list "
			+ "reports it as expanded again.", Boolean.FALSE, expanded(sidebar, OUTER, INNER));
		assertEquals("Only the group the user toggled changes.",
			Boolean.TRUE, expanded(sidebar, OUTER, SIBLING));
	}

	/**
	 * The expansion state the client is told for the group reached over the given path of ids.
	 */
	private static Object expanded(ReactSidebarControl sidebar, String... path) {
		List<?> level = items(sidebar);
		Map<String, Object> group = null;
		for (String id : path) {
			group = find(level, id);
			level = (List<?>) group.get(CHILDREN);
		}
		return group.get(EXPANDED);
	}

	private static Map<String, Object> find(List<?> items, String id) {
		for (Object item : items) {
			@SuppressWarnings("unchecked")
			Map<String, Object> state = (Map<String, Object>) item;
			if (id.equals(state.get(ID))) {
				return state;
			}
		}
		throw new AssertionError("The sidebar does not report an item '" + id + "' to the client.");
	}

	private static List<?> items(ReactSidebarControl sidebar) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> state = (Map<String, Object>) JSON.fromString(sidebar.stateAsJSON());
			return (List<?>) state.get(ITEMS);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * A sidebar of a group holding two groups, all of them configured expanded.
	 *
	 * @param initialGroupStates
	 *        The expansion states persisted for an earlier visit.
	 */
	private static ReactSidebarControl sidebar(Map<String, Boolean> initialGroupStates) {
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		GroupItem inner = new GroupItem(INNER, "Inner", null, List.of(), true);
		GroupItem sibling = new GroupItem(SIBLING, "Sibling", null, List.of(), true);
		List<SidebarItem> items =
			List.of(new GroupItem(OUTER, "Outer", null, List.of(inner, sibling), true));
		return new ReactSidebarControl(context, items, null, false, initialGroupStates,
			null, null, null, null, null, null);
	}

}
