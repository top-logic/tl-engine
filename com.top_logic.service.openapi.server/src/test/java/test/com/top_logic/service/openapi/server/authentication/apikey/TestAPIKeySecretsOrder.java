/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.authentication.apikey;

import java.lang.reflect.Proxy;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import jakarta.servlet.http.HttpServletRequest;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.server.authentication.apikey.ServerAPIKeySecret;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.conf.ServerSecret;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.util.model.ModelService;

/**
 * Test that the {@link OpenApiServer.Config#getSecrets()} of an {@link OpenApiServer}
 * configuration do not depend on the position of the {@link OpenApiServer.Config#SECRETS}
 * element relative to the {@link OpenApiServer.Config#PATHS} element.
 */
@SuppressWarnings("javadoc")
public class TestAPIKeySecretsOrder extends TestCase {

	private static final String DOMAIN = "apikey";

	private static final String HEADER = "X-API-Key";

	private static final String API_KEY = "secret-api-key";

	private static final String AUTHENTICATIONS =
		"<authentications>"
			+ "<api-key-authentication domain='" + DOMAIN + "' parameter-name='" + HEADER + "' position='header'/>"
		+ "</authentications>";

	private static final String PATHS =
		"<paths>"
			+ "<PathItem path='/ping'>"
				+ "<operations>"
					+ "<operation method='GET' authentication='" + DOMAIN + "'>"
						+ "<implementation><operation>\"pong\"</operation></implementation>"
					+ "</operation>"
				+ "</operations>"
			+ "</PathItem>"
		+ "</paths>";

	public void testSecretsBeforePaths() throws Exception {
		OpenApiServer.Config<?> config = parse(AUTHENTICATIONS + secrets() + PATHS);
		assertSecretsFound(config);
	}

	public void testSecretsAfterPaths() throws Exception {
		OpenApiServer.Config<?> config = parse(AUTHENTICATIONS + PATHS + secrets());
		assertSecretsFound(config);
	}

	public void testSameSecretsInBothOrders() throws Exception {
		List<ServerSecret> before = parse(AUTHENTICATIONS + secrets() + PATHS).getSecrets();
		List<ServerSecret> after = parse(AUTHENTICATIONS + PATHS + secrets()).getSecrets();

		assertEquals(1, before.size());
		assertEquals(1, after.size());
		ServerAPIKeySecret beforeSecret = (ServerAPIKeySecret) before.get(0);
		ServerAPIKeySecret afterSecret = (ServerAPIKeySecret) after.get(0);
		assertEquals(beforeSecret.getDomain(), afterSecret.getDomain());
		assertEquals(beforeSecret.getAPIKey(), afterSecret.getAPIKey());
		assertEquals(beforeSecret.getUserId(), afterSecret.getUserId());
	}

	private static String secrets() {
		return "<secrets>"
			+ "<api-key-server-secret domain='" + DOMAIN + "' api-key='" + ConfigurationEncryption.encrypt(API_KEY)
			+ "'/>"
			+ "</secrets>";
	}

	private void assertSecretsFound(OpenApiServer.Config<?> config) throws AuthenticationFailure {
		List<ServerSecret> secrets = config.getSecrets();
		assertEquals(1, secrets.size());
		ServerAPIKeySecret secret = (ServerAPIKeySecret) secrets.get(0);
		assertEquals(DOMAIN, secret.getDomain());
		assertEquals(API_KEY, secret.getAPIKey());

		// Resolve the authenticator as the server does for an operation.
		OperationByMethod operation = config.getPaths().get(0).getOperations().values().iterator().next();
		assertEquals(List.of(DOMAIN), operation.getAuthentication());
		ServerAuthentication.Config<?> authentication = config.getAuthentications().get(DOMAIN);
		assertTrue(authentication instanceof APIKeyAuthentication.Config<?>);

		Authenticator authenticator = TypedConfigUtil.createInstance(authentication).createAuthenticator(secrets);

		assertNull(authenticator.authenticate(request(API_KEY), null));
	}

	private static OpenApiServer.Config<?> parse(String content) throws ConfigurationException {
		String xml = "<instance class='" + OpenApiServer.class.getName() + "' base-url='/api'>" + content
			+ "</instance>";
		return TypedConfiguration.parse("instance", OpenApiServer.Config.class, CharacterContents.newContent(xml));
	}

	/**
	 * A {@link HttpServletRequest} that carries the given API key in the {@link #HEADER} header.
	 */
	private static HttpServletRequest request(String apiKey) {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				if ("getHeader".equals(method.getName())) {
					return HEADER.equals(args[0]) ? apiKey : null;
				}
				throw new UnsupportedOperationException(method.getName());
			});
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAPIKeySecretsOrder.class,
			ServiceTestSetup.createStarterFactoryForModules(
				SearchBuilder.Module.INSTANCE,
				ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}
}
