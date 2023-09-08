/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Calendar;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.EMailTask;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;

/**
 * Testcase for {@link com.top_logic.knowledge.wrap.EMailTask}. 
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestEMailTask extends BasicTestCase {

    /**
     * Constructor for TestEMailTask.
     * 
     * @param name name of function to execute
     */
    public TestEMailTask(String name) {
        super(name);
    }

	/**
	 * Test creation (and deletion) of TaskWrappers.
	 */
	public void testMain() throws Exception {

		KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();

		Calendar now = CalendarUtil.createCalendar();
		now.add(Calendar.MINUTE, 1); // 1 Minute ahead
		Transaction createTaskTx = kb.beginTransaction();
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		EMailTask mailTask = EMailTask.create(kb, "EMail Once", LegacySchedulesCommon.ONCE, 0, hour, minute);
		mailTask.setActive(true);
		mailTask.setUsers("no such User"); // make the mail a Noop by using invalid user
		mailTask.setSubject("Testing EMailTask");
		mailTask.setMessage("The Message is as follows ...");
		createTaskTx.commit();

		try {
			Scheduler sched = Scheduler.getSchedulerInstance();
			sched.addTask(mailTask);
			try {
				// Sleep two minutes so Mailtask was actually executed.
				long nextShed = mailTask.getNextShed();
				assertTrue("Task not scheduled ?", nextShed != SchedulingAlgorithm.NO_SCHEDULE);
				long delta = (nextShed - System.currentTimeMillis()) + sched.getPollingInterval() + 5000L;
				if (delta > 5 * 60 * 1000) {
					fail("If next schedule exists, it should be in less than 5 minutes.");
				}
				// System.out.println("Sleeping " + DebugHelper.getTime(delta));
				Logger.configureStdout("FATAL");
				Thread.sleep(delta);
				// The error Logged is quite OK
				assertTrue("Task not scheduled ?", mailTask.getNextShed() == SchedulingAlgorithm.NO_SCHEDULE);
				Logger.configureStdout();
			} finally {
				sched.removeTask(mailTask);
			}
		} finally {
			if (mailTask.tValid()) {
				Transaction deleteTaskTx = kb.beginTransaction();
				mailTask.tDelete();
				deleteTaskTx.commit();
			}
		}
	}

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		final Test innerTest =
			ServiceTestSetup.createSetup(TestEMailTask.class, MailSenderService.Module.INSTANCE, Scheduler.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
    }

    /**
     * Allow execution as main fucntion.
     */
    public static void main(String[] args) {
        // SHOW_TIME             = true;     // for debugging
        // KBSetup.CREATE_TABLES = false;    // for debugging
        
        Logger.configureStdout(); // "INFO"
        
        junit.textui.TestRunner.run(suite());
    }

}
