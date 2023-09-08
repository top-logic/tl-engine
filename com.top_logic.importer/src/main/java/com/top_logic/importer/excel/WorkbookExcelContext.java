/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.base.office.excel.ProxyExcelContext;

/**
 * Representation of a whole {@link Workbook} where you can act on one {@link Sheet} at a time.
 * 
 * <p>The initial sheet will be the first one in the workbook.</p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WorkbookExcelContext extends ProxyExcelContext {

    private final Map<String, ExcelContext> sheetMap;

    private List<String> hiddenSheets;

    private String sheetName;

    private ExcelContext currentSheet;

    /** 
     * Creates a {@link WorkbookExcelContext}.
     * 
     * @param    aWorkbook     The {@link Workbook} we acting on.
     */
    public WorkbookExcelContext(Workbook aWorkbook) {
        super(aWorkbook.getSheetAt(0));

        this.hiddenSheets = new ArrayList<>();
        this.sheetMap     = this.initContext(aWorkbook, false);
    }

    /** 
     * Creates a {@link WorkbookExcelContext}.
     * 
     * @param    aWorkbook     The {@link Workbook} we acting on.
     * @param    isMixed       Flag that we are supporting mixed mode.
     */
    public WorkbookExcelContext(Workbook aWorkbook, boolean isMixed) {
        super(new MixedModeExcelContext(aWorkbook.getSheetAt(0)));

        this.hiddenSheets = new ArrayList<>();
        this.sheetMap     = this.initContext(aWorkbook, isMixed);
    }

    private Map<String, ExcelContext> initContext(Workbook theBook, boolean isMixed) {
		Map<String, ExcelContext> theResult = new LinkedHashMap<>();
        int                       theSize   = theBook.getNumberOfSheets();

        for (int thePos = 0; thePos < theSize; thePos++) {
            Sheet        theSheet   = theBook.getSheetAt(thePos);
            String       theName    = theSheet.getSheetName();
            ExcelContext theContext;

            if (thePos == 0) {
                // Need super implementation for the first sheet.
                theContext = super.getInner();

                this.sheetName    = theName;
                this.currentSheet = theContext;
            }
            else {
                theContext = isMixed ? new MixedModeExcelContext(theSheet) : ExcelContext.getInstance(theSheet);
            }

            if (theBook.isSheetHidden(thePos)) {
                this.hiddenSheets.add(theName);
            }

            theResult.put(theName, theContext);
        }

        return theResult;
    }

    @Override
    protected ExcelContext getInner() {
        return this.currentSheet;
    }

    @Override
    public void prepareHeaderRows(int aMaxColumn) {
        for (ExcelContext theContext : this.sheetMap.values()) {
            theContext.prepareHeaderRows(aMaxColumn);
        }
    }

    @Override
    public void prepareColNamesAsHeaders() {
        for (ExcelContext theContext : this.sheetMap.values()) {
            theContext.prepareColNamesAsHeaders();
        }
    }

    /** 
     * Return all sheet names handled by this context.
     * 
     * @return    The requested sheet names.
     */
    public Collection<String> getSheetNames() {
        return this.sheetMap.keySet();
    }
    
    /** 
     * Return sheet name currently handled by this context.
     * 
     * @return    The requested sheet name.
     */
    public String getCurrentSheetName() {
        return this.sheetName;
    }

    /** 
     * Check, if a sheet has been hidden.
     * 
     * @param    aSheet   Name of the requested sheet.
     * @return   <code>true</code> when the sheet is hidden in excel.
     */
    public boolean isHidden(String aSheet) {
        return this.hiddenSheets.contains(aSheet);
    }

    /** 
     * Change the {@link ExcelContext} we work on.
     * 
     * @param    aName   The name of the requested sheet (as provided by {@link #getSheetNames()}.
     */
    public void setCurrentSheet(String aName) {
        this.sheetName    = aName;
        this.currentSheet = this.sheetMap.get(aName);
    }
}
