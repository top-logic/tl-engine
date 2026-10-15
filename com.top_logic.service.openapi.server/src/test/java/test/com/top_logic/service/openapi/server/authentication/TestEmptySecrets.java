/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.authentication;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import jakarta.servlet.http.HttpServletRequest;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.common.authentication.http.LoginCredentials;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthenticator;
import com.top_logic.service.openapi.server.authentication.apikey.ServerAPIKeySecret;
import com.top_logic.service.openapi.server.authentication.conf.ServerSecret;
import com.top_logic.service.openapi.server.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.server.authentication.http.basic.BasicTechnicalAuthenticator;
import com.top_logic.service.openapi.server.authentication.http.basic.ServerBasicAuthSecret;
import com.top_logic.service.openapi.server.authentication.impl.NeverAuthenticated;

/**
 * Test that secrets with an empty password or API key do not authenticate a request.
 *
 * <p>
 * Covers the technical user variant of {@link BasicAuthentication} and the
 * {@link APIKeyAuthentication}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestEmptySecrets extends TestCase {

	private static final String DOMAIN = "test-domain";

	private static final String HEADER = "X-API-Key";

	private static final String USER = "technical-user";

	private static final String OTHER_USER = "other-technical-user";

	private static final String PASSWORD = "valid-password";

	private static final String API_KEY = "valid-api-key";

	public void testBasicValidPassword() throws AuthenticationFailure {
		Authenticator authenticator = basicAuthenticator(basicSecret(USER, PASSWORD));
		assertTrue(authenticator instanceof BasicTechnicalAuthenticator);

		assertNull(authenticator.authenticate(basicRequest(USER, PASSWORD), null));
		assertFails(authenticator, basicRequest(USER, ""), basicWrongData());
	}

	public void testBasicOnlyEmptyPassword() {
		Authenticator authenticator = basicAuthenticator(basicSecret(USER, ""));
		assertTrue(authenticator instanceof NeverAuthenticated);

		assertFails(authenticator, basicRequest(USER, ""), missingSecret());
	}

	public void testBasicMixedPasswords() throws AuthenticationFailure {
		Authenticator authenticator = basicAuthenticator(basicSecret(USER, ""), basicSecret(OTHER_USER, PASSWORD));
		assertTrue(authenticator instanceof BasicTechnicalAuthenticator);

		assertNull(authenticator.authenticate(basicRequest(OTHER_USER, PASSWORD), null));
		assertFails(authenticator, basicRequest(USER, ""), basicWrongData());
	}

	public void testBasicEmptyPasswordRejectedAtRequest() {
		Authenticator authenticator = new BasicTechnicalAuthenticator(List.of(credentials(USER, "")));

		assertFails(authenticator, basicRequest(USER, ""), basicWrongData());
	}

	public void testAPIKeyValidKey() throws AuthenticationFailure {
		Authenticator authenticator = apiKeyAuthenticator(apiKeySecret(API_KEY));
		assertTrue(authenticator instanceof APIKeyAuthenticator);

		assertNull(authenticator.authenticate(apiKeyRequest(API_KEY), null));
		assertFails(authenticator, apiKeyRequest(""), invalidAPIKey());
	}

	public void testAPIKeyOnlyEmptyKey() {
		Authenticator authenticator = apiKeyAuthenticator(apiKeySecret(""));
		assertTrue(authenticator instanceof NeverAuthenticated);

		assertFails(authenticator, apiKeyRequest(""), missingSecret());
	}

	public void testAPIKeyMixedKeys() throws AuthenticationFailure {
		Authenticator authenticator = apiKeyAuthenticator(apiKeySecret(""), apiKeySecret(API_KEY));
		assertTrue(authenticator instanceof APIKeyAuthenticator);

		assertNull(authenticator.authenticate(apiKeyRequest(API_KEY), null));
		assertFails(authenticator, apiKeyRequest(""), invalidAPIKey());
	}

	public void testAPIKeyEmptyKeyRejectedAtRequest() {
		Authenticator authenticator = new APIKeyAuthenticator(APIKeyPosition.HEADER, HEADER, Map.of("", ""));

		assertFails(authenticator, apiKeyRequest(""), invalidAPIKey());
	}

	private static void assertFails(Authenticator authenticator, HttpServletRequest request, ResKey expectedError) {
		try {
			authenticator.authenticate(request, null);
			fail("Authentication must fail.");
		} catch (AuthenticationFailure ex) {
			assertEquals(expectedError, ex.getErrorKey());
		}
	}

	private static ResKey basicWrongData() {
		return com.top_logic.service.openapi.server.authentication.http.basic.I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA;
	}

	private static ResKey invalidAPIKey() {
		return com.top_logic.service.openapi.server.authentication.apikey.I18NConstants.AUTH_FAILED_INVALID_API_KEY__PARAMETER
			.fill(HEADER);
	}

	private static ResKey missingSecret() {
		return com.top_logic.service.openapi.server.authentication.impl.I18NConstants.ERROR_MISSING_AUTHENTICATION_SECRET;
	}

	private static Authenticator basicAuthenticator(ServerSecret... secrets) {
		BasicAuthentication.Config<?> authentication = TypedConfiguration.newConfigItem(BasicAuthentication.Config.class);
		authentication.setDomain(DOMAIN);
		authentication.setInUserContext(false);
		return TypedConfigUtil.createInstance(authentication).createAuthenticator(List.of(secrets));
	}

	private static Authenticator apiKeyAuthenticator(ServerSecret... secrets) {
		APIKeyAuthentication.Config<?> authentication =
			TypedConfiguration.newConfigItem(APIKeyAuthentication.Config.class);
		authentication.setDomain(DOMAIN);
		authentication.setPosition(APIKeyPosition.HEADER);
		authentication.setParameterName(HEADER);
		return TypedConfigUtil.createInstance(authentication).createAuthenticator(List.of(secrets));
	}

	private static ServerSecret basicSecret(String user, String password) {
		ServerBasicAuthSecret secret = TypedConfiguration.newConfigItem(ServerBasicAuthSecret.class);
		set(secret, SecretConfiguration.DOMAIN, DOMAIN);
		secret.setLogin(credentials(user, password));
		return secret;
	}

	private static LoginCredentials credentials(String user, String password) {
		LoginCredentials login = TypedConfiguration.newConfigItem(LoginCredentials.class);
		login.setUser(user);
		login.setPassword(password);
		return login;
	}

	private static ServerSecret apiKeySecret(String apiKey) {
		ServerAPIKeySecret secret = TypedConfiguration.newConfigItem(ServerAPIKeySecret.class);
		set(secret, APIKeySecret.DOMAIN, DOMAIN);
		set(secret, APIKeySecret.API_KEY, apiKey);
		return secret;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	/**
	 * A {@link HttpServletRequest} that sends the given credentials as <i>BasicAuth</i>
	 * authorization header.
	 */
	private static HttpServletRequest basicRequest(String user, String password) {
		return request(AuthorizationUtil.AUTHORIZATION_HEADER_NAME,
			AuthorizationUtil.createBasicAuthAuthorizationHeader(user, password));
	}

	/**
	 * A {@link HttpServletRequest} that carries the given API key in the {@link #HEADER} header.
	 */
	private static HttpServletRequest apiKeyRequest(String apiKey) {
		return request(HEADER, apiKey);
	}

	private static HttpServletRequest request(String header, String value) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				if ("getHeader".equals(method.getName())) {
					return header.equals(args[0]) ? value : null;
				}
				throw new UnsupportedOperationException(method.getName());
			});
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestEmptySecrets.class,
			ServiceTestSetup.createStarterFactoryForModules(PersonManager.Module.INSTANCE));
	}
}
