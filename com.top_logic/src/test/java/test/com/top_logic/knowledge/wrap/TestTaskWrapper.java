/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Date;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.TaskWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;

/**
 * Testcase for {@link com.top_logic.knowledge.wrap.TaskWrapper}.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestTaskWrapper extends BasicTestCase {

    /**
     * Constructor for TestTaskWrapper.
     * 
     * @param name name of function to execute
     */
    public TestTaskWrapper(String name) {
        super(name);
    }

    /**
     * Test creation (and deletion) of TaskWrappers.
     */
    public void testMain() throws Exception {
        
        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
        TaskWrapper   task  = null;
        
        try {
            task = TestedWrapper.create(theKB, "Once", LegacySchedulesCommon.ONCE, 0, 14, 22);
            assertTrue(theKB.commit());
            task.tDelete();
            assertTrue (theKB.commit());
            assertFalse(task.tValid());

            task = TestedWrapper.create(theKB, "Day", new Date(), 18, 22);
            assertTrue(theKB.commit());
            task.tDelete();
            assertTrue (theKB.commit());
            assertFalse(task.tValid());

            task = TestedWrapper.create(theKB, "Periodic", 
                        LegacySchedulesCommon.DAILY | LegacySchedulesCommon.PERIODICALLY,
                        0, 0,10, 60 * 60 * 1000L, 24 ,60 );
            assertTrue(theKB.commit());
            task.tDelete();
            task = null;
        }
        finally {
            if (task != null && task.tValid())
                task.tDelete();   
            assertTrue(theKB.commit());
        }
    }

    /**
     * Test creation (and deletion) of TaskWrappers.
     */
    public void testEquality() throws Exception {
        
        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
        TaskWrapper   task  = null;
        
        try {
            task = TestedWrapper.create(theKB, "Once", LegacySchedulesCommon.ONCE, 0, 14, 22);
            assertTrue(theKB.commit());
            
            assertEquals("TaskWrapper is not equal to itself.", task, task);
        }
        finally {
            if (task != null && task.tValid())
                task.tDelete();   
            assertTrue(theKB.commit());
        }
    }

	public static class TestedWrapper extends TaskWrapper {
        
        /** Type of KnowledgeObject wrapped by this class */
		public static final String OBJECT_NAME = "TestTask";

		/** Make superclass CTOR acessible for testing.
         */
        public TestedWrapper(KnowledgeObject ko) {
            super(ko);
        }

        /** Make superclass CTOR acessible for testing.
         */
        public static TestedWrapper create(
            KnowledgeBase aKB, String name,
            int daytype, int daymask,
            int hour   , int minute) {
            return (TestedWrapper) WrapperFactory.getWrapper(createKO(aKB, TestedWrapper.OBJECT_NAME, name, daytype, daymask, hour, minute));
        }

        /** Make superclass CTOR acessible for testing.
         */
        public static TestedWrapper create(
            KnowledgeBase aKB, String name,
            Date when,
            int hour, int minute) {
            return (TestedWrapper) WrapperFactory.getWrapper(createKO(aKB, TestedWrapper.OBJECT_NAME, name, when, hour, minute));
        }

        /** Make superclass CTOR acessible for testing.
         */
        public static TestedWrapper create(
            KnowledgeBase aKB, String name,
            int daytype     ,int daymask,
            int startHour   ,int startMinute,
            long interval   ,   
            int stopHour, int stopMinute) {
            return (TestedWrapper) WrapperFactory.getWrapper(createKO(aKB, TestedWrapper.OBJECT_NAME, name,daytype, daymask,
                startHour, startMinute,
                interval, stopHour,stopMinute));
        }
	}

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return KBSetup.getKBTest(TestTaskWrapper.class);
    }
    
}
