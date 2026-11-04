/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.table.FilterState;

/**
 * A view-supplied filter editor for a single column, shown in the {@link TableViewControl}'s filter
 * popover instead of the built-in {@code FilterEditors} pipeline.
 *
 * <p>
 * Lets the model-aware view layer build a richer filter UI (e.g. a whole form over a transient model
 * instance) that the lower react layer cannot assemble itself, while the control still owns the
 * popover and its footer: removing the filter, and applying the form. Unlike the built-in filters,
 * whose fields filter at once, such a form takes effect only when applied, since evaluating it may
 * be expensive. For ordinary filters no {@link ColumnFilterUI} is registered and the standard editor
 * pipeline is used.
 * </p>
 */
public interface ColumnFilterUI {

	/**
	 * Builds the filter form for one opening of the popover, and again after the filter was
	 * removed. What the new form shows is up to the UI: one that remembers its last input across
	 * openings shows it again, ready to be applied anew.
	 *
	 * <p>
	 * The popover keeps its content away from its border, so the form is returned without an inset
	 * of its own.
	 * </p>
	 */
	ReactControl buildForm(ReactContext context);

	/**
	 * Snapshots the current form input into the {@link FilterState} to apply, or {@code null} to
	 * clear the filter.
	 */
	FilterState read();

}
