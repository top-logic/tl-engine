/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.office.OfficeException;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractExcelImporter {

    /** The workbook representing the excel file. */
    protected HashMap workbook;

    /** 
     * Performs the main import work.
     * 
     * @param    aWriter      The writer to be used for user feedback, must not be <code>null</code>.
     * @param    aKB          The knowledge base to be used for import, must not be <code>null</code>.
     * @throws   Exception    If import fails.
     */
    protected abstract boolean doImport(PrintWriter aWriter, KnowledgeBase aKB) throws Exception;

    /** 
     * Import the file defined in method {@link #getImportFileName()}.
     * 
     * @param    anOut        The writer to be used for user feedback, must not be <code>null</code>.
     * @throws   Exception    If import fails or {@link #getImportFileName()} returns <code>null</code>.
     * @see      #doImport(PrintWriter, KnowledgeBase)
     */
    public void startImport(Writer anOut) throws Exception {
        this.startImport(this.getImportFileName(), anOut);
    }

    /** 
     * Import the file defined by the given file name.
     * 
     * @param    aFileName    The name of the file to be used for import, must not be <code>null</code>.
     * @param    anOut        The writer to be used for user feedback, must not be <code>null</code>.
     * @throws   Exception    If import fails or file cannot be found.
     * @see      #doImport(PrintWriter, KnowledgeBase)
     */
    public void startImport(String aFileName, Writer anOut) throws Exception {
		BinaryData theFile = FileManager.getInstance().getData(aFileName);

        this.startImport(theFile, anOut);
    }

    /** 
     * Impotr the given file.
     * 
     * @param    aFile    The file to be imported, must not be <code>null</code>.
     * @param    anOut    The writer to be used for user feedback, must not be <code>null</code>.
     * @throws   Exception    If import fails.
     * @see      #doImport(PrintWriter, KnowledgeBase)
     */
	public void startImport(BinaryData aFile, Writer anOut) throws Exception {
        this.initImporter(aFile);

        PrintWriter   thePW = new PrintWriter(anOut);
        KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();

        try {
            if (this.doImport(thePW, theKB)) {
                thePW.println("Finished import!");
            }
            else {
                thePW.println("Import failed!");
            }

            thePW.flush();
        }
        catch (Exception ex) {
            Logger.error("Unable to import file " + aFile, ex, this);

            theKB.rollback();

            thePW.println("Rollback import!");
            thePW.flush();

            throw ex;
        }
    }

    protected String getImportFileName() {
        return null;
    }

    /** 
     * Return the requested command handler.
     * 
     * @return    The requested command handler, may be <code>null</code>.
     */
    protected CommandHandler getHandler(String anID) {
        return CommandHandlerFactory.getInstance().getHandler(anID);
    }

    /** 
     * Return the requested excel sheet.
     * 
     * @param    aName    The name of the requested sheet, must not be <code>null</code>.
     * @return   The valued from the sheet as map, may be <code>null</code>.
     */
    protected Map getSheet(String aName) {
        return ((Map) this.workbook.get(aName));
    }

	protected void initImporter(BinaryData aFile) throws OfficeException {
		Map theValues = ExcelAccess.newInstance().getValues(aFile);
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
    }

}
