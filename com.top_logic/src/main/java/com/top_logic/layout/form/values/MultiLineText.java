/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} generating a multi-line {@link TextInputControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MultiLineText implements ControlProvider {

	/** Number of rows to display in the text area. */
	public static final int DEFAULT_ROWS = 5;

	/** Singleton {@link MultiLineText} instance. */
	public static final MultiLineText INSTANCE = new MultiLineText(DEFAULT_ROWS);

	private final int _rows;

	/**
	 * Creates a {@link MultiLineText}.
	 * 
	 * @param rows
	 *        The number of rows to display in the text area.
	 */
	public MultiLineText(int rows) {
		_rows = rows;
	}

	/**
	 * The number of rows to display in the text area.
	 */
	public int getRows() {
		return _rows;
	}

	@Override
	public Control createControl(Object model, String style) {
		FormField field = (FormField) model;
		TextInputControl control = createControl(field);
		control.setMultiLine(true);
		control.setRows(_rows);
		return control;
	}

	/**
	 * Creation of the result {@link TextInputControl}.
	 * 
	 * <p>
	 * The control is later set to {@link TextInputControl#setMultiLine(boolean) multi line} and
	 * gets the number of rows given at construction.
	 * </p>
	 */
	protected TextInputControl createControl(FormField field) {
		return new TextInputControl(field);
	}
}