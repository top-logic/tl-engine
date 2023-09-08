/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.currency.Currency;

/**
 * Import SAP Volume data into existing COSCompanyContacts.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAPVolumeImporter extends DataObjectImportTask  {
    
    /** 10 zeros to fill up SAP-Nr */
    static final String ZEROS = "0000000000";
    
    /** 
      * Names of Attributes we expect when importing.
      */ 
     protected String COLUMNS[] = { 
        "LIFNR", "CURRENCY", "VOLUME" };

     /** The DatasourceName configured to import from */
     protected String importFrom;
     
     /** Map of currencies, indexed by code */
     protected Map currencies;
     
     /** The KnowledgeBase to import into */
     protected transient KnowledgeBase kBase;

     /** 
      * Create a new SAPVolumeImporter (XML-) properties.
      * 
      * Not used since this importer is triggered manually.
      * 
      * @param prop Properties to configure the task and this class 
      */
     /*
     public SAPVolumeImporter(Properties prop) {
         super(prop);
         monitorType = MonitorMessage.INFO;
         importFrom = prop.getProperty("importFrom"); 
         if (importFrom == null) {
             throw new IllegalArgumentException("Mandatory AttributeType 'importFrom' is missing");
         }
         finished = true;
     }*/
     
     /**
      * Create a SAPVolumeImporter impoerting from given DataSourceName.
      */
	public SAPVolumeImporter(String name, String aDSN) {
		super(name);
         importFrom = aDSN;
     }

     // implementation of DataObjectImportTask

     /** 
      * Setup the the currencies map.
      */
     @Override
	protected void setupImport() throws Exception {
         super.setupImport();
         currencies = new HashMap();
         kBase      = AbstractContact.getDefaultKnowledgeBase(); 
     }

    /** 
     * Import on the whole structure
     * 
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#startImport()
     */
    @Override
	protected void startImport() {
    	this.startImport(Mandator.getRootMandator(), true);
    }
     
     /** 
      * Retrieve the List of DataObjects we are going to import.
      * 
      * This allow test classes to supply a DataObject for testing.
      */
     @Override
	protected List getImportObjects(Mandator aMandator) throws DatabaseAccessException, DataObjectException {
         DataAccessProxy dap            = new DataAccessProxy(importFrom);
                                                                   // TODO make this configurable
         return          (DOList) dap.getObjectEntry().getAttributeValue("Z_ME_GET_SUPPLIER_VOLUME(TBL)");
     }
     
     /** 
      * Check that the first COLUMNS are Attributes of aMeta.
      * 
      * @throws DataObjectException when format is not OK.
      */
     @Override
	protected void checkFormat(MetaObject aMeta) throws DataObjectException {
         for (int i=0; i < COLUMNS.length; i++) {
             String col   = COLUMNS[i];
             if (null == MetaObjectUtils.getAttribute(aMeta, col)) {
                 throw new NoSuchAttributeException("Expected column '" + col + "not found in " + aMeta);
             }
         }
     }
     /** 
      * Import a single DataObject.
      */
     @Override
	protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
         
         String sapNr    = (String) aDo.getAttributeValue(COLUMNS[0]);
         String currency = (String) aDo.getAttributeValue(COLUMNS[1]);
         String value    = (String) aDo.getAttributeValue(COLUMNS[2]);
         
         Currency theCurrency = (Currency) currencies.get(currency);
         if (theCurrency == null) {
             theCurrency = Currency.getCurrencyInstance(currency);
         }
         if (theCurrency == null) {
             logWarn("Invalid Currency '" + currency + "' ignored.", null);
             return;
         }
         
         sapNr = normaliseSAPNr(sapNr);
         AbstractContact contact = AbstractContact.getByForeignKey2(kBase,sapNr);
         if (!(contact instanceof COSCompanyContact)) {
             logInfo("No COSCompanyContact found for '" + sapNr + "' ignored", null);
             return;
         }
         
         COSCompanyContact ccContact = (COSCompanyContact) contact;
         
         Double dValue;
         try {
			dValue = Double.valueOf(value);
         } catch (NumberFormatException nfx) {
             Logger.info("'" + value + "' is not a valid number, ignored", this);
             return;
         }
         ccContact.setValue(COSCompanyContact.ATTRIBUTE_VOLUME  , dValue);
         ccContact.setValue(COSCompanyContact.ATTRIBUTE_CURRENCY, theCurrency);

         if (!kBase.commit()) {
             logError("Failed to commit()", null);
         }
     }
     
     /**
      * Fill up given sapNr with zeros to 10 places.
      * 
      * TODO KHA may not be needed...
      */
     public static final String normaliseSAPNr(String sapNr) {
         int len = sapNr.length();
         if (len < 10) {
             sapNr = ZEROS.substring(0, ZEROS.length() - len) + sapNr;
         }
         return sapNr;
     }
     
     /**
      * Must support contacts and have a supplier config
      *  
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#shouldImport(com.top_logic.element.structured.wrap.Mandator)
     */
    @Override
	protected boolean shouldImport(Mandator aMandator) {
     	if (aMandator == null) {
     		return false;
     	}
     	
     	if (!aMandator.allowType(ContactFactory.STRUCTURE_NAME)) {
     		return false;
     	}
     	
     	return true;
     }
     

     /** 
      * Overriden to clear the kBase and the kBase.
      */
     @Override
	protected void tearDownImport() {
         super.tearDownImport();
         currencies = null;
         kBase      = null;
     }
}
