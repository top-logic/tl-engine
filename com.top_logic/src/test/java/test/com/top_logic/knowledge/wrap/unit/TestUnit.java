/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.unit;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.XPathUtil;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;

/**
 * Testcase for {@link com.top_logic.knowledge.wrap.unit.Unit} and its implementation.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestUnit extends BasicTestCase {
    
    /**
     * Create tets for given function name.
     */
    public TestUnit(String aName) {
        super(aName);
    }
    
    /** 
     * Test a BaseUinit 
     */
    public void testBaseUnit() throws Exception {
        UnitWrapper meter= UnitWrapper.getInstance(KBSetup.getKnowledgeBase(), "m");
		assertEquals("m", meter.getName());
        assertTrue   (          meter.isBaseUnit());
        assertNull   (          meter.getBaseUnit());
        assertEquals (1.0     , meter.getConversionFactor(), EPSILON);
        assertNotNull(          meter.getFormat());
        assertNotNull(          meter.getFormat(Locale.KOREAN));
        assertEquals("1,11"   ,  meter.getFormat(Locale.GERMAN).format(1.11));
        assertEquals("1.11"   ,  meter.getFormat(Locale.US)    .format(1.11));
        assertEquals (1       , meter.parse("1").intValue());
    }
    
    /** 
     * Test some derived Unit.
     */
    public void testDerivedUnit1() throws Exception {
		final Unit km = UnitWrapper.getInstance(KBSetup.getKnowledgeBase(), "km");
		assertEquals("km", km.getName());
        assertFalse  (          km.isBaseUnit());
		assertEquals("m", km.getBaseUnit().getName());
        assertEquals (1000.0  , km.getConversionFactor(), EPSILON);
        assertNotNull(          km.getFormat());
        assertNotNull(          km.getFormat(Locale.KOREAN));
        assertEquals (1000    , km.parse("1").intValue());
		executeInLocale(Locale.GERMANY, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("42,00", km.format(Integer.valueOf(42000)));
				assertEquals("12,34", km.format(Double.valueOf(12340)));
			}
		});
    }

    /** 
     * Test some derived Unit.
     */
    public void testDerivedUnit2() throws Exception {
		final Unit mm = UnitWrapper.getInstance(KBSetup.getKnowledgeBase(), "mm");
		assertEquals("mm", mm.getName());
        assertFalse  (          mm.isBaseUnit());
		assertEquals("m", mm.getBaseUnit().getName());
		assertEquals(1 / 1000d, mm.getConversionFactor(), EPSILON);
        assertNotNull(          mm.getFormat());
        assertNotNull(          mm.getFormat(Locale.KOREAN));
		assertEquals(0.001, mm.parse("1").doubleValue(), EPSILON);
		executeInLocale(Locale.GERMANY, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("42.000,00", mm.format(Integer.valueOf(42)));
				assertEquals("1,23", mm.format(Double.valueOf(0.00123)));
			}
		});
    }

    /**
	 * Test some derived Unit.
	 */
	public void testDerivedUnit3() throws Exception {
		final Unit cm = UnitWrapper.getInstance(KBSetup.getKnowledgeBase(), "cm");
		assertEquals("cm", cm.getName());
		assertFalse(cm.isBaseUnit());
		assertEquals("m", cm.getBaseUnit().getName());
		assertEquals(1 / 100d, cm.getConversionFactor(), EPSILON);
		assertNotNull(cm.getFormat());
		assertNotNull(cm.getFormat(Locale.KOREAN));
		assertEquals(0.01, cm.parse("1").doubleValue(), EPSILON);
		executeInLocale(Locale.GERMANY, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("4.200,00", cm.format(Integer.valueOf(42)));
				assertEquals("1,23", cm.format(Double.valueOf(0.0123)));
			}
		});
	}

    /** 
     * Test Formatting using the TLContext
     */
    public void testFormatContext() throws Exception {
		final UnitWrapper meter = UnitWrapper.getInstance(KBSetup.getKnowledgeBase(), "m");
		executeInLocale(Locale.GERMANY, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("1,00", meter.format(Integer.valueOf(1)));
				assertEquals("22,00", meter.format(Double.valueOf(22.0)));
			}
		});
		executeInLocale(Locale.US, new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("13.00", meter.format(Integer.valueOf(13)));
				assertEquals("0.50", meter.format(Double.valueOf(0.5)));
			}
		});
    }
    
    /** 
     * Test the getAll function.
     */
    public void testGetAll() throws Exception {
		List<Unit> allUnits = UnitWrapper.getAllUnits();

		BinaryData input = FileManager.getInstance().getData("/WEB-INF/kbase/KBDataUnit.xml");
		int expectedCnt = XPathUtil.matchCount(input, "//knowledgeobject[@object_type='Unit']");
		assertEquals(expectedCnt, allUnits.size());
    }

    /** 
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestUnit.class);
    }
    
}
