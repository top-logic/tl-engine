/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.oauth.server;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.BearerTokenError;
import com.nimbusds.oauth2.sdk.token.Tokens;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.oauth.server.OAuthAccessTokenAuthentication;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;

/**
 * Test of {@link OAuthAccessTokenAuthentication}: which access tokens are accepted at an
 * endpoint, and how a refusal is answered.
 */
public class TestOAuthAccessTokenAuthentication extends AbstractOAuthServerTest {

	/**
	 * A token issued for the endpoint acts as the person who authorized the client.
	 */
	public void testValidToken() throws Exception {
		Tokens tokens = signIn(register(REDIRECT_URI));

		Person person = authenticator(RESOURCE_PATH)
			.authenticate(request(tokens.getBearerAccessToken()), response(new LinkedHashMap<>()));
		assertEquals(root(), person);
	}

	/**
	 * The resource can be configured as absolute URI as well.
	 */
	public void testValidTokenWithAbsoluteResource() throws Exception {
		Tokens tokens = signIn(register(REDIRECT_URI));

		Person person = authenticator(resource(RESOURCE_PATH).toString())
			.authenticate(request(tokens.getBearerAccessToken()), response(new LinkedHashMap<>()));
		assertEquals(root(), person);
	}

	/**
	 * An expired token is refused.
	 */
	public void testExpiredToken() throws Exception {
		Tokens tokens = signIn(register(REDIRECT_URI));
		expireToken(tokens.getAccessToken().getValue());

		assertRefused(RESOURCE_PATH, tokens.getBearerAccessToken(),
			BearerTokenError.INVALID_TOKEN);
	}

	/**
	 * A token of a revoked grant is refused.
	 */
	public void testRevokedToken() throws Exception {
		Tokens tokens = signIn(register(REDIRECT_URI));
		server().revoke(grantOf(tokens.getAccessToken().getValue()));

		assertRefused(RESOURCE_PATH, tokens.getBearerAccessToken(),
			BearerTokenError.INVALID_TOKEN);
	}

	/**
	 * A token issued for another endpoint is refused.
	 */
	public void testTokenForOtherResource() throws Exception {
		Tokens tokens = signIn(register(REDIRECT_URI), "/a2a");

		assertRefused(RESOURCE_PATH, tokens.getBearerAccessToken(),
			BearerTokenError.INVALID_TOKEN);
	}

	/**
	 * A refresh token is no access token.
	 */
	public void testRefreshTokenRefused() throws Exception {
		Tokens tokens = signIn(register(REDIRECT_URI));

		assertRefused(RESOURCE_PATH,
			new BearerAccessToken(tokens.getRefreshToken().getValue()), BearerTokenError.INVALID_TOKEN);
	}

	/**
	 * A request without a token gets a challenge without an error code.
	 */
	public void testNoToken() throws Exception {
		Map<String, String> headers = assertRefused(RESOURCE_PATH, null,
			BearerTokenError.MISSING_TOKEN);
		String challenge = headers.get(AuthorizationUtil.AUTHENTICATE_RESPONSE_HEADER_NAME);
		assertFalse(challenge, challenge.contains(BearerTokenError.INVALID_TOKEN.getCode()));
	}

	private Map<String, String> assertRefused(String resource, BearerAccessToken token, BearerTokenError expected)
			throws Exception {
		Map<String, String> headers = new LinkedHashMap<>();
		int[] status = new int[1];
		HttpServletResponse response = response(headers, status);
		try {
			authenticator(resource).authenticate(request(token), response);
			fail("Refusal expected.");
		} catch (AuthenticationFailure ex) {
			ex.enhanceResponse(response, resource);
		}
		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status[0]);
		String challenge = headers.get(AuthorizationUtil.AUTHENTICATE_RESPONSE_HEADER_NAME);
		assertNotNull("Challenge expected.", challenge);
		assertTrue(challenge, challenge.startsWith(AccessTokenType.BEARER.getValue()));
		BearerTokenError parsed = BearerTokenError.parse(challenge);
		assertEquals(expected.getCode(), parsed.getCode());
		if (token != null) {
			assertFalse("The challenge must not contain the token.", challenge.contains(token.getValue()));
		}
		return headers;
	}

	private static Authenticator authenticator(String resource) {
		OAuthAccessTokenAuthentication.Config<?> config =
			TypedConfiguration.newConfigItem(OAuthAccessTokenAuthentication.Config.class);
		config.update(config.descriptor().getProperty(OAuthAccessTokenAuthentication.Config.RESOURCE), resource);
		return TypedConfigUtil.createInstance(config).createAuthenticator(List.of());
	}

	/**
	 * A request to the application at the URL the servlet runner reaches it with.
	 */
	private HttpServletRequest request(BearerAccessToken token) throws Exception {
		URI application = URI.create(context().applicationUrl());
		return (HttpServletRequest) Proxy.newProxyInstance(TestOAuthAccessTokenAuthentication.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "getHeader":
						return AuthorizationUtil.AUTHORIZATION_HEADER_NAME.equalsIgnoreCase((String) args[0])
							&& token != null ? token.toAuthorizationHeader() : null;
					case "getScheme":
						return application.getScheme();
					case "getServerName":
						return application.getHost();
					case "getServerPort":
						return Integer.valueOf(application.getPort());
					case "getContextPath":
						return application.getRawPath();
					case "toString":
						return "request";
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	private static HttpServletResponse response(Map<String, String> headers) {
		return response(headers, new int[1]);
	}

	private static HttpServletResponse response(Map<String, String> headers, int[] status) {
		return (HttpServletResponse) Proxy.newProxyInstance(TestOAuthAccessTokenAuthentication.class.getClassLoader(),
			new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "setHeader":
					case "addHeader":
						headers.put((String) args[0], (String) args[1]);
						return null;
					case "sendError":
					case "setStatus":
						status[0] = ((Integer) args[0]).intValue();
						return null;
					case "toString":
						return "response";
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return suite(TestOAuthAccessTokenAuthentication.class);
	}

}
