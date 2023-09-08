/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.ExpandableTextInputControl;

/**
 * @author <a href="mailto:tri@top-logic.com">tri</a>
 */
public class ExpandableTextInputFormFieldControlProvider extends TextInputFormFieldControlProvider {

	/**
	 * create new...
	 */
	public ExpandableTextInputFormFieldControlProvider(int rows, int columns, boolean multiline) {
		super(rows, columns, multiline);
	}

	@Override
	public Control createControl(Object aModel, String style) {
		ExpandableTextInputControl control = new ExpandableTextInputControl((FormField) aModel);
		control.setRows(getRows());
		control.setColumns(getColumns());
		control.setMultiLineColumns(35);

		// Must not be set, since the multi-line mode is controlled by the field model.
		// control.setMultiLine(isMultiline());

		control.setMaxLengthShown(20);
		return control;
	}
}
