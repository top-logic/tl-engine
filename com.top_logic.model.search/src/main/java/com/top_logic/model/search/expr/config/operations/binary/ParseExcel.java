/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} parsing an Excel-File. By Default the active sheet will be imported. But
 * you can select to import all sheets, by setting the boolean IMPORT_ALL_SHEETS to true or a list
 * of Sheets given by their index or names.
 * 
 * @author <a href="mailto:maziar.behdju@top-logic.com">Maziar Behdju</a>
 */
public class ParseExcel extends GenericMethod {

	/**
	 * Position of the input xls or xlsx file (for parsing) in arguments
	 */
	private static final int INPUT_DATA_INDEX = 0;

	/**
	 * Position of the Boolean, which indicates if sheets in excel file should be imported.
	 */
	private static final int IMPORT_ALL_SHEETS_INDEX = 1;

	/**
	 * Position of the sheet lists indices for import.
	 */
	private static final int IMPORT_SELECTED_SHEETS_INDEX = 2;

	/**
	 * Position of given parsers in arguments
	 */
	private static final int HEADERS_INDEX = 3;

	private static final int IMPORT_ACTIVE_SHEET_INDEX = 4;

	/**
	 * Position of the Boolean, which indicates if the raw result of parsing is returned (as a List
	 * of Strings).
	 */
	private static final int RAW_INDEX = 5;

	private int NUMBER_OF_SHEETS_FOR_IMPORT = 1;

	private List<Object> IMPORT_SELECTED_SHEETS = Collections.emptyList();

	private Map<Object, List<Object>> HEADERS_AT = new HashMap<>();

	private Boolean IMPORT_ALL_SHEETS = false;

	private Boolean IMPORT_ACTIVE_SHEET = false;

	private String DEFAULT_HEADER = "A";

	private String OVERHEADER = "->";

	private String FROM_TO = ":";

	private String FOR_ALL = "all";

	/**
	 * Creates a {@link ParseExcel}.
	 */
	public ParseExcel(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ParseExcel(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[INPUT_DATA_INDEX];
		if (input == null) {
			return null;
		}

		IMPORT_ALL_SHEETS = asBoolean(arguments[IMPORT_ALL_SHEETS_INDEX]);
		IMPORT_ACTIVE_SHEET = asBoolean(arguments[IMPORT_ACTIVE_SHEET_INDEX]);
		IMPORT_SELECTED_SHEETS = (List<Object>) asList(arguments[IMPORT_SELECTED_SHEETS_INDEX]);
		fixSelectedSheetsEntries();
		HEADERS_AT = getHeaderMap(arguments);

		Map<Object, List<List<Object>>> importedSheets = parse(input);

		Boolean raw = asBoolean(arguments[RAW_INDEX]);
		if (raw == true) {
			return determineReturn(importedSheets);
		} else {
			HashMap<Object, List<Map<String, Object>>> mapedResult = new HashMap<>();
			ArrayList<String> headers = new ArrayList<>();
			for (Object key : importedSheets.keySet()) {
				List<List<Object>> value = importedSheets.get(key);
				ArrayList<Map<String, Object>> sheetEntries = new ArrayList<>();
				if (HEADERS_AT.containsKey(key) || HEADERS_AT.containsKey(FOR_ALL)) {
					ArrayList<Integer> headersPositions = new ArrayList<>();
					setHeaders(headers, headersPositions, key, value);
					if (headersPositions.size() > 0) {
						for (int i = headersPositions.get(0) + 1; i < value.size(); ++i) {
							List<Object> rawRow = value.get(i);
							AddRowMap(headers, rawRow, headersPositions, sheetEntries);
						}
						headers.clear();
					} else {
						generateDefaultHeaders(headers, value);
						for (List<Object> rawRow : value) {
							AddRowMap(headers, rawRow, null, sheetEntries);
						}
					}
				} else {
					generateDefaultHeaders(headers, value);
					for (List<Object> rawRow : value) {
						AddRowMap(headers, rawRow, null, sheetEntries);
					}
				}
				mapedResult.put(key, sheetEntries);
			}
			return determineReturn(mapedResult);
		}
	}

	private Object determineReturn(Map<Object, ?> result) {
		if (result.size() == 1) {
			Object key = result.keySet().toArray()[0];
			if (IMPORT_SELECTED_SHEETS.contains(key)) {
				return result;
			}
			return result.get(key);
		}
		return result;
	}

	private void setHeaders(ArrayList<String> headers, ArrayList<Integer> headersPositions, Object key,
			List<List<Object>> value) {

		List<Object> headersPositionsForKey = HEADERS_AT.get(key);
		List<Object> headersPositionsForALL = HEADERS_AT.get(FOR_ALL);
		if (headersPositionsForKey == null && headersPositionsForALL == null) {
			generateDefaultHeaders(headers, value);
		} else {
			List<Object> headersPositionsList;
			if (headersPositionsForKey != null) {
				headersPositionsList = headersPositionsForKey;
			} else {
				headersPositionsList = headersPositionsForALL;
			}
			for (int i = 0; i < headersPositionsList.size(); ++i) {
				Object headObject = headersPositionsList.get(i);

				if (headObject instanceof String) {
					String trimmedHeadObject = ((String) headObject).replace(" ", "");
					String[] splited;
					String[] multiHeader = trimmedHeadObject.split(OVERHEADER);
					if (multiHeader.length == 1) {
						splited = trimmedHeadObject.split(FROM_TO);
					} else {
						splited = multiHeader[1].split(FROM_TO);
					}
					if (splited.length == 1) {
						int[] parsedCell = parseCellID(splited[0]);
						if (headersPositions.size() == 0) {
							setStartRow(-1, parsedCell, headersPositions);
						} else {
							setStartRow(headersPositions.get(0), parsedCell, headersPositions);
						}
						headers.add((String) value.get(parsedCell[1]).get(parsedCell[0]));
						headersPositions.add(parsedCell[0]);
					} else {
						int[] parsedStart = parseCellID(splited[0]);
						int[] parsedEnd = parseCellID(splited[1]);
						if (headersPositions.size() == 0) {
							setStartRow(-1, parsedStart, headersPositions);
						} else {
							setStartRow(headersPositions.get(0), parsedStart, headersPositions);
						}
						setStartRow(headersPositions.get(0), parsedEnd, headersPositions);
						for (int j = parsedStart[0]; j <= parsedEnd[0]; ++j) {
							String header = (String) value.get(parsedStart[1]).get(j);
							if (multiHeader.length != 1) {
								int[] parsedCell = parseCellID(multiHeader[0]);
								header = (String) value.get(parsedCell[1]).get(parsedCell[0]) + OVERHEADER + header;
							}
							headers.add(header);
							headersPositions.add(j);
						}
					}
				} else {
					throw new TopLogicException(
						I18NConstants.ERROR_UNSUPPORTED_CONTENT_TYPE__VALUE_MSG.fill(headObject));
				}
			}
		}

	}

	private void setStartRow(int row, int[] parsedCell, ArrayList<Integer> headersPositions) {
		boolean isNextRow = (row == parsedCell[1] - 1);
		boolean isPreviousRow = (row == parsedCell[1] + 1);
		if (row == -1 || isNextRow) {
			row = parsedCell[1];
		} else if (row != parsedCell[1] && !isNextRow && !isPreviousRow) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_CELL_ENTRY__VALUE_MSG.fill(parsedCell[1], row));
		}
		if (headersPositions.size() == 0) {
			headersPositions.add(row);
		} else if (isNextRow) {
			headersPositions.set(0, row);
		}
	}

	private int[] parseCellID(String input) {
		int[] result = new int[2];
		int columnNumber = 0;
		int row = 0;
		int rowFactor = 10;
		int columnFactor = 26;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) >= '0' && input.charAt(i) <= '9') {
				row = row * rowFactor + (input.charAt(i) - '0');
			} else {
				columnNumber = columnNumber * columnFactor + (input.charAt(i) - 'A' + 1);
			}
		}
		result[0] = columnNumber - 1;
		result[1] = row - 1;
		return result;
	}

	private void AddRowMap(ArrayList<String> headers, List<Object> rawRow,
			ArrayList<Integer> headersPositions, ArrayList<Map<String, Object>> sheetEntries) {
		HashMap<String, Object> rowEntries = new HashMap<>();
		if (headersPositions == null || headersPositions.size() == 0) {
			for (int j = 0; j < headers.size(); ++j) {
				if (j < rawRow.size()) {
					rowEntries.put(headers.get(j), rawRow.get(j));
				} else {
					rowEntries.put(headers.get(j), "");
				}
			}
		} else {
			for (int j = 1; j < headersPositions.size(); ++j) {
				int column = headersPositions.get(j);
				String header = headers.get(j - 1);
				String[] multiHeader = header.split(OVERHEADER);
				if (column < rawRow.size()) {
					Object columnValue = rawRow.get(column);
					addValueToRowMap(columnValue, rowEntries, header, multiHeader);
				} else {
					addValueToRowMap("", rowEntries, header, multiHeader);
				}
			}
		}
		sheetEntries.add(rowEntries);
	}

	private void addValueToRowMap(Object value, HashMap<String, Object> rowEntries, String header,
			String[] multiHeader) {
		if (multiHeader.length == 1) {
			rowEntries.put(header, value);
		} else {
			HashMap<String, Object> multiHeaderMap =
				(HashMap<String, Object>) rowEntries.get(multiHeader[0]);
			if (multiHeaderMap == null) {
				multiHeaderMap = new HashMap<>();
			}
			multiHeaderMap.put(multiHeader[1], value);
			rowEntries.put(multiHeader[0], multiHeaderMap);
		}
	}

	private void generateDefaultHeaders(ArrayList<String> headers, List<List<Object>> value) {
		int neededHeadersCount = getMaxSize(value);
		String nextHeader = DEFAULT_HEADER;
		for (int i = 0; i < neededHeadersCount; ++i) {
			headers.add(nextHeader);
			nextHeader = incrementHeader(nextHeader);
		}
	}

	private int getMaxSize(List<List<Object>> list) {
		int maxSize = -1;
		for (List<Object> l : list) {
			if (l.size() > maxSize) {
				maxSize = l.size();
			}
		}
		return maxSize;
	}

	private String incrementHeader(String toInrease) {
		char[] charArray = toInrease.toCharArray();
		if (charArray[charArray.length - 1] != 'Z') {
			++charArray[charArray.length - 1];
			return new String(charArray);
		} else if (charArray.length == 1) {
			return "AA";
		} else {
			char[] newRequest = Arrays.copyOfRange(charArray, 0, charArray.length - 1);
			return incrementHeader(new String(newRequest)) + "A";
		}
	}

	private void fixSelectedSheetsEntries() {
		ArrayList<Object> newList = new ArrayList<>();
		for (int i = 0; i < IMPORT_SELECTED_SHEETS.size(); ++i) {
			Object value = IMPORT_SELECTED_SHEETS.get(i);
			if (value instanceof Number) {
				int intValue = asInt(value);
				newList.add(intValue);
			} else {
				newList.add(value);
			}
		}
		IMPORT_SELECTED_SHEETS = newList;
	}

	@SuppressWarnings("resource")
	private Map<Object, List<List<Object>>> parse(Object input) {
		try {
			BinaryDataSource data = (BinaryDataSource) input;

			MimeType mimeType = new MimeType(data.getContentType());
			String charset = mimeType.getParameter("charset");
			if (charset == null) {
				charset = ParseCSV.ISO_8859;
			}

			InputStream in = data.toData().getStream();
			Workbook workbook = WorkbookFactory.create(in);


			Map<Object, List<List<Object>>> importedSheets = new HashMap<>();
			if (IMPORT_ALL_SHEETS == true) {
				NUMBER_OF_SHEETS_FOR_IMPORT = workbook.getNumberOfSheets();

				for (int sheetIndex = 0; sheetIndex < NUMBER_OF_SHEETS_FOR_IMPORT; ++sheetIndex) {
					Sheet sheet = workbook.getSheetAt(sheetIndex);
					addSheetValues(sheet, importedSheets, sheetIndex);
				}
			} else {
				if (IMPORT_SELECTED_SHEETS.size() > 0) {
					Iterator<Object> sheetIterator = IMPORT_SELECTED_SHEETS.iterator();
					while (sheetIterator.hasNext()) {
						Object importSheet = sheetIterator.next();
						Sheet sheet;
						if (importSheet instanceof Integer) {
							sheet = workbook.getSheetAt((int) importSheet);
						} else {
							sheet = workbook.getSheet((String) importSheet);
						}
						if (sheet != null) {
							addSheetValues(sheet, importedSheets, importSheet);
						}
					}
				} else if(IMPORT_ACTIVE_SHEET) {
					Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
					addSheetValues(sheet, importedSheets, workbook.getActiveSheetIndex());
				} else {
					Sheet sheet = workbook.getSheetAt(0);
					addSheetValues(sheet, importedSheets, 0);
				}
			}
			workbook.close();

			return importedSheets;

		} catch (IOException | MimeTypeParseException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED__MSG_EXPR.fill(ex.getMessage(), this),
				ex);
		}
	}


	private void addSheetValues(Sheet sheet, Map<Object, List<List<Object>>> importedSheets, Object importSheet) {
		int lastRow = sheet.getLastRowNum();
		List<List<Object>> rows = new ArrayList<>();

		for (int i = 0; i < lastRow + 1; ++i) {
			Row nextRow = sheet.getRow(i);
			rows.add(getRowValue(nextRow));
		}

		importedSheets.put(importSheet, rows);
	}

	private List<Object> getRowValue(Row row) {
		List<Object> columns = new ArrayList<>();
		if (row != null) {
			int lastCell = row.getLastCellNum();

			for (int j = 0; j < lastCell; ++j) {
				Cell nextCell = row.getCell(j);
				columns.add(getCellValue(nextCell));
			}
		}
		return columns;
	}

	private Object getCellValue(Cell cell) {
		if (cell != null) {
			switch (cell.getCellType()) {
				case STRING:
					return cell.getRichStringCellValue().getString();
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						return cell.getDateCellValue();
					} else {
						return cell.getNumericCellValue();
					}
				case BOOLEAN:
					return cell.getBooleanCellValue();
				case FORMULA:
					return cell.getCellFormula();
				default:
					return "";
			}
		} else {
			return "";
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<Object, List<Object>> getHeaderMap(Object[] arguments) {
		Map<Object, List<Object>> result = new HashMap<>();
		Object value = arguments[HEADERS_INDEX];
		if (value != null) {
			if (value instanceof Map<?, ?>) {
				Map<?, ?> parsers = asMap(arguments[HEADERS_INDEX]);
				for (Entry<?, ?> entry : parsers.entrySet()) {
					Object key = entry.getKey();
					List<Object> headers = (List<Object>) asList(entry.getValue());
					if (key instanceof String) {
						result.put((String) key, headers);
					} else {
						int keyAsInt = asInt(key);
						result.put(keyAsInt, headers);
					}
				}
			} else {
				List<Object> headers = (List<Object>) asList(arguments[HEADERS_INDEX]);
				result.put(FOR_ALL, headers);
			}
		}
		return result;
	}


	/**
	 * {@link MethodBuilder} for {@link ParseExcel}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<ParseExcel> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("importAllSheets", false)
			.optional("importSheets")
			.optional("headers")
			.optional("importActiveSheet", false)
			.optional("raw", false)
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ParseExcel build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new ParseExcel(getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}
	}
}

