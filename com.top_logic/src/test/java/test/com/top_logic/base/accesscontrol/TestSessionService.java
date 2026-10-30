/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.monitor.UserSessionCleanup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.Resources;


/**
 * Test class for {@link com.top_logic.base.accesscontrol.SessionService}
 *
 * @author     <a href="mailto:mvo@top-logic.com">Michael Vogt</a>
 */
public class TestSessionService extends BasicTestCase {

	private UserSessionCleanup _userSessions;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_userSessions = UserSessionCleanup.snapshot(PersistencyLayer.getKnowledgeBase());
	}

	@Override
	protected void tearDown() throws Exception {
		// The logins of this test are recorded as user sessions, which must not be seen by later
		// tests that share the knowledge base.
		_userSessions.deleteCreated();
		super.tearDown();
	}

    public TestSessionService (String aName) {
        super (aName);
    }

    public void testAddRemoveSession () throws Exception {
        
        ServletRunner		sr = new ServletRunner();
		sr.registerServlet("lpServlet", TLLayoutServlet.class.getName());

        ServletUnitClient   sc = sr.newClient();

        WebRequest  myRequest  = new PostMethodWebRequest( "http://ignore/lpServlet" );

        InvocationContext   ic = sc.newInvocation(myRequest);

		HttpServletRequest servletRequest = ic.getRequest();

		ThreadContext.withoutSecurityChecks(() -> {
			HttpServletResponse response = ic.getResponse();
			SessionService myService;
			LoginCredentials login =
				LoginCredentials.fromUserAndPassword(PersonManager.getManager().getRoot(),
					SecureRandomService.getInstance().getRandomString().toCharArray());
			try {
				try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
					login.getPerson().getAuthenticationDevice().setPassword(login.getPerson(), login.getPassword());
					tx.commit();
				}

				boolean checkLoginCredentials = Login.getInstance().checkLoginCredentials(login, servletRequest, response);
				assertTrue(checkLoginCredentials);
				myService = SessionService.getInstance();
				myService.loginUser(servletRequest, response, login.getPerson());
			} finally {
				login.clearPassword();
			}
            
    		
    		assertTrue(myService.validateSession(servletRequest));
    		
			final Collection<String> sessionIDsAfterLogin = myService.getSessionIDs();
			assertTrue("expected there is a current session",
				sessionIDsAfterLogin.contains(servletRequest.getSession(false).getId()));
			assertEquals("expected there is exactly one session", 1, sessionIDsAfterLogin.size());
			
			myService.invalidateSession(myService.getSession(servletRequest));
    		
			final Collection<String> sessionIDsAfterLogout = myService.getSessionIDs();
			assertTrue("expected there is a no session when session was invalided", sessionIDsAfterLogout.isEmpty());
			return null;
		});
    }

	/**
	 * Tests that {@link SessionService#terminateSession(String)} ends the {@link HttpSession}.
	 */
	public void testTerminateSession() throws Exception {
		SessionService service = SessionService.getInstance();
		HttpSession session = login(PersonManager.getManager().getRoot());
		String sessionId = session.getId();
		assertTrue(service.getSessionIDs().contains(sessionId));

		service.terminateSession(sessionId);

		assertFalse(service.getSessionIDs().contains(sessionId));
		assertInvalidated(session);

		// Terminating an unknown session has no effect.
		service.terminateSession(sessionId);
	}

	/**
	 * Tests that {@link SessionService#terminateSessions(Person)} ends only the sessions of the
	 * given user.
	 */
	public void testTerminateSessionsOfUser() throws Exception {
		SessionService service = SessionService.getInstance();
		Person user = createPerson("sessionUser1");
		Person other = createPerson("sessionUser2");
		try {
			HttpSession userSession1 = login(user);
			HttpSession userSession2 = login(user);
			HttpSession otherSession = login(other);
			String otherSessionId = otherSession.getId();

			service.terminateSessions(user);

			assertInvalidated(userSession1);
			assertInvalidated(userSession2);
			assertTrue(service.getSessionIDs().contains(otherSessionId));

			service.terminateSession(otherSessionId);
		} finally {
			deleteIfValid(user);
			deleteIfValid(other);
		}
	}

	/**
	 * Tests that deleting a {@link Person} terminates its sessions and records the logout, while
	 * the sessions of other users are kept.
	 */
	public void testDeletingPersonTerminatesItsSessions() throws Exception {
		SessionService service = SessionService.getInstance();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		String deletedName = "deletedSessionUser";
		Person deleted = createPerson(deletedName);
		Person other = createPerson("remainingSessionUser");
		try {
			HttpSession deletedSession = login(deleted);
			String deletedSessionId = deletedSession.getId();
			HttpSession otherSession = login(other);
			String otherSessionId = otherSession.getId();
			assertNotNull("Login not recorded.", UserSession.findUserSession(kb, deletedName, deletedSessionId));

			try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
				deleted.tDelete();
				tx.commit();
			}

			// The session is terminated in the background. It is invalidated after the logout has
			// been announced (and recorded).
			long timeout = System.currentTimeMillis() + 10000;
			while (isValid(deletedSession)) {
				assertTrue("Session of deleted user not terminated.", System.currentTimeMillis() < timeout);
				Thread.sleep(20);
			}

			assertFalse(service.getSessionIDs().contains(deletedSessionId));
			// Make the logout recorded in the background visible to this thread.
			kb.getHistoryManager().updateSessionRevision();
			assertNull("Logout not recorded.", UserSession.findUserSession(kb, deletedName, deletedSessionId));
			assertTrue("Session of other user must be kept.", service.getSessionIDs().contains(otherSessionId));

			service.terminateSession(otherSessionId);
		} finally {
			deleteIfValid(deleted);
			deleteIfValid(other);
		}
	}

	/**
	 * Tests that {@link SessionService#loginUserInWindow(HttpServletRequest, HttpServletResponse, Person)}
	 * logs in only the current window, keeps the session, changes the session ID, and ends the login
	 * record of the previous user under the former ID.
	 */
	public void testLoginUserInWindow() throws Exception {
		SessionService service = SessionService.getInstance();
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		String userName = "windowLoginUser";
		String guestName = "windowLoginGuest";
		Person guest = createPerson(guestName);
		Person user = createPerson(userName);
		try {
			InvocationContext invocation = newInvocation();
			HttpServletRequest request = invocation.getRequest();
			HttpSession session = ThreadContext.inSystemContext(TestSessionService.class,
				() -> service.loginUser(request, invocation.getResponse(), guest));
			String oldId = session.getId();
			TLSessionContext sessionContext = service.getSession(session);
			TLSubSessionContext window = newWindow(sessionContext, "windowA", guest);
			TLSubSessionContext otherWindow = newWindow(sessionContext, "windowB", guest);
			assertNotNull("Guest login not recorded.", UserSession.findUserSession(kb, guestName, oldId));

			HttpSession result = ThreadContextManager.inContext(window,
				() -> service.loginUserInWindow(request, invocation.getResponse(), user));

			assertSame("Session must be kept.", session, result);
			assertSame("Session must be kept.", session, request.getSession(false));
			assertSame("Session context must be kept.", sessionContext, service.getSession(session));
			String newId = session.getId();
			assertNotEquals("Session ID not changed.", oldId, newId);
			assertFalse("Former session ID still registered.", service.getSessionIDs().contains(oldId));
			assertNull(service.getUser(oldId));
			assertTrue("Changed session ID not registered.", service.getSessionIDs().contains(newId));
			assertEquals(user, service.getUser(newId));
			assertTrue(service.validateSession(request));

			assertEquals(user, window.getPerson());
			assertEquals(Resources.findBestLocale(user), window.getCurrentLocale());
			assertEquals("Other window must keep its user.", guest, otherWindow.getPerson());
			assertEquals(user, sessionContext.getOriginalUser());
			assertSame(window, sessionContext.getSubSession("windowA"));
			assertSame(otherWindow, sessionContext.getSubSession("windowB"));

			kb.getHistoryManager().updateSessionRevision();
			assertNull("Guest login record under the former ID not ended.",
				UserSession.findUserSession(kb, guestName, oldId));
			assertNotNull("Login not recorded.", UserSession.findUserSession(kb, userName, newId));

			service.terminateSession(newId);
			assertInvalidated(session);
		} finally {
			deleteIfValid(user);
			deleteIfValid(guest);
		}
	}

	/**
	 * Tests that {@link SessionService#loginUserInWindow(HttpServletRequest, HttpServletResponse, Person)}
	 * creates a session, if the request has none.
	 */
	public void testLoginUserInWindowWithoutSession() throws Exception {
		SessionService service = SessionService.getInstance();
		InvocationContext invocation = newInvocation();
		HttpServletRequest request = invocation.getRequest();
		Person root = PersonManager.getManager().getRoot();

		HttpSession session = ThreadContext.inSystemContext(TestSessionService.class,
			() -> service.loginUserInWindow(request, invocation.getResponse(), root));

		String sessionId = session.getId();
		assertEquals(root, service.getUser(sessionId));
		assertEquals(root, service.getSession(session).getOriginalUser());

		service.terminateSession(sessionId);
	}

	private static TLSubSessionContext newWindow(TLSessionContext session, String name, Person person) {
		TLSubSessionContext result =
			(TLSubSessionContext) ThreadContextManager.getManager().newSubSessionContext();
		result.setSessionContext(session);
		result = session.setIfAbsent(name, result);
		result.setPerson(person);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		result.updateSessionRevision(kb.getHistoryManager(),
			((UpdateChainView) kb.getUpdateChain()).current());
		return result;
	}

	private static InvocationContext newInvocation() throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		return runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
	}

	private static void assertInvalidated(HttpSession session) {
		assertFalse("Session was not invalidated.", isValid(session));
	}

	private static boolean isValid(HttpSession session) {
		try {
			session.getCreationTime();
			return true;
		} catch (IllegalStateException ex) {
			return false;
		}
	}

	private static HttpSession login(Person user) throws Exception {
		InvocationContext invocation = newInvocation();
		// Keep the session context of the test thread.
		return ThreadContext.inSystemContext(TestSessionService.class,
			() -> {
				return SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user);
			});
	}

	private static Person createPerson(String name) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, name,
				PersonManager.getManager().getRoot().getAuthenticationDevice());
			tx.commit();
			return result;
		}
	}

	private static void deleteIfValid(Person person) {
		if (!person.tValid()) {
			return;
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			person.tDelete();
			tx.commit();
		}
	}

    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
        Test innerTest = new TestSuite (TestSessionService.class);
		innerTest =
			ServiceTestSetup.createSetup(innerTest, SessionService.Module.INSTANCE,
				Login.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
        
    }

}
