/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.event.logEntry;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;

import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.event.logEntry.ActualUserDayEntry;
import com.top_logic.event.logEntry.UserDayEntry;
import com.top_logic.event.logEntry.UserDayEntryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Test case for the {@link UserDayEntryManager}.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestUserDayEntryManager  extends TestLogHelper {
   
    /** 
     * Create a new TestUserDayEntryManager with given testName.
     */
    public TestUserDayEntryManager(String testName) {
        super(testName);
    }
    
    @Override
	protected void setUp() throws Exception {
    	super.setUp();
        KnowledgeBase theKB        = KBSetup.getKnowledgeBase();
        try {
            ThreadContext.pushSuperUser(); // need super-cleanuü-power
            deleteOldKOsAndKAs();
            assertTrue(theKB.commit());
            
        } finally {
            ThreadContext.popSuperUser();
            // TLContext.removeContext();
        }
    }

    /**
     * DeleteOldKOsAndKAs after test, as well.
     */
    @Override
	protected void tearDown() throws Exception {
        KnowledgeBase theKB        = KBSetup.getKnowledgeBase();
        try {
            ThreadContext.pushSuperUser(); // need super-cleanuü-power
            deleteOldKOsAndKAs();
            assertTrue(theKB.commit());
            
        } finally {
            ThreadContext.popSuperUser();
            // TLContext.removeContext();
        }
        super.tearDown();
    }

    public void testCreateNewLogEntry()throws Exception{
        try {
            ThreadContext.pushSuperUser();
            Date now = new Date();
            Person              root = PersonManager.getManager().getRoot();
            UserDayEntryManager udem = UserDayEntryManager.getInstance();
            
            UserDayEntry theEntry = udem.getEntry(root, now);
            int oldCount = 0;
            if(theEntry != null){            
                oldCount = theEntry.getLogEntries().size();
            }
            
            createLogEntry();

            theEntry = udem.getEntry(root, now);
            assertNotNull(theEntry);
            int count = theEntry.getLogEntries().size();
            assertEquals("Wrong number of LogEntries in ActualUserDayEnty",oldCount+1,count);
            
            assertTrue(theEntry instanceof ActualUserDayEntry);
            
        } finally {
            ThreadContext.popSuperUser();
        }           
    }
    
    public void testDifferentDays()throws Exception{
        try {
            ThreadContext.pushSuperUser();
            UserDayEntryManager udem = UserDayEntryManager.getInstance();
            
            // suppress removal of duplicates for old / new count
			Calendar cal = CalendarUtil.createCalendar();
			int numOfSeconds = 1 + udem.getDuplicateInterval();
            cal.add(Calendar.SECOND, - numOfSeconds);
            Date now     = cal.getTime();

            cal.add(Calendar.DAY_OF_YEAR,-1);
            Date yesterday = cal.getTime();
            
            // Attention oldCount / newCount can be misleading 
            // as duplicates are suppresed!
            
            Person root = PersonManager.getManager().getRoot();
            UserDayEntry theNowEntry = udem.getEntry(root, now);
            int oldNowCount = 0;
            if(theNowEntry!=null){            
                oldNowCount = theNowEntry.getLogEntries().size();
            }
            
            UserDayEntry theYesterdayEntry = udem.getEntry(root, yesterday);
            int oldYesterdayCount = 0;
            if(theYesterdayEntry!=null){            
                oldYesterdayCount = theYesterdayEntry.getLogEntries().size();
            }           
            
            createLogEntry(now);
            createLogEntry(yesterday);
            
            assertTrue(KBSetup.getKnowledgeBase().commit());

            
            theNowEntry = udem.getEntry(root, now);
            assertNotNull(theNowEntry);
            int newCount = theNowEntry.getLogEntries().size();
            assertEquals("Wrong number of LogEntries in ActualUserDayEnty",oldNowCount+1,newCount);            
            assertTrue(theNowEntry instanceof ActualUserDayEntry);
            
            theYesterdayEntry = udem.getEntry(root, yesterday);
            assertNotNull(theYesterdayEntry);
            newCount = theYesterdayEntry.getLogEntries().size();
            assertEquals("Wrong number of LogEntries in YesterdayEntry",oldYesterdayCount+1,newCount);            
            assertTrue(theYesterdayEntry instanceof ActualUserDayEntry);           
            
        } finally {
            ThreadContext.popSuperUser();
        }           
    }

    /** Return the suite of Tests to perform */
    public static Test suite () {
    	return suite(TestUserDayEntryManager.class);
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        // KBSetup.CREATE_TABLES = false;    // for debugging
        Logger.configureStdout();            // "INFO"
        junit.textui.TestRunner.run (suite ());
    }
    
}
