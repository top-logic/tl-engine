/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.POIExcelImporter;
import com.top_logic.contact.layout.company.CompanyContactDeleteCommandHandler.CompanyContactDeleteRule;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.contact.mandatoraware.layout.COSCompanyContactCreateHandler;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Utils;

/**
 * Import suppliers from an Excel file as specified at 2006-01-26.
 * 
 * The "specification" for this Importer is found by the file 
 * docs/lief/SAP Lief aktiv Stand 26.01.2006.xls as used by the testcase.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class ExcelSupplier20060529Importer extends POIExcelImporter {

    /** 
     * Names of Attributes we expect when importing.
     * 
     * (Stupid idea to use german headers ...)
     */ 
    protected static String COLUMNS_20060529[] = { 
        "MNDT", "LIFNR", "ANRED", "LAND1", "NAME1", "NAME2", "ORT01", "PFACH", "PSTLZ", 
        "REGIO" , "STRAS", "BRSCH" , "TELF1" , "TELFX" , "CAGE" };
                                                      // ZZSPC_SPL is allowed, too
    /*
    "Kreditor","Lnd","Name 1","Name 2","Ort","Ortsteil","Postfach","PLZ-Postf.",
       "Postleitz.","Rg","SuchBegr","Straﬂe","Adresse","Name 1","Name 2","Ort",
       "Branche","Telebox","Telefon-1","Telefon-2","Telefaxnummer","Teletexnummer","Telexnummer" };
    */

    /** We fill up SAP-Numbers with zeros ... */
    protected static String SAP_ZEROS = "000000000"; 

    /** We fill up SAP-Numbers with zeros ... */
    protected static int     SAP_LEN = SAP_ZEROS.length(); 

    /** When true Company Contacts will be created instead of ignoring them */
    protected boolean create;

    /** The KnowledgeBase to import into */
    protected transient KnowledgeBase kBase;
    
    protected Transaction currentTransaction;
    
    /** Used for creation of Contacts */
    protected transient ContactFactory cFactory;

    /** Used to commit only every n-th record */ 
    protected int commit;
    
    /** Indicates if original Data is in SAP-System */ 
    protected boolean isSAP;
    
    /** Indicates if original Data is in SAP-System */ 
    protected boolean isDeltaImport;

    /** Indicates if original Data is in SAP-System */ 
    protected boolean isSupplierImport;
    
    protected Collection foundContacts;
    
    protected String attName;

    /** 
     * when != null only Contacts for this mandator will be updated.
     * 
     * Otherwise the first matching contact will be updated or created.  
     */
    protected Mandator mandator;
    
    /** 
     * Create a new ExcelSupplier20060126Importer for given File.
     * @param aCreate   When true Company Contacts will be created instead of ignoring them.
     * @param aMandator when != null only Contacts for this mandator will be updated, 
     *                  otherwise the first matching contact will be updated or created.  
     */
	public ExcelSupplier20060529Importer(BinaryContent importSource, boolean aCreate, Mandator aMandator) {
		this(importSource, aCreate, true, true, aMandator);
    }
    
    /** 
     * Create a new ExcelSupplier20060126Importer for given File.
     * @param aCreate   When true Company Contacts will be created instead of ignoring them.
     * @param aDeltaImport		if true only add suppliers. Delete used ones otherwise.
     * @param aSupplierImport	if true interpret data as suppliers and as clients otherwise.
     * @param aMandator when != null only Contacts for this mandator will be updated, 
     *                  otherwise the first matching contact will be updated or created.  
     */
	public ExcelSupplier20060529Importer(BinaryContent importSource, boolean aCreate, boolean aDeltaImport,
    		boolean aSupplierImport, Mandator aMandator) {
		super(importSource);
        
        this.mandator = aMandator;
        
        this.create   = aCreate;
        this.isDeltaImport    = aDeltaImport;
        this.isSupplierImport = aSupplierImport;
        this.attName = (this.isSupplierImport) ? COSCompanyContact.SUPPLIER : COSCompanyContact.CLIENT;
        
        if (aMandator != null) {
            isSAP = !StringServices.isEmpty((String) aMandator.getValue(Mandator.SAP_SUPPLIERS));
        }
    }

    /** 
     * Setup the kBase for importing.
     * 
     * This will call setupLogWriter() so be sure to call super.
     */
    @Override
	protected void setupImport() {
        super.setupImport();
        kBase     = COSCompanyContact.getDefaultKnowledgeBase();
        cFactory  = ContactFactory.getInstance();
        commit    = 0;
        foundContacts = new ArrayList();
    }

    /** 
     * Check that the first column (as found in aRow) is as expected.
     * 
     * @throws IOException when format is not OK.
     */
    @Override
	protected boolean checkColumnFormat(int aSheetNum, String aSheetname, Row aRow) throws IOException {
    	if (aSheetNum != 0) {
    	    return false;
    	}
    	
        short len = (short) (COLUMNS_20060529.length - 1);
        
        if ((aRow.getLastCellNum() - 1) < len) {
        	throw new IOException("Column number incorrect. Skipping further checks");
        }
        
        String   col, found;
		Cell cell;
        for (short i=0; i < COLUMNS_20060529.length; i++) {
            col   = COLUMNS_20060529[i];
            cell  = aRow.getCell(i);
            found = cell.getStringCellValue();
            if (!col.equals(found)) {
                // System.err.println("Expected column '" + col + "' found '" + found + "'");
                throw new IOException("Expected column '" + col + "' found '" + found + "'");
            }
        }
        // take extra care for last column
        col   = COLUMNS_20060529[len]; 
        cell  = aRow.getCell(len);
        found = cell.getStringCellValue();
        if (!col.equals(found) && "ZZSPC_SPL".equals(found)) {
            throw new IOException("Expected column '" + col + "' or 'ZZSPC_SPL' found '" + found + "'");
        }
        return true;
    }

    /** Fix small SAP-Number by adding Zeros */ 
    static String fixSAPNr(String aSAPNr) {
        int missing = SAP_LEN - aSAPNr.length();
        if (missing > 0) {
            return SAP_ZEROS.substring(0, missing) + aSAPNr;
        }
        return aSAPNr;
    }
    
    /** 
     * Import a single row from the Excel file.
     */
    @Override
	protected void importRow(int aSheetNum, String aSheetname, Row aRow) throws Exception {
        
        if (this.currentTransaction == null) {
            this.currentTransaction = this.kBase.beginTransaction();
        }
        
        short i = 0;
                                       i++;
        /* String mandator = aRow.getCell(i++).getStringCellValue(); */ // ignored as of now 
        String lifnr    = fixAny(aRow.getCell(i++)); // AKA SAP-Nr
        if (isSAP) {
            lifnr = fixSAPNr(lifnr);
        }
        
        if (StringServices.isEmpty(lifnr) || !Character.isLetterOrDigit(lifnr.trim().charAt(0))) {
        	logInfo("Ignoring Contact without lifnr", null);
        	return;
        }
        
        message = "LIFNR: " + lifnr; // show in progressinfo

        List theList = COSCompanyContact.getListBySAP(ContactFactory.COMPANY_TYPE, lifnr, this.mandator);
        COSCompanyContact contact = null;
        if (theList != null && !theList.isEmpty()) {
            contact = (COSCompanyContact) theList.get(0);
        }
        
        if (contact == null && !create) {
            logWarn("No CompanyContact for " + message + " found", null);
            return;
        }
        String anred = fixAny(aRow.getCell(i++)); 
        String land1 = fixAny(aRow.getCell(i++));  // country as iso code
        String name1 = fixAny(aRow.getCell(i++)); 
        String name2 = fixAny(aRow.getCell(i++));
        String ort01 = fixAny(aRow.getCell(i++)); if (ort01 == null) { ort01 = ""; }
        String pfach = fixAny(aRow.getCell(i++)); 
        String pstlz = fixAny(aRow.getCell(i++)); if (pstlz == null) { pstlz = ""; } // must not be empty
        String regio = fixAny(aRow.getCell(i++));
        String stras = fixAny(aRow.getCell(i++)); if (stras == null) { stras = ""; } // must not be empty
        String brsch = fixAny(aRow.getCell(i++));
        String telf1 = fixAny(aRow.getCell(i++));
        String telfx = fixAny(aRow.getCell(i++));
        String cage  = fixAny(aRow.getCell(i++));
        // cage or zzspc_sp is (in SAP) a customer specific add-on table 
        // and used for CAGE (Commercial And Government Entity-Code)
        
        if (StringServices.isEmpty(name1) || StringServices.isEmpty(name1.trim())) {
        	logInfo("Ignoring Contact without name " + name1, null);
        	return;
        }
        
        
        message += " name: " + name1; // show in progressinfo

        // put unsupported values in remarks ...
        String rmrks = "";
        if (!StringServices.isEmpty(anred)) {
        	rmrks += "anred: " + anred + ' ';
        }
        if (!StringServices.isEmpty(name2)) {
        	rmrks += "name2: " + name2 + ' ';
        }
        if (!StringServices.isEmpty(pfach)) {
        	rmrks += "pfach: " + pfach + ' ';
        }
        if (!StringServices.isEmpty(telfx)) {
        	rmrks += "telfx: " + telfx + ' ';
        }
        
        boolean       changed;
        AddressHolder aho;
        if (create && contact == null) {
            aho = new AddressHolder();
            aho.setProperty(AddressHolder.STREET     ,stras);
            aho.setProperty(AddressHolder.CITY       ,ort01);
            aho.setProperty(AddressHolder.COUNTRY    ,land1);
            aho.setProperty(AddressHolder.STATE      ,regio);
            aho.setProperty(AddressHolder.ZIP_CODE   ,pstlz);
            aho.setProperty(AddressHolder.PHONE      ,telf1);

            contact = (COSCompanyContact) ((COSCompanyContactCreateHandler)CommandHandlerFactory.getInstance()
            		.getHandler(COSCompanyContactCreateHandler.COMMAND_ID)).createCompany(name1,aho,this.mandator);
            contact.setForeignKey2(lifnr);
            
            contact.setValue(this.attName         , Boolean.TRUE);
            changed = true;
            logInfo("Created new Contact for " + message, null);
        } 
        else { // Update ?
            aho = contact.getAddress();
            
            changed = !Utils.equals(name1, contact.getName())
                   || !Utils.equals(rmrks, contact.getRemarks())
                   || !Utils.equals(brsch , contact.getValue(COSCompanyContact.SECTOR_ATTRIBUTE))
                   || !Utils.equals(cage  , contact.getValue(COSCompanyContact.ATTRIBUTE_CAGE))
                   || !Utils.equals(stras, aho.getProperty(AddressHolder.STREET))
                   || !Utils.equals(ort01, aho.getProperty(AddressHolder.CITY))
                   || !Utils.equals(land1, aho.getProperty(AddressHolder.COUNTRY))
                   || !Utils.equals(regio, aho.getProperty(AddressHolder.STATE))
                   || !Utils.equals(pstlz, aho.getProperty(AddressHolder.ZIP_CODE))
                   || !Utils.equals(telf1, aho.getProperty(AddressHolder.PHONE));
           if (changed) {
               contact.setName(name1);
               contact.setRemarks(rmrks);
               
               aho = new AddressHolder();
               aho.setProperty(AddressHolder.STREET     ,stras);
               aho.setProperty(AddressHolder.CITY       ,ort01);
               aho.setProperty(AddressHolder.COUNTRY    ,land1);
               aho.setProperty(AddressHolder.STATE      ,regio);
               aho.setProperty(AddressHolder.ZIP_CODE   ,pstlz);
               aho.setProperty(AddressHolder.PHONE      ,telf1);
    
               contact.setAddress(aho);
           }
        }
        
        if (contact != null) {
        	this.foundContacts.add(contact);
        }
        
        if (changed) { // includes created ...
            contact.setRemarks(rmrks);
            contact.setValue  (COSCompanyContact.SECTOR_ATTRIBUTE        , brsch);
            contact.setValue  (COSCompanyContact.ATTRIBUTE_CAGE, cage);

            commit++;
            if (commit > 32) {
                try {
                    this.currentTransaction.commit();
                    this.currentTransaction = null;
                } catch (KnowledgeBaseException e) {
                    logError("Failed to commit around " + message, e);
                    this.setError();
                    
                }
                commit = 0;
            }
        }
    }

    /** 
     * Overriden to for last commit and to clear the kBase.
     */
    @Override
	protected void tearDownImport() {
        if (this.currentTransaction != null) {
            try {
                this.currentTransaction.commit();
                this.currentTransaction = null;
            } catch (KnowledgeBaseException e) {
                logError("Failed to commit", e);
                
            }
        }
        
        if (!this.isDeltaImport) {
        	if (!this.hasError()) {
				{
					// Get all companies
	        		List<CompanyContact> theCompanies = COSCompanyContact.getListByMandators(ContactFactory.COMPANY_TYPE, Collections.singletonList(this.mandator));
	
	        		// Do not delete all found ones
	        		theCompanies.removeAll(this.foundContacts);
					
	        		for(CompanyContact theCompany : theCompanies) {
	        			String theName = theCompany.getName();
	        			// Do not delete default client and supplier
	        			if (!COSContactConstants.DEFAULT_CONTACT_CLIENT.equals(theName) && !COSContactConstants.DEFAULT_CONTACT_SUPPLIER.equals(theName)) {
	        				// Do not delete the ones not matching the attribute (don't delete suppliers when importing clients)
	        				if (Boolean.TRUE.equals(theCompany.getValue(this.attName))) {
	        					// Do not delete the ones without foreign key (locally created)
	        					if (!StringServices.isEmpty(theCompany.getValue(CompanyContact.FKEY2_ATTRIBUTE))) {
		        					// Do not delete the referenced ones
		        					if (CompanyContactDeleteRule.INSTANCE.isExecutable(theCompany).isExecutable()) {
		        					    this.currentTransaction = this.kBase.beginTransaction();
	                                    // Delete the remaining
	                                    logInfo("Delete Contact for LIFNR: " + theCompany.getForeignKey2() + " name: " + theName, null);
	                                    theCompany.tDelete();
	                                    
	                                    try {
	                                        this.currentTransaction.commit();
	                                        this.currentTransaction = null;
	                                    } catch (KnowledgeBaseException e) {
	                                        logError("Failed to commit last entries", e);
	                                    }
		        					}
	        					}
	        				}
	        			}
	        		}        		
				}
        	}
        	else {
        		logError("Don't clean up because of errors", null);
        	}
        }
        
        super.tearDownImport();
        kBase    = null;
        cFactory = null;
        this.foundContacts = null;
    }
    
}
