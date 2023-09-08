/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.GenericDataObject;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.genericimport.GenericValueMapImpl;
import com.top_logic.element.genericimport.interfaces.GenericImporter.GenericFileImporter;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * Import the content of an XLS file using DataObjects as import object format. Finds the min and
 * max column and row and creates DataObjects from the data found using the Strings returned by
 * {@link #getColumns()} as attribute names. A more generic algorithm might use a header row to get
 * the column names.
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public abstract class AbstractExcelDOImporter extends DataObjectImportTask implements GenericFileImporter {

    public static final String DELETE_WHEN_DONE = "deleteWhenDone";
    public static final String IMPORT_FILE      = "importFile";
    public static final String SHEET_NAME       = "sheetName";
    public static final String MO_TYPE          = "moType";
    public static final String COL_NAME_ROW     = "colNameRow";
    public static final String FIRST_DATA_ROW   = "firstDataRow";

	/**
	 * when true file will be deleted after import
	 */
	protected boolean deleteWhenDone;

	/**
	 * The (xls/zip) file we import from
	 */
	protected BinaryData importFile;

	/** The XLS sheet name to use. */
	protected String sheetName;

	/**
	 * Min and max row and column that contain data.
	 * Note: First row is assumed to contain title data and is NOT imported!
	 */
	protected int minRow;
	protected int maxRow;
	protected int colNameRow;
	protected int firstDataRow;
	protected String minCol;
	protected String maxCol;

	protected String[] firstColumnNames;

	private String moType;

	/**
	 * The workbook representing the excel file.
	 */
	protected HashMap workbook;

	public AbstractExcelDOImporter(Properties aProp) {
	    super(aProp);
		{
	        String theFile = aProp.getProperty(IMPORT_FILE);
	        if (!StringServices.isEmpty(theFile)) {
				this.importFile = FileManager.getInstance().getData(aProp.getProperty(IMPORT_FILE));
	        }

	        this.sheetName      = aProp.getProperty(SHEET_NAME);
	        this.deleteWhenDone = Boolean.valueOf(aProp.getProperty(DELETE_WHEN_DONE, "false")).booleanValue();
	        this.moType         = aProp.getProperty(MO_TYPE, "ExternalContact");
	        this.firstDataRow   = Integer.valueOf(aProp.getProperty(FIRST_DATA_ROW, "1"));
	        this.colNameRow     = Integer.valueOf(aProp.getProperty(COL_NAME_ROW,   "0"));
	    }
	}

	/**
	 * Ctor with params needed.
	 *
	 * @param importSource		the import file. May be an XLS or a ZIP containing an XLS
	 * @param aSheetName		the sheet name in the XLS to use
	 * @param doDeleteWhenDone	if true, delete the file after upload
	 */
	public AbstractExcelDOImporter(BinaryData importSource, String aSheetName, boolean doDeleteWhenDone) {
		super("Import" + importSource.getName());
        this.sheetName      = aSheetName;
        this.importFile     = importSource;
        this.deleteWhenDone = doDeleteWhenDone;
        this.moType         = "ExternalContact";
        this.firstDataRow   = 1;
        this.colNameRow     = 0;
	}

	@Override
	public void setImportFile(BinaryData aFile) {
	    this.importFile       = aFile;
	    this.firstColumnNames = null;
	    this.workbook         = null;
	}

	@Override
	public String[] getSupportedFileExtensions() {
	    return new String[] { ".zip", getExtension() };
	}

	/** 
	 * Override this to read the column names from the XLS.
	 * The value is relative to the first row that contains data.
	 * -1 denote no column name row.
	 * 
	 * 
	 * @return true if we should read the first line as column names
	 */
	protected int getRelativeColumnNameRowNo() {
		return this.colNameRow;
	}
	
	/** 
	 * Override this to reading some lines as data.
	 * The value is relative to the first row that contains data.
	 * 
	 * @return true if the first line should be skipped
	 */
	protected int getRelativeFirstDataRowNo() {
		return this.firstDataRow;
	}

	/**
	 * Transform the XLS data into DataObjects
	 *
	 * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#getImportObjects(com.top_logic.element.structured.wrap.Mandator)
	 */
	@Override
	protected List getImportObjects(Mandator aMandator)
			throws DatabaseAccessException, DataObjectException {
    	Map theMap = this.getSheet(this.sheetName);

    	String[] excelColumns = this.getColumns();
    	if (! this.checkColumnFormat(excelColumns)) {
			StringBuilder error = new StringBuilder();
			error.append("Mismatch in columns of sheet '");
			error.append(this.sheetName);
			error.append("', Expected: '");
			error.append(StringServices.toString(getExpectedColumnNames()));
			error.append("', Actual: '");
			error.append(StringServices.toString(excelColumns));
			error.append("'");
			logError(error.toString(), null);
			throw new DatabaseAccessException(error.toString());
    	}
    	
    	// Transform data to DataObjects
    	// TODO fills DOs with empty Strings to fix checkFormat problems...
    	List theRes = new ArrayList();
    	int theColSize = excelColumns.length;
    	int theMinRow  = this.minRow + this.getRelativeFirstDataRowNo();
    	for (int theRow=theMinRow; theRow<=this.maxRow; theRow++) { // First row is title
    		String theColName = this.minCol;
			DataObject theDO =
				new GenericDataObject(this.moType, StringID.valueOf(this.moType + theColName + theRow), theColSize);
    		for (int theCol=0; theCol<theColSize; theCol++) {
    			Object theValue = theMap.get(theColName + theRow);
    			if (theValue== null) {
    				theValue = "";
    			}
    			theDO.setAttributeValue(excelColumns[theCol], theValue);
    			theColName = this.incExcelColumnName(theColName);
    		}

    		theRes.add(theDO);
    	}

    	return theRes;
	}

	protected boolean checkColumnFormat(String[] excelColumnNames) {
	    
	    String[] expected = this.getExpectedColumnNames();
	    
	    // No columns in Excel
	    if (excelColumnNames == null || expected == null) {
	        return false;
	    }

	    return ArrayUtil.equals(excelColumnNames, expected);
	}
	
	protected abstract String[] getExpectedColumnNames();
	
	@Override
	public List getPlainObjects() throws DatabaseAccessException, DataObjectException {
	    try {
	        List theList   = this.getImportObjects(null);
	        int  theSize   = theList.size();
	        List theResult = new ArrayList(theSize);
	        for (int i=0; i<theSize; i++) {
	            DataObject      theDO    = (DataObject) theList.get(i);
	            String[]        theAttrs = theDO.getAttributeNames();
	            GenericValueMap theMap   = new GenericValueMapImpl(theDO.tTable().getName(), theDO.getIdentifier(), theAttrs.length);
	            theResult.add(theMap);
	            for (int k=0; k<theAttrs.length; k++) {
	                String theAttr = theAttrs[k];
	                theMap.setAttributeValue(theAttr, theDO.getAttributeValue(theAttr));
	            }
	        }
	        return theResult;
	    } catch (Exception ex) {
	        Logger.error("Unable to set up import!", ex, this.getClass());
	    }
	    return Collections.EMPTY_LIST;
	}

	/**
	 * Get the next column name
	 *
	 * @param aColumn the column name (e.g. AZ)
	 * @return the next column (e.g. BA)
	 */
	protected String incExcelColumnName(String aColumn) {
		return this.getExcelColumnName(this.getColumnNumber(aColumn) + 1);
	}

	/**
	 * Get column number from XLS column name (A is column 1)
	 *
	 * @param aColumn the column name
	 * @return the column number
	 */
	protected int getColumnNumber(String aColumn) {
		aColumn = aColumn.toLowerCase();
		int theLen = aColumn.length();
		int theVal = 0;
		for (int i=0; i<theLen; i++) {
			int theDigitVal = aColumn.charAt(i) - 'a';
			theVal = theVal*('z' - 'a' + 1) + theDigitVal;
		}

		return theVal + 1;
	}

	/**
	 * Calc the XLS column name (e.g. BA) from the column number (starting with 1)
	 *
	 * @param aColumnIndex the column index (1 is first column)
	 * @return the column names as used in XLS
	 */
	protected String getExcelColumnName(int aColumnIndex) {
		String theStr = "";
		boolean isNeg = aColumnIndex < 0;
		if (isNeg) {
			aColumnIndex = -aColumnIndex;
		}
		aColumnIndex--;	// Excel starts with 1 == a;
		do {
			int theRest  = aColumnIndex % 26;
			char theChar = (char) ('a' + theRest);
			theStr =  theChar + theStr;
			aColumnIndex = aColumnIndex / 26;
		} while (aColumnIndex != 0);

		if (isNeg) {
			theStr = "-" + theStr;
		}

		return theStr.toUpperCase();
	}

	/**
	 * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#checkFormat(com.top_logic.dob.MetaObject)
	 */
	@Override
	protected void checkFormat(MetaObject aMeta) throws DataObjectException {
		String[] theColNames = this.getColumns();
		for (int i=0; i < theColNames.length; i++) {
            String col   = theColNames[i];
            if (null == MetaObjectUtils.getAttribute(aMeta, col)) {
                throw new NoSuchAttributeException("Expected column '" + col + "not found in " + aMeta);
            }
        }
    }

	/**
	 * Get the column names used as DataObject attribute names.
	 *
	 * Note that these attribute names have to be complete
	 * at least from the first column to the last column to be imported!
	 *
	 * @return the column names used as DataObject attribute names
	 */
	@Override
	public String[] getColumns() {
		if (this.getRelativeColumnNameRowNo() >= 0) {
			if (firstColumnNames == null) {
				List theColNames = new ArrayList();
				Map theMap = this.getSheet(this.sheetName);
				// Read first line
				int theMinCol = this.getColumnNumber(this.minCol);
				int theMaxCol = this.getColumnNumber(this.maxCol);

				String theColName = this.minCol;
				int    theMinRow  = this.minRow + this.getRelativeColumnNameRowNo();
	    		for (int theCol=theMinCol; theCol<=theMaxCol; theCol++) {
	    			Object theValue = theMap.get(theColName + theMinRow);
	    			if (theValue== null) {
	    				theValue = "";
	    			}
	    			theColNames.add(theValue.toString());
	    			theColName = this.incExcelColumnName(theColName);
	    		}

	    		int theSize = theColNames.size();
	    		String[] theColNamesArr = new String[theSize];
				for(int i=0; i<theSize; i++) {
					theColNamesArr[i] = (String) theColNames.get(i);
				}

				firstColumnNames = theColNamesArr;
			}

			return firstColumnNames;
		}

		throw new RuntimeException("Column names not set - cannot create DOs");
	}

    /**
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#setupImport()
     */
    @Override
	protected void setupImport() throws Exception {
    	super.setupImport();
    	this.readFile();
    }

    protected ExcelAccess getExcelAccess() {
        return ExcelAccess.newInstance();
    }

    private void readFile() {
        try {
        	Map theValues;
        	{
        		InputStream theStream = null;
        		if (this.importFile.getName().endsWith(getExtension())) {
					theStream = this.importFile.getStream();
        		} else if (this.importFile.getName().endsWith(".zip")) {
					ZipInputStream zipper = new ZipInputStream(importFile.getStream());
					while (true) {
						ZipEntry entry = zipper.getNextEntry();
						if (entry == null) {
							break;
						}
						String fName = entry.getName().toLowerCase();
        				if (fName.endsWith(getExtension())) {
							theStream = zipper;
        					break;
        				}
        			}
        		}
        		if (theStream == null) {
        			throw new IllegalArgumentException("Importfile " + importFile + " must have extension " + getExtension()+ " or it must be a zip archive with some content with that extension");
        		}
        		try {
        			theValues = this.getExcelAccess().getValues(theStream);
        		} finally {
        			theStream.close();
        		}
        	}
            Set       theSet    = theValues.keySet();
            ArrayList theColl   = new ArrayList(theSet);
            String    theSheet  = null;
            Map       theInner  = null;
            int       thePos    = -1;

            Collections.sort(theColl);

            this.workbook = new HashMap();

            for (Iterator theIt = theColl.iterator(); theIt.hasNext();) {
                String theKey = (String) theIt.next();

                if ((theSheet == null) || !theKey.startsWith(theSheet)) {
                    theInner = new HashMap();
                    thePos   = theKey.indexOf('!');
                    theSheet = theKey.substring(0, thePos);

                    this.workbook.put(theSheet, theInner);
                }

                theInner.put(theKey.substring(thePos + 1), theValues.get(theKey));
            }

            minRow = 65535;
            maxRow = 1;
            minCol = "ZZZ";
            maxCol = "A";

            Map theMap = this.getSheet(this.sheetName);
            if (theMap == null) {
                logWarn("Could not find sheet " + this.sheetName, null);
                return;
            }

            Iterator theKeys = theMap.keySet().iterator();
            while (theKeys.hasNext()) {
                String theKey = (String) theKeys.next();

                int theNumIndex = -1;
                int theKeyLen   = theKey.length();
                for (int i=0; i<theKeyLen && theNumIndex<0; i++) {
                    if (Character.isDigit(theKey.charAt(i))) {
                        theNumIndex = i;
                    }
                }

                if (theNumIndex > 0) {
                    String theString = theKey.substring(0, theNumIndex);
                    String theNumStr = theKey.substring(theNumIndex);
                    int theNum = Integer.valueOf(theNumStr).intValue();

                    if (theString.compareTo(minCol) < 0) {
                        minCol = theString;
                    }

                    if (theString.compareTo(maxCol) > 0) {
                        maxCol = theString;
                    }

                    if (theNum < minRow) {
                        minRow = theNum;
                    }

                    if (theNum > maxRow) {
                        maxRow = theNum;
                    }
                }
            }
        }
        catch (Exception ex) {
            logError("Import failed: " + ex, ex);
        }
    }

	/**
	 * Return the extenion of the file you expect.
	 *
	 * @return ".xls" here, override as needed.
	 */
	protected String getExtension() {
	    return ".xls";
	}

	/**
	 * Return the requested excel sheet.
	 *
	 * @param    aName    The name of the requested sheet, must not be <code>null</code>.
	 * @return   The valued from the sheet as map, may be <code>null</code>.
	 */
	protected Map getSheet(String aName) {
	    if (this.workbook == null) {
	    	try {
	    		this.setupImport(); // init the workbook
	    	} catch (Exception e) {
	    		throw new RuntimeException(e);
	    	}
	    }
	    Map sheet = ((Map) this.workbook.get(aName));
	    if (sheet == null) {
	        logError("There is no sheet with name '" + aName + "'", null);
	        throw new IllegalArgumentException("There is no sheet with name '" + aName + "'");
	    }
	    return sheet;
	}

	@Override
	protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
	}

	@Override
	protected void startImport() {
	    this.startImport(null, false);
	}
}
