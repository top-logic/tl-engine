/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.chart.dataset;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jfree.data.time.Day;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.chart.dataset.ExtendedTimeSeries;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.util.TLContext;

/**
 * The ExtendedTimeSeries tests the class {@link com.top_logic.base.chart.dataset.ExtendedTimeSeries}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestExtendedTimeSeries extends BasicTestCase {

    /**
     * This method tests the {@link ExtendedTimeSeries}. The tests stores
     * values and objects into a extended time series and checks if the values
     * and objects are correctly returned.
     */
    public void testTimeSeries() {
        String  theName   = "Series Name";
        String  theObj1    = "object1";
        String  theObj2    = "object2";
		Day theDay1 = new Day(new Date(), TimeZones.systemTimeZone(), TLContext.getLocale());
		Day theDay2 = new Day(DateUtil.nextDay(new Date()), TimeZones.systemTimeZone(), TLContext.getLocale());
        Double  theDouble = Double.valueOf(5.0);
        double  theValue   = 5.5d;
        
        ExtendedTimeSeries theSeries = new ExtendedTimeSeries(theName);
        theSeries.add(theDay1, theValue,  theObj1);
        theSeries.add(theDay2, theDouble, theObj2);
        assertEquals(theDay1, theSeries.getTimePeriod(0));
        assertEquals(theDay2, theSeries.getTimePeriod(1));
        assertEquals(theValue, theSeries.getValue(0).doubleValue(), EPSILON);
        assertEquals(theDouble, theSeries.getValue(1));
        assertEquals(theObj1, theSeries.getObject(0));
        assertEquals(theObj2, theSeries.getObject(1));
        
        theSeries.update(theDay1, 7.0, theObj2);
        assertEquals(7.0, theSeries.getValue(0).doubleValue(), EPSILON);
        assertEquals(theObj2, theSeries.getObject(0));
        
        Date theDate  = DateUtil.nextDay(new Date());
        theDate = DateUtil.nextDay(theDate);
		Day theDay3 = new Day(theDate, TimeZones.systemTimeZone(), TLContext.getLocale());
        theSeries.addOrUpdate(theDay3, 8.0, theObj1);
        assertEquals(8.0, theSeries.getValue(2).doubleValue(), EPSILON);
        assertEquals(theObj1, theSeries.getObject(2));
        
        theSeries.addOrUpdate(theDay3, 4.0, theObj2);
        assertEquals(4.0, theSeries.getValue(2).doubleValue(), EPSILON);
        assertEquals(theObj2, theSeries.getObject(2));
    }
    
    /** 
     * This method returns the test suite.
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestExtendedTimeSeries.class));
    }

    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        // Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
    
}

