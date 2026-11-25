/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.monitor.UserSessionCleanup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitHttpSession;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.resource.ClientResources;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.layout.view.login.PendingSessionAction;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.TopLogicServlet;

/**
 * Tests {@link ViewServlet#forwardToView(HttpServletRequest, HttpServletResponse, String, int, Map)}:
 * another servlet answers its request with an anonymous entry point of the view application, in
 * that very response and with a status of its own.
 */
public class TestForwardToView extends BasicTestCase {

	/** The anonymous entry point the test servlets forward to, see the test configuration. */
	static final String VIEW = "test/forward-to-view.view.xml";

	/** The parameter the view takes up from its query. */
	static final String NR_PARAM = "nr";

	/** The status the test servlets answer with. */
	static final int STATUS = HttpServletResponse.SC_FORBIDDEN;

	private static final String HOST = "http://localhost";

	private static final String VIEW_SERVLET = "/view/*";

	private static final String PLAIN_SERVLET = "/plain-forward";

	private static final String TL_SERVLET = "/tl-forward";

	/** A value of {@link #NR_PARAM} only the rendered view can contain. */
	private static final String NR_VALUE = "nr-4711-forwarded";

	/** Script of the bootstrap page, which a forwarded request must not be answered with. */
	private static final String BOOTSTRAP_MARKER = "window.location.replace";

	/**
	 * Forwards every request to {@link TestForwardToView#VIEW}, handing it the request's
	 * {@link TestForwardToView#NR_PARAM}.
	 */
	public static class PlainForwardServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			forward(request, response);
		}

	}

	/**
	 * {@link PlainForwardServlet} within the context a {@link TopLogicServlet} establishes.
	 */
	public static class TLForwardServlet extends TopLogicServlet {

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			forward(request, response);
		}

	}

	static void forward(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ViewServlet.forwardToView(request, response, VIEW, STATUS,
			Map.of(NR_PARAM, request.getParameter(NR_PARAM)));
	}

	private ServletUnitClient _client;

	private UserSessionCleanup _userSessions;

	private final List<Person> _created = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_userSessions = UserSessionCleanup.snapshot(PersistencyLayer.getKnowledgeBase());

		ServletRunner runner = new ServletRunner();
		runner.registerServlet(VIEW_SERVLET, ViewServlet.class.getName());
		runner.registerServlet(PLAIN_SERVLET, PlainForwardServlet.class.getName());
		runner.registerServlet(TL_SERVLET, TLForwardServlet.class.getName());
		_client = runner.newClient();
		// The pages are answered with an error status, and their content is what the test reads.
		_client.setExceptionsThrownOnErrorStatus(false);
	}

	@Override
	protected void tearDown() throws Exception {
		HttpSession session = _client.getSession(false);
		if (session != null) {
			SessionService.getInstance().terminateSession(session.getId());
		}
		_client = null;
		ThreadContext.inSystemContext(TestForwardToView.class, () -> {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
				for (Person person : _created) {
					if (person.tValid()) {
						person.tDelete();
					}
				}
				tx.commit();
			}
		});
		_created.clear();
		// The logins of this test are recorded as user sessions, which must not be seen by later
		// tests that share the knowledge base.
		_userSessions.deleteCreated();
		super.tearDown();
	}

	/**
	 * A visitor without a session is logged in anonymously and gets the view right away, with the
	 * status of the forwarding servlet.
	 */
	public void testWithoutSession() throws Exception {
		WebResponse response = request(PLAIN_SERVLET);

		assertForwardedPage(response);
		HttpSession session = _client.getSession(false);
		assertNotNull("The visitor is given a session.", session);
		assertEquals(PersonManager.getManager().getAnonymous(),
			SessionService.getInstance().getUser(session.getId()));
	}

	/**
	 * A visitor whose session is anonymous gets the view in that session.
	 */
	public void testWithAnonymousSession() throws Exception {
		request(PLAIN_SERVLET);
		String sessionId = _client.getSession(false).getId();

		WebResponse response = request(PLAIN_SERVLET);

		assertForwardedPage(response);
		assertEquals("The view is displayed in the existing session.", sessionId,
			_client.getSession(false).getId());
	}

	/**
	 * A user who is logged in gets the view in their session.
	 */
	public void testWithNamedSession() throws Exception {
		Person user = createPerson("forwardToViewUser");
		InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest(url(PLAIN_SERVLET)));
		ThreadContext.inSystemContext(TestForwardToView.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(), user));

		assertForwardedPage(service(invocation));
		assertEquals("The user stays logged in.", user,
			SessionService.getInstance().getUser(_client.getSession(false).getId()));
	}

	/**
	 * A servlet running in the context a {@link TopLogicServlet} establishes forwards as well.
	 */
	public void testFromTopLogicServlet() throws Exception {
		// The session the TopLogicServlet requires.
		request(PLAIN_SERVLET);

		assertForwardedPage(request(TL_SERVLET));
	}

	/**
	 * A logout still pending in the session is carried out with a redirect to the URL the browser
	 * asked for, the forwarding servlet's, which then decides anew.
	 */
	public void testPendingLogoutRedirectsToServlet() throws Exception {
		request(PLAIN_SERVLET);
		PendingSessionAction.requestLogout(_client.getSession(false));

		WebResponse response = request(PLAIN_SERVLET);

		assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getResponseCode());
		assertEquals(url(PLAIN_SERVLET).substring(HOST.length()), response.getHeaderField("Location"));
	}

	/**
	 * Only an anonymous entry point can be forwarded to.
	 */
	public void testOnlyAnonymousEntryPoints() throws Exception {
		assertRejected("app.view.xml");
		assertRejected("login-page.view.xml");
		assertRejected("no-such.view.xml");
	}

	private void assertRejected(String view) throws Exception {
		InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest(url(PLAIN_SERVLET)));
		try {
			ViewServlet.forwardToView(invocation.getRequest(), invocation.getResponse(), view, STATUS, Map.of());
			fail("View '" + view + "' is no anonymous entry point.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	private void assertForwardedPage(WebResponse response) throws IOException {
		String page = response.getText();
		assertEquals(page, STATUS, response.getResponseCode());
		assertNull("The view is no redirect.", response.getHeaderField("Location"));
		assertFalse("The view is rendered without bootstrap page.", page.contains(BOOTSTRAP_MARKER));
		assertTrue("The page keeps the URL of the servlet.", page.contains(ViewServlet.FIXED_URL_ATTR));
		assertTrue("The parameter reaches the view's channel: " + page, page.contains(NR_VALUE));
	}

	private WebResponse request(String servlet) throws Exception {
		return service(_client.newInvocation(new GetMethodWebRequest(url(servlet))));
	}

	/**
	 * Lets the servlet answer the given request, keeping the session the request ends with for the
	 * client's next request.
	 *
	 * <p>
	 * The response is read as text: the client does not load the scripts and stylesheets the page
	 * refers to, which only a browser would.
	 * </p>
	 */
	private WebResponse service(InvocationContext invocation) throws Exception {
		invocation.service();
		HttpSession session = invocation.getRequest().getSession(false);
		if (session != null) {
			_client.putCookie(ServletUnitHttpSession.SESSION_COOKIE_NAME, session.getId());
		}
		return invocation.getServletResponse();
	}

	private static String url(String servlet) {
		return HOST + servlet + "?" + NR_PARAM + "=" + NR_VALUE;
	}

	private Person createPerson(String name) {
		return ThreadContext.inSystemContext(TestForwardToView.class, () -> {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
				Person result = Person.create(kb, name,
					PersonManager.getManager().getRoot().getAuthenticationDevice());
				tx.commit();
				_created.add(result);
				return result;
			}
		});
	}

	/**
	 * The suite of tests to execute, with sessions, persons and the services rendering a page of
	 * the view application.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestForwardToView.class),
			SessionService.Module.INSTANCE, CachePolicy.Module.INSTANCE, SchedulerService.Module.INSTANCE, UIThemeService.Module.INSTANCE,
			ClientResources.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(test);
	}

}
