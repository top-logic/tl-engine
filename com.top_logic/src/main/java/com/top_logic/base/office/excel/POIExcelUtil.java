/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.AreaEval;
import org.apache.poi.ss.formula.eval.BlankEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.NumericValueEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.function.FunctionMetadata;
import org.apache.poi.ss.formula.function.FunctionMetadataRegistry;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;

import com.top_logic.base.office.excel.POIExcelTemplate.POITemplateEntry;
import com.top_logic.base.office.excel.handler.BinaryDataPOIType;
import com.top_logic.base.office.excel.handler.DataAccessProxyPOIType;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.renderer.TableDeclaration;

/**
 * This class is a simple collection of utility methods for POI excel access.
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public class POIExcelUtil {

	/**
	 * The maximum number of characters allowed for sheet names.
	 */
	public static final int MAX_SHEET_NAME_LENGTH = 31;

	/**
	 * The regular expression to be used for sheet name normalization.
	 */
	public static final String NORMALIZER = "\\\\|\\/|\\:|\\*|\\?|\\[|\\]|\\.|\\n|\\r|\\t";

	/**
	 * The character to be used for replacing invalid characters in a sheet name.
	 */
	public static final String NORMALIZE_REPLACER = "-";

	/**
	 * Static initialization code for POI 3.8 throwing an Exception when evaluating the Sumproduct
	 * function.
	 * 
	 * @deprecated Remove this code along with the TLSumProduct class upon migration to POI 3.9 or
	 *             later.
	 */
	static {
		try {
			// call this evaluation code once in order to fill the internal array.
			FunctionEval.getNotSupportedFunctionNames();

			// Evil hack to fix problem in POI excel functions.
			final FunctionMetadata metaData = FunctionMetadataRegistry.getFunctionByName("SUMPRODUCT");
			final Field funcField = FunctionEval.class.getDeclaredField("functions");

			if (funcField != null) {
				final boolean accessability = funcField.isAccessible();

				funcField.setAccessible(true);
				final Function[] functions = (Function[]) funcField.get(FunctionEval.class);

				functions[metaData.getIndex()] = new TLSumProduct();
				funcField.setAccessible(accessability);
			}
		} catch (Throwable ex) {
			Logger.error("Failed to adapt POI functions.", ex, POIExcelUtil.class);
		}
	}

	/**
	 * Computes the logical (zero-based) column index for the specified cell.
	 * 
	 * <p>
	 * The algorithm goes through all header cells (which are contained in the interval
	 * [aBounds.getFirstColumn(), aBounds.getLastColumn()]) considering potentially merged cells as
	 * well.
	 * </p>
	 * 
	 * @param cell
	 *        the cell to compute the logical column index for
	 * @param bounds
	 *        the {@link POITemplateEntry} defining the excel table
	 * @return the logical (zero-based) column index for the specified cell or -1 if the specified
	 *         cell does not belong to any of the specified table's columns
	 */
	public static int getLogicalColumnIndex(final Cell cell, final POITemplateEntry bounds) {
		final int cellColumn = cell.getColumnIndex();
		final int startColumn = bounds.getCell().getColumnIndex();
		final int headerRow = bounds.getCell().getRowIndex();
		final int columnCount = bounds.getInteger(POITemplateEntry.ATTRIBUTE_COLS, 1);

		// cache the sheet and the header row index for convenience
		final Sheet sheet = cell.getSheet();

		// now we have to go through all ranges within the specified bounds
		// and resolve the actual column index of the specified cell.
		int index = 0;
		for (int currentColumn = startColumn; index < columnCount; currentColumn++, index++) {

			// NOTE: we have to resolve the range for the HEADER row, NOT the
			// row the specified cell actually belongs to!
			final CellRangeAddress range = getRange(sheet, headerRow, currentColumn);

			if (range != null) {
				// the specified cell's column is within the found range!
				// we've found the logical column!
				if (range.isInRange(headerRow, cellColumn)) {
					return index;
				}

				// crap...we have to advance further
				else {
					currentColumn += range.getLastColumn() - range.getFirstColumn();
				}
			}

			// there is no range for the current cell, so check
			// if it matches the specified column's index
			else {
				if (cellColumn == currentColumn) {
					return index;
				}
			}
		}

		return -1;
	}

	/**
	 * Computes the logical (zero-based) row index for the specified cell.
	 * 
	 * <p>
	 * The algorithm goes through all rows (which are contained in the interval
	 * [aBounds.getFirstRow(), aBounds.getLastRow()]) considering potentially merged cells as well.
	 * </p>
	 * 
	 * @param cell
	 *        the {@link Cell} to compute the logical row index for
	 * @param bounds
	 *        the {@link POITemplateEntry} defining the excel table
	 * @return the logical (zero-based) row index for the specified cell or -1 if the specified cell
	 *         does not belong to any of the specified table's rows
	 */
	public static int getLogicalRowIndex(final Cell cell, final POITemplateEntry bounds) {
		final int cellRow = cell.getRowIndex();
		final int cellColumn = cell.getColumnIndex();
		final int startRow = bounds.getCell().getRowIndex();
		final int rowCount = bounds.getInteger(POITemplateEntry.ATTRIBUTE_ROWS, 1);

		// cache the sheet and the header row index for convenience
		final Sheet sheet = cell.getSheet();

		// now we have to go through all ranges within the specified bounds
		// and resolve the actual column index of the specified cell.
		int index = 0;
		for (int currentRow = startRow; index < rowCount; currentRow++, index++) {

			// resolve the range for the cell itself
			final CellRangeAddress range = getRange(sheet, currentRow, cellColumn);

			if (range != null) {
				// the specified cell's row is within the found range!
				// we've found the logical row!
				if (range.isInRange(cellRow, cellColumn)) {
					return index;
				}

				// crap...we have to advance further
				else {
					currentRow += range.getLastRow() - range.getFirstRow();
				}
			}

			// there is no range for the current cell, so check
			// if it matches the specified column's index
			else {
				if (cellRow == currentRow) {
					return index;
				}
			}
		}

		return -1;
	}

	/**
	 * Creates a new cell in the specified column of the specified row if it does not exist. If a
	 * valid style is provided, the new cell is automagically assigned the specified style.
	 * 
	 * @param row
	 *        the {@link Row} to create a new cell for
	 * @param column
	 *        the index of the column to create the cell at
	 * @param style
	 *        the {@link CellStyle} to be set for the newly created cell or {@code null} to preserve
	 *        the default style
	 * @param inheritWidth
	 *        {@code true} to inherit the width of the <strong>logical</strong> column before this
	 *        one, {@code false} to use the default width. However, merging is not performed!
	 * @return the newly created cell
	 */
	public static Cell createCell(final Row row, final int column, final CellStyle style, final boolean inheritWidth) {
		Cell cell = row.getCell(column);
		if (cell == null) {
			cell = row.createCell(column);
		}

		// inherit the width of the previous logical column.
		// In case there was no entry, we will create one.
		// In case the previous logical column was a merged one,
		// we will inherit its total width but not the merge behavior.
		if (inheritWidth) {
			final Sheet sheet = row.getSheet();
			final Cell prevCell = row.getCell(column - 1, MissingCellPolicy.CREATE_NULL_AS_BLANK);

			final int width = computeWidth(prevCell);
			sheet.setColumnWidth(column, width);
		}

		if (style != null) {
			cell.setCellStyle(style);
		}

		return cell;
	}

	/**
	 * Compute the width for the specified cell.
	 * 
	 * @param cell
	 *        the {@link Cell} to compute the width for
	 * @return the computed width
	 */
	public static int computeWidth(final Cell cell) {
		final Sheet sheet = cell.getSheet();

		int width = 0;

		// compute the width of the merged region (if any)
		final CellRangeAddress range = POIExcelUtil.getRange(cell);
		if (range != null) {
			for (int i = range.getFirstColumn(); i <= range.getLastColumn(); i++) {
				width += sheet.getColumnWidth(i);
			}
		}

		// the cell is not part of any merged regions
		else {
			width = sheet.getColumnWidth(cell.getColumnIndex());
		}

		return width;
	}

	/**
	 * Compute the height for the specified logical cell. If the specified cell is part of a merged
	 * region, the region's height is considered.
	 * 
	 * @param cell
	 *        the {@link Cell} to compute the height for
	 * @return the computed height
	 */
	public static int computeHeight(final Cell cell) {
		final Sheet sheet = cell.getSheet();

		int height = 0;

		// compute the height of the merged region (if any)
		final CellRangeAddress range = POIExcelUtil.getRange(cell);
		if (range != null) {
			for (int i = range.getFirstRow(); i <= range.getLastRow(); i++) {
				height += sheet.getRow(i).getHeight();
			}
		}

		// the cell is not part of any merged regions
		else {
			height = cell.getRow().getHeight();
		}

		return height;
	}

	/**
	 * Creates a new cell at the specified location in the specified sheet. If a valid style is
	 * provided, the new cell is automagically assigned the specified style.
	 * 
	 * <p>
	 * If a cell already at the specified location already existed only the cell's style will be
	 * adjusted
	 * </p>
	 * 
	 * @param sheet
	 *        the {@link Sheet} to create a new cell in
	 * @param rowIndex
	 *        the row index of the cell to create
	 * @param columnIndex
	 *        the column index of the cell to create
	 * @param style
	 *        the {@link CellStyle} to be set for the newly created cell or {@code null} to preserve
	 *        the default style
	 * @param inheritWidth
	 *        {@code true} to inherit the width of the column before this one, {@code false} to use
	 *        the default width
	 * @return the newly created cell
	 */
	public static Cell createCell(final Sheet sheet, final int rowIndex, final int columnIndex, final CellStyle style,
			final boolean inheritWidth) {
		Row row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}

		return createCell(row, columnIndex, style, inheritWidth);
	}

	/**
	 * Create a new row at the specified location.
	 * 
	 * <p>
	 * If a row at the specified location already exists, it will be returned.
	 * </p>
	 * 
	 * @param sheet
	 *        the {@link Sheet} to create a new row for
	 * @param rowIndex
	 *        the zero-based index of the row to be created
	 * @return the newly created row at the specified index
	 */
	public static Row createRow(final Sheet sheet, final int rowIndex) {
		Row row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}

		return row;
	}

	/**
	 * Exports the given image.
	 */
	public static void exportImage(BinaryData image, POITemplateEntry template, POIDrawingManager drawingMgr) {
		Cell cell = template.getCell();
		Workbook workbook = cell.getSheet().getWorkbook();
		POITypeSupporter support = POIExcelUtil.getSupport(workbook, template, drawingMgr);
		new BinaryDataPOIType().setValue(cell, workbook, image, support);
	}

	/**
	 * Exports the given image.
	 */
	public static void exportImage(DataAccessProxy image, POITemplateEntry template, POIDrawingManager drawingMgr) {
		Cell cell = template.getCell();
		Workbook workbook = cell.getSheet().getWorkbook();
		POITypeSupporter support = POIExcelUtil.getSupport(workbook, template, drawingMgr);
		new DataAccessProxyPOIType().setValue(cell, workbook, image, support);
	}

	private static POITypeSupporter getSupport(final Workbook workbook, final POITemplateEntry template,

			final POIDrawingManager drawingMgr) {
		return new POITypeSupporter() {

			private CellStyle _dateStyle;

			@Override
			public POITemplateEntry getTemplate(Cell aCell) {
				return template;
			}

			@Override
			public POIDrawingManager getDrawingManager() {
				return drawingMgr;
			}

			@Override
			public CellStyle getDateStyle() {
				if (_dateStyle == null) {
					_dateStyle = POIExcelValueSetter.createDateStyle(workbook);
				}

				return _dateStyle;
			}
		};
	}

	/**
	 * Export the specified table model (omitting the specified columns) to the location defined by
	 * the specified template entry.
	 * 
	 * <p>
	 * Same as
	 * {@link #exportTable(TableModel, Set, POITemplateEntry, POICellStyleProvider, POICellValueProvider, POICellStyleProvider, POICellValueProvider, int)}
	 * but uses the same style and value provider for rows and header (in case a header has to be
	 * exported).
	 * </p>
	 * 
	 * @param table
	 *        the {@link TableModel} to export
	 * @param ignoredColumnNames
	 *        a (possibly empty) {@link Set} column names to be ignored
	 * @param template
	 *        the {@link POITemplateEntry} being the table declaration to be used for proper export
	 * @param styleProvider
	 *        the {@link POICellStyleProvider} to be used for retrieving the appropriate style for a
	 *        cell
	 * @param valueProvider
	 *        the {@link POICellValueProvider} which will convert the table model values to the
	 *        appropriate excel cell values
	 * @param maxColumnsPerRow
	 *        max number of columns to export in one row. If more columns have to be exported the
	 *        table will be split into chunks of maxColumnsPerRow columns that are exported below
	 *        each other
	 * @return the number of rows actually written
	 */
	public static int exportTable(final TableModel table, final Set<String> ignoredColumnNames,
			final POITemplateEntry template, final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider, final int maxColumnsPerRow) {
		return exportTable(table, ignoredColumnNames, template, styleProvider, valueProvider, styleProvider,
			valueProvider, maxColumnsPerRow);
	}

	/**
	 * Export the specified table model (omitting the specified columns) to the location defined by
	 * the specified template entry.
	 * 
	 * @param table
	 *        the {@link TableModel} to export
	 * @param ignoredColumnNames
	 *        a (possibly empty) {@link Set} of column names to be ignored
	 * @param template
	 *        the {@link POITemplateEntry} being the table declaration to be used for proper export
	 * @param styleProvider
	 *        the {@link POICellStyleProvider} to be used for retrieving the appropriate style for a
	 *        cell
	 * @param valueProvider
	 *        the {@link POICellValueProvider} which will convert the table model values to the
	 *        appropriate excel cell values
	 * @param headerStyleProvider
	 *        the {@link POICellStyleProvider} to be used for retrieving the appropriate style for a
	 *        <strong>header</strong> cell
	 * @param headerValueProvider
	 *        the {@link POICellValueProvider} which will convert the column (header) names.
	 * @param maxColumnsPerRow
	 *        max number of columns to export in one row. If more columns have to be exported the
	 *        table will be split into chunks of maxCol columns that are exported below each other
	 * @return the number of rows actually written
	 */
	public static int exportTable(final TableModel table, final Set<String> ignoredColumnNames,
			final POITemplateEntry template,
			final POICellStyleProvider styleProvider, final POICellValueProvider valueProvider,
			final POICellStyleProvider headerStyleProvider, final POICellValueProvider headerValueProvider,
			final int maxColumnsPerRow) {
		// extract the appropriate table declaration from the table model

		// retrieve all columns and remove all ignored ones.
		final List<String> allColumnNames = table.getColumnNames();
		final List<String> columns = new ArrayList<>(allColumnNames);
		columns.removeAll(ignoredColumnNames);

		// Remove the columns which are not allowed to be exported
		for (final String columnName : allColumnNames) {
			final ColumnConfiguration columnDescription = table.getColumnDescription(columnName);
			if (columnDescription.isClassifiedBy(ColumnConfig.CLASSIFIER_NO_EXPORT)) {
				columns.remove(columnName);
			}
		}

		// extract the cell since we will use it often
		final Cell cell = template.getCell();
		final Sheet sheet = cell.getSheet();
		final Row headerTemplateRow = sheet.getRow(cell.getRowIndex());

		final int colNo = columns.size();
		final int columnsPerRow = maxColumnsPerRow <= 0 ? colNo : maxColumnsPerRow;
		final int numOfLines = (colNo + columnsPerRow - 1) / columnsPerRow;

		// this will result in a nice double for-loop
		final int offset = template.getInteger(POITemplateEntry.ATTRIBUTE_OFFSET, 0);
		final Row cellTemplateRow = sheet.getRow(cell.getRowIndex() + offset + 1);
		final int rowCount = table.getRowCount();

		// retrieve the object accessor and cache it.
		final Accessor<?> accessor = getAccessor(table);

		// go through all rows and write each
		int currentRow = cell.getRowIndex();

		int currColNo = 0;

		for (int lineNo = 0; lineNo < numOfLines; lineNo++) {
			// export the header only if necessary!
			final boolean showHeader = template.getBoolean(POITemplateEntry.ATTRIBUTE_HEADER, true);
			if (showHeader) {

				// extend the table if and only if we actually need it.
				if (lineNo > 0) {
					// insert a new row
					final Row insertRow = insertRow(sheet, currentRow, template);
					if (headerTemplateRow != null) {
						insertRow.setHeight(headerTemplateRow.getHeight());
					}
				}

				exportHeader(sheet.getRow(currentRow), columns, template, headerStyleProvider,
					headerValueProvider, currColNo, currColNo + columnsPerRow - 1);
				currentRow++;
			}

			if (lineNo == 0) {
				// go through all rows and write each
				currentRow = cell.getRowIndex() + offset + 1;
			}
			for (int i = 0; i < rowCount; i++, currentRow++) {

				// extend the table if and only if we actually need it.
				if (needsInsert(template, currentRow)) {
					// insert a new row
					final Row insertRow = insertRow(sheet, currentRow, template);
					if (cellTemplateRow != null) {
						insertRow.setHeight(cellTemplateRow.getHeight());
					}
				}

				// retrieve the row from the sheet at the specified row location
				final Row row = sheet.getRow(currentRow);

				// get the row object to be exported
				final Object rowObject = getRowObjectOrIndex(table, i);

				// let the generic method write the actual row
				exportRow(row, rowObject, accessor, columns, template, styleProvider, valueProvider,
					currColNo, currColNo + columnsPerRow - 1);
			}

			currColNo += columnsPerRow;
		}

		// return the number of rows we've actually written.
		return currentRow - cell.getRowIndex();
	}

	/**
	 * Returns the row object at the given row index in the given table. If the given table model
	 * does not support row object access, this method simply returns the given index.
	 * 
	 * @param table
	 *        the {@link TableModel} to retrieve the row object from
	 * @param row
	 *        the zero based index of the row to retrieve the object at
	 * @return the row object at the given row
	 */
	private static Object getRowObjectOrIndex(final TableModel table, final int row) {
		if (table instanceof ObjectTableModel) {
			return ((ObjectTableModel) table).getRowObject(row);
		}

		return row;
	}

	/**
	 * Returns the row accessor to be used for accessing row objects in the given table model.
	 * 
	 * @param table
	 *        the table to retrieve the row accessor for
	 * @return the row {@link Accessor} for the given table model
	 */
	private static Accessor<?> getAccessor(final TableModel table) {
		if (table instanceof ObjectTableModel) {
			return new DispatchingObjectTableModellAccessor((ObjectTableModel) table);
		}

		return new TableRowAccessor(table);
	}

	/**
	 * @see #exportTree(TLTreeModel, List, Accessor, POITemplateEntry, POICellStyleProvider,
	 *      POICellValueProvider)
	 */
	public static int exportTree(final TLTreeModel<?> tree, final TableDeclaration declaration,
			final POITemplateEntry template, final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider) {
		return exportTree(tree, declaration.getColumnNames(), declaration.getAccessor(), template, styleProvider,
			valueProvider);
	}

	/**
	 * Export the specified tree data model using the specified template description.
	 * 
	 * @param tree
	 *        the business model to export
	 * @param columns
	 *        a (possibly empty) {@link List} of column names to be exported
	 * @param accessor
	 *        the {@link Accessor} to be used for resolving column values from row objects
	 * @param template
	 *        the template defining the tree area to export to
	 * @param styleProvider
	 *        the provider to be used for retrieving the appropriate style for a cell
	 * @param valueProvider
	 *        the value provider which will convert the table model values to the appropriate excel
	 *        cell values
	 * @return the index of the last row this method wrote to
	 */
	public static int exportTree(final TLTreeModel<?> tree, final List<String> columns, final Accessor<?> accessor,
			final POITemplateEntry template, final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider) {
		return exportTree(tree, columns, accessor, template, styleProvider, valueProvider,
			styleProvider, valueProvider);
	}

	/**
	 * Export the specified tree data model using the specified template description.
	 * 
	 * @param tree
	 *        the business model to export
	 * @param columns
	 *        the columns to be exported
	 * @param accessor
	 *        the appropriate property accessor
	 * @param template
	 *        the template defining the tree area to export to
	 * @param contentStyleProvider
	 *        the provider to be used for retrieving the appropriate style for a cell
	 * @param contentValueProvider
	 *        the value provider which will convert the table model values to the appropriate excel
	 *        cell values
	 * @param headerStyleProvider
	 *        the provider to be used for retrieving the appropriate style for a header's Cell
	 * @param headerValueProvider
	 *        the value provider which will convert the table model values to the appropriate
	 *        header's cell values
	 * @return the index of the last row this method wrote to
	 */
	public static int exportTree(final TLTreeModel<?> tree,
			final List<String> columns,
			final Accessor<?> accessor,
			final POITemplateEntry template,
			final POICellStyleProvider contentStyleProvider,
			final POICellValueProvider contentValueProvider,
			final POICellStyleProvider headerStyleProvider,
			final POICellValueProvider headerValueProvider) {

		// extract the cell since we will use it often
		final Cell cell = template.getCell();
		final Sheet sheet = cell.getSheet();

		// export the header only if necessary!
		final boolean showHeader = template.getBoolean(POITemplateEntry.ATTRIBUTE_HEADER, true);

		if (showHeader) {
			exportHeader(cell.getRow(), columns, template, headerStyleProvider, headerValueProvider);
		}

		// retrieve the row from the sheet at the specified row location
		final int offset = template.getInteger(POITemplateEntry.ATTRIBUTE_OFFSET, 0);
		final int startRowIndex = cell.getRowIndex() + offset + 1;
		final Row startRow = POIExcelUtil.createRow(sheet, startRowIndex);

		// resolve the property indicating if we have to export the root node as well.
		final boolean exportRoot = template.getBoolean(POITemplateEntry.ATTRIBUTE_ROOT, true);

		// now export the node itself using the resolved properties.
		return exportNode(startRow, tree, tree.getRoot(), 0, exportRoot, accessor, columns, template,
			contentStyleProvider, contentValueProvider);
	}

	/**
	 * Exports the specified node to the specified row using the specified property accessor.
	 * 
	 * @param row
	 *        the {@link Row} to export the specified node to
	 * @param tree
	 *        the {@link TLTreeModel} to be exported
	 * @param node
	 *        the node to be exported
	 * @param depth
	 *        the specified node's current depth
	 * @param exportRoot
	 *        {@code true} to write the specified node itself, {@code false} to skip it and write
	 *        only the children
	 * @param accessor
	 *        the {@link Accessor} to be used for accessing properties in the node
	 * @param columns
	 *        the {@link List} of column names to export the values for
	 * @param template
	 *        the {@link POITemplateEntry} defining the tree to export to
	 * @param styleProvider
	 *        the {@link POICellStyleProvider} to be used for retrieving the appropriate style for a
	 *        cell
	 * @param valueProvider
	 *        the {@link POICellValueProvider} which will convert the table model values to the
	 *        appropriate excel cell values
	 * @return the row index of the row this method returned at
	 */
	private static int exportNode(final Row row, final TLTreeModel tree, final Object node, final int depth,
			final boolean exportRoot, final Accessor<?> accessor, final List<String> columns,
			final POITemplateEntry template, final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider) {
		// this is index of the row we're going to start writing at.
		int currentRowIndex = row.getRowNum() - 1;

		// extract the template Cell
		final Cell templateCell = template.getCell();
		final Sheet templateSheet = templateCell.getSheet();
		final int offset = template.getInteger(POITemplateEntry.ATTRIBUTE_OFFSET, 0);
		final Row cellTemplateRow = templateSheet.getRow(templateCell.getRowIndex() + offset + 1);

		// write the specified row itself if and only if we're to do so
		if (exportRoot) {
			exportRow(row, node, accessor, columns, template, styleProvider, valueProvider);
			currentRowIndex++;
		}

		// compute the new depth
		final int childDepth = depth + 1;
		if (isMaxDepthReached(template, childDepth)) {
			return currentRowIndex;
		}

		// retrieve the top-level elements.
		final List<?> children = tree.getChildren(node);
		final int rowCount = children.size();

		// go through all rows and write each
		final Sheet sheet = row.getSheet();

		for (int i = 0; i < rowCount; i++) {

			// extend the table if and only if we actually need it.
			if (needsInsert(template, ++currentRowIndex)) {
				// insert a new row
				final Row insertedRow = insertRow(sheet, currentRowIndex, template);
				if (cellTemplateRow != null) {
					insertedRow.setHeight(cellTemplateRow.getHeight());
				}
			}

			// retrieve the row from the sheet at the specified row location
			final Row currentRow = sheet.getRow(currentRowIndex);

			// retrieve the child node and export it.
			final Object child = children.get(i);

			// let the generic method write the actual row
			currentRowIndex =
				exportNode(currentRow, tree, child, childDepth, true, accessor, columns, template, styleProvider,
					valueProvider);
		}

		// automagically create excel groups here!
		final boolean autoGroup = template.getBoolean(POITemplateEntry.ATTRIBUTE_AUTOGROUP, false);
		if (exportRoot && autoGroup) {
			final int grouptart = row.getRowNum() + 1;
			final int groupEnd = currentRowIndex;

			// make sure to create groups for valid entries only.
			if (grouptart <= groupEnd) {
				sheet.groupRow(grouptart, currentRowIndex);

				// now collapse the newly created group if necessary.
				final boolean autoCollapse = template.getBoolean(POITemplateEntry.ATTRIBUTE_AUTOCOLLAPSE, false);
				if (autoCollapse) {
					sheet.setRowGroupCollapsed(grouptart, true);
				}
			}
		}

		return currentRowIndex;
	}

	/**
	 * Checks if the specified depth exceeds the maximum depth defined in the specified template.
	 * 
	 * @param template
	 *        the {@link POITemplateEntry} defining the maximum depth
	 * @param depth
	 *        the depth to be checked
	 * @return {@code true} if the maximum depth has been reached, {@code false} otherwise
	 */
	protected static boolean isMaxDepthReached(final POITemplateEntry template, final int depth) {
		final int maxDepth =
			template.getInteger(POITemplateEntry.ATTRIBUTE_MAX_DEPTH, POITemplateEntry.INFINITE_DEPTH);
		if (maxDepth != POITemplateEntry.INFINITE_DEPTH) {
			return depth > maxDepth;
		}

		return false;
	}

	/**
	 * Check if we have to insert a new row in order to be able to write to the row at the specified
	 * index.
	 * 
	 * @param template
	 *        the {@link POITemplateEntry} defining the start row
	 * @param currentIndex
	 *        the index to be written to
	 * @return {@code true} if a new row has to be inserted before writing to the specified index,
	 *         {@code false} otherwise
	 */
	protected static boolean needsInsert(final POITemplateEntry template, final int currentIndex) {
		// retrieve the maximum number of rows
		final int maxRows = template.getInteger(POITemplateEntry.ATTRIBUTE_ROWS, 1);

		// retrieve the first row index
		final int start = template.getCell().getRowIndex();

		// we have to insert a new row if we have to exceed the maximum
		// number of rows defined by the specified template.
		return currentIndex >= (start + maxRows);
	}

	/**
	 * Retrieves the cell at the specified logical location in the specified sheet.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to retrieve the cell from
	 * @param rowIndex
	 *        the row index of the cell to be retrieved
	 * @param colIndex
	 *        the column index of the cell to be retrieved
	 * @return the cell at the specified location or {@code null} if no logical cell is located at
	 *         the specified location
	 */
	public static Cell getCell(final Sheet sheet, final int rowIndex, final int colIndex) {
		final Row row = sheet.getRow(rowIndex);
		if (row != null) {
			return row.getCell(colIndex);
		}

		return null;
	}

	/**
	 * Returns the cell in the specified workbook which the specified {@link CellReference} instance
	 * refers to. If the specified reference does not point to a specific sheet, the default (first)
	 * sheet is considered.
	 * 
	 * @param book
	 *        the {@link Workbook} to resolve the reference in
	 * @param ref
	 *        the {@link CellReference} to be resolved
	 * @return the cell in the specified workbook the specified reference points to or {@code null}
	 *         if it could not be resolved or the actual cell does not exist yet
	 */
	public static Cell getCell(final Workbook book, final CellReference ref) {
		final Sheet sheet;

		// this one might be null...
		if (ref.getSheetName() == null) {
			sheet = book.getSheetAt(0);
		} else {
			sheet = book.getSheet(ref.getSheetName());
		}

		// no sheet - not cell!
		if (sheet == null) {
			return null;
		}

		final Row row = sheet.getRow(ref.getRow());
		return row != null ? row.getCell(ref.getCol()) : null;
	}

	/**
	 * Computes the number of logical columns defined by the specified range address in the
	 * specified sheet. Logical columns respect the fact that some cells might be merged. Merged
	 * cells are considered to represent exactly ONE column.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to compute the column count for the specified range for
	 * @param bounds
	 *        the {@link CellRangeAddress} to compute the number of logical columns for
	 * @return the total number of logical columns in the specified range
	 */
	public static int getLogicalColumnCount(final Sheet sheet, final CellRangeAddress bounds) {
		final Row row = sheet.getRow(bounds.getFirstRow());

		// go through the range of columns defined by the range address
		// and resolve the appropriate merge region.
		int colCount = 0;
		for (int i = bounds.getFirstColumn(); i <= bounds.getLastColumn(); i++) {
			final Cell cell = row.getCell(i);
			final CellRangeAddress range = getRange(cell);
			if (range != null) {
				i += range.getLastColumn() - range.getFirstColumn();
			}

			colCount++;
		}

		return colCount;
	}

	/**
	 * Returns the cell range the specified cell is part of.
	 * 
	 * @param cell
	 *        the {@link Cell} to retrieve the cell range for
	 * @return the {@link CellRangeAddress} the specified cell is part of or {@code null} if the
	 *         specified cell is not part of any range
	 */
	public static CellRangeAddress getRange(final Cell cell) {
		return getRange(cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
	}

	/**
	 * Retrieves the range in the specified sheet a cell with the specified coordinates belongs to.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to retrieve the cell range from
	 * @param rowIndex
	 *        the row index of the cell to retrieve the range for
	 * @param colIndex
	 *        the column index of the cell to retrieve the range for
	 * @return the cell range for the specified cell coordinates or {@code null} the cell does not
	 *         belong to any range
	 */
	public static CellRangeAddress getRange(final Sheet sheet, final int rowIndex, final int colIndex) {
		final int regionCnt = sheet.getNumMergedRegions();

		// can't help it... we have to go through all regions...
		for (int i = 0; i < regionCnt; i++) {
			final CellRangeAddress range = sheet.getMergedRegion(i);
			if (range.isInRange(rowIndex, colIndex)) {
				return range;
			}
		}

		return null;
	}

	/**
	 * Inserts a new row at the specified index in the specified sheet. Potentially existing rows
	 * are moved down.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to insert a new row for
	 * @param rowIndex
	 *        the index to insert a row at
	 * @param poiTemplateEntry
	 *        the template
	 * @return the newly inserted row
	 */
	public static Row insertRow(final Sheet sheet, final int rowIndex, final POITemplateEntry poiTemplateEntry) {
		// we have to shift all existing rows starting at the specified row
		// exactly one row down (if the specified row is not the last one that is)
		// Make sure to copy the row height when shifting the rows!
		boolean doShiftRows = poiTemplateEntry.getBoolean(POITemplateEntry.ATTRIBUTE_SHIFT_ROWS, true);

		if (rowIndex <= sheet.getLastRowNum() && doShiftRows) {
			sheet.shiftRows(rowIndex, sheet.getLastRowNum(), 1, true, false);
		}

		return sheet.createRow(rowIndex);
	}

	/**
	 * Merges the cells of the specified row.
	 * 
	 * <p>
	 * <font color="red"><b>Warning: </b>If the given range intersects with an already existing one,
	 * a new merged region is NOT CREATED since it would produce a corrupt Excel file.</font>
	 * </p>
	 * 
	 * @param row
	 *        the {@link Row} to merge the cells for
	 * @param firstColumnIndex
	 *        the index of the first column to be merged
	 * @param lastColumnIndex
	 *        the index of the last column to be merged
	 * @return the index in the sheet's cell range table or -1 if the given range intersects with an
	 *         already existing merged region
	 * @see Sheet#getMergedRegion(int)
	 */
	public static int mergeColumns(final Row row, final int firstColumnIndex, final int lastColumnIndex) {
		if (!isPartOfMergedRegion(row.getSheet(), row.getRowNum(), row.getRowNum(), firstColumnIndex,
			lastColumnIndex)) {
			return row.getSheet().addMergedRegion(
				new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstColumnIndex, lastColumnIndex));
		} else {
			return -1;
		}
	}

	/**
	 * Checks if the given region intersects with an already existing merged region in the given
	 * {@link Sheet}.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to check the region intersection for
	 * @param rowStart
	 *        the start row (inclusive)
	 * @param rowEnd
	 *        the end row (inclusive)
	 * @param colStart
	 *        the start column (inclusive)
	 * @param colEnd
	 *        the end column (inclusive)
	 * @return {@code true} if the given range intersects with an already existing merged region,
	 *         {@code false} otherwise
	 */
	public static boolean isPartOfMergedRegion(final Sheet sheet, final int rowStart, final int rowEnd,
			final int colStart, final int colEnd) {
		final CellRangeAddress newRegion = new CellRangeAddress(rowStart, rowEnd, colStart, colEnd);

		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			final CellRangeAddress oldRegion = sheet.getMergedRegion(i);

			if (CellRangeUtil.NO_INTERSECTION != CellRangeUtil.intersect(newRegion, oldRegion)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Creates a comment for the specified cell with the specified contents.
	 * 
	 * @param cell
	 *        the {@link Cell} to create a comment for
	 * @param comment
	 *        the contents of the comment
	 * @param mgr
	 *        the {@link POIDrawingManager} to cache the values in
	 * @param colSpan
	 *        the number of columns defining the comment's width
	 * @param rowSpan
	 *        the number of rows defining the comment's height
	 * @return the created {@link Comment}
	 */
	public static Comment createCellComment(final Cell cell, final String comment, final POIDrawingManager mgr,
			final int colSpan, final int rowSpan) {
		final Sheet sheet = cell.getSheet();
		final Workbook book = sheet.getWorkbook();
		final Drawing drawing = mgr.getDrawing(sheet);
		final CreationHelper factory = book.getCreationHelper();

		// create the appropriate anchor and configure it.
		final ClientAnchor anchor = factory.createClientAnchor();
		anchor.setRow1(cell.getRowIndex());
		anchor.setCol1(cell.getColumnIndex());

		anchor.setRow2(cell.getRowIndex() + rowSpan);
		anchor.setCol2(cell.getColumnIndex() + colSpan);

		// create the comment itself first.
		final Comment cellComment = drawing.createCellComment(anchor);

		// now create the appropriate rich text string and set it as the comment's contents.
		cellComment.setString(factory.createRichTextString(comment));

		// now assign the comment to the specified cell.
		cell.setCellComment(cellComment);

		return cellComment;
	}

	/**
	 * Creates a comment for the specified cell with the specified contents.
	 * 
	 * @param cell
	 *        the {@link Cell} to create a comment for
	 * @param comment
	 *        the contents of the comment
	 * @param mgr
	 *        the {@link POIDrawingManager} to cache the values in
	 * @return the created comment
	 */
	public static Comment createCellComment(final Cell cell, final String comment, final POIDrawingManager mgr) {
		return POIExcelUtil.createCellComment(cell, comment, mgr, 1, 1);
	}

	/**
	 * Sets the style for the specified cell to be the specified instance. If the boolean flag is
	 * set to {@code true} the specified cell's merged region is determined and all cells in that
	 * region are assigned the same style as well.
	 * 
	 * <p>
	 * <strong>Note:</strong> if cells in the region have not been created yet, they will be created
	 * as blank cells.
	 * </p>
	 * 
	 * @param cell
	 *        the {@link Cell} to set the style for
	 * @param style
	 *        the {@link CellStyle} to be set or {@code null} to force the cell to used the default
	 *        one
	 * @param entireRange
	 *        {@code true} to set the same style for all cells in the merged region the specified
	 *        one belongs to, {@code false} to set the style only for the specified cell
	 */
	public static void setCellStyle(final Cell cell, final CellStyle style, final boolean entireRange) {
		// don't bother with merged regions...
		if (!entireRange) {
			cell.setCellStyle(style);
			return;
		}

		// the things got a bit ugly here...:)
		final CellRangeAddress range = getRange(cell);
		final Sheet sheet = cell.getSheet();

		// go through the entire merged region and set the style for all
		// cells.
		if (range != null) {
			final int startRow = range.getFirstRow();
			final int endRow = range.getLastRow();
			final int startCol = range.getFirstColumn();
			final int endCol = range.getLastColumn();

			for (int i = startRow; i <= endRow; i++) {
				for (int j = startCol; j <= endCol; j++) {
					createCell(sheet, i, j, style, false);
				}
			}
		}

		// oops, we're not part of any cell range...
		else {
			cell.setCellStyle(style);
		}
	}

	/**
	 * Sets the specified cell's value to be the specified object.
	 * 
	 * @deprecated use {@link POIExcelContext#value(Object)} instead
	 * 
	 * @param cell
	 *        the {@link Cell} to set the value for
	 * @param value
	 *        the value to be set or {@code null}
	 */
	@Deprecated
	public static void setCellValue(final Cell cell, final Object value) {
		final POIExcelContext context = new POIExcelContext(cell.getSheet());

		// position the new cursor at the given cell
		context.row(cell.getRowIndex());
		context.column(cell.getColumnIndex());

		context.value(value);
	}

	/**
	 * Sets the value of the specified template cell to be the specified value.
	 * 
	 * @param template
	 *        the {@link POITemplateEntry} cell to set the value for
	 * @param value
	 *        the value to be set for the specified template cell or {@code null}
	 */
	public static void setCellValue(final POITemplateEntry template, final Object value) {
		final Object newValue;

		// check if we have to replace the template only or
		// override the content of the entire cell
		if (template.getBoolean(POITemplateEntry.ATTRIBUTE_REPLACE, false)) {
			final Object templateValue = getCellValue(template.getCell());
			final String stringValue = templateValue != null ? String.valueOf(templateValue) : "";
			final String replacer = value != null ? String.valueOf(value) : "";

			// now replace the cell values
			newValue = POIExcelTemplate.replaceTemplate(stringValue, replacer);
		} else {
			newValue = value;
		}

		setCellValue(template.getCell(), newValue);
	}

	/**
	 * Retrieves the value of the specified cell.
	 * 
	 * @deprecated use {@link POIExcelContext#value()} instead.
	 * 
	 * @param cell
	 *        the {@link Cell} to retrieve the value for
	 * @return the specified cell's value or {@code null} if the cell was blank
	 */
	@Deprecated
	public static Object getCellValue(final Cell cell) {
		final POIExcelContext context = new POIExcelContext(cell.getSheet());

		try {
			// position the new cursor at the given cell
			context.row(cell.getRowIndex());
			context.column(cell.getColumnIndex());

			return context.value();
		} finally {
			context.close();
		}
	}

	/**
	 * Export the header of the specified model to the specified {@link Row} object.
	 * 
	 * @param row
	 *        the {@link Row} to export the header to
	 * @param columns
	 *        a {@link List} of column names to be exported
	 * @param table
	 *        the {@link POITemplateEntry} defining the table in the excel sheet
	 * @param styleProvider
	 *        the {@link POICellStyleProvider} to be used for to retrieve the cell styles
	 * @param valueProvider
	 *        the {@link POICellValueProvider} which will convert the table model values to the
	 *        appropriate excel cell values
	 */
	protected static void exportHeader(final Row row, final List<String> columns, final POITemplateEntry table,
			final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider) {
		exportHeader(row, columns, table, styleProvider, valueProvider, 0, columns.size() - 1);
	}

	/**
	 * Export the header of the specified model to the specified {@link Row} object.
	 * 
	 * @param row
	 *        the row to export the header to
	 * @param columns
	 *        a {@link List} of columns to export
	 * @param table
	 *        the {@link POITemplateEntry} defining the table in the excel sheet
	 * @param styleProvider
	 *        the {@link POICellStyleProvider} to be used for to retrieve the cell styles
	 * @param valueProvider
	 *        the {@link POICellValueProvider} which will convert the table model values to the
	 *        appropriate excel cell values
	 * @param startCol
	 *        the (zero based) index of the column in the given column list to start exporting
	 *        values at
	 * @param endCol
	 *        the (zero based) index of the column in the given column list to end exporting values
	 *        at
	 */
	protected static void exportHeader(final Row row, final List<String> columns, final POITemplateEntry table,
			final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider, final int startCol, final int endCol) {

		// compute the logical column count in order to decide later on
		// if we need to inherit the previous column count or not.
		final int minColCnt = table.getInteger(POITemplateEntry.ATTRIBUTE_COLS, 1);
		final int modelColCnt = columns.size() - startCol;
		final int maxColCnt = Math.max(minColCnt, endCol - startCol + 1);

		// the current PHYSICAL column index
		int curColPhysical = table.getCell().getColumnIndex();

		// the current LOGICAL column index
		int curColLocigal = 0;

		// now go through all columns and create an entry for each
		for (int i = 0; i < maxColCnt; i++) {
			// the column title to be set.
			final String title = (i < modelColCnt) ? columns.get(startCol + i) : null;

			// retrieve or create the necessary cell.
			// also, make sure to inherit the column width of the previous
			// cell if the new cell has to be created.
			final Cell cell = createCell(row, curColPhysical, null, curColLocigal >= minColCnt);

			// set the value for non-ignored columns only.
			valueProvider.setCellValue(cell, title);

			final Sheet sheet = row.getSheet();
			final Row templateRow = table.getCell().getRow();

			// set the appropriate style
			setCellStyle(cell, styleProvider.getCellStyle(cell, columns, title, title), true);

			// check if the cell is merged
			final Cell headerCell = getCell(sheet, table.getCell().getRowIndex(), curColPhysical);
			final CellRangeAddress range = getRange(headerCell);
			if (range != null) {
				int firstColumn = range.getFirstColumn();
				int lastColumn = range.getLastColumn();
				if (templateRow != row) {
					for (int col = firstColumn + 1; col <= lastColumn; col++) {
						setCellStyle(createCell(row, col, null, col >= minColCnt),
							styleProvider.getCellStyle(templateRow.getCell(col), columns, title, title), true);
					}
					mergeColumns(row, firstColumn, lastColumn);
				}
				curColPhysical += lastColumn - firstColumn;
			}

			// increment the column cursors
			curColPhysical++;
			curColLocigal++;
		}
	}

	/**
	 * Export the specified table model's row at the specified row index to the specified
	 * {@link Row} object.
	 * 
	 * @param row
	 *        the {@link Row} to export the values to
	 * @param rowObject
	 *        the row {@link Object} to be exported to the given row
	 * @param accessor
	 *        the {@link Accessor} to be used for resolving properties of the given row object
	 * @param columns
	 *        a {@link List} of columns to be exported
	 * @param table
	 *        the {@link POITemplateEntry} defining the table in the excel sheet
	 * @param styleProvider
	 *        the {@link POICellStyleProvider} to be used for to retrieve the cell styles
	 * @param valueProvider
	 *        the {@link POICellValueProvider} which will convert the table model values to the
	 *        appropriate excel cell values
	 */
	protected static void exportRow(final Row row, final Object rowObject, final Accessor<?> accessor,
			final List<String> columns, final POITemplateEntry table, final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider) {

		exportRow(row, rowObject, accessor, columns, table, styleProvider, valueProvider, 0, columns.size() - 1);
	}

	/**
	 * Export the specified table model's row at the specified row index to the specified
	 * {@link Row} object.
	 * 
	 * @param row
	 *        the {@link Row} to export the values to
	 * @param rowObject
	 *        the row {@link Object} to be exported to the given row
	 * @param accessor
	 *        the {@link Accessor} to be used for resolving properties of the given row object
	 * @param columns
	 *        a {@link List} of columns to be exported
	 * @param table
	 *        the {@link POITemplateEntry} defining the table in the excel sheet
	 * @param styleProvider
	 *        the {@link POICellStyleProvider} to be used for to retrieve the cell styles
	 * @param valueProvider
	 *        the {@link POICellValueProvider} which will convert the table model values to the
	 *        appropriate excel cell values
	 * @param startCol
	 *        the (zero based) index of the column in the given column list to start exporting
	 *        values at
	 * @param endCol
	 *        the (zero based) index of the column in the given column list to end exporting values
	 *        at
	 */
	protected static void exportRow(final Row row, final Object rowObject, final Accessor accessor,
			final List<String> columns, final POITemplateEntry table, final POICellStyleProvider styleProvider,
			final POICellValueProvider valueProvider, final int startCol, final int endCol) {

		// compute the logical column count in order to decide later on
		// if we need to inherit the previous column count or not.
		final int minColCnt = table.getInteger(POITemplateEntry.ATTRIBUTE_COLS, 1);
		final int modelColCnt = columns.size() - startCol;
		final int maxColCnt = Math.max(minColCnt, endCol - startCol + 1);

		// the current PHYSICAL column index
		int curColPhysical = table.getCell().getColumnIndex();

		// go through all columns and create an entry for each
		for (int i = 0; i < maxColCnt; i++) {

			// the cell value
			String property = null;
			Object value = null;

			// we have a valid column in the model, retrieve the value!
			if (i < modelColCnt) {
				// retrieve the value from the model
				property = String.valueOf(columns.get(i + startCol));
				value = accessor.getValue(rowObject, property);
			}

			// retrieve or create the necessary cell.
			// also, make sure to inherit the column width of the previous
			// cell if the new cell has to be created.
			final Cell cell = createCell(row, curColPhysical, null, false);

			// set the value for non-ignored columns only.
			valueProvider.setCellValue(cell, value);

			final Cell headerCell = getCell(row.getSheet(), table.getCell().getRowIndex(), curColPhysical);
			final CellRangeAddress range = (headerCell != null) ? getRange(headerCell) : null;
			if (range != null) {
				mergeColumns(row, range.getFirstColumn(), range.getLastColumn());
				curColPhysical += range.getLastColumn() - range.getFirstColumn();
			}

			// set the cell's style (for the entire range).
			setCellStyle(cell, styleProvider.getCellStyle(cell, rowObject, property, value), true);

			// increment the column cursors
			curColPhysical++;
		}
	}

	/**
	 * Null resistant access to {@link Cell#getStringCellValue()}
	 */
	public static final String getStringValue(final Cell cell) {
		if (cell != null) {
			return cell.getStringCellValue();
		}
		return null;
	}

	/**
	 * Create a new cell style, adapt the given color to it and store this in the given style
	 * manager.
	 * 
	 * @param manager
	 *        The manager handling the given style, must not be <code>null</code>.
	 * @param key
	 *        The accessing key for the new created style, must not be <code>null</code>.
	 * @param color
	 *        The color to be used, must not be <code>null</code>.
	 * @param style
	 *        The style to create a new one for, must not be <code>null</code>.
	 * @return The new created style, never <code>null</code>.
	 */
	public static final CellStyle adaptCellStyle(POIStyleManager manager, String key, IndexedColors color,
			CellStyle style) {
		CellStyle newStyle = manager.newStyle(style);

		newStyle.setFillBackgroundColor(color.getIndex());
		newStyle.setFillForegroundColor(color.getIndex());
		newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		return manager.putStyle(key, newStyle);
	}

	/**
	 * Returns the first occurrence of a cell with the specified text as part of its contents in the
	 * specified sheet.
	 * 
	 * <p>
	 * <strong>Note:</strong> the sheet is traversed row-wise from top to bottom and from left to
	 * right.
	 * </p>
	 * 
	 * @param sheet
	 *        the {@link Sheet} to look for a cell with the specified contents in
	 * @param contents
	 *        the contents to look for
	 * @return the first occurrence of a cell with the specified text contents or {@code null} if no
	 *         such cell has been found
	 */
	public static Cell findCell(final Sheet sheet, final String contents) {
		// go through all rows in the specified sheet
		for (final Row row : sheet) {

			// go through all columns in the specified sheet
			for (final Cell cell : row) {
				final Object value = getCellValue(cell);

				// we can only check non-blank fields for their contents.
				if (value != null && String.valueOf(value).contains(contents)) {
					return cell;
				}
			}
		}

		return null;
	}

	/**
	 * Copies the contents of the specified source template to the specified target template
	 * overriding all properties.
	 * 
	 * @param source
	 *        the source {@link POITemplateEntry} to copy from
	 * @param target
	 *        the target {@link POITemplateEntry} to copy to
	 */
	public static void copyTemplate(final POITemplateEntry source, final POITemplateEntry target) {
		// copy all properties from source to target
		target.copyProperties(source);

		// now copy the actual excel contents from the source to the target location.
		final int maxRows = source.getInteger(POITemplateEntry.ATTRIBUTE_ROWS, 1);

		final Cell srcCell = source.getCell();
		final Sheet srcSheet = srcCell.getSheet();
		final int srcRowIndex = srcCell.getRowIndex();

		final Cell tgtCell = target.getCell();
		final Sheet tgtSheet = tgtCell.getSheet();
		final int tgtRowIndex = tgtCell.getRowIndex();

		// compute the total number of physical columns to be copied.
		final int columnCount = computePhysicalColumnNumber(source);

		/* This is part of a workaround for a BUG in POI. The real workaround and bug description
		 * you'll find a few lines further down below. */
		final int shift;
		if (srcSheet.equals(tgtSheet) && tgtRowIndex <= srcRowIndex) {
			shift = 1;
		} else {
			shift = 0;
		}

		for (int i = 0; i < maxRows; i++) {
			final Row tgtRow = i == 0 ? tgtSheet.getRow(tgtRowIndex) : insertRow(tgtSheet, tgtRowIndex + i, source);

			/* Part 2 of the above mentioned workaround for a POI bug: After a row shift row objects
			 * that represent rows affected by the shifting are not properly updated. So we must
			 * re-acquire the row object. In our case we fix this by asking for the source row AFTER
			 * we have inserted a new row, though we need to adapt our index to ask for. */
			final Row srcRow = i == 0 ? srcSheet.getRow(srcRowIndex) : srcSheet.getRow(srcRowIndex + i + shift);

			copyRow(srcRow, tgtRow, srcCell.getColumnIndex(), tgtCell.getColumnIndex(), columnCount);
		}
	}

	/**
	 * Computes the number of physical (non-merged) columns for the specified template.
	 * 
	 * @param template
	 *        the {@link POITemplateEntry} to compute the physical column number for
	 * @return the physical column number
	 */
	public static int computePhysicalColumnNumber(final POITemplateEntry template) {
		// retrieve the logical column count.
		final int columns = template.getInteger(POITemplateEntry.ATTRIBUTE_COLS, 1);

		// retrieve the starting cell for the template.
		final Cell templateCell = template.getCell();
		final Row templateRow = templateCell.getRow();

		// we're going to compute this one.
		int physicalCount = 0;
		int physicalIndex = templateCell.getColumnIndex();

		// now go through the logical columns and compute the physical column count.
		for (int i = 0; i < columns; i++) {
			final Cell cell = templateRow.getCell(physicalIndex);
			if (cell != null) {
				final CellRangeAddress range = POIExcelUtil.getRange(cell);
				final int size = range == null ? 1 : range.getLastColumn() - range.getFirstColumn() + 1;

				physicalIndex += size;
				physicalCount += size;
			}
		}

		return physicalCount;
	}

	/**
	 * Copy the specified original sheet to the specified new sheet.
	 * 
	 * @param source
	 *        the source {@link Sheet} to copy from
	 * @param target
	 *        the target {@link Sheet} to copy to
	 * @param maxRows
	 *        the maximum number of rows to copy
	 */
	public static void copySheets(final Sheet source, final Sheet target, final int maxRows) {
		final int rowsToCopy = Math.min(maxRows, source.getLastRowNum());

		int maxColumnNum = 0;
		for (int rowIndex = source.getFirstRowNum(); rowIndex <= rowsToCopy; rowIndex++) {
			final Row srcRow = source.getRow(rowIndex);
			final Row tgtRow = target.createRow(rowIndex);

			if (srcRow != null) {
				POIExcelUtil.copyRow(srcRow, tgtRow);

				if (srcRow.getLastCellNum() > maxColumnNum) {
					maxColumnNum = srcRow.getLastCellNum();
				}
			}
		}

		for (int colIndex = 0; colIndex <= Math.min(maxRows, maxColumnNum); colIndex++) {
			target.setColumnWidth(colIndex, source.getColumnWidth(colIndex));
		}
	}

	/**
	 * Copies the specified source sheet's print setup to the specified target sheet.
	 * 
	 * @param source
	 *        the {@link Sheet} to copy the print setup from
	 * @param target
	 *        the {@link Sheet} to copy the print setup to
	 */
	public static void copyPrintSetup(final Sheet source, final Sheet target) {
		final PrintSetup srcSetup = source.getPrintSetup();
		final PrintSetup tgtSetup = target.getPrintSetup();

		tgtSetup.setCopies(srcSetup.getCopies());
		tgtSetup.setDraft(srcSetup.getDraft());
		tgtSetup.setFooterMargin(srcSetup.getFooterMargin());
		tgtSetup.setHeaderMargin(srcSetup.getHeaderMargin());
		tgtSetup.setHResolution(srcSetup.getHResolution());
		tgtSetup.setVResolution(srcSetup.getVResolution());
		tgtSetup.setLandscape(srcSetup.getLandscape());
		tgtSetup.setLeftToRight(srcSetup.getLeftToRight());
		tgtSetup.setNoColor(srcSetup.getNoColor());
		tgtSetup.setNoOrientation(srcSetup.getNoOrientation());
		tgtSetup.setNotes(srcSetup.getNotes());
		tgtSetup.setPaperSize(srcSetup.getPaperSize());
		tgtSetup.setPageStart(srcSetup.getPageStart());
		tgtSetup.setUsePage(srcSetup.getUsePage());
		tgtSetup.setValidSettings(srcSetup.getValidSettings());
		tgtSetup.setFitWidth(srcSetup.getFitWidth());
		tgtSetup.setFitHeight(srcSetup.getFitHeight());
		tgtSetup.setScale(srcSetup.getScale());
	}

	/**
	 * Copies the contents of the header from the specified source to the specified target sheet.
	 * 
	 * @param source
	 *        the {@link Sheet} to copy the header from
	 * @param target
	 *        the {@link Sheet} to copy the header to
	 */
	public static void copyHeader(final Sheet source, final Sheet target) {
		final Header srcHeader = source.getHeader();
		final Header tgtHeader = target.getHeader();

		tgtHeader.setCenter(srcHeader.getCenter());
		tgtHeader.setLeft(srcHeader.getLeft());
		tgtHeader.setRight(srcHeader.getRight());
	}

	/**
	 * Copies the contents of the footer from the specified source to the specified target sheet.
	 * 
	 * @param source
	 *        the {@link Sheet} to copy the footer from
	 * @param target
	 *        the {@link Sheet} to copy the footer to
	 */
	public static void copyFooter(final Sheet source, final Sheet target) {
		final Footer srcFooter = source.getFooter();
		final Footer tgtFooter = target.getFooter();

		tgtFooter.setCenter(srcFooter.getCenter());
		tgtFooter.setLeft(srcFooter.getLeft());
		tgtFooter.setRight(srcFooter.getRight());
	}

	/**
	 * Copies the general sheet settings from the specified source to the specified target sheet.
	 * 
	 * @param source
	 *        the {@link Sheet} to copy the settings from
	 * @param target
	 *        the {@link Sheet} to copy the settings to
	 */
	public static void copySheetSettings(final Sheet source, final Sheet target) {
		target.setAutobreaks(source.getAutobreaks());
		target.setDefaultColumnWidth(source.getDefaultColumnWidth());
		target.setDefaultRowHeight(source.getDefaultRowHeight());
		target.setDisplayFormulas(source.isDisplayFormulas());
		target.setDisplayGridlines(source.isDisplayGridlines());
		target.setDisplayGuts(source.getDisplayGuts());
		target.setDisplayRowColHeadings(source.isDisplayRowColHeadings());
		target.setDisplayZeros(source.isDisplayZeros());
		target.setFitToPage(source.getFitToPage());
		target.setHorizontallyCenter(source.getHorizontallyCenter());
		target.setPrintGridlines(source.isPrintGridlines());
		target.setRowSumsBelow(source.getRowSumsBelow());
		target.setRowSumsRight(source.getRowSumsRight());
		target.setSelected(source.isSelected());
		target.setVerticallyCenter(source.getVerticallyCenter());
	}

	/**
	 * Copies the pane information from the specified source to the specified target sheet.
	 * 
	 * @param source
	 *        the {@link Sheet} to copy the pane information from
	 * @param target
	 *        the {@link Sheet} to copy the pane information to
	 */
	public static void copyPaneInformation(final Sheet source, final Sheet target) {
		PaneInformation srcPane = source.getPaneInformation();

		if (srcPane != null) {

			if (srcPane.isFreezePane()) {
				target.createFreezePane(srcPane.getVerticalSplitPosition(),
					srcPane.getHorizontalSplitPosition(),
					srcPane.getVerticalSplitLeftColumn(),
					srcPane.getHorizontalSplitTopRow());
			}

			else {
				target.createSplitPane(srcPane.getVerticalSplitPosition(),
					srcPane.getHorizontalSplitPosition(),
					srcPane.getVerticalSplitLeftColumn(),
					srcPane.getHorizontalSplitTopRow(),
					srcPane.getActivePane());
			}
		}
	}

	/**
	 * Copies the contents of the specified row starting at the specified column index to the
	 * specified row starting at the specified target column. The target row's height is adjusted to
	 * fit the logical row's height.
	 * 
	 * @param source
	 *        the source {@link Row} to copy from
	 * @param target
	 *        the target {@link Row} to copy to
	 * @param sourceStart
	 *        the index of the column to start copying from
	 * @param targetStart
	 *        the index of the column to start copying to
	 * @param maxCells
	 *        the total number of cells to be copied
	 */
	public static void copyRow(final Row source, final Row target, final int sourceStart, final int targetStart,
			final int maxCells) {

		// we have to compute the row height as well.
		int rowHeight = source.getHeight();

		final Sheet srcSheet = source.getSheet();
		final Sheet tgtSheet = target.getSheet();

		// now go through the cells to be copied.
		for (int i = 0; i < maxCells; i++) {
			final Cell srcCell = source.getCell(sourceStart + i);

			// there is nothing to copy, continue to the next cell
			if (srcCell == null) {
				continue;
			}

			// retrieve or create the cell in the destination row.
			final Cell tgtCell = createCell(target, targetStart + i, srcCell.getCellStyle(), false);

			// copy cell contents only
			copyCell(srcCell, tgtCell, false);

			// copy the merged region (optional)
			final CellRangeAddress srcRegion =
				POIExcelUtil.getRange(srcSheet, source.getRowNum(), srcCell.getColumnIndex());
			if (srcRegion != null && (srcRegion.getFirstColumn() != srcRegion.getLastColumn())) {

				// compute the height of the cell again if it's part of a merged region
				rowHeight = Math.max(rowHeight, computeHeight(srcCell));

				// check if the current cell is part of another merged region already.
				final CellRangeAddress tgtRegion =
					POIExcelUtil.getRange(tgtSheet, target.getRowNum(), tgtCell.getColumnIndex());

				// if not, create a new merged region of the same size as the source one.
				if (tgtRegion == null) {
					tgtSheet.addMergedRegion(
						new CellRangeAddress(
							target.getRowNum(),
							target.getRowNum(),
							tgtCell.getColumnIndex(),
							tgtCell.getColumnIndex()
								+ (Math.min(maxCells, srcRegion.getLastColumn() - srcRegion.getFirstColumn()))));
				}
			}
		}

		// set the appropriate row height using the height of the source.
		if (rowHeight > -1) {
			target.setHeight((short) rowHeight);
		}
	}

	/**
	 * Copies contents of the specified source row to the specified target row.
	 * 
	 * @param source
	 *        the source {@link Row} to copy contents from
	 * @param target
	 *        the target {@link Row} to copy contents to
	 */
	public static void copyRow(final Row source, final Row target) {
		final Sheet srcSheet = source.getSheet();
		final Sheet tgtSheet = target.getSheet();

		// copy the row's height
		if (source.getHeight() >= 0) {
			target.setHeight(source.getHeight());
		}

		// copy contents
		for (int cellIndex = source.getFirstCellNum(); cellIndex <= source.getLastCellNum(); cellIndex++) {
			final Cell srcCell = source.getCell(cellIndex);

			if (srcCell != null) {
				// create a new cell with the same cell style
				final Cell tgtCell = createCell(target, cellIndex, srcCell.getCellStyle(), false);

				// copy cell contents only
				POIExcelUtil.copyCell(srcCell, tgtCell, false);

				final CellRangeAddress srcRegion =
					POIExcelUtil.getRange(srcSheet, source.getRowNum(), srcCell.getColumnIndex());

				if (srcRegion != null) {
					final CellRangeAddress tgtRegion = new CellRangeAddress(
						srcRegion.getFirstRow(), srcRegion.getLastRow(),
						srcRegion.getFirstColumn(), srcRegion.getLastColumn());

					if (!isPartOfMergedRegion(tgtSheet, srcRegion.getFirstRow(), srcRegion.getLastRow(),
						srcRegion.getFirstColumn(), srcRegion.getLastColumn())) {
						tgtSheet.addMergedRegion(tgtRegion);
					}
				}
			}
		}
	}

	/**
	 * Merges the cells defined by the specified coordinates in the specified sheet.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to create a merge region in
	 * @param startRow
	 *        the region's starting row
	 * @param startCol
	 *        the region's start column
	 * @param endRow
	 *        the region's end row
	 * @param endCol
	 *        the region's end column
	 */
	public static void mergeRegion(final Sheet sheet, final int startRow, final int startCol, final int endRow,
			final int endCol) {
		final CellRangeAddress region = POIExcelUtil.getRange(sheet, startRow, startCol);

		if (region == null) {
			final CellRangeAddress newRegion = new CellRangeAddress(startRow, endRow, startCol, endCol);

			sheet.addMergedRegion(newRegion);
		}
	}

	/**
	 * Normalizes the specified sheet name by replacing each occurrence of the following characters
	 * with "-"
	 * <ul>
	 * <li>\</li>
	 * <li>/</li>
	 * <li>?</li>
	 * <li>*</li>
	 * <li>[</li>
	 * <li>]</li>
	 * </ul>
	 * 
	 * It also replaces the character ' if it appears at the beginning or at the end of the name by
	 * "-".
	 * 
	 * @param name
	 *        the sheet name to normalize
	 * @return the normalized sheet name
	 */
	public static String normalizeSheetName(final String name) {
		String normalizedName = name.replaceAll(POIExcelUtil.NORMALIZER, POIExcelUtil.NORMALIZE_REPLACER);

		if (normalizedName.charAt(0) == '\'') {
			normalizedName = POIExcelUtil.NORMALIZE_REPLACER + normalizedName.substring(1, normalizedName.length());
		}

		if (normalizedName.charAt(normalizedName.length() - 1) == '\'') {
			normalizedName = normalizedName.substring(0, normalizedName.length() - 1) + POIExcelUtil.NORMALIZE_REPLACER;
		}

		return normalizedName;
	}

	/**
	 * Create a unique sheet name for the given one.
	 * 
	 * @param workbook
	 *        The {@link Workbook} to create the sheet name in, must not be {@code null}.
	 * @param name
	 *        The name to create the sheet name for, must not be {@code null}.
	 * @return The created sheet name which is unique in the specified workbook, never {@code null}.
	 * @see #shortSheetName(String, int)
	 */
	public static String createSheetName(final Workbook workbook, final String name) {
		final int maxLen = POIExcelUtil.MAX_SHEET_NAME_LENGTH;
		final String normalizedName = POIExcelUtil.normalizeSheetName(name);

		String uniqueName = POIExcelUtil.shortSheetName(normalizedName, maxLen);
		int sheetCount = 0;

		// Avoid problems with existing sheet names
		while (workbook.getSheet(uniqueName) != null) {
			final String suffix;
			if (sheetCount > 0) {
				suffix = '(' + String.valueOf(sheetCount) + ')';
			} else {
				suffix = StringServices.EMPTY_STRING;
			}

			// shorten the name to accomodate suffix
			if (normalizedName.length() > (maxLen - suffix.length())) {
				uniqueName = POIExcelUtil.shortSheetName(normalizedName, maxLen - suffix.length());
			}

			uniqueName += suffix;
			sheetCount++;
		}

		return uniqueName;
	}

	/**
	 * Shorting mechanism for a sheet name, when the normal output would be longer than the given
	 * length. This is needed to allow creating correct sheet names in excel.
	 * 
	 * @param name
	 *        The name to be used for shortening it, must not be {@code null}.
	 * @param length
	 *        The length to short the report key to.
	 * @return The requested short name (maximum the given length).
	 */
	protected static String shortSheetName(final String name, final int length) {
		return StringServices.minimizeString(name, length, length - 3);
	}

	/**
	 * Copies the specified cell.
	 * 
	 * @param oldCell
	 *        the source {@link Cell} to copy from
	 * @param newCell
	 *        the target {@link Cell} to copy to
	 * @param copyStyle
	 *        {@code true} to copy the cell style, {@code false} to copy contents only
	 */
	public static void copyCell(final Cell oldCell, final Cell newCell, final boolean copyStyle) {
		if (copyStyle) {
			newCell.setCellStyle(oldCell.getCellStyle());
		}
		switch (oldCell.getCellType()) {
			case STRING:
				newCell.setCellValue(oldCell.getRichStringCellValue());
				break;
			case NUMERIC:
				newCell.setCellValue(oldCell.getNumericCellValue());
				break;
			case BLANK:
				newCell.setBlank();
				break;
			case BOOLEAN:
				newCell.setCellValue(oldCell.getBooleanCellValue());
				break;
			case ERROR:
				newCell.setCellErrorValue(oldCell.getErrorCellValue());
				break;
			case FORMULA:
				newCell.setCellFormula(oldCell.getCellFormula());
				break;
			default:
				break;
		}
	}

	/**
	 * Checks if the specified merged regions are equal when it comes to their range.
	 * 
	 * @param region1
	 *        the first {@link CellRangeAddress} or {@code null}
	 * @param region2
	 *        the second {@link CellRangeAddress} or {@code null}
	 * @return {@code true} if both regions are equal, {@code false} otherwise
	 */
	public static boolean areRegionsEqual(final CellRangeAddress region1, final CellRangeAddress region2) {
		if (region1 == null) {
			if (region2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (region2 == null) {
				return false;
			} else {
				return (region1.getFirstColumn() == region2.getFirstColumn()
					&& region1.getLastColumn() == region2.getLastColumn()
					&& region1.getFirstRow() == region2.getFirstRow() && region2
						.getLastRow() == region2.getLastRow());
			}
		}
	}

	/**
	 * Checks if the specified cell range is contained in the specified collection.
	 * 
	 * @param region
	 *        the {@link CellRangeAddress} to check
	 * @param regions
	 *        the {@link Collection} of {@link CellRangeAddress}es to look at
	 * @return {@code true} if the specified cell range has not been found in the specified
	 *         collection, {@code false} otherwise
	 */
	protected static boolean isNewMergedRegion(final CellRangeAddress region,
			final Collection<CellRangeAddress> regions) {
		for (final CellRangeAddress range : regions) {
			if (POIExcelUtil.areRegionsEqual(range, region)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * An {@link Accessor} implementation dispatching to the underlying {@link ObjectTableModel}.
	 * 
	 * @author <a href=mailto:wta@top-logic.com>wta</a>
	 */
	private static class DispatchingObjectTableModellAccessor implements Accessor {

		/**
		 * The {@link ObjectTableModel} providing actual acess to the values.
		 */
		private final ObjectTableModel _table;

		/**
		 * Create a new {@link DispatchingObjectTableModellAccessor}.
		 * 
		 * @param table
		 *        the {@link ObjectTableModel} to provide access to
		 */
		public DispatchingObjectTableModellAccessor(final ObjectTableModel table) {
			_table = table;
		}

		@Override
		public Object getValue(final Object object, final String property) {
			return _table.getValueAt(object, property);
		}

		@Override
		public void setValue(final Object object, final String property, final Object value) {
			throw new UnsupportedOperationException("setValue not supported by " + this);
		}

	}

	/**
	 * Bugfix copy of the original {@link org.apache.poi.ss.formula.functions.Sumproduct} function
	 * in current POI 3.8.
	 * 
	 * This implementation will return an {@link ErrorEval#VALUE_INVALID} when evaluating a sum
	 * field failed.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static final class TLSumProduct implements Function {

		@Override
		public ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {

			int maxN = args.length;

			if (maxN < 1) {
				return ErrorEval.VALUE_INVALID;
			}
			ValueEval firstArg = args[0];
			try {
				if (firstArg instanceof NumericValueEval) {
					return evaluateSingleProduct(args);
				}
				if (firstArg instanceof RefEval) {
					return evaluateSingleProduct(args);
				}
				if (firstArg instanceof TwoDEval) {
					TwoDEval ae = (TwoDEval) firstArg;
					if (ae.isRow() && ae.isColumn()) {
						return evaluateSingleProduct(args);
					}
					return evaluateAreaSumProduct(args);
				}
			} catch (EvaluationException e) {
				return e.getErrorEval();
			}
			return ErrorEval.VALUE_INVALID;
		}

		private static ValueEval evaluateSingleProduct(ValueEval[] evalArgs) throws EvaluationException {
			int maxN = evalArgs.length;

			double term = 1D;
			for (int n = 0; n < maxN; n++) {
				double val = getScalarValue(evalArgs[n]);
				term *= val;
			}
			return new NumberEval(term);
		}

		private static double getScalarValue(ValueEval arg) throws EvaluationException {

			ValueEval eval;
			if (arg instanceof RefEval) {
				RefEval re = (RefEval) arg;
				if (re.getFirstSheetIndex() != re.getLastSheetIndex()) {
					throw new EvaluationException(ErrorEval.VALUE_INVALID);
				}
				eval = re.getInnerValueEval(re.getFirstSheetIndex());
			} else {
				eval = arg;
			}

			if (eval == null) {
				throw new RuntimeException("parameter may not be null");
			}
			if (eval instanceof AreaEval) {
				AreaEval ae = (AreaEval) eval;
				// an area ref can work as a scalar value if it is 1x1
				if (!ae.isColumn() || !ae.isRow()) {
					throw new EvaluationException(ErrorEval.VALUE_INVALID);
				}
				eval = ae.getRelativeValue(0, 0);
			}

			return getProductTerm(eval, true);
		}

		private static ValueEval evaluateAreaSumProduct(ValueEval[] evalArgs) throws EvaluationException {
			int maxN = evalArgs.length;
			TwoDEval[] args = new TwoDEval[maxN];
			try {
				System.arraycopy(evalArgs, 0, args, 0, maxN);
			} catch (ArrayStoreException e) {
				// one of the other args was not an AreaRef
				return ErrorEval.VALUE_INVALID;
			}

			TwoDEval firstArg = args[0];

			int height = firstArg.getHeight();
			int width = firstArg.getWidth();

			// first check dimensions
			if (!areasAllSameSize(args, height, width)) {
				// normally this results in #VALUE!,
				// but errors in individual cells take precedence
				for (int i = 1; i < args.length; i++) {
					throwFirstError(args[i]);
				}
				return ErrorEval.VALUE_INVALID;
			}

			double acc = 0;

			for (int rrIx = 0; rrIx < height; rrIx++) {
				for (int rcIx = 0; rcIx < width; rcIx++) {
					double term = 1D;
					for (int n = 0; n < maxN; n++) {
						double val = getProductTerm(args[n].getValue(rrIx, rcIx), false);
						term *= val;
					}
					acc += term;
				}
			}

			return new NumberEval(acc);
		}

		private static void throwFirstError(TwoDEval areaEval) throws EvaluationException {
			int height = areaEval.getHeight();
			int width = areaEval.getWidth();
			for (int rrIx = 0; rrIx < height; rrIx++) {
				for (int rcIx = 0; rcIx < width; rcIx++) {
					ValueEval ve = areaEval.getValue(rrIx, rcIx);
					if (ve instanceof ErrorEval) {
						throw new EvaluationException((ErrorEval) ve);
					}
				}
			}
		}

		private static boolean areasAllSameSize(TwoDEval[] args, int height, int width) {
			for (int i = 0; i < args.length; i++) {
				TwoDEval areaEval = args[i];
				// check that height and width match
				if (areaEval.getHeight() != height) {
					return false;
				}
				if (areaEval.getWidth() != width) {
					return false;
				}
			}
			return true;
		}

        /**
         * Determines a <code>double</code> value for the specified <code>ValueEval</code>.
         * 
         * @param isScalarProduct
         *        {@code false} for SUMPRODUCTs over area refs.
         * @throws EvaluationException
         *         if <code>ve</code> represents an error value.
         *         <p/>
         *         Note - string values and empty cells are interpreted differently depending on
         *         <code>isScalarProduct</code>. For scalar products, if any term is blank or a
         *         string, the error (#VALUE!) is raised. For area (sum)products, if any term is
         *         blank or a string, the result is zero.
         */
        private static double getProductTerm(ValueEval ve, boolean isScalarProduct) throws EvaluationException {

            if (ve instanceof BlankEval || ve == null) {
                // TODO - shouldn't BlankEval.INSTANCE be used always instead of null?
                // null seems to occur when the blank cell is part of an area ref (but not reliably)
                if (isScalarProduct) {
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
                }
                return 0;
            }

            if (ve instanceof ErrorEval) {
                throw new EvaluationException((ErrorEval) ve);
            }
            if (ve instanceof StringEval) {
                if (isScalarProduct) {
                    throw new EvaluationException(ErrorEval.VALUE_INVALID);
                }
                // Note for area SUMPRODUCTs, string values are interpreted as zero
                // even if they would parse as valid numeric values
                return 0;
            }
            if (ve instanceof NumericValueEval) {
                NumericValueEval nve = (NumericValueEval) ve;
                return nve.getNumberValue();
            }
            throw new RuntimeException("Unexpected value eval class ("
                + ve.getClass().getName() + ")");
        }
    }
}