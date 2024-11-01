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

	private final Workbook workbook;

	private final Map<String, Map<Integer, Integer>> sheetMap;

	private final POIExportHelper exportHelper;

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
		this.workbook = Objects.requireNonNull(workbook);
		this.sheetMap = new HashMap<>();
		this.exportHelper = new POIExportHelper(this.workbook);
		_valueSetter = new POIExcelValueSetter(workbook, sheetMap);
	}

	/**
	 * Mapping holds the column widths for all sheets, i.e. a mapping from the name of a sheet to
	 * the mapping of the column numbers to the widths of that column.
	 */
	protected Map<String, Map<Integer, Integer>> getSheetMap() {
		return sheetMap;
	}

	/**
	 * {@link POIExportHelper} used by this {@link ExcelWriter}.
	 */
	protected POIExportHelper getExportHelper() {
		return exportHelper;
	}

	@Override
	public void setFreezePane(int col, int row) {
		Sheet sheet = workbook.getSheet(currentTable);
		if (sheet == null) {
			sheet = workbook.createSheet(currentTable);
		}
		sheet.createFreezePane(col, row);
	}

	@Override
	protected File internalClose() throws IOException {
		POIUtil.setAutoFitWidths(workbook, sheetMap);

		String fileSuffix = POIUtil.getFileSuffix(workbook);
		File   theResult  = File.createTempFile("ExcelWriter", fileSuffix, Settings.getInstance().getTempDir());

		POIUtil.doWriteWorkbook(theResult, workbook);

		return theResult;
	}

	/**
	 * The workbook used by this writer.
	 * 
	 * @return The inner workbook, never <code>null</code>.
	 */
	public Workbook getWorkbook() {
		return workbook;
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
			CellPosition position = new CellPosition(currentTable, currentRowIndex, currentColumnIndex);
			_valueSetter.setValue(position, (ExcelValue) cellvalue);
			MergeRegion mergeRegion = ((ExcelValue) cellvalue).getMergeRegion();
			if (mergeRegion != null) {
				currentColumnIndex += (mergeRegion.getToCol() - mergeRegion.getFromCol());
			}
		} else {
			exportHelper.addValue(workbook, currentTable, currentRowIndex, currentColumnIndex, cellvalue, sheetMap);
		}
	}

	/**
	 * Increments the column index.
	 */
	public void newColumn() {
		currentColumnIndex++;
	}

	/**
	 * Resolves the {@link Cell} for the given row and column.
	 * 
	 * @see POIExportHelper#resolveCell(Workbook, String, int, int)
	 */
	public Cell resolveCell(int row, int col) {
		return exportHelper.resolveCell(workbook, currentTable, row, col);
	}

	/**
	 * Writes an {@link ExcelValue} to the {@link CellPosition} described by table, row and col.
	 */
	public void writeAt(Object cellvalue, String table, int row, int col) {
		String sheet = table == null ? currentTable : table;
		if (cellvalue instanceof ExcelValue) {
			CellPosition position = new CellPosition(sheet, row, col);
			_valueSetter.setValue(position, (ExcelValue) cellvalue);
		} else {
			exportHelper.addValue(workbook, sheet, row, col, cellvalue, sheetMap);
		}
	}

	/**
	 * Writes the given value and applies the given {@link CellStyle}.
	 */
	public void write(Object cellvalue, CellStyle style) throws IOException {
		internalWrite(cellvalue);

		if (style != null) {
			Cell cell = resolveCell(currentRowIndex, currentColumnIndex);
			cell.setCellStyle(style);
		}
		newColumn();
	}

}
