/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.view.command.CombinedViewExecutabilityRule;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.NullInputHidden;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests for {@link ViewExecutabilityRule} implementations.
 */
public class TestViewExecutabilityRules extends TestCase {

	/**
	 * Tests that {@link NullInputDisabled} returns {@link ExecutableState#NO_EXEC_NO_MODEL} for
	 * {@code null} input.
	 */
	public void testNullInputDisabledWithNull() {
		ExecutableState state = NullInputDisabled.INSTANCE.isExecutable(null);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, state);
	}

	/**
	 * Tests that {@link NullInputDisabled} returns {@link ExecutableState#EXECUTABLE} for non-null
	 * input.
	 */
	public void testNullInputDisabledWithValue() {
		ExecutableState state = NullInputDisabled.INSTANCE.isExecutable("something");
		assertSame(ExecutableState.EXECUTABLE, state);
	}

	/**
	 * Tests that {@link NullInputHidden} returns {@link ExecutableState#NOT_EXEC_HIDDEN} for
	 * {@code null} input.
	 */
	public void testNullInputHiddenWithNull() {
		ExecutableState state = NullInputHidden.INSTANCE.isExecutable(null);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, state);
	}

	/**
	 * Tests that {@link NullInputHidden} returns {@link ExecutableState#EXECUTABLE} for non-null
	 * input.
	 */
	public void testNullInputHiddenWithValue() {
		ExecutableState state = NullInputHidden.INSTANCE.isExecutable("something");
		assertSame(ExecutableState.EXECUTABLE, state);
	}

	/**
	 * Tests that {@link CombinedViewExecutabilityRule#combine(List)} returns an executable rule for
	 * an empty list.
	 */
	public void testCombineEmpty() {
		ViewExecutabilityRule combined = CombinedViewExecutabilityRule.combine(Collections.emptyList());
		assertSame(ExecutableState.EXECUTABLE, combined.isExecutable(null));
		assertSame(ExecutableState.EXECUTABLE, combined.isExecutable("value"));
	}

	/**
	 * Tests that {@link CombinedViewExecutabilityRule#combine(List)} returns the single rule for a
	 * one-element list.
	 */
	public void testCombineSingle() {
		ViewExecutabilityRule single = NullInputDisabled.INSTANCE;
		ViewExecutabilityRule combined = CombinedViewExecutabilityRule.combine(Collections.singletonList(single));
		assertSame("Single-element combine should return the rule itself", single, combined);
	}

	/**
	 * Tests that a combined rule with all-executable rules returns
	 * {@link ExecutableState#EXECUTABLE}.
	 */
	public void testCombineAllExecutable() {
		ViewExecutabilityRule alwaysOk = input -> ExecutableState.EXECUTABLE;
		ViewExecutabilityRule combined =
			CombinedViewExecutabilityRule.combine(Arrays.asList(alwaysOk, alwaysOk));

		assertSame(ExecutableState.EXECUTABLE, combined.isExecutable("value"));
	}

	/**
	 * Tests that a combined rule short-circuits on the first non-executable result.
	 */
	public void testCombineShortCircuit() {
		int[] callCount = {0};
		ViewExecutabilityRule counter = input -> {
			callCount[0]++;
			return ExecutableState.EXECUTABLE;
		};

		List<ViewExecutabilityRule> rules = Arrays.asList(
			NullInputDisabled.INSTANCE,
			counter);

		ViewExecutabilityRule combined = CombinedViewExecutabilityRule.combine(rules);

		// With null input, NullInputDisabled should block, and counter should NOT be called.
		ExecutableState state = combined.isExecutable(null);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, state);
		assertEquals("Second rule should not be called", 0, callCount[0]);
	}

	/**
	 * Tests that a combined rule evaluates all rules when all pass.
	 */
	public void testCombineAllPass() {
		int[] callCount = {0};
		ViewExecutabilityRule counter = input -> {
			callCount[0]++;
			return ExecutableState.EXECUTABLE;
		};

		List<ViewExecutabilityRule> rules = Arrays.asList(
			NullInputDisabled.INSTANCE,
			counter);

		ViewExecutabilityRule combined = CombinedViewExecutabilityRule.combine(rules);

		// With non-null input, both rules should be evaluated.
		ExecutableState state = combined.isExecutable("value");
		assertSame(ExecutableState.EXECUTABLE, state);
		assertEquals("Second rule should be called", 1, callCount[0]);
	}

	/**
	 * Tests that the first non-executable result wins in a combined rule.
	 */
	public void testCombineFirstNonExecutableWins() {
		List<ViewExecutabilityRule> rules = Arrays.asList(
			NullInputHidden.INSTANCE,
			NullInputDisabled.INSTANCE);

		ViewExecutabilityRule combined = CombinedViewExecutabilityRule.combine(rules);

		// NullInputHidden comes first, so NOT_EXEC_HIDDEN should win.
		ExecutableState state = combined.isExecutable(null);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, state);
	}
}
