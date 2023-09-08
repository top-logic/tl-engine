/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.top_logic.base.office.POIUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.sched.BatchImpl;
import com.top_logic.basic.thread.InContext;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.util.TLContext;

/**
 * Abstract Superclass for importing Excel-files row-wise plain or from a zip-file
 * 
 * This was copied from CSVImporter and may need a Review to combine these
 * two to avoid duplicate code.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public abstract class POIExcelImporter extends BatchImpl implements ProgressInfo, InContext {
    
    /**
     * Used to convert double to Text with "no frills"
     */
    protected final NumberFormat numberFormat 
        = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));

    /**
     * Initial (maximum) size of stringWriter.
     */
    protected static final int INITAL_LOGSIZE = 10240;

    /** Useful constant to use for CTor */
    public static final boolean DELETE_WHEN_DONE = true;
    
    /** The (csv/zip) file we import from */
	protected BinaryContent importFile;
    
    /** Total size of data being imported */
    protected int totalSize;

    /** 
     * Current Size of data already imported [0..totalSize] 
     */
    protected int currentSize;

    /** Set to true when Import was completed */
    protected boolean finished;
    
    /** Message to show to User via progressInfo */
    protected String message;
    
    /** PrintWriter to log messages to, will be ignored when null. */
    protected PrintWriter logWriter;

    /** PrintWriter is by default based on this StringWriter*/
    protected StringWriter stringWriter;
    
    /** Data Format for conversions, valid during Import only */
	protected DataFormat dataFormat;

	protected boolean hasErrors;
	
	/** Flag for deciding whether to use excel 2007 format (xFormat) or not */
	private final boolean _useXFormat;
    
    /**
	 * Create a new {@link POIExcelImporter} for given {@link File}.
	 */
	public POIExcelImporter(BinaryContent importSource) {
		this(importSource, false);
    }
    
    /**
     * Creates a new {@link POIExcelImporter}.
     * @param useXFormat
	 *        Flag indicating whether to use "xlsx" (true) or "xls" (false) format.
     */
	public POIExcelImporter(BinaryContent importSource, boolean useXFormat) {
    	super("Import" + importSource.getName());
		this._useXFormat = useXFormat;
    	this.importFile     = importSource;
    	this.message        = "";
    	this.totalSize      = 1; // So Progress does not show NAN ...
    }
    
    /**
	 * Return the extension of the file you expect.
	 * 
	 * @return ".xlsx" here if "xFormat" is used and ".xls" otherwise, override as needed.
	 */
    protected String getExtension() {
		if (useXFormat()) {
			return POIUtil.XLSX_SUFFIX;
    	}else{
			return POIUtil.XLS_SUFFIX;
    	}
    }

    /** 
     * Open the file, check for .zip-file then forward to {@link #doPOIImport(InputStream)}
     * 
     * In case of a zip-file the first file matching the {@link #getExtension()} will be used.
     */
    @Override
	public void run() {
		TLContext.inSystemContext(getClass(), this);
	}

	@Override
	public void inContext() {
        InputStream iStream = null;
		ZipInputStream zipper = null;
        finished    = false;
        try {
            InputStream theStream = null;
            String fName  = importFile.getName().toLowerCase();
            String extens = getExtension();
            if (fName.endsWith(extens)) {
				iStream = theStream = importFile.getStream();
            } else if (fName.endsWith(".zip")) {
				zipper = new ZipInputStream(importFile.getStream());
				while (true) {
					ZipEntry zentry = zipper.getNextEntry();
					if (zentry == null) {
						break;
					}
                    fName = zentry.getName().toLowerCase();
                    if (fName.endsWith(extens)) {
						theStream = zipper;
                        break;
                    }
                }
            } 
            if (theStream != null) {
				doPOIImport(theStream);
            } else {
                logError(message, null);
            }
        } catch (Exception ex) {
            message = ex.toString();
            logError("Failed to run()", ex);
        } finally {
            iStream = StreamUtilities.close(iStream);
            if (zipper != null) try {
                zipper.close();
            } catch (IOException iox) {
                logWarn("Failed to close()" , iox);
            }        
            finished = true;
        }
    }

    /** 
     * Check that the first row (as found in CSVTokenizer) is as expected.
     * 
     * @throws IOException when format is not OK.
     * 
     * @return false to indicate that no check is needed,
     *         (assuming first line contains data)
     */
	abstract protected boolean checkColumnFormat(int aSheetNum, String aSheetname, Row aRow) throws IOException;

    
    /** 
     * Do the actual Import using a CSVParser.
     */
    protected void doPOIImport(InputStream aStream) throws IOException {
        
        /* If we have a input source, open the workbook from it... */
		Workbook theWorkbook = this.getWorkbook(aStream);
		dataFormat = theWorkbook.createDataFormat();
        
        int maxSheets = theWorkbook.getNumberOfSheets();

        setupImport();
        try {
            // First, check which sheets should be imported
            List<Integer> sheetsToImport = new ArrayList<>(maxSheets);
            for (int iSheet=0; iSheet<maxSheets; iSheet++) {
                String    sheetName = theWorkbook.getSheetName(iSheet);
                
                if (this.shoudImportSheet(iSheet, sheetName)) {
                    sheetsToImport.add(iSheet);
                }
            }
            
            // Second, check the format of all sheets marked for import and 
            // remember the start position of the data.
            // Calculate the total number of rows for the progress, too.
            List<Integer> firstDataRowOfSheet = new ArrayList<>(sheetsToImport.size());
            for (Integer iSheet : sheetsToImport) {
				Sheet sheet = theWorkbook.getSheetAt(iSheet);
				String sheetName = theWorkbook.getSheetName(iSheet);
                
                int firstRow = sheet.getFirstRowNum();
                int lastRow  = sheet.getLastRowNum();
                
				Row theRow = sheet.getRow(firstRow);
                try {
                    // If the column is a "header" column, the first row of data is the next row.
                    if (checkColumnFormat(iSheet, sheetName, theRow)) {
                        firstDataRowOfSheet.add(firstRow + 1);
                        totalSize += lastRow - firstRow;
                    }
                    // If not, then the first row of data is the first row on the sheet.
                    else {
                        firstDataRowOfSheet.add(firstRow);
                        totalSize += lastRow - firstRow + 1;
                    }
                // Log the error, but check all sheets to get an overall report. 
                } catch (IOException e) {
                    logError("The column format of sheet " + iSheet + "(" + sheetName +") does not fulfill the specification.", e);
                    this.setError();
                }
            }
            
            // Check of the column format was not successful, do not import anything
            if (this.hasError()) {
                return;
            }
            
            // Third, import all rows of all marked sheets
			for (int i = 0, cnt = sheetsToImport.size(); i < cnt && !getShouldStop(); i++) {
                int iSheet   = sheetsToImport.get(i);
				Sheet sheet = theWorkbook.getSheetAt(iSheet);
				String sheetName = theWorkbook.getSheetName(iSheet);
    
                this.startSheetImport(iSheet, sheetName);
                
                int firstRow = firstDataRowOfSheet.get(i);
                int lastRow  = sheet.getLastRowNum();
                
				for (int iRow = firstRow; iRow <= lastRow && !getShouldStop(); iRow++) {
					Row theRow = sheet.getRow(iRow);
                    importRow(iSheet, sheetName, theRow);
                    currentSize++;
                }
                // We have to commit the last pending updates. Before this will
                // happen a subclass may want to add some additional information
                this.endSheetImport(iSheet, sheetName);
                
            }
        } catch (Exception e) {
            logError("Failed to doPOIImport", e);
            this.setError();
        } finally {
            this.tearDownImport();
            dataFormat = null;
        }
    }

	/**
	 * Return the workbook out of the given stream.
	 * 
	 * @param is
	 *        The stream to get the workbook from, must not be <code>null</code>.
	 * 
	 * @return The requested workbook, never <code>null</code>.
	 * 
	 * @throws IOException
	 *         When creating the workbook fails.
	 */
	protected Workbook getWorkbook(InputStream is) throws IOException {
		if (useXFormat()) {
			return new XSSFWorkbook(is);
		}else{
			return new HSSFWorkbook(is, /* reserveNodes */false);
		}
	}

    /**
     * Check if the current sheet defined by numSheet and sheetName should be imported or ignored.
     * By default, only the first sheet should be imported. 
     */
    protected boolean shoudImportSheet(int numSheet, String sheetName) {
       return numSheet == 0; 
    }
    
    /**
     * Hook for subclasses to do something directly before a sheet is imported
     */
    protected void startSheetImport(int numSheet, String sheetName) {
    }
    
    /**
     * Hook for subclasses to do something directly after a sheet is imported
     */
    protected void endSheetImport(int numSheet, String sheetName) {
    }
    
    /** 
     * Hook for subclasses do setup something before importing.
     * 
     * This will call setupLogWriter() so be sure to call super.
     */
    protected void setupImport() {
        totalSize   = 1; // avoid division with 0 in getProgress()
        currentSize = 0; 
        logWriter = setupLogWriter();
    }
    
    protected void setError() {
    	this.hasErrors = true;
    }
    
    protected boolean hasError() {
    	return this.hasErrors;
    }

    /** 
     * By default will create a PrintWriter bases on a StringWriter.
     * 
     * @return null to signal that you do not want to have logging.
     */
    protected PrintWriter setupLogWriter() {
        int size = INITAL_LOGSIZE;
        stringWriter = new StringWriter(size);
        return new PrintWriter(stringWriter);
    }

    /** 
     * This method is called for every Row parsed.
     * 
     * @param aRow       The complete current row
     */
	abstract protected void importRow(int aSheetNum, String aSheetname, Row aRow) throws Exception;

    /**
     * Log an Error to normal logger and (if configured) the logWriter.
     */
    protected void logError(String aMessage, Throwable anException) {
        Logger.error(aMessage, anException, this);
        if (logWriter != null) {
            logWriter.print("ERROR: ");
            logWriter.println(aMessage);
            if (anException != null) {
                anException.printStackTrace(logWriter);
            }
        }
    }
    
    /**
     * Log a Warning to normal logger and (if configured) the logWriter.
     */
    protected void logWarn(String aMessage, Throwable anException) {
        Logger.warn(aMessage, anException, this);
        if (logWriter != null) {
            logWriter.print("WARNING: ");
            logWriter.println(aMessage);
            if (anException != null) {
                anException.printStackTrace(logWriter);
            }
        }
    }

    /**
     * Log some information to normal logger and (if configured) the logWriter.
     */
    protected void logInfo(String aMessage, Throwable anException) {
        Logger.info(aMessage, anException, this);
        if (logWriter != null) {
            logWriter.print("INFO: ");
            logWriter.println(aMessage);
            if (anException != null) {
                anException.printStackTrace(logWriter);
            }
        }
    }

    
    /** 
     * Hook for subclasses do tear down something after importing.
     * 
     * This will set message to the content (if not null) of the StringWriter.
     */
    protected void tearDownImport() {
        if (stringWriter != null) {
            message = stringWriter.toString();
            if (message.length() == 0) {
                message = null;
            }
            stringWriter = null; 
            logWriter    = null;
        }
        
        this.hasErrors = false;
    }

    /**
     * true when Import was completed 
     */
    @Override
	public boolean isFinished() {
        return finished;
    }

    /** 
     * Return the number of bytes currently imported.
     */
    @Override
	public long getCurrent() {
        return currentSize;
    }

    /** 
     * Return the completeness in %.
     */
    @Override
	public float getProgress() {
        return 100.0f * currentSize / totalSize;
    }

    /** 
     * Return the total size of the file to import
     */
    @Override
	public long getExpected() {
        return totalSize;
    }


    /** 
     * An (optional) message to show to user 
     *
     * @return null when there is no message.
     */ 
    @Override
	public String getMessage() {
        return message;
    }

	/**
	 * Returns the value of the variable {@link #_useXFormat}.
	 */
	public boolean useXFormat() {
		return _useXFormat;
	}

    /** 
     * Try using 1 second refresh (Import seems fast enough ..)
     */
    @Override
	public int getRefreshSeconds() {
        return MIN_REFRESH;
    }

    /** 
     * Fix empty Strings in a cell by replacing them with null.
     * 
     * Optimized to ignore blank cells
     */
	protected static String fixEmpty(Cell aCell) {
		if (aCell == null || aCell.getCellType() == CellType.BLANK)
            return null;
		if (aCell.getCellType() == CellType.FORMULA) {
        	return aCell.getCellFormula();
        }
		RichTextString rtfString = aCell.getRichStringCellValue();
        if (rtfString == null)
            return null;
        return fixEmpty (rtfString.getString());
    }
    
    /** 
     * Extract any value from the cell as String.
     * 
     * Optimize to ignore blank cells
     */
	protected String fixAny(Cell aCell) {
        if (aCell == null)
            return null;
		CellType type = aCell.getCellType();
        switch (type) {
			case NUMERIC:
                // String format = dataFormat.getFormat(aCell.getCellStyle().getDataFormat());
                return numberFormat.format(aCell.getNumericCellValue());
			case STRING:
			case FORMULA:
            default:    // dunno any better 
                return fixEmpty(aCell);
			case BLANK:
                return null;
			case BOOLEAN:
                return aCell.getBooleanCellValue() ? "T" : "F";
			case ERROR:
                return "ERROR " + aCell.getErrorCellValue();
        }
        
    }
    
    /** 
     * Fix empty Strings by replacing them with null.
     */
    protected static String fixEmpty(String aString) {
        if (aString != null) {
            aString = aString.trim();
            if (aString.length() == 0) {
                aString = null;
            }
        }
        return aString;
    }
    
}
