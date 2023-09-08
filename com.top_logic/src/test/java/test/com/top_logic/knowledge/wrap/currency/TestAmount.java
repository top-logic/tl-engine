/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.currency;

import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.CurrencySystem;
import com.top_logic.knowledge.wrap.currency.Amount;
import com.top_logic.knowledge.wrap.currency.AmountLabelProvider;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.TLContext;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestAmount extends BasicTestCase {

    protected static final double EUR_RATIO = 1.0d;

    
    protected static final double TOLERANCE = 0.00001d;

	static final double USD_RATIO = 1.46778d;
    
    private static final double GBP_RATIO = 0.69749d;
    
    private static final double YEN_RATIO = 165.69794d;

	private Unit piece0;

	private Unit piece1;

	private Unit piece2;

	private Unit piece3;

	private Unit EUR;

	private Unit USD;

	private Unit GBP;

	private Unit YEN;

    private Amount oneEUR;

    private Amount oneUSD;

    private Amount oneGBP;

    private Amount oneYEN;

	private Amount thirteenThings;

	private Amount valueWithPecision1;

	private Amount valueWithPecision2;

	private Amount valueWithPecision3;

    /** 
     * Creates a {@link TestAmount}.
     * 
     * @param aName   Name of the test case.
     */
    public TestAmount(String aName) {
        super(aName);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();

		piece0 = UnitWrapper.getPiece0();
		piece1 = UnitWrapper.getPiece1();
		piece2 = UnitWrapper.getPiece2();
		piece3 = UnitWrapper.getPiece3();

		EUR = Currency.getInstance("EUR");
		USD = Currency.getInstance("USD");
		GBP = Currency.getInstance("GBP");
		YEN = Currency.getInstance("YEN");

		this.oneEUR = new Amount(1.0d, EUR);
		this.oneUSD = new Amount(1.0d, USD);
		this.oneGBP = new Amount(1.0d, GBP);
		this.oneYEN = new Amount(1.0d, YEN);

		this.thirteenThings = new Amount(12.6666d, piece0);
		this.valueWithPecision1 = new Amount(12.6666d, piece1);
		this.valueWithPecision2 = new Amount(12.6666d, piece2);
		this.valueWithPecision3 = new Amount(12.6666d, piece3);
    }

	/**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        this.oneYEN = null;
        this.oneGBP = null;
        this.oneUSD = null;
        this.oneEUR = null;

        super.tearDown();
    }

    /**
     * Test method for {@link com.top_logic.knowledge.wrap.currency.Amount#getValue()}.
     */
    public void testGetValue() {
        assertEquals(EUR_RATIO, this.oneEUR.getValue(), 0.0d);
        assertEquals(EUR_RATIO, this.oneEUR.getValue(EUR), 0.0d);

        assertEquals(1.0d, this.oneUSD.getValue(), 0.0d);
        assertEquals(USD_RATIO, this.oneUSD.getValue(EUR), TOLERANCE);
        assertEquals(1 / USD_RATIO, this.oneEUR.getValue(USD), TOLERANCE);

        assertEquals(1.0d, this.oneGBP.getValue(), 0.0d);
        assertEquals(GBP_RATIO, this.oneGBP.getValue(EUR), TOLERANCE);
        assertEquals(1 / GBP_RATIO, this.oneEUR.getValue(GBP), TOLERANCE);

        assertEquals(1.0d, this.oneYEN.getValue(), 0.0d);
        assertEquals(YEN_RATIO, this.oneYEN.getValue(EUR), TOLERANCE);
        assertEquals(1 / YEN_RATIO, this.oneEUR.getValue(YEN), TOLERANCE);

        assertEquals(GBP_RATIO / USD_RATIO, this.oneGBP.getValue(USD), TOLERANCE);
        assertEquals(GBP_RATIO / YEN_RATIO, this.oneGBP.getValue(YEN), TOLERANCE * 100);

        assertEquals(USD_RATIO / GBP_RATIO, this.oneUSD.getValue(GBP), TOLERANCE);
        assertEquals(USD_RATIO / YEN_RATIO, this.oneUSD.getValue(YEN), TOLERANCE * 100);

        assertEquals(YEN_RATIO / GBP_RATIO, this.oneYEN.getValue(GBP), TOLERANCE);
        assertEquals(YEN_RATIO / USD_RATIO, this.oneYEN.getValue(USD), TOLERANCE);
    }

    /**
     * Test method for {@link com.top_logic.knowledge.wrap.currency.Amount#getUnit()}.
     */
    public void testGetUnit() {
        assertSame(EUR, this.oneEUR.getUnit());
        assertSame(GBP, this.oneGBP.getUnit());
        assertSame(USD, this.oneUSD.getUnit());
        assertSame(YEN, this.oneYEN.getUnit());
    }

	public void testUnitPiece() {
		assertEquals(12.6666, this.thirteenThings.getValue(), 0.0);
		assertEquals(12.6666, this.thirteenThings.getValue(piece0), 0.0d);
		assertEquals(12.6666, this.thirteenThings.getValue(piece1), 0.0d);
		assertSame(piece0, this.thirteenThings.getUnit());
	}

	public void testPieceFormat() {
		TLContext.getContext().setCurrentLocale(Locale.GERMAN);

		assertEquals("13", AmountLabelProvider.INSTANCE.getLabel(thirteenThings));
		assertEquals("12,7", AmountLabelProvider.INSTANCE.getLabel(valueWithPecision1));
		assertEquals("12,67", AmountLabelProvider.INSTANCE.getLabel(valueWithPecision2));
		assertEquals("12,667", AmountLabelProvider.INSTANCE.getLabel(valueWithPecision3));
	}

    public void testExceptionForNullUnit() {
    	try {
			Amount amount = new Amount(1, null);
			fail("Null Unit should throw an exception.");
		} catch (IllegalArgumentException ex) {
			assertEquals("Given unit is null", ex.getMessage());
		}
    }

    /**
     * Test method for {@link com.top_logic.knowledge.wrap.currency.Amount#add(com.top_logic.knowledge.wrap.currency.Amount)}.
     */
    public void testAdd() {
        Amount twoEUR = this.oneEUR.add(this.oneEUR);

        assertEquals(1.0d, this.oneEUR.getValue(), 0.0d);
        assertEquals(2.0d, twoEUR.getValue(), 0.0d);

        assertSame(this.oneEUR.getUnit(), twoEUR.getUnit());

        Amount theAmount = this.oneUSD.add(this.oneEUR);

        assertEquals(1.0d, this.oneEUR.getValue(), 0.0d);
        assertEquals(USD_RATIO / EUR_RATIO + (1.0d / EUR_RATIO), theAmount.getValue(), 0.0d);

        assertSame(this.oneEUR.getUnit(), theAmount.getUnit());
        
        theAmount = this.oneUSD.add(this.oneGBP);

        assertEquals(1.0d, this.oneUSD.getValue(), 0.0d);
        assertEquals((USD_RATIO / EUR_RATIO) + (GBP_RATIO / EUR_RATIO), theAmount.getValue(), TOLERANCE);

        assertSame(this.oneEUR.getUnit(), theAmount.getUnit());
        
        theAmount = this.oneUSD.add(this.oneYEN);

        assertEquals(1.0d, this.oneUSD.getValue(), 0.0d);
        assertEquals((USD_RATIO / EUR_RATIO) + (YEN_RATIO / EUR_RATIO), theAmount.getValue(), TOLERANCE);

        assertSame(this.oneEUR.getUnit(), theAmount.getUnit());
    }
    
    /**
     * Test method for {@link com.top_logic.knowledge.wrap.currency.Amount#sub(com.top_logic.knowledge.wrap.currency.Amount)}.
     */
    public void testSub() {
        Amount zeroEUR = this.oneEUR.sub(this.oneEUR);

        assertEquals(1.0d, this.oneEUR.getValue(), 0.0d);
        assertEquals(0.0d, zeroEUR.getValue(), 0.0d);

        assertSame(this.oneEUR.getUnit(), zeroEUR.getUnit());

        Amount theAmount = this.oneUSD.sub(this.oneEUR);

        assertEquals(1.0d, this.oneEUR.getValue(), 0.0d);
        assertEquals((USD_RATIO / EUR_RATIO) - (EUR_RATIO / EUR_RATIO), theAmount.getValue(), 0.0d);

        assertSame(this.oneEUR.getUnit(), theAmount.getUnit());
        
        theAmount = this.oneUSD.sub(this.oneGBP);

        assertEquals(1.0d, this.oneUSD.getValue(), 0.0d);
        assertEquals((USD_RATIO / EUR_RATIO) - (GBP_RATIO / EUR_RATIO), theAmount.getValue(), TOLERANCE);

        assertSame(this.oneEUR.getUnit(), theAmount.getUnit());
        
        theAmount = this.oneUSD.sub(this.oneYEN);

        assertEquals(1.0d, this.oneUSD.getValue(), 0.0d);
        assertEquals((USD_RATIO / EUR_RATIO) - (YEN_RATIO / EUR_RATIO), theAmount.getValue(), TOLERANCE);

        assertSame(this.oneEUR.getUnit(), theAmount.getUnit());
    }

    /**
     * Test method for {@link com.top_logic.knowledge.wrap.currency.Amount#convert(Unit, java.util.Date)}
     */
    public void testConvert() {
        Amount theYenAmount = this.oneUSD.convert(YEN, null);

        assertNotNull(theYenAmount);
        assertEquals(1.0d, this.oneUSD.getValue(), 0.0d);
        assertEquals(USD, this.oneUSD.getUnit());

        assertEquals((USD_RATIO / YEN_RATIO), theYenAmount.getValue(), TOLERANCE);
        assertSame(YEN, theYenAmount.getUnit());
    }

    public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestAmount.class, CurrencySystem.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE));
    }
}
