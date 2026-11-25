/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;

import org.junit.Assume;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Environment;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.vars.VariableExpander;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;

/**
 * Tests the {@link ClientConfigurator.Config#getLabel() labels} of the clients shipped in
 * <code>/WEB-INF/conf/pac4j-*.config.xml</code>, read with the aliases of their configuration
 * files <code>/WEB-INF/conf/pac4j-*.xml</code>.
 */
public class TestShippedClientLabels extends TestCase {

	/** The folder of the shipped client configurations. */
	private static final String CONF_FOLDER = "/WEB-INF/conf/";

	/** Suffix of the file defining the aliases of a client. */
	private static final String ALIAS_FILE_SUFFIX = ".xml";

	/** Suffix of the file defining a client. */
	private static final String CONFIG_FILE_SUFFIX = ".config.xml";

	/** The generic OpenID Connect client. */
	private static final String GENERIC_CLIENT = "pac4j-generic";

	/** The clients of a specific provider. */
	private static final List<String> PROVIDER_CLIENTS = List.of("pac4j-azure", "pac4j-google", "pac4j-keycloak");

	/** The environment variable with the name of the provider of the generic client. */
	private static final String LABEL_VARIABLE = "tl_pac_label";

	/** A provider name given in {@link #LABEL_VARIABLE}. */
	private static final String PROVIDER_NAME = "ACME";

	/**
	 * Without {@link #LABEL_VARIABLE}, the generic client signs in with "Single Sign-On".
	 */
	public void testGenericDefaultLabel() throws Exception {
		Assume.assumeTrue(LABEL_VARIABLE + " is set in the environment of the test.",
			Environment.getSystemPropertyOrEnvironmentVariable(LABEL_VARIABLE, null) == null);

		ResKey label = label(GENERIC_CLIENT);
		assertEquals("Sign in with Single Sign-On", translation(label, Locale.ENGLISH));
		assertEquals("Über Single Sign-On anmelden", translation(label, Locale.GERMAN));
	}

	/**
	 * {@link #LABEL_VARIABLE} names the provider on the button of the generic client.
	 */
	public void testGenericProviderLabel() throws Exception {
		Assume.assumeTrue(LABEL_VARIABLE + " is set in the environment of the test.",
			Environment.getSystemPropertyOrEnvironmentVariable(LABEL_VARIABLE, null) == null);

		System.setProperty(LABEL_VARIABLE, PROVIDER_NAME);
		try {
			ResKey label = label(GENERIC_CLIENT);
			assertEquals("Sign in with " + PROVIDER_NAME, translation(label, Locale.ENGLISH));
			assertEquals("Über " + PROVIDER_NAME + " anmelden", translation(label, Locale.GERMAN));
		} finally {
			System.clearProperty(LABEL_VARIABLE);
		}
	}

	/**
	 * The clients of a specific provider have a label in English and German.
	 */
	public void testProviderClientLabels() throws Exception {
		for (String client : PROVIDER_CLIENTS) {
			ResKey label = label(client);
			assertFalse(client, StringServices.isEmpty(translation(label, Locale.ENGLISH)));
			assertFalse(client, StringServices.isEmpty(translation(label, Locale.GERMAN)));
		}
	}

	private static String translation(ResKey label, Locale locale) {
		assertNotNull("No label.", label);
		assertTrue("Not a literal label: " + label, label.isLiteral());
		return ((ResKey.LiteralKey) label).getTranslationWithoutFallbacks(locale);
	}

	/**
	 * The configured label of the given shipped client, read from the application configuration
	 * extended by the client's configuration file, with the aliases of the application and of the
	 * client's alias file resolved.
	 */
	private static ResKey label(String client) throws Exception {
		VariableExpander expander = AliasManager.getInstance().getExpander().derive();
		AliasManager.putVariables(expander, clientAliases(client));

		List<BinaryContent> sources = new ArrayList<>(XMLProperties.Module.INSTANCE.config().getTypedConfigs());
		sources.add(FileManager.getInstance().getData(CONF_FOLDER + client + CONFIG_FILE_SUFFIX));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestShippedClientLabels.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(ApplicationConfig.ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		ApplicationConfig.Config config = (ApplicationConfig.Config) new ConfigurationReader(context, descriptors)
			.setSources(sources)
			.setVariableExpander(expander)
			.read();
		context.checkErrors();
		Pac4jConfigFactory.Config<?> factoryConfig =
			(Pac4jConfigFactory.Config<?>) config.getServices().get(Pac4jConfigFactory.class).getInstance();
		ClientConfigurator.Config<?> clientConfig = factoryConfig.getClients().get(client);
		assertNotNull("No client " + client + ".", clientConfig);
		return clientConfig.getLabel();
	}

	/**
	 * The values of the aliases defined in the alias file of the given client, determined from
	 * the environment as at application startup.
	 */
	private static Map<String, String> clientAliases(String client) throws IOException {
		XMLProperties aliasFile =
			new XMLProperties(FileManager.getInstance().getData(CONF_FOLDER + client + ALIAS_FILE_SUFFIX));
		Properties definitions = aliasFile.getAliases();
		ClientAliasManager aliases = new ClientAliasManager(
			definitions == null ? Collections.emptyMap() : propertiesMap(definitions));
		aliases.reload();
		return aliases.getDefinedAliases();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, String> propertiesMap(Properties properties) {
		return (Map) properties;
	}

	/**
	 * {@link AliasManager} determining the values of given alias definitions.
	 */
	private static final class ClientAliasManager extends AliasManager {

		private final Map<String, String> _definitions;

		ClientAliasManager(Map<String, String> definitions) {
			_definitions = definitions;
		}

		@Override
		protected Map<String, String> resolveConfiguredAliases() {
			return resolveDefinitions(_definitions);
		}

		@Override
		public boolean usesXMLProperties() {
			return false;
		}
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(TestShippedClientLabels.class, TypeIndex.Module.INSTANCE,
				AliasManager.Module.INSTANCE));
	}

}
