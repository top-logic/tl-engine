/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.table.TableCellFullText;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * An assertion about the full-text of a {@link TableCell}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableCellFullTextAssertionPlugin extends SingleValueAssertionPlugin<TableCell, FormField> {

	/**
	 * Creates a {@link TableCellFullTextAssertionPlugin}.
	 * 
	 * @param model
	 *        Is not allowed to be null.
	 */
	public TableCellFullTextAssertionPlugin(TableCell model) {
		super(model, true, "tableCellFullText");
	}

	@Override
	protected FormField createValueField(String name) {
		return FormFactory.newStringField(name);
	}

	@Override
	protected String getInitialValue() {
		Object value = getModel().getValue();
		String columnName = getModel().getColumnName();
		return TableCellFullTextAssertionPlugin.getFullText(getModel().getTableData(), columnName, value);
	}

	@Override
	protected GuiAssertion buildAssertion() {
		ModelName expectedValue = ModelResolver.buildModelName(getExpectedValueField().getValue());
		ModelName actualValue = ReferenceInstantiator.tableValue(TableCellFullText.class, getModel());
		return ActionFactory.valueAssertion(expectedValue, actualValue, getComment());
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.TABLE_CELL_FULL_TEXT;
	}

	/**
	 * Returns the full text of the given cell value containing in the column with given column
	 * name.
	 * 
	 * <p>
	 * If the actual label is <code>null</code>, the empty string is returned.
	 * </p>
	 */
	public static String getFullText(TableData tableData, String columnName, Object cellValue) {
		TableModel tableModel = tableData.getTableModel();
		ColumnConfiguration columnConfig = tableModel.getColumnDescription(columnName);
		cellValue = columnConfig.getSortKeyProvider().map(cellValue);
		LabelProvider fullTextProvider = columnConfig.getFullTextProvider();
		if (fullTextProvider != null) {
			return StringServices.nonNull(fullTextProvider.getLabel(cellValue));
		} else {
			return StringServices.EMPTY_STRING;
		}
	}

}
