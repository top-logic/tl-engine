/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactColumnsControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the state a {@link ReactColumnsControl} hands its React component: the weights, the
 * breakpoint and the gap the client needs to lay the columns out, that none of them reaches the
 * headless projection an agent reads, and that a weight list not matching the columns is rejected.
 */
public class TestReactColumnsControl extends TestCase {

	/**
	 * A columns layout that hands out its own state, which is otherwise visible to the control only.
	 */
	private static final class ColumnsProbe extends ReactColumnsControl {

		ColumnsProbe(ReactContext context, String breakpoint, StackGap gap, List<Integer> weights,
				List<? extends ReactControl> children) {
			super(context, breakpoint, gap, weights, children);
		}

		Object state(String key) {
			return getState(key);
		}
	}

	private static ReactContext createContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	private static ReactControl createColumn(ReactContext context) {
		return new ReactStackControl(context, List.of());
	}

	private ColumnsProbe createColumns(List<Integer> weights) {
		ReactContext context = createContext();
		return new ColumnsProbe(context, "48rem", StackGap.DEFAULT, weights,
			weights.stream().map(weight -> createColumn(context)).toList());
	}

	/**
	 * The weights reach the client in the order of the columns.
	 */
	public void testWeightsAreSent() {
		ColumnsProbe columns = createColumns(List.of(Integer.valueOf(2), Integer.valueOf(1)));

		assertEquals(List.of(Integer.valueOf(2), Integer.valueOf(1)), columns.state("weights"));
	}

	/**
	 * The breakpoint the columns stack below and the gap between them reach the client.
	 */
	public void testBreakpointAndGapAreSent() {
		ColumnsProbe columns = createColumns(List.of(Integer.valueOf(2), Integer.valueOf(1)));

		assertEquals("48rem", columns.state("breakpoint"));
		assertEquals(StackGap.DEFAULT.getExternalName(), columns.state("gap"));
	}

	/**
	 * Every column needs a weight of its own: a list that does not match the columns is rejected.
	 */
	public void testWeightCountMustMatchColumns() {
		ReactContext context = createContext();
		List<ReactControl> children = List.of(createColumn(context), createColumn(context));

		try {
			new ColumnsProbe(context, "48rem", StackGap.DEFAULT, List.of(Integer.valueOf(2)), children);
			fail("A weight list shorter than the columns must be rejected.");
		} catch (IllegalArgumentException ex) {
			/* Expected. */
		}
	}

	/**
	 * A column takes a share of the width, so its weight is positive.
	 */
	public void testWeightMustBePositive() {
		ReactContext context = createContext();
		List<ReactControl> children = List.of(createColumn(context));

		try {
			new ColumnsProbe(context, "48rem", StackGap.DEFAULT, List.of(Integer.valueOf(0)), children);
			fail("A weight of zero must be rejected.");
		} catch (IllegalArgumentException ex) {
			/* Expected. */
		}
	}

	/**
	 * A negative weight is rejected just as a weight of zero is.
	 */
	public void testNegativeWeightIsRejected() {
		ReactContext context = createContext();
		List<ReactControl> children = List.of(createColumn(context));

		try {
			new ColumnsProbe(context, "48rem", StackGap.DEFAULT, Arrays.asList(Integer.valueOf(-1)), children);
			fail("A negative weight must be rejected.");
		} catch (IllegalArgumentException ex) {
			/* Expected. */
		}
	}

	/**
	 * The weights, the breakpoint, the gap and the item class are rendering-only: they describe how
	 * the columns look, not what they display, so an agent reading the headless projection does not
	 * see them.
	 */
	public void testLayoutStateIsNotProjected() {
		ColumnsProbe columns = createColumns(List.of(Integer.valueOf(2), Integer.valueOf(1)));
		columns.setItemClass("demoColumn");

		Map<String, Object> projection = columns.scriptingScalarState();

		assertFalse("The weights are rendering-only.", projection.containsKey("weights"));
		assertFalse("The breakpoint is rendering-only.", projection.containsKey("breakpoint"));
		assertFalse("The gap is rendering-only.", projection.containsKey("gap"));
		assertFalse("The item class is rendering-only.", projection.containsKey("itemClass"));
	}

	/**
	 * A columns layout places its children and shows nothing of its own, so it is elided from the
	 * headless projection.
	 */
	public void testColumnsAreScriptingTransparent() {
		ColumnsProbe columns = createColumns(List.of(Integer.valueOf(1)));

		assertTrue("A columns layout is a pure layout container.", columns.scriptingTransparent());
	}

	/**
	 * Test suite. The {@link TypeIndex} module keeps the setup consistent with the other tests; the
	 * exercised code paths themselves need no application services.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactColumnsControl.class, TypeIndex.Module.INSTANCE);
	}
}
