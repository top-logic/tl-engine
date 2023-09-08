/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;


import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.POIExcelTemplate.POITemplateEntry;
import com.top_logic.base.office.excel.handler.POITypeProvider;
import com.top_logic.basic.Logger;

/**
 * Helper for creating POI based exports.
 * 
 * Needs to be consolidated with {@link POIExcelUtil}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>, <a href="mailto:wta@top-logic.com">Wjatscheslaw Talanow</a>
 */
public class POIExportHelper implements POITypeSupporter {

	private final CellStyle dateStyle;
	private final Map<Sheet, POIExcelTemplate> templates;
	private final POITypeProvider handlerProvider;
	private POIDrawingManager drawingMgr;

	/** 
	 * Creates a {@link POIExportHelper}.
	 */
	public POIExportHelper(Workbook aWorkbook) {
		this(POIExcelValueSetter.createDateStyle(aWorkbook), POIExcelValueSetter.parseTemplate(aWorkbook));
	}

	/** 
	 * Creates a {@link POIExportHelper}.
     */
	public POIExportHelper(CellStyle aDateStyle, Map<Sheet, POIExcelTemplate> someTemplates) {
		this.dateStyle       = aDateStyle;
		this.templates       = someTemplates;
		this.handlerProvider = new POITypeProvider();
	}

	@Override
	public CellStyle getDateStyle() {
		return this.dateStyle;
	}

	@Override
	public POITemplateEntry getTemplate(Cell aCell) {
		POIExcelTemplate theTemplate = this.templates.get(aCell.getSheet());

		return (theTemplate != null) ? theTemplate.getEntry(aCell.getStringCellValue()) : null;
	}

	@Override
	public POIDrawingManager getDrawingManager() {
	    if (this.drawingMgr == null) {
	    	this.drawingMgr = new POIDrawingManager();
	    }

	    return this.drawingMgr;
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
	public void addValue(Workbook aWorkbook, String aSheet, int aRow, int aColumn, Object aValue, Map<String, Map<Integer, Integer>> aSheetMap) {
		Cell theCell = this.resolveCell(aWorkbook, aSheet, aRow, aColumn);
		Object theValue = (aValue instanceof ExcelValue) ? ((ExcelValue) aValue).getValue() : aValue;
		PoiTypeHandler theHandler = this.handlerProvider.getPOITypeHandler(theValue);
		int theLength = theHandler.setValue(theCell, aWorkbook, theValue, this);

		if (aSheetMap != null) {
			this.storeColumnWidth(aSheet, aColumn, theLength, aSheetMap);
		}
	}

	/** 
	 * Applies cell styles from the given excelValue to the given cell.
	 * 
	 * @param workbook
	 *        preferred builder of a new cell style
	 * @param sheet
	 *        sheet, where the {@link ExcelValue} belongs to
	 * @param excelValue
	 *        the {@link ExcelValue} holding the desired style information
	 * @param styleTemplate
	 *        the style which shall be used as template of returned style, may be null
	 */
	protected CellStyle createCellStyle(Workbook workbook, String sheet, ExcelValue excelValue, CellStyle styleTemplate) {
		if (excelValue.hasReferenceStyle()) {
			Logger.warn("Streaming export cannot apply cell styles, provided by referenced cells! Using default cell style!", POIExportHelper.class);

			return POIUtil.createCopyOfMasterStyle(workbook);
		}

		return POIUtil.createCellStyle(workbook, excelValue, styleTemplate);
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
	public Cell resolveCell(Workbook aWorkbook, String aSheetName, int aRow, int aColumn) {
        Sheet theSheet = aWorkbook.getSheet(aSheetName);
		if (theSheet == null) {
			theSheet = aWorkbook.createSheet(aSheetName);
		}
		Row theRow = createIfNull(aRow, theSheet);

        return POIExportHelper.createIfNull(aColumn, theRow);
    }

    /**
     * Queries a row in the sheet. If the row does not exits, it is created.
     *
     * @param aRowNumber the row number inside the sheet
     * @param aSheet the sheet containing the row
     * @return the row
     */
	public Row createIfNull(int aRowNumber, Sheet aSheet) {
        Row theRow = aSheet.getRow(aRowNumber);

        if (theRow == null) {
            theRow = aSheet.createRow(aRowNumber);
        }

        return theRow;
    }

    /**
     * Queries a cell in the row. If the cell does not exits, it is created.
     *
     * When the sheet is given, and the cell has been created, this method will change
     * the style of the cell to the one taken from the upper cell.
     *
     * @param    aCellNumber    The cell number in the row.
     * @param    aRow           The row containing the cell.
     * @return   The requested cell.
     */
    public static Cell createIfNull(int aCellNumber, Row aRow) {
    	// beware: aRow.getCell(aNumber, aPolicy) is not supported by streaming implementation of POI
    	Cell cell = aRow.getCell(aCellNumber);
    	if(cell == null){
    		cell= aRow.createCell(aCellNumber);
    	}
    	return cell;
    }

    /** 
     * Store the width of the given string in the given sheet map for later resolving real maximum width.
     * 
     * @param    aSheetName    The name of the sheet to be used, must not be <code>null</code>.
     * @param    aColumn       The number of the requested column.
     * @param    aLength       The string width, may be <code>null</code>.
     */
	protected void storeColumnWidth(String aSheetName, int aColumn, int aLength, Map<String, Map<Integer, Integer>> aSheetMap) {
        if (aLength > 0) {
            /* Locate the map that holds the column widths for the given sheet. */
            Map<Integer,Integer> theWidthMap = aSheetMap.get(aSheetName);

            /* If non could be found, create a new one and store it. */
            if (theWidthMap == null) {
                theWidthMap = new HashMap<>();
                aSheetMap.put(aSheetName, theWidthMap);
            }
    
            /* Get the width in the map for the given column. */
            Integer theWidth  = theWidthMap.get(aColumn);
    
            /* If the value of the map is shorter, store the new value in the map. */
            if ((theWidth == null) || (theWidth.intValue() < aLength)) {
                theWidthMap.put(aColumn, aLength);
            }
        }
    }


}

