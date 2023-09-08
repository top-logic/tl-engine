/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.TableField;

/**
 * {@link SimpleTableDialog} displaying a {@link TableField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TableFieldDialog extends SimpleTableDialog {

	private boolean _selectable = false;

	/**
	 * Creates a new {@link TableFieldDialog}.
	 */
	public TableFieldDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height) {
		super(resourcePrefix, width, height);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		TableField tableField = createTableField(INPUT_FIELD);
		tableField.setSelectable(_selectable);
		context.addMember(tableField);
	}

	/**
	 * Whether the result table should be selectable.
	 */
	public void setSelectable(boolean selectable) {
		_selectable = selectable;
	}

	/**
	 * Creates the displayed {@link TableField}.
	 * 
	 * @param tableFieldName
	 *        Name of the returned {@link TableField}.
	 */
	protected abstract TableField createTableField(String tableFieldName);

}

