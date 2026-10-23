/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.authentication;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import jakarta.servlet.http.HttpServletRequest;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.accesscontrol.AuthorizationUtil;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.server.authentication.http.basic.BasicTechnicalAuthenticator;
import com.top_logic.service.openapi.server.authentication.http.basic.I18NConstants;
import com.top_logic.service.openapi.server.authentication.http.basic.ServerBasicAuthSecret;
import com.top_logic.service.openapi.server.authentication.oauth.ServerCredentialSecret;

/**
 * Test that the server reads the secrets requests are verified against from their secret sources,
 * at each verification.
 */
@SuppressWarnings("javadoc")
public class TestServerSecretSources extends TestCase {

	private static final String DOMAIN = "test-domain";

	private static final String USER = "technical-user";

	private static final String PASSWORD = "user-s3cr3t";

	private static final String VARIABLE = "TL_TEST_OPENAPI_SERVER_SECRET";

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	public void testBasicAttributeForm() throws Exception {
		Authenticator authenticator = basicAuthenticator(
			"<login user='" + USER + "' password='unencrypted:" + PASSWORD + "'/>");

		assertNull(authenticator.authenticate(basicRequest(USER, PASSWORD), null));
		assertWrongData(authenticator, basicRequest(USER, "wrong"));
		assertWrongData(authenticator, basicRequest("other-user", PASSWORD));
	}

	public void testBasicEncryptedAttributeForm() throws Exception {
		Authenticator authenticator = basicAuthenticator(
			"<login user='" + USER + "' password='" + ConfigurationEncryption.encrypt(PASSWORD) + "'/>");

		assertNull(authenticator.authenticate(basicRequest(USER, PASSWORD), null));
		assertWrongData(authenticator, basicRequest(USER, PASSWORD + "x"));
	}

	public void testBasicEnvironmentReadAtVerification() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		Authenticator authenticator = basicAuthenticator(
			"<login user='" + USER + "'>"
				+ "<password class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>"
				+ "</login>");

		assertNull(authenticator.authenticate(basicRequest(USER, PASSWORD), null));
		assertWrongData(authenticator, basicRequest(USER, "wrong"));

		System.setProperty(VARIABLE, "rotated");
		assertNull("A rotated password must be accepted without restart.",
			authenticator.authenticate(basicRequest(USER, "rotated"), null));
		assertWrongData(authenticator, basicRequest(USER, PASSWORD));
	}

	public void testBasicUnavailablePasswordRejects() throws Exception {
		Authenticator authenticator = basicAuthenticator(
			"<login user='" + USER + "'>"
				+ "<password class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>"
				+ "</login>");

		assertWrongData(authenticator, basicRequest(USER, PASSWORD));
	}

	public void testBasicSeveralPasswordsOfUser() throws Exception {
		System.setProperty(VARIABLE, "next-password");
		Authenticator authenticator = basicAuthenticator(
			"<login user='" + USER + "' password='unencrypted:" + PASSWORD + "'/>",
			"<login user='" + USER + "'>"
				+ "<password class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>"
				+ "</login>");

		assertNull(authenticator.authenticate(basicRequest(USER, PASSWORD), null));
		assertNull(authenticator.authenticate(basicRequest(USER, "next-password"), null));
		assertWrongData(authenticator, basicRequest(USER, "wrong"));
	}

	public void testClientSecretAttributeForm() throws Exception {
		ServerCredentialSecret secret = parse("client-credentials-server-secret", ServerCredentialSecret.class,
			"<client-credentials-server-secret domain='" + DOMAIN + "'>"
				+ "<client-secret client-id='my-app' password='unencrypted:client-s3cr3t'/>"
				+ "</client-credentials-server-secret>");

		assertEquals("my-app", secret.getClientSecret().getClientId());
		LiteralSecretSource.Config password = (LiteralSecretSource.Config) secret.getClientSecret().getPassword();
		assertEquals("client-s3cr3t", password.getValue());
	}

	private static void assertWrongData(Authenticator authenticator, HttpServletRequest request) {
		try {
			authenticator.authenticate(request, null);
			fail("Authentication must fail.");
		} catch (AuthenticationFailure ex) {
			assertEquals(I18NConstants.AUTH_FAILED_WRONG_AUTHENTICATION_DATA, ex.getErrorKey());
		}
	}

	private static Authenticator basicAuthenticator(String... logins) throws ConfigurationException {
		BasicAuthentication.Config<?> authentication = TypedConfiguration.newConfigItem(BasicAuthentication.Config.class);
		authentication.setDomain(DOMAIN);
		authentication.setInUserContext(false);
		List<ServerBasicAuthSecret> secrets = new ArrayList<>();
		for (String login : logins) {
			secrets.add(parse("basic-auth-server-secret", ServerBasicAuthSecret.class,
				"<basic-auth-server-secret domain='" + DOMAIN + "'>" + login + "</basic-auth-server-secret>"));
		}
		Authenticator result = TypedConfigUtil.createInstance(authentication).createAuthenticator(secrets);
		assertTrue(result instanceof BasicTechnicalAuthenticator);
		return result;
	}

	private static <T extends ConfigurationItem> T parse(String tag, Class<T> type, String xml)
			throws ConfigurationException {
		return type.cast(TypedConfiguration.parse(tag, type, CharacterContents.newContent(xml)));
	}

	/**
	 * A {@link HttpServletRequest} that sends the given credentials as <i>BasicAuth</i>
	 * authorization header.
	 */
	private static HttpServletRequest basicRequest(String user, String password) {
		String value = AuthorizationUtil.createBasicAuthAuthorizationHeader(user, password);
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				if ("getHeader".equals(method.getName())) {
					return AuthorizationUtil.AUTHORIZATION_HEADER_NAME.equals(args[0]) ? value : null;
				}
				throw new UnsupportedOperationException(method.getName());
			});
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestServerSecretSources.class,
			ServiceTestSetup.createStarterFactoryForModules(PersonManager.Module.INSTANCE));
	}

}
