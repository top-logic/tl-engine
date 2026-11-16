/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.Sort;

/**
 * Strategy turning a {@link ColumnSetup} into a green-field table {@link Column}, and optionally
 * contributing per-session UI (such as a custom filter dialog).
 *
 * <p>
 * This is the single extension point for column integration:
 * {@link com.top_logic.layout.view.element.TableElement} treats every
 * column uniformly through this interface, with no knowledge of concrete filter kinds. The
 * variation lives in the bindings themselves:
 * </p>
 * <ul>
 * <li>A column without a custom filter uses {@link #TYPE_DERIVED} - the model type of its values
 * decides renderer, comparator and filter.</li>
 * <li>A plain {@link ColumnFilter} configured on a column uses {@link #forValueFilter} - the column
 * filters by the {@link ColumnSetup#text() text of the cell value}.</li>
 * <li>A richer filter (e.g. {@link ScriptedFilter}) implements this interface itself, so it controls
 * both its column value model and its dialog UI.</li>
 * </ul>
 *
 * <p>
 * Adding a new kind of self-integrating filter therefore means implementing this interface - no edit
 * to the table element or any dispatch site.
 * </p>
 */
@FunctionalInterface
public interface ColumnBinding {

	/**
	 * Builds the runtime column for the given setup.
	 */
	Column<Object, ?> createColumn(ColumnSetup setup);

	/**
	 * Contributes per-session UI for the column (e.g. a custom filter dialog) once the control
	 * exists. The default contributes nothing.
	 *
	 * @param setup
	 *        The column descriptor (carries the {@link ColumnSetup#viewContext()} for resolving
	 *        channels).
	 * @param control
	 *        The control rendering the table.
	 */
	default void installUI(ColumnSetup setup, TableViewControl<?> control) {
		// No custom UI by default.
	}

	/**
	 * The channels the column this binding builds reads, in addition to the row - e.g. the inputs a
	 * filter evaluates its criteria with.
	 *
	 * <p>
	 * What the column shows or filters follows these channels by the column being built again. The
	 * default reads none.
	 * </p>
	 *
	 * @see ColumnDeclaration#inputs()
	 */
	default List<InputRef> inputs() {
		return List.of();
	}

	/**
	 * The sort capability of the column this binding builds, ordering its values as the given
	 * comparator orders the cell values they stand for.
	 *
	 * <p>
	 * The default is the {@link ColumnProviderService#cellSort(Supplier) sort of the cell values}
	 * themselves, for a column whose values are the cell values {@link ColumnSetup#value() read
	 * from the row}. A binding whose column holds other values - e.g. a cell value combined with
	 * its row - orders them by the cell value they carry, and marks those carrying no ordinary
	 * value {@link Sort#isTrailing(Object) trailing}.
	 * </p>
	 *
	 * @param cellOrder
	 *        The ascending order of the cell values, never passed {@code null}, the
	 *        {@link ColumnProviderService#UNREADABLE} marker or a value
	 *        {@link ColumnProviderService#isNoValue(Object) showing nothing}.
	 */
	default Sort<?> sort(Comparator<Object> cellOrder) {
		return ColumnProviderService.cellSort(() -> cellOrder);
	}

	/**
	 * The cell value a value of the column this binding builds stands for: what the
	 * {@link ColumnSetup#display() display}, the {@link ColumnSetup#tooltip() tooltip}, the
	 * {@link ColumnSetup#cellClass() cell class} and the {@link ColumnSetup#cellRole() cell role}
	 * of the column are given.
	 *
	 * <p>
	 * The default is the value itself, for a column whose values are the cell values
	 * {@link ColumnSetup#value() read from the row}. A binding whose column holds other values -
	 * e.g. a cell value combined with its row - answers the cell value such a value carries.
	 * </p>
	 *
	 * @param columnValue
	 *        A value of the column, also a value a group of rows is formed by; may be {@code null}.
	 */
	default Object cellValue(Object columnValue) {
		return columnValue;
	}

	/**
	 * Binding deriving the column purely from the {@link ColumnSetup#type() model type of its
	 * values}, used when a column configures no custom filter. A label column sorts and
	 * text-filters by the {@link ColumnSetup#text() text of its values}.
	 */
	ColumnBinding TYPE_DERIVED = setup -> ColumnProviderService.getInstance()
		.createColumn(setup.name(), setup.label(), setup.type(), setup.value(), setup.text());

	/**
	 * Binding for a value-based custom filter: the column shows the type-derived display of its
	 * values and sorts by the {@link ColumnSetup#text() text of its values}, with the configured
	 * filter applied to that text.
	 *
	 * @param filter
	 *        The configured filter; value-based filters operate on the display text, so it is used
	 *        as a {@code ColumnFilter<String>}.
	 */
	static ColumnBinding forValueFilter(ColumnFilter<?> filter) {
		@SuppressWarnings("unchecked")
		ColumnFilter<String> textFilter = (ColumnFilter<String>) filter;
		return setup -> ColumnProviderService.getInstance()
			.createColumn(setup.name(), setup.label(), setup.type(), setup.value(), textFilter, setup.text());
	}

}
