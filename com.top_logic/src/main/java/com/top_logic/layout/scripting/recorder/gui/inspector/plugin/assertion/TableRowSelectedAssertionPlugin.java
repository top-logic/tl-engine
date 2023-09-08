/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion.Comparision;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.ValueInContextNamingScheme.ValueInContextName;
import com.top_logic.layout.scripting.recorder.ref.value.TableAspect;
import com.top_logic.layout.table.TableData;

/**
 * {@link AssertionPlugin} to assert that the row object in a table selected.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableRowSelectedAssertionPlugin extends SingleValueAssertionPlugin<TableCell, FormField> {

	/**
	 * Creates a new {@link TableRowSelectedAssertionPlugin}.
	 */
	public TableRowSelectedAssertionPlugin(TableCell model) {
		super(model, false, "tableRowSelected");
	}

	@Override
	protected FormField createValueField(String name) {
		BooleanField field = FormFactory.newBooleanField(name);
		return field;
	}

	@Override
	protected Object getInitialValue() {
		TableData tableData = getModel().getTableData();
		return tableData.getSelectionModel().isSelected(getModel().getRowObject());
	}

	@Override
	protected GuiAssertion buildAssertion() {
		boolean selected = ((Boolean) getExpectedValueField().getValue()).booleanValue();
		ModelName selectionName = ModelResolver.buildModelName(getModel().getTableData(), getSelection(selected));

		ValueInContextName selectionInContext = createSelectionInContext(selectionName);

		TableAspect tableName = ReferenceInstantiator.tableSelection(getModel().getTableData());
		Comparision comparison = getComparison(selected);
		
		return ActionFactory.valueAssertion(selectionInContext, tableName, comparison, !selected, getComment());
	}

	private ValueInContextName createSelectionInContext(ModelName selectionName) {
		return ActionFactory.createValueInContext(ModelResolver.buildModelName(getModel().getTableData()), selectionName);
	}

	private Object getSelection(boolean isSelectedRow) {
		TableData tableData = getModel().getTableData();
		if (isSelectedRow) {
			return tableData.getSelectionModel().getSelection();
		} else {
			return getModel().getRowObject();
		}
	}

	private Comparision getComparison(boolean isSelectedRow) {
		if (isSelectedRow) {
			return Comparision.SET_EQUALS;
		} else {
			return Comparision.CONTAINS;
		}
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TABLE_ROW_SELECTION;
	}

}

