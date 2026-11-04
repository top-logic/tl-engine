/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Test for the handling of a failed login in {@link ExternalAuthenticationServlet}.
 *
 * @author <a href="mailto:marc.siebenhaar@top-logic.com">Marc Siebenhaar</a>
 */
public class TestExternalAuthenticationServlet extends BasicTestCase {

	/**
	 * {@link ExternalAuthenticationServlet} whose login fails with {@link #_failure}, before or after
	 * the session has been changed for the user (e.g. like a login whose tokens cannot be
	 * installed).
	 */
	public static class FailingLoginServlet extends ExternalAuthenticationServlet {

		/**
		 * The user the external authentication reports.
		 */
		static volatile Person _user;

		/**
		 * The {@link RuntimeException} or {@link Error} the login fails with.
		 */
		static volatile Throwable _failure;

		/**
		 * Whether the login fails before the session is changed, otherwise after.
		 */
		static volatile boolean _failBeforeLogin;

		@Override
		protected boolean isExtAuthEnabled() {
			return true;
		}

		@Override
		protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request,
				HttpServletResponse response) {
			return LoginCredentials.fromUser(_user);
		}

		@Override
		protected void loginUser(Person person, HttpServletRequest request, HttpServletResponse response)
				throws InMaintenanceModeException {
			if (!_failBeforeLogin) {
				super.loginUser(person, request, response);
			}
			if (_failure instanceof Error error) {
				throw error;
			}
			throw (RuntimeException) _failure;
		}

		static void reset() {
			_user = null;
			_failure = null;
			_failBeforeLogin = false;
		}

		/**
		 * Lets this servlet process the given request as a login.
		 */
		public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
			checkRequest(request, response);
		}

	}

	/**
	 * Stand-in for the page that a login failure is forwarded to.
	 */
	public static class LoginRetryServlet extends HttpServlet {

		/**
		 * Content of the page, by which the test recognizes that it was reached.
		 */
		public static final String MARKER = "login-retry-page";

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
			response.setContentType("text/plain");
			response.getWriter().write(MARKER);
		}

	}

	private static final String FAILING_LOGIN_SERVLET = "failingLoginServlet";

	private ServletUnitClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		runner.registerServlet(FAILING_LOGIN_SERVLET, FailingLoginServlet.class.getName());
		runner.registerServlet(ApplicationPages.getInstance().getLoginRetrySSOPage(),
			LoginRetryServlet.class.getName());
		_client = runner.newClient();
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;
		super.tearDown();
	}

	/**
	 * A login that fails after the session has been established for the user ends that session,
	 * so that no later request reuses a session the login has not completed (Ticket #30110).
	 */
	public void testFailedLoginEndsSession() throws Exception {
		assertFailedLoginEndsSession(new IllegalStateException("Installing the tokens failed in this test."));
	}

	/**
	 * A login failing with an {@link Error} after the session has been established ends that
	 * session as well.
	 */
	public void testFailedLoginWithErrorEndsSession() throws Exception {
		assertFailedLoginEndsSession(new AssertionError("Unexpected state in this test."));
	}

	/**
	 * A login denied after the session has been established (e.g. by an override checking the
	 * claims after the login) ends that session as well.
	 */
	public void testLoginDeniedAfterLoginEndsSession() throws Exception {
		assertFailedLoginEndsSession(new LoginDeniedException("Claims rejected in this test."));
	}

	private void assertFailedLoginEndsSession(Throwable failure) throws Exception {
		SessionService service = SessionService.getInstance();
		Person user = createPerson("externalFailedLoginUser");
		try {
			FailingLoginServlet._user = user;
			FailingLoginServlet._failure = failure;
			InvocationContext invocation =
				_client.newInvocation(new GetMethodWebRequest("http://ignore/" + FAILING_LOGIN_SERVLET));
			login(invocation);

			assertContains(LoginRetryServlet.MARKER, invocation.getServletResponse().getText());
			for (String sessionId : service.getSessionIDs()) {
				assertNotEquals("Session of the failed login still registered.", user, service.getUser(sessionId));
			}
		} finally {
			FailingLoginServlet.reset();
			deleteIfValid(user);
		}
	}

	/**
	 * A login that fails before it has changed the session of the request keeps that session, also
	 * if it already belongs to the user of the failed login.
	 */
	public void testLoginFailedBeforeLoginKeepsSession() throws Exception {
		SessionService service = SessionService.getInstance();
		Person user = createPerson("externalKeptSessionUser");
		try {
			FailingLoginServlet._user = user;
			FailingLoginServlet._failure = new IllegalStateException("Failure before the login in this test.");
			FailingLoginServlet._failBeforeLogin = true;
			InvocationContext invocation =
				_client.newInvocation(new GetMethodWebRequest("http://ignore/" + FAILING_LOGIN_SERVLET));
			HttpServletRequest request = invocation.getRequest();
			HttpSession session = ThreadContext.inSystemContext(TestExternalAuthenticationServlet.class,
				() -> service.loginUser(request, invocation.getResponse(), user));
			String sessionId = session.getId();

			login(invocation);

			assertContains(LoginRetryServlet.MARKER, invocation.getServletResponse().getText());
			assertEquals("Session not touched by the failed login must be kept.", user, service.getUser(sessionId));
			assertTrue("Session not touched by the failed login must be kept.", service.validateSession(request));

			service.invalidateSession(session);
		} finally {
			FailingLoginServlet.reset();
			deleteIfValid(user);
		}
	}

	private static void login(InvocationContext invocation) throws Exception {
		FailingLoginServlet servlet = (FailingLoginServlet) invocation.getServlet();
		ThreadContext.inSystemContext(TestExternalAuthenticationServlet.class, () -> {
			servlet.login(invocation.getRequest(), invocation.getResponse());
			return null;
		});
	}

	private static Person createPerson(String name) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, name,
				PersonManager.getManager().getRoot().getAuthenticationDeviceID());
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
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestExternalAuthenticationServlet.class),
			SessionService.Module.INSTANCE, Login.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
