/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;

/**
 * A {@link ReactLayoutControl} that places its children side by side in columns of configurable
 * width, via the {@code TLColumns} React component.
 *
 * <p>
 * Each child occupies one column and receives a share of the width proportional to its
 * {@link #WEIGHTS weight}: a page of a main column of weight 2 next to a side column of weight 1
 * splits the width two thirds to one third. Below the configured {@link #BREAKPOINT breakpoint} the
 * columns stack, each one taking the full width.
 * </p>
 */
public class ReactColumnsControl extends ReactLayoutControl {

	private static final String REACT_MODULE = "TLColumns";

	/** @see #ReactColumnsControl(ReactContext, String, StackGap, List, List) */
	private static final String BREAKPOINT = "breakpoint";

	/** @see #ReactColumnsControl(ReactContext, String, StackGap, List, List) */
	private static final String GAP = "gap";

	/** @see #ReactColumnsControl(ReactContext, String, StackGap, List, List) */
	private static final String WEIGHTS = "weights";

	/**
	 * Creates a {@link ReactColumnsControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param breakpoint
	 *        The width below which the columns stack, as a CSS length (e.g. "48rem"). Measured
	 *        against the width of the container the columns are placed in, not the width of the
	 *        viewport.
	 * @param gap
	 *        The gap between the columns.
	 * @param weights
	 *        The width share of each column, in the order of the children: one positive weight per
	 *        child.
	 * @param children
	 *        The content of the columns, one child per column.
	 * @throws IllegalArgumentException
	 *         If the number of weights differs from the number of children, or a weight is not
	 *         positive.
	 */
	public ReactColumnsControl(ReactContext context, String breakpoint, StackGap gap, List<Integer> weights,
			List<? extends ReactControl> children) {
		super(context, REACT_MODULE, checked(weights, children));

		putState(BREAKPOINT, breakpoint);
		putState(GAP, gap.getExternalName());
		putState(WEIGHTS, new ArrayList<>(weights));
	}

	/**
	 * Checks the weights against the columns they describe, before anything of the control is set
	 * up.
	 *
	 * @return The given children, so that the check happens in the argument list of the super
	 *         constructor.
	 */
	private static List<? extends ReactControl> checked(List<Integer> weights,
			List<? extends ReactControl> children) {
		if (weights.size() != children.size()) {
			throw new IllegalArgumentException("Each column needs a weight of its own, got " + weights.size()
				+ " weights for " + children.size() + " columns.");
		}
		for (Integer weight : weights) {
			if (weight == null || weight.intValue() < 1) {
				throw new IllegalArgumentException("A column weight is a positive number, got: " + weight);
			}
		}
		return children;
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return Set.of(BREAKPOINT, GAP, WEIGHTS, ITEM_CLASS);
	}

	/**
	 * Structural: this control arranges its children and is elided from the headless projection.
	 */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}
}
