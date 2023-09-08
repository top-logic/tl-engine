/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.filter.DateFilter;

/**
 * The TestDateFilter tests the class {@link TestDateFilter}.
 * 
 * @author  <a href=mailto:tdi@top-logic.com>Thomas Dickhut</a>
 */
public class TestDateFilter extends BasicTestCase {

    /** 
     * This method tests the date filter.
     */
    public void testDateFilter() {
        Date theToday       = new Date();
        Date theTodayBegin  = DateUtil.adjustToDayBegin(theToday);
        Date theBeforeBegin = getDate(theTodayBegin, -1);
        Date theTodayEnd    = DateUtil.adjustToDayEnd  (theToday);
        Date theAfterEnd    = getDate(theTodayEnd, +1);
        
        /* Test the limit values. */
        DateFilter theDateFilter = new DateFilter(theToday, theToday);
        assertTrue (theDateFilter.accept(theTodayBegin));
        assertFalse(theDateFilter.accept(theBeforeBegin));
        assertTrue(theDateFilter.accept(theTodayEnd  ));
        assertFalse(theDateFilter.accept(theAfterEnd));
        
        /* Test without adjusting dates. */
        Date theDate   = DateUtil.adjustToNoon(theToday);
        Date theBefore = getDate(theDate, -1);
        Date theAfter  = getDate(theDate, +1);
        theDateFilter  = new DateFilter(theDate, theDate, false);
        assertTrue (theDateFilter.accept(theDate));
        assertFalse(theDateFilter.accept(theBefore));
        assertFalse(theDateFilter.accept(theAfter));
        
    }

    private Date getDate(Date aDate, int someMilliseconds) {
		return DateUtil.addMilliseconds(aDate, someMilliseconds);
    }
    
    /**
     * Return the suite of tests to perform.
     */
    public static Test suite () {
		TestSuite suite = new TestSuite(TestDateFilter.class);
        return BasicTestSetup.createBasicTestSetup(suite);
    }

    /**
     * Main function for direct execution.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}

