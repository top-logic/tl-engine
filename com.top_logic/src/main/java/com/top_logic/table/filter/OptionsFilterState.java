/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.table.FilterState;

/**
 * {@link FilterState} of an {@link OptionsColumnFilter}: the set of selected option values.
 *
 * @param selected
 *        The selected option values; a row matches if its value is in this set.
 */
public record OptionsFilterState(Set<Object> selected) implements FilterState {

	/**
	 * Creates an {@link OptionsFilterState} with a defensive, immutable copy.
	 */
	public OptionsFilterState {
		selected = Set.copyOf(selected);
	}

	@Override
	public boolean isEmpty() {
		return selected.isEmpty();
	}

	/**
	 * The {@link OptionsFilterState} selecting the given declared value among the given option
	 * values, or {@code null} if the value names no option.
	 *
	 * <p>
	 * A value that is itself one of the options selects exactly that option; otherwise a
	 * collection of values selects each of its elements. Options are identified by
	 * {@link Object#equals(Object) value equality}, the same identity
	 * {@link OptionsColumnFilter#predicate(com.top_logic.table.FilterState)} matches cells by, so a
	 * business object equal to an option is accepted as that option.
	 * </p>
	 *
	 * <p>
	 * A value naming an option that does not exist yields {@code null} rather than a smaller
	 * selection: a selection missing one of its values matches other rows than the declared one.
	 * </p>
	 *
	 * @param value
	 *        The declared criterion value: an option value, or a collection of option values.
	 * @param options
	 *        The option values this filter offers.
	 * @see com.top_logic.table.ColumnFilter#stateFor(Object)
	 */
	public static OptionsFilterState select(Object value, Collection<?> options) {
		if (value == null) {
			return null;
		}
		if (options.contains(value)) {
			return new OptionsFilterState(Set.of(value));
		}
		if (!(value instanceof Collection<?> values) || values.isEmpty()) {
			return null;
		}
		Set<Object> selected = new LinkedHashSet<>();
		for (Object element : values) {
			if (!options.contains(element)) {
				return null;
			}
			selected.add(element);
		}
		return new OptionsFilterState(selected);
	}

}
