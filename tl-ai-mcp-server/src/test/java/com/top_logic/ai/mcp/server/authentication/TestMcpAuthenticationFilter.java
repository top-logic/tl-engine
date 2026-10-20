/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.ai.mcp.server.authentication.HttpDoubles.Recorded;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.tl.ai.core.access.EndpointAuthentication;

/**
 * Tests which requests {@link McpAuthenticationFilter} refuses, and how.
 *
 * <p>
 * These are the refusals, which are decidable without a {@code KnowledgeBase}: no credential, and a
 * credential that authenticates but names no account. What the filter does when it <em>does</em>
 * resolve an account - bind it, open its {@code TLContext}, own the MCP session - needs a real
 * {@link Person} and is asserted in {@code tl-ai-demo}'s {@code SecDemoMcpEndpointTest}.
 * </p>
 */
public class TestMcpAuthenticationFilter extends TestCase {

	/** The refusal messages are read from the resource bundles. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestMcpAuthenticationFilter.class));
	}

	/**
	 * A request with no valid credential never reaches the protocol.
	 *
	 * <p>
	 * The chain assertion is the point: a filter that wrote a 401 and called the chain anyway would
	 * still serve the request. So the test asserts the negative, not only the status code.
	 * </p>
	 */
	public void testAnUnauthenticatedRequestNeverReachesTheChain() throws IOException, ServletException {
		Recorded recorded = new Recorded();

		guard(refusing(), recorded);

		assertFalse("An unauthenticated request must not reach the MCP protocol.", recorded.chainInvoked);
		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, recorded.status);
		assertEquals("The scheme's own challenge must reach the client.",
			"Basic realm=\"test\"", recorded.responseHeader("WWW-Authenticate"));
	}

	/**
	 * With nothing configured, the filter refuses rather than serving the request.
	 *
	 * <p>
	 * The composed authenticator's own default, seen from the filter: the endpoint answers
	 * {@code 503} and the protocol is never reached.
	 * </p>
	 */
	public void testNothingConfiguredRefusesEveryRequest() throws IOException, ServletException {
		Recorded recorded = new Recorded();

		guard(EndpointAuthentication.build("/mcp/*", Map.of(), List.of()), recorded);

		assertFalse("Nothing configured must serve nothing.", recorded.chainInvoked);
		assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, recorded.status);
	}

	/**
	 * A credential that authenticates but names no account is refused.
	 *
	 * <p>
	 * The framework reads a secret with an empty {@code user-id} as "process this in system context"
	 * - the engine's super-user bypass. For {@code /mcp/*} that is never right: the endpoint acts as
	 * the account it authenticated or it does not act at all.
	 * </p>
	 */
	public void testAuthenticatedWithoutAnAccountIsRefused() throws IOException, ServletException {
		Recorded recorded = new Recorded();

		guard(systemContext(), recorded);

		assertFalse("A request with no account must not reach the MCP protocol.", recorded.chainInvoked);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, recorded.status);
		assertTrue("The answer must say what the operator has to change: " + recorded.message,
			recorded.message.contains("user-id"));
	}

	/**
	 * While the service is off, the filter lets the servlet answer.
	 *
	 * <p>
	 * The servlet's {@code 503} names the module an administrator has to enable, and the filter must
	 * not replace that with an authentication error about an endpoint that is not running.
	 * </p>
	 */
	public void testTheOffStateIsLeftToTheServlet() throws IOException, ServletException {
		assertNull("Precondition: this test runs without a started MCPServerService.",
			MCPServerService.getInstance());

		Recorded recorded = new Recorded();
		new McpAuthenticationFilter().doFilter(HttpDoubles.request(recorded),
			HttpDoubles.response(recorded), HttpDoubles.chain(recorded, null));

		assertTrue("The servlet has to answer, so the chain must run.", recorded.chainInvoked);
		assertEquals("The filter must not answer for a service that is not running.", 0, recorded.status);
	}

	/**
	 * Runs the filter's decision against the given authenticator and an empty session ownership.
	 */
	private static void guard(Authenticator authenticator, Recorded recorded)
			throws IOException, ServletException {
		McpAuthenticationFilter.guard(authenticator, new McpSessionPrincipals(),
			HttpDoubles.request(recorded), HttpDoubles.response(recorded),
			HttpDoubles.chain(recorded, null));
	}

	/**
	 * An authenticator that always fails, with a challenge of its own.
	 */
	private static Authenticator refusing() {
		return new Authenticator() {
			@Override
			public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
					throws AuthenticationFailure {
				AuthenticationFailure failure = new AuthenticationFailure(ResKey.text("No credential."));
				failure.setResponseEnhancer((response, ex, path) -> {
					response.setHeader("WWW-Authenticate", "Basic realm=\"test\"");
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
				});
				throw failure;
			}
		};
	}

	/**
	 * An authenticator that authenticates and names no account - what a secret with an empty
	 * {@code user-id} produces.
	 */
	private static Authenticator systemContext() {
		return (req, resp) -> null;
	}
}
