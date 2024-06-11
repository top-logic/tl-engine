/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external.importer;

import java.sql.SQLException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.contact.external.ExternalContact;
import com.top_logic.contact.external.ExternalContacts;
import com.top_logic.contact.external.PlainExternalContact;
import com.top_logic.contact.mandatoraware.imp.AbstractExcelDOImporter;
import com.top_logic.dob.DataObject;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * {@link AbstractExcelDOImporter} for {@link ExternalContact}s.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class ExcelExternalContactImporter extends AbstractExcelDOImporter {
	/**
	 * Names of Attributes we expect when importing.
	 */
	protected static String COLUMNS[] = { "U-Nummer", "Nachname", "Vorname", "Titel", "Fachbereich", "Gesellschaft", "Telefon", "Fax", "E-Mail", "Mobiltelefon"};

	KnowledgeBase kBase;
	
	public ExcelExternalContactImporter(BinaryData importSource, String aSheetName, boolean doDeleteWhenDone) {
		super(importSource, aSheetName, doDeleteWhenDone);
	}

	@Override
	protected String[] getExpectedColumnNames() {
	    return COLUMNS;
	}
	
    /** 
     * Import on the whole structure
     * 
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#startImport()
     */
    @Override
	protected void startImport() {
    	this.startImport(null, false);
    }
    
	/** 
	 * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#setupImport()
	 */
	@Override
	protected void setupImport() throws Exception {
		super.setupImport();
		
		kBase = PersistencyLayer.getKnowledgeBase();
		
		try {
			ExternalContacts.dropAllContacts();
			kBase.commit();
		} catch (SQLException e) {
			Logger.error("Failed to drop all ExternalContact before importing", e, this);
		}
	}
	
	@Override
	protected void tearDownImport() {
		super.tearDownImport();
		
		if (!kBase.commit()) {
			logError("Final commit failed!", null);
		}
		
		ExternalContacts.resetContactCache();
	}
	
	/** 
	 * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#importItem(com.top_logic.dob.DataObject, com.top_logic.element.structured.wrap.Mandator)
	 */
	@Override
	protected void importItem(DataObject aDo, Mandator aMandator)
			throws Exception {
        short i = 0;
        
        String theUNo       = (String) aDo.getAttributeValue(COLUMNS[i++]);
        String theName      = (String) aDo.getAttributeValue(COLUMNS[i++]);
        String theFirstName = (String) aDo.getAttributeValue(COLUMNS[i++]);
        i++;
        String theDivision  = (String) aDo.getAttributeValue(COLUMNS[i++]);
        i++;
        String thePhone     = (String) aDo.getAttributeValue(COLUMNS[i++]);
        i++;
        String theMail      = (String) aDo.getAttributeValue(COLUMNS[i++]);
             
        PlainExternalContact theEPC = new PlainExternalContact(theUNo, theFirstName, theName,
				theDivision, theMail, thePhone, this.getName());

		try {
			ExternalContacts.newContact(theEPC);
			numberOK++;
			
			if (numberOK % 50 == 0) {
				if (!kBase.commit()) {
					logError("Commit failed at count " + currentSize, null);
				}
			}
		}
		catch (Exception ex) {
			logError("Failed to create ExternalContact " + theEPC, ex);
			numberERR++;
		}

		if (!kBase.commit()) {
		    logError("Commit failed at count " + currentSize, null);
		}
	}

}
