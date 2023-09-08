/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.CSVReader;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * Provide an excel context for XLS, XLSX or CSV files.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class ExcelContextProvider<T> {

    private final AssistentComponent component;

    private Object result;

    private boolean isCreated;

    private ExcelContext context;

    /** 
     * Creates a {@link ExcelContextProvider}.
     * 
     * @param    aComponent    Component this provider lives in, may be <code>null</code>.
     */
    public ExcelContextProvider(AssistentComponent aComponent) {
        this.component = aComponent;
    }

    protected abstract T initImporter(final InputStream aStream) throws IOException, InvalidFormatException;

    protected abstract ExcelContext createContext(boolean isMixed, Object aResult);

    /** 
     * Initialize this provider with an excel file (XLSX, XLS or CSV).
     * 
     * @param    aFile   The file to provide the excel context.
     * @throws   OfficeException          When parsing the file content fails.
     * @throws   FileNotFoundException    When file cannot be found.
     */
    public void init(File aFile) throws OfficeException, FileNotFoundException {
        String theName = aFile.getAbsolutePath();

        if (theName.toLowerCase().endsWith(".csv")) {
            this.result = this.createCSVReader(new FileInputStream(aFile));
        }
        else {
            this.init(new FileInputStream(aFile));
        }
    }

	/**
	 * Initialize this provider with an {@link BinaryData} representing an excel file (XLSX, XLS or
	 * CSV).
	 * 
	 * @param data
	 *        The data to provide the excel context.
	 * @throws OfficeException
	 *         When parsing the file content fails.
	 * @throws IOException
	 *         When accessing data fail.
	 */
	public void init(BinaryContent data) throws OfficeException, IOException {
		String theName = data.getName();

		if (theName.toLowerCase().endsWith(".csv")) {
			this.result = createCSVReader(data.getStream());
		} else {
			this.init(data.getStream());
		}
	}

    /** 
     * Initialize this provider with an stream representing an excel file (XLSX, XLS).
     * 
     * @param    aStream   The stream to provide the excel context.
     * @throws   OfficeException          When parsing the stream content fails.
     * @throws   FileNotFoundException    When stream is invalid.
     */
    public void init(InputStream aStream) throws OfficeException, FileNotFoundException {
        this.result = this.importStream(aStream);
    }

    /** 
     * Return the assistant component this provider lives in (taken from the constructors call).
     * 
     * @return    The requested component.
     */
    public AssistentComponent getComponent() {
        this.checkInit();

        return this.component;
    }

    /** 
     * Return the excel context created by this provider.
     * 
     * @return    The requested context.
     */
    public ExcelContext getContext() {
        this.checkInit();

        return this.context;
    }

    /** 
     * Create the excel context for the file given in the {@link #init(File)} method.
     * 
     * <p><b>This method can only be called once!</b></p>
     * 
     * @param    isMixed    <code>true</code> when header can have names and also be identified by column "A", "B", ...
     * @return   The requested excel context.
     * @throws   IllegalStateException    When {@link #init(File)} not called before or this method is called twice.
     */
    public ExcelContext createContext(boolean isMixed) {
        this.checkInit();

        if (this.isCreated) {
            throw new IllegalStateException("createContext() called twice!");
        }
        else {
            try {
                this.context = this.createContext(isMixed, this.result);

                return this.context;
            }
            finally {
                this.isCreated = true;
            }
        }
    }

    /** 
     * Return the maximum number of the row to look up the header information.
     * 
     * @return    The requested row number.
     */
    public int getMaxHeaderRowSize() {
        this.checkInit();

        return (this.result instanceof Sheet) ? ((Sheet) this.result).getLastRowNum() : 20;
    }

    private T importStream(InputStream aStream) throws OfficeException, FileNotFoundException {
        try {
            return this.initImporter(aStream);
        }
        catch (InvalidFormatException ex) {
            throw new OfficeException(this.getMessage("Failed to parse stream", ex), ex);
        }
        catch (IOException ex) {
            String theMess = this.getMessage("Failed to read stream", ex);

            Logger.error(theMess, ex, ExcelContextProvider.class);
            throw new FileNotFoundException(theMess);
        }
    }

    private CSVReader createCSVReader(InputStream in) {
		Reader theReader = new InputStreamReader(in, Charset.availableCharsets().get("ISO-8859-1"));
		return new CSVReader(theReader, ';');
	}

    private String getMessage(String aPrefix, Exception ex) {
        String theMess = ex.getMessage();

        if (!StringServices.isEmpty(theMess)) {
            return aPrefix + " (reason: '" + theMess + "')!";
        }
        else {
            return aPrefix + '!';
        }
    }

    private void checkInit() {
        if (this.result == null) {
            throw new IllegalStateException("This object needs to be initialized first!");
        }
    }

    public static class WorkbookExcelContextProvider extends ExcelContextProvider<Workbook> {

        public WorkbookExcelContextProvider(AssistentComponent aComponent) {
            super(aComponent);
        }

        // Overridden methods from ExcelContextProvider

        @Override
        protected Workbook initImporter(final InputStream aStream) throws IOException, InvalidFormatException {
            return WorkbookFactory.create(aStream);
        }

        @Override
        protected ExcelContext createContext(boolean isMixed, Object aResult) {
            if (aResult instanceof Workbook) { 
                return isMixed ? new WorkbookExcelContext((Workbook) aResult, true) : new WorkbookExcelContext((Workbook) aResult);
            }
            else {
                CSVReader theReader = (CSVReader) aResult;

                return ExcelContext.getInstance(theReader);
            }
        }
    }
    
    public static class SheetExcelContextProvider extends ExcelContextProvider<Sheet> {
        
        private String sheetName;

        public SheetExcelContextProvider(AssistentComponent aComponent, String aSheet) {
            super(aComponent);

            this.sheetName = aSheet;
        }

        // Overridden methods from ExcelContextProvider

        @Override
        protected Sheet initImporter(final InputStream aStream) throws IOException, InvalidFormatException {
            Workbook theBook  = WorkbookFactory.create(aStream);
            Sheet    theSheet = (this.sheetName != null) ? theBook.getSheet(this.sheetName) : null;
            
            if (theSheet == null) {
                if (theBook.getNumberOfSheets() == 1) {
                    theSheet = theBook.getSheetAt(0);
                }
                else if (this.sheetName == null) {
                    throw new InvalidFormatException("Excel file must contain only one sheet");
                }
                else {
                    throw new InvalidFormatException("Cannot find sheet named '" + this.sheetName + "' in excel file");
                }
            }
            
            return theSheet;
        }

        @Override
        protected ExcelContext createContext(boolean isMixed, Object aResult) {
            if (isMixed) { 
                return new MixedModeExcelContext(aResult);
            }
            else {
                return ExcelContext.getInstance(aResult);
            }
        }
    }
}

