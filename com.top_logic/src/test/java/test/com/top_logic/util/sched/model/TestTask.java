/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched.model;

import java.util.Calendar;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.schedule.AlwaysSchedule;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;
import com.top_logic.util.sched.task.schedule.legacy.LegacyOnceSchedule;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;

/**
 * Tests for {@link com.top_logic.util.sched.task.Task} class.
 * 
 * The Test in here may fail when executed in some other TimeZone
 * than CET, CEST may even break them.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestTask extends TestCase {

    /**
     * Constructor for TestTask.
     * 
     * @param name method name of the test to execute.
     */
    public TestTask(String name) {
        super(name);
    }

	public void testSchedulingAlgorithmNoFurtherRun() throws ConfigurationException {
		Task task =
			TaskTestUtil.createTaskImpl("SchedulingAlgorithm Test Task", TaskImpl.class, null);

		long nextSchedule = task.calcNextShed(CalendarUtil.createCalendar().getTimeInMillis());

		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, nextSchedule);
		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, task.getNextShed());
		assertEquals(Maybe.none(), task.getNextSchedule());
	}

	public void testSchedulingAlgorithmNormalNextRun() throws ConfigurationException {
		PolymorphicConfiguration<? extends SchedulingAlgorithm> schedule =
			TypedConfiguration.newConfigItem(AlwaysSchedule.Config.class);
		Task task =
			TaskTestUtil.createTaskImpl("SchedulingAlgorithm Test Task", TaskImpl.class, schedule);
		Calendar currentTime = CalendarUtil.createCalendar();

		long nextSchedule = task.calcNextShed(currentTime.getTimeInMillis());

		assertEquals(currentTime.getTimeInMillis(), nextSchedule);
		assertEquals(currentTime.getTimeInMillis(), task.getNextShed());
		assertEquals(Maybe.some(currentTime), task.getNextSchedule());
	}

	/** Testcase for a Thread executed ONCE ... */
    public void testOnce() {
        
        // The name of this variable is for chickenish reasons ;-)
		Calendar highHuhn = newCalendar(2003, Calendar.JANUARY, 1, 12, 0);
		Calendar nextDay = newCalendar(2003, Calendar.JANUARY, 2, 12, 0);
        
        // This task should execute 14:22 the same day        
        Task once = new TaskImpl("Once", LegacySchedulesCommon.ONCE, 0, 14, 22);
		long next = once.calcNextShed(highHuhn.getTimeInMillis());
        String check = once.toString();
        assertNotNull(check);
        // System.out.println(check);
        
		assertEquals(newCalendar(2003, Calendar.JANUARY, 1, 14, 22).getTimeInMillis(), next);
        once.run();
		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, once.calcNextShed(nextDay.getTimeInMillis()));

        // This taks should execute 09:59 the next day  
        Properties prop = new Properties();
        prop.setProperty("name"   , "Once");
        prop.setProperty("daytype", "ONCE");
        prop.setProperty("hour"   , "9");
        prop.setProperty("minute" , "59");
        
        once = new TaskImpl(prop);
		next = once.calcNextShed(highHuhn.getTimeInMillis());
        check = once.toString();
        assertNotNull(check);
        // System.out.println(check);

		assertEquals(newCalendar(2003, Calendar.JANUARY, 2, 9, 59).getTimeInMillis(), next);
        once.run();
		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, once.calcNextShed(nextDay.getTimeInMillis()));
        
		assertEquals(LegacyOnceSchedule.class, once.getSchedulingAlgorithm().getClass());

        assertEquals("Once" , once.getName());
		assertEquals(9, ReflectionUtils.getValue(once.getSchedulingAlgorithm(), "hour"));
		assertEquals(59, ReflectionUtils.getValue(once.getSchedulingAlgorithm(), "minute"));

    } 

    /** Testcase for a Thread to be executed at a specific DATE ... */
    public void testDate() {
        
		Calendar today = newCalendar(2003, Calendar.FEBRUARY, 27, 14, 44);
		Calendar someDate = newCalendar(2003, Calendar.FEBRUARY, 27, 15, 33);
		Calendar tomorrow = newCalendar(2003, Calendar.MARCH, 1, 14, 44);
		Calendar later = newCalendar(2003, Calendar.MARCH, 2, 14, 44);
        
        // This task should execute 15:33 today    
        Task day = new TaskImpl("Day", someDate.getTime(), 15, 33);
		long next = day.calcNextShed(today.getTimeInMillis());
        String check = day.toString();
        assertNotNull(check);
        // System.out.println(check);
        
		assertEquals(newCalendar(2003, Calendar.FEBRUARY, 27, 15, 33).getTimeInMillis(), next);
        day.run();  
		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, day.calcNextShed(tomorrow.getTimeInMillis()));

        day = new TaskImpl("Day", someDate.getTime(), 15, 33);
		day.run();

		// This task should not execute its to late!
		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, day.calcNextShed(tomorrow.getTimeInMillis()));
        // later is to late, too.    
		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, day.calcNextShed(later.getTimeInMillis()));

		// This taks should be execute 2003-03-01 15:57
        Properties prop = new Properties();
        prop.setProperty("name"   , "Day");
        prop.setProperty("daytype", "DATE");
        prop.setProperty("when"   , "2003-03-01");
        prop.setProperty("hour"   , "15");
        prop.setProperty("minute" , "57");
        
        day = new TaskImpl(prop);
		next = day.calcNextShed(today.getTimeInMillis());
        check = day.toString();
        assertNotNull(check);
        // System.out.println(check);
        
		assertEquals(newCalendar(2003, Calendar.MARCH, 1, 15, 57).getTimeInMillis(), next);
        day.run(); 
		assertEquals(SchedulingAlgorithm.NO_SCHEDULE, day.calcNextShed(tomorrow.getTimeInMillis()));
    } 

    /** Testcase for a Thread executed MONTHLY ... */
    public void testMonthly() {
        
		Calendar firstOfMonth = newCalendar(1999, Calendar.JANUARY, 1, 12, 0);
		Calendar thirdOfMonth = newCalendar(1999, Calendar.JANUARY, 3, 12, 0);
		Calendar tenthOfMonth = newCalendar(1999, Calendar.JANUARY, 10, 12, 0);
        
        // This taks should execute every third and tenth day of a month
        Task monthly = new TaskImpl("Monthly", LegacySchedulesCommon.MONTHLY, 
                                1 << (3-1) | 1 << (10-1), 11, 30);
		long next = monthly.calcNextShed(firstOfMonth.getTimeInMillis());
		long tmp = newCalendar(1999, Calendar.JANUARY, 3, 11, 30).getTimeInMillis();
		assertEquals(tmp, next);
		monthly.markAsRun(tmp);
		next = monthly.calcNextShed(thirdOfMonth.getTimeInMillis());
		tmp = newCalendar(1999, Calendar.JANUARY, 10, 11, 30).getTimeInMillis();
		assertEquals(tmp, next); // Sun Jan 10 11:30:00
		monthly.markAsRun(tmp);
		next = monthly.calcNextShed(tenthOfMonth.getTimeInMillis());
		tmp = newCalendar(1999, Calendar.FEBRUARY, 3, 11, 30).getTimeInMillis();
		assertEquals(tmp, next); // Wed Feb 03 11:30:00

    } 

    /** Testcase for a Thread executed WEEKly ... */
    public void testWeekly() {
        
		Calendar saturday = newCalendar(2010, Calendar.FEBRUARY, 27, 12, 0);
		Calendar monday = newCalendar(2010, Calendar.MARCH, 1, 12, 0);
		Calendar wendnesday = newCalendar(2010, Calendar.MARCH, 3, 12, 0);
        
        // This taks should execute every Monday and Wednesday 11:30
        Properties prop = new Properties();
        prop.setProperty("name"   , "Weakly");
        prop.setProperty("daytype", "WEEKLY");
        prop.setProperty("daymask", "1,3");
        prop.setProperty("hour"   , "11");
        prop.setProperty("minute" , "30");

        Task weekly = new TaskImpl(prop);
        
        // new Task("Weakly", Task.WEEKLY, 1 << 1 | 1 << 3, 11, 30);
		long next = weekly.calcNextShed(saturday.getTimeInMillis());
        // System.out.println(weekly);
		long tmp = newCalendar(2010, Calendar.MARCH, 1, 11, 30).getTimeInMillis();
		assertEquals(tmp, next);
		weekly.markAsRun(tmp);
		next = weekly.calcNextShed(monday.getTimeInMillis());
        // System.out.println(weekly);
		tmp = newCalendar(2010, Calendar.MARCH, 3, 11, 30).getTimeInMillis();
		assertEquals(tmp, next);
		weekly.markAsRun(tmp);
		next = weekly.calcNextShed(wendnesday.getTimeInMillis());
        // System.out.println(weekly);
		tmp = newCalendar(2010, Calendar.MARCH, 8, 11, 30).getTimeInMillis();
		assertEquals(tmp, next);
    } 

	/** Testcase for a Thread executed DAYly ... */
    public void testDaily() {
        
		Calendar last = newCalendar(1999, Calendar.DECEMBER, 31, 0, 10);
		Calendar first = newCalendar(2001, Calendar.JANUARY, 1, 0, 10);
		Calendar second = newCalendar(2001, Calendar.MARCH, 3, 0, 10);
        
        // This taks should execute every Day at 0:00 (not 24:00 ;-)
        Task daily = new TaskImpl("Daily", LegacySchedulesCommon.DAILY,0, 0, 0);
		long next = daily.calcNextShed(last.getTimeInMillis());
        
		int schedulingWindowLength = 0;
        // System.out.println(daily);
		assertEquals(last.getTimeInMillis() - schedulingWindowLength, next);
		daily.markAsRun(newCalendar(1999, Calendar.DECEMBER, 31, 0, 10).getTimeInMillis());
		next = daily.calcNextShed(first.getTimeInMillis());
        // System.out.println(daily);
		long tmp = newCalendar(2000, Calendar.JANUARY, 1, 0, 0).getTimeInMillis();
		assertEquals(tmp, next);
		daily.markAsRun(tmp);
		next = daily.calcNextShed(first.getTimeInMillis());
        // System.out.println(daily);
		tmp = newCalendar(2000, Calendar.JANUARY, 2, 0, 0).getTimeInMillis();
		assertEquals(tmp, next);
		daily.markAsRun(tmp);
		next = daily.calcNextShed(second.getTimeInMillis());
        // System.out.println(daily);
		tmp = newCalendar(2000, Calendar.JANUARY, 3, 0, 0).getTimeInMillis();
		assertEquals(tmp, next);

    } 

    /** Testcase for a Task executed DAILY | PERIODICALLY ... */
    public void testDailyPeriodically() {
        
		Calendar last = newCalendar(1999, Calendar.DECEMBER, 31, 23, 55);
		Calendar first = newCalendar(2001, Calendar.JANUARY, 1, 0, 10);
		Calendar second = newCalendar(2001, Calendar.JANUARY, 1, 1, 10);
        
        // This taks should execute every hour
        Task periodic = new TaskImpl("Periodic", LegacySchedulesCommon.DAILY | LegacySchedulesCommon.PERIODICALLY,
            0, 0,10, 60 * 60 * 1000L, 23 ,50 );
		long next = periodic.calcNextShed(last.getTimeInMillis());
        // System.out.println(periodic);
		assertEquals(newCalendar(2000, Calendar.JANUARY, 1, 0, 10).getTimeInMillis(), next);
        periodic = new TaskImpl("Periodic", LegacySchedulesCommon.DAILY | LegacySchedulesCommon.PERIODICALLY,
            0, 0,10, 60 * 60 * 1000L, 24 ,60 );
		next = periodic.calcNextShed(first.getTimeInMillis());
        // System.out.println(periodic);
		long tmp = newCalendar(2001, Calendar.JANUARY, 1, 0, 10).getTimeInMillis();
		assertEquals(tmp, next);
		periodic.markAsRun(tmp);
		next = periodic.calcNextShed(second.getTimeInMillis());
        // System.out.println(periodic);
		tmp = newCalendar(2001, Calendar.JANUARY, 1, 1, 10).getTimeInMillis();
		assertEquals(tmp, next);
		periodic.markAsRun(tmp);
		next = periodic.calcNextShed(second.getTimeInMillis());
        // System.out.println(periodic);
		assertEquals(newCalendar(2001, Calendar.JANUARY, 1, 2, 10).getTimeInMillis(), next);
    } 

    /** Testcase for a Task executed DAILY | PERIODICALLY for some time. */
    public void testAroundTheClock() {
        
		int schedulingWindowLength = 0;
		Calendar cal = newCalendar(1999, Calendar.DECEMBER, 31, 12, 00);
        int      hours = 24 * 7 * 60;  // Will end at: Wed Jan 05 17:59:15 CET 2000
        // long  diff  = hours * 10L * 60 * 1000; // ten minutes difference.  
        // This taks should execute every hour
        Task periodic = new TaskImpl("Periodic", LegacySchedulesCommon.DAILY | LegacySchedulesCommon.PERIODICALLY,
            0, 0,0, 45 * 1000L, 24 ,00 );

        for (int i=0; i < hours; i++) {
			long next = periodic.calcNextShed(cal.getTimeInMillis());
            // System.out.println(periodic);
            // System.out.println(cal.getTime());
            long expected = cal.getTimeInMillis();
			assertEquals(next, expected - schedulingWindowLength);
            cal.add(Calendar.SECOND, 45);
        }
        
    } 

    /** Testcase for a Task executed WEEKLY | PERIODICALLY ... */
    public void testWeeklyPeriodically() {
        
		Calendar last = newCalendar(2004, Calendar.NOVEMBER, 30, 0, 0);
		Calendar first = newCalendar(2004, Calendar.JANUARY, 1, 0, 10);
		Calendar second = newCalendar(2004, Calendar.JANUARY, 1, 1, 10);
        
        // This taks should execute every Saturday twice at 0:00 and 12:00 
        Task periodic = new TaskImpl("Periodic", LegacySchedulesCommon.WEEKLY | LegacySchedulesCommon.PERIODICALLY,
            1 << 6, 0,0, 12* 60 * 60 * 1000L, 13 ,0 );
		long next = periodic.calcNextShed(last.getTimeInMillis());
        // System.out.println(periodic);
		long tmp = newCalendar(2004, Calendar.DECEMBER, 4, 0, 0).getTimeInMillis();
		assertEquals(tmp, next);
		periodic.markAsRun(tmp);
		next = periodic.calcNextShed(first.getTimeInMillis());
        // System.out.println(periodic);
		tmp = newCalendar(2004, Calendar.DECEMBER, 4, 12, 0).getTimeInMillis();
		assertEquals(tmp, next);
		periodic.markAsRun(tmp);
		next = periodic.calcNextShed(second.getTimeInMillis());
        // System.out.println(periodic);
		tmp = newCalendar(2004, Calendar.DECEMBER, 11, 0, 0).getTimeInMillis();
		assertEquals(tmp, next);
    } 

    /** Testcase for a Task executed MONTHLY | PERIODICALLY ... */
    public void testMonthlyPeriodically() {
        
		Calendar cal1 = newCalendar(2002, Calendar.NOVEMBER, 30, 0, 0);
		Calendar cal2 = newCalendar(2002, Calendar.DECEMBER, 1, 0, 0);
		Calendar cal3 = newCalendar(2002, Calendar.DECEMBER, 1, 8, 0);
		Calendar cal4 = newCalendar(2003, Calendar.DECEMBER, 1, 16, 0);
        
        // This taks should execute every 2nd of a month    
        // at 0:00 and 8:00 and 16:00  
        Task periodic = new TaskImpl("Periodic", LegacySchedulesCommon.MONTHLY | LegacySchedulesCommon.PERIODICALLY,
            1 << 1, 0,0, 8 * 60 * 60 * 1000L, 17 ,0 );
		long next = periodic.calcNextShed(cal1.getTimeInMillis());
        // System.out.println(periodic);
		long tmp = newCalendar(2002, Calendar.DECEMBER, 2, 0, 0).getTimeInMillis();
		assertEquals(tmp, next);
		periodic.markAsRun(tmp);
		next = periodic.calcNextShed(cal2.getTimeInMillis());
        // System.out.println(periodic);
		tmp = newCalendar(2002, Calendar.DECEMBER, 2, 8, 0).getTimeInMillis();
		assertEquals(tmp, next);
		periodic.markAsRun(tmp);
		next = periodic.calcNextShed(cal3.getTimeInMillis());
        // System.out.println(periodic);
		tmp = newCalendar(2002, Calendar.DECEMBER, 2, 16, 0).getTimeInMillis();
		assertEquals(tmp, next);
		periodic.markAsRun(tmp);
		next = periodic.calcNextShed(cal4.getTimeInMillis());
        // System.out.println(periodic);
		tmp = newCalendar(2003, Calendar.JANUARY, 2, 0, 0).getTimeInMillis();
		assertEquals(tmp, next);
    } 


	private Calendar newCalendar(int year, int month, int date, int hourOfDay, int minute) {
		Calendar cal = CalendarUtil.createCalendar();
		cal.clear();
		cal.set(year, month, date, hourOfDay, minute);
		return cal;
	}

	/** Test Creation of Tasks via Properties */
    public void testProperties() {
        
        Properties prop = new Properties();
        prop.setProperty("name"         ,  "PropertiesTask");
        prop.setProperty("daytype"      ,  "ONCE");
        prop.setProperty("timetype"     , "PERIODICALLY");
        prop.setProperty("startHour"    , "10");
        prop.setProperty("startMinute"  , "30");
        prop.setProperty("interval"     , "134567");
        prop.setProperty("stopHour"     , "21");
        prop.setProperty("stopMinute"   , "22");
        
        TaskImpl task = new TaskImpl(prop);
        assertEquals("PropertiesTask", task.getName());
        
		assertEquals(LegacyOnceSchedule.class, task.getSchedulingAlgorithm().getClass());

		assertEquals(10, ReflectionUtils.getValue(task.getSchedulingAlgorithm(), "hour"));
		assertEquals(30, ReflectionUtils.getValue(task.getSchedulingAlgorithm(), "minute"));
    } 

    /** Test Creation of Tasks via Properties */
    public void testDefaultCtor() {
		String name = ExampleTask.class.getName();
		TaskImpl task = new ExampleTask(name);
		assertEquals(name, task.getName());
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestTask.class, Scheduler.Module.INSTANCE));
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
