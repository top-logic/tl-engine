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
import test.com.top_logic.knowledge.monitor.UserSessionCleanup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.base.accesscontrol.I18NConstants;
import com.top_logic.base.accesscontrol.IdentityVerifications.Outcome;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.UnknownAccountException;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSessionContext;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.Resources;

/**
 * Test for the identity verification and the unknown account branch of
 * {@link ExternalAuthenticationServlet}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExternalAuthenticationServlet extends BasicTestCase {

	/**
	 * {@link ExternalAuthenticationServlet} that opens the hooks under test and authenticates
	 * no one.
	 */
	public static class TestingServlet extends ExternalAuthenticationServlet {

		@Override
		public String getVerificationToken(HttpServletRequest request) {
			return super.getVerificationToken(request);
		}

		@Override
		public void writeVerificationResult(HttpServletResponse response, Outcome outcome)
				throws IOException {
			super.writeVerificationResult(response, outcome);
		}

		@Override
		protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request,
				HttpServletResponse response) {
			throw new UnsupportedOperationException("No external authentication in this test.");
		}

		@Override
		public void loginUser(Person person, HttpServletRequest request, HttpServletResponse response)
				throws InMaintenanceModeException {
			super.loginUser(person, request, response);
		}

	}

	/**
	 * {@link ExternalAuthenticationServlet} whose external authentication has verified the identity
	 * of {@link TestExternalAuthenticationServlet#LOGIN_NAME}, for whom the application has no
	 * account.
	 */
	public static class UnknownAccountServlet extends TestingServlet {

		@Override
		protected boolean isExtAuthEnabled() {
			return true;
		}

		@Override
		protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request,
				HttpServletResponse response) {
			throw new UnknownAccountException(LOGIN_NAME, "No account '" + LOGIN_NAME + "' in this application.");
		}

	}

	/**
	 * {@link ExternalAuthenticationServlet} that denies the login without having established who
	 * the user is.
	 */
	public static class DeniedServlet extends TestingServlet {

		@Override
		protected boolean isExtAuthEnabled() {
			return true;
		}

		@Override
		protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request,
				HttpServletResponse response) {
			throw new LoginDeniedException("Authentication rejected in this test.");
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

	/**
	 * The name that the external authentication of {@link UnknownAccountServlet} reports.
	 */
	static final String LOGIN_NAME = "alice";

	/**
	 * Name of an account that does not exist in this application.
	 */
	private static final String UNKNOWN_ACCOUNT = "no-such-account-xyz";

	private static final String UNKNOWN_ACCOUNT_SERVLET = "unknownAccountServlet";

	private static final String DENIED_SERVLET = "deniedServlet";

	private ServletUnitClient _client;

	private UserSessionCleanup _userSessions;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_userSessions = UserSessionCleanup.snapshot(PersistencyLayer.getKnowledgeBase());

		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		runner.registerServlet(UNKNOWN_ACCOUNT_SERVLET, UnknownAccountServlet.class.getName());
		runner.registerServlet(DENIED_SERVLET, DeniedServlet.class.getName());
		runner.registerServlet(ApplicationPages.getInstance().getLoginRetrySSOPage(),
			LoginRetryServlet.class.getName());
		_client = runner.newClient();
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;
		// The logins of this test are recorded as user sessions, which must not be seen by later
		// tests that share the knowledge base.
		_userSessions.deleteCreated();

		super.tearDown();
	}

	/**
	 * A request without the parameter is an ordinary login, one with it names a verification.
	 */
	public void testVerificationToken() throws Exception {
		TestingServlet servlet = new TestingServlet();

		assertNull("Without the parameter the request is an ordinary login.",
			servlet.getVerificationToken(request("http://ignore/lpServlet")));
		assertNull("An empty parameter names no verification.",
			servlet.getVerificationToken(
				request("http://ignore/lpServlet?" + ExternalAuthenticationServlet.VERIFICATION_PARAM + "=")));
		assertEquals("abc-123", servlet.getVerificationToken(
			request("http://ignore/lpServlet?" + ExternalAuthenticationServlet.VERIFICATION_PARAM + "=abc-123")));
	}

	/**
	 * Every outcome produces a page that says what happened, and only the confirmed one tries to
	 * close the window.
	 */
	public void testResultPage() throws Exception {
		assertPage(Outcome.VERIFIED, true);
		assertPage(Outcome.MISMATCH, false);
		assertPage(Outcome.UNKNOWN, false);
	}

	private void assertPage(Outcome outcome, boolean closesWindow) throws Exception {
		InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest("http://ignore/lpServlet"));
		new TestingServlet().writeVerificationResult(invocation.getResponse(), outcome);
		String page = invocation.getServletResponse().getText();

		assertContains("<!DOCTYPE html>", page);
		assertContains(Resources.getInstance().getString(I18NConstants.IDENTITY_VERIFICATION_TITLE), page);
		assertContains(expectedMessage(outcome), page);
		assertEquals("Only a confirmed identity may close the window.", closesWindow,
			page.contains("window.close()"));
	}

	private String expectedMessage(Outcome outcome) {
		Resources resources = Resources.getInstance();
		switch (outcome) {
			case VERIFIED:
				return resources.getString(I18NConstants.IDENTITY_VERIFIED);
			case MISMATCH:
				return resources.getString(I18NConstants.ERROR_IDENTITY_MISMATCH);
			default:
				return resources.getString(I18NConstants.ERROR_IDENTITY_VERIFICATION_UNKNOWN);
		}
	}

	/**
	 * A name that the application has no account for is reported as such, together with the name
	 * that was looked up.
	 */
	public void testUnknownAccountLookup() throws Exception {
		assertNull("Test requires that no such account exists.", Person.byName(UNKNOWN_ACCOUNT));

		try {
			ExternalUserMapping.findAccount(UNKNOWN_ACCOUNT);
			fail("An account that does not exist must not be found.");
		} catch (UnknownAccountException ex) {
			assertEquals(UNKNOWN_ACCOUNT, ex.getLoginName());
		}
	}

	/**
	 * A user whose identity the external authentication has verified, but who has no account, is
	 * sent to the page that says so, with their login name in the URL.
	 */
	public void testUnknownAccountRedirect() throws Exception {
		WebResponse response = service(UNKNOWN_ACCOUNT_SERVLET);

		assertEquals("An unknown account is redirected to the page explaining that.",
			HttpServletResponse.SC_MOVED_TEMPORARILY, response.getResponseCode());
		assertEquals(ApplicationPages.getInstance().getUnknownAccountPage() + "?"
			+ ExternalAuthenticationServlet.LOGIN_NAME_PARAM + "=" + LOGIN_NAME,
			response.getHeaderField("Location"));
	}

	/**
	 * A denial that does not establish who the user is stays with the login page.
	 */
	public void testLoginDeniedForward() throws Exception {
		WebResponse response = service(DENIED_SERVLET);

		assertEquals("A denial without an established identity must not be redirected.",
			HttpServletResponse.SC_OK, response.getResponseCode());
		assertNull("A denial without an established identity must not be redirected.",
			response.getHeaderField("Location"));
		assertContains(LoginRetryServlet.MARKER, response.getText());
	}

	/**
	 * A login naming a window of its session applies to that window only: the session and its other
	 * windows are kept, and the session ID changes.
	 */
	public void testLoginInWindow() throws Exception {
		SessionService service = SessionService.getInstance();
		Person guest = createPerson("externalWindowGuest");
		Person user = createPerson("externalWindowUser");
		try {
			InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest(
				"http://ignore/lpServlet?" + ExternalAuthenticationServlet.WINDOW_PARAM + "=windowA"));
			HttpServletRequest request = invocation.getRequest();
			HttpSession session = ThreadContext.inSystemContext(TestExternalAuthenticationServlet.class,
				() -> service.loginUser(request, invocation.getResponse(), guest));
			String oldId = session.getId();
			TLSessionContext sessionContext = service.getSession(session);
			TLSubSessionContext window = newWindow(sessionContext, "windowA", guest);
			TLSubSessionContext otherWindow = newWindow(sessionContext, "windowB", guest);

			new TestingServlet().loginUser(user, request, invocation.getResponse());

			assertSame("Session must be kept.", session, request.getSession(false));
			assertSame("Session context must be kept.", sessionContext, service.getSession(session));
			assertNotEquals("Session ID not changed.", oldId, session.getId());
			assertEquals(user, service.getUser(session.getId()));
			assertEquals(user, window.getPerson());
			assertEquals("Other window must keep its user.", guest, otherWindow.getPerson());
			assertEquals(user, sessionContext.getOriginalUser());

			service.terminateSession(session.getId());
		} finally {
			deleteIfValid(user);
			deleteIfValid(guest);
		}
	}

	/**
	 * A login naming a window its session does not know applies to the whole session.
	 */
	public void testLoginInUnknownWindow() throws Exception {
		SessionService service = SessionService.getInstance();
		Person guest = createPerson("externalSessionGuest");
		Person user = createPerson("externalSessionUser");
		try {
			InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest(
				"http://ignore/lpServlet?" + ExternalAuthenticationServlet.WINDOW_PARAM + "=noSuchWindow"));
			HttpServletRequest request = invocation.getRequest();
			HttpSession session = ThreadContext.inSystemContext(TestExternalAuthenticationServlet.class,
				() -> service.loginUser(request, invocation.getResponse(), guest));
			TLSessionContext guestContext = service.getSession(session);

			ThreadContext.inSystemContext(TestExternalAuthenticationServlet.class, () -> {
				new TestingServlet().loginUser(user, request, invocation.getResponse());
				return null;
			});

			assertEquals(user, service.getUser(session.getId()));
			assertNotSame("The whole session is logged in anew.", guestContext, service.getSession(session));
			assertEquals(user, service.getSession(session).getOriginalUser());

			service.terminateSession(session.getId());
		} finally {
			deleteIfValid(user);
			deleteIfValid(guest);
		}
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

	private static Person createPerson(String name) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
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
		try (Transaction tx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			person.tDelete();
			tx.commit();
		}
	}

	/**
	 * Lets the servlet registered under the given name answer a request to it.
	 */
	private WebResponse service(String servletName) throws Exception {
		InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest("http://ignore/" + servletName));
		ExternalAuthenticationServlet servlet = (ExternalAuthenticationServlet) invocation.getServlet();
		servlet.doPost(invocation.getRequest(), invocation.getResponse());
		return invocation.getServletResponse();
	}

	private HttpServletRequest request(String url) throws Exception {
		WebRequest webRequest = new GetMethodWebRequest(url);
		return _client.newInvocation(webRequest).getRequest();
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
