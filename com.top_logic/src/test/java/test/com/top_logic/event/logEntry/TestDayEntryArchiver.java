/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.event.logEntry;

import static test.com.top_logic.util.sched.model.TaskTestUtil.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.event.logEntry.ArchiveUserDayEntry;
import com.top_logic.event.logEntry.DayEntryArchiverTask;
import com.top_logic.event.logEntry.UserDayEntry;
import com.top_logic.event.logEntry.UserDayEntryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;

/**
 * Test the {@link DayEntryArchiverTask}.
 * 
 * TODO KHA/FMA this test depends on the current time.
 * e.G. it seems correct at the morning but fails in the evening. 
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestDayEntryArchiver extends TestLogHelper {
    
    /** Number of Events to create for MassTest */
    public static final int MASS_EVENTS = 1000;

   /** 
     * Create a new TestDayEntryArchiver for given test-name.
     */
    public TestDayEntryArchiver(String aName) {
        super(aName);
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        try {
            ThreadContext.pushSuperUser();
                     
            deleteOldKOsAndKAs();
            assertTrue(KBSetup.getKnowledgeBase().commit());
            
        } finally {
            ThreadContext.popSuperUser();
        }
    }

    public void testRun()throws Exception{
		TLContext tlc = TLContext.getContext();
		Person root = PersonManager.getManager().getRoot();
		tlc.setCurrentPerson(root);

		DayEntryArchiverTask archiver = new DayEntryArchiverTask(DayEntryArchiverTask.class.getName());
		initTaskLog(archiver);
		archiver.run();
    }
    
    public void testMultiDays_10() throws Exception{
        executeMultidayTest(10);           
    }

    public void testMultiDays_25() throws Exception{
        executeMultidayTest(25);           
    }

    /**
     * Create numOfDays historical events (30 Days in the past and later) events and archive them.
     * 
     * @param numOfDays will in result in numOfDays * numOfDays / 2 Events 
     */
	private void executeMultidayTest(int numOfDays) {
        Person root = PersonManager.getManager().getRoot();

        Date[]         dates    = new Date[numOfDays];
        
		Calendar cal = CalendarUtil.createCalendar();
        
        // go back some days to have no interference with other tests
        cal.add(Calendar.DAY_OF_YEAR, -30 - numOfDays);
        
        for(int i=0;i<numOfDays;i++){
            dates[i]=cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR,1);
        }

        // Count the old entries, usually all 0
        int[]               oldCount       = new int[numOfDays];
        UserDayEntryManager userDayManager = UserDayEntryManager.getInstance();
        for(int i=0;i<numOfDays;i++){
            UserDayEntry entry = userDayManager.getEntry(root, dates[i]);
            if(entry != null){            
                oldCount[i] = entry.getLogEntries().size();
            } 
        }           
    
        for(int i=0; i<numOfDays;i++){
            cal.setTime(dates[i]);
            cal.set(Calendar.HOUR_OF_DAY,1);
			KnowledgeBase kb = KBSetup.getKnowledgeBase();
			Transaction tx = kb.beginTransaction();
            // Create 1    , 2    , 3     ... LogEntries
            // for    01:00, 01:11, 01:22 ...
            for(int j=0;j<=i;j++) { 
                cal.add(Calendar.MINUTE,11);
                createLogEntry(cal.getTime());
            }                
			tx.commit();
        }                      
        
        // check set up
        startTime();
        for(int i=0; i<numOfDays;i++) {
            UserDayEntry theEntry = userDayManager.getEntry(root, dates[i]);
            assertNotNull("No Entry for #" + i, theEntry);
            int theCount = theEntry.getLogEntries().size();
            assertEquals("Wrong number of LogEntries # " 
                    + i + " at " + dates[i],oldCount[i]+1+i,theCount);
        }           
        logTime("Fetching  " + numOfDays + " Days");
      
        
        // archive everything
		TestedArchiver archiver = new TestedArchiver(TestedArchiver.class.getName());
		initTaskLog(archiver);
        startTime();
        archiver.archive();             
        logTime("Archiving " + numOfDays + " Days");
        
        for(int i=0; i<numOfDays;i++){
            UserDayEntry theEntry = userDayManager.getEntry(root, dates[i]);
            assertNotNull("No Entry for #" + i, theEntry);
            int theCount = theEntry.getLogEntries().size();
            assertEquals("Wrong number of LogEntries in ArchiveUserDayEntry # " 
                        + i + " at " + dates[i],oldCount[i]+1+i,theCount);
        }            
        logTime("Fetching  " + numOfDays + " Days, archived");

      
    }

    /**
     * Test running the task more than once.
     */
	public void testMultiRun() throws DataObjectException {
        try {
            ThreadContext.pushSuperUser();
			Calendar cal = CalendarUtil.createCalendar();
			TestedArchiver archiver = new TestedArchiver(TestedArchiver.class.getName());
			initTaskLog(archiver);
            int archivedCount = 0; 
            int expectedCount = 0;
            Random rand = new Random(0x192837465L);

            int alive = PersonManager.getManager().getAllAlivePersons().size();

            cal.add(Calendar.MINUTE, -10 * MASS_EVENTS);
            final KnowledgeBase kBase = KBSetup.getKnowledgeBase();
            
            startTime();
            for (int i=0; i < MASS_EVENTS; i++) {
                createLogEntry(cal.getTime()); 
                expectedCount += alive;
                if (rand.nextBoolean()) {
                    assertTrue(kBase.getName(), kBase.commit());
                    archivedCount += archiver.archive(); // implies commit
                    assertEquals("@" + i + " " + kBase.getName(), expectedCount, archivedCount);
                    if (rand.nextBoolean()) {
                        assertEquals(0, archiver.archive()); // must be a noop.
                    }
                }
                cal.add(Calendar.MINUTE, 10);
            }
            logTime("MultiRun        " + expectedCount + " * " + alive);
        } finally {
            ThreadContext.popSuperUser();
        }
    }    

    /**
     * Test archiving multitude of  Events for one day..
     */
	public void testMassEvent() throws DataObjectException {
        try {
            ThreadContext.pushSuperUser();
			Calendar cal = CalendarUtil.createCalendar();
			TestedArchiver archiver = new TestedArchiver(TestedArchiver.class.getName());
			initTaskLog(archiver);
            int alive = PersonManager.getManager().getAllAlivePersons().size();

            cal.add(Calendar.SECOND, -MASS_EVENTS);
            int expectedCount = MASS_EVENTS * alive;
             
            startTime();
            for (int i=0; i < MASS_EVENTS; i++) {
                createLogEntry(cal.getTime()); 
                cal.add(Calendar.SECOND, 1);
            }
            assertTrue(KBSetup.getKnowledgeBase().commit());

           startTime();
           assertEquals(expectedCount, archiver.archive()); 
           logTime("MassEvents      " + expectedCount + " * " + alive);
        } finally {
            ThreadContext.popSuperUser();
        }
    }    

    /**
     * Test archiving a multitude of Events distributed along a long range in time.
     */
	public void testMassEventSpread() {
        try {
            ThreadContext.pushSuperUser();
			Calendar cal = CalendarUtil.createCalendar();
			TestedArchiver archiver = new TestedArchiver(TestedArchiver.class.getName());
			initTaskLog(archiver);
            List<Person>   alivePersons = PersonManager.getManager().getAllAlivePersons();
            int            alive        = alivePersons.size();

            cal.add(Calendar.HOUR_OF_DAY, -MASS_EVENTS);
            int expectedCount = MASS_EVENTS * alive;
             
            startTime();
            for (int i=0; i < MASS_EVENTS; i++) {
                createLogEntry(cal.getTime()); 
                cal.add(Calendar.HOUR_OF_DAY, 1);
            }
            assertTrue(KBSetup.getKnowledgeBase().commit());

           startTime();
           assertEquals(expectedCount, archiver.archive()); 
           logTime("MassEventSpread " + expectedCount + " * " + alive);
           
           if (MASS_EVENTS > 48) { // must have 24 events for yesterday
               cal.add(Calendar.DAY_OF_YEAR, -1);
               for (Iterator<Person>  it = alivePersons.iterator(); it.hasNext();) {
					Person person = it.next();
					Date when = cal.getTime();
					ArchiveUserDayEntry aude = ArchiveUserDayEntry.getExistingEntry(person, when);
					assertNotNull("No Entry for " + person + " at " + when, aude);
                   List entries = aude.getLogEntries();
                   assertNotNull(entries);
                   assertEquals(24, entries.size());
               }
           }
           
        } finally {
            ThreadContext.popSuperUser();
        }

    }    

    /** Return the suite of Tests to perform */
    public static Test suite () {
		return suite(TestDayEntryArchiver.class);
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        // KBSetup.CREATE_TABLES = false;    
        SHOW_TIME = true;
        Logger.configureStdout();      //  "INFO"
        TestRunner.run (suite ());
    }
}
