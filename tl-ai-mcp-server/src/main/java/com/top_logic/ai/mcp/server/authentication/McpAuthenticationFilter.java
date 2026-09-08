/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;

import io.modelcontextprotocol.spec.HttpHeaders;

/**
 * Authenticates every request to {@code /mcp/*} and gives it the account it acts as.
 *
 * <p>
 * The MCP specification requires authorization on <em>every</em> HTTP request from client to server,
 * not only on {@code initialize}, so this is a per-request filter and not a session check. The
 * mechanism is whatever the deployment configured - see {@link McpAuthentication}; the challenge and
 * the status code on a failure come from the framework's own {@code ResponseEnhancer}, so a basic
 * scheme answers {@code 401} with {@code WWW-Authenticate: Basic} and a bearer scheme with
 * {@code WWW-Authenticate: Bearer ... error="invalid_token"}.
 * </p>
 *
 * <p>
 * Compare {@code tl-chat}'s {@code ChatSessionFilter}, which this mirrors in placement but not in
 * substance: that one checks that a browser session exists, and the identity comes from the session
 * cookie further in. An MCP client has no browser login and no window id, so it needs the opposite
 * - a credential on the request.
 * </p>
 *
 * <h2>What it establishes for the rest of the request</h2>
 *
 * <ul>
 * <li>The account is bound to the thread ({@link McpCallerContext}) and the {@code TLContext} of that
 * account is opened around the rest of the chain. The MCP server is built with
 * {@code immediateExecution(true)}, so the protocol handlers run on this very thread while the
 * transport blocks it - which is what makes a filter the right place for this.</li>
 * <li>The account that opened an MCP session owns it ({@link McpSessionPrincipals}). Without that,
 * two accounts holding valid credentials could take turns on one {@code mcp-session-id}.</li>
 * </ul>
 *
 * <h2>What it refuses</h2>
 *
 * <ul>
 * <li>A request with no valid credential - with the framework's challenge.</li>
 * <li>A credential that authenticates but names no application account. The framework reads an empty
 * {@code user-id} on a secret as "process this in system context", which is the engine's super-user
 * bypass; for this endpoint that is never right, so it is refused rather than honoured.</li>
 * <li>A request for a session another account opened.</li>
 * <li>A request whose thread already carries a TopLogic interaction. {@code inPersonContext} is a
 * no-op when one is bound, so the request would silently act as whoever that interaction belongs to.
 * Nothing in the shipped deployment produces this - {@code MCPTransportServlet} is a plain
 * {@code HttpServlet} - and the check is here because the failure it prevents is an identity
 * confusion that no test would catch by accident.</li>
 * </ul>
 */
public class McpAuthenticationFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to initialize: the authenticator belongs to the service, which is started and
		// stopped independently of this filter.
	}

	@Override
	public void destroy() {
		// Nothing to clean up.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		MCPServerService service = MCPServerService.getInstance();
		if (service == null) {
			// The service is off - the shipped state. Let the servlet answer, because its 503 names
			// the module an administrator has to enable.
			chain.doFilter(request, response);
			return;
		}

		guard(service.getAuthenticator(), service.getSessionPrincipals(),
			(HttpServletRequest) request, (HttpServletResponse) response, chain);
	}

	/**
	 * Decides and serves one request against the given authenticator and session ownership.
	 *
	 * <p>
	 * Separated from {@link #doFilter} so that the decisions can be exercised without a started
	 * {@link MCPServerService}: everything that matters here is a function of the authenticator, the
	 * session map and the request. Public rather than package-visible because the tests that need a
	 * real {@code Person} - and therefore a booted application - live in {@code tl-ai-demo}'s
	 * security test bed.
	 * </p>
	 */
	public static void guard(Authenticator authenticator, McpSessionPrincipals sessions,
			HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		if (ThreadContextManager.getInteraction() != null) {
			refuse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"a TopLogic interaction is already bound to the request thread",
				message(I18NConstants.ERROR_INTERACTION_ALREADY_BOUND));
			return;
		}

		AuthenticatedCaller caller;
		try {
			caller = resolve(authenticator, req, resp);
		} catch (AuthenticationFailure ex) {
			// The failure carries the scheme's own challenge and status code.
			ex.enhanceResponse(resp, req.getRequestURI());
			return;
		}

		if (caller == null) {
			refuse(resp, HttpServletResponse.SC_FORBIDDEN, "no account for the credential",
				message(I18NConstants.ERROR_NO_ACCOUNT_FOR_CREDENTIAL));
			return;
		}

		String sessionId = req.getHeader(HttpHeaders.MCP_SESSION_ID);
		if (sessionId != null && !sessions.mayUse(sessionId, caller.name())) {
			refuse(resp, HttpServletResponse.SC_FORBIDDEN,
				"account '" + caller.name() + "' on a foreign MCP session",
				message(I18NConstants.ERROR_SESSION_BELONGS_TO_ANOTHER_ACCOUNT));
			return;
		}

		serve(req, resp, chain, caller, sessions, sessionId);
	}

	/**
	 * The account the request's credential names, in a context that can look one up.
	 *
	 * <p>
	 * <b>Resolving a credential is itself a {@code KnowledgeBase} read</b> - every authenticator ends
	 * in an account lookup by login name - and a servlet request thread carries no TopLogic
	 * interaction, so one has to be opened around it. It is a <em>system</em> interaction because at
	 * this point there is no account to open it as; that is the one thing a system interaction is
	 * legitimately for here, and it is closed again before anything of the request is served. What
	 * runs inside it is the authenticator and nothing else.
	 * </p>
	 *
	 * <p>
	 * The login name is read here for the same reason: it is a {@code KnowledgeBase} read too, and
	 * the session-ownership check and the binding happen outside this interaction.
	 * </p>
	 *
	 * @return The caller, or <code>null</code> if the credential authenticated but names no account.
	 */
	private static AuthenticatedCaller resolve(Authenticator authenticator, HttpServletRequest req,
			HttpServletResponse resp) throws AuthenticationFailure {
		return ThreadContextManager.<AuthenticatedCaller, AuthenticationFailure, RuntimeException> inSystemInteraction(
			McpAuthenticationFilter.class, () -> {
				Person account = authenticator.authenticate(req, resp);
				return account == null ? null : new AuthenticatedCaller(account, account.getName());
			});
	}

	/**
	 * Runs the rest of the chain as {@code caller}, and maintains that account's session ownership.
	 */
	private static void serve(HttpServletRequest req, HttpServletResponse resp, FilterChain chain,
			AuthenticatedCaller caller, McpSessionPrincipals sessions, String requestedSession)
			throws IOException, ServletException {
		McpCallerContext.<Void, IOException, ServletException> withCaller(caller.account(),
			() -> TLContextManager.<Void, IOException, ServletException> inPersonContext(caller.account(),
				() -> {
					chain.doFilter(req, resp);
					return null;
				}));

		// After the chain, because only then does an initialize request have a session id: the
		// transport creates the session while handling the request and writes the id into the
		// response.
		String openedSession = resp.getHeader(HttpHeaders.MCP_SESSION_ID);
		if (openedSession != null) {
			sessions.bind(openedSession, caller.name());
		}
		if (requestedSession != null && "DELETE".equals(req.getMethod())) {
			sessions.release(requestedSession);
		}
	}

	/**
	 * An authenticated account and its login name.
	 *
	 * <p>
	 * The name is carried alongside the wrapper because reading it is a {@code KnowledgeBase}
	 * operation, and the filter needs it in places that have no interaction bound - see
	 * {@link #resolve}.
	 * </p>
	 *
	 * @param account
	 *        The authenticated account, never <code>null</code>.
	 * @param name
	 *        Its login name, never <code>null</code>.
	 */
	private record AuthenticatedCaller(Person account, String name) {
		// Pure value.
	}

	/**
	 * Answers a request this filter will not pass on.
	 *
	 * @param logged
	 *        What to write to the log - the technical reason, which is not always what the caller is
	 *        told.
	 * @param sent
	 *        The message for the caller.
	 */
	private static void refuse(HttpServletResponse resp, int status, String logged, String sent) throws IOException {
		Logger.info("Refused an MCP request (" + status + "): " + logged, McpAuthenticationFilter.class);
		resp.sendError(status, sent);
	}

	/**
	 * The given message in the application's own language.
	 *
	 * <p>
	 * The system locale, not the caller's: there is no session to read a locale from at this point,
	 * and the recipient is an operator reading an HTTP error, not an interactive user.
	 * </p>
	 */
	private static String message(ResKey key) {
		return Resources.getSystemInstance().getString(key);
	}
}
