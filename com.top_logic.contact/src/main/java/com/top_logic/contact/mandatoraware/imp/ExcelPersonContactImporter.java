/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.POIExcelImporter;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.contact.mandatoraware.layout.COSPersonContactCreateHandler;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.Utils;

/**
 * Import PersonContacts from an Excel file.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class ExcelPersonContactImporter extends POIExcelImporter {

    /** 
     * Names of Attributes we expect when importing.
     */ 
    protected static String COLUMNS[] = { 
        "MANDATOR", "SUPPLIER_NO", "TITLE", "SURNAME", "FIRSTNAME", "POSITION", "PHONE_COMPANY", 
        "PHONE_CELL", "PHONE_PRIVATE", "FAX", "EMAIL", "MAYBE_OWNER", "REMARKS"};

    /** We fill up SAP-Numbers with zeros ... */
    protected static String SAP_ZEROS = "0000000000"; 

    /** We fill up SAP-Numbers with zeros ... */
    protected static int     SAP_LEN = SAP_ZEROS.length(); 

    /** When true PersonContacts will be created instead of ignoring them */
    protected boolean create;

    /** The KnowledgeBase to import into */
    protected transient KnowledgeBase kBase;
    
    protected Transaction currentTransaction;
    
    /** Used for ceration of Contacts */
    protected transient ContactFactory cFactory;

    /** Used to commit only every n-th record */ 
    protected int commit;

    /** Indicates if original Data is in SAP-System */ 
    protected boolean isSAP;

    /** 
     * when != null only Contacts for this mandator will be updated.
     * 
     * Otherwise the first matching contact will be updated or created.  
     */
    protected Mandator mandator;
    
    /** Mandators where PersonContacts are found. */
    protected List mandators;

    
    /** 
     * Create a new ExcelSupplier20060126Importer for given File.
     * @param aCreate   When true Company Contacts will be created instead of ignoring them.
     * @param aMandator when != null only Contacts for this mandator will be updated, 
     *                  otherwise the first matching contact will be updated or created.  
     */
	public ExcelPersonContactImporter(BinaryData importSource, boolean aCreate, Mandator aMandator) {
		super(importSource);
        create   = aCreate;
        mandator = aMandator;
        if (aMandator != null) {
            isSAP = !StringServices.isEmpty((String) aMandator.getValue(Mandator.SAP_SUPPLIERS));
        }
        
        // Init the parents incl. current mandator
        if (this.mandator != null) {
        	this.mandators = new ArrayList();
        	StructuredElement theParent=this.mandator;
        	while(theParent != null) {
            	this.mandators.add(theParent);
        		theParent = theParent.getParent();
        	}
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
    }

    /** 
     * Check that the first column (as found in aRow) is as expected.
     * 
     * @throws IOException when format is not OK.
     */
    @Override
	protected boolean checkColumnFormat(int aSheetNum, String aSheetname, Row aRow) throws IOException {
        String   col, found;
		Cell cell;
        for (short i=0; i < COLUMNS.length; i++) {
            col   = COLUMNS[i];
            cell  = aRow.getCell(i);
            found = cell.getStringCellValue();
            if (!col.equals(found)) {
                throw new IOException("Expected column '" + col + "' found '" + found + "'");
            }
        }
        return true;
    }

    /** Fix small SAP-Number by adding Zeros */ 
    public static String fixSAPNr(String aSAPNr) {
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
        short i = 0;
                    
        if (this.currentTransaction == null) {
            this.currentTransaction = this.kBase.beginTransaction();
        }
        
        i++; /* String mandator = aRow.getCell(i++).getStringCellValue(); */ // ignored as of now 
        String lifnr    = fixAny(aRow.getCell(i++)); // AKA SAP-Nr
        if (isSAP) {
            lifnr = fixSAPNr(lifnr);
        }
        
        if (StringServices.isEmpty(lifnr) || !Character.isLetterOrDigit(lifnr.trim().charAt(0))) {
//        	logInfo("Ignoring Contact without lifnr", null);
        	return;
        }
        
        message = "SUPPLIER_NO:" + lifnr; // show in progressinfo

        List theList = COSCompanyContact.getListBySAP(ContactFactory.COMPANY_TYPE, lifnr, this.mandators);
        COSCompanyContact theCompany = null;
        if (theList != null && !theList.isEmpty()) {
            theCompany = (COSCompanyContact) theList.get(0);
        }
        
        if (theCompany == null) {
            logWarn("No CompanyContact for " + message + " found", null);
            return;
        }

        String title     = fixAny(aRow.getCell(i++)); if (title == null) { title = ""; }
        String surname   = fixAny(aRow.getCell(i++)); 
        String firstname = fixAny(aRow.getCell(i++)); 
        String position  = fixAny(aRow.getCell(i++));

        String phonecomp = fixAny(aRow.getCell(i++));
        String phonecell = fixAny(aRow.getCell(i++));
        String phonepriv = fixAny(aRow.getCell(i++));
        String fax       = fixAny(aRow.getCell(i++));
        String email     = fixAny(aRow.getCell(i++));
        String maybeowner= fixAny(aRow.getCell(i++)); if(maybeowner == null) { maybeowner = "false"; }
        String remarks   = fixAny(aRow.getCell(i++));

        if (StringServices.isEmpty(surname) || !Character.isLetterOrDigit(surname.trim().charAt(0))) {
//        	logInfo("Ignoring Contact without surname", null);
        	return;
        }

        if (StringServices.isEmpty(firstname) || !Character.isLetterOrDigit(firstname.trim().charAt(0))) {
//        	logInfo("Ignoring Contact without first name", null);
        	return;
        }
        
        maybeowner = maybeowner.trim().toUpperCase();
        Boolean maybeownerB = null;
        if ("TRUE".equalsIgnoreCase(maybeowner) || "YES".equalsIgnoreCase(maybeowner) || "JA".equalsIgnoreCase(maybeowner) || "1".equals(maybeowner)) {
        	maybeownerB = Boolean.TRUE;
        }
        else if ("FALSE".equalsIgnoreCase(maybeowner) || "NO".equalsIgnoreCase(maybeowner) || "NEIN".equalsIgnoreCase(maybeowner) || "0".equals(maybeowner)) {
        	maybeownerB = Boolean.FALSE;
        }
        
        List thePersons = COSPersonContact.getListByName(surname, firstname, this.mandators);
        
        COSPersonContact thePerson = null;
        if (!thePersons.isEmpty()) {
        	if (thePersons.size() > 1) {
        		logWarn("More than 1 person with name " + surname + ", " + firstname, null);
            	return;
        	}
        	else {
        		thePerson = (COSPersonContact) thePersons.get(0);
        	}
        }
        
        message += " name :" + surname + ", " + firstname; // show in progressinfo
        boolean personCreated = false;
        boolean       changed;
        if (create && thePerson == null) {
			COSPersonContactCreateHandler theHandler = COSPersonContactCreateHandler.INSTANCE;
            thePerson = (COSPersonContact) theHandler.createPersonContact(surname, firstname, this.mandator);
            
            changed = true;
            personCreated = true;
            logInfo("Created new PersonContact for " + message, null);
        } 
        else { // Update ?
            changed = !Utils.equals(title,       thePerson.getValue(COSPersonContact.TITLE))
                   || !Utils.equals(phonecell,   thePerson.getValue(COSPersonContact.PHONE_MOBILE))
                   || !Utils.equals(phonepriv,   thePerson.getValue(COSPersonContact.PHONE_PRIVATE))
                   || !Utils.equals(phonecomp,   thePerson.getValue(COSPersonContact.PHONE))
                   || !Utils.equals(email,       thePerson.getValue(COSPersonContact.EMAIL))
                   || !Utils.equals(position,    thePerson.getValue(COSPersonContact.POSITION))
                   //TODO KBU SEC/CHECK REIMPLEMENT
//                   || maybeownerB != null && !Utils.equals(maybeownerB, thePerson.getValue(COSPersonContact.ATT_MAYBEOWNER))
                   || !Utils.equals(fax,         thePerson.getValue(COSPersonContact.ATT_FAX))
                   || !Utils.equals(remarks,     thePerson.getValue(COSPersonContact.REMARKS_ATTRIBUTE));
        }
        
        if (!thePerson.equals(theCompany.getValue(COSCompanyContact.ATTRIBUTE_LEAD_BUYER))) {
        	try {
	        	theCompany.setValue(COSCompanyContact.ATTRIBUTE_LEAD_BUYER, thePerson);
	        	
	        	changed = true;
        	}
        	catch (Exception ex) {
        		logError("Person " + thePerson.getFullname() + " on Mandator " + thePerson.getMandator().getName() 
        				+ " cannot be assigned to " + theCompany.getName() + " on Mandator " 
        				+ theCompany.getMandator().getName(), null);
        	}
        }
        
        if (changed) { // includes created ...
            thePerson.setValue(COSPersonContact.TITLE,          title);
            thePerson.setValue(COSPersonContact.PHONE_MOBILE,   phonecell);
            thePerson.setValue(COSPersonContact.PHONE_PRIVATE,  phonepriv);
            thePerson.setValue(COSPersonContact.PHONE,   phonecomp);
            thePerson.setValue(COSPersonContact.EMAIL,           email);
            thePerson.setValue(COSPersonContact.POSITION,       position);
            //TODO KBU SEC/CHECK REIMPLEMENT
//            if (maybeownerB != null) {
//            	thePerson.setValue(COSPersonContact.ATT_MAYBEOWNER, maybeownerB);
//            }
            thePerson.setValue(COSPersonContact.ATT_FAX,            fax);
            thePerson.setValue(COSPersonContact.REMARKS_ATTRIBUTE,  remarks);
            
            commit++;
            if (commit > 32 || personCreated) { // Note force commit on person creation. KB search will fail for them otherwise
                try {
                    this.currentTransaction.commit();
                    this.currentTransaction = null;
                } catch (KnowledgeBaseException e) {
                    logError("Failed to commit around " + message, e);
                    
                }
                commit = 0;
            }
        }
    }

    @Override
    protected void endSheetImport(int numSheet, String sheetName) {
        super.endSheetImport(numSheet, sheetName);
        if (this.currentTransaction != null) {
            try {
                this.currentTransaction.commit();
                this.currentTransaction = null;
            } catch (KnowledgeBaseException e) {
                logError("Failed to commit sheet " + sheetName, e);
                
            }
        }
    }
    
    /** 
     * Overriden to for last commit and to clear the kBase.
     */
    @Override
	protected void tearDownImport() {
        super.tearDownImport();
        kBase    = null;
        cFactory = null;
    }
    
}
