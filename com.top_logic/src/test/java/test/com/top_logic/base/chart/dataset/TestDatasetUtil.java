/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.chart.dataset;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.chart.dataset.DatasetUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.util.TLContext;

/**
 * The TestDatasetUtil tests the class {@link DatasetUtil}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestDatasetUtil extends BasicTestCase {

    /**
     * This method tests the lowest and highest date in a
     * {@link TimeSeriesCollection}.
     */
    public void testDatasetDateRange() {
		TimeSeriesCollection theCollection = new TimeSeriesCollection(TimeZones.systemTimeZone());
        TimeSeries           theSeries     = new TimeSeries("Test");
        
        Calendar theCalendar = CalendarUtil.createCalendar();
        Date     theDate1    = DateUtil.adjustToDayBegin(theCalendar);
        theCalendar.add(Calendar.DAY_OF_MONTH, 10);
        Date     theDate2    = theCalendar.getTime();
        theCalendar.add(Calendar.DAY_OF_MONTH, 10);
		Date theDate3 = DateUtil.adjustToDayEnd(theCalendar);
        theCalendar.add(Calendar.DAY_OF_MONTH, 10);
        
		theSeries.add(new Day(theDate1, TimeZones.systemTimeZone(), TLContext.getLocale()), 1);
		theSeries.add(new Day(theDate2, TimeZones.systemTimeZone(), TLContext.getLocale()), 2);
		theSeries.add(new Day(theDate3, TimeZones.systemTimeZone(), TLContext.getLocale()), 3);
        theCollection.addSeries(theSeries);
        
        assertEquals(theDate1, DatasetUtil.getLowestDate (theCollection));
        assertEquals(theDate3, DatasetUtil.getHighestDate(theCollection));
    }
    
    /** 
     * This method test the methos {@link DatasetUtil#getSpaceLabel(int, String)}.
     */
    public void testGetSpaceLabel() {
        String theLabel          = "MyLabel";
        int    theNumberOfSpaces = 3;
        
        assertEquals("   " + theLabel, DatasetUtil.getSpaceLabel(theNumberOfSpaces, theLabel));
        
        try {
            DatasetUtil.getSpaceLabel(theNumberOfSpaces, null);
            fail();
        } catch (NullPointerException npe) {
            /* Expected */
        }
        
        try {
            DatasetUtil.getSpaceLabel(-1, theLabel);
            fail();
        } catch (IllegalArgumentException iae) {
            /* Expected */
        }
    }
    
    /** 
     * This method returns the test suite.
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestDatasetUtil.class));
    }
    
    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        // Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
    
}

