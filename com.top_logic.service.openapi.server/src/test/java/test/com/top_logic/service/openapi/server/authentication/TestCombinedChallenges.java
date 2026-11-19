/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.authentication;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.impl.CombinedResponseEnhancer;
import com.top_logic.service.openapi.server.authentication.impl.DispatchingAuthenticator;

/**
 * Test that a request every one of several authenticators refused is answered with the challenges
 * of all of them ({@link DispatchingAuthenticator}, {@link CombinedResponseEnhancer}).
 */
@SuppressWarnings("javadoc")
public class TestCombinedChallenges extends TestCase {

	private static final String BEARER = "Bearer realm=\"app\", resource_metadata=\"https://host/app/.well-known/oauth-protected-resource/mcp\"";

	private static final String BASIC = "Basic realm=\"app\"";

	public void testAllChallengesInOrder() throws IOException {
		Answer answer = refuse(refusing(BEARER, 401).or(refusing(BASIC, 401)));

		assertEquals(401, answer.status);
		assertEquals(List.of(BEARER, BASIC), answer.challenges);
		assertTrue(answer.message, answer.message.contains("/mcp"));
	}

	public void testNestedCombination() throws IOException {
		Answer answer = refuse(refusing(BEARER, 401).or(refusing("Bearer realm=\"tokens\"", 401))
			.or(refusing(BASIC, 401)));

		assertEquals(List.of(BEARER, "Bearer realm=\"tokens\"", BASIC), answer.challenges);
	}

	public void testUnauthorizedWinsOverOtherStatus() throws IOException {
		Answer answer = refuse(refusing(null, 500).or(refusing(BASIC, 401)));

		assertEquals("A credential changes the answer, so the client is challenged.", 401, answer.status);
		assertEquals(List.of(BASIC), answer.challenges);

		Answer unavailable = refuse(refusing(null, 503).or(refusing(null, 500)));
		assertEquals(503, unavailable.status);
		assertEquals(List.of(), unavailable.challenges);
	}

	private static Answer refuse(Authenticator authenticator) throws IOException {
		Answer answer = new Answer();
		HttpServletResponse response = answer.response();
		try {
			authenticator.authenticate(request(), response);
			fail("Refusal expected.");
		} catch (AuthenticationFailure ex) {
			ex.enhanceResponse(response, "/mcp");
		}
		return answer;
	}

	private static Authenticator refusing(String challenge, int status) {
		return (req, resp) -> {
			AuthenticationFailure failure = new AuthenticationFailure(ResKey.text("Refused."));
			failure.setResponseEnhancer((response, ex, path) -> {
				if (challenge != null) {
					response.setHeader(AuthorizationUtil.AUTHENTICATE_RESPONSE_HEADER_NAME, challenge);
				}
				response.sendError(status, ex.getMessage());
			});
			throw failure;
		};
	}

	private static HttpServletRequest request() {
		return (HttpServletRequest) Proxy.newProxyInstance(TestCombinedChallenges.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
				throw new UnsupportedOperationException(method.getName());
			});
	}

	/**
	 * What the refusal answered.
	 */
	private static final class Answer {

		int status;

		String message;

		final List<String> challenges = new ArrayList<>();

		HttpServletResponse response() {
			return (HttpServletResponse) Proxy.newProxyInstance(TestCombinedChallenges.class.getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> {
					switch (method.getName()) {
						case "setHeader":
							if (AuthorizationUtil.AUTHENTICATE_RESPONSE_HEADER_NAME.equals(args[0])) {
								challenges.clear();
								challenges.add((String) args[1]);
							}
							return null;
						case "addHeader":
							if (AuthorizationUtil.AUTHENTICATE_RESPONSE_HEADER_NAME.equals(args[0])) {
								challenges.add((String) args[1]);
							}
							return null;
						case "sendError":
							status = ((Integer) args[0]).intValue();
							message = args.length > 1 ? (String) args[1] : null;
							return null;
						case "toString":
							return "response";
						default:
							throw new UnsupportedOperationException(method.getName());
					}
				});
		}
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestCombinedChallenges.class));
	}

}
