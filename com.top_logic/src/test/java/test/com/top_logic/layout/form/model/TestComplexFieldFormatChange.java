/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import junit.framework.Test;
import junit.textui.TestRunner;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;

/**
 * Test {@link ComplexField} with {@link NumberFormat}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestComplexFieldFormatChange extends AbstractComplexFieldTest {

    private static final Double PI = Double.valueOf(Math.PI);
	static final NumberFormat THREE_DIGITS_FORMAT = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.ENGLISH));
    static final NumberFormat INT_FORMAT = NumberFormat.getIntegerInstance(Locale.ENGLISH);
//    static final NumberFormat INT_FORMAT = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));
    
    /**
     * @see test.com.top_logic.layout.form.model.AbstractComplexFieldTest#setUp()
     */
    @Override
    public void setUp() throws Exception {
		super.setUp();
        Object value = Long.valueOf(0);
        field = FormFactory.newComplexField("name", THREE_DIGITS_FORMAT, value, false);
        field.setDefaultValue(value);
    }
    
    public void testFormat() throws ParseException, VetoException {
        field.setValue(PI);
        assertEquals(PI, field.getValue()); // Value is left alone
        assertEquals("3.142", field.getRawString());
    }
    
    /**
     * Regression test for {@link ComplexField#setFormat(java.text.Format)} #1661.
     */
    public void testFormatChange() throws ParseException, VetoException {
        field.setValue(PI);
        assertEquals(PI, field.getValue()); // Initial value.
        assertEquals("3.142", field.getRawString());
        
        field.setFormat(INT_FORMAT);
        assertEquals(PI , field.getValue()); // value was left alone
        assertEquals("3", field.getRawString());
        
        field.setAsString("3.1415927");
        assertEquals(Double.valueOf(3.1415927), field.getValue());
        assertEquals("3", field.getRawString()); // was reformatted with intFormat
    }
    
    public void testFormatChangeInErrorState() throws ParseException, VetoException {
    	field.update("abc");
    	assertTrue(field.hasError());
    	assertFalse(field.hasValue());
    	
    	field.setFormat(INT_FORMAT);
    	assertTrue(field.hasError());
    	assertFalse(field.hasValue());
    }

    public void testParserChangeInErrorState() throws ParseException, VetoException {
    	field.update("abc");
    	assertTrue(field.hasError());
    	assertFalse(field.hasValue());
    	
    	field.setParser(INT_FORMAT);
    	assertTrue(field.hasError());
    	assertFalse(field.hasValue());
    }
    
    public void testParser() throws ParseException, VetoException {
        field.setValue(PI);
        assertEquals(PI, field.getValue()); // Initial value.
        assertEquals("3.142", field.getRawString());
        
        field.setAsString("3.1415927"); // Must be rounded by format ...
        Number expected = THREE_DIGITS_FORMAT.parse("3.1415927");
        assertEquals(expected, field.getValue());
        assertEquals(THREE_DIGITS_FORMAT.format(expected), field.getRawString());
    }
    
    public void testParseError() throws ParseException, VetoException {
    	field.setParser(INT_FORMAT);
    	
        field.update("3.1415927");
		assertFalse("Input does not conform to parser but to format.", field.hasError());
		assertTrue("Input does not conform to parser but to format.", field.hasValue());
		// Rounded by format ...
		assertEquals("3.142", field.getRawString());
        
        field.update("3");
        assertEquals(Long.valueOf(3), field.getValue());
        assertEquals("3.000", field.getRawString());
        
        field.setAsString("3");
        assertEquals(Long.valueOf(3), field.getValue());
        assertEquals("3.000", field.getRawString());
    }
    
    /**
     * Regression test for {@link ComplexField#setParser(java.text.Format)} #1661.
     */
    public void testParserChange() throws ParseException, VetoException {
        field.setValue(PI);
        assertEquals(PI, field.getValue()); // Initial value.
        assertEquals("3.142", field.getRawString());
    }

    /**
	 * Regression test for {@link ComplexField#setFormatAndParser(java.text.Format)} #1661.
	 */
	public void testFormatAndParserError() throws ParseException, VetoException {
        field.setValue(PI);
        assertEquals(PI, field.getValue()); // Initial value.
        assertEquals("3.142", field.getRawString());

        field.setFormatAndParser(INT_FORMAT);
        
        assertTrue("Input does not conform to new format.", field.hasError());
        assertEquals("3.142", field.getRawString());    // user Input was left alone
	}
	
	public void testFormatAndParser() throws ParseException, VetoException {
		field.setFormatAndParser(INT_FORMAT);
		
        field.update("3.142");
        assertTrue("Input does not conform to format.", field.hasError());
        assertEquals("3.142", field.getRawString());    // user Input was left alone
        
        field.setFormatAndParser(THREE_DIGITS_FORMAT);

        assertFalse("Input does now conform to format.", field.hasError());
        assertEquals(3.142d, field.getValue());
        assertEquals("3.142", field.getRawString());    // user Input was left alone
    }

	/**
	 * Regression test for {@link ComplexField#setFormatAndParser(java.text.Format)} #1661.
	 */
	public void testFormatAndParser2() throws ParseException, VetoException {
	    field.setValue(Double.valueOf(Math.PI));
        field.setFormatAndParser(THREE_DIGITS_FORMAT);
        assertEquals(3.142d, field.getValue()); // Value is left alone
        assertEquals("3.142", field.getRawString());
        field.setAsString("3.1415927"); // Must be rounded by format ...
        assertEquals(3.1415927d, field.getValue()); // Parser does accept more digits than specified.
        assertEquals("3.142", field.getRawString());
        
        field.setFormatAndParser(INT_FORMAT);
        assertTrue(field.hasError());        // was reformatted with intFormat
        assertFalse(field.hasValue());        // was reformatted with intFormat

        try {
			field.setAsString("3.1415927");
			fail("Must not set value with parse error.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
		
		field.update("3.1415");
        assertTrue(field.hasError());        // was reformatted with intFormat
        assertFalse(field.hasValue());        // was reformatted with intFormat
		
		field.update("3");
        assertEquals(Long.valueOf(3), field.getValue());
        assertEquals(            "3", field.getRawString());
    }

	public void testFormatCrossChange() throws ParseException, VetoException {
        field.update("3.142");
        assertEquals(3.142d, field.getValue());
		
        field.setFormatAndParser(XmlDateTimeFormat.INSTANCE);
        assertEquals("3.142", field.getRawString());    // user Input was left alone
        assertTrue("Input does not conform to format.", field.hasError());
        
        field.update("2009-12-01T17:41:00.001Z");
        assertFalse("Input does conform to format.", field.hasError());
        assertEquals("2009-12-01T17:41:00.001Z", field.getRawValue());
        assertEquals(XmlDateTimeFormat.INSTANCE.parseObject("2009-12-01T17:41:00.001Z"), field.getValue());
	}

	public void testDifferentFormatAndParser() throws VetoException {
		field.setFormatAndParser(DATE_TIME_FORMAT);
		field.setParser(DATE_FORMAT);
		field.update(NOW_AS_STRING);
		assertFalse("Value is accepted by format", field.hasError());
	}

	public static Test suite () {
		return suite(TestComplexFieldFormatChange.class);
    }

	public static void main(String[] args) {
		TestRunner.run(suite());
	}
}
