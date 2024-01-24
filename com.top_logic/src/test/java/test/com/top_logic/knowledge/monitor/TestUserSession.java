/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.monitor;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Testcase for the {@link com.top_logic.knowledge.monitor.UserSession}
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestUserSession extends BasicTestCase {

    /**
     * Constructor for TestUserSession.
     */
    public TestUserSession(String name) {
        super(name);
    }

    /** 
     * Test starting a Session and some null parameters 
     */
	public void testStartSession() throws InterruptedException {
        
        KnowledgeBase kb = KBSetup.getKnowledgeBase();
        
        UserSession us = UserSession.startSession(kb, 
            "TestUserSession", "xxxxx",  "127.0.0.1", null); 
        try {
            assertNotNull(us.toString());
            assertSame(us, UserSession.getInstance(us.tHandle()));
            
            Thread.sleep(20);   // othewise duration may be 0
            
            long duration = us.getDuration();

            assertEquals ("TestUserSession"   , us.getUsername());
            assertEquals ("xxxxx"             , us.getSessionId());
            assertNull   (                      us.getPerson());
            assertNotNull(                      us.getLogin());
            assertNull   (                      us.getLogout());
            assertEquals ("127.0.0.1"         , us.getMachine());
            assertNotNull(                      us.getServer());
            assertTrue   ("Duration is " + duration, duration > 0);
            assertFalse  (                      us.isFinished());
        }
        finally {
            kb.rollback();
        }
    }

    /** Test starting and ending Session and some other null parameters. */
    public void testEndSession() {
        KnowledgeBase kb = KBSetup.getKnowledgeBase();
        
        Date theStart = new Date(1083575486623L);
        Date theEnd   = new Date(1083575496622L);
        UserSession us = UserSession.startSession(kb, 
            "guest_de", "SYSTEM_ID", "127.0.0.1", theStart); 
        kb.commit();
        us.endSession(theEnd);
        try {
            ThreadContext.pushSuperUser();
            assertNotNull(us.toString());
            assertSame  (us, UserSession.getInstance(us.tHandle()));
            assertEquals ("guest_de"           , us.getUsername());
            assertEquals ("SYSTEM_ID"          , us.getSessionId());
            assertNotNull(                       us.getPerson());
            assertEquals (theStart             , us.getLogin());
            assertEquals (theEnd               , us.getLogout());
            assertEquals ("127.0.0.1"          , us.getMachine());
            assertNotNull(                       us.getServer());
            assertEquals (9999                 , us.getDuration());
            assertTrue   (                       us.isFinished());
        }
        finally {
            ThreadContext.popSuperUser();
            us.tDelete();
            kb.commit();
        }
    }

    /** Test starting and ending Session and some other null parameters. */
    public void testCleanupUserSessions() {
        KnowledgeBase kb = KBSetup.getKnowledgeBase();
        
        Date theStart = new Date(1083575486623L);
        UserSession us = UserSession.startSession(kb, 
            "dummy", "SYSTEM_ID", "127.0.0.1", theStart); 
        kb.commit();
        try {
            // Previous Testcases may have created UserSesions, mmh
            UserSession.cleanupUserSessions(kb, 150);
            assertNotNull(us.getLogout());
            assertTrue   (us.isFinished());
        }
        finally {
            us.tDelete();
            kb.commit();
        }
    }

    /** 
     * Remove all UserSeeions to have a clean DB.
     */
    public void doRemoveSessions() throws DataObjectException {
        KnowledgeBase kb       = KBSetup.getKnowledgeBase();
		kb.deleteAll(kb.getAllKnowledgeObjects(UserSession.OBJECT_NAME));
        kb.commit();
    }
    
    /** Return the suite of tests to perform. 
     */
    public static Test suite () {
		TestFactory f = new TestFactory() {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase, suiteName);
				suite.addTest(new TestUserSession("doRemoveSessions"));
				return suite;
			}
		};
        
		return PersonManagerSetup.createPersonManagerSetup(TestUserSession.class, f);
    }

    /** Main function for direct testing.
     */
    public static void main (String[] args) {
        
        // SHOW_TIME               = true;
        // SHOW_RESULTS            = true;   // for debugging
        KBSetup.setCreateTables(false);  // for debugging
        
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }


}
