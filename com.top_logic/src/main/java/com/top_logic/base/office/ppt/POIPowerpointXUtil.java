/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFHyperlink;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import com.top_logic.base.office.AbstractOffice.FixedTableInformation;
import com.top_logic.base.office.AbstractOffice.ImageReplacerData;
import com.top_logic.base.office.style.FontStyle;
import com.top_logic.base.office.style.Underline;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.tool.export.ExportUtil;
import com.top_logic.util.Utils;

/**
 * Util class to replace tokens in the PPTX template.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class POIPowerpointXUtil {
	
	/**
	 * Text token suffixed with a token name. Will be replaced by a {@link String} or
	 * {@link HyperlinkDefinition} value.
	 */
	public static final String PREFIX_VALUE          = "VALUE_";

	/** Image token, suffixed with a token name. Will be replaced by an image given as a {@link File} */
	public static final String PREFIX_PICTURE        = "PICTURE_";

	/** Token to insert content of the first slide from the given file into the referring slide. */
	public static final String PREFIX_SLIDECONTENT = "SLIDECONTENT_";

	/**
	 * Table token to create a table from a single shape with a PptXTable value. TODO implement
	 * shape-based to handle pictures
	 */
	public static final String PREFIX_AUTOTABLE      = "AUTOTABLE_";

	/**
	 * Table area token suffixed with a token name and a List, Object[] or Object[][] value to fill
	 * a table area with values. Accepts {@link String}, {@link HyperlinkDefinition}, {@link Color},
	 * and {@link File} (for images) values.
	 */
	public static final String PREFIX_FIXEDTABLE     = "FIXEDTABLE_";

	/** Will be replaced by the final number of slides in the slide show */
	public static final String SLIDE_COUNT           = "SL_COUNT";

	/**
	 * If token is found in the TITLE of a slide, slide will be replaced by a
	 * {@link SlideReplacement} value, i.e. another slide show will be loaded, its tokens replaced
	 * and the resulting slides will be inserted in this slide show.
	 */
	public static final String PREFIX_ADDSLIDES = "ADDSLIDES_";


	/** Allows value prefixes */
	public static final String[] PREFIX_VALUES = new String[] {
		PREFIX_VALUE, PREFIX_PICTURE,
		PREFIX_AUTOTABLE, PREFIX_FIXEDTABLE,
		PREFIX_SLIDECONTENT
	};

	/** index of first cell in powerpoint) */
	public static final int FIRST_CELL = 1;
	
	/**
	 * Takes an array of {@link XSLFSlide}s and searches for a slide with the given title.
	 * 
	 * @param slides
	 *        an array of slides
	 * @param aTitle
	 *        a title
	 * @return a {@link XSLFSlide} with the given title or <code>null</code> if no {@link XSLFSlide}
	 *         with the given title was found.
	 */
	public static XSLFSlide getSlideByTitle(XSLFSlide[] slides, String aTitle) {
		if (aTitle == null) {
			return null;
		}
		for (int i = 0; i < slides.length; i++) {
			String title = extractTitle(slides[i]);
			if (title.equals(aTitle)) {
				return slides[i];
			}
		}
		return null;
	}

	/**
	 * Parses a given slide with its given shapes for tokens and replaces them with the respective
	 * values given in a token map.
	 * 
	 * @param aSlide
	 *        a {@link XSLFSlide} which should be parsed.
	 * @param someShapes
	 *        the shapes of the given slide
	 * @param someTokens
	 *        a {@link Map} of tokens to be used for replacement.
	 * @throws IOException
	 *         if the replacement of picture tokens goes wrong.
	 */
	public void parseForTokens(XSLFSheet aSlide, int slideIdx, List<XSLFShape> someShapes,
			Map<String, Object> someTokens,
			Map<XSLFTableCell, XSLFTable> someCells)
			throws IOException {
		Map<XSLFShape, Object> pictures = new HashMap<>();
		if (someCells == null) {
			someCells = new HashMap<>();
		}
		for (XSLFShape theShape : someShapes) {
			if (theShape instanceof XSLFTextShape) {
				XSLFTextShape shape = (XSLFTextShape) theShape;
				String theShapeText = shape.getText();
				List<String> theTokens = findTokens(theShapeText);
				if (!CollectionUtil.isEmptyOrNull(theTokens)) {
					boolean replaced = false;
					for (int i = 0; !replaced && i < theTokens.size(); i++) {
						String theToken = theTokens.get(i);
						Object theTokenValue = someTokens.get(theToken);
						replaced = replaceToken(aSlide, shape, theToken, theTokenValue, someCells);
					}
				}
			} else if (theShape instanceof XSLFTable) {
				parseTable(aSlide, slideIdx, someTokens, pictures, someCells, (XSLFTable) theShape);
			} else if (theShape instanceof XSLFGroupShape) {
				XSLFGroupShape theGrp = (XSLFGroupShape) theShape;
				List<XSLFShape> sh = POIPowerpointUtil.shapesStable(theGrp);
				parseForTokens(aSlide, slideIdx, sh, someTokens, someCells);
			}
			// other shapes are not eligible for token replacement
		}

		// Handle all pictures at the end
		if (!CollectionUtil.isEmptyOrNull(pictures)) {
			for (Iterator<XSLFShape> theIt = pictures.keySet().iterator(); theIt.hasNext();) {
				XSLFShape next = theIt.next();
				replacePicture(aSlide, next, pictures.get(next), someCells);
			}
		}
	}

	/**
	 * Parses a {@link XSLFTable}. If the table contains ONLY one token that starts with
	 * {@link #PREFIX_FIXEDTABLE} the cells starting with the row the token was will be replaced
	 * with the list of respective objects. Otherwise the table will be treated as a normal
	 * {@link XSLFGroupShape}. TODO don't understand why this is done that way...
	 * 
	 * @param aSlide
	 *        the slide the table is on
	 * @param someTokens
	 *        a {@link Map} of String tokens and objects the tokens shall be replaced with
	 * @param somePictures
	 *        a {@link Map} for shapes that shall be replaced with picture files
	 * @param aTable
	 *        the shape that shall be parsed as a table
	 * @throws IOException
	 *         if a picture token was encountered and its replacement failed
	 */
	private void parseTable(XSLFSheet aSlide, int slideIdx, Map<String, Object> someTokens,
			Map<XSLFShape, Object> somePictures, Map<XSLFTableCell, XSLFTable> someCells,
			XSLFTable aTable) throws IOException {
		int tableRow = 0;
		for (XSLFTableRow theRow : aTable.getRows()) {
			int tableCol = 0;
			for (XSLFTableCell cell : theRow.getCells()) {
				if (cell == null) {
					continue;
				}
				
				// Check for tokens
				List<String> theTokens = findTokens(cell.getText());
				if (!CollectionUtil.isEmptyOrNull(theTokens)) {
					String theToken = theTokens.get(0);
					if (theToken.startsWith(PREFIX_FIXEDTABLE)) {
						// Remark: this will alter the table! Further replacements aren't possible
						// (ConcurrentModificationException)
						replaceFixedTable(someTokens, somePictures, someCells, aTable, tableRow, tableCol, theToken);
						return;
					} else {
						// All normal tokens will be replaced in all cells of the table. So this
						// call will only be done once in a table
						replaceNormalTokensInTable(aSlide, slideIdx, someTokens, aTable, someCells);
					}
				}
				tableCol++;
			}
			tableRow++;
		}
	}

	/**
	 * Handle a table with a fixed table token by filling in values in the area beginning with a
	 * cell given by its coordinates (starting with 0).
	 * 
	 * @param someTokens
	 *        the tokens and values without the pictures
	 * @param somePictures
	 *        the picture tokens and values
	 * @param aTable
	 *        the table
	 * @param tableRow
	 *        the first row (starting with) where the replacement begins
	 * @param tableCol
	 *        the first column (starting with) where the replacement begins
	 * @param theToken
	 *        the token
	 */
	private void replaceFixedTable(Map<String, Object> someTokens, Map<XSLFShape, Object> somePictures,
			Map<XSLFTableCell, XSLFTable> someCells, XSLFTable aTable,
			int tableRow, int tableCol, String theToken) {
		List<Object> theTableAsList = getTableAsList(someTokens, theToken);
		if (CollectionUtil.isEmptyOrNull(theTableAsList)) {
			this.replaceText(this.getCellAt(aTable, tableRow, tableCol), theToken, ""); // remove
			return;
		}

		// iterator is not empty
		Iterator<Object> theIt = theTableAsList.iterator();

		// Create rows and columns table if necessary
		Object firstObject = theTableAsList.get(0);
		if (firstObject instanceof Object[]) {
			int tableRowNo = theTableAsList.size();
			int tableColNo = ((Object[]) firstObject).length;

			// Due to the problem that empty cells don't apply the cell style when content is added
			// we have to take care of that by applying the style of the token cell where necessary.

			// Initialize cell style in existing cells right of token cell in the same line
			XSLFTableCell tokenCell = getCellAt(aTable, tableRow, tableCol);
			PptXTableCell theTokenCell = new PptXTableCell(tokenCell, true);
			for (int col = tableCol + 1; col < tableCol + tableColNo; col++) {
				XSLFTableCell tableCell = getCellAt(aTable, tableRow, col);
				applyStyleIfNecessary(tableCell, theTokenCell);
			}

			// create rows
			int numberOfRows = aTable.getNumberOfRows();
			double rowHeight = aTable.getRows().get(numberOfRows - 1).getHeight();
			for (int i = numberOfRows; i < tableRowNo + tableRow; i++) {
				XSLFTableRow addRow = aTable.addRow();
				addRow.setHeight(rowHeight);
				int theCellNo = addRow.getCells().size();
				for (int k = theCellNo; k < tableColNo + tableCol; k++) {
					addRow.addCell();
				}
			}

			// create cells (in new rows and for additional columns)
			List<XSLFTableRow> rows = aTable.getRows();
			int newNumberOfRows = rows.size();
			int numberOfCols = rows.get(tableRow).getCells().size();
			int newNumberOfCols = Math.max(numberOfCols, tableCol + tableColNo);
			for (int row = 0; row < newNumberOfRows; row++) {
				XSLFTableRow theRow = rows.get(row);
				int theCellNo = theRow.getCells().size();
				boolean newRow = row >= numberOfRows;
				if (newRow) {
					for (int col = 0; col < newNumberOfCols; col++) {
						PptXTableCell aboveCell = new PptXTableCell(getCellAt(aTable, row - 1, col), true);
						applyStyleIfNecessary(getCellAt(aTable, row, col), aboveCell);
					}
				}
				else {
					for (int col = theCellNo; col < newNumberOfCols; col++) {
						XSLFTableCell addCell = theRow.addCell();
						PptXTableCell leftCell = new PptXTableCell(getCellAt(aTable, row, col - 1), true);
						applyStyleIfNecessary(addCell, leftCell);
					}
				}
			}
		}

		// set table row heights
		Object theTokenValue = someTokens.get(theToken);
		if (theTokenValue instanceof FixedTableInformation) {
			setTableRowHeights(aTable, (FixedTableInformation) theTokenValue);
		}

		// Fill in the values
		int row = 0;
		for (XSLFTableRow theInnerRow : aTable.getRows()) {
			if (row >= tableRow) {	// start at token row
				boolean isRowArray = false;
				Object object = null;
				int col = 0;
				for (XSLFTableCell cell2 : theInnerRow.getCells()) {
					if (col >= tableCol) {	// start at token column
						if (!isRowArray && theIt.hasNext()) { // get next token if we don't have a
																// row data
							object = theIt.next();
						}

						isRowArray = replaceFixedTableCell(cell2, aTable, tableRow, tableCol, row, col, object, somePictures, someCells);
						
						if (!theIt.hasNext() && !isRowArray) { // out of data
							deleteUnfilledTableRows(aTable, theTokenValue, row);
							return;
						}
					}
					col++;
				}
			}
			row++;
		}
	}

	/**
	 * Deletes the table rows that were not filled.
	 */
	protected void deleteUnfilledTableRows(XSLFTable aTable, Object theTokenValue, int row) {
		if (theTokenValue instanceof FixedTableInformation && ((FixedTableInformation) theTokenValue).getDeleteUnfilledRows()) {
			for (int rowToDelete = aTable.getRows().size() - 1; rowToDelete >= row; rowToDelete--) {
				aTable.getCTTable().removeTr(rowToDelete);
			}
		}
	}

	private boolean replaceFixedTableCell(XSLFTableCell cell2, XSLFTable aTable, int tableRow, int tableCol, int row, int col, Object object,
			Map<XSLFShape, Object> somePictures, Map<XSLFTableCell, XSLFTable> someCells) {

		if (object instanceof StyledValue) {
			StyledValue styledValue = (StyledValue) object;
			setBackgroundColor(cell2, styledValue);
			setTextWrap(cell2, styledValue);
			setTextRunValues(cell2, styledValue);

			object = styledValue.getValue();
		}
		if (object instanceof String) { // single text replacement
			insertText(cell2, (String) object);
		} else if (object instanceof File || object instanceof ImageReplacerData || object instanceof BinaryContent) {
			// single image replacement.
			somePictures.put(cell2, object);
			someCells.put(cell2, aTable);
		} else if (object instanceof Color) { // set color
			cell2.setFillColor((Color) object);
			if (row == tableRow && col == tableCol) {
				insertText(cell2, ExportUtil.EXPORT_EMPTY_CELL_VALUE); // replace FIXEDTABLE token
			}
		} else if (object instanceof HyperlinkDefinition) { // Set hyper link
			insertLink(cell2, (HyperlinkDefinition) object);
		} else if (object instanceof Object[]) { // row data object
			Object[] rowArray = (Object[]) object;
			if (col - tableCol < rowArray.length) {
				Object rowArrayValue = rowArray[col - tableCol];
				replaceFixedTableCell(cell2, aTable, tableRow, tableCol, row, col, rowArrayValue, somePictures, someCells);
			}
			return true;
		} else if (object instanceof PptXTableCell) {
			cell2.clearText();
			PptXTableCell tokenTableCell = (PptXTableCell) object;
			tokenTableCell.copy(cell2, true, true, true);
		} else if (object == null && row == tableRow && col == tableCol) {
			// replace FIXEDTABLE token
			insertText(cell2, StringServices.EMPTY_STRING);
		}

		return false;
	}

	private void setTextWrap(XSLFTableCell cell, StyledValue styledValue) {
		Boolean textWrap = styledValue.isTextWrap();
		if (textWrap != null) {
			cell.setWordWrap(textWrap.booleanValue());
		}
	}

	private void setBackgroundColor(XSLFTableCell cell, StyledValue styledValue) {
		Color backgroundColor = styledValue.getBackgroundColor();
		if (backgroundColor != null) {
			cell.setFillColor(backgroundColor);
		}
	}

	private void setTextRunValues(XSLFTableCell cell, StyledValue styledValue) {
		FontStyle style = styledValue.getFontStyle();
		if (style.isDefault()) {
			// escape because following loop will do nothing
			return;
		}

		String fontName = style.getFontName();
		Double fontSize = style.getFontSize();
		Color textColor = style.getTextColor();
		Decision isBold = style.isBold();
		Decision isItalic = style.isItalic();
		Decision strikesthrough = style.isStrikeout();
		Underline isUnderline = style.getUnderline();

		List<XSLFTextParagraph> textParagraphs = cell.getTextParagraphs();
		for (XSLFTextParagraph paragraph : textParagraphs) {
			List<XSLFTextRun> textRuns = paragraph.getTextRuns();
			for (XSLFTextRun run : textRuns) {
				if (fontName != null) {
					run.setFontFamily(fontName);
				}
				if (fontSize != null) {
					run.setFontSize(fontSize);
				}
				if (textColor != null) {
					run.setFontColor(textColor);
				}
				if (isBold.isDecided()) {
					run.setBold(isBold.toBoolean(false));
				}
				if (isItalic.isDecided()) {
					run.setItalic(isItalic.toBoolean(false));
				}
				if (isUnderline.isDecided()) {
					run.setUnderlined(isUnderline.isUnderline());
				}
				if (strikesthrough.isDecided()) {
					run.setStrikethrough(strikesthrough.toBoolean(false));
				}
			}
		}
	}

	private void applyStyleIfNecessary(XSLFTableCell aTableCell, PptXTableCell aTokenCell) {
		if (aTableCell == null || aTokenCell == null) {
			return;
		}

		List<XSLFTextParagraph> textParagraphs = aTableCell.getTextParagraphs();
		if (CollectionUtil.isEmptyOrNull(textParagraphs)
			|| CollectionUtil.isEmptyOrNull(textParagraphs.get(0).getTextRuns())) {
			aTokenCell.copy(aTableCell, true, false, false);
		}
	}

	private XSLFTableCell getCellAt(XSLFTable aTable, int aRow, int aCol) {
		if (aTable == null || aRow + 1 > aTable.getNumberOfRows() || aCol + 1 > aTable.getNumberOfColumns()) {
			return null;
		}

		return aTable.getRows().get(aRow).getCells().get(aCol);
	}

	/**
	 * Get the values to fill into a table (area) as a List. The values may come as an Object[][],
	 * or a List
	 * 
	 * @param someTokens
	 *        all tokens
	 * @param theToken
	 *        the token
	 * @return the values as a List as defined above
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> getTableAsList(Map<String, Object> someTokens, String theToken) {
		Object theTokenValue = someTokens.get(theToken);
		List<Object> theTableList = null;
		if (theTokenValue instanceof List) {
			theTableList = (List<Object>) theTokenValue;
		} else if (theTokenValue instanceof Object[]) {
			// Results in a List<Object[]>
			theTableList = Arrays.asList((Object[]) theTokenValue);
		} else if (theTokenValue instanceof FixedTableInformation) {
			theTableList = Arrays.asList(((FixedTableInformation) theTokenValue).getTableValues());
		}

		return theTableList;
	}

	/**
	 * Sets the table row heights provided by the given {@link FixedTableInformation}.
	 */
	protected void setTableRowHeights(XSLFTable aTable, FixedTableInformation fixedTableInformation) {
		List<Integer> tableRowSizes = fixedTableInformation.getTableRowHeights();
		List<XSLFTableRow> rows = aTable.getRows();
		for (int i = 0; i < rows.size(); i++) {
			if (i < tableRowSizes.size()) {
				Integer rowHeight = tableRowSizes.get(i);
				if (rowHeight > -1) {
					XSLFTableRow tableRow = rows.get(i);
					tableRow.setHeight(rowHeight);
				}
			}
		}
	}

	/**
	 * Handle table without a fixed table token by getting all cells and feeding them into the
	 * standard replacement.
	 * 
	 * @param aSlide
	 *        the slide
	 * @param someTokens
	 *        the tokens
	 * @param aTable
	 *        the table to be handled
	 * @throws IOException
	 *         re-thrown if the replacement throws one
	 */
	private void replaceNormalTokensInTable(XSLFSheet aSlide, int slideIdx, Map<String, Object> someTokens,
			XSLFTable aTable, Map<XSLFTableCell, XSLFTable> someCells)
			throws IOException {
		List<XSLFShape> shapes = new ArrayList<>();
		for (XSLFTableRow theRow1 : aTable.getRows()) {
			for (XSLFTableCell theCell1 : theRow1.getCells()) {
				if (theCell1 != null) {
					shapes.add(theCell1);
					someCells.put(theCell1, aTable);
				}
			}
		}
		parseForTokens(aSlide, slideIdx, shapes, someTokens, someCells);
	}
	
	/**
	 * @param aSheet
	 *        the sheet
	 * @return the trimmed title or <code>null</code>
	 */
	protected static String extractTitle(XSLFSheet aSheet) {
		if (aSheet instanceof XSLFSlide) {
			return StringServices.trim(((XSLFSlide) aSheet).getTitle());
	    }
	    return null;
	}
	
	/**
	 * Takes a String and parses it for tokens starting with one of the
	 * predefined prefixes. The tokens found are stored in a {@link List} which
	 * is also the return value.
	 * 
	 * @param aSearchText
	 *            a String to parse.
	 * @return the tokens found in the theShapeText.
	 */
	private List<String> findTokens(String aSearchText) {
		if(aSearchText == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		String toTest = aSearchText;
		while(toTest.length() > 6) {
			boolean startsWithPrefix = false;
			for (int i = 0; i < PREFIX_VALUES.length; i++) {
				if (toTest.startsWith(PREFIX_VALUES[i])) {
					startsWithPrefix = true;
				}
			}

			if (startsWithPrefix || toTest.startsWith(SLIDE_COUNT)) {
				toTest = extractToken(result, toTest);
			}
			else {
				toTest = toTest.substring(1);
			}
		}
	    return result;
    }

	/**
	 * Takes a {@link String} that starts with one of the defined Token prefixes
	 * removes the token from the String, adds it to the result {@link List} and
	 * returns the rest of the string.
	 * 
	 * @param aResultList
	 *            a List of Tokens
	 * @param toTest
	 *            a String starting with one of the token prefixes
	 * @return the passed in string without the token
	 */
	public static String extractToken(List<String> aResultList, String toTest) {
	    String[] split = toTest.split("[^\\w]");
	    String theToken = split[0];
		aResultList.add(theToken);
	    if (split.length > 1) {
	    	toTest = toTest.substring(theToken.length());
		} else {
	    	toTest = StringServices.EMPTY_STRING;
	    }
	    return toTest;
    }

	/**
	 * Replaces picture and value token with the values defined in the given map of tokens.
	 * 
	 * @param aSlide
	 *        a slide that contains the shapes with the tokens to be replaced.
	 * @param aShape
	 *        the shape that contains the token
	 * @param aToken
	 *        the token to be replaced
	 * @param aReplacable
	 *        an object that is used to replace the token
	 * @param someCells
	 *        the cell - table mapping
	 * @throws IOException
	 *         if the picture shape could not be added to the {@link XSLFSlide}/
	 *         {@link XMLSlideShow}.
	 */
	private boolean replaceToken(XSLFSheet aSlide, XSLFTextShape aShape, String aToken,
			Object aReplacable, Map<XSLFTableCell, XSLFTable> someCells)
			throws IOException {
		if (aReplacable == null && !aToken.startsWith(SLIDE_COUNT)) {
			// remove token from text
			replaceText(aShape, aToken, "");
			return false;
		}
		if (aToken.startsWith(PREFIX_AUTOTABLE)) {
			if (aReplacable instanceof PptXTable) {
				PptXTable.writeAsTable(aSlide, aShape, (PptXTable) aReplacable);
				return true;
		    }
		    else if (aReplacable != null) {
				throw new IllegalArgumentException("The value for '" + aToken + "' must be a PptXTable");
		    }
		}
		else if(aToken.startsWith(PREFIX_PICTURE)) {
			if (aReplacable instanceof File || aReplacable instanceof ImageReplacerData
				|| aReplacable instanceof BinaryContent) {
				replacePicture(aSlide, aShape, aReplacable, someCells);
				return true;
			} else {
				throw notAnImage(aReplacable);
			}
		}
		else if (aToken.startsWith(PREFIX_SLIDECONTENT)) {
		    if (aReplacable instanceof File) { 
				try (FileInputStream is = new FileInputStream((File) aReplacable)) {
					this.insertSlideContents(aSlide, is);
					return true;
				}
			} else if (aReplacable instanceof BinaryContent) {
				try (InputStream is = ((BinaryContent) aReplacable).getStream()) {
					this.insertSlideContents(aSlide, is);
					return true;
				}
            } else if (aReplacable != null) {
				throw new IllegalArgumentException("The value for '" + aToken
						+ "' must be a File or BinaryContent, is a " + aReplacable.getClass().getName());
            }
		}
		else if(aToken.startsWith(PREFIX_VALUE)) {
			if(aReplacable instanceof String) {
				replaceText(aShape, aToken, (String) aReplacable);
				return false;
			}
			else if (aReplacable instanceof PptXTextShape) {
				PptXTextShape theTextShape = ((PptXTextShape) aReplacable);
				theTextShape.copy(aShape, true, true, true);
				return false;
			} else if (aReplacable instanceof HyperlinkDefinition) {
				this.replaceLink(aShape, aToken, (HyperlinkDefinition) aReplacable);
			}
		}
		else if (aReplacable != null && aToken.startsWith(SLIDE_COUNT)) {
			String replaceAll = "";

			if (aReplacable instanceof String) {
				replaceAll = (String) aReplacable;
			}
			else if(aReplacable instanceof Integer) {
				Integer num = (Integer) aReplacable;
				replaceAll = num.toString();
			}

			this.replaceText(aShape, aToken, replaceAll);
			return false;
		}

		return false;
	}

	/**
	 * Ensure that the new shape has a a paragraph and a text run and adapt their styles to the
	 * first paragraph and text run of the old shape
	 * 
	 * @param anOldShape
	 *        the original shape
	 * @param aNewShape
	 *        the shape to adapt
	 */
	protected void adaptTextShape(XSLFTextShape anOldShape, XSLFTextShape aNewShape) {
		aNewShape.setBottomInset(anOldShape.getBottomInset());
		aNewShape.setLeftInset(anOldShape.getLeftInset());
		aNewShape.setPlaceholder(anOldShape.getTextType());
		aNewShape.setRightInset(anOldShape.getRightInset());
		aNewShape.setTextAutofit(anOldShape.getTextAutofit());
		aNewShape.setTextDirection(anOldShape.getTextDirection());
		aNewShape.setTopInset(anOldShape.getTopInset());
		aNewShape.setVerticalAlignment(anOldShape.getVerticalAlignment());
		aNewShape.setWordWrap(anOldShape.getWordWrap());

		List<XSLFTextParagraph> oldTextParagraphs = anOldShape.getTextParagraphs();
		List<XSLFTextParagraph> newTextParagraphs = aNewShape.getTextParagraphs();
		if (!oldTextParagraphs.isEmpty()) {
			XSLFTextParagraph oldParagr = oldTextParagraphs.get(0);

			XSLFTextParagraph newParagr = null;
			if (newTextParagraphs.isEmpty()) {
				newParagr = aNewShape.addNewTextParagraph();
			} else {
				newParagr = newTextParagraphs.get(0);
			}

			newParagr.setBullet(oldParagr.isBullet());
			newParagr.setBulletCharacter(oldParagr.getBulletCharacter());
			newParagr.setBulletFont(oldParagr.getBulletFont());
			newParagr.setBulletFontColor(oldParagr.getBulletFontColor());
			newParagr.setBulletFontSize(oldParagr.getBulletFontSize());
			newParagr.setIndent(oldParagr.getIndent());
			newParagr.setLeftMargin(oldParagr.getLeftMargin());
			newParagr.setIndentLevel(oldParagr.getIndentLevel());
			newParagr.setLineSpacing(oldParagr.getLineSpacing());
			newParagr.setSpaceAfter(oldParagr.getSpaceAfter());
			newParagr.setSpaceBefore(oldParagr.getSpaceBefore());
			newParagr.setTextAlign(oldParagr.getTextAlign());

			List<XSLFTextRun> oldTextRuns = oldParagr.getTextRuns();
			List<XSLFTextRun> newTextRuns = newParagr.getTextRuns();
			if (!oldTextRuns.isEmpty()) {
				XSLFTextRun oldTextRun = oldTextRuns.get(0);

				XSLFTextRun newTextRun = null;
				if (newTextRuns.isEmpty()) {
					newTextRun = newParagr.addNewTextRun();
				} else {
					newTextRun = newTextRuns.get(0);
				}

				newTextRun.setBold(oldTextRun.isBold());
				newTextRun.setFontColor(oldTextRun.getFontColor());
				newTextRun.setFontFamily(oldTextRun.getFontFamily());
				newTextRun.setFontSize(oldTextRun.getFontSize());
				newTextRun.setItalic(oldTextRun.isItalic());
				newTextRun.setStrikethrough(oldTextRun.isStrikethrough());
				newTextRun.setUnderlined(oldTextRun.isUnderlined());
			}
		}
	}

	/**
	 * Replace the token in a shape with the given text
	 * 
	 * @param aShape
	 *        the shape
	 * @param aToken
	 *        the token
	 * @param aLink
	 *        the hyper link
	 */
	protected void replaceLink(XSLFTextShape aShape, String aToken, HyperlinkDefinition aLink) {
		for (XSLFTextParagraph theParagr : aShape.getTextParagraphs()) {
			for (XSLFTextRun theRun : theParagr.getTextRuns()) {
				String text = theRun.getRawText();
				String repl = text.replaceAll(aToken, aLink.getDisplayName().replaceAll("([\\$\\\\])", "\\\\$1"));
				if (somethingReplaced(text, repl)) {
					setLinkAddress(aShape, theRun, aLink);
					insertText(aShape, theRun, repl);
				}
			}
		}
	}

	/**
	 * Replace the token in a shape with the given text
	 * 
	 * @param aShape
	 *        the shape
	 * @param aToken
	 *        the token
	 * @param aText
	 *        the new text
	 */
	protected void replaceText(XSLFTextShape aShape, String aToken, String aText) {
		for (XSLFTextParagraph theParagr : aShape.getTextParagraphs()) {
			for (XSLFTextRun theRun : theParagr.getTextRuns()) {
				String text = theRun.getRawText();
				String repl = text.replaceAll(aToken, aText.replaceAll("([\\$\\\\])", "\\\\$1"));
				if (somethingReplaced(text, repl)) {
					insertText(aShape, theRun, repl);
				}
			}
		}
	}

	/**
	 * Since {@link String#replaceAll(String, String)} returns the original string, if no match
	 * occurred, it is OK to check the result and the original for identity to determine whether a
	 * match occurred.
	 */
	private boolean somethingReplaced(Object text, Object repl) {
		return text != repl;
	}

	/**
	 * Insert a text in a shape
	 * 
	 * @param aShape
	 *        the shape
	 * @param aLink
	 *        the hyper link
	 */
	public static void insertLink(XSLFTextShape aShape, HyperlinkDefinition aLink) {
		// Get or create paragraph and run
		List<XSLFTextParagraph> theParagrs = aShape.getTextParagraphs();
		XSLFTextParagraph theParagr = (theParagrs.isEmpty()) ? aShape.addNewTextParagraph() : theParagrs.get(0);
		List<XSLFTextRun> theRuns = theParagr.getTextRuns();
		XSLFTextRun theRun = (theRuns.isEmpty()) ? theParagr.addNewTextRun() : theRuns.get(0);

		// Set link
		setLinkAddress(aShape, theRun, aLink);

		// Insert text
		insertText(aShape, theRun, aLink.getDisplayName());
	}

	/**
	 * Set a hyper link in a text run. Note: does NOT set the link text!
	 * 
	 * @param aShape
	 *        the surrounding shape
	 * @param aRun
	 *        the run
	 * @param aLink
	 *        the hyper link
	 */
	private static void setLinkAddress(XSLFTextShape aShape, XSLFTextRun aRun, HyperlinkDefinition aLink) {
		XSLFHyperlink hyperlink = aRun.createHyperlink();
		if (aLink.getSlideNumber() != null) {
			hyperlink.linkToSlide(aShape.getSheet().getSlideShow().getSlides().get(aLink.getSlideNumber() - 1));
		} else {
			try {
				String normalizedLink = normalizeLinkAddress(aLink);
				hyperlink.setAddress(normalizedLink);
			} catch (MalformedURLException ex) {
				Logger.warn("Invalid URL: " + aLink.getBookmark(), ex, POIPowerpointXUtil.class);
			}
		}
	}

	/**
	 * Due to the fact that {@link XSLFHyperlink#setAddress(String)} produces an error when called with
	 * a link that contains a "\" we have to normalize all given links so that they do not contain this
	 * character.
	 */
	private static String normalizeLinkAddress(HyperlinkDefinition aLink) throws MalformedURLException {
		String normalizedLink = new URL(aLink.getBookmark()).toExternalForm();

		// Depending on the server (windows/Linux) the normalizing via URL (see line above) will not
		// replace "\" with "/" when run under linux (because "\" is an allowed filename character under
		// linux) so we have to replace this by hand
		normalizedLink = normalizedLink.replace("\\", "/");

		// Use UrlEscaper to normalize URL.
		// This is necessary because the method XSLFHyperlink.setAddress() throws an exception when it is
		// called with a URL that e.g. contains whitespace. Because of that we have to escape whitespace
		// with "%20".
		Escaper fragmentEscaper = UrlEscapers.urlFragmentEscaper();
		normalizedLink = fragmentEscaper.escape(normalizedLink);

		return normalizedLink;
	}

	/**
	 * Insert a text in a shape
	 * 
	 * @param aShape
	 *        the shape
	 * @param aText
	 *        the text
	 */
	public static void insertText(XSLFTextShape aShape, String aText) {
		List<XSLFTextParagraph> theParagrs = aShape.getTextParagraphs();
		XSLFTextParagraph theParagr = (theParagrs.isEmpty()) ? aShape.addNewTextParagraph() : theParagrs.get(0);
		List<XSLFTextRun> theRuns = theParagr.getTextRuns();
		XSLFTextRun theRun = (theRuns.isEmpty()) ? theParagr.addNewTextRun() : theRuns.get(0);

		insertText(aShape, theRun, aText);
	}

	/**
	 * Set text into a shape trying to use a text run. If that fails try to use the shape itself.
	 * 
	 * @param aShape
	 *        the shape
	 * @param theRun
	 *        the run
	 * @param repl
	 *        the text
	 */
	public static void insertText(XSLFTextShape aShape, XSLFTextRun theRun, String repl) {
		if (repl == null) {
			repl = "";
		}
		try {
			theRun.setText(repl);
		}
		catch (NullPointerException e) {
		    int          theLength = repl.length();
		    StringBuffer theBuffer = new StringBuffer(theLength);

		    for (int thePos = 0; thePos < theLength; thePos++) {
		        char theChar = repl.charAt(thePos);

		        if (Character.isLetterOrDigit(theChar)) {
		            theBuffer.append(theChar);
		        }
		        else {
		            switch (theChar) {
		            case ' ':
		            case '.':
		            case ',':
		            case ':':
		            case ';':
		            case '"':
		            case '+':
		            case '-':
		            case '*':
		            case '/':
		            case '=':
		            case '€':
		            case '$':
		            case '(':
		            case ')':
		            case '!':
		            case '?':
		            case '\'':
		                theBuffer.append(theChar);
		                break;
		            default:
		                theBuffer.append('_');
		                break;
		            }
		        }
		    }

		    try {
				theRun.setText(theBuffer.toString());
		    }
		    catch (NullPointerException ex) {
				try {
					aShape.setText(repl);
				} catch (Exception ex1) {
					Logger.error("The value for the field '" + theBuffer.toString()
						+ "' most likely contains chars which can not be interpreted by POI", ex,
						POIPowerpointXUtil.class);
				}
		    }
		}
	}

	private XSLFShape replacePicture(XSLFSheet aSlide, XSLFShape aShape, Object image,
			Map<XSLFTableCell, XSLFTable> someCells) throws IOException {
		if (aShape instanceof XSLFTableCell) {
			XSLFTableCell theCell = (XSLFTableCell) aShape;
			XSLFTable theTable = someCells.get(theCell);
			Rectangle2D anchor = getAnchorForTableCell(theTable, theCell);

			VerticalAlignment verticalAlignment = getVerticalAlignment(theCell);
			TextAlign textAlign = getHorizontalAlignment(theCell);
			XSLFPictureShape picShape = createPicture(aSlide, image, theCell, anchor, verticalAlignment, textAlign);
			insertText(theCell, "");

			return picShape;
		} else {
			Rectangle2D anchor = aShape.getAnchor();

			VerticalAlignment verticalAlignment = getVerticalAlignment(aShape);
			TextAlign textAlign = getHorizontalAlignment(aShape);
			XSLFPictureShape picShape = createPicture(aSlide, image, null, anchor, verticalAlignment, textAlign);
			aSlide.removeShape(aShape);

			return picShape;
		}
	}

	/**
	 * If the given element is a {@link XSLFTextShape} this method returns the
	 * {@link VerticalAlignment} of the element, otherwise {@link VerticalAlignment#TOP}.
	 */
	private VerticalAlignment getVerticalAlignment(Object element) {
		if (element instanceof XSLFTextShape) {
			return ((XSLFTextShape) element).getVerticalAlignment();
		} else {
			return VerticalAlignment.TOP;
		}
	}

	/**
	 * If the given element is a {@link XSLFTextShape} this method returns the {@link TextAlign}
	 * within the element, otherwise {@link TextAlign#LEFT}.
	 */
	private TextAlign getHorizontalAlignment(Object element) {
		if (element instanceof XSLFTextShape) {
			List<XSLFTextParagraph> textParagraphs = ((XSLFTextShape) element).getTextParagraphs();
			if (!CollectionUtil.isEmptyOrNull(textParagraphs)) {
				return textParagraphs.get(0).getTextAlign();
			} else {
				return TextAlign.LEFT;
			}
		} else {
			return TextAlign.LEFT;
		}
	}

	/**
	 * Imports the content of the slice represented by the given stream into the given slide.
	 */
	public void insertSlideContents(XSLFSheet aSlide, InputStream content) throws IOException {
		try (XMLSlideShow theShow = new XMLSlideShow(content)) {
			List<XSLFSlide> importedSlides = theShow.getSlides();
			switch (importedSlides.size()) {
				case 0:
					// Nothing to import
					break;
				case 1:
					aSlide.appendContent(importedSlides.get(0));
					break;
				default:
					aSlide.appendContent(importedSlides.get(0));
					XMLSlideShow slideShow = aSlide.getSlideShow();
					for (int i = 1; i < importedSlides.size(); i++) {
						XSLFSlide importedSlide = importedSlides.get(i);
						XSLFSlide newSlide = slideShow.createSlide(importedSlide.getSlideLayout());
						newSlide.appendContent(importedSlide);
					}
					break;
			}
		}
	}

	private Rectangle2D getAnchorForTableCell(XSLFTable aTable, XSLFTableCell aCell) {
		double startX = aTable.getAnchor().getX();
		double startY = aTable.getAnchor().getY();
		for (XSLFTableRow theRow : aTable.getRows()) {
			double rowX = startX;
			int theColNo = 0;
			double rowHeight = theRow.getHeight();
			for (XSLFTableCell theCell : theRow.getCells()) {
				double colWidth = aTable.getColumnWidth(theColNo);
				if (theCell == aCell) {
					return new Rectangle2D.Double(rowX, startY, colWidth, rowHeight);
				}
				rowX += colWidth;
				theColNo++;
			}
			startY += rowHeight;
		}

		return null;
	}

	private XSLFPictureShape createPicture(XSLFSheet aSlide, Object image, XSLFTableCell theCell, Rectangle2D anchor,
			VerticalAlignment verticalAlign, TextAlign textAlign) throws IOException {
		Rectangle2D theBox = adaptToAspectRatio(theCell, anchor, verticalAlign, textAlign, image);
		XSLFPictureData data = aSlide.getSlideShow().addPicture(getImageBytes(image), PictureType.PNG);
		XSLFPictureShape picShape = aSlide.createPicture(data);
		picShape.setAnchor(theBox); // use width and height from the layout box
		return picShape;
	}

	private Rectangle2D adaptToAspectRatio(XSLFTableCell theCell, Rectangle2D aBox, VerticalAlignment verticalAlign, TextAlign textAlign,
			Object image) {
		if (image instanceof ImageReplacerData) {
			return adaptToAspectRatio(aBox, (ImageReplacerData) image);
		}

		try {
			ImageInputStream theIOS = getImageInputStream(image);
			Iterator<ImageReader> theIter  = ImageIO.getImageReaders(theIOS);
			
			if (theIter.hasNext()) {
				ImageReader theReader = theIter.next();
				theReader.setInput(theIOS);
				
				float aspectRatio = theReader.getAspectRatio(0);
				
				Dimension inputDim = getInputDimension(theCell, aBox);
				Dimension outDim = Utils.getImageDimension(inputDim, aspectRatio);

				int xFactor = computeXMoveFactor(theCell, aBox, textAlign, outDim);
				int yFactor = computeYMoveFactor(theCell, aBox, verticalAlign, outDim);
				return new Rectangle(Math.round((float) aBox.getX()) + xFactor,
									 Math.round((float) aBox.getY()) + yFactor,
									 outDim.width,
									 outDim.height); 
			}
		} catch (IOException e) {
			Logger.warn("Failed to adapt aspect ratio for '" + getImageDebugName(image) + "'. Using original size.", e,
				POIPowerpointXUtil.class);
		}
		return aBox;
	}

	static Rectangle2D adaptToAspectRatio(Rectangle2D anAlignmentShape, ImageReplacerData aData) {
		// Calc size
		float theOldHeight = (float) anAlignmentShape.getHeight();
		float theOldWidth = (float) anAlignmentShape.getWidth();

		float theWidth = aData.hSize;
		float theHeight = aData.vSize;

		// Handle absolute size parameters
		if (theWidth == 0 && theHeight == 0 || theOldHeight <= 1 || theOldWidth <= 1) {
			// Both scales are 0 -> no resize
			// OR: image is just 1 pixel -> used for empty images -> no resize
			theWidth = theOldWidth;
			theHeight = theOldHeight;
		} else if (theWidth == 0) {
			// Keep aspect ratio -> resize height
			theWidth = theOldWidth * theHeight / theOldHeight;
		} else if (theHeight == 0) {
			// Keep aspect ratio -> resize width
			theHeight = theOldHeight * theWidth / theOldWidth;
		}

		// Calc position
		float theAlignWidth = (float) anAlignmentShape.getWidth();
		float theAlignHeight = (float) anAlignmentShape.getHeight();
		float theAlignLeft = (float) anAlignmentShape.getMinX();
		float theAlignTop = (float) anAlignmentShape.getMinY();

		long theHAlign = aData.hAlignment;
		long theVAlign = aData.vAlignment;

		boolean lockRatio = aData.lockRatio;

		float theTop = theAlignTop;
		float theLeft = theAlignLeft;

		// Vertical aligment
		if (theVAlign == ImageReplacerData.VALIGN_TOP) {
			// Move to top + 2
			theTop = theAlignTop + 2;
		} else if (theVAlign == ImageReplacerData.VALIGN_BOTTOM) {
			// Move to bottom - 2
			theTop = theAlignTop + theAlignHeight - 2 - theHeight;
		} else if (theVAlign == ImageReplacerData.VALIGN_CENTER) {
			// Move to center of rel. shape
			theTop = theAlignTop + (theAlignHeight - theHeight) / 2;
		} else if (theVAlign == ImageReplacerData.VALIGN_FIT) {
			// Move to top of rel. shape and resize to fit
			theTop = theAlignTop;
			theHeight = theAlignHeight;
			if (lockRatio) {
				// Keep aspect ratio -> resize height
				theWidth = theOldWidth * theHeight / theOldHeight;
			}
		}

		// Horizontal alignment
		if (theHAlign == ImageReplacerData.HALIGN_LEFT) {
			// Move to left + 2
			theLeft = theAlignLeft + 2;
		} else if (theHAlign == ImageReplacerData.HALIGN_RIGHT) {
			// Move to right - 2
			theLeft = theAlignLeft + theAlignWidth - 2 - theWidth;
		} else if (theHAlign == ImageReplacerData.HALIGN_CENTER) {
			// Move to center of rel. shape
			theLeft = theAlignLeft + (theAlignWidth - theWidth) / 2;
		}
		if (theHAlign == ImageReplacerData.HALIGN_FIT) {
			// Move to left and resize to fit
			theLeft = theAlignLeft;
			theWidth = theAlignWidth;
			if (lockRatio) {
				// Keep aspect ratio -> resize height
				theHeight = theOldHeight * theWidth / theOldWidth;
			}
		}

		// Resize
		Rectangle2D theRect = new Rectangle2D.Float(theLeft, theTop, theWidth, theHeight);

		// Scale to max size fitting into parent shape
		// preserving aspect ratio
		theOldWidth = (float) theRect.getWidth();
		theOldHeight = (float) theRect.getHeight();
		if (theOldWidth > theAlignWidth) {
			theRect.setRect(theRect.getX(), theRect.getY(), theAlignWidth, theOldHeight * theAlignWidth / theOldWidth);
		}

		theOldWidth = (float) theRect.getWidth();
		theOldHeight = (float) theRect.getHeight();
		if (theOldHeight > theAlignHeight) {
			theRect
				.setRect(theRect.getX(), theRect.getY(), theOldWidth * theAlignHeight / theOldHeight, theAlignHeight);
		}

		return theRect;
	}

	private Dimension getInputDimension(XSLFTableCell cell, Rectangle2D box) {
		int width;
		int height;
		if (cell != null) {
			double borderLeft = borderLeft(cell);
			double borderRight = borderRight(cell);
			double bordersLeftRight = borderLeft + borderRight;
			
			double borderTop = borderTop(cell);
			double borderBottom = borderBottom(cell);
			double bordersTopBottom = borderTop + borderBottom;
			
			width = (int) Math.floor(box.getWidth() - bordersLeftRight);
			height = (int) Math.floor(box.getHeight() - bordersTopBottom);
		}
		else {
			width = Math.round((float) box.getWidth());
			height = Math.round((float) box.getHeight());
		}
		return new Dimension(width, height);
	}

	private double borderBottom(XSLFTableCell cell) {
		return (cell.getBottomInset() / 2) + 1/* 1 pixel space to bottom border */;
	}

	private double borderTop(XSLFTableCell cell) {
		return (cell.getTopInset() / 2) + 1/* 1 pixel space to top border */;
	}

	private double borderRight(XSLFTableCell cell) {
		return (cell.getRightInset() / 2) + 1/* 1 pixel space to right border */;
	}

	private double borderLeft(XSLFTableCell cell) {
		return (cell.getLeftInset() / 2) + 1/* 1 pixel space to left border */;
	}

	static byte[] getImageBytes(Object image) throws IOException {
		if (image instanceof File) {
			return FileUtilities.getBytesFromFile((File) image);
		} else if (image instanceof ImageReplacerData) {
			return FileUtilities.getBytesFromFile(((ImageReplacerData) image).imgFile);
		} else if (image instanceof BinaryContent) {
			return StreamUtilities.readStreamContents(((BinaryContent) image).getStream());
		} else {
			throw notAnImage(image);
		}
	}

	static String getImageDebugName(Object image) {
		if (image instanceof File) {
			return ((File) image).getName();
		} else if (image instanceof ImageReplacerData) {
			return ((ImageReplacerData) image).imgFile.getName();
		} else if (image instanceof BinaryContent) {
			return ((BinaryContent) image).toString();
		} else {
			throw notAnImage(image);
		}
	}

	static ImageInputStream getImageInputStream(Object image) throws IOException {
		if (image instanceof File) {
			return ImageIO.createImageInputStream(image);
		} else if (image instanceof ImageReplacerData) {
			return ImageIO.createImageInputStream(((ImageReplacerData) image).imgFile);
		} else if (image instanceof BinaryContent) {
			return ImageIO.createImageInputStream(((BinaryContent) image).getStream());
		} else {
			throw notAnImage(image);
		}
	}

	static IllegalArgumentException notAnImage(Object image) {
		if (image == null) {
			throw new IllegalArgumentException("Null is not an exportable image.");
		}
		StringBuilder noImageType = new StringBuilder();
		noImageType.append("Object '");
		noImageType.append(image);
		noImageType.append("' of type '");
		noImageType.append(image.getClass().getName());
		noImageType.append("' is not an exportable image: Use instances of '");
		noImageType.append(File.class.getName());
		noImageType.append("' or '");
		noImageType.append(BinaryContent.class.getName());
		noImageType.append("' or '");
		noImageType.append(ImageReplacerData.class.getName());
		noImageType.append("'.");
		throw new IllegalArgumentException(noImageType.toString());
	}

	private int computeXMoveFactor(XSLFTableCell cell, Rectangle2D aBox, TextAlign textAlign, Dimension pictureDim) {
		double spaceToSide = 0; // default space to side
		switch (textAlign) {
			case LEFT:
				// use horizontal-align left
				if (cell != null) {
					spaceToSide = borderLeft(cell);
				}
				return (int) spaceToSide;
			case RIGHT:
				// use horizontal-align right
				if (cell != null) {
					spaceToSide = borderRight(cell);
				}
				return (int) (Math.ceil((float) aBox.getWidth()) - pictureDim.width - spaceToSide);
			default:
				// use horizontal-align center
				if (cell != null) {
					double borderLeft = borderLeft(cell);
					double borderRight = borderRight(cell);
					spaceToSide = borderLeft + borderRight;
				}
				int widthDiff = (int) (Math.floor((float) aBox.getWidth() - spaceToSide) - pictureDim.width);
				if (cell != null) {
					double leftBorderSpace = borderLeft(cell);
					return (int) ((widthDiff / 2) + leftBorderSpace);
				} else {
					return widthDiff / 2;
				}
		}
	}

	private int computeYMoveFactor(XSLFTableCell cell, Rectangle2D aBox, VerticalAlignment verticalAlign,
			Dimension pictureDim) {
		double spaceToSide = 0; // default space to side
		switch (verticalAlign) {
			case TOP:
				// use vertical-align top
				if (cell != null) {
					spaceToSide = borderTop(cell);
				}
				return (int) spaceToSide;
			case BOTTOM:
				// use vertical-align bottom
				if (cell != null) {
					spaceToSide = borderBottom(cell);
				}
				return (int) (Math.ceil((float) aBox.getHeight()) - pictureDim.height - spaceToSide);
			default:
				// use vertical-align center
				if (cell != null) {
					double borderTop = borderTop(cell);
					double borderBottom = borderBottom(cell);
					spaceToSide = borderTop + borderBottom;
				}
				int heightDiff = (int) (Math.floor((float) aBox.getHeight() - spaceToSide) - pictureDim.height);
				if (cell != null) {
					double topBorderSpace = borderTop(cell);
					return (int) ((heightDiff / 2) + topBorderSpace);
				} else {
					return heightDiff / 2;
				}
		}
	}

	/**
	 * Prints the given {@link XSLFShape}s with the given indentation to System.out. For each
	 * {@link XSLFShape} the type of the shape, its position and (if present) its text will be
	 * printed.
	 * 
	 * @param anIndentation
	 *        a String defining the indentation.
	 * @param aShapeArray
	 *        a Array of {@link XSLFShape}s to print.
	 */
	public static void printShapes(String anIndentation, List<XSLFShape> aShapeArray) {
		for (int j = 0; j < aShapeArray.size(); j++) {
			// name of the shape
			XSLFShape theShape = aShapeArray.get(j);
			String name = theShape.getShapeName();
			// shapes's anchor which defines the position of this shape in the
			// slide
			Rectangle2D anchor = theShape.getAnchor();
			Rectangle2D anchor2D = theShape.getAnchor();

			if (theShape instanceof XSLFAutoShape) {
				XSLFAutoShape shape = (XSLFAutoShape) theShape;
				System.out.println(anIndentation + "ShapeType: XSLFAutoShape");
				System.out.println(anIndentation + "\t" + shape.getText());
				System.out.println(anIndentation + "\tx: " + anchor.getX() + ", y: " + anchor.getY());
				System.out.println(anIndentation + "\tx: " + anchor2D.getX() + ", y: " + anchor2D.getY());
				System.out.println(anIndentation + "\theight: " + anchor.getHeight() + ", width: " + anchor.getWidth());
			} else if (theShape instanceof XSLFTextBox) {
				XSLFTextBox shape = (XSLFTextBox) theShape;
				System.out.println(anIndentation + "ShapeType: XSLFTextBox");
				System.out.println(anIndentation + "\t" + shape.getText());
			} else if (theShape instanceof XSLFPictureShape) {
				System.out.println(anIndentation + "ShapeType: XSLFPictureShape");
			} else if (theShape instanceof XSLFTable) {
				XSLFTable table = (XSLFTable) theShape;
				System.out.println(anIndentation + "ShapeType: XSLFTable");
				for (XSLFTableRow theRow : table.getRows()) {
					System.out.println(anIndentation + "\tType: XSLFTableRow");
					for (XSLFTableCell theCell : theRow.getCells()) {
						System.out.println(anIndentation + "\t\tShapeType: XSLFTableCell");
						System.out.println(anIndentation + "\t\t\t" + theCell.getText());
					}
				}
			} else if (theShape instanceof XSLFGroupShape) {
				XSLFGroupShape theGrp = (XSLFGroupShape) theShape;
				System.out.println(anIndentation + "ShapeType: XSLFGroupShape");
				printShapes(anIndentation + "\t", POIPowerpointUtil.shapes(theGrp));
			}
			else {
				System.out.println(anIndentation + "name: " + name);
			}
		}
	}
}
