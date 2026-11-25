/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * Tests that a {@link ReactSplitPanelControl} shrinks the area of a {@link CollapsibleChild} that
 * collapses, and collapses itself in its own split panel when all its children are collapsed.
 */
public class TestSplitPanelCollapse extends TestCase {

	private static final String TOGGLE_MINIMIZE = "toggleMinimize";

	private ReactContext _context;

	private List<String> _reported;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_reported = new ArrayList<>();
	}

	/**
	 * A child that only implements {@link CollapsibleChild} gets the size it reports when collapsing,
	 * and its former size back when expanding.
	 */
	public void testCollapsibleChildGetsReportedSize() {
		ReactSplitPanelControl split = split();
		Collapsible rail = new Collapsible(_context);
		split.addChild(rail, ChildConstraint.pixels(280));
		split.addChild(text(), ChildConstraint.percent(100));

		assertTrue(rail._container.canCollapse());
		rail._container.collapsed(48);
		assertChild(split, 0, 48, DisplayUnit.PIXEL, true);
		assertEquals(List.of("0:true"), _reported);

		rail._container.expanded();
		assertChild(split, 0, 280, DisplayUnit.PIXEL, false);
		assertEquals(List.of("0:true", "0:false"), _reported);
	}

	/**
	 * Reporting the current state again changes nothing: a repeated collapse only updates the size,
	 * the size to expand to stays; expanding an open child does nothing.
	 */
	public void testRepeatedReportsKeepTheExpandedSize() {
		ReactSplitPanelControl split = split();
		Collapsible rail = new Collapsible(_context);
		split.addChild(rail, ChildConstraint.percent(30));
		split.addChild(text(), ChildConstraint.percent(70));

		rail._container.expanded();
		assertChild(split, 0, 30, DisplayUnit.PERCENT, false);
		assertEquals(List.of(), _reported);

		rail._container.collapsed(48);
		rail._container.collapsed(56);
		assertChild(split, 0, 56, DisplayUnit.PIXEL, true);
		assertEquals(List.of("0:true"), _reported);

		rail._container.expanded();
		assertChild(split, 0, 30, DisplayUnit.PERCENT, false);
	}

	/**
	 * A child of a removed pane no longer changes the split panel.
	 */
	public void testRemovedChildIsDisconnected() {
		ReactSplitPanelControl split = split();
		Collapsible rail = new Collapsible(_context);
		split.addChild(rail, ChildConstraint.pixels(280));
		split.addChild(text(), ChildConstraint.percent(100));

		split.removeChild(0);
		assertNull(rail._container);
	}

	/**
	 * The last open child of a root split panel cannot collapse; the split panel stays as it is.
	 */
	public void testLastOpenChildOfRootCannotCollapse() {
		ReactSplitPanelControl split = split();
		Collapsible a = new Collapsible(_context);
		Collapsible b = new Collapsible(_context);
		split.addChild(a, ChildConstraint.percent(50));
		split.addChild(b, ChildConstraint.percent(50));

		a._container.collapsed(48);
		assertFalse("Nothing would remain open.", b._container.canCollapse());
	}

	/**
	 * When all children of a nested split panel are collapsed, its area in the outer split panel
	 * shrinks to the sum of the collapsed sizes; expanding one child gives the area its size back.
	 */
	public void testAllCollapsedCascadesToOuterSplit() {
		ReactSplitPanelControl outer = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);
		ReactSplitPanelControl inner = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);
		Collapsible a = new Collapsible(_context);
		Collapsible b = new Collapsible(_context);
		inner.addChild(a, ChildConstraint.percent(50));
		inner.addChild(b, ChildConstraint.percent(50));
		outer.addChild(inner, ChildConstraint.pixels(400));
		outer.addChild(text(), ChildConstraint.percent(100));

		a._container.collapsed(48);
		assertChild(outer, 0, 400, DisplayUnit.PIXEL, false);

		assertTrue("The inner split can collapse in the outer one.", b._container.canCollapse());
		b._container.collapsed(40);
		assertChild(outer, 0, 88, DisplayUnit.PIXEL, true);

		a._container.expanded();
		assertChild(outer, 0, 400, DisplayUnit.PIXEL, false);
	}

	/**
	 * A panel collapses to its header through the minimize command and reports it to the split panel,
	 * unless it is the last open pane of a root split panel.
	 */
	public void testPanelMinimizeCollapsesItsArea() {
		ReactSplitPanelControl split = split();
		ReactPanelControl first = panel();
		ReactPanelControl second = panel();
		split.addChild(first, ChildConstraint.percent(40));
		split.addChild(second, ChildConstraint.percent(60));

		first.executeCommand(TOGGLE_MINIMIZE, Map.of());
		assertChild(split, 0, 36, DisplayUnit.PIXEL, true);

		second.executeCommand(TOGGLE_MINIMIZE, Map.of());
		assertChild(split, 1, 60, DisplayUnit.PERCENT, false);
		assertEquals("The last open panel stays open.", "NORMALIZED", state(second).get("expansionState"));

		first.executeCommand(TOGGLE_MINIMIZE, Map.of());
		assertChild(split, 0, 40, DisplayUnit.PERCENT, false);
		assertEquals(List.of("0:true", "0:false"), _reported);
	}

	/**
	 * A panel in a pane the split panel restores as collapsed starts minimized to its header; expanding
	 * it gives the pane the size it is to expand to.
	 */
	public void testRestoredCollapsedPaneMinimizesPanel() {
		ReactSplitPanelControl split = split();
		ReactPanelControl panel = panel();
		split.addChild(panel, ChildConstraint.percent(25), true, 25, DisplayUnit.PERCENT);
		split.addChild(text(), ChildConstraint.percent(75));

		assertEquals("MINIMIZED", state(panel).get("expansionState"));
		assertChild(split, 0, 36, DisplayUnit.PIXEL, true);
		assertEquals("Taking over the restored state is no change.", List.of(), _reported);

		panel.executeCommand(TOGGLE_MINIMIZE, Map.of());
		assertEquals("NORMALIZED", state(panel).get("expansionState"));
		assertChild(split, 0, 25, DisplayUnit.PERCENT, false);
		assertEquals(List.of("0:false"), _reported);
	}

	/**
	 * A child that keeps a collapse state of its own reports it when it gets its container; the split
	 * panel follows the child, even against the state it restored.
	 */
	public void testChildWithOwnStateOverridesRestoredState() {
		ReactSplitPanelControl split = split();
		Collapsible open = new Collapsible(_context) {
			@Override
			public void setCollapseContainer(CollapseContainer container) {
				super.setCollapseContainer(container);
				container.expanded();
			}
		};
		split.addChild(open, ChildConstraint.pixels(280), true, 280, DisplayUnit.PIXEL);
		split.addChild(text(), ChildConstraint.percent(100));

		assertChild(split, 0, 280, DisplayUnit.PIXEL, false);
		assertEquals(List.of("0:false"), _reported);
	}

	/**
	 * A nested split panel restored as collapsed whose children are open expands in the outer split
	 * panel; one whose children are all collapsed stays collapsed with the sum of their sizes.
	 */
	public void testNestedSplitReportsItsOwnState() {
		ReactSplitPanelControl outer = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);

		ReactSplitPanelControl open = new ReactSplitPanelControl(_context, Orientation.VERTICAL, true);
		open.addChild(text(), ChildConstraint.percent(100));
		outer.addChild(open, ChildConstraint.pixels(300), true, 300, DisplayUnit.PIXEL);

		ReactSplitPanelControl closed = new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true);
		closed.addChild(panel(), ChildConstraint.percent(50), true, 50, DisplayUnit.PERCENT);
		closed.addChild(panel(), ChildConstraint.percent(50), true, 50, DisplayUnit.PERCENT);
		outer.addChild(closed, ChildConstraint.pixels(200));

		assertChild(outer, 0, 300, DisplayUnit.PIXEL, false);
		assertChild(outer, 1, 72, DisplayUnit.PIXEL, true);
	}

	private ReactSplitPanelControl split() {
		return new ReactSplitPanelControl(_context, Orientation.HORIZONTAL, true, null,
			(index, collapsed) -> _reported.add(index + ":" + collapsed));
	}

	private ReactTextControl text() {
		return new ReactTextControl(_context, "content");
	}

	private ReactPanelControl panel() {
		return new ReactPanelControl(_context, "title", text(), null, null, true, false, false);
	}

	private static void assertChild(ReactSplitPanelControl split, int index, float size, DisplayUnit unit,
			boolean collapsed) {
		Map<?, ?> child = (Map<?, ?>) ((List<?>) state(split).get("children")).get(index);
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

	/**
	 * A control that knows of collapsing only through {@link CollapsibleChild}.
	 */
	private static class Collapsible extends ReactControl implements CollapsibleChild {

		CollapseContainer _container;

		Collapsible(ReactContext context) {
			super(context, null, "TLTest");
		}

		@Override
		public void setCollapseContainer(CollapseContainer container) {
			_container = container;
		}
	}
}
