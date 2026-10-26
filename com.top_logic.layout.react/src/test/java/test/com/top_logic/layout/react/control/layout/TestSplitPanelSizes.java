/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;

/**
 * Tests that a splitter drag of a {@link ReactSplitPanelControl} changes the sizes of its panes but
 * keeps the unit of each pane, and that the split reports the sizes in these units.
 */
public class TestSplitPanelSizes extends TestCase {

	private ReactContext _context;

	private List<Map<String, Float>> _reported;

	private ReactSplitPanelControl _split;

	private ReactTextControl _fixed;

	private ReactTextControl _flexible;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_reported = new ArrayList<>();
		_split = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true, _reported::add, null);
		_fixed = new ReactTextControl(_context, "fixed");
		_flexible = new ReactTextControl(_context, "flexible");
	}

	/** A pane sized in pixels stays in pixels, a pane sized in percent stays in percent. */
	public void testDragKeepsUnits() {
		_split.addChild(_fixed, ChildConstraint.pixels(300));
		_split.addChild(_flexible, ChildConstraint.percent(100));

		updateSizes(Map.of(_fixed.getID(), 420, _flexible.getID(), 100));

		assertChild(0, 420, DisplayUnit.PIXEL, false);
		assertChild(1, 100, DisplayUnit.PERCENT, false);

		assertEquals(1, _reported.size());
		Map<String, Float> reported = _reported.get(0);
		assertEquals(List.of(_fixed.getID(), _flexible.getID()), new ArrayList<>(reported.keySet()));
		assertEquals(420f, reported.get(_fixed.getID()).floatValue(), 0.001f);
		assertEquals(100f, reported.get(_flexible.getID()).floatValue(), 0.001f);
	}

	/**
	 * A collapsed pane is not changed by a drag; the split reports the size the collapsed pane expands
	 * to.
	 */
	public void testDragLeavesCollapsedPane() {
		ReactTextControl other = new ReactTextControl(_context, "other");
		_split.addChild(_fixed, new ChildConstraint(32, DisplayUnit.PIXEL, 0,
			Scrolling.AUTO), true, 250, DisplayUnit.PIXEL);
		_split.addChild(_flexible, ChildConstraint.percent(30));
		_split.addChild(other, ChildConstraint.percent(70));

		updateSizes(Map.of(_fixed.getID(), 999, _flexible.getID(), 45, other.getID(), 55));

		assertChild(0, 32, DisplayUnit.PIXEL, true);
		assertChild(1, 45, DisplayUnit.PERCENT, false);
		assertChild(2, 55, DisplayUnit.PERCENT, false);

		Map<String, Float> reported = _reported.get(0);
		assertEquals(List.of(_fixed.getID(), _flexible.getID(), other.getID()), new ArrayList<>(reported.keySet()));
		assertEquals("A collapsed pane reports the size it expands to.", 250f,
			reported.get(_fixed.getID()).floatValue(), 0.001f);
		assertEquals(45f, reported.get(_flexible.getID()).floatValue(), 0.001f);
		assertEquals(55f, reported.get(other.getID()).floatValue(), 0.001f);
	}

	private void updateSizes(Map<String, ?> sizes) {
		_split.executeCommand(ReactSplitPanelControl.UPDATE_SIZES_COMMAND,
			Map.of(ReactSplitPanelControl.SIZES_ARG, sizes));
	}

	private void assertChild(int index, float size, DisplayUnit unit, boolean collapsed) {
		Map<?, ?> child = child(index);
		assertEquals(size, ((Number) child.get("size")).floatValue(), 0.001f);
		assertEquals(unit.getExternalName(), child.get("unit"));
		assertEquals(Boolean.valueOf(collapsed), child.get("collapsed"));
	}

	private Map<?, ?> child(int index) {
		try {
			Map<?, ?> state = (Map<?, ?>) JSON.fromString(_split.stateAsJSON());
			return (Map<?, ?>) ((List<?>) state.get("children")).get(index);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}
}
