/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.slot;

import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.view.slot.SlotContribution;

/**
 * The contributions a slot holds belong to controls the window displays, also when a page is loaded
 * into a display the window already holds.
 *
 * <p>
 * The scenario is the one of an application: an app shell whose app bar holds a
 * {@code <slot name="appbar-content"/>}, and a sidebar of two items whose views each project
 * something into that slot. The item switch is replayed once as the user clicking the sidebar and
 * once as a page loaded on the other item's URL, the way
 * {@link com.top_logic.layout.view.ViewServlet} does it.
 * </p>
 */
public class TestSlotUrlAdoption extends AbstractSlotViewTest {

	private static final String ROOT_VIEW = "slot-root.view.xml";

	private static final String FIRST_VIEW = "slot-first.view.xml";

	private static final String SECOND_VIEW = "slot-second.view.xml";

	private static final String INNER_VIEW = "slot-overview.view.xml";

	private static final String DETAIL_VIEW = "slot-detail.view.xml";

	/** The ID of the first sidebar item, which is also the URL naming it. */
	private static final String FIRST_ITEM = "first";

	/** The ID of the second sidebar item, which is also the URL naming it. */
	private static final String SECOND_ITEM = "second";

	/** The URL suffix drilling an item's display into its detail frame. */
	private static final String DETAIL_URL = "/detail";

	@Override
	protected String[] fixtures() {
		return new String[] { ROOT_VIEW, FIRST_VIEW, SECOND_VIEW, INNER_VIEW, DETAIL_VIEW };
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createView(ROOT_VIEW);
	}

	/**
	 * Picking the other item in the sidebar leaves only that item's contribution.
	 */
	public void testClick() throws IOException {
		firstPage();
		assertEquals("The displayed item contributes to the app bar.", 1, contributions().size());

		sidebar().selectItem(SECOND_ITEM);

		assertOneDisplayedContribution("after the sidebar switched the item");
	}

	/**
	 * Loading the page on the other item's URL - a URL typed into the address bar of a tab that
	 * already displays the application, or a reload - displays that item, and nothing of the one
	 * left behind.
	 */
	public void testUrlAdoption() throws IOException {
		firstPage();
		assertEquals("The displayed item contributes to the app bar.", 1, contributions().size());

		renderPage(SECOND_ITEM);
		assertEquals("The rendered page displays only the item its URL names: " + describe(),
			1, contributions().size());

		openEventStream();

		assertOneDisplayedContribution("after the loaded page opened its event stream");
	}

	/**
	 * The same, for a display drilled one frame deep in each item - the scenario of the ticket.
	 */
	public void testUrlAdoptionOfDrilledItems() throws IOException {
		loadPage(FIRST_ITEM + DETAIL_URL);
		sidebar().selectItem(SECOND_ITEM);
		loadPage(SECOND_ITEM + DETAIL_URL);

		loadPage(FIRST_ITEM + DETAIL_URL);

		assertOneDisplayedContribution("after the drilled display was loaded on the other item's URL");
	}

	/**
	 * A deep link into a display built for it: the item the URL names is the only one contributing.
	 */
	public void testDeepLink() throws IOException {
		loadPage(SECOND_ITEM);

		assertOneDisplayedContribution("after a deep link was adopted by a fresh display");
	}

	/**
	 * Renders the page the window displays when a URL is entered into its address bar.
	 */
	private void firstPage() throws IOException {
		loadPage("");
	}

	/**
	 * Every registered contribution belongs to a control the display contains, and the app bar holds
	 * exactly one.
	 */
	private void assertOneDisplayedContribution(String step) {
		Set<ReactControl> displayed = Collections.newSetFromMap(new IdentityHashMap<>());
		collect(root(), displayed);
		for (SlotContribution contribution : contributions()) {
			assertTrue(step + ": a contribution of a control that is not displayed survives: "
				+ describe(), displayed.contains(contribution));
		}
		assertEquals(step + ": " + describe(), 1, contributions().size());
	}

	private ReactSidebarControl sidebar() {
		return findSidebar(root());
	}

	private static ReactSidebarControl findSidebar(ReactControl control) {
		if (control instanceof ReactSidebarControl sidebar) {
			return sidebar;
		}
		for (ReactControl child : control.displayedChildren()) {
			ReactSidebarControl found = findSidebar(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * The registered contributions, each named by the controls it carries, plus the number of
	 * placeholders they are routed to.
	 */
	private String describe() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("sidebar item: ");
		buffer.append(sidebar().activeRouteSegment() == null ? "-" : sidebar().activeRouteSegment().path());
		buffer.append(", contributions: ");
		for (SlotContribution contribution : contributions()) {
			buffer.append(contribution.getSlotName());
			buffer.append(contribution.getControls().stream().map(ReactControl::getID).toList());
			buffer.append(((ReactControl) contribution).isAttached() ? "(attached)" : "(detached)");
			buffer.append(' ');
		}
		buffer.append(", placeholders: ");
		buffer.append(registry().getPlaceholders().size());
		return buffer.toString();
	}

	/**
	 * Test suite with the services of {@link AbstractSlotViewTest#createSuite(Class)}.
	 */
	public static Test suite() {
		return createSuite(TestSlotUrlAdoption.class);
	}
}
