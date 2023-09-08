/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.DefaultCellRenderer;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnContainer;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.tree.TreeTableAccessor;
import com.top_logic.layout.tree.renderer.NoResourceProvider;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.ExcelExportSupport;

/**
 * {@link TableConfigurationProvider} for comparison table, whereby differences will be shown as
 * overlay.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class OverlayCompareTableProvider<CI extends CompareInfo> extends AbstractCompareTableProvider<CI> {

	private final TableConfiguration _origTable;

	private TableConfigurationProvider _decoratorProvider;

	/**
	 * Creates a new {@link OverlayCompareTableProvider}.
	 * 
	 * @param compareService
	 *        {@link CompareService} used to decorate changed cells.
	 * @param origTable
	 *        {@link TableConfigurationProvider} defining the original table.
	 * @param decorationColumn
	 *        {@link TableConfigurationProvider} providing additional modification, e.g. like adding
	 *        a column for the {@link CompareInfo}.
	 * @param excelExport
	 *        {@link ExcelExportSupport} to get default {@link ExcelCellRenderer}:
	 *        {@link ExcelExportSupport#defaultExcelCellRenderer()}.
	 * @param isTreeTable
	 *        Whether the displayed table is a tree table.
	 */
	public OverlayCompareTableProvider(CompareService<CI> compareService, TableConfigurationProvider origTable,
			TableConfigurationProvider decorationColumn, ExcelExportSupport excelExport, boolean isTreeTable) {
		this(compareService, TableConfigurationFactory.build(origTable), decorationColumn, excelExport, isTreeTable);
	}

	/**
	 * Creates a new {@link OverlayCompareTableProvider}.
	 * 
	 * @param compareService
	 *        {@link CompareService} used to decorate changed cells.
	 * @param origTable
	 *        {@link TableConfiguration} defining the original table.
	 * @param decorationColumn
	 *        {@link TableConfigurationProvider} providing additional modification, e.g. like adding
	 *        a column for the {@link CompareInfo}.
	 * @param excelExport
	 *        {@link ExcelExportSupport} to get default {@link ExcelCellRenderer}:
	 *        {@link ExcelExportSupport#defaultExcelCellRenderer()}.
	 * @param isTreeTable
	 *        Whether the displayed table is a tree table.
	 */
	public OverlayCompareTableProvider(CompareService<CI> compareService, TableConfiguration origTable,
			TableConfigurationProvider decorationColumn, ExcelExportSupport excelExport, boolean isTreeTable) {
		super(compareService, excelExport, isTreeTable);
		_origTable = origTable;
		_decoratorProvider = decorationColumn;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		copyTable(table, _origTable);

		_decoratorProvider.adaptConfigurationTo(table);
	}

	private final void copyTable(TableConfiguration target, TableConfiguration source) {
		copyGlobalTableProperties(target, source);
		copyColumns(target, source);
	}

	@Override
	protected ColumnConfiguration createTargetColumn(ColumnContainer<? extends ColumnConfiguration> target,
			ColumnConfiguration sourceColumn) {
		return target.declareColumn(sourceColumn.getName());
	}

	@Override
	protected CellRenderer createCompareTreeCellRenderer(ColumnConfiguration column) {
		ResourceProvider resourceProvider;
		String idColumn = _origTable.getIDColumn();
		if (!column.getName().equals(idColumn)) {
			resourceProvider = MetaResourceProvider.INSTANCE;
		} else {
			resourceProvider = NoResourceProvider.INSTANCE;
		}
		return new CompareTreeCellRenderer(resourceProvider, DefaultCellRenderer.INSTANCE,
			CompareTreeCellRenderer.DEFAULT_INDENT_CHARS).init(getCompareService());
	}

	@Override
	protected CellRenderer createCompareRowTypeCellRenderer(ColumnConfiguration column) {
		ResourceProvider resourceProvider;
		String idColumn = _origTable.getIDColumn();
		if (!column.getName().equals(idColumn)) {
			resourceProvider = MetaResourceProvider.INSTANCE;
		} else {
			resourceProvider = NoResourceProvider.INSTANCE;
		}
		return new CompareRowTypeCellRenderer(resourceProvider, DefaultCellRenderer.INSTANCE).init(getCompareService());
	}

	@Override
	protected void adaptSimpleCellRender(ColumnConfiguration column) {
		CellRenderer finalCellRenderer = column.finalCellRenderer();
		column.setCellRenderer(new SimpleCompareCellRenderer(finalCellRenderer, isTreeTable()));
	}

	@Override
	protected CompareCellRenderer<CI> createCompareCellRenderer(CellRenderer finalCellRenderer) {
		return new CompareCellRenderer<>(finalCellRenderer, getCompareService(), isTreeTable());
	}

	@Override
	protected void adaptAccessor(ColumnConfiguration column, Accessor<Object> columnAccessor) {
		Accessor<? extends Object> wrappedAccessor = new CompareTableAccessor(columnAccessor, getCompareService());
		if (isTreeTable()) {
			wrappedAccessor = new TreeTableAccessor(wrappedAccessor);
		}
		column.setAccessor(wrappedAccessor);
	}

	@Override
	protected CellExistenceTester getCompareRowCellExistenceTester(
			ColumnConfiguration targetColumn) {
		final CellExistenceTester configuredCellExistenceTester = targetColumn.getCellExistenceTester();
		return new CellExistenceTester() {

			@Override
			public boolean isCellExistent(Object rowObject, String columnName) {
				Object compareValue = getCompareValue((CompareRowObject) rowObject);
				if (compareValue != null) {
					return configuredCellExistenceTester.isCellExistent(compareValue, columnName);
				} else {
					return true;
				}
			}

			private Object getCompareValue(CompareRowObject rowObject) {
				if (rowObject == null) {
					return null;
				}
				if (rowObject.changeValue() != null) {
					return rowObject.changeValue();
				} else {
					return rowObject.baseValue();
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void adaptSortKeyProvider(ColumnConfiguration targetColumn) {
		final Mapping<Object, Object> sortKeyProvider = targetColumn.getSortKeyProvider();
		if (sortKeyProvider != null) {
			targetColumn.setSortKeyProvider(new Mapping<CompareInfo, Object>() {

				@Override
				public Object map(CompareInfo input) {
					return sortKeyProvider.map(getDisplayedObject(input));
				}

				private Object getDisplayedObject(CompareInfo input) {
					if (input == null) {
						return null;
					}
					return input.getDisplayedObject();
				}

			});
		}
	}
}
