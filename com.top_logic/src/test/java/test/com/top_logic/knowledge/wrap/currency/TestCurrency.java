/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.currency;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.CurrencySystem;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.knowledge.wrap.currency.CurrencyComparator;

/**
 * Testcase for {@link Currency}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
@SuppressWarnings("javadoc")
public class TestCurrency extends BasicTestCase {

	private List<Currency> _exchangeRateCleanupItems = new ArrayList<>();
   
    /**
     * Constructor for TestCurrency.
     * 
     * @param aName name of the test to execute
     */
    public TestCurrency(String aName) {
        super(aName);
    }
    
	@Override
	protected void tearDown() throws Exception {
		for (Currency currency : _exchangeRateCleanupItems) {
			dropExchanges(currency);
		}
		super.tearDown();
	}

    /** 
     * EUR is the SystemCurrency and has some special abilities.
     */
    public void testEuro() throws Exception {
        KnowledgeBase theKBase = KBSetup.getKnowledgeBase();
		final Currency eur = Currency.getCurrencyInstance(theKBase, "EUR");
        
		assertEquals("EUR", eur.getName());
        assertEquals(1.0   , eur.getConversionFactor(), EPSILON);
        assertNull  (eur.getExchangeRateKAs());
        assertSame  (Currency.getSystemCurrency(), eur);

        assertTrue   (          eur.isBaseUnit());
        assertNull   (          eur.getBaseUnit());
        assertNotNull(          eur.getFormat());
        assertNotNull(          eur.getFormat(Locale.KOREAN));

		executeInLocale(Locale.GERMANY, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("42,00", eur.format(Integer.valueOf(42)));
				assertEquals("12,34", eur.format(Double.valueOf(12.34)));
				assertEquals("2.147.483.647,00", eur.format(Integer.valueOf(Integer.MAX_VALUE)));
				assertEquals("-2.147.483.648,00", eur.format(Integer.valueOf(Integer.MIN_VALUE)));
				assertEquals(1, eur.parse("1").intValue());
				assertEquals(1234, eur.parse("1.234").intValue());
				assertEquals(1.234, eur.parse("1,234").doubleValue(), EPSILON);
			}
		});
		executeInLocale(Locale.US, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("17.00", eur.format(Integer.valueOf(17)));
				assertEquals("0.99", eur.format(Double.valueOf(0.99)));
				assertEquals(1.234, eur.parse("1.234").doubleValue(), EPSILON);
				assertEquals(1234, eur.parse("1,234").intValue());
			}
		});
    }

	public void testUpdateExchangeRateCache() {
		KnowledgeBase theKBase = KBSetup.getKnowledgeBase();
		Currency dem = Currency.getCurrencyInstance(theKBase, "DEM");
		// call getExchangeRateKAs() to ensure cache is filled.
		assertEquals(0, dem.getExchangeRateKAs().size());
		Currency brk = Currency.getCurrencyInstance(theKBase, "BRK$");
		// call getExchangeRateKAs() to ensure cache is filled.
		assertEquals(0, brk.getExchangeRateKAs().size());
		saveExchangeRatio(brk, new Date(), Double.valueOf(0.9462));

		assertEquals(1, brk.getExchangeRateKAs().size());
		assertEquals(0, dem.getExchangeRateKAs().size());
	}

	private void saveExchangeRatio(Currency currency, Date date, Double value) {
		currency.saveExchangeRatio(date, value);
		_exchangeRateCleanupItems.add(currency);
	}

    /** 
     * DEM today is just seen as EUR
     */
    public void testDEM() throws Exception {
        KnowledgeBase theKBase = KBSetup.getKnowledgeBase();
		final Currency dem = Currency.getCurrencyInstance(theKBase, "DEM");
        
		assertEquals("DEM", dem.getName());
        assertEquals(0.5112918812   , dem.getConversionFactor(), EPSILON);
        assertTrue  (dem.getExchangeRateKAs().isEmpty());

        assertFalse  (                dem.isBaseUnit());
        assertNotNull("DEM has no BaseUnit", dem.getBaseUnit());
		assertEquals("EUR", dem.getBaseUnit().getName());
        assertNotNull(                dem.getFormat());
        assertNotNull(                dem.getFormat(Locale.GERMANY));

		executeInLocale(Locale.GERMANY, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("82,14", dem.format(Integer.valueOf(42)));
				assertEquals("24,13", dem.format(Double.valueOf(12.34)));
				assertEquals("4.200.112.941,28", dem.format(Integer.valueOf(Integer.MAX_VALUE)));
				assertEquals("-4.200.112.943,24", dem.format(Integer.valueOf(Integer.MIN_VALUE)));
				assertEquals(0.5112918812, dem.parse("1").doubleValue(), EPSILON);
				assertEquals(630, dem.parse("1.234").intValue());
				assertEquals(0.6309341814008, dem.parse("1,234").doubleValue(), EPSILON);
			}
		});
		executeInLocale(Locale.US, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("33.25", dem.format(Integer.valueOf(17)));
				assertEquals("1.94", dem.format(Double.valueOf(0.99)));
				assertEquals(0.6309341814008, dem.parse("1.234").doubleValue(), EPSILON);
				assertEquals(630, dem.parse("1,234").intValue());
			}
		});
    }

    public void testUSD() throws Exception {
        KnowledgeBase theKBase = KBSetup.getKnowledgeBase();
        Currency usd = Currency.getCurrencyInstance(theKBase,"USD");
        
		assertEquals("USD", usd.getName());
		assertEquals(TestAmount.USD_RATIO, usd.getConversionFactor(), EPSILON);

		assertFalse(usd.isBaseUnit());
		assertEquals("EUR", usd.getBaseUnit().getName());
        assertNotNull(         usd.getFormat());
        assertNotNull(         usd.getFormat(Locale.US));

		Transaction tx = theKBase.beginTransaction();
		usd.saveExchangeRatio(new Date(), Double.valueOf(0.9462));
		assertEquals(1, usd.getExchangeRateKAs().size());
        assertEquals(0.9462   , usd.getConversionFactor(), EPSILON);
		tx.rollback();
		assertEquals(0, usd.getExchangeRateKAs().size());
		assertEquals(TestAmount.USD_RATIO, usd.getConversionFactor(), EPSILON);
	}

    /** 
     * Test a partially broken currency.
     */
    public void testBroken() throws Exception {
        KnowledgeBase theKBase = KBSetup.getKnowledgeBase();
		final Currency broken = Currency.getCurrencyInstance(theKBase, "BRK$");
        
		assertEquals("BRK$", broken.getName());
        assertEquals(99.99      , broken.getConversionFactor(), EPSILON);

        assertTrue   (         broken.isBaseUnit());
        assertNull   (         broken.getBaseUnit());
        assertNotNull(         broken.getFormat());
        assertNotNull(         broken.getFormat(Locale.US));

		executeInLocale(Locale.GERMANY, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("42,00", broken.format(Integer.valueOf(42)));
				assertEquals("12,34", broken.format(Double.valueOf(12.34)));
				assertEquals("2.147.483.647,00", broken.format(Integer.valueOf(Integer.MAX_VALUE)));
				assertEquals("-2.147.483.648,00", broken.format(Integer.valueOf(Integer.MIN_VALUE)));
				assertEquals(1, broken.parse("1").intValue());
				assertEquals(1234, broken.parse("1.234").intValue());
				assertEquals(1.234, broken.parse("1,234").doubleValue(), EPSILON);
			}
		});
		executeInLocale(Locale.US, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("17.00", broken.format(Integer.valueOf(17)));
				assertEquals("0.99", broken.format(Double.valueOf(0.99)));
				assertEquals(1.234, broken.parse("1.234").doubleValue(), EPSILON);
				assertEquals(1234, broken.parse("1,234").intValue());
			}
		});
    }
    
    /** 
     * Test massive storage of conversion factors
     */
    public void testMassive() throws Exception {

        final int NUM_DATES  = 500;
        final int DIF_MILLIS = 1000 * 60 * 60 * 24; // one rate per day ...

        KnowledgeBase theKBase = KBSetup.getKnowledgeBase();
        Currency massive = Currency.getCurrencyInstance(theKBase,"BRK$");
		assertEquals(0, massive.getExchangeRateKAs().size());

		Transaction tx = theKBase.beginTransaction();
		long start = System.currentTimeMillis();
		long now = start;
		Random rand = new Random(0x9865423L);
		startTime();
		for (int i = 0; i < NUM_DATES; i++) {
			double rate = 0.5 + rand.nextGaussian();
			assertEquals(i, massive.getExchangeRateKAs().size());
			saveExchangeRatio(massive, new Date(now), Double.valueOf(rate));
			now += DIF_MILLIS;
			assertEquals(rate, massive.getConversionFactor(), EPSILON);
        }
		logTime("adding     " + NUM_DATES + " ExchangeRates");
		Logger.configureStdout("INFO"); // See why commit failed
		tx.commit();
		Logger.configureStdout();
		logTime("committing " + NUM_DATES + " ExchangeRates");
    }

	private void dropExchanges(Currency currency) {
		KnowledgeObject ko = currency.tHandle();
		try (Transaction cleanUpTX = ko.getKnowledgeBase().beginTransaction()) {
			KBUtils.deleteAllKI(ko.getOutgoingAssociations(Currency.HAS_EXCHANGE));
			cleanUpTX.commit();
		}
	}

	/**
	 * Tests #9174: Internal cache of exchanges is not updated when exchange is deleted.
	 */
	public void testChangeExchange() {
		KnowledgeBase theKBase = KBSetup.getKnowledgeBase();
		Currency currency = Currency.getCurrencyInstance(theKBase, "BRK$");
		assertEquals(0, currency.getExchangeRateKAs().size());
		saveExchangeRatio(currency, new Date(), Double.valueOf(0));
		assertEquals(1, currency.getExchangeRateKAs().size());
		Iterator<KnowledgeAssociation> outgoingAssociations =
			currency.tHandle().getOutgoingAssociations(Currency.HAS_EXCHANGE);
		assertTrue("Exchanges are stored via associations.", outgoingAssociations.hasNext());
		outgoingAssociations.next().delete();
		assertFalse("The sole exchange was deleted.", outgoingAssociations.hasNext());
		assertEquals("Ticket #9174: Exchange was deleted but cache not updated.", 0, currency.getExchangeRateKAs()
			.size());
	}

    /** Test all currencies, must be called first to commit the initial Currencies */
    public static void checkAllCurrencies() {
		List<Currency> allCur = Currency.getAllCurrencies();
        
		Set<String> names = new HashSet<>();
		for (Currency currency : allCur) {
			names.add(currency.getName());
		}
		assertEquals("Duplicate currencies.", names.size(), allCur.size());

		// in case commit fails uncomment this
		// Logger.configureStdout("INFO");
		assertSorted(allCur, CurrencyComparator.INSTANCE);

		List<?> all = Currency.getUnitsAndCurrencies();
		allCur.removeAll(all);
		assertTrue("Not all Currencies are contained in 'all currencies and units'.", allCur.isEmpty());
	}

    /**
     * Return the suite of tests to perform.
     */
    public static Test suite() {
        TestFactory startCurrencyModule = ServiceTestSetup.createStarterFactoryForModules(new TestFactory() {
			
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				return new TestCurrencySetup(suite);
			}
		}, CurrencySystem.Module.INSTANCE);
        
		return PersonManagerSetup.createPersonManagerSetup(TestCurrency.class, startCurrencyModule);
    }
    
	private static class TestCurrencySetup extends ThreadContextSetup {
    	
		public TestCurrencySetup(Test test) {
			super(test);
		}

		@Override
		protected void doSetUp() throws Exception {
			checkAllCurrencies();
		}

		@Override
		protected void doTearDown() throws Exception {
		}
    }

}
