/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.Optional;

import com.top_logic.basic.util.ResKey;

/**
 * A declarative, type-safe column definition.
 *
 * <p>
 * Replaces the legacy {@code ColumnConfiguration} + untyped {@code Accessor}. A column is
 * generic over the row business object type {@code R} and its own cell value type
 * {@code V}; a table holds {@code List<Column<R, ?>>}. Capabilities beyond display
 * (sorting, filtering, editing, aggregation) are optional and absent by default.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 * @param <V>
 *        The cell value type produced by this column.
 */
public interface Column<R, V> {

	/**
	 * Stable, unique programmatic name of this column.
	 */
	String name();

	/**
	 * The column header label.
	 */
	ResKey label();

	/**
	 * Extracts the typed cell value from a row business object.
	 */
	V value(R row);

	/**
	 * Describes how to render a cell value into UI-neutral {@link CellContent}.
	 */
	CellRenderer<V> renderer();

	/**
	 * The sort capability, or empty if the column is not sortable.
	 */
	default Optional<Sort<V>> sort() {
		return Optional.empty();
	}

	/**
	 * The filter capability, or empty if the column is not filterable.
	 */
	default Optional<ColumnFilter<V>> filter() {
		return Optional.empty();
	}

	/**
	 * The inline-editing capability, or empty if the column is read-only.
	 */
	default Optional<CellEditor<R, V>> editor() {
		return Optional.empty();
	}

	/**
	 * The aggregation capability for footer/group-total cells, or empty if none.
	 */
	default Optional<Aggregator<R, V>> aggregate() {
		return Optional.empty();
	}

	/**
	 * The default display width in pixels.
	 */
	default int defaultWidth() {
		return 150;
	}

	/**
	 * Whether this column may be frozen (fixed) by the user.
	 */
	default boolean frozenEligible() {
		return true;
	}

	/**
	 * Optional CSS class for a cell in the given row, or {@code null} for none.
	 */
	default String cssClass(R row) {
		return null;
	}

	/**
	 * Optional cell-existence test gating filter visibility, or empty if cells always
	 * exist.
	 */
	default Optional<CellExistence<R>> existence() {
		return Optional.empty();
	}

}
