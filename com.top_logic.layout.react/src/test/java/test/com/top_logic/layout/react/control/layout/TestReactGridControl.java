/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackJustify;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the state a {@link ReactGridControl} hands its React component: the column bound, the width
 * bound and the item class the client needs to lay the grid out, and that none of them reaches the
 * headless projection an agent reads.
 */
public class TestReactGridControl extends TestCase {

	/**
	 * A grid that hands out its own state, which is otherwise visible to the control only.
	 */
	private static final class GridProbe extends ReactGridControl {

		GridProbe(ReactContext context, String minColumnWidth, Integer maxColumns, StackGap gap) {
			super(context, minColumnWidth, maxColumns, gap, List.of());
		}

		Object state(String key) {
			return getState(key);
		}
	}

	private GridProbe createGrid(Integer maxColumns) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return new GridProbe(context, "24rem", maxColumns, StackGap.DEFAULT);
	}

	/**
	 * An unbounded grid leaves the column bound unset, so that the client keeps placing as many
	 * columns as fit.
	 */
	public void testUnboundedGridHasNoColumnBound() {
		GridProbe grid = createGrid(null);

		assertEquals("24rem", grid.state("minColumnWidth"));
		assertNull("An unbounded grid must not send a column bound.", grid.state("maxColumns"));
	}

	/**
	 * The configured column bound reaches the client.
	 */
	public void testColumnBoundIsSent() {
		GridProbe grid = createGrid(Integer.valueOf(2));

		assertEquals(Integer.valueOf(2), grid.state("maxColumns"));
	}

	/**
	 * The item class reaches the client, and is unset until one is given.
	 */
	public void testItemClassIsSent() {
		GridProbe grid = createGrid(null);

		assertNull("A grid wraps its children only once an item class is set.", grid.state("itemClass"));

		grid.setItemClass("demoCard");
		assertEquals("demoCard", grid.state("itemClass"));
	}

	/**
	 * The width bound reaches the client, and is unset until one is given.
	 */
	public void testMaxWidthIsSent() {
		GridProbe grid = createGrid(null);

		assertNull("A grid spans its container until it is bounded.", grid.state("maxWidth"));

		grid.setMaxWidth("60rem");
		assertEquals("60rem", grid.state("maxWidth"));
	}

	/**
	 * The column bound and the item class are rendering-only: they describe how the grid looks, not
	 * what it displays, so an agent reading the headless projection does not see them.
	 */
	public void testLayoutStateIsNotProjected() {
		GridProbe grid = createGrid(Integer.valueOf(2));
		grid.setItemClass("demoCard");
		grid.setMaxWidth("60rem");

		Map<String, Object> projection = grid.scriptingScalarState();

		assertFalse("The column bound is rendering-only.", projection.containsKey("maxColumns"));
		assertFalse("The width bound is rendering-only.", projection.containsKey("maxWidth"));
		assertFalse("The item class is rendering-only.", projection.containsKey("itemClass"));
		assertFalse("The minimum column width is rendering-only.", projection.containsKey("minColumnWidth"));
		assertFalse("The gap is rendering-only.", projection.containsKey("gap"));
	}

	/**
	 * A stack wraps its children only once an item class is set, and the class is rendering-only.
	 */
	public void testStackItemClass() {
		ReactStackControl stack = createStack();

		stack.setItemClass("demoCard");

		assertFalse("The item class is rendering-only.", stack.scriptingScalarState().containsKey("itemClass"));
	}

	/**
	 * How a stack arranges its children says nothing about what they display, so none of the
	 * arrangement reaches the headless projection - not even where the stack fills a named slot of
	 * its container and is therefore kept rather than elided.
	 */
	public void testStackLayoutStateIsNotProjected() {
		ReactStackControl stack = createStack();

		stack.setJustify(StackJustify.SPACE_BETWEEN);
		stack.setWrap(true);
		stack.setMaxWidth("60rem");
		stack.setGrowFirst(true);

		Map<String, Object> projection = stack.scriptingScalarState();

		for (String key : List.of("direction", "gap", "align", "justify", "wrap", "maxWidth", "growFirst")) {
			assertFalse("The arrangement is rendering-only, but '" + key + "' is projected.",
				projection.containsKey(key));
		}
	}

	private ReactStackControl createStack() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return new ReactStackControl(context, List.of());
	}

	/**
	 * Test suite. The {@link TypeIndex} module keeps the setup consistent with the other tests; the
	 * exercised code paths themselves need no application services.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactGridControl.class, TypeIndex.Module.INSTANCE);
	}
}
