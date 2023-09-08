/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;


import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorUtil;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;

/**
 * An assertion about the value of a {@link TableCell}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableCellValueAssertionPlugin extends SingleValueAssertionPlugin<TableCell, FormField> {

	/**
	 * Creates a {@link TableCellValueAssertionPlugin}.
	 * 
	 * @param model
	 *        Is not allowed to be null.
	 */
	public TableCellValueAssertionPlugin(TableCell model) {
		super(unwrapValue(model), true, "tableCellValue");
	}

	private static TableCell unwrapValue(TableCell model) {
		Object value = model.getValue();
		if (!(value instanceof CompareInfo)) {
			return model;
		}
		TableCell clonedModel = model.clone();
		clonedModel.setValue(((CompareInfo) value).getDisplayedObject());
		return clonedModel;
	}

	@Override
	protected FormField createValueField(String name) {
		return GuiInspectorUtil.newFieldForValueType(getModel().getValue(), name);
	}

	@Override
	protected Object getInitialValue() {
		return getModel().getValue();
	}

	@Override
	protected GuiAssertion buildAssertion() {
		getModel().setValue(getExpectedValueField().getValue());
		return ActionFactory.tableCellValueAssertion(getModel(), getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TABLE_CELL_VALUE;
	}
}
