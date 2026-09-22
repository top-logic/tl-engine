/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.config;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import jakarta.servlet.ServletContext;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import com.nimbusds.openid.connect.sdk.Prompt;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;

/**
 * Tests the clients {@link Pac4jConfigFactory} builds from application configuration, and the
 * client names it resolves back to that configuration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPac4jConfigFactory extends TestCase {

	/** The {@link ClientConfigurator.Config#getName() name} of the configured OIDC client. */
	private static final String OIDC_CLIENT = "test-oidc";

	/** The {@link ClientConfigurator.Config#getName() name} of the configured non-OIDC client. */
	private static final String ANONYMOUS_CLIENT = "test-anonymous";

	/** The context path the {@link ServletContext} of this test reports. */
	private static final String CONTEXT_PATH = "/test-app";

	/**
	 * {@link Pac4jConfigFactory} handing out the pac4j configuration it builds without being
	 * started as a service.
	 */
	public static class ExposedFactory extends Pac4jConfigFactory<Pac4jConfigFactory.Config<?>> {

		/**
		 * Creates an {@link ExposedFactory} from configuration.
		 *
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		public ExposedFactory(InstantiationContext context, Pac4jConfigFactory.Config<?> config) {
			super(context, config);
		}

		/**
		 * The clients this factory registers, by {@link Client#getName() name}, each completed with
		 * the settings the {@link Clients} container hands out.
		 */
		public Map<String, Client> clients(ServletContext context) {
			org.pac4j.core.config.Config config = buildPac4jConfig(context);
			Map<String, Client> result = new LinkedHashMap<>();
			for (Client client : config.getClients().findAllClients()) {
				result.put(client.getName(), client);
			}
			return result;
		}

	}

	private ExposedFactory _factory;

	private Map<String, Client> _clients;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPac4jConfigFactory.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("config",
			TypedConfiguration.getConfigurationDescriptor(Pac4jConfigFactory.Config.class));
		BinaryContent source =
			new ClassRelativeBinaryContent(TestPac4jConfigFactory.class, "test-clients.config.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		Pac4jConfigFactory.Config<?> config = (Pac4jConfigFactory.Config<?>) reader.read();
		context.checkErrors();

		_factory = new ExposedFactory(context, config);
		context.checkErrors();
		_clients = _factory.clients(servletContext());
	}

	@Override
	protected void tearDown() throws Exception {
		_clients = null;
		_factory = null;

		super.tearDown();
	}

	/**
	 * Tests that a configured OIDC client is joined by a re-authentication client of its own, and
	 * that an authentication which cannot re-authenticate contributes none.
	 */
	public void testRegisteredClients() {
		assertEquals("Every configured client, plus the re-authentication client of the OIDC one.",
			List.of(OIDC_CLIENT, ANONYMOUS_CLIENT, Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT)),
			List.copyOf(_clients.keySet()));
		assertTrue("The login clients come first, so that a request naming no client reaches one.",
			_clients.get(ANONYMOUS_CLIENT) instanceof AnonymousClient);
	}

	/**
	 * Tests that the re-authentication client demands a fresh authentication, and otherwise carries
	 * the settings of the client it is derived from.
	 */
	public void testReauthenticationClient() {
		OidcConfiguration login = oidcConfig(OIDC_CLIENT);
		OidcConfiguration reauthentication = oidcConfig(Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT));

		assertNotSame("The derived client has a configuration of its own.", login, reauthentication);

		assertEquals("No authentication that happened before the request is accepted.",
			Integer.valueOf(0), reauthentication.getMaxAge());
		assertEquals("The provider authenticates the user itself.",
			Prompt.Type.LOGIN.toString(), reauthentication.getCustomParam(OidcConfiguration.PROMPT));

		assertEquals(login.getClientId(), reauthentication.getClientId());
		assertEquals(login.getSecret(), reauthentication.getSecret());
		assertEquals(login.getDiscoveryURI(), reauthentication.getDiscoveryURI());
		String callbackUrl = ((OidcClient) _clients.get(OIDC_CLIENT)).getCallbackUrl();
		assertEquals(CONTEXT_PATH + "/servlet/callback", callbackUrl);
		assertEquals("Both clients answer at the same callback URL, told apart by the client name"
			+ " pac4j appends to it.", callbackUrl,
			((OidcClient) _clients.get(Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT))).getCallbackUrl());
	}

	/**
	 * Tests that the login client keeps accepting the single-sign-on session of the provider.
	 */
	public void testLoginClientUnchanged() {
		OidcConfiguration login = oidcConfig(OIDC_CLIENT);

		assertNull("A login accepts an earlier authentication.", login.getMaxAge());
		assertNull("A login does not force the provider to ask again.",
			login.getCustomParam(OidcConfiguration.PROMPT));
	}

	/**
	 * Tests that a re-authentication client name is answered by the configuration of the client it
	 * is derived from, so that its callback maps the external user to an account like a login does.
	 */
	public void testConfigurationOfDerivedName() {
		ClientConfigurator.Config<?> expected = _factory.getClientConfig(OIDC_CLIENT);

		assertNotNull(expected);
		assertSame(expected, _factory.getClientConfig(Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT)));
		assertSame(expected.getUserMapping(),
			_factory.getUserMapping(Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT)));
		assertSame(expected.getUserNameExtractor(),
			_factory.getUserNameExtractor(Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT)));
	}

	/**
	 * Tests the names a re-authentication client is derived under and resolved back from.
	 */
	public void testClientNames() {
		assertEquals(OIDC_CLIENT + Pac4jConfigFactory.REAUTHENTICATION_SUFFIX,
			Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT));
		assertEquals(OIDC_CLIENT,
			Pac4jConfigFactory.getConfiguredName(Pac4jConfigFactory.getReauthenticationName(OIDC_CLIENT)));
		assertEquals("A configured name stands for itself.", OIDC_CLIENT,
			Pac4jConfigFactory.getConfiguredName(OIDC_CLIENT));
		assertNull(Pac4jConfigFactory.getConfiguredName(null));
	}

	private OidcConfiguration oidcConfig(String clientName) {
		Client client = _clients.get(clientName);
		assertTrue("Client '" + clientName + "' is an OIDC client.", client instanceof OidcClient);
		return ((OidcClient) client).getConfiguration();
	}

	/**
	 * A {@link ServletContext} that knows nothing but the path the application is deployed under.
	 */
	private static ServletContext servletContext() {
		return (ServletContext) Proxy.newProxyInstance(TestPac4jConfigFactory.class.getClassLoader(),
			new Class<?>[] { ServletContext.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "getContextPath" -> CONTEXT_PATH;
				case "toString" -> ServletContext.class.getName() + "(" + CONTEXT_PATH + ")";
				case "hashCode" -> Integer.valueOf(System.identityHashCode(proxy));
				case "equals" -> Boolean.valueOf(proxy == args[0]);
				default -> null;
			});
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestPac4jConfigFactory.class, TypeIndex.Module.INSTANCE);
	}

}
