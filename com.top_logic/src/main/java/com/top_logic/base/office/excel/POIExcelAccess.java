/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelValue.CellPosition;
import com.top_logic.base.office.excel.POIRowGroupDescription.GroupDescription;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.util.Resources;

/**
 * Excel export using the Apache POI library. 
 * <p>The POI library lacks a good formula support at the moment. 
 * It cannot evaluate shared formulas, that is formulas created 
 * by copy and paste.</p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class POIExcelAccess extends ExcelAccess {

	/**
	 * {@link ExcelAccess.ExcelAccessFactory} creating {@link POIExcelAccess} instances.
	 * 
	 * @see ExcelAccess.GlobalConfig#getFactory()
	 */
	public static final class Factory implements ExcelAccessFactory {

		@Override
		public ExcelAccess newInstance() {
			return new POIExcelAccess();
		}

	}

	private static final String DEFAULT_SHEETNAME = "Sheet1";
    
    /* ---------------------------------------------------------------------- *
     * Attributes & References
     * ---------------------------------------------------------------------- */
    
    /** The formula evaluator that is used while reading excel files. */
	protected FormulaEvaluator formularEvaluator;

    /**
	 * Creates a new POIExcelAccess object.
	 * 
	 * @see Factory#newInstance()
	 */
	protected POIExcelAccess() {
        super();
    }

    @Override
	public Object[][] getValues(BinaryData aFile, String aSheet) throws Exception {
		POIUtil.setUserTimeZoneAndLocale();
		try (InputStream theInput = aFile.getStream()) {

            Workbook theWorkbook = this.getWorkbook(theInput, aFile.getName());
            /* Extract the sheet we will read from. */
			Sheet theSheet = null;
            if(aSheet==null){
            	theSheet= theWorkbook.getSheetAt(0);
            }
            else{
            	theSheet = theWorkbook.getSheet(aSheet);
            }
            /* Create a new workbook reader. */
            WorkbookReader theReader = new WorkbookReader(theSheet);
            /* Create the return value array with the initial values. */
			int lastColumnNumber = POIUtil.calculateLastColumnNumber(theSheet);
			int lastRowNumber = theSheet.getLastRowNum();
			final Object[][] theValues = new Object[lastColumnNumber][lastRowNumber + 1];
            
            
            /* Create a callback. */
            CellCallback theCallback = new CellCallback() {
                @Override
				public void foundCell(Cell aCell, String aSheetName, int aRow, int aColumn) {
					theValues[aColumn][aRow] = getValue(aSheetName, aCell);
                }
            };
            /* Process the workbook and afterwards close the input stream. */
            theReader.processWorkbook(theCallback);
            
            return theValues;
        } catch (IOException theIoe) {
			throw new OfficeException(Resources.getInstance().getString(I18NConstants.ERROR_READING_FILE),
				theIoe);
		} finally {
			POIUtil.resetUserTimeZoneAndLocale();
		}
    }
    
    @Override
	public Map<String, Object> getValues(BinaryData aFile, Map<String, Set<String>> someCoords) {
		POIUtil.setUserTimeZoneAndLocale();
		try (InputStream theInput = aFile.getStream()) {
            Workbook                  theWorkbook = this.getWorkbook(theInput, aFile.getName());
            final Map<String, Object> theValues   = new HashMap<>();

            this.formularEvaluator = POIUtil.newFormulaEvaluator(theWorkbook);

            for (String theKey : someCoords.keySet()) {
				Sheet theSheet = theWorkbook.getSheet(theKey);

                if (theSheet != null) {
                    Collection<String> theCoords = someCoords.get(theKey);

                    for (String thePos : theCoords) {
                        if (!thePos.startsWith("chart!")) {
                            int[] theCoord = POIUtil.convertCellName(thePos);
							Row   theRow   = theSheet.getRow(theCoord[0]);

                            if (theRow != null) {
								Cell theCell = theRow.getCell(theCoord[1]);
    
                                if (theCell != null) {
									theValues.put(theKey + '!' + thePos, getValue(theSheet.getSheetName(), theCell));
                                }
                            }
						} else {
							// TODO #9333: Add extraction of charts
                        }
                    }
                }
            }
            
            formularEvaluator = null;

            return theValues;
		} catch (IOException ex) {
			Logger.error(Resources.getInstance().getString(I18NConstants.ERROR_READING_FILE), ex, this);

            return Collections.emptyMap();
		} finally {
			POIUtil.resetUserTimeZoneAndLocale();
        }
    }

    @Override
	public final boolean setValuesDirect(InputStream aSource, File aDest, ExcelValue[] aValueList, boolean autoFit)
			throws OfficeException {
		List<POIColumnWidthDescription> columnDescriptions = Collections.emptyList();
		List<POIRowGroupDescription> groups = Collections.emptyList();
		POIColumnFilterDescription colFilterDescription = null;
		return setValuesDirect(aSource, aDest, aValueList, autoFit, columnDescriptions, groups, colFilterDescription);
	}

	/**
	 * This method closes the given input stream after the reading finished.
	 * 
	 * @param colDescriptions
	 *        List of {@link POIColumnWidthDescription} defining with for columns.
	 * @param someGroups
	 *        Definition of row groups.
	 * @param colFilterDescription
	 *        Definition of the Filter in Excel. May be <code>null</code>.
	 * @throws OfficeException
	 *         thrown if the destination is read only or if the stream throws an error while reading
	 *         from it
	 * @see #createValueSetter(Workbook)
	 */
	public boolean setValuesDirect(InputStream aSource, File aDest, ExcelValue[] aValueList, boolean autoFit,
			List<POIColumnWidthDescription> colDescriptions, List<POIRowGroupDescription> someGroups,
			POIColumnFilterDescription colFilterDescription) throws OfficeException {
		POIUtil.setUserTimeZoneAndLocale();
        try {
            // Obtain the workbook.
			Workbook            theWorkbook = this.getWorkbook(aSource, aDest.getName());
			POIExcelValueSetter theSetter   = this.createValueSetter(theWorkbook);

            for (int i = 0; i < aValueList.length; i++) {
                ExcelValue theValue = aValueList[i];
				String     theSheet = theSetter.getSheetNameNotNull(theValue.getSheet());

                // Store the value in the cell.
                this.setValue(theSetter, new CellPosition(theSheet, theValue.getRow(), theValue.getCol()), theValue);
            }

			if (!autoFit) {
				for (POIColumnWidthDescription description : colDescriptions) {
					Sheet sheet = description.getSheet(theWorkbook);
					sheet.setColumnWidth(description.getColumn(), description.getWidth());
				}
			}

			for (POIRowGroupDescription description : someGroups) {
				Sheet sheet = description.getSheet(theWorkbook);
				/* Display the collapse button under the group. If displayed above the group, it is
				 * not possible to build a group with first row as start row. */
				sheet.setRowSumsBelow(true);
				for (GroupDescription group : description.getGroups()) {
					sheet.groupRow(group._startRow, group._endRow);
					sheet.setRowGroupCollapsed(group._startRow, group._collapsed);
				}
			}

			if (colFilterDescription != null) {
				Sheet sheet = colFilterDescription.getSheet(theWorkbook);
				sheet.setAutoFilter(colFilterDescription.getRegion().createCellRangeAddress());

			}

			// Auto fit the workbook if requested.
            if (autoFit) {
            	theSetter.performAutoAdjust();
            }

            // Save the modified workbook.
            POIUtil.writeWorkbook(aDest, theWorkbook);

            return true;
        } catch (IOException theIoe) {
			throw new OfficeException(Resources.getInstance().getString(I18NConstants.ERROR_WRITING_FILE),
				theIoe);
		} finally {
			POIUtil.resetUserTimeZoneAndLocale();
        }
    }

    @Override
	public boolean setValues(InputStream aSource, File aDest, Map<String, Object> aMap) throws OfficeException {
		POIUtil.setUserTimeZoneAndLocale();
        try {
            // Obtain the workbook.
			Workbook            theWorkbook = this.getWorkbook(aSource, aDest.getName());
			POIExcelValueSetter theSetter   = this.createValueSetter(theWorkbook);

			for (Entry<String, Object> theEntry : aMap.entrySet()) {
                this.setValue(theSetter, new CellPosition(theEntry.getKey()), theEntry);
            }

			// Auto fit the workbook.
        	theSetter.performAutoAdjust();

        	// Save the modified workbook.
            POIUtil.writeWorkbook(aDest, theWorkbook);

            return true;
        } catch (IOException theIoe) {
			throw new OfficeException(Resources.getInstance().getString(I18NConstants.ERROR_WRITING_FILE),
				theIoe);
		} finally {
			POIUtil.resetUserTimeZoneAndLocale();
        }
    }
    
    @Override
	public Map<String, Object> getValues(BinaryData aSource) throws OfficeException {
		try (InputStream in = aSource.getStream()) {
			String theName = aSource.getName();
			return this.getValues(in, theName.substring(theName.lastIndexOf('.')));
		} catch (IOException theFnfe) {
			throw new OfficeException(Resources.getInstance().getString(I18NConstants.ERROR_OPENING_FILE),
				theFnfe);
		}
    }
    
    @Override
    public Map<String, Object> getValues(InputStream aSource) throws OfficeException {
    	return this.getValues(aSource, ExcelAccess.XLS_EXT);
    }

    /**
	 * Extract all texts from the given input stream (which must contain some excel stuff).
	 * 
	 * <p>
	 * The returned map will contain a unique key, representing the position of the found value in
	 * the file. The value will be the found value.
	 * </p>
	 * 
	 * <p>
	 * This method opens the given file and closes it after the reading finished.
	 * </p>
	 * 
	 * @param aSource
	 *        The stream to be read.
	 * @param anExt
	 *        The extension to be used when opening the work book ({@link POIUtil#XLS_SUFFIX} or
	 *        {@link POIUtil#XLSX_SUFFIX}).
	 * @throws NullPointerException
	 *         if the given input stream is <code>null</code>
	 * @throws OfficeException
	 *         if an error occurred while reading the file
	 */
    public Map<String, Object> getValues(InputStream aSource, String anExt) throws OfficeException {
		POIUtil.setUserTimeZoneAndLocale();
        try {
            /* Create the return map. */
            final Map<String, Object> theMap = new HashMap<>();
            /* Open the workbook for reading. */
			Workbook theWorkbook = POIUtil.getWorkbook(aSource, anExt);
            /* Create a new workbook reader. */
            WorkbookReader theReader = new WorkbookReader(theWorkbook);
            /* Create a callback. */
            CellCallback theCellCallback = new CellCallback() {
                @Override
				public void foundCell(Cell aCell, String aSheetName, int aRow, int aColumn) {
					theMap.put(POIUtil.convertToCellName(aSheetName, aRow, aColumn), getValue(aSheetName, aCell));
                }
            };
            /* Process the workbook and afterwards close the input stream. */
            theReader.processWorkbook(theCellCallback);
            aSource.close();
            
            return theMap;
        } catch (IOException theIoe) {
			throw new OfficeException(Resources.getInstance().getString(I18NConstants.ERROR_READING_FILE),
				theIoe);
		} finally {
			POIUtil.resetUserTimeZoneAndLocale();
        }
    }

    /** 
     * Sets a value into a cell. This method first resolves the cell to set the
     * value in from the given destination and then calls the appropriate method
     * to set the value into the cell.
     * 
     * @param aSetter    The helper class to do the operation.
     * @param aRef       The reference to the requested field.
     * @param aValue     The value to be set.
     * @see   #toExcelValue(Object, CellPosition)
     * @see   POIExcelValueSetter#setValue(CellPosition, ExcelValue)
     */
    protected void setValue(POIExcelValueSetter aSetter, CellPosition aRef, Object aValue) {
        ExcelValue theValue = this.toExcelValue(aValue, aRef);

        if (theValue != null) { 
        	try {
        		aSetter.setValue(aRef, theValue);
        	}
        	catch (Exception ex) {
        		Logger.error("Unable to set value in cell: " + aRef, ex, this);
        	}
        }
    }

	/** 
	 * Convert the given value into an excel value.
	 * 
	 * <p>If the given value is already an {@link ExcelValue} it'll be returned directly.</p>
	 * 
	 * @param    anObject    The value to be converted.
	 * @param    aRef        The cell reference the value has to be stored to.
	 * @return   The requested value.
	 */
	@SuppressWarnings("rawtypes")
	protected ExcelValue toExcelValue(Object anObject, CellPosition aRef) {
        // Wrapping the values into ExcelValues to use general style cache mechanism
        if (anObject instanceof Entry) {
        	return new ExcelValue(aRef.getRow(), aRef.getColumn(), ((Entry) anObject).getValue()); 
        }
        else if (!(anObject instanceof ExcelValue)) {
        	return new ExcelValue(aRef.getRow(), aRef.getColumn(), anObject);
        }
        else {
        	return (ExcelValue) anObject;
        }
	}

	/** 
	 * Create a setter for excel values.
	 * 
	 * @param    aWorkbook   The workbook to be handled. 
	 * @return   The requested new instance of a setter.
	 */
	protected POIExcelValueSetter createValueSetter(Workbook aWorkbook) {
		return new POIExcelValueSetter(aWorkbook);
	}

    /* ---------------------------------------------------------------------- *
     * Overrideable Getter Methods
     * ---------------------------------------------------------------------- */
    
    /**
	 * The value from given {@link Cell}.
	 * 
	 * @param sheetName
	 *        The context of the accessed {@link Cell} used for error reporting.
	 * @param cell
	 *        The cell to access.
	 * @return The application value of the given cell.
	 */
	protected final Object getValue(String sheetName, Cell cell) {
		try {
			return tryGetValue(cell);
		} catch (RuntimeException ex) {
			StringBuilder message = new StringBuilder();
			message.append("Failed to get cell value at row ");
			message.append(cell.getRowIndex());
			message.append(", column ");
			message.append(cell.getColumnIndex());
			message.append(" of sheet '");
			message.append(sheetName);
			message.append("'.");
			throw new RuntimeException(message.toString(), ex);
		}
	}

	/**
	 * Try accessing the application value of the given {@link Cell} depending on its
	 * {@link Cell#getCellType()}.
	 * 
	 * @param aCell
	 *        The cell to access.
	 * @return The application value of the given cell.
	 */
    protected Object tryGetValue(Cell aCell) {
        switch (aCell.getCellType()) {
			case STRING:
                return getStringValue(aCell);
			case BOOLEAN:
                return Boolean.valueOf(getBooleanValue(aCell));
			case NUMERIC:
                /* Dates are handled here because internally they are stored as
                 * double values. */
				if (DateUtil.isCellDateFormatted(aCell)) {
                    return getDateValue(aCell);
                }
                
                return Double.valueOf(getNumericValue(aCell));
			case FORMULA:
                /* Evaluate the formula. */
                CellValue theValue = this.formularEvaluator.evaluate(aCell);
                /* Get the result in the appropriate format. For this we have to
                 * set the cell type to the value format, otherwise we would
                 * get a ClassCastException. */
                switch (theValue.getCellType()) {
					case STRING:
						String value = theValue.getStringValue();
						// beware of cells which have a list of values and no value is choosen
						if (!StringServices.isEmpty(value)) {
							aCell.setCellType(CellType.STRING);
						}
						aCell.setCellValue(POIUtil.newRichTextString(aCell, value));
                        return getStringValue(aCell);
					case BOOLEAN:
                        /* This is necessary, because the single call to set
                         * the cell type to boolean causes the underlying
                         * implementation to try to preserve the existing value.
                         * This fails for boolean formulas. */
						aCell.setBlank();
						aCell.setCellType(CellType.BOOLEAN);
                        aCell.setCellValue(theValue.getBooleanValue());
                        return Boolean.valueOf(getBooleanValue(aCell));
					case NUMERIC:
						aCell.setCellType(CellType.NUMERIC);
                        aCell.setCellValue(theValue.getNumberValue());
                        
                        /* Dates are handled here because internally they are stored as
                         * double values. */
						if (DateUtil.isCellDateFormatted(aCell)) {
                            return getDateValue(aCell);
                        }
                        
                        return Double.valueOf(getNumericValue(aCell));
                    default:
						aCell.setCellType(CellType.STRING);
						aCell.setCellValue(POIUtil.newRichTextString(aCell, theValue.getStringValue()));
                        return aCell.getRichStringCellValue().getString();
                }
            default:
                return aCell.getRichStringCellValue().getString();
        }
    }
    
    /** 
     * Returns the value from the cell as a string.
     * 
     * @param aCell the cell to read the value from
     * @return the cell value as a string
     */
    protected String getStringValue(Cell aCell) {
        return aCell.getRichStringCellValue().getString();
    }
    
    /** 
     * Returns the value from the cell as a boolean.
     * 
     * @param aCell the cell to read the value from
     * @return the cell value as a boolean
     */
    protected boolean getBooleanValue(Cell aCell) {
        return aCell.getBooleanCellValue();
    }
    
    /** 
     * Returns the value from the cell as a double.
     * 
     * @param aCell the cell to read the value from
     * @return the cell value as a double
     */
    protected double getNumericValue(Cell aCell) {
        return aCell.getNumericCellValue();
    }
    
    /** 
     * Returns the value from the cell as a date.
     * 
     * @param aCell the cell to read the value from
     * @return the cell value as a date
     */
    protected Date getDateValue(Cell aCell) {
        return aCell.getDateCellValue();
    }
    
    /**
     * POI cannot implement this.
     * 
     * This heavily depends on several unknown defaults found in the customers 
     * version of Excel and cannot be emulated as of now. 
     * 
     * @throws UnsupportedOperationException always
     */
    @Override
	public void autoFitColumns(File aFile) throws OfficeException {
        throw new UnsupportedOperationException("Use real Excel POI can't help here.");
    }
 
    // KHA: Not implemented as of now please compare With Excel9/10/11 implementation
    
    @Override
	protected void closeDocument(Object aDoc, Stack<?> aSomeRefs) throws Exception {
        // Not needed for POI ?
    }

    @Override
	protected Object createApplication(Stack<?> aSomeRefs) throws Exception {
        // Not needed for POI ?
        return null;
    }

    @Override
	protected Object getDocument(BinaryData aName, Object anApplication, Stack<?> aSomeRefs) throws Exception {
        // Not needed for POI ?
        return null;
    }

    @SuppressWarnings("rawtypes")
	@Override
	protected Map getFields(Object aDoc, Stack<?> aSomeRefs) throws Exception {
        // Not needed for POI ?
        return null;
    }

	@Override
	protected Map<String, Object> getValuesFromDoc(Object aDoc, Mapping<String, Object> aMapping, Stack<?> aSomeRefs) throws Exception {
        // Not needed for POI ?
        return null;
    }

    @Override
	protected String getVersion(Stack<?> aSomeRefs) throws Exception {
        throw new UnsupportedOperationException("POI does not depend of the installed Excel.");
    }

    @Override
	protected void save(Object anAppl, Object aDoc, String aName, Stack<?> someRefs) throws Exception {
        // Not needed for POI ?
    }

    @Override
	protected boolean setResult(Object anAppl, Object aDoc, Object anObject, String aKey, Object aValue, Stack<?> aSomeRefs) throws Exception {
    	return false;
        // Not needed for POI ?
    }
    
    @Override
	protected void releaseStack(Stack<?> aSomeRefs) {
        // No evil COM-Objects, nothing to release.
    }

	/** 
	 * Return the workbook for the given input stream.
	 * 
	 * @param    aStream   The stream to get the workbook for.
	 * @param    aName     File name to get the correct extension from.
	 * @return   The requested workbook.
	 * @throws   IOException    When reading the stream fails.
	 */
	protected Workbook getWorkbook(InputStream aStream, String aName) throws IOException {
		return POIUtil.getWorkbook(aStream, aName.substring(aName.lastIndexOf('.')));
	}

    private Cell resolveCell(Workbook aWorkbook, String aSheetName, int aRow, int aColumn) {
		Sheet theSheet;
        if (aSheetName != null) {
            /* If no sheet name is provided create a default sheet. */
            theSheet = POIUtil.createIfNull(aSheetName, aWorkbook);
        } else {
            /* Query the sheet from the workbook. */
            theSheet = POIUtil.createIfNull(DEFAULT_SHEETNAME, aWorkbook);
        }
        
		Row theHSSFRow = POIUtil.createIfNull(aRow, theSheet);
        return POIUtil.createIfNull(aColumn, theHSSFRow, theSheet);
    }
    
    /* ---------------------------------------------------------------------- *
     * Private classes
     * ---------------------------------------------------------------------- */
    
	/**
	 * This interface represents a callback that will be registered with a
	 * {@linkplain POIExcelAccess.WorkbookReader reader}. Each time the workbook reader encounters a
	 * cell this callback gets called.
	 * 
	 * @author <a href=mailto:cdo@top-logic.com>cdo</a>
	 */
    private static interface CellCallback {
        
        /** 
         * Will be called each time the {@linkplain POIExcelAccess.WorkbookReader reader} encounters
         * a new cell.
         * 
         * @param aCell the cell that is read
         * @param aSheetName the sheet name of the cell. Can be
         *        <code>null</code> or empty
         * @param aRow the row number of the cell
         * @param aColumn the column number of the cell
         */
        void foundCell(Cell aCell, String aSheetName, int aRow, int aColumn);
        
    }
    
    /**
     * The workbook reader reads all cells that are inside the workbook. For
     * each cell found inside the workbook, a {@linkplain POIExcelAccess.CellCallback callback}
     * will be called to signal a new cell.
     *
     * @author    <a href=mailto:cdo@top-logic.com>cdo</a>
     */
    private class WorkbookReader {
        
        /* ------------------------------------------------------------------ *
         * Attributes & References
         * ------------------------------------------------------------------ */
        /** The workbook to read from. */
		private final Workbook workbook;
        
        /** The sheet to read from. */
		private final Sheet sheet;
        
        /* ------------------------------------------------------------------ *
         * Constructors
         * ------------------------------------------------------------------ */
        
        /** 
         * Creates a new WorkbookReader object that reads from the given
         * sheet.
         */
        public WorkbookReader(Sheet aSheet) {
			this.workbook = aSheet.getWorkbook();
            this.sheet = aSheet;
        }
        
        /** 
         * Creates a new WorkbookReader object that reads from the given
         * workbook.
         * 
         * @param aWorkbook the workbook to read from
         */
        public WorkbookReader(Workbook aWorkbook) {
            this.workbook = aWorkbook;
            this.sheet = null;
        }
        
        /** 
         * Starts reading the workbook. Uses the given callback for signaling
         * new cells.
         * 
         * @param aCellCallback the cell callback that gets called for each cell
         */
        public void processWorkbook(CellCallback aCellCallback) {
			if (this.sheet == null) {
                for (int i = 0; i < this.workbook.getNumberOfSheets(); i++) {
					Sheet theSheet = workbook.getSheetAt(i);
					formularEvaluator = POIUtil.newFormulaEvaluator(this.workbook);
                    processSheet(theSheet, this.workbook.getSheetName(i), aCellCallback);
                }
                
                formularEvaluator = null;
            } else {
				formularEvaluator = POIUtil.newFormulaEvaluator(this.workbook);
                processSheet(this.sheet, null, aCellCallback);
                formularEvaluator = null;
            }
        }
        
        /* ------------------------------------------------------------------ *
         * Private Methods
         * ------------------------------------------------------------------ */
		private void processSheet(Sheet aSheet, String aSheetName, CellCallback aCellCallback) {
			for (Iterator<Row> theRowIterator = aSheet.rowIterator(); theRowIterator.hasNext();) {
				Row theRow = theRowIterator.next();
				for (Iterator<Cell> theCellIterator = theRow.cellIterator(); theCellIterator.hasNext();) {
					Cell theCell = theCellIterator.next();
                    aCellCallback.foundCell(theCell, aSheetName, theRow.getRowNum(), theCell.getColumnIndex());
                }
            }
        }
        
    }

}
