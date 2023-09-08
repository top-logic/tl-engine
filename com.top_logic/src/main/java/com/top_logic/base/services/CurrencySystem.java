/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;

/**
 * The class {@link CurrencySystem} initializes and creates the currencies in a <i>TopLogic</i> system.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 * 
 */
@ServiceDependencies(PersistencyLayer.Module.class)
public class CurrencySystem extends ManagedClass {

	/** Name of Configuration property defining the main (System) Currency */
	private static final String SYSTEM_CURRENCY = "system-currency";

	/** Default SystemCurrency when none is found in property */
	public static final String DEFAULT_SYSTEM_CURRENCY = "EUR";

	/** The domestic currency used for conversion calculations. */
	private Currency systemCurrency;

	/**
	 * Configuration for currency system.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<CurrencySystem> {

		/**
		 * String representation of the system currency.
		 */
		@Name(SYSTEM_CURRENCY)
		@StringDefault(DEFAULT_SYSTEM_CURRENCY)
		String getSystemCurrency();

		/**
		 * All known currency codes.
		 */
		@ListBinding()
		List<String> getKnownCodes();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        {@link Config} for the {@link CurrencySystem}.
	 */
	public CurrencySystem(InstantiationContext context, Config config) {
		super(context, config);

		String systemCode = config.getSystemCurrency();

		KnowledgeBase theBase = PersistencyLayer.getKnowledgeBase();
		Set<String> knownCodes = getExistingCodes(theBase);

		Set<String> confCodes = new HashSet<>(config.getKnownCodes());
		confCodes.removeAll(knownCodes);

		List<Currency> createdCurrencies = Collections.emptyList();
		if (!confCodes.isEmpty()) {
			Transaction tx = theBase.beginTransaction();
			try {
				createdCurrencies = createCurrencies(theBase, confCodes);

				initSystemCurrency(systemCode);

				for (Currency currency : createdCurrencies) {
					currency.setBaseUnit(systemCurrency);
				}
			} finally {
				tx.commit();
			}
		} else {
			initSystemCurrency(systemCode);
		}
	}

	private void initSystemCurrency(String systemCode) {
		systemCurrency = Currency.getCurrencyInstance(systemCode);
	}

	/**
	 * fetch existing Currencies into allCurencies
	 * 
	 * Must be called in synchronized context from initCurrencies.
	 * 
	 * @return A Set of all found Currency IDs (codes)
	 */
	private Set<String> getExistingCodes(KnowledgeBase aKBase) {
		Collection<KnowledgeObject> allKOs = aKBase.getAllKnowledgeObjects(Currency.OBJECT_NAME);

		Set<String> knownCodes = new HashSet<>(allKOs.size());
		for (KnowledgeObject ko : allKOs) {
			Currency currency = WrapperFactory.getWrapper(ko);
			knownCodes.add(currency.getName());
		}
		return knownCodes;
	}

	/**
	 * System currency.
	 */
	public Currency getSystemCurrency() {
		return systemCurrency;
	}

	/**
	 * @param code
	 *        New currency code.
	 * @param sortOrder
	 *        Sorting order of the new currency.
	 * @return New created Currency.
	 */
	public Currency createNewCurrency(String code, int sortOrder) {
		if (code == null)
			throw new IllegalArgumentException("The given code is null.");
		if (!code.matches("[A-Z]{3}"))
			throw new IllegalArgumentException("The given code (" + code + ") is invalid. It must match the regular expression '[A-Z]{3}'.");
		Currency theCurrency = Currency.getCurrencyInstance(code);
		if (theCurrency != null)
			throw new IllegalArgumentException("Currency with the given code '" + code + "' already exists.");
		return createCurrency(KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase(), code, sortOrder);
	}

	/**
	 * Create a new Currency
	 * 
	 * @param aCode
	 *        the code (identifier) of the currency to create no consistency
	 *        checks are made
	 * 
	 * @throws IllegalArgumentException
	 *         when aCode is no valid ISO 4217 code.
	 * 
	 * @see java.util.Currency#getInstance(String)
	 */
	private Currency createCurrency(KnowledgeBase aBase, String aCode, int aSortOrder) {
		{
			KnowledgeObject ko = aBase.createKnowledgeObject(Currency.OBJECT_NAME);
			ko.setAttributeValue(UnitWrapper.NAME_ATTRIBUTE, aCode);
			ko.setAttributeValue(UnitWrapper.FACTOR, Currency.FACTOR1);
			ko.setAttributeValue(UnitWrapper.FORMAT, "###,###,###,##0.00");
			ko.setAttributeValue(UnitWrapper.SORT_ORDER, Integer.valueOf(aSortOrder));
			Currency theCurrency = (Currency) WrapperFactory.getWrapper(ko);
			return theCurrency;
		}
	}

	/**
	 * Create new Currencies for missing codes.
	 * 
	 * Must be called in synchronized context from initCurrencies.
	 * 
	 * @param aKBase
	 *        KnowledgeBase to create the currencies in.
	 * @param confCodes
	 *        Set of codes that does not exist yet.
	 * @return The created {@link Currency} instances.
	 */
	private List<Currency> createCurrencies(KnowledgeBase aKBase, Set<String> confCodes) {
		Iterator<String> iter = confCodes.iterator();
		int sortOrder = 2000;
		ArrayList<Currency> createdCurrencies = new ArrayList<>();
		while (iter.hasNext()) {
			String theCode = iter.next();
			Logger.debug("Creating default currency " + theCode, Currency.class);
			createdCurrencies.add(createCurrency(aKBase, theCode, sortOrder++));
		}
		return createdCurrencies;
	}

	/**
	 * Module for {@link CurrencySystem}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<CurrencySystem> {

		/**
		 * Module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<CurrencySystem> getImplementation() {
			return CurrencySystem.class;
		}
	}

}
