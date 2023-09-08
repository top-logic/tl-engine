/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.table;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.GuiInspectorPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.SingleValueAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.ui.table.TableColumnSortabilityNaming.TableColumnSortabilityName;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.TableData;

/**
 * {@link GuiInspectorPlugin} for asserting whether a table column can be sorted.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableColumnSortabilityAssertionPlugin extends SingleValueAssertionPlugin<TableCell, BooleanField> {

	/**
	 * Creates a {@link TableColumnSortabilityAssertionPlugin}.
	 */
	public TableColumnSortabilityAssertionPlugin(TableCell model) {
		super(model, false, "columnSortability");
	}

	@Override
	protected BooleanField createValueField(String name) {
		return FormFactory.newBooleanField(name);
	}

	@Override
	protected Object getInitialValue() {
		String columnName = getModel().getColumnName();
		TableData tableData = getModel().getTableData();
		return ScriptTableUtil.canBeSorted(tableData, columnName);
	}

	@Override
	protected GuiAssertion buildAssertion() {
		boolean expectedValue = (Boolean) getExpectedValueField().getValue();
		return ActionFactory.tableColumnAspectAssertion(
			TableColumnSortabilityName.class, getModel(), expectedValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TABLE_COLUMN_SORTABILITY;
	}

}
