/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Collection;
import java.util.List;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.tree.TreeTableAccessor;
import com.top_logic.util.Resources;

/**
 * Returns {@link CompareInfo} for the compare column from the {@link CompareRowObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareInfoAccessor extends ReadOnlyAccessor<Object> {

	private final CompareService<? extends CompareInfo> _compareService;
	private TableModel _tableModel;
	private Accessor<Object> rowObjectAccessor;

	/**
	 * Creates a new {@link CompareInfoAccessor}.
	 * 
	 * @param compareService
	 *        The {@link CompareService} used for comparison.
	 * @param isTree
	 *        - whether the accessor shall be applied to a tree table or not.
	 */
	@SuppressWarnings("unchecked")
	public CompareInfoAccessor(CompareService<? extends CompareInfo> compareService, boolean isTree) {
		_compareService = compareService;
		if (isTree) {
			rowObjectAccessor = (Accessor<Object>) (Accessor<?>) new TreeTableAccessor(IdentityAccessor.INSTANCE);
		} else {
			rowObjectAccessor = IdentityAccessor.INSTANCE;
		}
	}

	@Override
	public Object getValue(Object rowObject, String property) {
		CompareRowObject compareRow = (CompareRowObject) rowObjectAccessor.getValue(rowObject, property);
		CompareInfo compareInfo = _compareService.newCompareInfo(compareRow.baseValue(), compareRow.changeValue());
		if (isUnchanged(compareInfo.getChangeInfo())) {
			compareInfo = getDeepChanges(rowObject, compareRow, property);
		}
		return compareInfo;
	}

	private CompareInfo getDeepChanges(Object rawRowObject, CompareRowObject compareRow, String property) {
		AttributeCompareInfo compareInfo =
			_compareService.newAttributeCompareInfo(compareRow.baseValue(), compareRow.changeValue());
		if (_tableModel != null) {
			if (isSideBySideDisplay()) {
				Column leftTablePart =
					_tableModel.getHeader().getColumn(SideBySideCompareTableProvider.LEFT_TABLE_PART);
				List<Column> elementaryColumnsOfGroup =
					_tableModel.getHeader().getElementaryColumnsOfGroup(leftTablePart);
				getCompareInfosFromColumns(rawRowObject, property, compareInfo, elementaryColumnsOfGroup);
			} else {
				getCompareInfosFromColumns(rawRowObject, property, compareInfo, _tableModel.getHeader().getAllElementaryColumns());
			}
		}
		return compareInfo;
	}

	private boolean isSideBySideDisplay() {
		Collection<Column> groups = _tableModel.getHeader().getGroups();
		for (Column column : groups) {
			if (column.getName().equals(SideBySideCompareTableProvider.LEFT_TABLE_PART)) {
				return true;
			}
		}

		return false;
	}
	
	private void getCompareInfosFromColumns(Object rawRowObject, String property, AttributeCompareInfo compareInfo, List<Column> elementaryColumns) {
		for (Column column : elementaryColumns) {
			if (isApplicableColumn(rawRowObject, column, property)) {
				CompareInfo attributeCompareInfo =
					(CompareInfo) _tableModel.getValueAt(rawRowObject, column.getName());
				if (attributeCompareInfo != null) {
					compareInfo.addCompareInfo(column.getName(), attributeCompareInfo,
						Resources.getInstance().getString(column.getLabel(_tableModel.getTableConfiguration())));
				}
			}
		}
	}

	private boolean isApplicableColumn(Object rawRowObject, Column column, String property) {
		return isNotSelectColumn(column) && isNotCompareInfoColumn(column, property) && isNotExcludedColumn(column)
			&& isExistentColumn(rawRowObject, column);
	}

	private boolean isNotCompareInfoColumn(Column column, String property) {
		return !column.getName().equals(property);
	}

	private boolean isNotSelectColumn(Column column) {
		return !column.getName().equals(TableControl.SELECT_COLUMN_NAME);
	}

	private boolean isNotExcludedColumn(Column column) {
		return column.getConfig().getVisibility() != DisplayMode.excluded;
	}

	private boolean isExistentColumn(Object rawRowObject, Column column) {
		return column.getConfig().getCellExistenceTester().isCellExistent(rawRowObject, column.getName());
	}

	private boolean isUnchanged(ChangeInfo changeInfo) {
		return changeInfo == ChangeInfo.NO_CHANGE;
	}

	/**
	 * Sets the {@link TableModel}, which is used to determine column cell values.
	 */
	public void setTableModel(TableModel tableViewModel) {
		_tableModel = tableViewModel;
	}

}

