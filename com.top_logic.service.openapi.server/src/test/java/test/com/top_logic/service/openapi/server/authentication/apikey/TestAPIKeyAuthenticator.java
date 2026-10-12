/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.authentication.apikey;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import jakarta.servlet.http.HttpServletRequest;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthenticator;
import com.top_logic.service.openapi.server.authentication.apikey.I18NConstants;
import com.top_logic.service.openapi.server.authentication.apikey.ServerAPIKeySecret;
import com.top_logic.service.openapi.server.authentication.conf.ServerSecret;

/**
 * Tests for {@link APIKeyAuthenticator}.
 *
 * <p>
 * The authenticator is called outside of any interaction, as during the authentication of a
 * request.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAPIKeyAuthenticator extends TestCase {

	private static final String DOMAIN = "apikey";

	private static final String HEADER = "X-API-Key";

	private static final String KEY_ACCOUNT = "key-for-account";

	private static final String KEY_SYSTEM = "key-for-system";

	private static final String KEY_UNKNOWN_ACCOUNT = "key-for-unknown-account";

	private static final String UNKNOWN_ACCOUNT = "no-such-account-29774";

	private Authenticator _authenticator;

	private String _accountName;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertNull("Test requires that no interaction is active, as during request authentication.",
			ThreadContextManager.getInteraction());

		_accountName = PersonManager.getManager().getSuperUserName();

		APIKeyAuthentication.Config<?> authentication =
			TypedConfiguration.newConfigItem(APIKeyAuthentication.Config.class);
		authentication.setDomain(DOMAIN);
		authentication.setPosition(APIKeyPosition.HEADER);
		authentication.setParameterName(HEADER);

		List<ServerSecret> secrets = List.of(
			secret(KEY_ACCOUNT, _accountName),
			secret(KEY_SYSTEM, null),
			secret(KEY_UNKNOWN_ACCOUNT, UNKNOWN_ACCOUNT));

		_authenticator = TypedConfigUtil.createInstance(authentication).createAuthenticator(secrets);
		assertTrue(_authenticator instanceof APIKeyAuthenticator);
	}

	@Override
	protected void tearDown() throws Exception {
		_authenticator = null;
		super.tearDown();
	}

	public void testKeyForAccount() throws AuthenticationFailure {
		Person person = _authenticator.authenticate(request(KEY_ACCOUNT), null);

		assertNotNull(person);
		assertEquals(_accountName,
			ThreadContextManager.inSystemInteraction(TestAPIKeyAuthenticator.class, () -> person.getName()));
	}

	public void testKeyForSystemContext() throws AuthenticationFailure {
		assertNull(_authenticator.authenticate(request(KEY_SYSTEM), null));
	}

	public void testKeyForUnknownAccount() {
		assertFails(request(KEY_UNKNOWN_ACCOUNT),
			I18NConstants.ERROR_REQUEST_USER_DOES_NOT_EXIST__NAME.fill(UNKNOWN_ACCOUNT));
	}

	public void testUnknownKey() {
		assertFails(request("not-a-configured-key"), I18NConstants.AUTH_FAILED_INVALID_API_KEY__PARAMETER.fill(HEADER));
	}

	public void testMissingHeader() {
		assertFails(request(null), I18NConstants.AUTH_FAILED_NO_HEADER__PARAMETER.fill(HEADER));
	}

	private void assertFails(HttpServletRequest request, ResKey expectedError) {
		try {
			_authenticator.authenticate(request, null);
			fail("Authentication must fail.");
		} catch (AuthenticationFailure ex) {
			assertEquals(expectedError, ex.getErrorKey());
		}
	}

	private static ServerSecret secret(String apiKey, String userId) {
		ServerAPIKeySecret secret = TypedConfiguration.newConfigItem(ServerAPIKeySecret.class);
		set(secret, APIKeySecret.DOMAIN, DOMAIN);
		set(secret, APIKeySecret.API_KEY, apiKey);
		set(secret, ServerAPIKeySecret.USER_ID, userId);
		return secret;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	/**
	 * A {@link HttpServletRequest} that carries the given API key in the {@link #HEADER} header.
	 */
	private static HttpServletRequest request(String apiKey) {
		Map<String, String> headers = apiKey == null ? Map.of() : Map.of(HEADER, apiKey);
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				if ("getHeader".equals(method.getName())) {
					return headers.get(args[0]);
				}
				throw new UnsupportedOperationException(method.getName());
			});
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAPIKeyAuthenticator.class,
			ServiceTestSetup.createStarterFactoryForModules(PersonManager.Module.INSTANCE));
	}
}
