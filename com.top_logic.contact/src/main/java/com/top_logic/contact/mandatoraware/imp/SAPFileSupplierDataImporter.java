/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.SAPFormatHelper;
import com.top_logic.contact.mandatoraware.layout.COSCompanyContactCreateHandler;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.GenericDataObject;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Country;
import com.top_logic.util.Utils;

/**
 * The SAPFileSupplierDataImporter creates or updates CompanyContacts from  
 * a plain ascii file.
 * 
 * The format of the data file may be configured using 
 * {@link #attributeNames} and {@link #columnSizes}.
 * If column names match attribute names of a contact, these attributes will 
 * be set automatically.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class SAPFileSupplierDataImporter extends DataObjectImportTask {

    /**
     * Boolean attribute, whether companies should be imported or not
     */
    private static final String FLAG_IMPORT = "sapFileDataImportImporter";

    /**
     * String attribute
     */
    private static final String FILE_VARIABLE_PART = "sapFileVariablePart";

    /**
     * String attribute, path to the data file in format of the
     * {@link FileManager}. May contain one parameter as of
     * {@link MessageFormat}
     */
    private static final String FILE_CONSTANT_PART = "sapFileConstantPart";
    
    /** flag for deletion contacts */
    private boolean doDelete;
    /** flag for create contacts */
    private boolean doCreate;
    /** flag for updating existing contacts */
    private boolean doUpdate;
    /** flag for comming changes */
    private boolean doCommit;
    /** flag for deleting old files */
    private boolean doDeleteFile;
    
    /** boolean attribute for flagging */
    private String   deleteMark;
    /** all column/attribute names, in order. These names should match the names of meta attributes */
    private String[] attributeNames;
    /** all sizes of columns, in order */
    private int[]    columnSizes;
    /** inital size of interal object caches */
    private int      estimatedObjects;
    /** interval after how many operations a commit is done */
    private int      commitInterval;
    
    /** map of {@link Mapping}s that are applied on values indexed by column name */
    private Map<String, Mapping> mappingFunctions;
    
    private KnowledgeBase  kBase;
    private Transaction    currentTransaction;
    private COSCompanyContactCreateHandler factory;
    
    /** cache companies by fkey */
    private Map<Mandator, Map<String, CompanyContact>> ccCache;
    /** cache all modified companies (=created or updated)*/
    private Set<CompanyContact> ccImported;
    
    private int countCreated;
    private int countUpdated;
    private int countDeleted;

    /**
	 * Creates a {@link SAPFileSupplierDataImporter}.
	 * 
	 */
	public SAPFileSupplierDataImporter(String name) {
		super(name);
    }

    /**
	 * Creates a {@link SAPFileSupplierDataImporter}.
	 */
    public SAPFileSupplierDataImporter(Properties aProp) {
        super(aProp);
        
        this.kBase   = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        
        this.factory = (COSCompanyContactCreateHandler) CommandHandlerFactory.getInstance().getHandler(COSCompanyContactCreateHandler.COMMAND_ID);
        
        this.setUp(aProp);
        
    }

    /**
     * Read properties and set up parameters
     */
    public void setUp(Properties aProp) {
        this.deleteMark = aProp.getProperty("deleteMark");
        if (StringServices.isEmpty(this.deleteMark)) {
            this.deleteMark = null;
        }
        
        this.doCommit = Boolean.valueOf(aProp.getProperty("commit", "false")).booleanValue();
        this.doUpdate = Boolean.valueOf(aProp.getProperty("update", "true" )).booleanValue();
        this.doCreate = Boolean.valueOf(aProp.getProperty("create", "true" )).booleanValue();
        this.doDelete = Boolean.valueOf(aProp.getProperty("delete", "false")).booleanValue();
        this.doDeleteFile = Boolean.valueOf(aProp.getProperty("deleteFile", "false")).booleanValue();
        
        this.estimatedObjects = Integer.parseInt(aProp.getProperty("estimatedObjects", "45000"));
        this.commitInterval   = Integer.parseInt(aProp.getProperty("commitInterval", "50"));
        
        this.attributeNames = StringServices.toArray(aProp.getProperty("attributeNames"), ",");
        String[]   theSizes = StringServices.toArray(aProp.getProperty("columnSizes"), ",");
        
        if (attributeNames.length != theSizes.length) {
            throw new IllegalArgumentException("attributeNames must have same length as columnSizes");
        }
        
        this.columnSizes = new int[theSizes.length];
        
        for (int i=0; i<theSizes.length; i++) {
            this.columnSizes[i] = Integer.parseInt(theSizes[i]);
        }

        this.mappingFunctions = new HashMap<>();
        
        Iterator theIter = aProp.keySet().iterator();
        while (theIter.hasNext()) {
            String theKey = (String) theIter.next();
            if (theKey.startsWith("mapping:")) {
                String theValue = aProp.getProperty(theKey);
                String theAttr  = theKey.substring("mapping:".length());
                try {
                    Class<Mapping> theClass = (Class<Mapping>) Class.forName(theValue);
                    this.mappingFunctions.put(theAttr, theClass.newInstance());
                } catch (Exception ex) {
                    logError("Unable to set up mapping function " + theValue, ex);
                }
                
            }
        }
    }
    
    /**
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#checkFormat(com.top_logic.dob.MetaObject)
     */
    @Override
	protected void checkFormat(MetaObject aMeta) throws DataObjectException {
        // who cares?
        return;
    }

    /**
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#startImport()
     */
    @Override
	protected void startImport() {
        try {
            this.startImport(Mandator.getRootMandator(), true);
        } finally {
            if (this.inTransaction()) {
                this.currentTransaction = null;
            }
        }
    }
    
    @Override
    protected boolean shouldImport(Mandator aMandator) {
        if (Utils.isTrue(aMandator.getBoolean(FLAG_IMPORT))) {
            if (StringServices.isEmpty(aMandator.getString(FILE_CONSTANT_PART))) {
                Logger.error("Invalid configuration of mandator " + aMandator.getName() + ": " + FLAG_IMPORT + " is activated, but "
                        + FILE_CONSTANT_PART + " is not set.", SAPFileSupplierDataImporter.class);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Init caches, counters, etc.
     */
    @Override
	protected void setupImport() throws Exception {
        super.setupImport();
        
        this.kBase   = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        this.factory = (COSCompanyContactCreateHandler) CommandHandlerFactory.getInstance().getHandler(COSCompanyContactCreateHandler.COMMAND_ID);
        
        // cache contacts by foreign key
        List theContacts = ContactFactory.getInstance().getAllContacts(ContactFactory.COMPANY_TYPE);
        this.ccCache = new HashMap<>();
        int countAdded = 0;
        for (Iterator theIter = theContacts.iterator(); theIter.hasNext();) {
            CompanyContact theContact = (CompanyContact) theIter.next();
            if (addToCache(theContact)) {
                countAdded++;
            }
        }
        
        logInfo("Cached " + countAdded + " contacts.", null);
        
        // cache all imported objects (= all objects modified by the importer)
        this.ccImported = new HashSet<>(this.estimatedObjects);
        
        // reset counter
        this.countCreated = 0;
        this.countDeleted = 0;
        this.countUpdated = 0;
        
        if (this.doCommit) {
            this.beginTransaction();
        }
    }
    
    protected final boolean addToCache(CompanyContact contact) {
        String fkey = this.getFKey(contact);
        if (StringServices.isEmpty(fkey)) {
            return false;
        }

        Mandator mandator = (Mandator) contact.getValue(COSCompanyContact.ATTRIBUTE_MANDATOR);
        Map<String, CompanyContact> innerMap = this.ccCache.get(mandator);
        if (innerMap == null) {
            innerMap = new HashMap<>();
            this.ccCache.put(mandator, innerMap);
        }

        CompanyContact theOld = innerMap.put(SAPFormatHelper.fillSAPNo(fkey), contact);
        if (theOld != null) {
            logWarn("Duplicate contact found for " + fkey + ". Using " + printContact(theOld), null);
            innerMap.put(fkey, theOld);
            return false;
        }

        return true;
    }

    protected final CompanyContact getFromCache(Mandator mandator, String fkey) {
        Map<String, CompanyContact> innerMap = this.ccCache.get(mandator);
        if (innerMap == null) {
            return null;
        }

        return innerMap.get(SAPFormatHelper.fillSAPNo(fkey));
    }

    protected final boolean inTransaction() {
        return this.currentTransaction != null;
    }
    
    
    protected final void beginTransaction() {
        if (this.inTransaction()) {
            throw new IllegalStateException("There is alreay an open transaction");
        }
        
        this.currentTransaction = this.kBase.beginTransaction();
    }
    
    protected final boolean commitTransaction() {
        if (! this.inTransaction()) {
            throw new IllegalStateException("There is no open transaction");
        }
        
        try {
            this.currentTransaction.commit();
            this.currentTransaction = null;
            return true;
        } catch (KnowledgeBaseException e) {
            logWarn("Commit failed", e);
        }
        return false;
    }
    
    /**
     * All contacts with sap numbers that not had been imported by this run will 
     * be deleted or marked as deleted, if they a still in use.   
     */
    @Override
	protected void importFinished(List aTheList, Mandator aMandator) {
        super.importFinished(aTheList, aMandator);
        
        if (this.doCommit) {
            if (this.inTransaction() && ! this.commitTransaction()) {
                logWarn("Final commit failed!", null);
            }
            else {
                this.beginTransaction();
            }
        }
        
        // avoid deleting all contacts if no object was imported
        if (this.doDelete && !CollectionUtil.isEmptyOrNull(aTheList)) {
            Collection theAll = new HashSet(ContactFactory.getInstance().getAllContacts(ContactFactory.COMPANY_TYPE));
            theAll.removeAll(this.ccImported); // remove all imported contacts
            
            int commitCount = 0;
            for (Iterator theIter = theAll.iterator(); theIter.hasNext(); ) {
                CompanyContact theContact = (CompanyContact) theIter.next();
                commitCount++;
                
                // delete only contacts from actual mandator
                if (!Utils.equals(theContact.getValue(COSCompanyContact.ATTRIBUTE_MANDATOR), aMandator)) {
                    continue;
                }

                // delete only contacts with sap numbers
                if (StringServices.isEmpty(theContact.getForeignKey2())) {
                    continue;
                }
                
                try {
                    if (this.shouldDelete(theContact)) {
                        this.deleteCompanyContact(aMandator, theContact);
                    }
                    else {
                        if (this.deleteMark != null) {
                            if (! Utils.isTrue(theContact.getBoolean(this.deleteMark))) {
                                theContact.setValue(this.deleteMark, Boolean.TRUE); // mark contact as deleted
                                logInfo("Set deletion mark to " + printContact(theContact), null);
                            }
                        }
                    }
                    
                    if (this.doCommit && (commitCount%this.commitInterval == 0)) {
                        if (! this.commitTransaction()) {
                            logWarn("Commit failed for deleting " + printContact(theContact), null);
                        }
                        else {
                            this.beginTransaction();
                        }
                    }
                } catch (Exception ex) {
                    logError("Unable to delete company " + printContact(theContact), ex);
                }
            }
        }
        
        // last commit
        if (this.doCommit) {
            if (this.inTransaction() && ! this.commitTransaction()) {
                logWarn("Final commit failed!", null);
            }
        }
        
        if (this.doDeleteFile) {
            this.deleteFile(aMandator);
        }
        
        logInfo("-------------------------", null);
        logInfo("Importer Result:", null);
        logInfo("Created: " + this.countCreated, null);
        logInfo("Updated: " + (this.countUpdated - this.countCreated), null);
        logInfo("Deleted: " + this.countDeleted, null);
    }
    
    /**
     * Clean up caches, reset counters, etc.
     * Run garbage collection!
     */
    @Override
	protected void tearDownImport() {
        super.tearDownImport();
        
        this.kBase      = null;
        this.factory    = null;
        
        // cleanup caches
        this.ccCache    = null;
        this.ccImported = null;
        
        // reset counter
        this.countCreated = 0;
        this.countUpdated = 0;
        this.countDeleted = 0;
        
        // cleanup memory
        System.gc();
    }
    
    /**
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#getImportObjects(com.top_logic.element.structured.wrap.Mandator)
     */
    @Override
	protected List getImportObjects(Mandator aMandator) throws DatabaseAccessException,
            DataObjectException {
        return this.readFile(aMandator);
    }

    /**
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#importItem(com.top_logic.dob.DataObject, com.top_logic.element.structured.wrap.Mandator)
     */
    @Override
	protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
        
        String theFKey = this.getFKey(aDo);
        
        CompanyContact theContact = this.getFromCache(aMandator, theFKey);
        
        if (this.doCreate && theContact == null && this.shouldCreate(aDo)) {
            theContact = this.createCompanyContact(aMandator, aDo, theFKey);
        }
        else if (this.doUpdate && shouldUpdate(theContact, aDo)) {
            this.updateCompanyContact(aMandator, theContact, aDo, theFKey);
        }
        
        if (this.doCommit && (currentSize % this.commitInterval == 0)) {
            if (! this.commitTransaction()) {
                logWarn("Commit failed for " + printContact(theContact), null);
            }
            else {
                this.beginTransaction();
            }
        }
    }

    /**
     * Update valid wrappers only
     */
    protected boolean shouldUpdate(CompanyContact aContact, DataObject aDo) throws Exception {
        return aContact != null && aContact.tValid();
    }
    
    /**
     * Create contacts
     */
    protected boolean shouldCreate(DataObject aDo) throws Exception {
        return true;
    }
    
    /**
     * Delete contacts that are not used anymore
     */
    protected boolean shouldDelete(CompanyContact aContact) throws Exception {
		return aContact.tValid() && !WrapperMetaAttributeUtil.hasWrappersWithValue(aContact);
    }
    
    /**
     * Delete a contact
     */
    protected boolean deleteCompanyContact(Mandator aMandator, CompanyContact aContact) throws Exception {
        logInfo("Deleted " + printContact(aContact), null);
        this.countDeleted++;
		aContact.tDelete();
		return true;
    }
    
    /**
     * Update a contact with values from the data object and
     * mark the contact as imported.
     */
    protected void updateCompanyContact(Mandator aMandator, CompanyContact aContact, DataObject aDo, Object aFKey) throws Exception {
        
		TLClass type = aContact.tType();
        
        boolean isUpdate = false; // only for count and logger out.
        String [] theAttrs = aDo.getAttributeNames();
        for (int i=0; i<theAttrs.length; i++) {
            
            // omit remarks attribute since it is set in postUpdateCompanyContact;
            if (CompanyContact.REMARKS_ATTRIBUTE.equals(theAttrs[i])) {
                continue;
            }
            
			if (type.getPart(theAttrs[i]) != null) {
                Object theValue = this.getValue(aDo, theAttrs[i]);
                if (! Utils.equals(theValue, aContact.getValue(theAttrs[i]))) {
                    aContact.setValue(theAttrs[i], theValue);
                    isUpdate = true;
                }
            }
        }
        
        isUpdate |= this.postUpdateCompanyContact(aMandator, aContact, aDo, aFKey);
        
        if (this.deleteMark != null) { // mark contact as not deleted
            aContact.setValue(this.deleteMark, Boolean.FALSE);
        }
        
        this.ccImported.add(aContact);
        if (isUpdate) {
            this.countUpdated++;
            logInfo("Updated " + printContact(aContact), null);
        }
    }
    
        /**
     * Update a contact with some addtional values
     */
    protected boolean postUpdateCompanyContact(Mandator aMandator, CompanyContact aContact, DataObject aDo, Object aFKey) throws Exception {
        String thePOBox    = (String) this.getValue(aDo, "pfach");
        String thePOBoxZIP = (String) this.getValue(aDo, "plzfach");
        String theName2    = (String) this.getValue(aDo, "name2");

        String theRemark   = "name2:" + theName2 + ' '
                           + "pfach:" + thePOBoxZIP + ' ' + thePOBox;

        String theAdd =(String) this.getValue(aDo,CompanyContact.REMARKS_ATTRIBUTE);
        if (! StringServices.isEmpty(theAdd)) {
            theRemark += " remarks: " + theAdd;
        }

        if (! Utils.equals(theRemark, aContact.getValue(CompanyContact.REMARKS_ATTRIBUTE))) {
            aContact.setValue(CompanyContact.REMARKS_ATTRIBUTE, theRemark);
            return true;
        }

        return false;
    }
    
    /**
     * Create a contact and update it afterwards.
     * Values will be set in {@link #updateCompanyContact(Mandator, CompanyContact, DataObject, Object)}
     */
    protected CompanyContact createCompanyContact(Mandator aMandator, DataObject aDo, String aFKey) throws Exception {
        
        String         theName    = (String) this.getValue(aDo, CompanyContact.NAME_ATTRIBUTE);
        CompanyContact theContact = factory.createCompany(theName, this.getAddress(aDo), aMandator);
        theContact.setValue(COSCompanyContact.SUPPLIER, Boolean.TRUE);
        theContact.setForeignKey2(aFKey);

        logInfo("Created " + printContact(theContact), null);
        this.addToCache(theContact);
        this.countCreated++;
        
        this.updateCompanyContact(aMandator, theContact, aDo, aFKey);
        
        return theContact;
    }
    
    /**
     * Extract an AddressHolder with values from a data object
     */
    protected AddressHolder getAddress(DataObject aDo) throws Exception {
        AddressHolder theAddress = new AddressHolder();

        String theStreet  = (String) this.getValue(aDo, AddressHolder.STREET);
        String theZip     = (String) this.getValue(aDo, AddressHolder.ZIP_CODE);
        String theCity    = (String) this.getValue(aDo, AddressHolder.CITY);
        Country theCountry = (Country) this.getValue(aDo, AddressHolder.COUNTRY);
        
        if (theStreet != null) {
            theAddress.setProperty(AddressHolder.STREET, theStreet);
        }
        if (theZip != null) {
            theAddress.setProperty(AddressHolder.ZIP_CODE, theZip);
        }
        if (theCity != null) {
            theAddress.setProperty(AddressHolder.CITY, theCity);
        }
        if (theCountry != null) {
            theAddress.setProperty(AddressHolder.COUNTRY, theCountry.getCode());
        }
        return theAddress;
    }

    /**
     * Get a unique identifier for a contact.
     * This method must return an identifier that is equal  
     * to the identifier of {@link #getFKey(DataObject)}.
     */
    protected String getFKey(CompanyContact aContact) {
        return aContact.getString(CompanyContact.FKEY2_ATTRIBUTE);
    }
    
    /**
     * Get a unique identifier for a data object.
     * This method must return an identifier that is equal 
     * to the identifier of {@link #getFKey(CompanyContact)}.
     */
    protected String getFKey(DataObject aDo) throws Exception {
        return (String) this.getValue(aDo, CompanyContact.FKEY2_ATTRIBUTE);
    }
    
    protected final String getFilename(Mandator mandator, Date aDate) {
        
        String variable = mandator.getString(FILE_VARIABLE_PART);
        String constant = mandator.getString(FILE_CONSTANT_PART);

        if (StringServices.isEmpty(variable)) {
            return constant;
        }
        else {
			SimpleDateFormat theDateForm = CalendarUtil.newSimpleDateFormat(variable);
            String theDate = theDateForm.format(aDate);
            
            MessageFormat theMess = new MessageFormat(constant);
            
            return theMess.format(new Object[] {theDate});
        }
    }
    
    /**
     * Delete the file that was used one week ago.
     */
    protected final void deleteFile(Mandator mandator) {

        String theFilename = this.getFilename(mandator, DateUtil.addDays(new Date(), -7));
		File theFile = FileManager.getInstance().getIDEFile(theFilename);
		if (theFile.delete()) {
			logInfo("Deleted old data file: " + theFile.getName(), null);
        }
    }
    
    /**
     * Read the data file.
     * For each line this method creates a data object containing converted values.
     * The conversion method is configured for each attribute in {@link #mappingFunctions}.  
     * The attributes of the data object are configured in {@link #attributeNames}.
     */
    private List readFile(Mandator aMandator) throws DatabaseAccessException {
        
        String theFilename = this.getFilename(aMandator, DateUtil.addDays(new Date(), -1));
        
        try {
            List theList = new ArrayList(this.estimatedObjects);
            
			try (
					InputStream in = FileManager.getInstance().getStream(theFilename);
					BufferedReader theReader = new BufferedReader(new InputStreamReader(in))) {
				String theLine = null;
				int theLineCount = 0;
				while ((theLine = theReader.readLine()) != null) {
					try {
						Map theValues = new HashMap(this.attributeNames.length);

						int thePos = 0;
						int theLength = theLine.length();

						// fill all attributes at least with StringServices.EMPTY_STRING
						// this will prevent GenericDataObject.getAttributeValue() from
						// throwing silly NoSuchAttributeExceptions
						for (int i = 0; i < this.attributeNames.length; i++) {
							int theSize = this.columnSizes[i];
							String theAttr = this.attributeNames[i];

							String theValue = StringServices.EMPTY_STRING;
							if (thePos < theLength) { // avoid StringIndexOurOfBoundException
								theValue = theLine.substring(thePos,
									thePos + theSize <= theLength ? thePos + theSize : theLength);
								theValue = StringServices.checkOnNull(theValue).trim();
							}

							theValues.put(theAttr, theValue);
							thePos = thePos + theSize;
						}

						theList.add(new GenericDataObject(theValues, CompanyContact.META_ELEMENT, LongID
							.valueOf(theLineCount++)));
					} catch (Exception ex) {
						logError("Failed to read line nr. " + theLineCount + ": " + theLine, ex);
					}
				}

				return theList;
			}
            // just log error and return empty list
        } catch (FileNotFoundException fex) {
            this.logError(theFilename + " not found. Import aborted.", fex);
            return Collections.EMPTY_LIST;
        } catch (IOException iox) {
            this.logError(theFilename + " not found. Import aborted", iox);
            return Collections.EMPTY_LIST;
        }
    }
    
    protected final Object getValue(DataObject aDo, String anAttribute) throws NoSuchAttributeException {
        Object theValue = aDo.getAttributeValue(anAttribute);
        if (theValue instanceof String) {
            return this.convertValue(anAttribute, (String) theValue);
        }
        return theValue;
    }
    
    protected final Object convertValue(String anAttribute, String theValue) {
        
        Mapping theMapping = (Mapping) this.mappingFunctions.get(anAttribute);
        
        if (theMapping != null && theValue != null) {
            return theMapping.map(theValue);
        }
        
        return theValue;
    }
    
    @Override
	protected void logInfo(String aMessage, Throwable aAnException) {
        super.logInfo(aMessage, aAnException);
        //Logger.info(aMessage, aAnException, this.getClass());
    }
    
    @Override
	protected void logWarn(String aMessage, Throwable aAnException) {
        super.logWarn(aMessage, aAnException);
        //Logger.warn(aMessage, aAnException, this.getClass());
    }
    
    @Override
	protected void logError(String aMessage, Throwable aAnException) {
        super.logError(aMessage, aAnException);
        //Logger.error(aMessage, aAnException, this.getClass());
    }
    
    protected String printContact(CompanyContact aContact) {
        if (aContact.tValid()) {
            return aContact.getForeignKey2() + " " + aContact.getName(); 
        }
        else {
            return aContact.getName();
        }
    }
    
    public static class BooleanXMapping implements Mapping {
        @Override
		public Object map(Object aInput) {
            return "X".equalsIgnoreCase((String) aInput) ? Boolean.TRUE : Boolean.FALSE;
        }
    }
    
    public static class CountryMapping implements Mapping {
        @Override
		public Object map(Object aInput) {
            return new Country((String) aInput);
        }
    }
    
    public static class SAPKeyMapping implements Mapping {
        @Override
		public Object map(Object aInput) {
            return SAPFormatHelper.fillSAPNo((String) aInput);
        }
    }
}
