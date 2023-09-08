/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.SimpleCSVTokenizer;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.CSVImporter;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.currency.Currency;

/**
 * Import SAP Volume data into existing COSCompanyContacts.
 * 
 * Not is use since we can access the SAP System directly.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAP_CSVVolumeImporter extends CSVImporter {

    /** Map of currencies, indexed by code */
    protected Map currencies;
    
    /** The Numberformt used to parse the input */
    protected NumberFormat valueFomat;
    
    /** The mandator. */
    protected Mandator mandator;
	
	/** the mandator aware flag. */
	protected boolean mandatorAware;
    
    /** date of the last import */
    private Date date;
	
    /** 
     * Create a new SAPVolumeImporter importing from given file.
     */
    public SAP_CSVVolumeImporter(File aImportSource, boolean aDeleteWhenDone, Mandator aMandator, boolean isMandatorAware) {
        super(aImportSource, aDeleteWhenDone);
        
        this.mandatorAware = isMandatorAware && aMandator != null;
        this.mandator      = isMandatorAware ? aMandator  : null;
    }
    
    public SAP_CSVVolumeImporter(File aImportSource, boolean aDeleteWhenDone, Mandator aMandator, Date aDate, boolean isMandatorAware) {
    	this(aImportSource, aDeleteWhenDone, aMandator, isMandatorAware);
    	this.date = aDate;
    }

    /** 
     * Overriden to create a SimpleCSVTokenizer with ';' as seperator.
     */
    @Override
	protected SimpleCSVTokenizer createTokenizer(String aFirstLine) {
        // No quotes here ...
        return new SimpleCSVTokenizer(aFirstLine, ';');
    }
    
    /** 
     * Check for "Liferant" and "Währung" but ignore year 
     * 
     * @see com.top_logic.contact.business.CSVImporter#checkColumnFormat(com.top_logic.basic.io.SimpleCSVTokenizer)
     */
    @Override
	protected boolean checkColumnFormat(SimpleCSVTokenizer aCvsToken)
            throws IOException {
        String token = aCvsToken.nextToken();
        if (!"Lieferant".equals(token)) {
            throw new IOException("Expected 'Lieferant' got '" + token + "' in first line, first column");
        }
        String year = aCvsToken.nextToken();
        token = aCvsToken.nextToken();
        if (!"Währung".equals(token)) {
            throw new IOException("Expected 'Währung' got '" + token + "' in first line, 3d column");
        }
        Logger.info("Importing year '" + year + "'", this);
        return true;
    }

    /** 
     * Overriden to set up the currencies Map.
     */
    @Override
	protected void setupImport() {
        super.setupImport();
        currencies = new HashMap();
            // This is a german number style, well
        valueFomat = NumberFormat.getInstance(Locale.GERMAN);
    }
    
    /** 
     * Import a Line like sapNr,value,currency into a COSCompanyContact.
     */
    @Override
	protected void importLine(String aLine, SimpleCSVTokenizer aCsvToken)
            throws Exception {
        
        KnowledgeBase kBase = AbstractContact.getDefaultKnowledgeBase();
        
        String sapNr    = aCsvToken.nextToken();
        String value    = aCsvToken.nextToken();
        String currency = aCsvToken.nextToken();
        
        Currency theCurrency = (Currency) currencies.get(currency);
        if (theCurrency == null) {
            theCurrency = Currency.getCurrencyInstance(currency);
        }
        if (theCurrency == null) {
            logWarn("Invalid Currency in line '" + aLine + "' ignored.", null);
            return;
        }
        
        sapNr = SAPSupplierRecord.getSAPNr(sapNr);
    	List theList = COSCompanyContact.getListBySAP(ContactFactory.COMPANY_TYPE, sapNr, this.mandator);
    	AbstractContact contact = null;
    	if (theList != null && !theList.isEmpty()) {
    		contact = (AbstractContact) theList.get(0);
    	}
        if (contact == null) {
            logWarn("No COSCompanyContact found for '" + sapNr + "' ignored", null);
            return;
        }
        
        COSCompanyContact ccContact = (COSCompanyContact) contact;
        
        Number nValue;
        try {
            nValue = valueFomat.parse(value);
        } catch (ParseException px) {
            logWarn("'" + value + "' is not a valid german number, ignored", px);
            return;
        }
        Double dValue; 
        if (nValue instanceof Double) {
            dValue = (Double) nValue;
        } else {
            dValue = Double.valueOf(nValue.doubleValue());
        }
        ccContact.setValue(COSCompanyContact.ATTRIBUTE_VOLUME  , dValue);
        ccContact.setValue(COSCompanyContact.ATTRIBUTE_CURRENCY, theCurrency);

        if (!kBase.commit()) {
            logError("Failed to commit() " + aLine, null);
        }
    }
    
    /** 
     * Overriden to clear the currencies.
     */
    @Override
	protected void tearDownImport() {
        currencies = null;
        valueFomat = null;
        super.tearDownImport();
    }
    
    @Override
	protected void beforeLastCommitHook() {
        super.beforeLastCommitHook();
        this.mandator.setValue("LAST_IMPORT_VOLUME", this.date);
    }

}
