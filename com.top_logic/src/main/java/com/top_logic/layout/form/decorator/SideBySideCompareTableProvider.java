/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.DefaultCellRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnContainer;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.tree.TreeTableAccessor;
import com.top_logic.layout.tree.renderer.NoResourceProvider;
import com.top_logic.model.TLNamed;
import com.top_logic.tool.export.ExcelExportSupport;

/**
 * {@link TableConfigurationProvider} for comparison table, whereby structures will be shown side by
 * side.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SideBySideCompareTableProvider<CI extends CompareInfo> extends AbstractCompareTableProvider<CI> {

	/** Group column name of left table part, when displaying compare table in side by side mode. */
	public static final String LEFT_TABLE_PART = "leftTablePart";

	/** Group column name of right table part, when displaying compare table in side by side mode. */
	public static final String RIGHT_TABLE_PART = "rightTablePart";

	private final TableConfiguration _leftTablePartConfig;

	private final TableConfiguration _rightTablePartConfig;

	private TableConfigurationProvider _decoratorProvider;

	private boolean _isLeftTablePart = true;

	private Map<String, String> _columnIdNameMapping;

	private int _columnIdCounter;

	/**
	 * Create a new {@link SideBySideCompareTableProvider}.
	 */
	public SideBySideCompareTableProvider(CompareService<CI> compareService, TableConfiguration leftTablePart,
			TableConfiguration rightTablePart, TableConfigurationProvider decoratorProvider,
			ExcelExportSupport excelExport, boolean isTreeTable) {
		super(compareService, excelExport, isTreeTable);
		_leftTablePartConfig = leftTablePart;
		_rightTablePartConfig = rightTablePart;
		_decoratorProvider = decoratorProvider;
		_columnIdNameMapping = new HashedMap<>();
		_columnIdCounter = 0;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		_columnIdCounter = 0;
		_columnIdNameMapping.clear();
		copyGlobalTableProperties(table, _leftTablePartConfig);
		createLeftTablePart(table);
		_decoratorProvider.adaptConfigurationTo(table);
		createRightTablePart(table);
	}

	private void createLeftTablePart(TableConfiguration table) {
		_isLeftTablePart = true;
		createTablePart(table, LEFT_TABLE_PART, _leftTablePartConfig, I18NConstants.COMPARE_BASE_TABLE_PART);
	}

	private void createTablePart(TableConfiguration table, String groupName, TableConfiguration tableConfigPart,
			ResKey groupLabel) {
		ColumnConfiguration tablePart = table.declareColumn(groupName);
		tablePart.setColumnLabelKey(groupLabel);
		copyColumns(tablePart, tableConfigPart);
	}

	private void createRightTablePart(TableConfiguration table) {
		_isLeftTablePart = false;
		createTablePart(table, RIGHT_TABLE_PART, _rightTablePartConfig, I18NConstants.COMPARE_CHANGE_TABLE_PART);
	}

	@Override
	protected ColumnConfiguration createTargetColumn(ColumnContainer<? extends ColumnConfiguration> target,
			ColumnConfiguration sourceColumn) {
		String columnName = sourceColumn.getName();
		if (!columnName.equals(TableControl.SELECT_COLUMN_NAME)) {
			String columnId = Integer.toString(_columnIdCounter++);
			_columnIdNameMapping.put(columnId, columnName);
			return target.declareColumn(columnId);
		} else {
			return null;
		}
	}

	@Override
	protected CompareCellRenderer<CI> createCompareCellRenderer(CellRenderer finalCellRenderer) {
		if (_isLeftTablePart) {
			return new SideBySideCompareCellRenderer<>(finalCellRenderer, getCompareService(), isTreeTable(),
				ChangeInfo.REMOVED);
		} else {
			return new SideBySideCompareCellRenderer<>(finalCellRenderer, getCompareService(), isTreeTable(),
				ChangeInfo.CREATED);
		}
	}

	@Override
	protected CellRenderer createCompareTreeCellRenderer(ColumnConfiguration column) {
		ResourceProvider resourceProvider;
		if (!TLNamed.NAME_ATTRIBUTE.equals(_columnIdNameMapping.get(column.getName()))) {
			resourceProvider = MetaResourceProvider.INSTANCE;
		} else {
			resourceProvider = NoResourceProvider.INSTANCE;
		}

		ChangeInfo changeInfoSide;
		if (_isLeftTablePart) {
			changeInfoSide = ChangeInfo.REMOVED;
		} else {
			changeInfoSide = ChangeInfo.CREATED;
		}
		return new SideBySideCompareTreeCellRenderer(resourceProvider, DefaultCellRenderer.INSTANCE,
			SideBySideCompareTreeCellRenderer.DEFAULT_INDENT_CHARS, changeInfoSide).init(getCompareService());
	}

	@Override
	protected CellRenderer createCompareRowTypeCellRenderer(ColumnConfiguration column) {
		ResourceProvider resourceProvider;
		if (!TLNamed.NAME_ATTRIBUTE.equals(_columnIdNameMapping.get(column.getName()))) {
			resourceProvider = MetaResourceProvider.INSTANCE;
		} else {
			resourceProvider = NoResourceProvider.INSTANCE;
		}

		ChangeInfo changeInfoSide;
		if (_isLeftTablePart) {
			changeInfoSide = ChangeInfo.REMOVED;
		} else {
			changeInfoSide = ChangeInfo.CREATED;
		}
		return new SideBySideCompareRowTypeCellRenderer(resourceProvider, DefaultCellRenderer.INSTANCE, changeInfoSide)
			.init(getCompareService());
	}

	@Override
	protected void adaptSimpleCellRender(ColumnConfiguration column) {
		CellRenderer finalCellRenderer = column.finalCellRenderer();
		SimpleCompareCellRenderer compareCellRenderer;
		if (_isLeftTablePart) {
			compareCellRenderer =
				new SideBySideSimpleCompareCellRenderer(finalCellRenderer, isTreeTable(), ChangeInfo.REMOVED);
		} else {
			compareCellRenderer =
				new SideBySideSimpleCompareCellRenderer(finalCellRenderer, isTreeTable(), ChangeInfo.CREATED);
		}
		column.setCellRenderer(compareCellRenderer);
	}

	@Override
	protected void adaptAccessor(ColumnConfiguration column, Accessor<Object> columnAccessor) {
		Accessor<? extends Object> wrappedAccessor;
		if (_isLeftTablePart) {
			wrappedAccessor = new TablePartAccessor(columnAccessor, getCompareService(),
				_columnIdNameMapping.get(column.getName()));
		} else {
			wrappedAccessor = new TablePartAccessor(columnAccessor, getCompareService(),
				_columnIdNameMapping.get(column.getName()));
		}
		if (isTreeTable()) {
			wrappedAccessor = new TreeTableAccessor(wrappedAccessor);
		}
		column.setAccessor(wrappedAccessor);
	}

	@Override
	protected CellExistenceTester getCompareRowCellExistenceTester(
			ColumnConfiguration targetColumn) {
		return new TablePartCellExistenceTester(targetColumn.getCellExistenceTester(), _isLeftTablePart,
			_columnIdNameMapping.get(targetColumn.getName()));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void adaptSortKeyProvider(ColumnConfiguration targetColumn) {
		final Mapping<Object, ?> configuredSortKeyProvider = targetColumn.getSortKeyProvider();
		targetColumn.setSortKeyProvider(new TablePartMapping(configuredSortKeyProvider, _isLeftTablePart));
	}

	private static class TablePartAccessor extends CompareTableAccessor {

		private String _columnName;

		public TablePartAccessor(Accessor<Object> origAccessor,
				CompareService<? extends CompareInfo> compareService, String columnName) {
			super(origAccessor, compareService);
			_columnName = columnName;
		}

		@Override
		public Object getValue(CompareRowObject object, String property) {
			return super.getValue(object, _columnName);
		}
	}

	private static class TablePartCellExistenceTester implements CellExistenceTester {

		private CellExistenceTester _configuredCellExistenceTester;

		private String _columnName;
		private boolean _useBaseValue;

		public TablePartCellExistenceTester(CellExistenceTester configuredCellExistenceTester, boolean useBaseValue,
				String columnName) {
			_configuredCellExistenceTester = configuredCellExistenceTester;
			_useBaseValue = useBaseValue;
			_columnName = columnName;
		}

		@Override
		public boolean isCellExistent(Object rowObject, String columnName) {
			Object compareValue = getCompareValue((CompareRowObject) rowObject);
			if (compareValue != null) {
				return _configuredCellExistenceTester.isCellExistent(compareValue, _columnName);
			} else {
				return true;
			}
		}

		private Object getCompareValue(CompareRowObject rowObject) {
			if (rowObject == null) {
				return null;
			}
			if (_useBaseValue) {
				return rowObject.baseValue();
			} else {
				return rowObject.changeValue();
			}
		}

	}

	private static class TablePartMapping implements Mapping<CompareInfo, Object> {

		private boolean _useBaseValue;

		private Mapping<Object, ?> _configuredSortKeyProvider;

		public TablePartMapping(Mapping<Object, ?> configuredSortKeyProvider, boolean useBaseValue) {
			_configuredSortKeyProvider = configuredSortKeyProvider;
			_useBaseValue = useBaseValue;
		}

		@Override
		public Object map(CompareInfo input) {
			return _configuredSortKeyProvider.map(getDisplayedValue(input));
		}

		private Object getDisplayedValue(CompareInfo input) {
			if (_useBaseValue) {
				return input.getBaseValue();
			} else {
				return input.getChangeValue();
			}
		}

	}
}
