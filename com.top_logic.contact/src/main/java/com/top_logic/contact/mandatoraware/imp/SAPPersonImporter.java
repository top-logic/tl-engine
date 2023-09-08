/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.contact.mandatoraware.layout.COSPersonContactCreateHandler;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Utils;
import com.top_logic.util.monitor.MonitorMessage;

/**
 * Import SAP Data into {@link COSCompanyContact}s.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAPPersonImporter extends DataObjectImportTask  {
    
   /** 
     * Names of Attributes we expect when importing.
     */ 
    protected static String COLUMNS[] = { 
        "MANDA", "LIFNR", "TITLTX", "NAMEP", "NAMEV", "FUNKT", "TELF1", 
        "MOBIL", "TPRIV", "TELFX", "EMAIL", "MAYBE", "REMARK"};

    /** 
     * The DatasourceName configured to import from.
     * 
     * When mandator is given then the name of the attribute
     * where such a datsource is found
     */
    protected String importFrom;
    
    /** The Name of the Attribute in the outer DO where the (DO-)List of Objects is found  */
    protected String doListAttr;
    
    /** When true Company Contacts will be created instead of ignoring them */
    protected boolean create;

    /** The KnowledgeBase to import into */
    protected transient KnowledgeBase kBase;
    
    /** Used for creation of Contacts */
    protected transient COSPersonContactCreateHandler cFactory;
    
    /** Used to commit only every n-th record */ 
    protected int commit;

	/**
	 * Mandators where PersonContacts are found. 
	 */
	protected List mandators;

    /** 
     * Create a new SAPPersonImporter from (XML-) properties.
     * 
     * @param prop Properties to configure the task and 
     */
    public SAPPersonImporter(Properties prop) {
        super(prop);
        monitorType = MonitorMessage.Status.INFO;
        importFrom = prop.getProperty("importFrom"); 
        if (importFrom == null) {
            throw new IllegalArgumentException("Mandatory AttributeType 'importFrom' is missing");
        }
        doListAttr = prop.getProperty("doListAttr");
        if (doListAttr == null) {
            throw new IllegalArgumentException("Mandatory AttributeType 'doListAttr' is missing");
        }
        create = StringServices.parseBoolean(prop.getProperty("create"));
        finished = true;
        this.setRunOnStartup(false);
    }

    @Override
	protected boolean shouldImport(Mandator aMandator) {
    	if (aMandator == null) {
    		return false;
    	}
    	
    	if (!aMandator.allowType(ContactFactory.STRUCTURE_NAME)) {
    		return false;
    	}
    	
    	try {
    		return !StringServices.isEmpty(this.getImportDSN(aMandator));
    	}
    	catch (Exception ex) {
    		return false;
    	}
    }

    /** 
     * Retrieve the List of DataObjects we are going to import.
     * 
     * This allows test classes to supply a DataObject for testing.
     */
    @Override
	protected List getImportObjects(Mandator aMandator) throws DatabaseAccessException, DataObjectException {
        // Init the parents incl. current mandator
        if (aMandator != null) {
        	this.mandators = new ArrayList();
        	StructuredElement theParent=aMandator;
        	while(theParent != null) {
            	this.mandators.add(theParent);
        		theParent = theParent.getParent();
        	}
        }
        
        DataAccessProxy dap  = new DataAccessProxy(getImportDSN(aMandator));
        return  (DOList) dap.getObjectEntry().getAttributeValue(doListAttr);
    }

    /**
     * Return a DatasourceName where the Data can be found.
     * 
     * This version extracts the value from the mandator if not null,
     * otherwise importFrom will be returned directly.
     * 
     * @param aMandator the mandator
     */
    protected String getImportDSN(Mandator aMandator) throws DatabaseAccessException {
        if (aMandator == null) {
            Logger.info("No mandator given, using " + importFrom, this);
            return importFrom;
        }
        try {
        	return (String) aMandator.getValue(importFrom);
        }
        catch (Exception ex) {
        	throw new DatabaseAccessException("Problem getting value '" + importFrom + "' from Mandator " + aMandator);
        }
    }
    
    /** 
     * Setup the kBase for importing.
     * 
     * This will call setupLogWriter() so be sure to call super.
     */
    @Override
	protected void setupImport() throws Exception {
        super.setupImport();
        kBase     = COSPersonContact.getDefaultKnowledgeBase();
        cFactory  = (COSPersonContactCreateHandler) CommandHandlerFactory.getInstance().getHandler(COSPersonContactCreateHandler.COMMAND_ID);
        commit    = 0;
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

    @Override
	protected void startImport() {
    	this.startImport(Mandator.getRootMandator(), true);
    }
    
    /** 
     * Import a single DataObject.
     */
    @Override
	protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
    	String lastName  = (String) aDo.getAttributeValue(COLUMNS[3]);
    	String firstName = (String) aDo.getAttributeValue(COLUMNS[4]);
    	String theMessage;
    	
        if (StringServices.isEmpty(lastName) || !Character.isLetterOrDigit(lastName.trim().charAt(0))) {
//        	logInfo("Ignoring Contact without surname", null);
        	return;
        }

        if (StringServices.isEmpty(firstName) || !Character.isLetterOrDigit(firstName.trim().charAt(0))) {
//        	logInfo("Ignoring Contact without first name", null);
        	return;
        }
    	
    	
        String lifnr    = ExcelPersonContactImporter.fixSAPNr((String) aDo.getAttributeValue(COLUMNS[1])); // AKA SAP-Nr
        if (StringServices.isEmpty(lifnr) || !Character.isLetterOrDigit(lifnr.trim().charAt(0))) {
//        	logInfo("Ignoring Contact without lifnr", null);
        	return;
        }
        
        theMessage = "SAP number: " + lifnr;

        List theList = COSCompanyContact.getListBySAP(ContactFactory.COMPANY_TYPE, lifnr, this.mandators);
        COSCompanyContact theCompany = null;
        if (theList != null && !theList.isEmpty()) {
            theCompany = (COSCompanyContact) theList.get(0);
        }
        
        if (theCompany == null) {
            logWarn("No CompanyContact with SAP number '" + lifnr + "' found", null);
            return;
        }

        List thePersons = COSPersonContact.getListByName(lastName, firstName, this.mandators);
        
        COSPersonContact thePerson = null;
        if (!thePersons.isEmpty()) {
        	if (thePersons.size() > 1) {
        		logWarn("More than one person with name " + lastName + ", " + firstName, null);
            	return;
        	}
        	else {
        		thePerson = (COSPersonContact) thePersons.get(0);
        	}
        }

        if (thePerson == null && !create) {
            numberWARN ++;
            logWarn("No person named '" + lastName + ", " + firstName + "' found", null);
            return;
        }
        
        theMessage += ", Person: " + lastName + ", " + firstName; // show in progressinfo

        Boolean maybeownerB = null;
        String maybeowner   = (String) aDo.getAttributeValue(COLUMNS[11]);
        if ("TRUE".equalsIgnoreCase(maybeowner) || "YES".equalsIgnoreCase(maybeowner) || "JA".equalsIgnoreCase(maybeowner) || "1".equals(maybeowner)) {
        	maybeownerB = Boolean.TRUE;
        }
        else if ("FALSE".equalsIgnoreCase(maybeowner) || "NO".equalsIgnoreCase(maybeowner) || "NEIN".equalsIgnoreCase(maybeowner) || "0".equals(maybeowner)) {
        	maybeownerB = Boolean.FALSE;
        }
        
        boolean changed;
        boolean personCreated = false;

        if (create && thePerson == null) {
            thePerson = (COSPersonContact) cFactory.createPersonContact(lastName, firstName, aMandator);
            
            changed = true;
            personCreated = true;
            logInfo("Created new PersonContact for " + theMessage, null);
        } 
        else { // Update ?
            logInfo("Processing " + theMessage, null);
            changed = !Utils.equals(aDo.getAttributeValue(COLUMNS[2]),  thePerson.getValue(COSPersonContact.ATT_TITLE))
			       || !Utils.equals(aDo.getAttributeValue(COLUMNS[5]),  thePerson.getValue(COSPersonContact.ATT_POSITION))
			       || !Utils.equals(aDo.getAttributeValue(COLUMNS[6]),  thePerson.getValue(COSPersonContact.ATT_PHONE_OFFICE))
			       || !Utils.equals(aDo.getAttributeValue(COLUMNS[7]),  thePerson.getValue(COSPersonContact.ATT_PHONE_MOBILE))			           
			       || !Utils.equals(aDo.getAttributeValue(COLUMNS[8]),  thePerson.getValue(COSPersonContact.ATT_PHONE_PRIVATE))
			       || !Utils.equals(aDo.getAttributeValue(COLUMNS[9]),  thePerson.getValue(COSPersonContact.ATT_FAX))
			       || !Utils.equals(aDo.getAttributeValue(COLUMNS[10]), thePerson.getValue(COSPersonContact.ATT_MAIL))
			       //TODO KBU SEC/CHECK REIMPLEMENT
//			       || maybeownerB != null && !Utils.equals(maybeownerB, thePerson.getValue(COSPersonContact.ATT_MAYBEOWNER))	// TODO KBU check boolean!
			       || !Utils.equals(aDo.getAttributeValue(COLUMNS[12]), thePerson.getValue(COSPersonContact.REMARKS_ATTRIBUTE));
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
        	thePerson.setValue(COSPersonContact.ATT_TITLE,         aDo.getAttributeValue(COLUMNS[2]));
		    thePerson.setValue(COSPersonContact.ATT_POSITION,      aDo.getAttributeValue(COLUMNS[5]));
		    thePerson.setValue(COSPersonContact.ATT_PHONE_OFFICE,  aDo.getAttributeValue(COLUMNS[6]));
		    thePerson.setValue(COSPersonContact.ATT_PHONE_MOBILE,  aDo.getAttributeValue(COLUMNS[7]));
		    thePerson.setValue(COSPersonContact.ATT_PHONE_PRIVATE, aDo.getAttributeValue(COLUMNS[8]));
		    thePerson.setValue(COSPersonContact.ATT_FAX,           aDo.getAttributeValue(COLUMNS[9]));
		    thePerson.setValue(COSPersonContact.ATT_MAIL,          aDo.getAttributeValue(COLUMNS[10]));
		    //TODO KBU SEC/CHECK REIMPLEMENT
//            if (maybeownerB != null) {
//            	thePerson.setValue(COSPersonContact.ATT_MAYBEOWNER, maybeownerB);
//            }
            thePerson.setValue(COSPersonContact.REMARKS_ATTRIBUTE, aDo.getAttributeValue(COLUMNS[12]));

            commit++;
            if (commit > 32 || personCreated) { // Note force commit on person creation. KB search will fail for them otherwise
                if (!kBase.commit()) {
                    logError("Failed to commit around " + message, null);
                }
                commit = 0;
            }
            numberOK ++;
        }
    }
    

    /** 
     * Overriden to clear the kBase.
     */
    @Override
	protected void tearDownImport() {
        if (!kBase.commit()) {
            logError("Failed to commit last entries", null);
        }
        super.tearDownImport();
        kBase    = null;
        cFactory = null;
    }
}
