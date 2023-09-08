/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.io.SimpleCSVTokenizer;
import com.top_logic.contact.business.CSVImporter;

/**
 * Import a Customer specific CSV-File intom some temporary Structure.
 * 
 * Not is use since we can access the SAP System directly.
 * 
 * This is a Customer specific component and needs modifications for generic 
 * (if possible) usage. The customer wishes to compare these with existing 
 *  CompanyContacts and / or ContractNumbers...) 
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAP_CSVSupplierImporter extends CSVImporter {
    
    /** 
     * Desscribing colums we expect as first line.
     * 
     * (German names as got from customer)
     * 
     */ 
    protected String COLUMNS_SUPPL[] = { "Kreditor","Lnd","Name 1","Name 2",
                                         "Ort","Ortsteil","Postfach","PLZ-Postf.","Postleitz.","Rg",
                                         "SuchBegr","Straﬂe","Adresse","Name 1","Name 2","Ort","Branche",
                                         "Telebox","Telefon-1","Telefon-2","Telefaxnummer","Teletexnummer","Telexnummer" };

    /** 
     * Map of SAPRecords indexed bei theire SAP-Number (Integer)
     */
    protected Map sapRecords;

    /** 
     * Create a new SAPSupplierImporter for given file
     * 
     * @param deleteWhenDone when true file will be deleted after import.
     */
    public SAP_CSVSupplierImporter(File aImportSource, boolean deleteWhenDone) {
        super(aImportSource, deleteWhenDone);
    }

    /** 
     * Chek that the first columns (as found in CSVTokenizer) are as expected.
     * 
     * @throws IOException when format is not OK.
     */
    @Override
	protected boolean checkColumnFormat(SimpleCSVTokenizer cvsToken) throws IOException {
        for (int i=0; i < COLUMNS_SUPPL.length; i++) {
            String col   = COLUMNS_SUPPL[i];
            String found = cvsToken.nextToken();
            if (!col.equals(found)) {
                throw new IOException("Expected column '" + col + "' found '" + found + "'");
            }
        }
        return true;
    }
    
    /** 
     * Overriden to create a SimpleCSVTokenizer for ';'.
     */
    @Override
	protected SimpleCSVTokenizer createTokenizer(String firstLine) {
        return new SimpleCSVTokenizer(firstLine, ';');
        // For now format is not quoted ...
        // return new CSVTokenizer(firstLine, ';', '"');
    }
    
    /** 
     * Create the sapRecord Map based on size of importFile.
     */
    @Override
	protected void setupImport() {
        super.setupImport();
        boolean isZip = importFile.getName().endsWith(".zip");
        // int     factor = isZip ? 32 : 256;
        int     shift  = isZip ? 7  : 9;
        int     size = (int) (importFile.length() >> shift); // / factor;
        sapRecords = new HashMap(size);
    }

    /** 
     * Create a new SAPSupplierRecord by parsing the given CsvToken
     */
    @Override
	protected void importLine(String aLine, SimpleCSVTokenizer aCsvToken)
            throws Exception {
        
        SAPSupplierRecord sapRec = new SAPSupplierRecord();
        sapRec.sapNr            = Integer.valueOf(aCsvToken.nextToken());   // Kreditor
        sapRec.Lnd              = aCsvToken.nextToken();
        sapRec.Name1            = aCsvToken.nextToken();
        sapRec.Name2            = aCsvToken.nextToken();
        sapRec.Ort              = aCsvToken.nextToken();
        sapRec.Ortsteil         = aCsvToken.nextToken();
        sapRec.Postfach         = aCsvToken.nextToken();
        sapRec.PLZPostf         = aCsvToken.nextToken();
        sapRec.Postleitz        = aCsvToken.nextToken();
        sapRec.Rg               = aCsvToken.nextToken();
        sapRec.SuchBegr         = aCsvToken.nextToken();
        sapRec.Strasse          = aCsvToken.nextToken();
        sapRec.Adresse          = aCsvToken.nextToken();
        sapRec.PlzName1         = aCsvToken.nextToken();
        sapRec.PlzName2         = aCsvToken.nextToken();
        sapRec.PlzOrt           = aCsvToken.nextToken();
        sapRec.Branche          = aCsvToken.nextToken();
        sapRec.Telebox          = aCsvToken.nextToken();
        sapRec.Telefon1         = aCsvToken.nextToken();
        sapRec.Telefon2         = aCsvToken.nextToken();
        sapRec.Telefaxnummer    = aCsvToken.nextToken();
        sapRec.Teletexnummer    = aCsvToken.nextToken();
        sapRec.Telexnummer      = aCsvToken.nextToken();
        
        sapRecords.put(sapRec.sapNr, sapRec);
    }
    
    /** 
     * Overriden to Log the number of imported Records.
     */
    @Override
	protected void tearDownImport() {
        logInfo("Imported " + sapRecords.size() + " SAPRecords", null);
        super.tearDownImport();
    }
    
    /** Allow extraction of imported Records as List */
    public List getSapRecords() {
        return new ArrayList(sapRecords.values());
    }

}
