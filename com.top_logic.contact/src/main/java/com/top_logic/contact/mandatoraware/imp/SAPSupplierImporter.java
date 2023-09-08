/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.SAPFormatHelper;
import com.top_logic.contact.mandatoraware.layout.COSCompanyContactCreateHandler;
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
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.Utils;
import com.top_logic.util.monitor.MonitorMessage;

/**
 * Import SAP Data into {@link Mandator} objects.
 * 
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAPSupplierImporter extends DataObjectImportTask  {
    
   /** 
     * Names of Attributes we expect when importing.
     */ 
    protected String COLUMNS[] = { 
       "LIFNR", "ANRED", "LAND1", "NAME1", "NAME2", "ORT01", "PFACH", "PSTLZ", 
       "REGIO" , "STRAS", "BRSCH" , "TELF1" , "TELFX" , "ZZSPC_SPL" };

    /** 
     * The DatasourceName configured to import from.
     * 
     * When mandator is given then the name of the attribute
     * where such a datsource is found
     */
    protected String importFrom;
    
    /** The Name of the Attribute in the outer DO where the (DO-)List of Objects is found  */
    protected final String doListAttr;
    
    /** When true Company Contacts will be created instead of ignoring them */
    protected final boolean create;

    /** When true Company Contacts will be deleted if they are not used anymore */
    protected final boolean delete;

    /** The KnowledgeBase to import into */
    protected transient KnowledgeBase kBase;
    
    /** Used for ceration of Contacts */
    protected transient COSCompanyContactCreateHandler cFactory;
    
    /** Used to commit only every n-th record */ 
    protected int commit;
    
    /** Number of deleted contacts */
    protected int numDeleted;
    
    /** holds DataAccessProxies for each mandator. Lazy filled in getTableEntries(Mandator) */
    private Map<Mandator, DataAccessProxy> dapByMandator; 
    
    private Set<CompanyContact> importedCompanies;
    
    /** 
     * Create a new SAPSupplierImporter from (XML-) properties.
     * 
     * @param prop Properties to configure the task and 
     */
    public SAPSupplierImporter(Properties prop) {
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
        
        this.commit = Integer.parseInt(prop.getProperty("commitInterval", "50"));
        this.delete = StringServices.parseBoolean(prop.getProperty("delete", "false"));
        create = StringServices.parseBoolean(prop.getProperty("create"));
        finished = true;
        this.setRunOnStartup(false);
        
        this.dapByMandator = new HashMap<>();

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
        return this.getTableEntries(aMandator, this.doListAttr);
    }

    /**
     * Return a subtable from the big sap data table
     */
    protected final List getTableEntries(Mandator aMandator, String aTable) throws DatabaseAccessException, DataObjectException {
        return (DOList) this.getDAP(aMandator).getObjectEntry().getAttributeValue(aTable);
    }

    protected final DataAccessProxy getDAP(Mandator aMandator) throws DatabaseAccessException, DataObjectException {
        DataAccessProxy theDAP = this.dapByMandator.get(aMandator);
        if (theDAP == null) {
            theDAP = new DataAccessProxy(getImportDSN(aMandator));
            this.dapByMandator.put(aMandator, theDAP);
        }
        return theDAP;
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
        kBase     = COSCompanyContact.getDefaultKnowledgeBase();
        cFactory  = (COSCompanyContactCreateHandler) CommandHandlerFactory.getInstance().getHandler(COSCompanyContactCreateHandler.COMMAND_ID);
        this.dapByMandator = new HashMap<>();
        this.importedCompanies = new HashSet<>(10000);
        this.numDeleted = 0;
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
     * Import on whole mandator structure
     * 
     * @see com.top_logic.contact.mandatoraware.imp.DataObjectImportTask#startImport()
     */
    @Override
	protected void startImport() {
    	this.startImport(Mandator.getRootMandator(), true);
    }
    
    /** 
     * Import a single DataObject.
     */
    @Override
	protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
        String keyCol     = COLUMNS[0];
        String key        = (String) aDo.getAttributeValue(keyCol);
               key        = SAPFormatHelper.fillSAPNo(key);
        String theMessage = keyCol + ": '" + key + '\'';

    	List theList = COSCompanyContact.getListBySAP(ContactFactory.COMPANY_TYPE, key, aMandator);
    	COSCompanyContact contact = null;
    	if (theList != null && !theList.isEmpty()) {
    		contact = (COSCompanyContact) theList.get(0);
    	}
        
        if (contact == null && !create) {
            numberWARN ++;
            logWarn("No CompanyContact for " + theMessage + " found", null);
            return;
        }
        String anred = (String) aDo.getAttributeValue(COLUMNS[ 1]);
        String land1 = (String) aDo.getAttributeValue(COLUMNS[ 2]);
        String name1 = (String) aDo.getAttributeValue(COLUMNS[ 3]);
        String name2 = (String) aDo.getAttributeValue(COLUMNS[ 4]);
        String ort01 = (String) aDo.getAttributeValue(COLUMNS[ 5]);
        String pfach = (String) aDo.getAttributeValue(COLUMNS[ 6]);
        String pstlz = (String) aDo.getAttributeValue(COLUMNS[ 7]);
        String regio = fixEmpty((String) aDo.getAttributeValue(COLUMNS[ 8]));
        String stras = (String) aDo.getAttributeValue(COLUMNS[ 9]);
        String brsch = (String) aDo.getAttributeValue(COLUMNS[10]);
        String telf1 = fixEmpty((String) aDo.getAttributeValue(COLUMNS[11]));
        String telfx = (String) aDo.getAttributeValue(COLUMNS[12]);
        String zzspc_spl = fixEmpty(
                     (String) aDo.getAttributeValue(COLUMNS[13]));
        // zzspc_sp is an customer specific add-on table and used
        // for CAGE (Commercial And Government Entity-Code)
        
		theMessage += ", Name: " + name1 + " (" + getName() + ')';

        // put unsupported values in remarks ...
        String rmrks = "anred:" + anred + ' '
                     + "name2:" + name2 + ' '
                     + "pfach:" + pfach + ' '
                     + "telfx:" + telfx; 
        
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

            contact = (COSCompanyContact) cFactory.createCompany(name1,aho,aMandator);

            contact.setForeignKey2(key);
            contact.setValue(COSCompanyContact.SUPPLIER          , Boolean.TRUE);
            
            changed = true;
            this.importedCompanies.add(contact);
            logInfo("Created new CompanyContact " + theMessage, null);
        } 
        else { // Update ?
            this.importedCompanies.add(contact);
            aho = contact.getAddress();
            changed = !Utils.equals(name1, contact.getName())
                   || !Utils.equals(rmrks, contact.getRemarks())
                   || !Utils.equals(brsch    , contact.getValue(COSCompanyContact.SECTOR_ATTRIBUTE))
                   || !Utils.equals(zzspc_spl, contact.getValue(COSCompanyContact.ATTRIBUTE_CAGE))
                   || !Utils.equals(stras, aho.getProperty(AddressHolder.STREET))
                   || !Utils.equals(ort01, aho.getProperty(AddressHolder.CITY))
                   || !Utils.equals(land1, aho.getProperty(AddressHolder.COUNTRY))
                   || !Utils.equals(regio, aho.getProperty(AddressHolder.STATE))
                   || !Utils.equals(pstlz, aho.getProperty(AddressHolder.ZIP_CODE))
                   || !Utils.equals(telf1, aho.getProperty(AddressHolder.PHONE));

            if (changed) {
               logInfo("Modifying CompanyContact " + theMessage, null);
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
        if (changed) { // includes created ...
            contact.setRemarks(rmrks);
            contact.setValue  (COSCompanyContact.SECTOR_ATTRIBUTE        , brsch);
            contact.setValue  (COSCompanyContact.ATTRIBUTE_CAGE, zzspc_spl);

            
            if (currentSize % this.commit == 0) {
                if (!kBase.commit()) {
                    logError("Failed to commit around " + theMessage, null);
                }
            }
            numberOK ++;
        }
    }
    
    @Override
	protected void importFinished(List theList, Mandator mandator) {
        super.importFinished(theList, mandator);
        if (! this.kBase.commit()) {
            logError("Failed to commit in importFinished()", null);
        }

        // run cleanup only if it is configured
        // and if there were imported objects (this check is an additional heuristic to guarantee not to delete the whole contact data)
        if (this.delete && ! CollectionUtil.isEmptyOrNull(this.importedCompanies)) {
            
            logInfo("Deleting unused companies", null);
            
            List allCompanies = ContactFactory.getInstance().getAllContacts(ContactFactory.COMPANY_TYPE);
            
            // only delete companies that were not imported
            allCompanies.removeAll(this.importedCompanies);

            Transaction t = this.kBase.beginTransaction();
            try {
				for (int i = 0; i < allCompanies.size(); i++) {
					CompanyContact company = (CompanyContact) allCompanies.get(i);
					String theName = company.getName();
					String theNum = StringServices.nonNull(company.getForeignKey2());

					{
						if (shouldDelete(company, mandator)) {
							if (delete(company, mandator)) {
								numDeleted++;
							}
						}
					}

					if (i % this.commit == 0) {
						try {
							t.commit();
							t = this.kBase.beginTransaction();
						} catch (KnowledgeBaseException e) {
							logError("Failed to commit deleted contacts at " + i, e);
						}
					}
				}

				try {
					t.commit();
				} catch (KnowledgeBaseException e) {
					logError("Failed to commit last deleted contacts", e);
				}

			} finally {
				t.rollback();
            }
            
            logInfo("Deleted " + numDeleted + " contacts", null);
        }
    }

    /**
     * Check if this {@link CompanyContact} (which was not imported) should be deleted.
     * 
     * Default is to delete all {@link CompanyContact}s where {@link CompanyContact#getForeignKey2()} is not null.
     */
    protected boolean shouldDelete(CompanyContact company, Mandator mandator) {
        String theSAPNo = company.getForeignKey2();
        
        // do not delete companies that were created manually
        if (StringServices.isEmpty(theSAPNo)) {
            return false;
        }
        
        // do not delete companies of other mandators in this run
        if (! Utils.equals(company.getValue(COSCompanyContact.ATTRIBUTE_MANDATOR), mandator)) {
            return false;
        }
        
        return true;
    }

    /**
     * Delete a company contact
     */
    protected boolean delete(CompanyContact company, Mandator mandator) {
		company.tDelete();
		return true;
    }

    /** 
     * Overridden to clear the kBase.
     */
    @Override
	protected void tearDownImport() {
        if (!kBase.commit()) {
            logError("Failed to commit last entries", null);
        }
        super.tearDownImport();
        kBase    = null;
        cFactory = null;
        this.dapByMandator = null;
    }

	
	public static Boolean isInternalSupplier(String aSAPNo) {
	    return Boolean.valueOf(!StringServices.isEmpty(aSAPNo) && aSAPNo.indexOf("VERB") >= 0);
	}
	
	protected abstract class AbstractFunction {
        private final String name;
        private final String tablename;
        private boolean doCommit;
        
        public AbstractFunction(String aName, String aTablename, boolean needsCommit) {
            this.name      = aName;
            this.tablename = aTablename;
            this.doCommit  = needsCommit;
        }
        
        public String getName() {
            return (this.name);
        }
        
        public String getTablename() {
            return (this.tablename);
        }
        
        public void run(Mandator aMandator) {
            logInfo("Begin " + this, null);
            try {
                List theEntries = getTableEntries(aMandator, this.getTablename());
                for (int i = 0, cnt = theEntries.size(); i < cnt; i++) {
                    DataObject theDO = (DataObject) theEntries.get(i);
                    currentSize++;
                    try {
                    
                        this.eval(theDO, aMandator);
                        
                        if (this.doCommit && i % commit == 0) {
                            if (kBase.commit()) {
                                logInfo("Commiting " + this +" " + i , null);
                            }
                            else {
                                logError("Commiting " + this +" " + i + " failed.", null);
                            }
                        }
                        
                    } catch (Exception ex) {
                        logError("Unable to run function " + this.name + " on " + theDO, ex);
                    }
                }
                if (this.doCommit) {
                    if (kBase.commit()) {
                        logInfo("Commiting " + this , null);
                    }
                    else {
                        logError("Commiting " + this, null);
                    }
                }
                
            } catch (Exception ex) {
                logError("Unable to run function " + this , ex);
            }
            
            logInfo("Finished " + this, null);
        }
        
        protected abstract void eval(Object anObject, Mandator aMandator) throws Exception;
        
        @Override
		public String toString() {
            return this.name + "/" + this.tablename;
        }
    }
}
