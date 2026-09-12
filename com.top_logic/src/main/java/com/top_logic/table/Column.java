/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import java.util.List;
import java.util.Optional;

import com.top_logic.basic.util.ResKey;

/**
 * A declarative, type-safe column definition.
 *
 * <p>
 * Replaces the legacy {@code ColumnConfiguration} + untyped {@code Accessor}. A column is
 * generic over the row business object type {@code R} and its own cell value type
 * {@code V}; a table holds {@code List<Column<R, ?>>}. Capabilities beyond display
 * (sorting, filtering, aggregation) are optional and absent by default.
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
	 * Renders the cell content for the given row.
	 *
	 * <p>
	 * The default implementation renders the {@link #value(Object) cell value} through the
	 * {@link #renderer()}. Implementations that need the row object itself (e.g. to produce an
	 * interactive cell control bound to the row) override this method while keeping the typed
	 * {@link #value(Object)} accessor for sorting and filtering.
	 * </p>
	 */
	default CellContent renderCell(R row) {
		return renderer().render(value(row));
	}

	/**
	 * The text of this row's cell examined by the free-text {@link SearchSpec search}, or
	 * {@code null} if this column holds no searchable text.
	 *
	 * <p>
	 * The default implementation takes the text from the {@link #renderCell(Object) rendered
	 * cell content}, so the search finds what the user sees: the
	 * {@link CellContent.Text} and {@link CellContent.Labeled} variants carry text, while
	 * {@link CellContent.Editable}, {@link CellContent.Raw} and {@link CellContent.Empty}
	 * carry none - a column rendering those never matches a search. A column that can produce
	 * its text more cheaply than by rendering, or that renders a bespoke control over a
	 * textual value, overrides this method.
	 * </p>
	 */
	default String searchText(R row) {
		CellContent content = renderCell(row);
		if (content instanceof CellContent.Text text) {
			return text.text();
		}
		if (content instanceof CellContent.Labeled labeled) {
			return labeled.text();
		}
		return null;
	}

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
	 *
	 * <p>
	 * A column {@link #pinnedEnd() pinned} to the end of the table is never frozen: it already
	 * stays visible, at the other edge.
	 * </p>
	 */
	default boolean frozenEligible() {
		return !pinnedEnd();
	}

	/**
	 * Whether the user may decide about this column, i.e. show, hide and move it.
	 *
	 * <p>
	 * Switch this off for a column that is part of what the table <em>does</em> rather than of the
	 * data it shows - an action column holding a per-row button, for instance. Such a column has no
	 * header label to offer in a column selection, and hiding it would take away the action with no
	 * way to bring it back. It is therefore left out of {@link TableView#columnOptions()} and stays
	 * where it is when {@link TableView#setColumnOrder(List)} rearranges the rest.
	 * </p>
	 */
	default boolean selectable() {
		return !pinnedEnd();
	}

	/**
	 * Whether this column keeps its place at the end of the table.
	 *
	 * <p>
	 * Such a column is rendered after every other column and stays visible while the table scrolls
	 * horizontally, so that what it holds is at hand wherever the table is scrolled to - the buttons
	 * acting on a row, for instance. It is the table's own, not part of the arrangement the user
	 * makes: it can neither be moved, hidden, frozen nor resized, and the rows cannot be grouped by
	 * it.
	 * </p>
	 *
	 * <p>
	 * A pinned column is therefore neither {@link #frozenEligible() frozen} nor
	 * {@link #selectable() selectable}.
	 * </p>
	 */
	default boolean pinnedEnd() {
		return false;
	}

	/**
	 * Optional CSS class put on every cell of this column, its heading included, or {@code null}
	 * for none.
	 *
	 * <p>
	 * Describes how the column presents its cells, independently of the rows: a column holding a
	 * button instead of text, for instance, drops the padding a text cell needs and centers its
	 * content. What one cell looks like depending on the row it is in is {@link #cssClass(Object)}.
	 * </p>
	 */
	default String cssClass() {
		return null;
	}

	/**
	 * Optional CSS class for a cell in the given row, or {@code null} for none.
	 *
	 * @see #cssClass() The class the whole column carries.
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
