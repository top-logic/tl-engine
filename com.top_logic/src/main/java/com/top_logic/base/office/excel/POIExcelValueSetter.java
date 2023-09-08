/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.ExcelValue.CellPosition;
import com.top_logic.base.office.excel.ExcelValue.MergeRegion;
import com.top_logic.base.office.excel.POIExcelTemplate.POITemplateEntry;
import com.top_logic.base.office.excel.handler.POITypeProvider;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;

/**
 * Bring values from {@link ExcelValue} into a POI based excel {@link Workbook}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class POIExcelValueSetter implements POITypeSupporter {

	/** Cache for column width in the different sheets. */
	protected final Map<String, Map<Integer, Integer>> sheetMap;

	/** The workbook we are operating on. */
	protected final Workbook workbook;

	/** Cache for the used styles. */
	private final Map<Tuple, CellStyle> cellStyleCache;

	/** The map of sheet and the extract template definition. */
	private Map<Sheet, POIExcelTemplate> templates;

	/** The cell style for date fields. */
	private final CellStyle dateStyle;

	/**  A holder for all shapes of a sheet, used to render comments. */
	private Drawing patriarch;

	/** The type provider for setting values. */
	private POITypeProvider typeProvider;

	/** Drawing manager for images in the excel file. */
	private POIDrawingManager drawingMgr;

	/** 
	 * Creates a {@link POIExcelValueSetter}.
	 */
	public POIExcelValueSetter(Workbook aWorkbook) {
		this(aWorkbook, new HashMap<>());
	}

	/**
	 * Creates a {@link POIExcelValueSetter}.
	 */
	public POIExcelValueSetter(Workbook aWorkbook, Map<String, Map<Integer, Integer>> aSheetMap) {
		this.workbook       = aWorkbook;
		this.sheetMap = aSheetMap;
		this.cellStyleCache = new HashMap<>();
		this.dateStyle      = POIExcelValueSetter.createDateStyle(aWorkbook);
		this.templates      = POIExcelValueSetter.parseTemplate(aWorkbook);
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
	 * Set the given value to the referenced cell in the workbook provided by the constructor.
	 * 
	 * @param    aRef      The reference to the requested cell.
	 * @param    aValue    The value to be stored.
	 */
	public void setValue(CellPosition aRef, ExcelValue aValue) {
		Cell   theCell  = this.resolveCell(aRef);
		String theSheet = aRef.getSheet();

		this.handleMergeRegion(theSheet, aValue); 
        this.handleComment(theSheet, theCell, aValue);

        int theWidth = this.handleSetCellValue(theCell, aValue);

		this.setCellStyle(theCell, aRef, aValue);
        this.storeColumnWidth(aRef, theWidth);
	}

    /** 
     * Return the cell defined by the given cell reference.
     * 
     * @param    aSheet    The sheet to find the cell.
     * @param    aRow      The requested row.
     * @param    aColumn   The requested column.
     * @return   The requested cell.
     * @see      POIUtil#createIfNull(String, Workbook)
     * @see      POIUtil#createIfNull(int, Sheet)
     * @see      POIUtil#createIfNull(int, Row, Sheet)
     */
	public Cell resolveCell(String aSheet, int aRow, int aColumn) {
    	String theName = this.getSheetNameNotNull(aSheet);
    	Sheet  theSheet = POIUtil.createIfNull(theName, this.workbook);
		Row    theRow   = POIUtil.createIfNull(aRow, theSheet);

		return POIUtil.createIfNull(aColumn, theRow, theSheet);
    }

	/** 
	 * Auto fit the workbook.
	 */
	public void performAutoAdjust() {
        POIUtil.setAutoFitWidths(this.workbook, this.sheetMap);
	}

	/** 
	 * Return a value sheet name (also when the given name is <code>null</code>).
	 * 
	 * @param aSheet    The sheet name as defined by the calling method, may be <code>null</code>.
	 * @return    The given name or the first sheet of the {@link #workbook}.
	 */
	public String getSheetNameNotNull(String aSheet) {
		return (aSheet != null) ? aSheet : this.workbook.getSheetName(0);
	}

	/** 
	 * Handle {@link ExcelValue#getMergeRegion()} value (if there is one).
	 * 
	 * <p>This method will only create a merge region, when the {@link ExcelValue#getMergeRegion() method} returns one.</p>
	 * 
	 * @param aSheet    The sheet to merge regions.
	 * @param aValue    The value containing a potential merge region.
	 */
	protected void handleMergeRegion(String aSheet, ExcelValue aValue) {
        MergeRegion theMergeRegion = aValue.getMergeRegion();

        if (theMergeRegion != null) {
			CellRangeAddress theAdress = theMergeRegion.createCellRangeAddress();
			Sheet            theSheet  = sheet(aSheet);

			theSheet.addMergedRegion(theAdress); 

			for (int theCol = theAdress.getFirstColumn(); theCol <= theAdress.getLastColumn(); theCol++) { 
			    for (int theRow = theAdress.getFirstRow(); theRow <= theAdress.getLastRow(); theRow++) { 
					Cell theMergeCell = this.resolveCell(aSheet, theRow, theCol);

			        this.setCellStyle(theMergeCell, aSheet, aValue); 
			    } 
			}
        }
	}

	private Sheet sheet(String sheetName) {
		return sheetName == null ? this.workbook.getSheetAt(0) : this.workbook.getSheet(sheetName);
	}

	/** 
	 * Handle {@link ExcelValue#getComment()} value (if there is one).
	 * 
	 * <p>This method will only create a comment, when the {@link ExcelValue#getComment() method} returns a string.</p>
	 * 
	 * @param aSheet    The sheet to add the comment to.
	 * @param aCell     The cell to bind the comment to.
	 * @param aValue    The value containing a potential comment.
	 */
	protected void handleComment(String aSheet, Cell aCell, ExcelValue aValue) {
        String theComment = aValue.getComment();

        if (theComment != null) {
			this.setComment(aSheet, aCell, theComment, aValue.getCommentRegion(), aValue.getCommentFillColor());
        }
	}

	/** 
	 * Set the value in the given {@link ExcelValue} by using the matching {@link PoiTypeHandler}.
	 * 
	 * @param    aCell     The cell to set the value in.
	 * @param    aValue    The value to get the raw data from.
	 * @return   The length of the written value.
	 */
	protected int handleSetCellValue(Cell aCell, ExcelValue aValue) {
		Object         theValue   = aValue.getValue();
		PoiTypeHandler theHandler = this.getTypeProvider().getPOITypeHandler(theValue);
    	int            theResult  = theHandler.setValue(aCell, this.workbook, theValue, this);

		return aValue.isAutoWidth() ? theResult : aValue.getCellWidth();
	}

	/**
	 * Applies cell styles from the given excelValue to the given cell.
	 * 
	 * @param sheet
	 *        sheet, where the {@link ExcelValue} belongs to
	 * @param excelValue
	 *        the {@link ExcelValue} holding the desired style information
	 * @param styleTemplate
	 *        the style which shall be used as template of returned style, may be null
	 */
	protected CellStyle createCellStyle(String sheet, ExcelValue excelValue, CellStyle styleTemplate) {
		if (excelValue.hasReferenceStyle()) {
			Logger.warn("Streaming export cannot apply cell styles, provided by referenced cells! Using default cell style!", POIExcelValueSetter.class);

			return POIUtil.createCopyOfMasterStyle(this.workbook);
		}

		return POIUtil.createCellStyle(this.workbook, excelValue, styleTemplate);
	}

    /**
     * Sets the comment of the given cell to the given value.
     */
    protected void setComment(String aSheetName, Cell aCell, String aComment, MergeRegion aCommentRegion, Color aFillColor) {
        if (this.patriarch == null) {
        	this.patriarch = sheet(aSheetName).createDrawingPatriarch();	
        }

		if (aCommentRegion == null) {
		    int cellRow = aCell.getRowIndex(), cellCol = aCell.getColumnIndex();
		    int ammount = StringServices.count(aComment, "\n");
		    aCommentRegion = new MergeRegion(cellRow, cellCol + 1, cellRow + ammount + 1, cellCol + 5);
		}

        Comment comment = patriarch.createCellComment(patriarch.createAnchor(10, 10, 10, 10,
                (short)aCommentRegion.getFromCol(), aCommentRegion.getFromRow(),
                (short)(aCommentRegion.getToCol()+1), (aCommentRegion.getToRow()+1)));
		comment.setString(POIUtil.newRichTextString(this.workbook, aComment));
		if (aFillColor != null && comment instanceof HSSFShape) {
            short[] color = POIUtil.getNearestColor(aFillColor).getTriplet();
			((HSSFShape) comment).setFillColor(color[0], color[1], color[2]);
        }
        aCell.setCellComment(comment);
	}

    /** 
     * Return the cell defined by the given cell reference.
     * 
     * @param    aRef    The reference to get the need information from.
     * @return   The requested cell.
     * @see      #resolveCell(String, int, int)
     */
	protected Cell resolveCell(CellPosition aRef) {
		return this.resolveCell(aRef.getSheet(), aRef.getRow(), aRef.getColumn());
	}

	/** 
	 * Create the type provider to be used by this setter.
	 * 
	 * @return    The requested type provider, never <code>null</code>.
	 */
	protected POITypeProvider createTypeProvider() {
		return new POITypeProvider();
	}

	private POITypeProvider getTypeProvider() {
		if (this.typeProvider == null) {
			this.typeProvider = this.createTypeProvider();
		}

		return this.typeProvider;
	}

	private void setCellStyle(Cell aCell, CellPosition aRef, ExcelValue anExcelValue) {
		this.setCellStyle(aCell, aRef.getSheet(), anExcelValue); 
	}

	private void setCellStyle(Cell aCell, String aSheet, ExcelValue anExcelValue) {
		if (anExcelValue != null) {
			if (anExcelValue.hasReferenceStyle()) {
	            this.setReferenceStyle(aSheet, aCell, anExcelValue.getCellStyleFrom());
	        }
	        else {
				CellStyle currentCellStyle = aCell.getCellStyle();
				/* Include current cell style into the cache key because the Excel-Template can have
				 * different template styles for different cells. In such case the styles must not
				 * be shared. */
				Tuple theStyleKey = TupleFactory.newTuple(aSheet, currentCellStyle, anExcelValue.getStyleKey());
				CellStyle theStyle;

				if (this.cellStyleCache.containsKey(theStyleKey)) {
					theStyle = this.cellStyleCache.get(theStyleKey);
				}
				else {
					theStyle = this.createCellStyle(aSheet, anExcelValue, currentCellStyle);
					this.cellStyleCache.put(theStyleKey, theStyle);
				}

				aCell.setCellStyle(theStyle);
	        }
		}
	}

	private void setReferenceStyle(String aSheetName, Cell aCell, CellPosition aCellPosition) {
		Cell theRefCell = this.resolveCell(aSheetName, aCellPosition.getRow(), aCellPosition.getColumn());

		if (theRefCell != null) {
            aCell.setCellStyle(theRefCell.getCellStyle());
        }
    }

    private void storeColumnWidth(CellPosition aRef, int aColumnWidth) {
    	if (aColumnWidth > 0) {
	        Map<Integer, Integer> theMap    = this.findColumnMap(aRef);
	        Integer               theColumn = Integer.valueOf(aRef.getColumn());
	        Integer               theWidth  = theMap.get(theColumn);

	        /* If the value of the map is shorter, store the new value in the map. */
	        if ((theWidth == null) || (theWidth.intValue() < aColumnWidth)) {
				theMap.put(theColumn, aColumnWidth);
	        }
    	}
    }

	private Map<Integer, Integer> findColumnMap(CellPosition aRef) {
		/* Ensure the sheet name is not null. */
		String theSheetName = this.getSheetNameNotNull(aRef.getSheet());

		/* Locate the map that holds the column widths for the given sheet. */
		Map<Integer, Integer> theColumnWidthMap = this.sheetMap.get(theSheetName);

		/* If non could be found, create a new one and store it. */
		if (theColumnWidthMap == null) {
		    theColumnWidthMap = new HashMap<>();
		    this.sheetMap.put(theSheetName, theColumnWidthMap);
		}

		return theColumnWidthMap;
	}

	/** 
	 * Create the default date style for the given workbook.
	 * 
	 * @param    aWorkbook    The workbook to get the cell style for.
	 * @return   The requested style, never <code>null</code>.
	 * @see      ExcelValue#DATE_FORMAT_STANDARD
	 */
	public static CellStyle createDateStyle(Workbook aWorkbook) {
        CellStyle theDateStyle = aWorkbook.createCellStyle();

        theDateStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat(ExcelValue.DATE_FORMAT_STANDARD));

        return theDateStyle;
    }

	/** 
	 * Parse the {@link POIExcelTemplate}s for the given workbook.
	 * 
	 * @param    aWorkbook    The workbook to get the templates from.
	 * @return   The requested templates, never <code>null</code>.
	 */
    public static Map<Sheet, POIExcelTemplate> parseTemplate(Workbook aWorkbook) {
        Map<Sheet, POIExcelTemplate> theTemplates = new HashMap<>();

        for (int thePos = 0; thePos < aWorkbook.getNumberOfSheets(); thePos++) {
               Sheet theSheet = aWorkbook.getSheetAt(thePos);

               theTemplates.put(theSheet, new POIExcelTemplate(theSheet));
        }

        return theTemplates;
    }
}

