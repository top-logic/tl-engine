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
import com.top_logic.layout.react.control.sidebar.DrawerToggleControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that the hamburger button of a {@link ReactSidebarControl} reports whether the drawer it opens is open, and
 * which navigation it controls.
 *
 * <p>
 * A screen reader announces the button as expanded or collapsed; without the state the button cannot say it.
 * </p>
 */
public class TestDrawerToggleState extends TestCase {

	/** The button names the navigation of its sidebar and starts out closed. */
	public void testTheToggleStartsClosedAndNamesTheNavigation() {
		ReactContext context = context();
		ReactSidebarControl sidebar = sidebar(context);
		DrawerToggleControl toggle = new DrawerToggleControl(context, sidebar);

		assertEquals(Boolean.FALSE, state(toggle).get("open"));
		assertEquals(sidebar.getID() + "-nav", state(toggle).get("controls"));
	}

	/** Opening and closing the drawer reaches the button. */
	public void testTheToggleFollowsTheDrawer() {
		ReactContext context = context();
		ReactSidebarControl sidebar = sidebar(context);
		DrawerToggleControl toggle = new DrawerToggleControl(context, sidebar);

		sidebar.toggleDrawer();
		assertEquals("The drawer was opened, but the button still reports it closed.",
			Boolean.TRUE, state(toggle).get("open"));

		sidebar.toggleDrawer();
		assertEquals(Boolean.FALSE, state(toggle).get("open"));
	}

	private static Map<?, ?> state(DrawerToggleControl toggle) {
		try {
			return (Map<?, ?>) JSON.fromString(toggle.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	private static ReactSidebarControl sidebar(ReactContext context) {
		return new ReactSidebarControl(context, List.of(), null, false, Map.of(),
			null, null, null, null, null, null);
	}

}
