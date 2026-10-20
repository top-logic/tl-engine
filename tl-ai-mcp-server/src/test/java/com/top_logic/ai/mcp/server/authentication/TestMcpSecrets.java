/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.ai.mcp.server.authentication.HttpDoubles.Recorded;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.tl.ai.core.access.EndpointAuthentication;
import com.top_logic.tl.ai.core.access.EndpointSecret;

/**
 * Tests that the secrets {@code doc/security/mcp-endpoint.md} documents are read from an
 * application configuration file and reach the authenticator (issue #672).
 *
 * <p>
 * The engine's secret types may only stand in the configuration of its <i>OpenAPI</i> server, so a
 * documented {@code <api-key-server-secret>} on {@code MCPServerService} stopped the boot. The
 * configuration is therefore read here exactly as the application reads it: from the XML of a
 * configuration file, not from items a test builds in code.
 * </p>
 *
 * <p>
 * No account is resolved here - that needs a {@code KnowledgeBase}. The account half is asserted in
 * {@code tl-ai-demo}'s {@code SecDemoMcpEndpointTest}, from the same XML. What is asserted here is
 * that the configured key is the key the authenticator checks: a wrong key is refused as a wrong
 * key ({@code 401}), not as a scheme without a secret ({@code 500}).
 * </p>
 */
public class TestMcpSecrets extends TestCase {

	/**
	 * The API-key configuration of {@code doc/security/mcp-endpoint.md}: the content of the
	 * {@code <instance>} element of {@code MCPServerService}.
	 */
	public static final String DOCUMENTED_API_KEY_CONFIGURATION = ""
		+ "  <authentications>\n"
		+ "    <api-key-authentication domain=\"mcp\" position=\"header\" parameter-name=\"X-Api-Key\"/>\n"
		+ "  </authentications>\n"
		+ "  <secrets>\n"
		+ "    <api-key-server-secret domain=\"mcp\"\n"
		+ "      api-key=\"unencrypted:some-key\"\n"
		+ "      user-id=\"mcp-service\"\n"
		+ "      description=\"The MCP client of the reporting pipeline.\"/>\n"
		+ "  </secrets>";

	/**
	 * Reading a configuration resolves its types through the TypeIndex, and the refusal messages
	 * are read from the resource bundles.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestMcpSecrets.class));
	}

	/**
	 * The documented API-key configuration is read from XML, and its key is the one checked.
	 */
	public void testDocumentedApiKeyConfigurationIsRead() throws Exception {
		MCPServerService.Config<?> config = read(DOCUMENTED_API_KEY_CONFIGURATION);

		assertEquals(1, config.getSecrets().size());
		EndpointSecret.ApiKey secret = (EndpointSecret.ApiKey) config.getSecrets().get(0);
		assertEquals("mcp", secret.getDomain());
		assertEquals("some-key", secret.getAPIKey());
		assertEquals("mcp-service", secret.getUserId());

		Authenticator authenticator =
			EndpointAuthentication.build("/mcp/*", config.getAuthentications(), config.getSecrets());

		Recorded recorded = new Recorded();
		recorded.requestHeaders.put("X-Api-Key", "not-the-configured-key");
		assertRefused(authenticator, recorded);
		assertEquals("A wrong key must be refused as a wrong key. A 500 says the scheme found no"
			+ " secret, i.e. the configured one did not reach it.",
			HttpServletResponse.SC_UNAUTHORIZED, recorded.status);
	}

	/**
	 * The OAuth client-credentials secret is read from XML on the MCP service as well.
	 */
	public void testClientCredentialsSecretIsRead() throws Exception {
		MCPServerService.Config<?> config = read(""
				+ "  <authentications>\n"
			+ "    <client-credentials-authentication domain=\"oidc\" in-user-context=\"true\""
			+ "      username-field=\"preferred_username\">\n"
			+ "      <uri-provider class=\"com.top_logic.service.openapi.common.authentication.oauth.DefaultURIProvider\""
			+ "        introspection-url=\"https://idp.example.com/introspect\""
			+ "        token-url=\"https://idp.example.com/token\"/>\n"
			+ "    </client-credentials-authentication>\n"
			+ "  </authentications>\n"
			+ "  <secrets>\n"
			+ "    <client-credentials-server-secret domain=\"oidc\">\n"
			+ "      <client-secret client-id=\"mcp\" password=\"unencrypted:pw\"/>\n"
			+ "    </client-credentials-server-secret>\n"
			+ "  </secrets>");

		EndpointSecret.ClientCredentials secret = (EndpointSecret.ClientCredentials) config.getSecrets().get(0);
		assertEquals("oidc", secret.getDomain());
		assertEquals("mcp", secret.getClientSecret().getClientId());
		assertEquals("pw", secret.getClientSecret().getPassword());

		Authenticator authenticator =
			EndpointAuthentication.build("/mcp/*", config.getAuthentications(), config.getSecrets());
		Recorded recorded = new Recorded();
		assertRefused(authenticator, recorded);
		assertEquals("A request without a token must be challenged. A 500 says the scheme found no"
			+ " secret, i.e. the configured one did not reach it.",
			HttpServletResponse.SC_UNAUTHORIZED, recorded.status);
	}

	/**
	 * Reads the {@code MCPServerService} configuration from an application configuration file with
	 * the given content of its {@code <instance>} element.
	 */
	public static MCPServerService.Config<?> read(String instance) throws ConfigurationException {
		String file = "<application config:interface=\"com.top_logic.basic.config.ApplicationConfig$Config\"\n"
			+ "  xmlns:config=\"http://www.top-logic.com/ns/config/6.0\">\n"
			+ "  <services>\n"
			+ "    <config service-class=\"" + MCPServerService.class.getName() + "\">\n"
			+ "      <instance class=\"" + MCPServerService.class.getName() + "\">\n"
			+ instance + "\n"
			+ "      </instance>\n"
			+ "    </config>\n"
			+ "  </services>\n"
			+ "</application>\n";
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class);
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("application", descriptor));
		List<CharacterContent> sources = new ArrayList<>();
		sources.add(CharacterContents.newContent(file));
		reader.setSources(sources);
		ApplicationConfig.Config config = (ApplicationConfig.Config) reader.read();
		return (MCPServerService.Config<?>) config.getServices().get(MCPServerService.class).getInstance();
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
