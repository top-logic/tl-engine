/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;

/**
 * {@link TableActionOp} that sorts a table by a a given column.
 * 
 * @see SortTableColumn
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SortTableColumnOp extends TableActionOp<SortTableColumn> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SortTableColumnOp}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public SortTableColumnOp(InstantiationContext context, SortTableColumn config) {
		super(context, config);
	}

	@Override
	protected void internalProcess(ActionContext context, TableControl table, Object argument) {
		TableData tableData = table.getTableData();
		List<SortConfig> sortConfigs = resolveSortOrder(tableData);
		checkColumns(tableData, sortConfigs);
		TableControl.sortTable(tableData, sortConfigs);
	}

	private void checkColumns(TableData tableData, List<SortConfig> sortConfigs) {
		for (SortConfig sortConfig : sortConfigs) {
			String columnName = sortConfig.getColumnName();
			checkColumn(tableData, columnName);
		}
	}

	private void checkColumn(TableData tableData, String columnName) {
		checkColumnExists(tableData, columnName);
		checkColumnIsVisible(tableData, columnName);
		checkColumnIsSortable(tableData, columnName);
	}

	private void checkColumnIsSortable(TableData tableData, String columnName) {
		if (!isColumnSortable(tableData, columnName)) {
			fail("Column '" + columnName + "' is not sortable.");
		}
	}

	private boolean isColumnSortable(TableData tableData, String columnName) {
		TableViewModel viewModel = tableData.getViewModel();
		int columnIndex = viewModel.getColumnIndex(columnName);
		return viewModel.isSortable(columnIndex);
	}

	private List<SortConfig> resolveSortOrder(TableData tableData) {
		if (!getConfig().isLabel()) {
			return getConfig().getSortOrders();
		}
		List<SortConfig> result = new ArrayList<>();
		for (SortConfig labelSortConfig : getConfig().getSortOrders()) {
			result.add(createSortConfig(tableData, labelSortConfig));
		}
		return result;
	}

	private SortConfig createSortConfig(TableData tableData, SortConfig labelSortConfig) {
		String columnName = ScriptTableUtil.getColumnName(labelSortConfig.getColumnName(), tableData);
		boolean ascending = labelSortConfig.getAscending();
		return ReferenceInstantiator.sortConfig(columnName, ascending);
	}

}
