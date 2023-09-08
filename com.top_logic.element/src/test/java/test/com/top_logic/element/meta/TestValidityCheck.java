/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.element.meta.ValidityCheck;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestValidityCheck extends TestCase {

    /** 
     * Creates a {@link TestValidityCheck}.
     */
    public TestValidityCheck(String name) {
        super(name);
    }



    /**
     * Test method for {@link com.top_logic.element.meta.ValidityCheck#getValidityCheck(java.lang.String)}.
     */
    public void testValidityCheck() {
        
        Date now = DateUtil.adjustToDayEnd(new Date());
        
        ValidityCheck theCheck = ValidityCheck.getValidityCheck(null);
        assertNotNull(theCheck);
        assertEquals(-1L, theCheck.getCheckDuration());
        assertNull(theCheck.getNextTimeout(null));
        assertNull(theCheck.getNextTimeout(new Date()));
        assertNull(theCheck.getTouchTimeFor(null));
        assertNull(theCheck.getTouchTimeFor(new Date()));

		Calendar calendarExpected = CalendarUtil.createCalendar(now);
        calendarExpected.add(Calendar.YEAR, +1);
        
        theCheck = ValidityCheck.getValidityCheck("validity:12M;duration:3M;");
        assertEquals(3L * 1000 * 60 * 60 * 24 * 30, theCheck.getCheckDuration());
        
        // calculate timeout
        Date theNextTimeout = theCheck.getNextTimeout(now);
        assertEquals(calendarExpected.getTime(), theNextTimeout);
        // inverse calculation
        assertEquals(now, theCheck.getTouchTimeFor(theNextTimeout));
        
		Calendar calendarTouched = CalendarUtil.createCalendar();
		calendarTouched.set(Calendar.YEAR, 2012);
		calendarTouched.set(Calendar.DAY_OF_MONTH, 1);
		calendarTouched.set(Calendar.MONTH, Calendar.DECEMBER);

		calendarExpected.set(Calendar.YEAR, 2013);
        calendarExpected.set(Calendar.DAY_OF_MONTH, 1);
		calendarExpected.set(Calendar.MONTH, Calendar.SEPTEMBER);

		theCheck = ValidityCheck.getValidityCheck("validity:9M;duration:3M;type:fixed;referenceDate:20110601");
		assertEquals(calendarExpected.getTime(), theCheck.getNextTimeout(calendarTouched.getTime()));
    }

    /** 
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
        return ElementWebTestSetup.createElementWebTestSetup(new TestSuite(TestValidityCheck.class));
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] args) throws IOException {
        junit.textui.TestRunner.run(TestValidityCheck.suite());
    } 
}

