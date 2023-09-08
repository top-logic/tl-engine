/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskComparator;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;

/**
 * Test case for the {@link com.top_logic.util.sched.task.TaskComparator}.
 * 
 * @author    <a href=mailto:kha@top-logic.com">kha</a>
 */
public class TestTaskComparator extends TestCase {

    /**
     * Constructor for TestTaskComparator.
     * 
     * @param name method name of the test to execute.
     */
    public TestTaskComparator(String name) {
        super(name);
    }

    /** Test comparison of Threads */
    public void testComparison() {
        
        Calendar now = new GregorianCalendar(2004,Calendar.JULY, 14 ,12,0);

        Task daily1 = new TaskImpl("Daily", LegacySchedulesCommon.DAILY,0, 0 , 0);
        Task daily2 = new TaskImpl("Daily", LegacySchedulesCommon.DAILY,0, 1 , 0);
        Task daily3 = new TaskImpl("Daily", LegacySchedulesCommon.DAILY,0, 12, 30);
        
        Task daily4 = new TaskImpl("Daily4", LegacySchedulesCommon.DAILY,0, 0 , 0);
        Task daily5 = new TaskImpl("Daily5", LegacySchedulesCommon.DAILY,0, 1 , 0);
        Task daily6 = new TaskImpl("Daily6", LegacySchedulesCommon.DAILY,0, 12, 30);        

        daily1.calcNextShed(now.getTimeInMillis()); // Wed Jul 14 12:00:00 CEST 2004
        daily2.calcNextShed(now.getTimeInMillis()); // Wed Jul 14 12:00:00 CEST 2004
        daily3.calcNextShed(now.getTimeInMillis()); // Wed Jul 14 12:30:00 CEST 2004
        
        daily4.calcNextShed(now.getTimeInMillis()); // Wed Jul 14 12:00:00 CEST 2004
        daily5.calcNextShed(now.getTimeInMillis()); // Wed Jul 14 12:00:00 CEST 2004
        daily6.calcNextShed(now.getTimeInMillis()); // Wed Jul 14 12:30:00 CEST 2004        


        // Changed by asc: Added TaskComparator
        TaskComparator theComp = TaskComparator.INSTANCE;
        
        assertTrue(0 == theComp.compare(daily1,daily1));
        assertTrue(0 == theComp.compare(daily2,daily2));
        assertTrue(0 == theComp.compare(daily3,daily3));
                
        assertTrue(0 == theComp.compare(daily4,daily4));
        assertTrue(0 == theComp.compare(daily5,daily5));
        assertTrue(0 == theComp.compare(daily6,daily6));
        
        // compare tasks with different names
        assertFalse(0 == theComp.compare(daily1,daily4));
        assertFalse(0 == theComp.compare(daily2,daily5));
        assertFalse(0 == theComp.compare(daily3,daily6));
                
        
        // compare tasks with the same name
        assertTrue(theComp.compare(daily1,daily2) == 0);
        assertTrue(theComp.compare(daily1,daily3) < 0);
        assertTrue(theComp.compare(daily2,daily1) == 0);
        assertTrue(theComp.compare(daily2,daily3) < 0);
        assertTrue(theComp.compare(daily3,daily1) > 0);
        assertTrue(theComp.compare(daily3,daily2) > 0);
        
        // compare tasks with other names
        // Now, two Tasks with the same nextShed are not equal, if they have
        // other names    
        // The String "Daily4" is smaller than "Daily5"
        assertTrue(theComp.compare(daily4,daily5) < 0);
        assertTrue(theComp.compare(daily4,daily6) < 0);
        // The String "Daily5" is bigger than "Daily4"
        assertTrue(theComp.compare(daily5,daily4) > 0);   
        assertTrue(theComp.compare(daily5,daily6) < 0);
        assertTrue(theComp.compare(daily6,daily4) > 0);
        assertTrue(theComp.compare(daily6,daily5) > 0);


        // compare tasks with other names to some with the same name
        // but other nextSheduled-Dates
        assertTrue(theComp.compare(daily4,daily2) > 0);
        assertTrue(theComp.compare(daily4,daily3) < 0);
        // The String "Daily" is smaller than "Daily5"
        assertTrue(theComp.compare(daily2,daily5) < 0);  
        assertTrue(theComp.compare(daily5,daily3) < 0);
        assertTrue(theComp.compare(daily3,daily1) > 0);
        assertTrue(theComp.compare(daily3,daily2) > 0);        
    } 

    /** Test Null and other Strnge comparisons */
    public void testStrange() {
        // Changed by aasc: Added TaskComparator
        TaskComparator theComp = TaskComparator.INSTANCE;
        
        assertEquals( 0, theComp.compare(null        ,null));
        
        Task nulltask  = new TaskImpl(null      , LegacySchedulesCommon.DAILY,0, 0 , 0);
        Task someTask  = new TaskImpl("someTask", LegacySchedulesCommon.DAILY,0, 0 , 0);

        assertEquals( 0, theComp.compare(nulltask ,nulltask));
		assertTrue(theComp.compare(nulltask, someTask) < 0);
		assertTrue(theComp.compare(someTask, nulltask) > 0);

    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite (TestTaskComparator.class);
        // return new TestTaskComparator("testComparison");
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
