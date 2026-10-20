/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.ai.mcp.server.authentication.HttpDoubles.Recorded;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.http.basic.BasicAuthentication;
import com.top_logic.tl.ai.core.access.EndpointAuthentication;

/**
 * Tests what {@link EndpointAuthentication} builds from a configuration, and what it refuses.
 *
 * <p>
 * The decision under test is the one the design turns on: <b>an application that configured no
 * authentication gets an endpoint that serves nobody</b>. The <i>OpenAPI</i> server reads an
 * operation with no declared authentication as "publish this", because a REST path is written out by
 * hand; {@code /mcp/*} exists because the module is on the classpath, so the opposite default is the
 * only safe one. Getting this backwards reopens issue #261, which is why it has a test of its own.
 * </p>
 *
 * <p>
 * No account is resolved here - that needs a {@code KnowledgeBase}, and it is asserted against real
 * accounts in {@code tl-ai-demo}'s {@code SecDemoMcpEndpointTest}. What these tests cover is the
 * composition and the answers a caller gets.
 * </p>
 */
public class TestMcpAuthentication extends TestCase {

	/**
	 * Instantiating a {@link ServerAuthentication} resolves its implementation through the
	 * TypeIndex, and the refusal messages are read from the resource bundles.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestMcpAuthentication.class));
	}

	/**
	 * With no authentication configured, every request is refused.
	 *
	 * <p>
	 * {@code 503} and not {@code 401}: there is no scheme to challenge with, and no credential the
	 * caller could add would change the answer. What the caller needs to know is that the operator
	 * has to configure something.
	 * </p>
	 */
	public void testNothingConfiguredServesNobody() throws IOException {
		Authenticator authenticator = EndpointAuthentication.build("/mcp/*", Map.of(), List.of());

		Recorded recorded = refusalOf(authenticator);

		assertEquals("Nothing configured must refuse, not pass.",
			HttpServletResponse.SC_SERVICE_UNAVAILABLE, recorded.status);
		assertTrue("The answer must name what to configure: " + recorded.message,
			recorded.message.contains("authentication"));
	}

	/**
	 * The refusal of an unconfigured endpoint is not an {@code AlwaysAuthenticated} in disguise.
	 *
	 * <p>
	 * Asserted separately from the status code because this is the invariant, not the code: whatever
	 * the answer looks like, it must never be "authenticated, no account", which is how the framework
	 * expresses "run this in system context".
	 * </p>
	 */
	public void testNothingConfiguredNeverReturnsAnAccount() {
		Authenticator authenticator = EndpointAuthentication.build("/mcp/*", Map.of(), List.of());
		Recorded recorded = new Recorded();

		try {
			Person account = authenticator.authenticate(HttpDoubles.request(recorded),
				HttpDoubles.response(recorded));
			fail("An unconfigured endpoint must throw, and it returned " + account + " instead.");
		} catch (AuthenticationFailure expected) {
			// The only acceptable outcome.
		}
	}

	/**
	 * A configured HTTP basic scheme challenges a request that carries no credential.
	 *
	 * <p>
	 * {@code 401} plus {@code WWW-Authenticate: Basic}, both produced by the framework's own
	 * {@code ResponseEnhancer} - which is the reason this reuses the framework instead of writing a
	 * credential mechanism: the correct challenge per scheme comes for free.
	 * </p>
	 */
	public void testMissingCredentialIsChallenged() throws IOException {
		Authenticator authenticator = EndpointAuthentication.build("/mcp/*", Map.of("basic", basicAuthentication()), List.of());

		Recorded recorded = refusalOf(authenticator);

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, recorded.status);
		String challenge = recorded.responseHeader("WWW-Authenticate");
		assertNotNull("A 401 must tell the client how to authenticate.", challenge);
		assertTrue("The challenge must name the scheme: " + challenge, challenge.startsWith("Basic"));
	}

	/**
	 * An API key that is not among the configured secrets is refused.
	 */
	public void testUnknownApiKeyIsRefused() throws IOException {
		Authenticator authenticator =
			EndpointAuthentication.build("/mcp/*", Map.of("keys", apiKeyAuthentication()), List.of());

		Recorded recorded = new Recorded();
		recorded.requestHeaders.put("X-Api-Key", "not-a-configured-key");

		assertRefused(authenticator, recorded);
		assertTrue("An API key scheme with no secret must not authenticate anybody.",
			recorded.status >= 400);
	}

	/**
	 * A request without the configured API key header is refused.
	 */
	public void testMissingApiKeyHeaderIsRefused() throws IOException {
		Authenticator authenticator =
			EndpointAuthentication.build("/mcp/*", Map.of("keys", apiKeyAuthentication()), List.of());

		Recorded recorded = refusalOf(authenticator);

		assertTrue("A missing API key must be refused: " + recorded.status, recorded.status >= 400);
	}

	/**
	 * Several configured schemes are alternatives, so the second still answers when the first cannot.
	 *
	 * <p>
	 * The composition the <i>OpenAPI</i> server uses for an operation that accepts several schemes:
	 * a caller presents one credential, not all of them. Asserted through the failure, because a
	 * success would need an account.
	 * </p>
	 */
	public void testSchemesAreAlternatives() throws IOException {
		Authenticator authenticator = EndpointAuthentication.build("/mcp/*", 
			Map.of("basic", basicAuthentication(), "keys", apiKeyAuthentication()), List.of());

		Recorded recorded = refusalOf(authenticator);

		assertTrue("A request with no credential at all must still be refused.", recorded.status >= 400);
		assertNotNull("Both schemes failed, so the answer must state the reasons.", recorded.message);
	}

	/** An HTTP basic scheme authenticating against the accounts' own password database. */
	private static ServerAuthentication.Config<?> basicAuthentication() {
		BasicAuthentication.Config<?> config =
			TypedConfiguration.newConfigItem(BasicAuthentication.Config.class);
		config.setDomain("basic");
		config.setInUserContext(true);
		return config;
	}

	/** An API key scheme reading the key from a header. */
	private static ServerAuthentication.Config<?> apiKeyAuthentication() {
		APIKeyAuthentication.Config<?> config =
			TypedConfiguration.newConfigItem(APIKeyAuthentication.Config.class);
		config.setDomain("keys");
		config.setPosition(APIKeyPosition.HEADER);
		config.setParameterName("X-Api-Key");
		return config;
	}

	/**
	 * Authenticates an empty request and records how the refusal was answered.
	 */
	private static Recorded refusalOf(Authenticator authenticator) throws IOException {
		Recorded recorded = new Recorded();
		assertRefused(authenticator, recorded);
		return recorded;
	}

	private static void assertRefused(Authenticator authenticator, Recorded recorded) throws IOException {
		try {
			Person account = authenticator.authenticate(HttpDoubles.request(recorded),
				HttpDoubles.response(recorded));
			fail("The request must not authenticate; it resolved to " + account + ".");
		} catch (AuthenticationFailure failure) {
			failure.enhanceResponse(HttpDoubles.response(recorded), recorded.path);
		}
	}
}
