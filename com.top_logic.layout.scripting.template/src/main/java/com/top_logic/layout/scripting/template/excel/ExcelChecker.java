/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.POIUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.StringValue;
import com.top_logic.layout.scripting.template.action.BusinessOperationTemplateAction;
import com.top_logic.layout.scripting.template.action.TemplateAction.Parameter;
import com.top_logic.layout.scripting.template.excel.ExcelActionRegistry.Config;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelFileConfig.BasicSheetSet;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelSheetConfig;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelSheetConfig.SheetOperator;
import com.top_logic.layout.scripting.util.LazyActionProvider;

/**
 * Central parser to convert {@link ExcelRow}s into an {@link ActionChain}.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExcelChecker extends LazyActionProvider {

	private static final String MICROSOFTS_TEMP_FILE_PREFIX = "~$";

	/**
	 * I18N values.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public interface I18NConfig extends ConfigurationItem {
        /** German translation. */
    	@Name("de")
        String getDE();

    	/** English translation. */
        @Name("en")
        String getEN();
    }

	/**
	 * The first line usually contains the column headers. Therefore, the default start row is the
	 * second row, which has the index 1.
	 */
	public static final int DEFAULT_START_ROW = 1;

	private static final int START_COLUMN = 0;

	// Lower case only
	private static final String[] COLUMN_HEADERS = {
		"ergebnis",
		"aktion",
		"fachobjekt",
		"kontext",
		"parameter"
	};

	private final String _name;

	private final BinaryData _excelLocation;

	private final BasicSheetSet _basicSheetSet;

	private final List<ExcelSheetConfig> _sheetConfigs;

	private final int _startRow;

	/**
	 * Creates an {@link ExcelChecker} with the {@link #DEFAULT_START_ROW}.
	 * 
	 * @param excelLocation
	 *        Must not be <code>null</code>.
	 * @param sheetNames
	 *        Must not be <code>null</code>. Is allowed to be empty.
	 */
	public ExcelChecker(String name, BinaryData excelLocation, String... sheetNames) {
		this(name, excelLocation, DEFAULT_START_ROW, sheetNames);
	}

	/**
	 * Creates an {@link ExcelChecker}.
	 * 
	 * @param excelLocation
	 *        Must not be <code>null</code>.
	 * @param sheetNames
	 *        Must not be <code>null</code>. Is allowed to be empty.
	 */
	public ExcelChecker(String name, BinaryData excelLocation, int startRow, String... sheetNames) {
		this(name, excelLocation, startRow, BasicSheetSet.NONE, buildSheetsConfig(sheetNames));
	}

	/**
	 * Creates an {@link ExcelChecker} with the {@link #DEFAULT_START_ROW}.
	 * 
	 * @param excelLocation
	 *        Must not be <code>null</code>.
	 * @param basicSheetSet
	 *        Must not be <code>null</code>.
	 * @param sheetConfigs
	 *        Is allowed to be <code>null</code>.
	 */
	public ExcelChecker(String name, BinaryData excelLocation, BasicSheetSet basicSheetSet,
			List<ExcelSheetConfig> sheetConfigs) {
		this(name, excelLocation, DEFAULT_START_ROW, basicSheetSet, sheetConfigs);
	}

	/**
	 * Creates an {@link ExcelChecker}.
	 * 
	 * @param excelLocation
	 *        Must not be <code>null</code>.
	 * @param basicSheetSet
	 *        Must not be <code>null</code>.
	 * @param sheetConfigs
	 *        Is allowed to be <code>null</code>.
	 */
	public ExcelChecker(String name, BinaryData excelLocation, int startRow, BasicSheetSet basicSheetSet,
			List<ExcelSheetConfig> sheetConfigs) {
		if (StringServices.isEmpty(name)) {
			throw new IllegalArgumentException("The name is required and must not be null or empty.");
		}
		if (excelLocation == null) {
			throw new NullPointerException("No excel file location given.");
		}
		if (basicSheetSet == null) {
			throw new NullPointerException("basicSheetSet must not be null.");
		}
		_name = name;
		_excelLocation = excelLocation;
		_startRow = startRow;
		_basicSheetSet = basicSheetSet;
		_sheetConfigs = CollectionUtil.nonNull(sheetConfigs);
	}

	/** The name of the excel checker is never <code>null</code> or empty. */
	public String getName() {
		return _name;
	}

	private static List<ExcelSheetConfig> buildSheetsConfig(String[] sheetNames) {
		List<ExcelSheetConfig> sheetConfigs = new ArrayList<>();
		for (String sheetName : sheetNames) {
			ExcelSheetConfig sheetConfig = TypedConfiguration.newConfigItem(ExcelSheetConfig.class);
			sheetConfig.setName(sheetName);
			sheetConfig.setSheetOperator(SheetOperator.ADD);
			sheetConfigs.add(sheetConfig);
		}
		return sheetConfigs;
	}

	@Override
	protected ApplicationAction createAction() {
		try {
			return parse();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create ApplicationActions for excel '" + _excelLocation
				+ "', basic sheet set: " + _basicSheetSet + ", sheet configurations: '" + _sheetConfigs + "': "
				+ ex.getMessage(), ex);
		}
	}

	private ActionChain parse() throws Exception {
		List<ApplicationAction> theActions = new ArrayList<>();

		Workbook workbook = getWorkbook(_excelLocation);
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		List<Sheet> sheets = getRequestedSheets(workbook, evaluator);
		for (Sheet sheet : sheets) {
			theActions.add(processSheet(sheet, evaluator, _startRow));
		}

		ActionChain theChain = ActionFactory.actionChain(theActions);
		String name = _excelLocation.getName();
		int index = name.lastIndexOf('.');
		if (index >= 0) {
			name = name.substring(0, index);
		}
		theChain.setComment(name);
		return theChain;
	}

	private List<Sheet> getRequestedSheets(Workbook workbook, FormulaEvaluator evaluator) {
		Map<String, ExcelSheetConfig> unappliedSheetConfigs = buildSheetMap();
		List<Sheet> sheets = new ArrayList<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			Sheet sheet = workbook.getSheetAt(i);
			String sheetName = sheet.getSheetName();
			if (isRequested(sheet, unappliedSheetConfigs.get(sheetName), evaluator)) {
				sheets.add(sheet);
			}
			unappliedSheetConfigs.remove(sheetName);
		}
		checkForUnknownRequestedSheets(unappliedSheetConfigs);
		return sheets;
	}

	private void checkForUnknownRequestedSheets(Map<String, ExcelSheetConfig> sheetMap) {
		removeUnrequestedSheets(sheetMap);
		if (!sheetMap.isEmpty()) {
			throw new IllegalArgumentException("Sheets not found in excel file, but potentially requested: " + sheetMap);
		}
	}

	private void removeUnrequestedSheets(Map<String, ExcelSheetConfig> sheetMap) {
		Iterator<ExcelSheetConfig> iterator = sheetMap.values().iterator();
		while (iterator.hasNext()) {
			ExcelSheetConfig sheetEntry = iterator.next();
			if (sheetEntry.getSheetOperator() == SheetOperator.REMOVE) {
				iterator.remove();
			}
		}
	}

	private Map<String, ExcelSheetConfig> buildSheetMap() {
		Map<String, ExcelSheetConfig> sheets = new HashMap<>();
		for (ExcelSheetConfig sheetConfig : _sheetConfigs) {
			sheets.put(sheetConfig.getName(), sheetConfig);
		}
		return sheets;
	}

	private boolean isRequested(Sheet sheet, ExcelSheetConfig sheetConfig, FormulaEvaluator evaluator) {
		switch (_basicSheetSet) {
			case NONE: {
				if (sheetConfig == null || sheetConfig.getSheetOperator() == SheetOperator.REMOVE) {
					return false;
				}
				if (sheetConfig.getSheetOperator() == SheetOperator.ADD) {
					return true;
				}
				return isActive(sheet, evaluator);
			}
			case ALL: {
				if (sheetConfig == null || sheetConfig.getSheetOperator() == SheetOperator.ADD) {
					return true;
				}
				if (sheetConfig.getSheetOperator() == SheetOperator.REMOVE) {
					return false;
				}
				return isActive(sheet, evaluator);
			}
			case FILE_DEFINED: {
				if (sheetConfig == null || sheetConfig.getSheetOperator() == SheetOperator.SHEET_DEFINED) {
					return isActive(sheet, evaluator);
				}
				return sheetConfig.getSheetOperator() == SheetOperator.ADD;
			}
		}
		throw new UnreachableAssertion("Unknown " + BasicSheetSet.class + " instance '"
			+ StringServices.getObjectDescription(_basicSheetSet) + "'.");
	}

	/**
	 * Is the given sheet active?
	 * <p>
	 * A sheet is active, if it contains the action column headers in the row over the start row.
	 * More precisely: The cell values have to start with:
	 * "Ergebnis,Aktion,Fachobjekt,Kontext,Parameter"
	 * </p>
	 * */
	public boolean isActive(Sheet sheet, FormulaEvaluator evaluator) {
		// The row before the first test has to contain the column headers.
		int columnHeaderRow = _startRow - 1;
		Row row = sheet.getRow(columnHeaderRow);
		if (row == null) {
			return false;
		}
		for (int i = START_COLUMN; i < COLUMN_HEADERS.length + START_COLUMN; i++) {
			Cell columnHeaderCell = row.getCell(i);
			if (columnHeaderCell == null) {
				return false;
			}
			CellValue columnHeaderValue = evaluator.evaluate(columnHeaderCell);
			if (columnHeaderValue.getCellType() != CellType.STRING) {
				return false;
			}
			String columnHeaderText = columnHeaderValue.getStringValue();
			if (columnHeaderText.isEmpty()) {
				return false;
			}
			if (!columnHeaderText.toLowerCase().startsWith(COLUMN_HEADERS[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Execute the test as described in the given sheet.
	 * 
	 * @param sheet
	 *        The sheet to get the input from, must not be <code>null</code>.
	 * @param evaluator
	 *        Must not be <code>null</code>.
	 * @param ignoreRows
	 *        The row to start parsing from (to ignore the header rows).
	 * @return The action chain to be executed, never <code>null</code>.
	 * @throws Exception
	 *         If errors occurred in processing the tests.
	 */
	private ApplicationAction processSheet(Sheet sheet, FormulaEvaluator evaluator, int ignoreRows)
			throws Exception {
		Config excelActionRegistry = ExcelActionRegistry.getExcelActionRegistry();

		List<ApplicationAction> actions = new ArrayList<>();

		for (ExcelRow row : getSheetRows(sheet, evaluator)) {
			if (ignoreRows > 0) {
				ignoreRows--;
			} else {
				ApplicationAction action = prepareRow(row, excelActionRegistry);
				actions.add(action);
			}
		}

		// Group by comments.
		int end = actions.size();
		for (int n = end - 1; n >= 0; n--) {
			ApplicationAction action = actions.get(n);
			if (isComment(action)) {
				Optional<Parameter> textParam = ((BusinessOperationTemplateAction) action).getParameters().stream()
					.filter(p -> "text".equals(p.getName())).findFirst();

				ActionChain group = ActionFactory.actionChain(actions.subList(n, end));
				if (textParam.isPresent()) {
					ModelName text = textParam.get().getValue();
					if (text instanceof StringValue) {
						group.setComment(((StringValue) text).getString());
					}
				}
				actions.set(n, group);
				for (int m = end - 1; m > n; m--) {
					actions.remove(m);
				}
				end = n;
			}
		}

		ActionChain result = ActionFactory.actionChain(actions);
		result.setComment(sheet.getSheetName());
		return result;
	}

	private boolean isComment(ApplicationAction action) {
		if (action instanceof BusinessOperationTemplateAction) {
			return "Kommentar".equals(((BusinessOperationTemplateAction) action).getBusinessAction());
		}
		return false;
	}

	/** 
     * Convert an excel row into an application action.
     * 
     * @param    aRow    The row to be executed, must not be <code>null</code>.
     * @return   The action represented by the given row, never <code>null</code>.
     * @throws   ConfigurationException   When reading the configuration fails.
     */
	private ApplicationAction prepareRow(ExcelRow aRow, Config excelActionRegistry)
			throws ConfigurationException {
		return aRow.getAction(excelActionRegistry);
    }

    /**
	 * Store the given I18N strings to the system.
	 * 
	 * @param key
	 *        The I18N key to store the values for, must not be <code>null</code>.
	 * @param aDE
	 *        The German translation, may be <code>null</code>.
	 * @param anEN
	 *        The English translation, may be <code>null</code>.
	 */
	private static void saveI18N(ResKey key, String aDE, String anEN) {
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			tx.saveI18N(Locale.GERMAN, key, aDE);
			tx.saveI18N(Locale.ENGLISH, key, anEN);

			tx.commit();
		}
    }

    /**
	 * Store the given I18N strings to the system.
	 * 
	 * @param key
	 *        The I18N key to store the values for, must not be <code>null</code>.
	 * @param someI18N
	 *        The parameters containing the German ("de") and English ("en") translation.
	 */
	public static void saveI18N(ResKey key, I18NConfig someI18N) {
		ExcelChecker.saveI18N(key, someI18N.getDE(), someI18N.getEN());
    }

	/**
	 * Return {@link Workbook} of an excel file.
	 */
	private static Workbook getWorkbook(BinaryData excelFile) {
		try {
			InputStream theStream = excelFile.getStream();
			if (theStream == null) {
				throw new IllegalArgumentException("File '" + excelFile + "' cannot be found!");
			}

			try {
				return getWorkbook(excelFile.getName(), theStream);
			} finally {
				theStream.close();
			}
		} catch (IOException ex) {
			throw new RuntimeException("Failed to access excel file '" + excelFile + "'. Reason: " + ex.getMessage(),
				ex);
		}
	}

	private static Workbook getWorkbook(String anExcelName, InputStream aStream) throws IOException {
        return POIUtil.newWorkbook(anExcelName, aStream);
    }

	private List<ExcelRow> getSheetRows(Sheet aSheet, FormulaEvaluator evaluator) {
        List<ExcelRow> theRows = new ArrayList<>();
        String        theName = aSheet.getSheetName();

        for (Iterator<Row> theRowIterator = aSheet.rowIterator(); theRowIterator.hasNext();) {
            List<Object> theValues = new ArrayList<>();
            Row          theRow    = theRowIterator.next();
            short        theSize   = theRow.getLastCellNum();

            for (short thePos = 0; thePos < theSize; thePos++) {
				theValues.add(this.getCellValue(theRow, thePos, evaluator));
            }

			String filePath = _excelLocation.getName();
			ExcelRow theTestRow = new ExcelRow(filePath, theName, theRow, theValues);

            if (theTestRow.hasAction()) {
                theRows.add(theTestRow);
            }
        }

        return theRows;
    }

    @SuppressWarnings("unchecked")
	private <T> T getCellValue(Row aRow, int aActionColumn, FormulaEvaluator evaluator) {
        Cell theCell = aRow.getCell(aActionColumn);

        if (theCell != null) {
            Object theValue;

            switch (theCell.getCellType()) {
				case NUMERIC:
                    theValue = theCell.getNumericCellValue();
                    break;
				case BOOLEAN:
                    theValue = theCell.getBooleanCellValue();
                    break;
				case STRING:
                    theValue = theCell.getStringCellValue();
                    break;
				case FORMULA:
					theValue = this.evaluate(theCell, evaluator);
                	break;
                default : theValue = null;
            }

            return (T) theValue;
        }
        else { 
            return null;
        }
    }
    
	private Object evaluate(Cell aCell, FormulaEvaluator evaluator) {
		CellValue cellValue = evaluator.evaluate(aCell);

    	switch (cellValue.getCellType()) {
			case BOOLEAN:
    	        return cellValue.getBooleanValue();
			case NUMERIC:
    	    	return cellValue.getNumberValue();
			case STRING:
    	    	return cellValue.getStringValue();
    	    default:
    	        break;
    	}

    	return null;
    }

	/**
	 * Is the given {@link File} supported by this class?
	 * <p>
	 * Checks only the file ending, not the actual content.
	 * </p>
	 */
	public static boolean supportsFile(File file) {
		return supportsFile(file.getName().toLowerCase());
	}

	/**
	 * Is the given {@link BinaryData} supported by this class?
	 * <p>
	 * Checks only the file ending, not the actual content.
	 * </p>
	 */
	public static boolean supportsFile(BinaryData file) {
		return supportsFile(file.getName().toLowerCase());
	}

	/**
	 * Whether a file with that name is supported.
	 * <p>
	 * Checks only the file ending.
	 * </p>
	 */
	public static boolean supportsFile(String fileName) {
		if (fileName.startsWith(MICROSOFTS_TEMP_FILE_PREFIX)) {
			return false;
		}
		return fileName.endsWith(".xls") || fileName.endsWith(".xlsx");
	}

}
