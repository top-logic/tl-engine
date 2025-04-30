/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.streaming;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.ExcelValue.CellPosition;
import com.top_logic.base.office.excel.ExcelValue.MergeRegion;
import com.top_logic.base.office.excel.POIDrawingManager;
import com.top_logic.base.office.excel.POIExcelTemplate;
import com.top_logic.base.office.excel.POIExcelTemplate.POITemplateEntry;
import com.top_logic.base.office.excel.POIExcelValueSetter;
import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.base.office.excel.handler.POITypeProvider;
import com.top_logic.basic.Settings;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExcelWriter extends AbstractCellStreamWriter implements POITypeSupporter {

	private final Workbook _workbook;

	private final Map<String, Map<Integer, Integer>> _sheetMap;

	private final POIExcelValueSetter _valueSetter;

	private final CellStyle _dateStyle;

	private final Map<Sheet, POIExcelTemplate> _templates;

	private final POITypeProvider _handlerProvider;

	private POIDrawingManager _drawingMgr;

	/**
	 * Creates a new {@link ExcelWriter}.
	 * 
	 * @param useXFormat
	 *        Flag indicating whether to use "xlsx" (true) or "xls" (false) format.
	 */
	public ExcelWriter(boolean useXFormat) {
		this(useXFormat ? new SXSSFWorkbook(10) : new HSSFWorkbook());
	}

	/**
	 * Creates a new {@link ExcelWriter}.
	 * 
	 * @param workbook
	 *        The workbook to use by this {@link ExcelWriter}.
	 */
	public ExcelWriter(Workbook workbook) {
		_workbook = Objects.requireNonNull(workbook);
		_sheetMap = new HashMap<>();
		_valueSetter = new POIExcelValueSetter(workbook, _sheetMap);

		_dateStyle = POIExcelValueSetter.createDateStyle(workbook);
		_templates = POIExcelValueSetter.parseTemplate(workbook);
		_handlerProvider = new POITypeProvider();
	}

	@Override
	public void setFreezePane(int col, int row) {
		Sheet sheet = _workbook.getSheet(currentTable());
		if (sheet == null) {
			sheet = _workbook.createSheet(currentTable());
		}
		sheet.createFreezePane(col, row);
	}

	@Override
	protected File internalClose() throws IOException {
		POIUtil.setAutoFitWidths(_workbook, _sheetMap);

		String fileSuffix = POIUtil.getFileSuffix(_workbook);
		File   theResult  = File.createTempFile("ExcelWriter", fileSuffix, Settings.getInstance().getTempDir());

		POIUtil.doWriteWorkbook(theResult, _workbook);

		return theResult;
	}

	/**
	 * The workbook used by this writer.
	 * 
	 * @return The inner workbook, never <code>null</code>.
	 */
	public Workbook getWorkbook() {
		return _workbook;
	}

	@Override
	protected void internalNewRow() throws IOException {
		// does nothing
	}

	@Override
	protected void internalNewTable(String tablename) throws IOException {
		// does nothing
	}

	@Override
	protected void internalWrite(Object cellvalue) throws IOException {
		if (cellvalue instanceof ExcelValue) {
			CellPosition position = new CellPosition(currentTable(), currentRow(), currentColumn());
			_valueSetter.setValue(position, (ExcelValue) cellvalue);
			MergeRegion mergeRegion = ((ExcelValue) cellvalue).getMergeRegion();
			if (mergeRegion != null) {
				incColumn((mergeRegion.getToCol() - mergeRegion.getFromCol()));
			}
		} else {
			addValue(_workbook, currentTable(), currentRow(), currentColumn(), cellvalue, _sheetMap);
		}
	}

	@Override
	public CellStyle getDateStyle() {
		return _dateStyle;
	}

	@Override
	public POITemplateEntry getTemplate(Cell aCell) {
		POIExcelTemplate theTemplate = _templates.get(aCell.getSheet());

		return (theTemplate != null) ? theTemplate.getEntry(aCell.getStringCellValue()) : null;
	}

	@Override
	public POIDrawingManager getDrawingManager() {
		if (_drawingMgr == null) {
			_drawingMgr = new POIDrawingManager();
		}

		return _drawingMgr;
	}

	/**
	 * Add the given value to the workbook (on the position described by the other parameters).
	 * 
	 * @param aWorkbook
	 *        The workbook to be filled, must not be <code>null</code>.
	 * @param aSheet
	 *        The name of the sheet, must not be <code>null</code>.
	 * @param aRow
	 *        The row to write the value to.
	 * @param aColumn
	 *        The column to write the value to.
	 * @param aValue
	 *        The value to be exported, may be <code>null</code>.
	 * @see #resolveCell(Workbook, String, int, int)
	 * @see #storeColumnWidth(String, int, int, Map)
	 */
	private void addValue(Workbook aWorkbook, String aSheet, int aRow, int aColumn, Object aValue,
			Map<String, Map<Integer, Integer>> aSheetMap) {
		Cell theCell = resolveCell(aWorkbook, aSheet, aRow, aColumn);
		Object theValue = (aValue instanceof ExcelValue) ? ((ExcelValue) aValue).getValue() : aValue;
		PoiTypeHandler theHandler = _handlerProvider.getPOITypeHandler(theValue);
		int theLength = theHandler.setValue(theCell, aWorkbook, theValue, this);

		if (aSheetMap != null) {
			storeColumnWidth(aSheet, aColumn, theLength, aSheetMap);
		}
	}

	/**
	 * Return the cell described by the given parameters.
	 * 
	 * @param aWorkbook
	 *        The workbook to be used, must not be <code>null</code>.
	 * @param aSheetName
	 *        The name of the sheet, must not be <code>null</code>.
	 * @param aRow
	 *        The row of the requested cell.
	 * @param aColumn
	 *        The column of the requested cell.
	 * @return The requested cell, never <code>null</code>.
	 */
	private Cell resolveCell(Workbook aWorkbook, String aSheetName, int aRow, int aColumn) {
		Sheet theSheet = aWorkbook.getSheet(aSheetName);
		if (theSheet == null) {
			theSheet = aWorkbook.createSheet(aSheetName);
		}
		Row theRow = createIfNull(aRow, theSheet);

		return createIfNull(aColumn, theRow);
	}

	/**
	 * Queries a row in the sheet. If the row does not exits, it is created.
	 *
	 * @param aRowNumber
	 *        the row number inside the sheet
	 * @param aSheet
	 *        the sheet containing the row
	 * @return the row
	 */
	private Row createIfNull(int aRowNumber, Sheet aSheet) {
		Row theRow = aSheet.getRow(aRowNumber);

		if (theRow == null) {
			theRow = aSheet.createRow(aRowNumber);
		}

		return theRow;
	}

	/**
	 * Queries a cell in the row. If the cell does not exits, it is created.
	 *
	 * When the sheet is given, and the cell has been created, this method will change the style of
	 * the cell to the one taken from the upper cell.
	 *
	 * @param aCellNumber
	 *        The cell number in the row.
	 * @param aRow
	 *        The row containing the cell.
	 * @return The requested cell.
	 */
	private static Cell createIfNull(int aCellNumber, Row aRow) {
		// beware: aRow.getCell(aNumber, aPolicy) is not supported by streaming implementation of
		// POI
		Cell cell = aRow.getCell(aCellNumber);
		if (cell == null) {
			cell = aRow.createCell(aCellNumber);
		}
		return cell;
	}

	/**
	 * Store the width of the given string in the given sheet map for later resolving real maximum
	 * width.
	 * 
	 * @param aSheetName
	 *        The name of the sheet to be used, must not be <code>null</code>.
	 * @param aColumn
	 *        The number of the requested column.
	 * @param aLength
	 *        The string width, may be <code>null</code>.
	 */
	private void storeColumnWidth(String aSheetName, int aColumn, int aLength,
			Map<String, Map<Integer, Integer>> aSheetMap) {
		if (aLength > 0) {
			/* Locate the map that holds the column widths for the given sheet. */
			Map<Integer, Integer> theWidthMap = aSheetMap.get(aSheetName);

			/* If non could be found, create a new one and store it. */
			if (theWidthMap == null) {
				theWidthMap = new HashMap<>();
				aSheetMap.put(aSheetName, theWidthMap);
			}

			/* Get the width in the map for the given column. */
			Integer theWidth = theWidthMap.get(aColumn);

			/* If the value of the map is shorter, store the new value in the map. */
			if ((theWidth == null) || (theWidth.intValue() < aLength)) {
				theWidthMap.put(aColumn, aLength);
			}
		}
	}

}
