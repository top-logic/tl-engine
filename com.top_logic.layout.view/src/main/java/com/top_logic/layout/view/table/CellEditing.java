/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FormControl;

/**
 * How the cells of a table column are edited: which rows offer an edited cell at all, what holds
 * the edited value, and which control the user enters it with.
 *
 * <p>
 * This is the only thing a table asks about editing a cell, so what a column shows and where it
 * writes an edit back to are described together, by the column itself. A column that declares none
 * of this is displayed but not edited.
 * </p>
 *
 * @see ColumnSetup#editing()
 */
public interface CellEditing {

	/**
	 * Whether the cell of the given row can be edited.
	 *
	 * <p>
	 * Asked for every row of an edited table, so a column can offer the edit on some of its rows
	 * and not on others.
	 * </p>
	 *
	 * @param row
	 *        The row whose cell is displayed.
	 */
	boolean canEdit(Object row);

	/**
	 * Creates the field model holding the edited value of the given row's cell.
	 *
	 * <p>
	 * Created once per row and column and asked again whenever that cell is displayed, so the model
	 * carries the state of the edit - the value entered, whether it is valid, whether it changed
	 * anything.
	 * </p>
	 *
	 * @param row
	 *        The row whose cell is edited, for which {@link #canEdit(Object)} holds.
	 * @param form
	 *        The form the edit takes place in.
	 */
	BoundFieldModel createModel(Object row, FormControl form);

	/**
	 * Creates the control the given row's cell is edited with.
	 *
	 * @param context
	 *        The React context the control is created in.
	 * @param row
	 *        The row whose cell is edited.
	 * @param model
	 *        The field model of that cell, created by {@link #createModel(Object, FormControl)}.
	 */
	ReactControl createControl(ReactContext context, Object row, BoundFieldModel model);

}
