/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;

import com.top_logic.table.FilterState;

/**
 * A UI-toolkit-neutral editor for a single column filter: a set of {@link FilterField
 * input fields} (seeded from the current state) and a {@link #read()} that turns the
 * current field values back into a {@link FilterState}.
 *
 * <p>
 * This is the "mini-form" backing a filter popup: a UI tier renders the fields with its
 * native input controls and, on apply, calls {@link #read()} to obtain the new filter
 * state. The same editor is reusable by any renderer.
 * </p>
 */
public interface FilterEditor {

	/**
	 * The input fields, seeded from the current filter state.
	 */
	List<FilterField> fields();

	/**
	 * Reads the current field values into a {@link FilterState}. An empty selection yields
	 * an {@link FilterState#isEmpty() empty} state.
	 */
	FilterState read();

}
