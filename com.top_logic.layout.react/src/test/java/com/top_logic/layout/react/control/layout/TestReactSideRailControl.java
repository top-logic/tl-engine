/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.BadgeDisplay;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactSideRailControl.Section;
import com.top_logic.layout.react.control.layout.ReactSideRailControl.Side;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.react.control.overlay.ReactDrawerControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.SideRailState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * Tests {@link ReactSideRailControl}: opening and closing as a column and as a drawer, the sections,
 * the unseen badges, and how the rail takes part in a split panel.
 */
public class TestReactSideRailControl extends TestCase {

	private static final String TASKS = "tasks";

	private static final String SOURCES = "sources";

	private static final String WIDE = "wide";

	private ReactContext _context;

	private List<String> _events;

	private int _unseen;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_events = new ArrayList<>();
		_unseen = 0;
	}

	/**
	 * A click on the icon of a section opens the rail with the section expanded; a second click on
	 * the shown section closes the rail. Both are the user's choice for the column.
	 */
	public void testStripOpensAndClosesTheRail() {
		ReactSideRailControl rail = rail(false, Set.of());

		show(rail, SOURCES);
		assertTrue(rail.isOpen());
		assertTrue(rail.isExpanded(SOURCES));
		assertEquals(Boolean.TRUE, state(rail).get(SideRailState.OPEN__PROP));
		assertEquals(List.of("expanded sources true", "column true", "open true"), _events);

		show(rail, SOURCES);
		assertFalse(rail.isOpen());
		assertTrue("Closing keeps the section expanded.", rail.isExpanded(SOURCES));
		assertEquals(List.of("expanded sources true", "column true", "open true", "column false", "open false"),
			_events);
	}

	/**
	 * In the drawer state, opening opens the drawer and leaves the user's column choice alone; leaving
	 * the drawer state gives the rail its column choice back.
	 */
	public void testDrawerKeepsTheColumnChoice() {
		ReactSideRailControl rail = rail(true, Set.of(TASKS));
		ReactDrawerControl drawer = drawer(rail);

		setDrawer(rail, true);
		assertFalse("A narrowing area does not open the drawer over the neighbour.", rail.isOpen());
		assertFalse(drawer.isOpen());

		show(rail, SOURCES);
		assertTrue(drawer.isOpen());
		assertTrue(rail.isOpen());

		// Escape or a click on the scrim.
		drawer.executeCommand(ReactDrawerControl.CMD_CLOSE, Map.of());
		assertFalse(drawer.isOpen());
		assertFalse(rail.isOpen());

		setDrawer(rail, false);
		assertTrue("The column is open as the user left it.", rail.isOpen());
		assertFalse("Nothing in the drawer state changed the column choice.", _events.contains("column false"));
		assertFalse(_events.contains("column true"));
	}

	/**
	 * A section header expands and collapses its section; the change is reported.
	 */
	public void testToggleSection() {
		ReactSideRailControl rail = rail(true, Set.of(TASKS));

		body(rail).executeCommand(ReactSideRailControl.TOGGLE_SECTION_COMMAND,
			Map.of(ReactSideRailControl.NAME_ARG, TASKS));
		assertFalse(rail.isExpanded(TASKS));
		assertEquals(List.of("expanded tasks false"), _events);
	}

	/**
	 * What a section counts beyond what was seen of it is the unseen sum; a section counts as seen
	 * while it is shown, and a hidden or collapsed section is not shown.
	 */
	public void testUnseenBadges() {
		ReactSideRailControl rail = rail(false, Set.of(TASKS));

		rail.setBadge(TASKS, count("3"));
		rail.setBadge(SOURCES, count("2"));
		assertEquals(5, _unseen);

		rail.setOpen(true);
		assertEquals("The expanded section is seen, the collapsed one not.", 2, _unseen);

		rail.setBadge(TASKS, count("4"));
		assertEquals("A count rising in a shown section is seen.", 2, _unseen);

		rail.setOpen(false);
		rail.setBadge(TASKS, count("6"));
		assertEquals(4, _unseen);

		rail.setHidden(SOURCES, true);
		rail.setExpanded(SOURCES, true);
		rail.setOpen(true);
		assertEquals("A hidden section is not shown.", 2, _unseen);

		rail.setHidden(SOURCES, false);
		assertEquals(0, _unseen);

		rail.setOpen(false);
		rail.setBadge(SOURCES, count("1"));
		assertEquals("A count going down is seen as far as it goes.", 0, _unseen);

		rail.setBadge(SOURCES, count("99+"));
		assertEquals("An overflow counts one more than the largest count with digits.", 99, _unseen);
	}

	/**
	 * In a split panel, the closed rail and the rail as drawer shrink their pane to the strip; the
	 * open rail fills its pane, without a handle of its own.
	 */
	public void testCollapsesInSplitPanel() {
		ReactSplitPanelControl split = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);
		ReactSideRailControl rail = rail(true, Set.of(TASKS));
		split.addChild(rail, ChildConstraint.pixels(300));
		split.addChild(text(), ChildConstraint.percent(100));

		assertEquals(Boolean.FALSE, state(rail).get(SideRailState.RESIZABLE__PROP));
		assertPane(split, 300, DisplayUnit.PIXEL, false);

		rail.setOpen(false);
		assertPane(split, ReactSideRailControl.STRIP_SIZE, DisplayUnit.PIXEL, true);

		rail.setOpen(true);
		assertPane(split, 300, DisplayUnit.PIXEL, false);

		setDrawer(rail, true);
		assertPane(split, ReactSideRailControl.STRIP_SIZE, DisplayUnit.PIXEL, true);
		setDrawer(rail, false);
		assertPane(split, 300, DisplayUnit.PIXEL, false);
	}

	/**
	 * The rail decides whether it is open, even against the collapse state a split panel restored.
	 */
	public void testOwnStateOverridesRestoredState() {
		ReactSplitPanelControl split = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);
		split.addChild(rail(true, Set.of(TASKS)), ChildConstraint.pixels(300), true, 300, DisplayUnit.PIXEL);
		split.addChild(text(), ChildConstraint.percent(100));
		assertPane(split, 300, DisplayUnit.PIXEL, false);

		ReactSplitPanelControl other = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);
		other.addChild(rail(false, Set.of(TASKS)), ChildConstraint.pixels(300));
		other.addChild(text(), ChildConstraint.percent(100));
		assertPane(other, ReactSideRailControl.STRIP_SIZE, DisplayUnit.PIXEL, true);
	}

	/**
	 * A shown section asking for a width requests it from the split panel while it is shown; the
	 * user's size of the pane is not changed and comes back when the section is no longer shown.
	 */
	public void testSectionWidthIsRequestedWhileShown() {
		List<Map<String, Float>> saved = new ArrayList<>();
		ReactSplitPanelControl split =
			new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true, saved::add, null);
		ReactSideRailControl rail = rail(true, Set.of(TASKS));
		ReactTextControl detail = text();
		split.addChild(rail, ChildConstraint.percent(25));
		split.addChild(detail, ChildConstraint.percent(75));

		show(rail, WIDE);
		assertEquals(480, rail.getSectionWidth());
		assertEquals(480, ((Number) state(rail).get(SideRailState.SECTION_WIDTH__PROP)).intValue());
		assertPane(split, 480, DisplayUnit.PIXEL, false);

		split.executeCommand(ReactSplitPanelControl.UPDATE_SIZES_COMMAND,
			Map.of(ReactSplitPanelControl.SIZES_ARG, Map.of(rail.getID(), 500, detail.getID(), 100)));
		assertEquals("The requested width is not the user's size.", 25f,
			saved.get(0).get(rail.getID()).floatValue(), 0.001f);

		body(rail).executeCommand(ReactSideRailControl.TOGGLE_SECTION_COMMAND,
			Map.of(ReactSideRailControl.NAME_ARG, WIDE));
		assertEquals(0, rail.getSectionWidth());
		assertPane(split, 25, DisplayUnit.PERCENT, false);
	}

	/**
	 * The drawer is named by the labels of the sections it shows, and follows them.
	 */
	public void testDrawerIsNamedByItsSections() {
		ReactSideRailControl rail = rail(true, Set.of(TASKS));
		assertEquals("Tasks", state(drawer(rail)).get(ReactDrawerControl.LABEL));

		rail.setExpanded(SOURCES, true);
		assertEquals("Tasks, Sources", state(drawer(rail)).get(ReactDrawerControl.LABEL));

		rail.setHidden(TASKS, true);
		assertEquals("Sources", state(drawer(rail)).get(ReactDrawerControl.LABEL));
	}

	/**
	 * A section without header is always expanded: it starts expanded and cannot be collapsed.
	 */
	public void testHeadlessSectionStaysExpanded() {
		ReactSideRailControl rail = new ReactSideRailControl(_context, Side.LEFT, true, 0, 280, true,
			List.of(new Section(TASKS, "Tasks", null, 0, true, text())), Set.of());
		assertTrue(rail.isExpanded(TASKS));

		body(rail).executeCommand(ReactSideRailControl.TOGGLE_SECTION_COMMAND,
			Map.of(ReactSideRailControl.NAME_ARG, TASKS));
		assertTrue("There is nothing to expand it again.", rail.isExpanded(TASKS));
	}

	/**
	 * Outside a split panel, the column has a width of its own; a drag reports it.
	 */
	public void testOwnWidthOutsideSplit() {
		ReactSideRailControl rail = rail(true, Set.of(TASKS));
		assertEquals(Boolean.TRUE, state(rail).get(SideRailState.RESIZABLE__PROP));

		rail.executeCommand(ReactSideRailControl.SET_WIDTH_COMMAND, Map.of(ReactSideRailControl.WIDTH_ARG, 340));
		assertEquals(340, ((Number) state(rail).get(SideRailState.WIDTH__PROP)).intValue());
		assertEquals(List.of("width 340"), _events);
	}

	private ReactSideRailControl rail(boolean open, Set<String> expanded) {
		List<Section> sections = List.of(
			new Section(TASKS, "Tasks", "css:fa-solid fa-list-check", 0, false, text()),
			new Section(SOURCES, "Sources", null, 0, false, text()),
			new Section(WIDE, "Wide", null, 480, false, text()));
		ReactSideRailControl rail =
			new ReactSideRailControl(_context, Side.RIGHT, true, 44, 280, open, sections, expanded);
		rail.setListener(new ReactSideRailControl.Listener() {
			@Override
			public void openChanged(boolean isOpen) {
				_events.add("open " + isOpen);
			}

			@Override
			public void columnOpenChanged(boolean isOpen) {
				_events.add("column " + isOpen);
			}

			@Override
			public void sectionExpandedChanged(String name, boolean isExpanded) {
				_events.add("expanded " + name + " " + isExpanded);
			}

			@Override
			public void widthChanged(int width) {
				_events.add("width " + width);
			}

			@Override
			public void unseenChanged(int unseen) {
				_unseen = unseen;
			}
		});
		return rail;
	}

	private ReactTextControl text() {
		return new ReactTextControl(_context, "content");
	}

	private static BadgeDisplay count(String content) {
		return new BadgeDisplay(content, false, null, null);
	}

	private static void show(ReactSideRailControl rail, String name) {
		rail.executeCommand(ReactSideRailControl.SHOW_SECTION_COMMAND, Map.of(ReactSideRailControl.NAME_ARG, name));
	}

	private static void setDrawer(ReactSideRailControl rail, boolean drawer) {
		rail.executeCommand(ReactSideRailControl.SET_DRAWER_COMMAND,
			Map.of(ReactSideRailControl.DRAWER_ARG, Boolean.valueOf(drawer)));
	}

	private static ReactDrawerControl drawer(ReactSideRailControl rail) {
		return rail.getDrawer();
	}

	private static ReactControl body(ReactSideRailControl rail) {
		return rail.getBody();
	}

	private static void assertPane(ReactSplitPanelControl split, float size, DisplayUnit unit, boolean collapsed) {
		Map<?, ?> child = (Map<?, ?>) ((List<?>) state(split).get("children")).get(0);
		assertEquals(size, ((Number) child.get("size")).floatValue(), 0.001f);
		assertEquals(unit.getExternalName(), child.get("unit"));
		assertEquals(Boolean.valueOf(collapsed), child.get("collapsed"));
	}

	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}
}
