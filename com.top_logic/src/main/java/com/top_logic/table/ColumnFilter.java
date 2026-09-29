/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The filter capability of a {@link Column}: a UI-neutral input descriptor, plus the logic
 * that turns a user-chosen {@link FilterState} into a predicate (and optionally a query
 * pushdown).
 *
 * <p>
 * This is the clean separation that the legacy {@code ConfiguredFilter} /
 * {@code FilterDialogBuilder} entangled: state ({@link FilterState}), logic
 * ({@link #predicate}) and UI descriptor ({@link #input()}) are independent, and no form
 * or control type appears anywhere.
 * </p>
 *
 * @param <V>
 *        The cell value type being filtered.
 */
public interface ColumnFilter<V> {

	/**
	 * The inputs this filter needs, for a UI tier to render an editor.
	 */
	FilterInput input();

	/**
	 * The in-memory predicate for the given user-chosen state. An
	 * {@link FilterState#isEmpty() empty} state should yield an accept-all predicate.
	 */
	Predicate<V> predicate(FilterState state);

	/**
	 * Optional translation of the given state into a data-tier query restriction.
	 */
	default Optional<FilterPushdown> pushdown(FilterState state) {
		return Optional.empty();
	}

	/**
	 * Whether this filter wants per-option {@link MatchCounts facet counts} computed.
	 */
	default boolean countsMatches() {
		return false;
	}

	/**
	 * Whether a UI should offer to invert this filter (accept exactly the non-matching rows).
	 *
	 * <p>
	 * Inversion is applied generically via {@link NegatedFilterState}; a filter only opts into
	 * the inversion checkbox by returning {@code true}. Filters for which inversion is redundant
	 * (e.g. a boolean filter, where picking the other value already inverts) leave this
	 * {@code false}.
	 * </p>
	 */
	default boolean supportsInversion() {
		return false;
	}

	/**
	 * Serializes the given state of this filter into a JSON value model (nested
	 * {@link java.util.Map}/{@link java.util.List}/{@link String}/{@link Number}/{@link Boolean}),
	 * for cross-session personalization.
	 *
	 * <p>
	 * Returns {@code null} when this filter's state cannot be stably serialized (the default), so
	 * the column's filter is simply not personalized. {@link NegatedFilterState inversion} is
	 * handled by the caller - implementations (de)serialize only their own inner state.
	 * </p>
	 *
	 * @param state
	 *        The state to serialize (never a {@link NegatedFilterState}).
	 */
	default Object toJson(FilterState state) {
		return null;
	}

	/**
	 * Reconstructs a filter state from a value previously produced by {@link #toJson(FilterState)},
	 * or {@code null} if it cannot be restored (e.g. stale or malformed).
	 */
	default FilterState fromJson(Object json) {
		return null;
	}

	/**
	 * The facet buckets a single cell value contributes to, used to compute {@link MatchCounts}.
	 *
	 * <p>
	 * For a value-equality options filter this is the value itself (one bucket). A predicate-based
	 * options filter (e.g. regular-expression facets) overrides this to return every option key the
	 * value matches - so one cell can count toward several facets.
	 * </p>
	 */
	default Collection<Object> facetKeys(V value) {
		return value == null ? List.of() : List.of(value);
	}

}
