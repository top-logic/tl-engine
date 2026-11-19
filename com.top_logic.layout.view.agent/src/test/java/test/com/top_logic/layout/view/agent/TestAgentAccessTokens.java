/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.view.agent.AccessTokenAuthenticator;
import com.top_logic.layout.view.agent.AgentAccess;
import com.top_logic.layout.view.agent.AgentServlet;
import com.top_logic.util.CachePolicy;
import com.top_logic.util.model.ModelService;

/**
 * Test of the access tokens at the {@link AgentServlet}: what a token with the permissions
 * {@link AccessTokenAuthenticator#UI_OBSERVE_PERMISSION} and
 * {@link AccessTokenAuthenticator#UI_ACT_PERMISSION} admits an agent to, and which tokens admit
 * nobody.
 *
 * <p>
 * A user logs in, and the session is {@link AgentAccess#bindSession(String, HttpSession) bound} as
 * on issuing a token. The agent is a client of its own, without the session cookie, presenting the
 * token only. The permission {@link #OTHER_PERMISSION} of another endpoint is contributed by the
 * test configuration.
 * </p>
 */
public class TestAgentAccessTokens extends BasicTestCase {

	private static final String OTHER_PERMISSION = "test-other";

	private static final String SESSION_KEY = "test-agent-session";

	private static final String UNKNOWN_SESSION_KEY = "test-ended-session";

	private static final String LOGIN_SERVLET = "lpServlet";

	private static final String SERVLET_NAME = "agent-api";

	private static final String BASE_URL = "http://localhost/" + SERVLET_NAME;

	private static final String PATH_WINDOWS = "/windows";

	private static final String PATH_ACT = "/act";

	private ServletRunner _runner;

	private ServletUnitClient _agent;

	private HttpSession _session;

	private final List<AccessToken> _tokens = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_runner = new ServletRunner();
		_runner.registerServlet(LOGIN_SERVLET, TLLayoutServlet.class.getName());
		_runner.registerServlet(SERVLET_NAME + "/*", AgentServlet.class.getName());

		_session = login(_runner.newClient());
		AgentAccess.getInstance().bindSession(SESSION_KEY, _session);

		_agent = _runner.newClient();
		_agent.getClientProperties().setAutoRedirect(false);
		_agent.setExceptionsThrownOnErrorStatus(false);
	}

	@Override
	protected void tearDown() throws Exception {
		for (AccessToken token : _tokens) {
			if (token.tValid()) {
				AccessTokens.revoke(token);
			}
		}
		_tokens.clear();
		AgentAccess.getInstance().unbindSession(SESSION_KEY);
		ThreadContext.inSystemContext(TestAgentAccessTokens.class,
			() -> SessionService.getInstance().terminateSession(_session.getId()));
		_runner.shutDown();
		super.tearDown();
	}

	/**
	 * A token that may observe reads the session, and is refused (403) on every change.
	 */
	public void testObserveReadsButDoesNotAct() throws Exception {
		String token = token(List.of(AccessTokenAuthenticator.UI_OBSERVE_PERMISSION), SESSION_KEY);

		assertEquals(HttpServletResponse.SC_OK, status(get(PATH_WINDOWS), token));
		assertEquals(HttpServletResponse.SC_FORBIDDEN, status(post(PATH_ACT), token));
	}

	/**
	 * A token that may act reads the session and passes the check of a change: the empty command
	 * is refused as a bad request, not for the credential.
	 */
	public void testActReadsAndActs() throws Exception {
		String token = token(List.of(AccessTokenAuthenticator.UI_ACT_PERMISSION), SESSION_KEY);

		assertEquals(HttpServletResponse.SC_OK, status(get(PATH_WINDOWS), token));
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, status(post(PATH_ACT), token));
	}

	/**
	 * A token with a permission of another endpoint only admits nobody.
	 */
	public void testOtherPermissionAdmitsNobody() throws Exception {
		String token = token(List.of(OTHER_PERMISSION), null);

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status(get(PATH_WINDOWS), token));
		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status(post(PATH_ACT), token));
	}

	/**
	 * A token bound to a session that has ended admits nobody.
	 */
	public void testUnknownSessionAdmitsNobody() throws Exception {
		String token = token(List.of(AccessTokenAuthenticator.UI_ACT_PERMISSION), UNKNOWN_SESSION_KEY);

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status(get(PATH_WINDOWS), token));
		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status(post(PATH_ACT), token));
	}

	/**
	 * An expired token admits nobody.
	 */
	public void testExpiredTokenAdmitsNobody() throws Exception {
		AccessTokens.Created created = create(List.of(AccessTokenAuthenticator.UI_ACT_PERMISSION), SESSION_KEY);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			created.token().setExpiresAt(Date.from(Instant.now().minusSeconds(1)));
			tx.commit();
		}

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status(get(PATH_WINDOWS), created.plain()));
	}

	private String token(List<String> permissions, String sessionKey) {
		return create(permissions, sessionKey).plain();
	}

	private AccessTokens.Created create(List<String> permissions, String sessionKey) {
		AccessTokens.Created created =
			AccessTokens.create(PersonManager.getManager().getRoot(), "agent", null, permissions, sessionKey);
		_tokens.add(created.token());
		return created;
	}

	private int status(WebRequest request, String token) throws Exception {
		request.setHeaderField(AgentAccess.AUTHORIZATION_HEADER, AgentAccess.BEARER_PREFIX + token);
		return _agent.getResponse(request).getResponseCode();
	}

	private static WebRequest get(String path) {
		return new GetMethodWebRequest(BASE_URL + path);
	}

	private static WebRequest post(String path) {
		return new PostMethodWebRequest(BASE_URL + path);
	}

	private static HttpSession login(ServletUnitClient browser) throws Exception {
		InvocationContext invocation =
			browser.newInvocation(new PostMethodWebRequest("http://ignore/" + LOGIN_SERVLET));
		HttpSession session = ThreadContext.inSystemContext(TestAgentAccessTokens.class,
			() -> SessionService.getInstance().loginUser(invocation.getRequest(), invocation.getResponse(),
				PersonManager.getManager().getRoot()));
		browser.getResponse(invocation);
		return session;
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestAgentAccessTokens.class);
		innerTest = ServiceTestSetup.createSetup(innerTest, ModelService.Module.INSTANCE,
			SessionService.Module.INSTANCE, CachePolicy.Module.INSTANCE, AgentAccess.Module.INSTANCE);
		String customFile = CustomPropertiesDecorator.createFileName(TestAgentAccessTokens.class);
		innerTest = new CustomPropertiesSetup(innerTest, customFile, true);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}
