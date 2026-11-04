/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.table.FilterState;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Option;

/**
 * Mapping between the check marks of a value list ("checked = shown") and the {@link FilterState}
 * of an {@link OptionsColumnFilter}.
 *
 * <p>
 * The stored form of "some values are hidden" is {@link NegatedFilterState} over the unchecked
 * values, not an {@link OptionsFilterState} over the checked ones, for two reasons: A value that
 * appears in the data after the filter was set is not an option yet and must stay visible rather
 * than silently vanish; and "everything unchecked" is the negation of all values, which is not
 * {@link FilterState#isEmpty() empty} and therefore survives, whereas an empty include-selection
 * would be dropped as "no filter" and show everything again.
 * </p>
 */
public final class OptionChecks {

	private OptionChecks() {
		// Utility class.
	}

	/**
	 * The values that are checked (shown) under the given state.
	 *
	 * @param options
	 *        The options the value list offers.
	 * @param state
	 *        The current filter state, or {@code null} for no filter.
	 * @return The checked option values; all of them if the state does not restrict the options.
	 */
	public static Set<Object> checked(List<Option> options, FilterState state) {
		Set<Object> all = values(options);
		if (state instanceof NegatedFilterState negated && negated.inner() instanceof OptionsFilterState hidden) {
			all.removeAll(hidden.selected());
			return all;
		}
		if (state instanceof OptionsFilterState shown && !shown.selected().isEmpty()) {
			all.retainAll(shown.selected());
			return all;
		}
		return all;
	}

	/**
	 * The filter state that represents the given check marks.
	 *
	 * @param options
	 *        The options the value list offers.
	 * @param checked
	 *        The checked (shown) values.
	 * @return {@code null} (no filter) if every option is checked, or if there are no options at
	 *         all; otherwise the negation of the unchecked values.
	 */
	public static FilterState state(List<Option> options, Set<Object> checked) {
		Set<Object> unchecked = values(options);
		unchecked.removeAll(checked);
		if (unchecked.isEmpty()) {
			return null;
		}
		return new NegatedFilterState(new OptionsFilterState(unchecked));
	}

	private static Set<Object> values(List<Option> options) {
		Set<Object> result = new LinkedHashSet<>();
		for (Option option : options) {
			result.add(option.value());
		}
		return result;
	}
}
