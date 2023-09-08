/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.format.MailAddressFormat;
import com.top_logic.layout.form.format.StringTokenFormat;
import com.top_logic.layout.form.model.DescriptiveParsePosition;

/**
 * The TestStringTokenFormat tests the class 
 * {@link com.top_logic.layout.form.format.StringTokenFormat}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestStringTokenFormat extends BasicTestCase{

    /** 
     * This method test the getter and setter methods.
     */
    public void testAccessorMethods() {
        SimpleDateFormat  simple     = CalendarUtil.newSimpleDateFormat("yyyy-mm");
        String            delimiters = ";,";
        StringTokenFormat format     = new StringTokenFormat(simple, delimiters);
        assertNotNull(format);
        
        assertEquals(format.getParseDelimiters(), delimiters);
        assertEquals(format.getFormatDelimiter(), ";");

        delimiters = "; ";
        format.setParseDelimiters(delimiters);
        assertEquals(format.getParseDelimiters(), delimiters);
        assertEquals(format.getFormatDelimiter(), delimiters);

        delimiters = ", ";
        format.setParseDelimiters(delimiters);
        format.setFormatDelimiter(delimiters);
        assertEquals(format.getFormatDelimiter(), delimiters);
        
        assertEquals(format.getFormat(), simple);
        format.setFormat(null);
        assertEquals(format.getFormat(), null);
        
        assertFalse(format.isRemoveSpaces());
        format.setRemoveSpaces(true);
        assertTrue(format.isRemoveSpaces());
    }
    
    /** 
     * This method tests the {@link StringTokenFormat#parseObject(String, ParsePosition)}.
     */
    public void testParseObject() {
        /*
         * Test parsing simple date formats.
         */
        SimpleDateFormat  simple     = CalendarUtil.newSimpleDateFormat("yyyy-MM");
        String            delimiters = ",;";
        StringTokenFormat format     = new StringTokenFormat(simple, delimiters);
        
		Calendar cal = CalendarUtil.createCalendar();
        cal.set(2006, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date     date1 = cal.getTime();
        
        cal.set(2006, Calendar.FEBRUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date2 = cal.getTime();
        
        cal.set(2006, Calendar.MARCH, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date3 = cal.getTime();
        
        String dates = simple.format(date1) + "," + simple.format(date2) + "," + 
        simple.format(date3);
        
        Object result = format.parseObject(dates, new DescriptiveParsePosition(0));
        assertInstanceof(result, List.class);
        List   list   = (List)result;
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), date1);
        assertEquals(list.get(1), date2);
        assertEquals(list.get(2), date3);
        
        format = new StringTokenFormat(new MailAddressFormat(), delimiters);
        format.setRemoveSpaces(true);
        DescriptiveParsePosition des = new DescriptiveParsePosition(0);
        String mailAddress = "dfskj@dddddd";
        result = format.parseObject(mailAddress, des);
        assertTrue(des.getIndex() < mailAddress.length());
        assertNotNull(des.getErrorMessage());
        
        /*
         * Test exceptions.
         */
        try {
            format.parseObject("", null);
            fail();
        } catch (NullPointerException e) {
            /* expected */
        }
        
        result = format.parseObject(null, new ParsePosition(0));
        assertInstanceof(result, List.class);
        list   = (List)result;
        assertEquals(list, Collections.EMPTY_LIST);
    }
    
    /** 
     * This method tests the 
     * {@link StringTokenFormat#format(Object, StringBuffer, FieldPosition)}.
     */
    public void testFormatObject() {
        StringTokenFormat format = new StringTokenFormat(new MailAddressFormat(), ";, ");
        FieldPosition     pos    = new FieldPosition(0);
        
        StringBuffer buffer = new StringBuffer();
        ArrayList    list   = new ArrayList();
        list.add("tdi@bos.local");
        list.add("he@web.de");
        list.add("hello@gmx.com");
        format.format(list, buffer, pos);
        assertEquals(buffer.toString(), "tdi@bos.local; he@web.de; hello@gmx.com");
        
        /* 
        * Test formate with one space between the tokens (e.g. 'a, b, c, ...').
        */
        list.clear();
        buffer = new StringBuffer();
        pos    = new FieldPosition(0);
        format.setFormatDelimiter(", ");
        list.add("tdi@bos.local");
        list.add("he@web.de");
        list.add("hello@gmx.com");
        format.format(list, buffer, pos);
        assertEquals(buffer.toString(), "tdi@bos.local, he@web.de, hello@gmx.com");
        
        /*
         * Test formate with a simple date formatting.
         */
        SimpleDateFormat  simple     = CalendarUtil.newSimpleDateFormat("yyyy-MM");
        String            delimiters = ",; ";
        format = new StringTokenFormat(simple, delimiters);
        
        list.clear();
        buffer = new StringBuffer();
        pos    = new FieldPosition(0);
		Calendar cal = CalendarUtil.createCalendar();
        Date date1 = cal.getTime();
        list.add(date1);
        
        cal.add(Calendar.MONTH, 1);
        Date date2 = cal.getTime();
        list.add(date2);
        
        cal.add(Calendar.MONTH, 1);
        Date date3 = cal.getTime();
        list.add(date3);
        
        format.format(list, buffer, pos);
        String result = simple.format(date1) + ", " + simple.format(date2) + ", " +   
                        simple.format(date3);
        assertEquals(buffer.toString(), result);
        
        /*
         * Test the illegal argument exception.
         */
        buffer = new StringBuffer();
        pos    = new FieldPosition(0);
        format.setFormatDelimiter(", ");
        try {
            format.format("i am not a list", buffer, pos);
        } catch (IllegalArgumentException e) {
            /* expected */
        }
        
        /*
         * Test the null pointer exceptions.
         */
        try {
            format.format(list, null, pos);
            fail();
        } catch (NullPointerException e) {
            /* expected */
        }
        
        try {
            format.format(pos, new StringBuffer(), null);
            fail();
        } catch (NullPointerException e) {
            /* expected */
        }
    }
    
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite(TestStringTokenFormat.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }
    
    /**
     * This method can be used for direct testing.
     * 
     * @param args A string array of argument.
     */
    public static void main (String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite());
    }
    
}

