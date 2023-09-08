/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.Collection;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.IntValue;
import com.top_logic.layout.scripting.recorder.ref.value.TableAspect;
import com.top_logic.layout.table.TableModel;

/**
 * {@link AssertionPlugin} to the number of rows in a table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableRowCountAssertionPlugin extends SingleValueAssertionPlugin<TableCell, FormField> {

	/**
	 * Creates an {@link AssertionPlugin} for the number of all rows in the table.
	 */
	public static TableRowCountAssertionPlugin assertAllRows(TableCell model) {
		return new TableRowCountAssertionPlugin(model, true);
	}

	/**
	 * Creates an {@link AssertionPlugin} for the number of displayed rows in the table.
	 */
	public static TableRowCountAssertionPlugin assertDisplayedRows(TableCell model) {
		return new TableRowCountAssertionPlugin(model, false);
	}

	private boolean _allRows;

	private TableRowCountAssertionPlugin(TableCell model, boolean allRows) {
		super(model, false, allRows ? "tableAllRows" : "tableDisplayedRows");
		_allRows = allRows;
	}

	@Override
	protected FormField createValueField(String name) {
		IntField field = FormFactory.newIntField(name);
		return field;
	}

	@Override
	protected Object getInitialValue() {
		TableModel tableModel = getModel().getTableData().getTableModel();
		Collection<?> rows;
		if (_allRows) {
			rows = tableModel.getAllRows();
		} else {
			rows = tableModel.getDisplayedRows();
		}
		return rows.size();
	}

	@Override
	protected GuiAssertion buildAssertion() {
		IntValue expectedNumberRows = ReferenceInstantiator.intValue((Integer) getExpectedValueField().getValue());
		TableAspect actualNumberRowsValue = ReferenceInstantiator.rowCount(getModel().getTableData(), _allRows);
		return ActionFactory.valueAssertion(expectedNumberRows, actualNumberRowsValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return _allRows ? I18NConstants.TABLE_ALL_ROWS : I18NConstants.TABLE_DISPLAYED_ROWS;
	}

}

