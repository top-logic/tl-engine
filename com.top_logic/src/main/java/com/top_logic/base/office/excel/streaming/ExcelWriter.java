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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.ExcelValue.CellPosition;
import com.top_logic.base.office.excel.ExcelValue.MergeRegion;
import com.top_logic.base.office.excel.POIExcelValueSetter;
import com.top_logic.base.office.excel.POIExportHelper;
import com.top_logic.basic.Settings;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExcelWriter extends AbstractCellStreamWriter {

	private final Workbook _workbook;

	private final Map<String, Map<Integer, Integer>> _sheetMap;

	private final POIExportHelper _exportHelper;

	private final POIExcelValueSetter _valueSetter;

	/**
	 * Creates a new {@link ExcelWriter} using "xlsx" format.
	 */
	public ExcelWriter() {
		this(true);
	}

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
		_exportHelper = new POIExportHelper(_workbook);
		_valueSetter = new POIExcelValueSetter(workbook, _sheetMap);
	}

	/**
	 * Mapping holds the column widths for all sheets, i.e. a mapping from the name of a sheet to
	 * the mapping of the column numbers to the widths of that column.
	 */
	protected Map<String, Map<Integer, Integer>> getSheetMap() {
		return _sheetMap;
	}

	/**
	 * {@link POIExportHelper} used by this {@link ExcelWriter}.
	 */
	protected POIExportHelper getExportHelper() {
		return _exportHelper;
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
			_exportHelper.addValue(_workbook, currentTable(), currentRow(), currentColumn(), cellvalue, _sheetMap);
		}
	}

	/**
	 * Resolves the {@link Cell} for the given row and column.
	 * 
	 * @see POIExportHelper#resolveCell(Workbook, String, int, int)
	 */
	public Cell resolveCell(int row, int col) {
		return _exportHelper.resolveCell(_workbook, currentTable(), row, col);
	}

	/**
	 * Writes an {@link ExcelValue} to the {@link CellPosition} described by table, row and col.
	 */
	public void writeAt(Object cellvalue, String table, int row, int col) {
		String sheet = table == null ? currentTable() : table;
		if (cellvalue instanceof ExcelValue) {
			CellPosition position = new CellPosition(sheet, row, col);
			_valueSetter.setValue(position, (ExcelValue) cellvalue);
		} else {
			_exportHelper.addValue(_workbook, sheet, row, col, cellvalue, _sheetMap);
		}
	}

	/**
	 * Writes the given value and applies the given {@link CellStyle}.
	 */
	public void write(Object cellvalue, CellStyle style) throws IOException {
		internalWrite(cellvalue);

		if (style != null) {
			Cell cell = resolveCell(currentRow(), currentColumn());
			cell.setCellStyle(style);
		}
		newColumn();
	}

}
