/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;

/**
 * Strategy turning a {@link ColumnSetup} into a green-field table {@link Column}, and optionally
 * contributing per-session UI (such as a custom filter dialog).
 *
 * <p>
 * This is the single extension point for column integration:
 * {@link com.top_logic.layout.view.element.TableElement} treats every
 * column uniformly through this interface, with no knowledge of concrete filter kinds. The variation
 * that used to be a type switch lives in the bindings themselves:
 * </p>
 * <ul>
 * <li>A column without a custom filter uses {@link #TYPE_DERIVED} - the attribute's model type
 * decides accessor, renderer, comparator and filter.</li>
 * <li>A plain {@link ColumnFilter} configured on a column uses {@link #forValueFilter} - the column
 * filters by the cell's display text.</li>
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
	 * Binding deriving the column purely from the attribute's model type, used when a column
	 * configures no custom filter.
	 */
	ColumnBinding TYPE_DERIVED =
		setup -> ColumnProviderService.getInstance().createColumn(setup.attribute(), setup.label(), setup.part());

	/**
	 * Binding for a value-based custom filter: the column shows the attribute's type-derived
	 * display and sorts by the cell's display text, with the configured filter applied to that
	 * text.
	 *
	 * @param filter
	 *        The configured filter; value-based filters operate on the display text, so it is used
	 *        as a {@code ColumnFilter<String>}.
	 */
	static ColumnBinding forValueFilter(ColumnFilter<?> filter) {
		@SuppressWarnings("unchecked")
		ColumnFilter<String> textFilter = (ColumnFilter<String>) filter;
		return setup -> ColumnProviderService.getInstance()
			.createColumn(setup.attribute(), setup.label(), setup.part(), textFilter);
	}

}
