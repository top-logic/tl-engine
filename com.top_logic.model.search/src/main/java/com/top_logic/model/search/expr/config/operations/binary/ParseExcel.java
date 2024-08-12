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

	/**
	 * Position of the Boolean, which indicates if the raw result of parsing is returned (as a List
	 * of Strings).
	 */
	private static final int RAW_INDEX = 4;

	private int NUMBER_OF_SHEETS_FOR_IMPORT = 1;

	private List<Object> IMPORT_SELECTED_SHEETS = Collections.emptyList();;

	private Map<String, List<Object>> HEADERS_AT = new HashMap<>();

	private Boolean IMPORT_ALL_SHEETS = false;

	private String DEFAULT_HEADER = "A";

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

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[INPUT_DATA_INDEX];
		if (input == null) {
			return null;
		}

		IMPORT_ALL_SHEETS = asBoolean(arguments[IMPORT_ALL_SHEETS_INDEX]);
		IMPORT_SELECTED_SHEETS = (List<Object>) asList(arguments[IMPORT_SELECTED_SHEETS_INDEX]);
		fixSelectedSheetsEntries();
		HEADERS_AT = getHeaderMap(arguments);

		Map<String, List<List<Object>>> importedSheets = parse(input);

		Boolean raw = asBoolean(arguments[RAW_INDEX]);
		if (raw == true) {
			return importedSheets;
		} else {
			HashMap<String, List<Map<String, Object>>> mapedResult = new HashMap<>();
			if (HEADERS_AT.size() > 0) {
				DEFAULT_HEADER = incrementHeader(DEFAULT_HEADER);
			} else {
				for (String key : importedSheets.keySet()) {
					ArrayList<String> headers = new ArrayList<>();
					List<List<Object>> value = importedSheets.get(key);
					generateDefaultHeaders(headers, value);
					ArrayList<Map<String, Object>> sheetEntries = new ArrayList<>();
					for (List<Object> rawRow : value) {
						HashMap<String, Object> rowEntries = createRowMap(headers, rawRow);
						sheetEntries.add(rowEntries);
					}
					mapedResult.put(key, sheetEntries);
				}
			}
			return mapedResult;
		}
	}

	private HashMap<String, Object> createRowMap(ArrayList<String> headers, List<Object> rawRow) {
		HashMap<String, Object> rowEntries = new HashMap<>();
		for (int j = 0; j < headers.size(); ++j) {
			if (j < rawRow.size()) {
				rowEntries.put(headers.get(j), rawRow.get(j));
			} else {
				rowEntries.put(headers.get(j), "");
			}
		}
		return rowEntries;
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
	private Map<String, List<List<Object>>> parse(Object input) {
		try {
			BinaryDataSource data = (BinaryDataSource) input;

			MimeType mimeType = new MimeType(data.getContentType());
			String charset = mimeType.getParameter("charset");
			if (charset == null) {
				charset = ParseCSV.ISO_8859;
			}

			InputStream in = data.toData().getStream();
			Workbook workbook = WorkbookFactory.create(in);


			Map<String, List<List<Object>>> importedSheets = new HashMap<>();
			if (IMPORT_ALL_SHEETS == true) {
				NUMBER_OF_SHEETS_FOR_IMPORT = workbook.getNumberOfSheets();

				for (int sheetIndex = 0; sheetIndex < NUMBER_OF_SHEETS_FOR_IMPORT; ++sheetIndex) {
					Sheet sheet = workbook.getSheetAt(sheetIndex);
					addSheetValues(sheet, importedSheets);
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
							addSheetValues(sheet, importedSheets);
						}
					}
				} else {
					Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
					addSheetValues(sheet, importedSheets);
				}
			}
			workbook.close();

			return importedSheets;

		} catch (IOException | MimeTypeParseException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED__MSG_EXPR.fill(ex.getMessage(), this),
				ex);
		}
	}


	private void addSheetValues(Sheet sheet, Map<String, List<List<Object>>> importedSheets) {
		String sheetName = sheet.getSheetName();
		int lastRow = sheet.getLastRowNum();
		List<List<Object>> rows = new ArrayList<>();

		for (int i = 0; i < lastRow + 1; ++i) {
			Row nextRow = sheet.getRow(i);
			rows.add(getRowValue(nextRow));
		}

		importedSheets.put(sheetName, rows);
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
	
	
	private Map<String, List<Object>> getHeaderMap(Object[] arguments) {
		Map<String, List<Object>> result = new HashMap<>();
		if (arguments[HEADERS_INDEX] != null) {
			Map<?, ?> parsers = asMap(arguments[HEADERS_INDEX]);
			for (Entry<?, ?> entry : parsers.entrySet()) {
				String sheetName = asString(entry.getKey());
				List<Object> headers = (List<Object>) asList(entry.getValue());
				result.put(sheetName, headers);
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

