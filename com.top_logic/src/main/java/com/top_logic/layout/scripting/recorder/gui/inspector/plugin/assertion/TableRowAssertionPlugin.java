/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.IntValue;
import com.top_logic.layout.scripting.recorder.ref.value.RowTableValue;

/**
 * {@link AssertionPlugin} to assert that the row object in a table is displayed in a row with a
 * given number.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableRowAssertionPlugin extends SingleValueAssertionPlugin<TableCell, FormField> {

	public TableRowAssertionPlugin(TableCell model) {
		super(model, true, "tableRow");
	}

	@Override
	protected FormField createValueField(String name) {
		IntField field = FormFactory.newIntField(name);
		return field;
	}

	@Override
	protected Object getInitialValue() {
		return getModel().getTableData().getViewModel().getRowOfObject(getModel().getRowObject());
	}

	@Override
	protected GuiAssertion buildAssertion() {
		IntValue expectedRow = ReferenceInstantiator.intValue((Integer) getExpectedValueField().getValue());
		RowTableValue cellValue = ReferenceInstantiator.tableValue(RowTableValue.class, getModel());
		
		ModelName actualRow = ReferenceInstantiator.rowInTable(cellValue);
		return ActionFactory.valueAssertion(expectedRow, actualRow, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TABLE_ROW;
	}

}

