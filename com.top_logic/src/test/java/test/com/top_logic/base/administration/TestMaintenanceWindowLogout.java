/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.administration;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.monitor.UserSessionCleanup;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContextManager;

/**
 * Test for the sessions ended when the {@link MaintenanceWindowManager} enters the maintenance
 * window.
 */
@SuppressWarnings("javadoc")
public class TestMaintenanceWindowLogout extends BasicTestCase {

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

	/**
	 * Entering the maintenance window immediately ends the sessions of users that may not log in
	 * during the maintenance window, but keeps the session that switches the maintenance window on
	 * and the sessions of users that may log in during the maintenance window.
	 */
	public void testEnterMaintenanceWindow() throws Exception {
		doTestEnterMaintenanceWindow(0);
	}

	/**
	 * When an announced maintenance window starts, the session that announced it is kept, although
	 * the maintenance window is entered by the timer thread outside of this session.
	 */
	public void testEnterMaintenanceWindowDelayed() throws Exception {
		doTestEnterMaintenanceWindow(1500);
	}

	private void doTestEnterMaintenanceWindow(long delay) throws Exception {
		SessionService service = SessionService.getInstance();
		MaintenanceWindowManager manager = MaintenanceWindowManager.getInstance();
		Person switching = createPerson("maintenanceSwitchingUser");
		Person other = createPerson("maintenanceOtherUser");
		try {
			HttpSession rootSession = login(PersonManager.getManager().getRoot());
			HttpSession otherSession = login(other);
			String otherSessionId = otherSession.getId();
			HttpSession switchingSession = loginAndEnterMaintenanceWindow(switching, delay);
			try {
				long timeout = System.currentTimeMillis() + 60000;
				while (manager.getMaintenanceModeState() != MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
					assertTrue("Maintenance window not entered.", System.currentTimeMillis() < timeout);
					Thread.sleep(100);
				}

				assertFalse("Session of a user that must not log in was not ended.", isValid(otherSession));
				assertFalse(service.getSessionIDs().contains(otherSessionId));

				assertTrue("Session that switched on the maintenance window must be kept.",
					isValid(switchingSession));
				assertTrue(service.getSessionIDs().contains(switchingSession.getId()));

				assertTrue("Session of a user that may log in must be kept.", isValid(rootSession));
				assertTrue(service.getSessionIDs().contains(rootSession.getId()));
			} finally {
				manager.leaveMaintenanceWindow();
				service.terminateSession(switchingSession.getId());
				service.terminateSession(rootSession.getId());
			}
		} finally {
			delete(switching);
			delete(other);
		}
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
		return TLContextManager.inSystemInteraction(TestMaintenanceWindowLogout.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));
	}

	/**
	 * Logs in the given user and enters the maintenance window after the given delay in the
	 * session of this user.
	 */
	private static HttpSession loginAndEnterMaintenanceWindow(Person user, long delay) throws Exception {
		InvocationContext invocation = newInvocation();
		return TLContextManager.inSystemInteraction(TestMaintenanceWindowLogout.class, () -> {
			HttpSession result =
				SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user);
			MaintenanceWindowManager.getInstance().enterMaintenanceWindow(delay);
			return result;
		});
	}

	private static InvocationContext newInvocation() throws Exception {
		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		return runner.newClient().newInvocation(new PostMethodWebRequest("http://ignore/lpServlet"));
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

	private static void delete(Person person) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			person.tDelete();
			tx.commit();
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestMaintenanceWindowLogout}.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestMaintenanceWindowLogout.class);
		innerTest = ServiceTestSetup.createSetup(innerTest, MaintenanceWindowManager.Module.INSTANCE);
		String customFile = CustomPropertiesDecorator.createFileName(TestMaintenanceWindowLogout.class);
		innerTest = new CustomPropertiesSetup(innerTest, customFile, true);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}
}
