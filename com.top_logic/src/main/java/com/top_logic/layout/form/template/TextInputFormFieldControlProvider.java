/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.TextInputControl;

/**
 * Creates new {@link TextInputControl}s.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class TextInputFormFieldControlProvider extends DefaultFormFieldControlProvider {

	private int rows;
	private int columns;
	private boolean multiline;
	
	/** 
	 * Creates a new {@link TextInputFormFieldControlProvider} with the given parameters.
	 */
	public TextInputFormFieldControlProvider(int rows, int columns, boolean multiline) {
		super();
		this.rows = rows;
		this.columns = columns;
		this.multiline = multiline;
	}

	@Override
	public Control createControl(Object model, String style) {
		TextInputControl control = new TextInputControl((FormField) model);
		control.setRows(this.rows);
		control.setColumns(this.columns);
		control.setMultiLine(this.multiline);
		
		return control;
	}
	
	/**
	 * Returns the rows.
	 */
	public int getRows() {
		return this.rows;
	}

	/**
	 * Returns the columns.
	 */
	public int getColumns() {
		return this.columns;
	}

	/**
	 * Returns the multiline.
	 */
	public boolean isMultiline() {
		return this.multiline;
	}
	
}
