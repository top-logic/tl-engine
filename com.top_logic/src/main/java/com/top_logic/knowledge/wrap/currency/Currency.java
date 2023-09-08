/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.currency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

import com.top_logic.base.services.CurrencySystem;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBCache;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;

/**
 * Generic Handling of Currencies and ExchangeRates.
 *
 * We assume there is a System currency (EUR) and all other currencies
 * are related to this one. resulting in simple triangulations.
 *
 * Currencies are only expected in the default KnowledgeBase.
 *
 * Exchange rates are saved in a attributed {@link KnowledgeAssociation}
 * between to currencies. The Attributes are the date and the exchange rate.
 * Those are cached in a SortedMap to gain speed. 
 *
 * @author    <a href="mailto:fma@top-logic.com>fma</a>
 */
public class Currency extends UnitWrapper {
	
    /** Factor to return for system currencies and such. */
    public static final Double FACTOR1 = Double.valueOf(1.0);
   
    /** Name of underlying KnowledgeObject */
	public static final String OBJECT_NAME = "Currency";
	
    /** Name of (attributed) KA between Two currencies defining an Exchange rate */
    public static final String HAS_EXCHANGE="hasExchange"; 

    /** Attribute at HAS_CURRENCY KA describing when exchange rate was valid */
	static final String DATE = "date";

    /** Attribute at HAS_CURRENCY KA describing the actual exchange rate*/
    public static final String EXCHANGE_RATE = "exchangeRate";

    /** Default SystemCurrency when none is found in property */
	public static final String DEFAULT_SYSTEM_CURRENCY = CurrencySystem.DEFAULT_SYSTEM_CURRENCY;

    /** List of all Currencies ordered by ID */
	// private static List     allCurencies;
	
	private final KBCache<SortedMap<Object, KnowledgeAssociation>> _exchanges;

	/**
	 * Construct an instance wrapped around the specified
	 * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
	 *
	 * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
	 * CONSTRUCTOR!</b> Use always the getInstance() method of the wrappers.
	 *  
	 * @param    ko    The KnowledgeObject, must never be <code>null</code>.
	 * 
	 * @throws   NullPointerException  If the KO is <code>null</code>.
	 */
	public Currency(KnowledgeObject ko) {
		super(ko);
		_exchanges = new ExchangeRateCache(ko);
	}
	
    /**
	 * Return a {@link Map} containing the {@link #HAS_EXCHANGE} association with the date as key.
	 * 
	 * <p>
	 * The returned value is not updated when a new association is created or dropped. Method must
	 * be called again to get current result.
	 * </p>
	 * 
	 * @return <code>null</code> for SystemCurrency.
	 */
	public SortedMap<Object, KnowledgeAssociation> getExchangeRateKAs() {
		if (getSystemCurrency() == this) {
			return null;
		}
		return _exchanges.getValue();
	}

	/** 
	 * Return the (per date) most recent exchange rate.
	 */
	@Override
	public double getConversionFactor() {
        try {
			SortedMap<Object, KnowledgeAssociation> theRates = getExchangeRateKAs();
            if (theRates != null && !theRates.isEmpty()) { // SystemCurrency ?
				KnowledgeAssociation theKA = theRates.get(theRates.lastKey());
                if (theKA != null)
                    return ((Number) theKA.getAttributeValue(EXCHANGE_RATE)).doubleValue();
            }
        } catch (Exception exp) {
            Logger.error("Failed to  getExchangeRateKAs() using default", exp, Currency.class);
        }
        // else static factor, OK for EUR currencies 
        double result = super.getConversionFactor();
        if (isBaseUnit() && this != getSystemCurrency()) {
            Logger.info("Using default exchange rate of " + result + " for " + getName(), Currency.class);
        }
        return result;
	}

    /** 
     * Return the (per date) most exchange rate for given date, or nearest before.
     */
    public double getConversionFactor(Date atDate) {
        try {
			SortedMap<Object, KnowledgeAssociation> theRates = getExchangeRateKAs();
            if (theRates != null) {
				SortedMap<Object, KnowledgeAssociation> beforeRates = theRates.headMap(atDate);
                if (!beforeRates.isEmpty()) { 
                    Object lastKey = beforeRates.lastKey();
					KnowledgeAssociation theKA = theRates.get(lastKey);
                    return ((Number) theKA.getAttributeValue(EXCHANGE_RATE)).doubleValue();
                }
                // else try exact date
				KnowledgeAssociation theKA = theRates.get(atDate);
                if (theKA != null)
                    return ((Number) theKA.getAttributeValue(EXCHANGE_RATE)).doubleValue();
            }
        } catch (Exception exp) {
            Logger.error("Failed to  getExchangeRateKAs() using default", exp, Currency.class);
        }
        // else static factor, OK for EUR currencies 
        double result = super.getConversionFactor();
        if (isBaseUnit() && this != getSystemCurrency()) {
            Logger.warn("Using default exchange rate of " + result + " for " + getName() , Currency.class);
        }
        return result;
    }

    /**
	 * Creates a exchange ratio for this currency and the given date.
     * 
     * In case such a ratio already exists it will be overridden.
     * 
	 * @param aDate the date to create an exchange ratio for.
     *        Should be aligned to Midnight of a Day
	 * @param aRate the value of the exchange ratio to create
	 */
	public void saveExchangeRatio(Date aDate, Double aRate) {
        Currency system = getSystemCurrency();
		if(system == this) 
			throw new IllegalArgumentException("Must not saveExchangeRatio for SystemCurrency");
        
		{
			KnowledgeAssociation knownKA = getExchangeRateKAs().get(aDate);
			if (knownKA != null) {
				knownKA.setAttributeValue(EXCHANGE_RATE, aRate);
			} else {
				addExchangeRatio(aDate, aRate, system);
            }
        }
	}

	private void addExchangeRatio(Date date, Double rate, Currency system) throws DataObjectException {
		KnowledgeAssociation ka =
			this.getKnowledgeBase().createAssociation(tHandle(), system.tHandle(), HAS_EXCHANGE);
		ka.setAttributeValue(DATE, date);
		ka.setAttributeValue(EXCHANGE_RATE, rate);
	}
	    
    /**
     * Return currency for given identifier (ISO-CODE) or null, if no such currency exists
     */
	public static Currency getCurrencyInstance(String name) {
		return getCurrencyInstance(PersistencyLayer.getKnowledgeBase(), name);
    }
    
    /**
     * Return currency for given identifier (ISO-CODE) or null, if no such currency exists
     */
	public static Currency getCurrencyInstance(KnowledgeBase aKbase, String name) {
		final DataObject item = aKbase.getObjectByAttribute(OBJECT_NAME, NAME_ATTRIBUTE, name);
		return (Currency) WrapperFactory.getWrapper((KnowledgeObject) item);
    }

    /**
     * Creates a new currency.
     * 
     * @param aCode
     *        the code (identifier) of the currency to create
     * @param aSortOrder
     *        the sort order of the currency
     * @return the new created currency
     * @throws IllegalArgumentException
     *         if a currency with the given code already exists or the given code is invalid
     */
	public static Currency createCurrency(String aCode, int aSortOrder) {
        return CurrencySystem.Module.INSTANCE.getImplementationInstance().createNewCurrency(aCode, aSortOrder);
    }

	/**
     * a List with all Currencies of the system
     */
	public static List<Currency> getAllCurrencies() {
		Collection<KnowledgeObject> allKOs = getDefaultKnowledgeBase().getAllKnowledgeObjects(OBJECT_NAME);

		List<Currency> allCurr = new ArrayList<>(allKOs.size());
		for (KnowledgeObject ko : allKOs) {
			allCurr.add((Currency) WrapperFactory.getWrapper(ko));
        }
        Collections.sort(allCurr, CurrencyComparator.INSTANCE);
        return allCurr;
    }
        
    /** 
     * a List with all Units am Currencies of the system and a leading null.
     */
    public static List getUnitsAndCurrencies() {
        List allList = getAllUnits();
        allList.add(0, null); // THis will confuse modern SelectFields
        allList.addAll(getAllCurrencies());
        return allList;
    }

    /** 
     * a List with all Units am Currencies of the system.
     */
    public static List getUnitsCurrencies() {
        List allList = getAllUnits();
        allList.addAll(getAllCurrencies());
        return allList;
    }

    /**
     * This method returns if a currency is removable. A currency is removable
     * if it not defined in a xml-file and if it has no incoming associations.
     * 
     * @param aCurrency A currency.
     * @return Returns <i>true</i> if the given currency is removable, false otherwise.
     */
    public static boolean isRemovableCurrency(Currency aCurrency){
        Properties theProps   = Configuration.getConfiguration(Currency.class).getProperties();
		String currencyID = theProps.getProperty(aCurrency.getName());
        if(!StringServices.isEmpty(currencyID)){
            return false;
        }
		{
            Iterator it = aCurrency.tHandle().getIncomingAssociations();
            return !(it != null && it.hasNext());
        }
    }

    /** 
     * Returns the domestic currency.
     */
    public static Currency getSystemCurrency(){
    	return CurrencySystem.Module.INSTANCE.getImplementationInstance().getSystemCurrency();
    }
    
    /** 
     * Return a Currency this one is based on.
     * 
     * @return null for base Currencies (isBaseUnit())
     */
    public Currency getBaseCurrency() {
		return (Currency) super.getBaseUnit();
    }
    
}
