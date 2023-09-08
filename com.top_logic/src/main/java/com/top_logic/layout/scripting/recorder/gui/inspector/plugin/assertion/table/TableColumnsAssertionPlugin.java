/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.table;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.GuiInspectorPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ui.table.TableColumnsNaming.TableColumnsName;
import com.top_logic.layout.scripting.util.ScriptTableUtil;

/**
 * {@link GuiInspectorPlugin} for asserting the column labels of a table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableColumnsAssertionPlugin extends SingleValueAssertionPlugin<TableCell, StringField> {

	private boolean _allColumns;

	/**
	 * Creates a {@link TableColumnsAssertionPlugin}.
	 */
	public TableColumnsAssertionPlugin(TableCell model, boolean allColumns) {
		super(model, false, allColumns ? "allColumns" : "displayedColumns");
		_allColumns = allColumns;
	}

	@Override
	protected StringField createValueField(String name) {
		return FormFactory.newStringField(name);
	}

	@Override
	protected Object getInitialValue() {
		List<String> columnLabels = ScriptTableUtil.getColumnLabels(getModel().getTableData(), _allColumns);
		return StringServices.join(columnLabels, ", ");
	}

	@Override
	protected GuiAssertion buildAssertion() {
		String expectedValue = (String) getExpectedValueField().getValue();
		List<String> expectedColumns = Arrays.asList(expectedValue.split("\\s*,\\s*"));
		return ActionFactory.tableColumnsAssertion(TableColumnsName.class, getModel(), _allColumns, expectedColumns,
			getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return _allColumns ? I18NConstants.TABLE_ALL_COLUMNS : I18NConstants.TABLE_DISPLAYED_COLUMNS;
	}

}
