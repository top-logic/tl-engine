/*
* SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
* 
* SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
*/
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} for generating Excel files in TL-Script.
 * 
 * Supports both matrix and cell-based content creation with styling and formulas.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class ExcelFile extends GenericMethod {

	/**
	 * Sheet content property keys
	 */
	private static final String PROP_SHEET_NAME = "name";
	private static final String PROP_SHEET_CONTENT = "content";
	private static final String PROP_COL_WIDTHS = "colWidths";
	private static final String PROP_ROW_HEIGHTS = "rowHeights";

	private static final String PROP_AUTO_GENERATE_NAME = "autoGenerateName";

	/**
	 * Cell content property keys
	 */
	private static final String PROP_CELL_CONTENT = "content";
	private static final String PROP_CELL_ROW = "row";
	private static final String PROP_CELL_COL = "col";
	private static final String PROP_CELL_FORMULA = "formula";

	/**
	 * Styling property keys
	 */
	private static final String PROP_COLOR = "color";
	private static final String PROP_BACKGROUND = "background";
	private static final String PROP_BOLD = "bold";
	private static final String PROP_ITALIC = "italic";
	private static final String PROP_FONT_SIZE = "fontSize";
	private static final String PROP_FONT_FAMILY = "fontFamily";
	
	/**
	 * Border styling property keys
	 */
	private static final String PROP_BORDER_TOP = "borderTop";
	private static final String PROP_BORDER_BOTTOM = "borderBottom";
	private static final String PROP_BORDER_LEFT = "borderLeft";
	private static final String PROP_BORDER_RIGHT = "borderRight";
	
	/**
	 * Alignment property keys
	 */
	private static final String PROP_ALIGN = "align";
	private static final String PROP_VALIGN = "valign";
	
	/**
	 * Number format property key
	 */
	private static final String PROP_NUMBER_FORMAT = "numberFormat";

	/**
	 * Span property keys
	 */
	private static final String PROP_ROW_SPAN = "rowSpan";

	private static final String PROP_COL_SPAN = "colSpan";

	/**
	 * Track merged regions for collision detection
	 */
	private static class MergedRegionTracker {
		private final List<CellRangeAddress> regions = new ArrayList<>();

		public void addRegion(CellRangeAddress region) {
			regions.add(region);
		}

		public void checkCellPosition(int row, int col) {
			for (CellRangeAddress existing : regions) {
				if(existing.isInRange(row, col)) {
					throw new TopLogicException(
						ResKey.text("Cannot create cell at " + CellReference.convertNumToColString(col) + (row + 1)
							+ " - it is within existing merged region " + existing.formatAsString()));
				}
			}
		}

	}

	/**
	 * Cache for CellStyle objects to avoid creating duplicate styles
	 */
	private static class StyleCache {
		private final Map<String, CellStyle> styleMap = new HashMap<>();

		private final Workbook workbook;

		private final ExcelFile excelFile;

		public StyleCache(Workbook workbook, ExcelFile excelFile) {
			this.workbook = workbook;
			this.excelFile = excelFile;
		}

		public CellStyle getOrCreateStyle(Map<String, Object> styleProps) {
			if (styleProps == null || styleProps.isEmpty()) {
				return null;
			}

			// Create a cache key from the style properties
			String cacheKey = createCacheKey(styleProps);

			// Return cached style if it exists
			if (styleMap.containsKey(cacheKey)) {
				return styleMap.get(cacheKey);
			}

			// Create new style and cache it
			CellStyle newStyle = excelFile.createCellStyle(workbook, styleProps);
			styleMap.put(cacheKey, newStyle);
			return newStyle;
		}

		private String createCacheKey(Map<String, Object> styleProps) {
			StringBuilder key = new StringBuilder();
			// Sort the properties to ensure consistent keys
			styleProps.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> {
					key.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
				});
			return key.toString();
		}
	}

	/**
	 * Creates a {@link ExcelFile}.
	 */
	public ExcelFile(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ExcelFile(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BINARY_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object content = arguments[0];
		if (content == null) {
			content = new ArrayList<>();
		}

		String fileName = asString(arguments[1]);

		try {
			byte[] excelData = createExcelFile(content);
			return createBinaryData(fileName, excelData);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create Excel file: " + ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	private byte[] createExcelFile(Object content) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			List<Object> sheets = (List<Object>) asList(content);

			for (Object sheetObj : sheets) {
				Map<String, Object> sheetMap = (Map<String, Object>) asMap(sheetObj);
				String sheetName = (String) sheetMap.get(PROP_SHEET_NAME);
				if (asBoolean(sheetMap.get(PROP_AUTO_GENERATE_NAME))) {
					sheetName = "Sheet " + (workbook.getNumberOfSheets() + 1);
				}

				Sheet sheet = workbook.createSheet(sheetName);
				Object sheetContent = sheetMap.get(PROP_SHEET_CONTENT);

				if (sheetContent != null) {
					processSheetContent(workbook, sheet, sheetContent);
				}

				// Apply column widths if specified
				Map<String, Object> colWidths = (Map<String, Object>) sheetMap.get(PROP_COL_WIDTHS);
				if (colWidths != null) {
					for (Map.Entry<String, Object> entry : colWidths.entrySet()) {
						Integer colIndex = asInt(entry.getKey());
						Double width = asDouble(entry.getValue());
						if (colIndex >= 0 && width >= 0) {
							sheet.setColumnWidth(colIndex, (int) (width * 256)); // POI uses 1/256
																					// of a
																					// character
																					// width
						}
					}
				}

				// Apply row heights if specified
				Map<String, Object> rowHeights = (Map<String, Object>) sheetMap.get(PROP_ROW_HEIGHTS);
				if (rowHeights != null) {
					for (Map.Entry<String, Object> entry : rowHeights.entrySet()) {
						Integer rowIndex = asInt(entry.getKey());
						Double height = asDouble(entry.getValue());
						if (rowIndex >= 0 && height >= 0) {
							Row row = sheet.getRow(rowIndex);
							if (row == null) {
								row = sheet.createRow(rowIndex);
							}
							row.setHeightInPoints((float) (double) height);
						}
					}
				}
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return outputStream.toByteArray();
		}
	}

	@SuppressWarnings("unchecked")
	private void processSheetContent(Workbook workbook, Sheet sheet, Object content) {
		List<Object> contentList = (List<Object>) asList(content);

		// Check if this is matrix format or cell format
		if (isMatrixFormat(contentList)) {
			processMatrixFormat(workbook, sheet, contentList);
		} else {
			processCellFormat(workbook, sheet, contentList);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean isMatrixFormat(List<Object> contentList) {
		if (contentList.isEmpty()) {
			return true;
		}

		Object firstRow = contentList.get(0);
		if (firstRow instanceof List) {
			return true;
		}

		// Check if all items are maps with row/col properties
		for (Object item : contentList) {
			if (item instanceof Map) {
				Map<String, Object> itemMap = (Map<String, Object>) item;
				if (asInt(itemMap.get(PROP_CELL_ROW)) != -1 && asInt(itemMap.get(PROP_CELL_COL)) != -1) {
					return false;
				}
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private void processMatrixFormat(Workbook workbook, Sheet sheet, List<Object> contentList) {
		// Track current cursor position
		int currentRow = 0;
		int currentCol = 0;
		
		// Track merged regions for collision detection
		MergedRegionTracker regionTracker = new MergedRegionTracker();

		// Cache styles to avoid creating duplicates
		StyleCache styleCache = new StyleCache(workbook, this);

		// Process each row in the matrix
		for (int matrixRow = 0; matrixRow < contentList.size(); matrixRow++) {
			Object rowObj = contentList.get(matrixRow);
			List<Object> rowList = (List<Object>) asList(rowObj);
			
			// Reset column position at the start of each row
			currentCol = 0;
			
			// Process each cell in the row
			for (int matrixCol = 0; matrixCol < rowList.size(); matrixCol++) {
				Object cellValue = rowList.get(matrixCol);
				
				// Check if the cell value is an excelCell map
				if (cellValue instanceof Map) {
					Map<String, Object> cellMap = (Map<String, Object>) cellValue;
					if (cellMap.containsKey("content")) {
						// This is an excelCell object
						Object content = cellMap.get("content");
						Map<String, Object> styleProps = new HashMap<>();
						
						// Extract styling properties
						for (Map.Entry<String, Object> entry : cellMap.entrySet()) {
							String key = entry.getKey();
							if (!key.equals("content") && !key.equals("row") && !key.equals("col")) {
								styleProps.put(key, entry.getValue());
							}
						}
						
						// Use cursor position if row/col not specified
						Integer cellRow = asInt(cellMap.get("row"));
						Integer cellCol = asInt(cellMap.get("col"));
						
						int targetRow = (cellRow != -1) ? cellRow : currentRow;
						int targetCol = (cellCol != -1) ? cellCol : currentCol;
						
						// Ensure we have the correct row
						Row targetExcelRow = sheet.getRow(targetRow);
						if (targetExcelRow == null) {
							targetExcelRow = sheet.createRow(targetRow);
						}
						
						createCell(workbook, sheet, targetExcelRow, targetCol, content, styleProps, regionTracker,
							styleCache);

					}
				} else {
				
				// Regular cell value (not an excelCell) - create at current cursor position
				Row excelRow = sheet.getRow(currentRow);
				if (excelRow == null) {
					excelRow = sheet.createRow(currentRow);
				}
				
				createCell(workbook, sheet, excelRow, currentCol, cellValue, null, regionTracker, styleCache);
			}
				// Advance cursor position
				currentCol++;

			}
			
			// Advance to next row after processing all columns
			currentRow++;
		}
	}

	@SuppressWarnings("unchecked")
	private void processCellFormat(Workbook workbook, Sheet sheet, List<Object> contentList) {
		// Track current cursor position for sequential cell placement
		int currentRow = 0;
		int currentCol = 0;

		// Track merged regions for collision detection
		MergedRegionTracker regionTracker = new MergedRegionTracker();

		// Cache styles to avoid creating duplicates
		StyleCache styleCache = new StyleCache(workbook, this);

		for (Object cellObj : contentList) {
			Map<String, Object> cellMap = (Map<String, Object>) asMap(cellObj);
			Integer row = asInt(cellMap.get(PROP_CELL_ROW));
			Integer col = asInt(cellMap.get(PROP_CELL_COL));
			Object content = cellMap.get(PROP_CELL_CONTENT);

			// Determine target position using cursor if row/col not specified
			int targetRow = (row != -1) ? row : currentRow;
			int targetCol = (col != -1) ? col : currentCol;

			Row excelRow = sheet.getRow(targetRow);
			if (excelRow == null) {
				excelRow = sheet.createRow(targetRow);
			}

			// Extract styling properties
			Map<String, Object> styleProps = new HashMap<>();
			for (Map.Entry<String, Object> entry : cellMap.entrySet()) {
				String key = entry.getKey();
				if (!key.equals(PROP_CELL_CONTENT) && !key.equals(PROP_CELL_ROW) && !key.equals(PROP_CELL_COL)) {
					styleProps.put(key, entry.getValue());
				}
			}

			createCell(workbook, sheet, excelRow, targetCol, content, styleProps, regionTracker, styleCache);

			if (row != -1) {
				currentRow = row;
			}
			if (col != -1) {
				currentCol = col;
			}
			currentCol++;
		}
	}

	@SuppressWarnings("unchecked")
	private void createCell(Workbook workbook, Sheet sheet, Row row, int colNum, Object value,
			Map<String, Object> styleProps, MergedRegionTracker regionTracker, StyleCache styleCache) {
		// Check if content is a list/matrix (nested excel Cell)
		if (value instanceof List) {
			List<Object> nestedContent = (List<Object>) value;
			processNestedContent(workbook, sheet, nestedContent, row.getRowNum(), colNum, styleProps, regionTracker,
				styleCache);
			return;
		}

		// Check if this cell position conflicts with existing merged regions
		regionTracker.checkCellPosition(row.getRowNum(), colNum);

		Cell cell = row.createCell(colNum);

		// Apply styling if provided
		CellStyle cellStyle = styleCache.getOrCreateStyle(styleProps);
		if (cellStyle != null) {
			cell.setCellStyle(cellStyle);
		}

		// Handle optional cell merging
		if (styleProps != null) {
			Integer rowSpan = asInt(styleProps.get(PROP_ROW_SPAN));
			Integer colSpan = asInt(styleProps.get(PROP_COL_SPAN));

			if (rowSpan > 1 || colSpan > 1) {
				int firstRow = row.getRowNum();
				int firstCol = colNum;
				int lastRow = firstRow + ((rowSpan > 1) ? rowSpan - 1 : 0);
				int lastCol = firstCol + ((colSpan > 1) ? colSpan - 1 : 0);

				CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
				sheet.addMergedRegion(region); // apache will throw error if conflict
				regionTracker.addRegion(region);
			}
		}

		// Set cell value based on type
		if (value instanceof Map) {
			Map<String, Object> valueMap = (Map<String, Object>) value;
			if (valueMap.containsKey(PROP_CELL_FORMULA)) {
				// Formula cell
				String formula = asString(valueMap.get(PROP_CELL_FORMULA));
				if (formula != null) {
					cell.setCellFormula(formula);
				}
			} else {
				// Default to string
				cell.setCellValue(MetaLabelProvider.INSTANCE.getLabel(value));
			}
		} else if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else {
			cell.setCellValue(MetaLabelProvider.INSTANCE.getLabel(value));
		}
	}

	/**
	 * Processes a nested content within an excelCell. This enables positioning entire inner lists
	 * of excel cells at specific locations with inherited styling.
	 */
	@SuppressWarnings("unchecked")
	private void processNestedContent(Workbook workbook, Sheet sheet, List<Object> nestedContent,
			int startRow, int startCol, Map<String, Object> containerStyle,
			MergedRegionTracker regionTracker, StyleCache styleCache) {

		// Process each row in the nested table
		for (int rowOffset = 0; rowOffset < nestedContent.size(); rowOffset++) {
			Object rowObj = nestedContent.get(rowOffset);
			List<Object> rowList = (List<Object>) asList(rowObj);

			// Process each cell in the row
			for (int colOffset = 0; colOffset < rowList.size(); colOffset++) {
				Object cellValue = rowList.get(colOffset);
				int targetRow = startRow + rowOffset;
				int targetCol = startCol + colOffset;

				// Handle nested excelCell objects with relative positioning
				if (cellValue instanceof Map) {
					Map<String, Object> cellMap = (Map<String, Object>) cellValue;
					if (cellMap.containsKey("content")) {
						// This is an excelCell - process with relative positioning
						processNestedExcelCell(workbook, sheet, cellMap, targetRow, targetCol,
							containerStyle, regionTracker, styleCache);
					}
				} else {
					// Regular cell value with inherited container style
					Row excelRow = sheet.getRow(targetRow);
					if (excelRow == null) {
						excelRow = sheet.createRow(targetRow);
					}
					createCell(workbook, sheet, excelRow, targetCol, cellValue, containerStyle, regionTracker,
						styleCache);
				}
			}
		}
	}

	/**
	 * Processes an excelCell within a nested content with relative positioning and style
	 * inheritance.
	 */
	@SuppressWarnings("unchecked")
	private void processNestedExcelCell(Workbook workbook, Sheet sheet, Map<String, Object> cellMap,
			int baseRow, int baseCol, Map<String, Object> containerStyle,
			MergedRegionTracker regionTracker, StyleCache styleCache) {

		Object content = cellMap.get("content");
		Integer rowOffset = asInt(cellMap.get("row"));
		Integer colOffset = asInt(cellMap.get("col"));

		// Calculate final position (base + relative offset)
		int targetRow = (rowOffset != -1) ? (baseRow + rowOffset) : baseRow;
		int targetCol = (colOffset != -1) ? (baseCol + colOffset) : baseCol;

		// Extract and merge styles (cell styles override container styles)
		Map<String, Object> mergedStyle = mergeStyles(containerStyle, cellMap);

		// Create the cell
		Row excelRow = sheet.getRow(targetRow);
		if (excelRow == null) {
			excelRow = sheet.createRow(targetRow);
		}

		createCell(workbook, sheet, excelRow, targetCol, content, mergedStyle, regionTracker, styleCache);
	}

	/**
	 * Merges container styles with cell-specific styles. Cell styles take precedence over container
	 * styles.
	 */
	private Map<String, Object> mergeStyles(Map<String, Object> containerStyle, Map<String, Object> cellStyle) {
		Map<String, Object> mergedStyle = new HashMap<>();

		// Start with container style
		if (containerStyle != null) {
			mergedStyle.putAll(containerStyle);
		}

		// Add cell-specific properties, overriding container ones
		if (cellStyle != null) {
			for (Map.Entry<String, Object> entry : cellStyle.entrySet()) {
				String key = entry.getKey();
				// Skip special properties that aren't styles
				if (!key.equals("content") && !key.equals("row") && !key.equals("col")) {
					mergedStyle.put(key, entry.getValue());
				}
			}
		}

		return mergedStyle;
	}
	private CellStyle createCellStyle(Workbook workbook, Map<String, Object> styleProps) {
		if (styleProps == null || styleProps.isEmpty()) {
			return null;
		}

		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();

		// Font properties
		if (styleProps.containsKey(PROP_BOLD)) {
			font.setBold(asBoolean(styleProps.get(PROP_BOLD)));
		}

		if (styleProps.containsKey(PROP_ITALIC)) {
			font.setItalic(asBoolean(styleProps.get(PROP_ITALIC)));
		}

		if (styleProps.containsKey(PROP_FONT_SIZE)) {
			Double fontSize = asDouble(styleProps.get(PROP_FONT_SIZE));
			if (fontSize != null) {
				font.setFontHeightInPoints(fontSize.shortValue());
			}
		}

		if (styleProps.containsKey(PROP_FONT_FAMILY)) {
			String fontFamily = asString(styleProps.get(PROP_FONT_FAMILY));
			if (fontFamily != null) {
				font.setFontName(fontFamily);
			}
		}

		// Font color
		if (styleProps.containsKey(PROP_COLOR)) {
			String color = asString(styleProps.get(PROP_COLOR));
			if (color != null) {
				if (workbook instanceof XSSFWorkbook && font instanceof XSSFFont) {
					XSSFColor xssfColor = parseColorXSSF(color);
					if (xssfColor != null) {
						((XSSFFont) font).setColor(xssfColor);
					}
				} else {
					// Fallback to indexed colors for non-XSSF workbooks
					IndexedColors indexedColor = parseColor(color);
					if (indexedColor != null) {
						font.setColor(indexedColor.getIndex());
					}
				}
			}
		}

		style.setFont(font);

		// Background color
		if (styleProps.containsKey(PROP_BACKGROUND)) {
			String backgroundColor = asString(styleProps.get(PROP_BACKGROUND));
			if (backgroundColor != null) {
				if (workbook instanceof XSSFWorkbook) {
					XSSFColor xssfColor = parseColorXSSF(backgroundColor);
					if (xssfColor != null) {
						XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
						xssfStyle.setFillForegroundColor(xssfColor);
						xssfStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					}
				} else {
					// Fallback to indexed colors for non-XSSF workbooks
					IndexedColors indexedColor = parseColor(backgroundColor);
					if (indexedColor != null) {
						style.setFillForegroundColor(indexedColor.getIndex());
						style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					}
				}
			}
		}

		// Border styling
		if (styleProps.containsKey(PROP_BORDER_TOP)) {
			style.setBorderTop(parseBorderStyle(asString(styleProps.get(PROP_BORDER_TOP))));
		}
		if (styleProps.containsKey(PROP_BORDER_BOTTOM)) {
			style.setBorderBottom(parseBorderStyle(asString(styleProps.get(PROP_BORDER_BOTTOM))));
		}
		if (styleProps.containsKey(PROP_BORDER_LEFT)) {
			style.setBorderLeft(parseBorderStyle(asString(styleProps.get(PROP_BORDER_LEFT))));
		}
		if (styleProps.containsKey(PROP_BORDER_RIGHT)) {
			style.setBorderRight(parseBorderStyle(asString(styleProps.get(PROP_BORDER_RIGHT))));
		}

		// Alignment
		if (styleProps.containsKey(PROP_ALIGN)) {
			style.setAlignment(parseHorizontalAlignment(asString(styleProps.get(PROP_ALIGN))));
		}
		if (styleProps.containsKey(PROP_VALIGN)) {
			style.setVerticalAlignment(parseVerticalAlignment(asString(styleProps.get(PROP_VALIGN))));
		}

		// Number format
		if (styleProps.containsKey(PROP_NUMBER_FORMAT)) {
			String numberFormat = asString(styleProps.get(PROP_NUMBER_FORMAT));
			if (numberFormat != null) {
				style.setDataFormat(workbook.createDataFormat().getFormat(numberFormat));
			}
		}

		return style;
	}

	private IndexedColors parseColor(String colorName) {
		if (colorName == null) {
			return null;
		}

		// Convert common color names to indexed colors
		switch (colorName.toLowerCase()) {
			case "black": return IndexedColors.BLACK;
			case "white": return IndexedColors.WHITE;
			case "red": return IndexedColors.RED;
			case "green": return IndexedColors.GREEN;
			case "blue": return IndexedColors.BLUE;
			case "yellow": return IndexedColors.YELLOW;
			case "lightyellow":
				return IndexedColors.LEMON_CHIFFON;
			case "orange": return IndexedColors.ORANGE;
			case "purple": return IndexedColors.VIOLET;
			case "gray": case "grey": return IndexedColors.GREY_50_PERCENT;
			case "lightgray":
			case "lightgrey":
				return IndexedColors.GREY_25_PERCENT;
			case "darkgray":
			case "darkgrey":
				return IndexedColors.GREY_50_PERCENT;
			default:
				return null;
		}
	}

	private XSSFColor parseColorXSSF(String colorName) {
		if (colorName == null) {
			return null;
		}

		// Handle hex colors
		if (colorName.startsWith("#")) {
			try {
				byte[] rgb = hexToRgb(colorName);
				return new XSSFColor(rgb, null);
			} catch (IllegalArgumentException e) {
				// Invalid hex format, fall back to named colors
			}
		}

		// Enhanced color support for XSSF with brighter colors
		switch (colorName.toLowerCase()) {
			case "black":
				return new XSSFColor(new byte[] { (byte) 0, (byte) 0, (byte) 0 }, null);
			case "white":
				return new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null);
			case "red":
				return new XSSFColor(new byte[] { (byte) 255, (byte) 0, (byte) 0 }, null);
			case "green":
				return new XSSFColor(new byte[] { (byte) 0, (byte) 128, (byte) 0 }, null);
			case "blue":
				return new XSSFColor(new byte[] { (byte) 0, (byte) 0, (byte) 255 }, null);
			case "yellow":
				return new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 0 }, null);
			case "lightyellow":
				return new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 224 }, null);
			case "orange":
				return new XSSFColor(new byte[] { (byte) 255, (byte) 165, (byte) 0 }, null);
			case "purple":
				return new XSSFColor(new byte[] { (byte) 128, (byte) 0, (byte) 128 }, null);
			case "gray":
			case "grey":
				return new XSSFColor(new byte[] { (byte) 128, (byte) 128, (byte) 128 }, null);
			case "lightgray":
			case "lightgrey":
				return new XSSFColor(new byte[] { (byte) 211, (byte) 211, (byte) 211 }, null);
			case "darkgray":
			case "darkgrey":
				return new XSSFColor(new byte[] { (byte) 169, (byte) 169, (byte) 169 }, null);
			default: return null;
		}
	}

	private byte[] hexToRgb(String hexColor) {
		if (hexColor.startsWith("#")) {
			hexColor = hexColor.substring(1);
		}

		if (hexColor.length() == 6) {
			int rgb = Integer.parseInt(hexColor, 16);
			return new byte[] {
				(byte) ((rgb >> 16) & 0xFF),
				(byte) ((rgb >> 8) & 0xFF),
				(byte) (rgb & 0xFF)
			};
		} else if (hexColor.length() == 3) {
			// Handle shorthand hex like #FFF
			char r = hexColor.charAt(0);
			char g = hexColor.charAt(1);
			char b = hexColor.charAt(2);
			return new byte[] {
				(byte) Integer.parseInt("" + r + r, 16),
				(byte) Integer.parseInt("" + g + g, 16),
				(byte) Integer.parseInt("" + b + b, 16)
			};
		}

		throw new IllegalArgumentException("Invalid hex color format: " + hexColor);
	}

	private BorderStyle parseBorderStyle(String borderStyle) {
		if (borderStyle == null) {
			return null;
		}

		switch (borderStyle.toLowerCase()) {
			case "none": return BorderStyle.NONE;
			case "thin": return BorderStyle.THIN;
			case "medium": return BorderStyle.MEDIUM;
			case "dashed": return BorderStyle.DASHED;
			case "dotted": return BorderStyle.DOTTED;
			case "thick": return BorderStyle.THICK;
			case "double": return BorderStyle.DOUBLE;
			case "hair": return BorderStyle.HAIR;
			case "medium_dashed": return BorderStyle.MEDIUM_DASHED;
			case "dash_dot": return BorderStyle.DASH_DOT;
			case "medium_dash_dot": return BorderStyle.MEDIUM_DASH_DOT;
			case "dash_dot_dot": return BorderStyle.DASH_DOT_DOT;
			case "medium_dash_dot_dot": return BorderStyle.MEDIUM_DASH_DOT_DOT;
			case "slanted_dash_dot": return BorderStyle.SLANTED_DASH_DOT;
			default: return BorderStyle.THIN;
		}
	}

	private HorizontalAlignment parseHorizontalAlignment(String alignment) {
		if (alignment == null) {
			return null;
		}

		switch (alignment.toLowerCase()) {
			case "left": return HorizontalAlignment.LEFT;
			case "center": return HorizontalAlignment.CENTER;
			case "right": return HorizontalAlignment.RIGHT;
			case "fill": return HorizontalAlignment.FILL;
			case "justify": return HorizontalAlignment.JUSTIFY;
			case "center_selection": return HorizontalAlignment.CENTER_SELECTION;
			case "distributed": return HorizontalAlignment.DISTRIBUTED;
			default: return HorizontalAlignment.GENERAL;
		}
	}

	private VerticalAlignment parseVerticalAlignment(String alignment) {
		if (alignment == null) {
			return null;
		}

		switch (alignment.toLowerCase()) {
			case "top": return VerticalAlignment.TOP;
			case "center": return VerticalAlignment.CENTER;
			case "bottom": return VerticalAlignment.BOTTOM;
			case "justify": return VerticalAlignment.JUSTIFY;
			case "distributed": return VerticalAlignment.DISTRIBUTED;
			default: return VerticalAlignment.BOTTOM;
		}
	}

	private BinaryData createBinaryData(String fileName, byte[] data) throws IOException {
		return BinaryDataFactory.createMemoryBinaryData(
			new ByteArrayInputStream(data),
			data.length,
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			fileName);
	}

	/**
	 * {@link MethodBuilder} for {@link ExcelFile}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ExcelFile> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("content")
			.optional("name", "data")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public ExcelFile build(Expr expr, SearchExpression[] args) {
			return new ExcelFile(getName(), args);
		}

	}
}