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
 * A view-supplied filter editor for a single column, hosted by the {@link TableViewControl}'s filter
 * dialog instead of the built-in {@code FilterEditors} pipeline.
 *
 * <p>
 * Lets the model-aware view layer build a richer filter UI (e.g. a whole form over a transient model
 * instance) that the lower react layer cannot assemble itself, while the control still owns the
 * dialog chrome (window, reset / cancel / apply). For ordinary filters no {@link ColumnFilterUI} is
 * registered and the standard editor pipeline is used.
 * </p>
 */
public interface ColumnFilterUI {

	/**
	 * Builds the dialog body (the filter form) for one opening of the dialog.
	 */
	ReactControl buildForm(ReactContext context);

	/**
	 * Snapshots the current form input into the {@link FilterState} to apply, or {@code null} to
	 * clear the filter.
	 */
	FilterState read();

}
