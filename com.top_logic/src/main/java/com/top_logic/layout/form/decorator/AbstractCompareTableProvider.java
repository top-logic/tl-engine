/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnContainer;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.renderer.UniformCellRenderer;
import com.top_logic.layout.table.tree.TreeTableCellTester;
import com.top_logic.layout.tree.renderer.RowTypeCellRenderer;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.ExcelExportSupport;

/**
 * {@link TableConfigurationProvider} for the comparison table.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCompareTableProvider<CI extends CompareInfo> extends NoDefaultColumnAdaption {

	private final CompareService<CI> _compareService;

	private boolean _isTreeTable;

	private ExcelExportSupport _excelExport;

	/**
	 * Creates a new {@link AbstractCompareTableProvider}.
	 * 
	 * @param compareService
	 *        {@link CompareService} used to decorate changed cells.
	 * @param excelExport
	 *        {@link ExcelExportSupport} to get default {@link ExcelCellRenderer}:
	 *        {@link ExcelExportSupport#defaultExcelCellRenderer()}.
	 * @param isTreeTable
	 *        Whether the displayed table is a tree table.
	 */
	public AbstractCompareTableProvider(CompareService<CI> compareService, ExcelExportSupport excelExport,
			boolean isTreeTable) {
		_compareService = compareService;
		_excelExport = excelExport;
		_isTreeTable = isTreeTable;
	}

	/**
	 * Copy all global table settings from source configuration to target configuration.
	 */
	protected final void copyGlobalTableProperties(TableConfiguration target, TableConfiguration source) {
		source.copyGlobalSettingsTo(target);
	}

	/**
	 * Copy all declared columns from source configuration to target configuration.
	 */
	protected final void copyColumns(ColumnContainer<? extends ColumnConfiguration> target,
			ColumnContainer<? extends ColumnConfiguration> source) {
		Collection<? extends ColumnConfiguration> declaredColumns = source.getDeclaredColumns();
		for (ColumnConfiguration sourceColumn : declaredColumns) {
			ColumnConfiguration targetColumn = createTargetColumn(target, sourceColumn);
			if (targetColumn != null) {
				sourceColumn.copyTo(targetColumn);
				adaptTargetColumn(targetColumn);
				copyColumns(targetColumn, sourceColumn);
			}
		}
	}

	/**
	 * target {@link ColumnConfiguration}, declared in given target column container, based
	 *         on given source {@link ColumnConfiguration}.
	 */
	protected abstract ColumnConfiguration createTargetColumn(ColumnContainer<? extends ColumnConfiguration> target,
			ColumnConfiguration sourceColumn);

	/**
	 * Adapts {@link ColumnConfiguration} properties to compare table.
	 */
	@SuppressWarnings("unchecked")
	protected final void adaptTargetColumn(ColumnConfiguration targetColumn) {
		adaptPreloadContribution(targetColumn);
		adaptCellExistenceTester(targetColumn);
		adaptSortKeyProvider(targetColumn);
		Accessor<Object> accessor = targetColumn.getAccessor();
		if (accessor == null) {
			/* If the column does not have an Accessor, it must not be adapted. Moreover a standard
			 * CompareColumnCellRenderer can not be used, because it renders a change state
			 * depending on the cell values. As there is no accessor, no access to cell values are
			 * possible. Such a cell is always treaded as unchanged. */
			adaptSimpleCellRender(targetColumn);
		} else {
			adaptAccessor(targetColumn, accessor);
			adaptCellRenderer(targetColumn);
		}
		adaptExcelCellRenderer(targetColumn);
	}

	private void adaptCellExistenceTester(ColumnConfiguration targetColumn) {
		if(isTreeTable()) {
			targetColumn
				.setCellExistenceTester(new TreeTableCellTester(getCompareRowCellExistenceTester(targetColumn)));
		} else {
			targetColumn.setCellExistenceTester(getCompareRowCellExistenceTester(targetColumn));
		}
	}

	/**
	 * {@link CellExistenceTester} for {@link CompareRowObject}
	 */
	protected abstract CellExistenceTester getCompareRowCellExistenceTester(
			ColumnConfiguration targetColumn);

	/** 
	 * Adapts the configured column accessor to compare row object.
	 * 
	 * @param targetColumn - column, where the custom accessor has been defined.
	 * @param accessor - custom accessor of given column.
	 */
	protected abstract void adaptAccessor(ColumnConfiguration targetColumn, Accessor<Object> accessor);

	private void adaptExcelCellRenderer(ColumnConfiguration column) {
		if (column.isClassifiedBy(ColumnConfig.CLASSIFIER_NO_EXPORT)) {
			return;
		}
		ExcelCellRenderer excelCellRenderer = column.finalExcelCellRenderer(_excelExport);
		column.setExcelRenderer(new CompareExcelRenderer(excelCellRenderer, _isTreeTable));
	}

	/**
	 * Create a compare cell renderer for columns without accessor.
	 */
	protected abstract void adaptSimpleCellRender(ColumnConfiguration column);

	private void adaptCellRenderer(ColumnConfiguration column) {
		CellRenderer finalCellRenderer = column.finalCellRenderer();
		CellRenderer cellRenderer;
		if (finalCellRenderer instanceof TreeCellRenderer) {
			cellRenderer = createCompareTreeCellRenderer(column);
		} else if (finalCellRenderer instanceof RowTypeCellRenderer) {
			cellRenderer = createCompareRowTypeCellRenderer(column);
		} else {
			if (finalCellRenderer instanceof UniformCellRenderer) {
				/* UniformCellRenderer checks whether the cell exists. This is already checked.
				 * Moreover the "finalCellRenderer" gets a wrapped Cell to write which is not
				 * compatible to the wrapped CellExistenceTester! */
				Renderer<Object> finalRenderer = ((UniformCellRenderer) finalCellRenderer).getRenderer();
				finalCellRenderer = new AbstractCellRenderer() {

					@Override
					public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
						finalRenderer.write(context, out, cell.getValue());
					}
				};
			}
			cellRenderer = createCompareCellRenderer(finalCellRenderer);
		}
		column.setCellRenderer(cellRenderer);
	}

	/**
	 * {@link CompareRowTypeCellRenderer} of row type compare table cells.
	 */
	protected abstract CellRenderer createCompareRowTypeCellRenderer(ColumnConfiguration column);

	/**
	 * {@link CompareTreeCellRenderer} of tree compare table cells.
	 */
	protected abstract CellRenderer createCompareTreeCellRenderer(ColumnConfiguration column);

	/**
	 * {@link CompareCellRenderer} of regular compare table cells.
	 */
	protected abstract CompareCellRenderer<CI> createCompareCellRenderer(CellRenderer finalCellRenderer);
	
	/**
	 * Adapts the configured sort key provider, to sort or filter {@link CompareInfo}s.
	 */
	protected abstract void adaptSortKeyProvider(ColumnConfiguration targetColumn);

	private void adaptPreloadContribution(ColumnConfiguration targetColumn) {
		final PreloadContribution preloadContribution = targetColumn.getPreloadContribution();
		if (preloadContribution != null) {
			// original table contains Wrapper as row object, this table doesn't. Therefore the
			// original preload leads to errors.
			targetColumn.setPreloadContribution(null);
		}
	}

	/**
	 * true, if a tree table shall be shown, false otherwise.
	 */
	protected boolean isTreeTable() {
		return _isTreeTable;
	}

	/**
	 * compare service, which uses this {@link AbstractCompareTableProvider}.
	 */
	protected CompareService<CI> getCompareService() {
		return _compareService;
	}
}

