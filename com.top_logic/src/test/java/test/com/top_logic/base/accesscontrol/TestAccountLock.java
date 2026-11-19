/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.monitor.UserSessionCleanup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.base.accesscontrol.I18NConstants;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.AccountLockedException;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Test of the {@link Person#isLocked() account lock}: a locked account cannot log in, and locking
 * an account ends its sessions.
 */
public class TestAccountLock extends BasicTestCase {

	private static final String PASSWORD = "lock-Test-1234";

	private static final String WRONG_PASSWORD = "wrong-Test-1234";

	private UserSessionCleanup _userSessions;

	private Person _account;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_userSessions = UserSessionCleanup.snapshot(kb());
		_account = ThreadContext.inSystemContext(TestAccountLock.class, () -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				Person result = Person.create(kb(), "lockedAccountUser",
					PersonManager.getManager().getRoot().getAuthenticationDevice());
				result.getAuthenticationDevice().setPassword(result, PASSWORD.toCharArray());
				tx.commit();
				return result;
			}
		});
	}

	@Override
	protected void tearDown() throws Exception {
		if (_account.tValid()) {
			ThreadContext.inSystemContext(TestAccountLock.class, () -> {
				try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
					_account.tDelete();
					tx.commit();
				}
			});
		}
		_userSessions.deleteCreated();
		super.tearDown();
	}

	/**
	 * A locked account with the correct password is refused with the reason that it is locked.
	 */
	public void testLockedAccountWithCorrectPassword() throws Exception {
		setLocked(true);
		try {
			checkUserPassword(PASSWORD);
			fail("Login of a locked account must be refused.");
		} catch (AccountLockedException ex) {
			assertEquals(I18NConstants.ERROR_ACCOUNT_LOCKED, ex.getReason());
		}
	}

	/**
	 * A locked account with a wrong password gets the same refusal as an unlocked one, which does
	 * not disclose the lock.
	 */
	public void testLockedAccountWithWrongPassword() throws Exception {
		setLocked(true);
		assertFalse(checkUserPassword(WRONG_PASSWORD));
	}

	/**
	 * An account that is not locked logs in, and unlocking a locked account restores its login.
	 */
	public void testUnlockRestoresLogin() throws Exception {
		assertFalse(_account.isLocked());
		assertTrue(checkUserPassword(PASSWORD));

		setLocked(true);
		try {
			checkUserPassword(PASSWORD);
			fail("Login of a locked account must be refused.");
		} catch (AccountLockedException ex) {
			// Expected.
		}

		setLocked(false);
		assertTrue(checkUserPassword(PASSWORD));
		Login.getInstance().checkMayLogIn(_account);
	}

	/**
	 * {@link SessionService#loginUser(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Person)}
	 * refuses a locked account, whichever way the login was reached.
	 */
	public void testSessionServiceRefusesLockedAccount() throws Exception {
		setLocked(true);
		InvocationContext invocation = newInvocation();
		try {
			ThreadContext.inSystemContext(TestAccountLock.class, () -> {
				SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), _account);
			});
			fail("Session login of a locked account must be refused.");
		} catch (AccountLockedException ex) {
			assertNull("No session must be created.", invocation.getRequest().getSession(false));
		}
	}

	/**
	 * Locking an account ends its running sessions, while the sessions of other accounts are kept.
	 */
	public void testLockingTerminatesSessions() throws Exception {
		SessionService service = SessionService.getInstance();
		HttpSession lockedSession = login(_account);
		HttpSession otherSession = login(PersonManager.getManager().getRoot());
		String otherSessionId = otherSession.getId();
		try {
			setLocked(true);

			// The session is terminated in the background.
			long timeout = System.currentTimeMillis() + 10000;
			while (isValid(lockedSession)) {
				assertTrue("Session of locked account not terminated.", System.currentTimeMillis() < timeout);
				Thread.sleep(20);
			}
			assertTrue("Session of other account must be kept.", service.getSessionIDs().contains(otherSessionId));
		} finally {
			service.terminateSession(otherSessionId);
		}
	}

	private boolean checkUserPassword(String password) throws Exception {
		InvocationContext invocation = newInvocation();
		return ThreadContext.inSystemContext(TestAccountLock.class, () -> {
			return Login.getInstance().checkUserPassword(_account.getName(), password.toCharArray(),
				invocation.getRequest(), invocation.getResponse());
		});
	}

	private void setLocked(boolean locked) {
		ThreadContext.inSystemContext(TestAccountLock.class, () -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				_account.setLocked(locked);
				tx.commit();
			}
		});
		assertEquals(locked, _account.isLocked());
	}

	private static HttpSession login(Person user) throws Exception {
		InvocationContext invocation = newInvocation();
		return ThreadContext.inSystemContext(TestAccountLock.class, () -> {
			return SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user);
		});
	}

	private static InvocationContext newInvocation() throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		return runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
	}

	private static boolean isValid(HttpSession session) {
		try {
			session.getCreationTime();
			return true;
		} catch (IllegalStateException ex) {
			return false;
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestAccountLock.class);
		innerTest = ServiceTestSetup.createSetup(innerTest, SessionService.Module.INSTANCE, Login.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}
