/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.POIExcelImporter;
import com.top_logic.contact.mandatoraware.COSCompanyContact;
import com.top_logic.contact.mandatoraware.COSContactConstants;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.currency.Currency;

/**
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class XLSVolumeImporter extends POIExcelImporter {

	protected String COLUMNS[] = {"Lieferant", "Umsatz", "Währung"};
	protected String COLUMNS_EN[] = {"Supplier", "Volume", "Currency"};
	
	/**
	 * The mandator. 
	 */
	protected Mandator mandator;
	
	/** the mandator aware flag. */
	protected boolean mandatorAware;
	
	/**
	 * Map of currencies, indexed by code 
	 */
	protected Map currencies;
	/**
	 * Check if mandator uses SAP. 
	 */
	protected boolean isSAP;
	/**
	 * The Numberformat used to parse the input 
	 */
	protected NumberFormat valueFomat;

    /** date of the last import */
    private Date date;
	
	/** 
	 * Create a new XLSVolumeImporter
	 * 
	 * @param importSource		the XLS file
	 * @param aMandator			the mandator on which the volumes will be imported
	 */
	public XLSVolumeImporter(BinaryData importSource, Mandator aMandator, boolean isMandatorAware) {
		super(importSource);
		
        this.mandatorAware = isMandatorAware && aMandator != null;
        this.mandator      = isMandatorAware ? aMandator  : null;
		
        if (aMandator != null) {
            isSAP = !StringServices.isEmpty((String) aMandator.getValue(Mandator.SAP_SUPPLIERS));
        }
	}
	
	public XLSVolumeImporter(BinaryData importSource, Mandator aMandator, Date aDate, boolean isMandatorAware) {
		this(importSource, aMandator, isMandatorAware);
		this.date = aDate;
	}

	/** 
	 * @see com.top_logic.contact.business.POIExcelImporter#setupImport()
	 */
	@Override
	protected void setupImport() {
		super.setupImport();
		
        currencies = new HashMap();
        // This is a german number style, well
        valueFomat = NumberFormat.getInstance(Locale.GERMAN);
	}
	
	@Override
	protected boolean checkColumnFormat(int aSheetNum, String aSheetname, Row aRow) throws IOException {
		try {
	        for (short i=0; i < COLUMNS.length; i++) {
	            String   col   = COLUMNS[i];
				Cell cell = aRow.getCell(i);
	            String   found = cell.getStringCellValue();
	            if (!col.equals(found)) {
	                throw new IOException("Expected column '" + col + "' found '" + found + "'");
	            }
	        }
		}
		catch (IOException ex) {
			// Try english format
	        for (short i=0; i < COLUMNS_EN.length; i++) {
	            String   col   = COLUMNS_EN[i];
				Cell cell = aRow.getCell(i);
	            String   found = cell.getStringCellValue();
	            if (!col.equals(found)) {
	                throw new IOException("Expected column '" + col + "' found '" + found + "'");
	            }
	        }
		}
		
        return true;
	}

	@Override
	protected void importRow(int aSheetNum, String aSheetname, Row aRow) throws Exception {
        KnowledgeBase kBase = AbstractContact.getDefaultKnowledgeBase();
        
        short i=0;
        String sapNr    = fixAny(aRow.getCell(i++));
        double value    = aRow.getCell(i++).getNumericCellValue();
        String currency = fixAny(aRow.getCell(i++));
        
        Currency theCurrency = (Currency) currencies.get(currency);
        if (theCurrency == null && currency != null) {
            theCurrency = Currency.getCurrencyInstance(currency);
        }
        if (theCurrency == null) {
            logWarn("Invalid Currency '" + theCurrency + "' ignored.", null);
            return;
        }
        
        if (this.isSAP) {
        	sapNr = SAPSupplierRecord.getSAPNr(sapNr);
        }
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
        
//        Number nValue;
//        try {
//            nValue = valueFomat.parse(value);
//        } catch (ParseException px) {
//            logWarn("'" + value + "' is not a valid german number, ignored", px);
//            return;
//        }
        Double dValue=Double.valueOf(value); 
//        if (nValue instanceof Double) {
//            dValue = (Double) nValue;
//        } else {
//            dValue = Double.valueOf(nValue.doubleValue());
//        }
        ccContact.setValue(COSCompanyContact.ATTRIBUTE_VOLUME  , dValue);
        ccContact.setValue(COSCompanyContact.ATTRIBUTE_CURRENCY, theCurrency);

        if (!kBase.commit()) {
            logError("Failed to commit() " + ccContact, null);
        }
	}
	
	@Override
	protected void endSheetImport(int numSheet, String sheetName) {
	    super.endSheetImport(numSheet, sheetName);
	    
	    if (this.mandator != null) {
			try (Transaction t = this.mandator.getKnowledgeBase().beginTransaction()) {
                this.mandator.setValue(COSContactConstants.VOLUME_IMPORT_DATE, this.date);
                
                try {
                    t.commit();
                } catch (KnowledgeBaseException k) {
                    logError("Failed to commit import date", k);
                }
                
            }
	    }
	}

    /** 
     * Overriden to clear the currencies.
     * 
     * @see com.top_logic.contact.business.POIExcelImporter#tearDownImport()
     */
    @Override
	protected void tearDownImport() {
        currencies = null;
        valueFomat = null;
        super.tearDownImport();
    }
}
