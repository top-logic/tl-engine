/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.style.FontProperties;
import com.top_logic.base.office.style.TextOffset;
import com.top_logic.base.office.style.Underline;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.util.TLContext;

/**
 * Utility class for working with POI.
 * 
 * @author cdo
 */
public class POIUtil {

	/**
	 * File suffix for excel files in format 2007.
	 */
	public static final String XLSX_SUFFIX = ".xlsx";

	/**
	 * File suffix for excel files in format 2003.
	 */
	public static final String XLS_SUFFIX = ".xls";

	/* ---------------------------------------------------------------------- * Static Attributes
	 * ---------------------------------------------------------------------- */

	/** Number of CAHRS between 'A' and 'Z' */
	private static final int NUM_CHARS = 26;

	/** Default columns width */
	private static final int DEFAULT_COLUMN_WIDTH = 3200;

	/**
	 * Maximum columns width. This number was found in
	 * org.apache.poi.hssf.model.Sheet.setColumnWidth(columnIndex, width)
	 */
	private static final int MAX_COLUMN_WIDTH = 65280;

	/* ---------------------------------------------------------------------- * General methods
	 * ---------------------------------------------------------------------- */

	/**
	 * Formats the cell to display its value as a date.
	 * 
	 * @param aCell
	 *        the cell to format
	 * @param aWorkbook
	 *        is needed to be able to create the styling for the cell
	 */
	public static void formatCellToCurrency(Cell aCell, Workbook aWorkbook) {
		formatCell(aCell, aWorkbook, "#,##0.00");
	}

	/**
	 * Formats the cell to display its value as a date.
	 * 
	 * @param aCell
	 *        the cell to format
	 * @param aWorkbook
	 *        is needed to be able to create the styling for the cell
	 * @param aFormat
	 *        the format to use for the date, see org.apache.poi.ss.usermodel.BuiltinFormats
	 */
	public static void formatCellToDate(Cell aCell, Workbook aWorkbook, String aFormat) {
		if (aFormat == null) {
			aFormat = ExcelValue.DATE_FORMAT_STANDARD;
		}
		formatCell(aCell, aWorkbook, aFormat);
	}

	/**
	 * This method formats the given cell with the given format. The workbook is needed to be able
	 * to create the styling for the cell.
	 * 
	 * @param aCell
	 *        The cell to format.
	 * @param aWorkbook
	 *        A workbook.
	 * @param aFormat
	 *        A format (e.g. 'm/d/yy').
	 */
	private static void formatCell(Cell aCell, Workbook aWorkbook, String aFormat) {
		CellStyle theCellStyle = aCell.getCellStyle();
		theCellStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat(aFormat));
	}

	/**
	 * For every sheet in the given workbook, sets all columns to the precalculated autofit widths.
	 * 
	 * @param aWorkbook
	 *        the workbook to set the column widths
	 * @param aSheetMap
	 *        the map holding all column widths for all sheets
	 */
	public static void setAutoFitWidths(Workbook aWorkbook,
			Map<String, Map<Integer, Integer>> aSheetMap) {
		for (int i = 0, size = aWorkbook.getNumberOfSheets(); i < size; i++) {
			Sheet theSheet = aWorkbook.getSheetAt(i);

			for (int j = 0, last = calculateLastColumnNumber(theSheet); j <= last; j++) {
				int widthToSet = DEFAULT_COLUMN_WIDTH;
				Map<Integer, Integer> theColumnWidthMap = aSheetMap.get(aWorkbook.getSheetName(i));
				if (theColumnWidthMap != null) {
					Integer theWidthInteger = theColumnWidthMap.get(Integer.valueOf(j));
					if (theWidthInteger != null) {
						widthToSet = theWidthInteger.intValue() * 260 + 1000;
					}
				}
				// maximum column width, otherwise POI / excel will fail
				if (widthToSet > MAX_COLUMN_WIDTH)
					widthToSet = MAX_COLUMN_WIDTH;
				theSheet.setColumnWidth(j, widthToSet);
			}
		}
	}

	/**
	 * Calculates the last column number in a sheet
	 * 
	 * @param aSheet
	 *        the sheet to look for the last column number
	 * @return the last column number
	 */
	public static int calculateLastColumnNumber(Sheet aSheet) {
		int theBuf = -1;

		for (Iterator<Row> theRowIterator = aSheet.rowIterator(); theRowIterator.hasNext();) {
			Row theRow = theRowIterator.next();
			int theLastColumnNumber = theRow.getLastCellNum();

			if (theLastColumnNumber > theBuf) {
				theBuf = theLastColumnNumber;
			}
		}

		return theBuf;
	}

	/**
	 * This method returns the sheet index of the given sheet in the given workbook. If the workbook
	 * does not contains the sheet <code>-1</code> is returned.
	 * 
	 * @param aWorkbook
	 *        A {@link Workbook}.
	 * @param aSheet
	 *        A {@link Sheet}.
	 * @return Returns the sheet index of the given sheet in the given workbook.
	 */
	public static int getSheetIndex(Workbook aWorkbook, Sheet aSheet) {
		int index = -1;
		for (int i = 0; i < aWorkbook.getNumberOfSheets(); i++) {
			if (aWorkbook.getSheetAt(i).equals(aSheet)) {
				index = i;
				break;
			}
		}
		return index;
	}

	/* ---------------------------------------------------------------------- * Getter that create
	 * if value is null ---------------------------------------------------------------------- */

	/**
	 * Queries a sheet in the workbook. If the sheet does not exits, it is created.
	 * 
	 * @param aSheetname
	 *        the name of the sheet to query
	 * @param aWorkbook
	 *        the workbook, where the sheet is in
	 * @return the sheet
	 */
	public static Sheet createIfNull(String aSheetname, Workbook aWorkbook) {
		Sheet theSheet = aWorkbook.getSheet(aSheetname);

		if (theSheet == null) {
			theSheet = aWorkbook.createSheet(aSheetname);
		}

		return theSheet;
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
	public static Row createIfNull(int aRowNumber, Sheet aSheet) {
		Row theRow = aSheet.getRow(aRowNumber);

		if (theRow == null) {
			theRow = aSheet.createRow(aRowNumber);
		}

		return theRow;
	}

	/**
	 * Queries a cell in the row. If the cell does not exits, it is created.
	 * 
	 * @param aCellNumber
	 *        the cell number in the row
	 * @param aRow
	 *        the row containing the cell
	 * @return the cell
	 */
	public static Cell createIfNull(int aCellNumber, Row aRow) {
		return (POIUtil.createIfNull(aCellNumber, aRow, null));
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
	 * @param aSheet
	 *        The sheet to create the cell in, may be <code>null</code>.
	 * @return The requested cell,
	 */
	public static Cell createIfNull(int aCellNumber, Row aRow, Sheet aSheet) {
		Cell theCell = aRow.getCell(aCellNumber);

		if (theCell == null) {
			theCell = aRow.createCell(aCellNumber);
		}

		return theCell;
	}

	/* ---------------------------------------------------------------------- * Reading and Storing
	 * Workbooks. ---------------------------------------------------------------------- */

	/**
	 * Gets a workbook from the given source. If the source is <code>null</code> a new workbook will
	 * be created.
	 * 
	 * @param aSource
	 *        the source to read the workbook from, can be <code>null</code>
	 * @return the workbook from the source or a new workbook
	 * @throws IOException
	 *         if an error occurred while reading from the source
	 */
	public static Workbook getWorkbook(InputStream aSource) throws IOException {
		return POIUtil.getWorkbook(aSource, ExcelAccess.XLS_EXT);
	}

	/**
	 * Gets a workbook from the given source. If the source is <code>null</code> a new workbook will
	 * be created.
	 * 
	 * @param aSource
	 *        the source to read the workbook from, can be <code>null</code>
     * @param    anExt     The extension to be used when opening the work book ({@link POIUtil#XLS_SUFFIX} or {@link POIUtil#XLSX_SUFFIX}).
	 * @return the workbook from the source or a new workbook
	 * @throws IOException
	 *         if an error occurred while reading from the source
	 */
	public static Workbook getWorkbook(InputStream aSource, String anExt) throws IOException {
		if (aSource != null) {
			if (ExcelAccess.XLS_EXT.equals(anExt)) {
				POIFSFileSystem thePoiFs = new POIFSFileSystem(aSource);

				return new HSSFWorkbook(thePoiFs);
			}
			else {
				return new XSSFWorkbook(aSource);
			}
		}
		else {
			return new HSSFWorkbook();
		}
	}

	/**
	 * Updates formular in all cells and writes the workbook Writes the given workbook to the given
	 * file.
	 * 
	 * @param aDest
	 *        the file that the workbook will be written to
	 * @param theWorkbook
	 *        the workbook to save
	 * @throws FileNotFoundException
	 *         if the file could not be found
	 * @throws IOException
	 *         if there was an error writing to the file
	 */
	public static void writeWorkbook(File aDest, Workbook theWorkbook)
			throws FileNotFoundException, IOException {
		updateFormulaCells(theWorkbook);
		doWriteWorkbook(aDest, theWorkbook);
	}

	/**
	 * writes the workbook to the file
	 * 
	 * @param  aDest        The destination file to write the data to.
	 * @param  aWorkbook    The workbook containing the data to be written.
	 * @throws FileNotFoundException    When given destination doesn't exists. 
	 * @throws IOException              When writing fails.
	 */
	public static void doWriteWorkbook(File aDest, Workbook aWorkbook)
			throws FileNotFoundException, IOException {
		// Write the workbook
		try (FileOutputStream theOut = new FileOutputStream(aDest)) {
			aWorkbook.write(theOut);
		}
	}

	/**
	 * This methods updates all cells with a formula of all sheets of the given workbook.
	 * 
	 * @param aWorkbook
	 *        The workbook to update. Must not be <code>null</code>.
	 */
	public static void updateFormulaCells(Workbook aWorkbook) {
		int numberOfSheets = aWorkbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			Sheet theSheet = aWorkbook.getSheetAt(i);
			for (Iterator<Row> theRowIterator = theSheet.rowIterator(); theRowIterator.hasNext();) {
				Row theRow = theRowIterator.next();

				for (Iterator<Cell> theCellIterator = theRow.cellIterator(); theCellIterator.hasNext();) {
					Cell theCell = theCellIterator.next();
					if (theCell.getCellType() == CellType.FORMULA) {
						theCell.setCellFormula(theCell.getCellFormula());
					}
				}
			}
		}
	}

	/* ---------------------------------------------------------------------- * Extraction Methods
	 * ---------------------------------------------------------------------- */

	/**
	 * This method returns the excel column name as a string (e.g. 'A' or 'AD').
	 * 
	 * @return Returns the excel column name as a string.
	 */
	public static String getExcelColumnName(int aColumn) {
		if (aColumn < NUM_CHARS) {
			return getExcelChar(aColumn);
		}
		int theFirst = ((aColumn) / NUM_CHARS) - 1;
		int theSecond = ((aColumn) % NUM_CHARS);
		return getExcelChar(theFirst) + getExcelChar(theSecond);
	}

	/**
	 * This method returns the excel char for the given mod (e.g. 'A' or 'F').
	 * 
	 * @param aMod
	 *        A mod.
	 * @return Returns the excel char for the given mod.
	 */
	private static final String getExcelChar(int aMod) {
		return String.valueOf((char) ('A' + aMod));
	}

	/**
	 * Extracts the sheet name from the full qualified cell name. If the full cell name does not
	 * contain a sheet name this method returns <code>null</code>.
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre>
	 * "Sheet1!A2" --> "Sheet1"
	 * "A2"        -->  null
	 * </pre>
	 * 
	 * @param aFullCellName
	 *        the full qualified cell name
	 * @return the sheet name or <code>null</code>
	 */
	public static String extractSheetName(String aFullCellName) {
		int theIndex = aFullCellName.indexOf('!');
		String theSheetName = null;

		if (theIndex != -1) {
			theSheetName = aFullCellName.substring(0, theIndex);
		}

		return theSheetName;
	}

	/**
	 * Extracts the cellname from the full qualified cell name without the sheet information.
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre>
	 * "Sheet1!A2" --> "A2"
	 * "A2"        --> "A2"
	 * </pre>
	 * 
	 * @param aFullCellName
	 *        the full qualified cell name
	 * @return the cell name
	 */
	public static String extractCellName(String aFullCellName) {
		int theIndex = aFullCellName.indexOf('!');
		String theCellName;

		if (theIndex == -1) {
			/* If no sheet name is provided create a default sheet. */
			theCellName = aFullCellName;
		} else {
			theCellName = aFullCellName.substring(theIndex + 1);
		}

		return theCellName;
	}

	/* ---------------------------------------------------------------------- * Conversion Methods
	 * ---------------------------------------------------------------------- */

	/**
	 * Converts the cell name in a row and column value. This method expects a cell name consisting
	 * of a column description of uppercase letters and a row description of digits.
	 * <p>
	 * <b>Note: </b> The cell name values are expected to be 1 based and the returning values are 0
	 * based.
	 * </p>
	 * 
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre>
	 * "A2"  --> [1,0]
	 * "F15" --> [14,5]
	 * </pre>
	 * 
	 * <p>
	 * This function is fast but not robust.
	 * </p>
	 * 
	 * @param aCellName
	 *        the cell name containing the column and row description
	 * @return a one dimension array of two values; the row information at index 0 and the column
	 *         information at index 1
	 */
	public static int[] convertCellName(String aCellName) {
		boolean firstNonDigit = true;
		int theFactor = 1;

		/* Because the rows and columns are 0 based we have to start with -1 */
		int theRow = -1;
		int theColumn = -1;

		/* The following for loop extract from the cell name (B4 e.g.) the row and column value as
		 * an integer. Therefore the string is evaluated backwards. Until the first character that
		 * is a non digit the row number is extracted by converting the digits to a number. After
		 * that the same procedure is used to convert the cell name to a number. */
		for (int i = aCellName.length() - 1; i >= 0; i--) {
			char theChar = aCellName.charAt(i);

			if ('!' == theChar) {
				break;
			}
			if (Character.isDigit(theChar)) {
				theRow += (theChar - '0') * theFactor;
				theFactor *= 10;
			}
			else {
				if (firstNonDigit) {
					firstNonDigit = false;
					theFactor = 1;
				}

				theColumn += (theChar - '@') * theFactor;
				theFactor *= 26;
			}
		}

		return new int[] { theRow, theColumn };
	}

	/**
	 * Converts the cell name in a row value.
	 * 
	 * @param aCellName
	 *        the cell name to convert
	 * @return the row of the given cell
	 * @see #convertCellName(String)
	 */
	public static int getRow(String aCellName) {
		return StringServices.isEmpty(aCellName) ? -1 : POIUtil.convertCellName(aCellName)[0];
	}

	/**
	 * Converts the cell name in a column value.
	 * 
	 * @param aCellName
	 *        the cell name to convert
	 * @return the column of the given cell
	 * @see #convertCellName(String)
	 */
	public static int getColumn(String aCellName) {
		return StringServices.isEmpty(aCellName) ? -1 : POIUtil.convertCellName(aCellName)[1];
	}

	/**
	 * Converts the given cell location information into a string name. The given sheet name maybe
	 * <code>null</code> or empty.
	 * 
	 * @param aSheetName
	 *        the sheet name where the cell is located
	 * @param aRow
	 *        the row number of the cell
	 * @param aColumn
	 *        the column number of the cell
	 * @return the string cell name
	 */
	public static String convertToCellName(String aSheetName, int aRow, int aColumn) {
		/* Append the sheet name. */
		if (StringServices.isEmpty(aSheetName)) {
			return convertToCellName(aRow, aColumn);
		}
		StringBuffer theCellName = new StringBuffer(16 + aSheetName.length());
		theCellName.append(aSheetName).append('!')
			/* Append the cell number. */
			.append(toColumnName(aColumn))
			/* Append the row number. */
			.append(aRow + 1);

		return theCellName.toString();
	}

	/**
	 * Converts the given cell location information into a string name.
	 * 
	 * @param aRow
	 *        the row number of the cell
	 * @param aColumn
	 *        the column number of the cell
	 * @return the string cell name
	 */
	public static String convertToCellName(int aRow, int aColumn) {
		StringBuffer theCellName = new StringBuffer();

		/* Append the cell number. */
		theCellName.append(toColumnName(aColumn))
			/* Append the row number. */
			.append(aRow + 1);

		return theCellName.toString();
	}

	/* ---------------------------------------------------------------------- * Other Methods
	 * ---------------------------------------------------------------------- */

	/**
	 * This method returns for a given Color the nearest {@link HSSFColor} in the available color
	 * palette. Maybe <code>null</code>.
	 * 
	 * @param anAwtColor
	 *        An AWT color. Must NOT be <code>null</code>.
	 */
	public static HSSFColor getNearestColor(Color anAwtColor) {
		HSSFColor theColor = null;
		Map<?, ?> theTriplets = HSSFColor.getTripletHash();
		if (theTriplets != null) {
			Collection<?> theKeys = theTriplets.keySet();
			if (theKeys != null && theKeys.size() > 0) {
				HSSFColor theCrtColor = null;
				short[] theRgb = null;
				int theDiff = 0;
				int theMinDiff = 999;
				for (Iterator<?> theIter = theKeys.iterator(); theIter.hasNext();) {
					Object theKey = theIter.next();

					theCrtColor = (HSSFColor) theTriplets.get(theKey);
					theRgb = theCrtColor.getTriplet();

					theDiff = Math.abs(theRgb[0] - anAwtColor.getRed())
						+ Math.abs(theRgb[1] - anAwtColor.getGreen())
						+ Math.abs(theRgb[2] - anAwtColor.getBlue());

					if (theDiff < theMinDiff) {
						theMinDiff = theDiff;
						theColor = theCrtColor;
					}
				}
			}
		}

		return theColor;
	}

	/**
	 * This method copies all font values from the source font to the target font.
	 * 
	 * @param aSource
	 *        A source font. Must NOT be <code>null</code>.
	 * @param aTarget
	 *        A target font. Must NOT be <code>null</code>.
	 */
	public static void copyFont(Font aSource, Font aTarget) {
		aTarget.setBold(aSource.getBold());
		aTarget.setCharSet(aSource.getCharSet());
		aTarget.setColor(aSource.getColor());
		aTarget.setFontHeight(aSource.getFontHeight());
		aTarget.setFontHeightInPoints(aSource.getFontHeightInPoints());
		aTarget.setFontName(aSource.getFontName());
		aTarget.setItalic(aSource.getItalic());
		aTarget.setStrikeout(aSource.getStrikeout());
		aTarget.setTypeOffset(aSource.getTypeOffset());
		aTarget.setUnderline(aSource.getUnderline());
	}

	/**
	 * This method gets or creates a font with the properties given in the source font.
	 * 
	 * @param aBook
	 *        the workbook to search and create the font in
	 * @param aSource
	 *        A source font. Must NOT be <code>null</code>.
	 */
	public static Font getOrCreateFont(Workbook aBook, Font aSource) {
		Font workbookFont = getEquivalentWorkbookFont(aBook, aSource);
		if (workbookFont != null) {
			return workbookFont;
		}

		Font theFont = aBook.createFont();
		copyFont(aSource, theFont);
		return theFont;
	}
	
	private static Font getEquivalentWorkbookFont(Workbook workbook, Font fontToCheck) {
		for (short fontIdx = 0; fontIdx < workbook.getNumberOfFonts(); fontIdx++) {
			Font workbookFont = workbook.getFontAt(fontIdx);
			if (fontsEqual(workbookFont, fontToCheck)) {
				return workbookFont;
			}
		}
		return null;
	}

	/**
	 * true, if the fonts are equal, false otherwise
	 */
	public static boolean fontsEqual(Font firstFont, Font secondFont) {
		return firstFont.getFontName().equals(secondFont.getFontName())
			&& firstFont.getBold() == secondFont.getBold()
			   && firstFont.getCharSet() == secondFont.getCharSet()
			   && firstFont.getColor() == secondFont.getColor()
			   && firstFont.getFontHeight() == secondFont.getFontHeight()
			   && firstFont.getFontHeightInPoints() == secondFont.getFontHeightInPoints()
			   && firstFont.getItalic() == secondFont.getItalic()
			   && firstFont.getStrikeout() == secondFont.getStrikeout()
			   && firstFont.getTypeOffset() == secondFont.getTypeOffset()
			   && firstFont.getUnderline() == secondFont.getUnderline();
	}

	/**
	 * This method copies all style values from the source style to the target style. Both styles
	 * must belong to the same workbook!
	 * 
	 * @param aSource
	 *        A source style. Must NOT be <code>null</code>.
	 * @param aTarget
	 *        A target style. Must NOT be <code>null</code>.
	 * @param aWorkbook
	 *        The workbook these styles belong to.
	 */
	public static void copyStyle(CellStyle aSource, CellStyle aTarget, Workbook aWorkbook) {
		aTarget.setAlignment(aSource.getAlignment());
		aTarget.setBorderBottom(aSource.getBorderBottom());
		aTarget.setBorderLeft(aSource.getBorderLeft());
		aTarget.setBorderRight(aSource.getBorderRight());
		aTarget.setBorderTop(aSource.getBorderTop());
		aTarget.setBottomBorderColor(aSource.getBottomBorderColor());
		aTarget.setDataFormat(aSource.getDataFormat());
		aTarget.setFillBackgroundColor(aSource.getFillBackgroundColor());
		aTarget.setFillForegroundColor(aSource.getFillForegroundColor());
		aTarget.setFillPattern(aSource.getFillPattern());
		aTarget.setFont(aWorkbook.getFontAt(aSource.getFontIndex()));
		aTarget.setHidden(aSource.getHidden());
		aTarget.setIndention(aSource.getIndention());
		aTarget.setLeftBorderColor(aSource.getLeftBorderColor());
		aTarget.setLocked(aSource.getLocked());
		aTarget.setRightBorderColor(aSource.getRightBorderColor());
		aTarget.setRotation(aSource.getRotation());
		aTarget.setTopBorderColor(aSource.getTopBorderColor());
		aTarget.setVerticalAlignment(aSource.getVerticalAlignment());
		aTarget.setWrapText(aSource.getWrapText());
	}

	/* ---------------------------------------------------------------------- * Private Methods
	 * ---------------------------------------------------------------------- */
	public static String toColumnName(int columnId) {
		int remainder = columnId % 26;

		if ((columnId - remainder) == 0) {
			return Character.toString((char) (remainder + 0x41));
		}

		return toColumnName(((columnId - remainder) / 26) - 1) + Character.toString((char) (remainder + 0x41));
	}

	/**
	 * Converts border style information of {@link ExcelValue} to corresponding {@link CellStyle}
	 * value.
	 * 
	 */
	public static BorderStyle translateBorderToPOIShort(String border) {
		if (ExcelValue.BORDER_THIN.equals(border)) {
			return BorderStyle.THIN;
		} else if (ExcelValue.BORDER_DASHED.equals(border)) {
			return BorderStyle.DASHED;
		} else if (ExcelValue.BORDER_DOTTED.equals(border)) {
			return BorderStyle.DOTTED;
		} else if (ExcelValue.BORDER_THICK.equals(border)) {
			return BorderStyle.THICK;
		} else if (ExcelValue.BORDER_MEDIUM.equals(border)) {
			return BorderStyle.MEDIUM;
		} else if (ExcelValue.BORDER_HAIR.equals(border)) {
			return BorderStyle.HAIR;
		} else if (ExcelValue.BORDER_MEDIUM_DASHED.equals(border)) {
			return BorderStyle.MEDIUM_DASHED;
		} else if (ExcelValue.BORDER_MEDIUM_DASH_DOT.equals(border)) {
			return BorderStyle.MEDIUM_DASH_DOT;
		} else if (ExcelValue.BORDER_MEDIUM_DASH_DOT_DOT.equals(border)) {
			return BorderStyle.MEDIUM_DASH_DOT_DOT;
		} else if (ExcelValue.BORDER_DASH_DOT.equals(border)) {
			return BorderStyle.DASH_DOT;
		} else if (ExcelValue.BORDER_DASH_DOT_DOT.equals(border)) {
			return BorderStyle.DASH_DOT_DOT;
		} else if (ExcelValue.BORDER_SLANTED_DASH_DOT.equals(border)) {
			return BorderStyle.SLANTED_DASH_DOT;
		} else if (ExcelValue.BORDER_DOUBLE.equals(border)) {
			return BorderStyle.DOUBLE;
		}

		throw new IllegalArgumentException("Can not transform the border string ('" + border + "') in a POI border.");
	}

	/**
	 * Creates a copy of the {@link Workbook}`s master style.
	 * 
	 * @param aWorkbook
	 *        - container, from the master style will be retrieved
	 */
	public static CellStyle createCopyOfMasterStyle(Workbook aWorkbook) {
		CellStyle masterStyle = aWorkbook.getCellStyleAt((short) 0);
		CellStyle individualCellStyle = aWorkbook.createCellStyle();
		individualCellStyle.cloneStyleFrom(masterStyle);
		return individualCellStyle;
	}

	/**
	 * Creates a {@link CellStyle} from given {@link ExcelValue}
	 * 
	 * @param workbook
	 *        - workbook, to which the {@link ExcelValue} belongs to
	 * @param excelValue
	 *        - the style container, from which the {@link CellStyle} will be created
	 * @param styleTemplate
	 *        - base style information, which will be applied before excel value styles
	 */
	public static CellStyle createCellStyle(Workbook workbook, ExcelValue excelValue, CellStyle styleTemplate) {

		if (excelValue.hasIndividualStyle() || styleTemplate != null) {

			// Create a new cell style and set the colors. This avoids
			// that the cell style of others cells are changed too.
			CellStyle newStyle = createCopyOfMasterStyle(workbook);

			if (styleTemplate != null) {
				newStyle.cloneStyleFrom(styleTemplate);
			}

			// Transform the AWT color to an HSSF color and set it to
			// the new created cell style
			Color backgroundColor = excelValue.getBackgroundColor();
			if (backgroundColor != null) {
				if (workbook instanceof HSSFWorkbook) {
					HSSFColor hssfBackgroundColor = getNearestColor(backgroundColor);

					if (hssfBackgroundColor != null) {
						newStyle.setFillForegroundColor(hssfBackgroundColor.getIndex());
						newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					}
				}
				else {
					((XSSFCellStyle) newStyle).setFillForegroundColor(new XSSFColor(backgroundColor, null));
					newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
			}

			// Set cell alignment
			if (excelValue.getCellAlignment() != ExcelValue.NO_HORIZONTAL_ALIGNMENT_SET) {
				newStyle.setAlignment(excelValue.getCellAlignment());
			}
			if (excelValue.getVerticalAlignment() != ExcelValue.NO_VERTIVAL_ALIGNMENT_SET) {
				newStyle.setVerticalAlignment(excelValue.getVerticalAlignment());
			}
			if (excelValue.isTextWrap() != null) {
				newStyle.setWrapText(excelValue.isTextWrap());
			}

			// Set the border to the cell
			if (excelValue.getBorderTop() != null) {
				newStyle.setBorderTop(translateBorderToPOIShort(excelValue.getBorderTop()));
			}
			if (excelValue.getBorderRight() != null) {
				newStyle.setBorderRight(translateBorderToPOIShort(excelValue.getBorderRight()));
			}
			if (excelValue.getBorderBottom() != null) {
				newStyle.setBorderBottom(translateBorderToPOIShort(excelValue.getBorderBottom()));
			}
			if (excelValue.getBorderLeft() != null) {
				newStyle.setBorderLeft(translateBorderToPOIShort(excelValue.getBorderLeft()));
			}
			if (excelValue.getRotation() != 0) {
				newStyle.setRotation(excelValue.getRotation());
			}

			if (excelValue.hasDataFormat()) {
				DataFormat dataFormat = workbook.createDataFormat();
				short format = dataFormat.getFormat(excelValue.getDataFormat());
				newStyle.setDataFormat(format);
			} else {
				/* Date fields must have a different style, otherwise they would be displayed as a
				 * double value, cause internally it is a double. */
				if (excelValue.hasDateValue()) {
					DataFormat dataFormat = workbook.createDataFormat();
					short format = dataFormat.getFormat(excelValue.getDateFormat());
					newStyle.setDataFormat(format);
				}
			}

			if (excelValue.hasIndividualFontStyle()) {
				// Create a new font with the same properties.
				// Use the new created font with the specified properties.
				Font newFont = workbook.createFont();
				Font oldFont = workbook.getFontAt(newStyle.getFontIndex());
				copyFont(oldFont, newFont);
				applyFontProperties(excelValue, newFont);
				newStyle.setFont(newFont);
			}
			return newStyle;
		} else {
			// Return default cell style
			return workbook.getCellStyleAt((short) 0);
		}
	}

	/**
	 * Applies the given {@link FontProperties} to the given {@link Font}.
	 */
	public static void applyFontProperties(FontProperties config, Font font) {
		Color textColor = config.getTextColor();
		if (textColor != null) {
			HSSFColor hssfFontColor = getNearestColor(textColor);

			if (hssfFontColor != null) {
				font.setColor(hssfFontColor.getIndex());
			}
		}

		String fontName = config.getFontName();
		if (fontName != null) {
			font.setFontName(fontName);
		}

		Double fontSize = config.getFontSize();
		if (fontSize != null) {
			font.setFontHeight((short) Math.round(fontSize.doubleValue() * 20));
		}

		Decision isBold = config.isBold();
		if (isBold != Decision.DEFAULT) {
			font.setBold(isBold.toBoolean(false));
		}

		Underline isUnderline = config.getUnderline();
		if (isUnderline != Underline.DEFAULT) {
			font.setUnderline(toUnderline(isUnderline));
		}

		Decision isItalic = config.isItalic();
		if (isItalic != Decision.DEFAULT) {
			font.setItalic(isItalic.toBoolean(false));
		}

		Decision isStrikeout = config.isStrikeout();
		if (isStrikeout != Decision.DEFAULT) {
			font.setStrikeout(isStrikeout.toBoolean(false));
		}

		TextOffset textOffset = config.getTextOffset();
		if (textOffset != null) {
			font.setTypeOffset(toTypeOffset(textOffset));
		}
	}

	private static byte toUnderline(Underline underline) {
		switch (underline) {
			case DEFAULT:
			case NONE:
				return Font.U_NONE;
			case SINGLE:
				return Font.U_SINGLE;
			case DOUBLE:
				return Font.U_DOUBLE;
			case SINGLE_ACCOUNTING:
				return Font.U_SINGLE_ACCOUNTING;
			case DOUBLE_ACCOUNTING:
				return Font.U_DOUBLE_ACCOUNTING;
		}
		throw new UnreachableAssertion("No such underline setting: " + underline);
	}

	private static short toTypeOffset(TextOffset textOffset) {
		switch (textOffset) {
			case DEFAULT:
			case NORMAL:
				return Font.SS_NONE;
			case SUB:
				return Font.SS_SUB;
			case SUPER:
				return Font.SS_SUPER;
		}
		throw new UnreachableAssertion("No such text offset: " + textOffset);
	}

	/**
	 * Creates a {@link FormulaEvaluator} appropriate for the given {@link Workbook} implementation.
	 * 
	 * @param workbook
	 *        The workbook to create {@link FormulaEvaluator} for.
	 * 
	 * @return a {@link FormulaEvaluator}.
	 * 
	 * @throws IllegalArgumentException
	 *         if given workbook is <code>null</code>.
	 * @throws UnsupportedOperationException
	 *         if no {@link FormulaEvaluator} can be created for given workbook.
	 */
	public static FormulaEvaluator newFormulaEvaluator(Workbook workbook) {
		if (workbook instanceof HSSFWorkbook) {
			return new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
		} else if (workbook instanceof XSSFWorkbook) {
			return new XSSFFormulaEvaluator((XSSFWorkbook) workbook);
		} else if (workbook instanceof SXSSFWorkbook) {
			return new XSSFFormulaEvaluator(((SXSSFWorkbook) workbook).getXSSFWorkbook());
		} else {
			if (workbook == null) {
				throw new IllegalArgumentException("Can not create formula evaluator for 'null' workbook.");
			}
			throw new UnsupportedOperationException("Can not produce forumal evaluator for workbook of class '"
				+ workbook.getClass().getCanonicalName() + "': " + workbook);
		}
	}

	/**
	 * Creates a {@link RichTextString} appropriate for the given {@link Workbook} implementation.
	 * 
	 * @param workbook
	 *        The workbook to create {@link RichTextString} for.
	 * 
	 * @return a {@link RichTextString}.
	 * 
	 * @throws IllegalArgumentException
	 *         if given workbook is <code>null</code>.
	 * @throws UnsupportedOperationException
	 *         if no {@link FormulaEvaluator} can be created for given workbook.
	 */
	public static RichTextString newRichTextString(Workbook workbook, String text) {
		if (workbook instanceof HSSFWorkbook) {
			return new HSSFRichTextString(text);
		} else if (workbook instanceof XSSFWorkbook) {
			return new XSSFRichTextString(text);
		} else if (workbook instanceof SXSSFWorkbook) {
			return new XSSFRichTextString(text);
		} else {
			if (workbook == null) {
				throw new IllegalArgumentException("Can not create rich text string for 'null' workbook.");
			}
			throw new UnsupportedOperationException("Can not produce rich text string for workbook of class '"
				+ workbook.getClass().getCanonicalName() + "': " + workbook);
		}
	
	}

	/**
	 * Creates a {@link RichTextString} appropriate for the given {@link Cell} implementation.
	 * 
	 * @param cell
	 *        The cell to create {@link RichTextString} for.
	 * 
	 * @return a {@link RichTextString}.
	 * 
	 * @throws IllegalArgumentException
	 *         if given cell is <code>null</code>.
	 * @throws UnsupportedOperationException
	 *         if no {@link FormulaEvaluator} can be created for given cell.
	 */
	public static RichTextString newRichTextString(Cell cell, String text) {
		if (cell instanceof HSSFCell) {
			return new HSSFRichTextString(text);
		} else if (cell instanceof XSSFCell) {
			return new XSSFRichTextString(text);
		} else if (cell instanceof SXSSFCell) {
			return new XSSFRichTextString(text);
		} else {
			if (cell == null) {
				throw new IllegalArgumentException("Can not create rich text string for 'null' cell.");
			}
			throw new UnsupportedOperationException("Can not produce rich text string for cell of class '"
				+ cell.getClass().getCanonicalName() + "': " + cell);
		}
	}

	/**
	 * Creates a new {@link Workbook} from the given content.
	 * 
	 * @param excelFileName
	 *        Name of the excel file to read data from. This name is used to determine the kind of
	 *        workbook to create.
	 * @param content
	 *        The content of the Excel file.
	 * 
	 * @return A {@link Workbook} with the content of the input stream.
	 * 
	 * @see #getFileSuffix(Workbook)
	 */
	public static Workbook newWorkbook(String excelFileName, InputStream content) throws IOException {
		if (excelFileName.endsWith(XLS_SUFFIX)) {
	        return new HSSFWorkbook(content);
		} else {
	        return new XSSFWorkbook(content);
	    }
	}

	/**
	 * Returns the file suffix to use for writing the given {@link Workbook}.
	 * 
	 * @param workbook
	 *        The workbook to write to a file.
	 * 
	 * @see #newWorkbook(String, InputStream)
	 */
	public static String getFileSuffix(Workbook workbook) {
		if (workbook instanceof HSSFWorkbook) {
			return XLS_SUFFIX;
		} else {
			return XLSX_SUFFIX;
		}
	}

	/**
	 * Ensures that POI uses the {@link TimeZone} of the <i>TopLogic</i> System
	 * ({@link TimeZones#systemTimeZone()}) and the {@link Locale} of the current user for the
	 * export and import.
	 * 
	 * <p>
	 * This call must be reverted after successful export or import using
	 * {@link #resetUserTimeZoneAndLocale()}
	 * </p>
	 * 
	 * @see #resetUserTimeZoneAndLocale()
	 */
	public static void setUserTimeZoneAndLocale() {
		LocaleUtil.setUserTimeZone(TimeZones.systemTimeZone());
		LocaleUtil.setUserLocale(TLContext.getLocale());
	}

	/**
	 * Resets the setting of the POI user {@link TimeZone} and {@link Locale} formerly set by
	 * {@link #setUserTimeZoneAndLocale()}.
	 * 
	 * @see #setUserTimeZoneAndLocale()
	 */
	public static void resetUserTimeZoneAndLocale() {
		LocaleUtil.resetUserLocale();
		LocaleUtil.resetUserTimeZone();
	}

}
