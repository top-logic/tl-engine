/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.model.Column;

/**
 * An assertion about the label of a table column.
 * <p>
 * Asserts only {@link Column#getLabel(TableData)}, not what the {@link TableRenderer} actually
 * writes, as that would be to much effort to implement.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableColumnLabelAssertionPlugin extends SingleValueAssertionPlugin<TableCell, StringField> {

	/**
	 * Creates a {@link TableColumnLabelAssertionPlugin}.
	 * 
	 * @param model
	 *        Is not allowed to be null.
	 */
	public TableColumnLabelAssertionPlugin(TableCell model) {
		super(model, false, "tableColumnLabel");
	}

	@Override
	protected StringField createValueField(String name) {
		return FormFactory.newStringField(name);
	}

	@Override
	protected String getInitialValue() {
		String columnName = getModel().getColumnName();
		return getLabel(columnName);
	}

	private String getLabel(String columnName) {
		TableData tableData = getModel().getTableData();
		return ScriptTableUtil.getColumnLabel(columnName, tableData);
	}

	@Override
	protected GuiAssertion buildAssertion() {
		ModelName expectedValue = ModelResolver.buildModelName(getExpectedValueField().getValue());
		ModelName actualValue = ReferenceInstantiator.tableColumnLabel(getModel());
		return ActionFactory.valueAssertion(expectedValue, actualValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TABLE_COLUMN_LABEL;
	}

}
