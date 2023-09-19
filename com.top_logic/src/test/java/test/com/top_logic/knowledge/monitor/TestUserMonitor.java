/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.monitor;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.monitor.UserMonitor;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Test case for the {@link com.top_logic.knowledge.monitor.UserMonitor}.
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestUserMonitor extends BasicTestCase {

    /** The Number of UserSessions for mass Storage. */
    private static final int COUNT = 100;
	private KnowledgeBase kb;
	private UserMonitor um;

	private Set<String> sessionsBefore;
    
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
		kb = PersistencyLayer.getKnowledgeBase();
		um = UserMonitor.getInstance();
		this.sessionsBefore = Mappings.mapIntoSet(new Mapping<KnowledgeObject, String>() {

			@Override
			public String map(KnowledgeObject input) {
				return KBUtils.getObjectKeyString(input);
			}
		}, getAllUserSessions());
    }

	private Collection<KnowledgeObject> getAllUserSessions() {
		return kb.getAllKnowledgeObjects(UserSession.OBJECT_NAME);
	}
    
    @Override
    protected void tearDown() throws Exception {
		Transaction tx = kb.beginTransaction();
		for (KnowledgeObject userSession : getAllUserSessions()) {
			if (sessionsBefore.contains(KBUtils.getObjectKeyString(userSession))) {
				continue;
			}
			userSession.delete();
		}
		kb = null;
		um = null;
		tx.commit();
    	super.tearDown();
    }

	/**
	 * Testcases using some synthetical dates.
	 */
	public void testFixedDates() throws KnowledgeBaseException {
    
        Date              start  = new Date(1083575486623L);    // 03.05.2004 11:11:26 
        Date              end    = new Date(1083576548662L);    // 03.05.2004 11:29:08
        String            server = UserSession.getServerName();
		Transaction tx = kb.beginTransaction();
		UserSession us = UserSession.startSession(kb, "TestUserSession", "xxxxx", "127.0.0.1", start);
		tx.commit();
        
		assertEquals(us, um.findUserSession(kb, "TestUserSession", "xxxxx", server));
		assertEquals(us, findSessionOnServer(kb, "TestUserSession", "xxxxx"));
		// Wont work since it depends on current Date ...
		// assertInIterator(us, um.getOpenSessionsIterated(kb));
		List<?> theSessions = getUserSessions(kb, start, null);
		assertNotNull("No Sessions for " + kb, theSessions);
		assertTrue(theSessions.contains(us));
		theSessions = getUserSessions(kb, start, UserSession.LOGOUT);
		assertNotNull("No Sessions for " + kb, theSessions);
		assertTrue(theSessions.contains(us));
		endSession(us, end);

		assertNotInIterator(us, um.getOpenSessionsIterated());
	}

	private void endSession(UserSession us, Date end) throws KnowledgeBaseException {
		Transaction tx = kb.beginTransaction();
		us.endSession(end);
		tx.commit();
	}
    
	/** Testcases using some "current" dates. */
	public void testNowDates() throws Exception {
    
        long              now    = System.currentTimeMillis();
        Date              start  = new Date(now - 1000*60*60*10);
        Date              end    = new Date(now - 1000*60*60* 5);
        String            server = UserSession.getServerName();
		Transaction tx = kb.beginTransaction();
        UserSession       us     = UserSession.startSession(kb, 
            "TestUserSession", "xxxxx",  "127.0.0.1", start); 
		String testUserName = TestPersonSetup.USER_ID;
		UserInterface dummy = Person.getUser(PersonManager.getManager().getPersonByName(testUserName));
		assertNotNull("Unable to get user for name " + testUserName, us);
        Sender            sender = new Sender("Testing", "TestUserMonitor");
        UserEvent         login  = new UserEvent(sender, dummy, "yyyyy", server, UserEvent.LOGGED_IN);
        UserEvent         logout = new UserEvent(sender, dummy, "yyyyy", server, UserEvent.LOGGED_OUT);
		tx.commit();

		um.receive(login);

        Thread.sleep(1000);
		// Wait a second due to implementation hack for Oracle evil for MSSQL
		// See UserMonitor.java:324

		assertEquals(us, um.findUserSession("TestUserSession", "xxxxx", server));
		assertEquals(us, findSessionOnServer("TestUserSession", "xxxxx"));
		assertInIterator(us, um.getOpenSessionsIterated());

		Collection sessions = um.getUserSessions();
		assertTrue(sessions.contains(us));
		assertEquals(2, sessions.size());

		endSession(us, end);

		um.receive(logout);

        assertNotInIterator(us, um.getOpenSessionsIterated());
		assertTrue(um.getUserSessions().contains(us));
    }

    /** 
     * Test massive Storage of Sessions. 
     */
	public void testMassStorage() throws Exception {
    
        long    now    = 1083575486623L;
        Random  rand   = new Random(now);
        
        int     delta1 = 1000*60*20;    // average 10 minutes until logout 
        int     delta2 = 1000*60*4;     // average 2  minutes until next Session 
    
        String            server   = UserSession.getServerName();
        Date              allStart = new Date(now);
        Date              start    = null;
        Date              end      = null;
        
		startTime();
		for (int i = 0; i < COUNT; i++) {
			start = new Date(now);
			end = new Date(now + rand.nextInt(delta1));
			Transaction tx = kb.beginTransaction();
			String session = "S" + i;
			UserSession us = UserSession.startSession(kb,
				"TestUserSession", session, "127.0.0.1", start);
			tx.commit();

			assertEquals(us, um.findUserSession(kb, "TestUserSession", session, server));
			assertEquals(us, findSessionOnServer(kb, "TestUserSession", session));
			assertTrue(getUserSessions(kb, start, null).contains(us));
			assertTrue(getUserSessions(kb, start, UserSession.LOGOUT).contains(us));
			endSession(us, end);

			assertTrue("Not contained#1 for " + kb, getUserSessions(kb, start, new Date(now + 1000L), null)
				.contains(us));
			assertTrue("Not contained#2 for " + kb, getUserSessions(kb, start, UserSession.LOGOUT).contains(us));

			assertNotInIterator(us, um.getOpenSessionsIterated());
			now += rand.nextInt(delta2);
        }
		logTime("creating " + COUNT + " Sessions " + kb);
		assertEquals(COUNT, getUserSessions(kb, allStart, null).size());
		assertEquals(COUNT, getUserSessions(kb, allStart, UserSession.LOGOUT).size());
		assertEquals(1, getUserSessions(kb, start, null).size());
		assertEquals(1, getUserSessions(kb, start, UserSession.LOGOUT).size());
		assertEquals(0, getUserSessions(kb, end, null).size());
		assertEquals(0, getUserSessions(kb, end, UserSession.LOGOUT).size());
		logTime("searching " + COUNT + " Sessions " + kb);
    }
    
    /** Return the suite of tests to perform. 
     */
    public static Test suite () {
		TestFactory f = ServiceTestSetup.createStarterFactory(UserMonitor.Module.INSTANCE);
		f = TestPersonSetup.wrap(f);
		return PersonManagerSetup.createPersonManagerSetup(TestUserMonitor.class, f);
    }

    private List getUserSessions(KnowledgeBase aBase, Date aDate, String aSort) {
    	Class<?>[] signature = new Class<?>[]{KnowledgeBase.class, Date.class, String.class};
		Object[] args = new Object[] {aBase,aDate,aSort};
		return ReflectionUtils.executeMethod(um, "getUserSessions", signature, args, List.class);
	}

	private List getUserSessions(KnowledgeBase aBase, Date aStartDate, Date anEndDate, String aSort) {
		Class<?>[] signature = new Class<?>[]{KnowledgeBase.class, Date.class, Date.class, String.class};
		Object[] args = new Object[] {aBase,aStartDate, anEndDate ,aSort};
		return ReflectionUtils.executeMethod(um, "getUserSessions", signature, args, List.class);
	}

	private UserSession findSessionOnServer(KnowledgeBase aBase, String aUser, String anID) {
		Class<?>[] signature = new Class<?>[]{KnowledgeBase.class,String.class, String.class};
		Object[] args = new Object[] {aBase,aUser ,anID};
		return ReflectionUtils.executeMethod(um, "findSessionOnServer", signature, args, UserSession.class);
	}

	private UserSession findSessionOnServer(String aUser, String anID) {
		Class<?>[] signature = new Class<?>[]{String.class, String.class};
		Object[] args = new Object[] {aUser ,anID};
		return ReflectionUtils.executeMethod(um, "findSessionOnServer", signature, args, UserSession.class);
	}


}
