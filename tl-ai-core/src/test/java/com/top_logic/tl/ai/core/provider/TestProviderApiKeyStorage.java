/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.tl.ai.core.provider.LLMProviderService.ModelCatalogConfig;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.tl.ai.core.provider.mistral.MistralProviderConfig;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Tests that an LLM provider whose API key is referenced through an {@link EnvironmentSecretSource}
 * is stored by the service editor in the form it was read, holding only the name of the variable.
 */
public class TestProviderApiKeyStorage extends TestCase {

	/** The system property holding the API key. */
	private static final String VARIABLE = "test_provider_api_key_storage";

	/** The API key in {@link #VARIABLE}. */
	private static final String API_KEY = "sk-test-0123456789";

	/** The variable of the provider {@link #MISTRAL}, which is not set. */
	private static final String UNSET_VARIABLE = "test_provider_api_key_storage_unset";

	/** The model of the provider {@link #ANTHROPIC}. */
	private static final String MODEL = "claude-sonnet-4-6";

	/** The provider whose key is in {@link #VARIABLE}. */
	private static final String ANTHROPIC = "anthropic";

	/** The provider whose key is in {@link #UNSET_VARIABLE}. */
	private static final String MISTRAL = "mistral";

	/**
	 * The stored configuration, in the form {@link InAppServiceConfigStore} writes it.
	 */
	private static final String STORED = """
		<?xml version="1.0" encoding="utf-8" ?>

		<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
			xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
			<services>
				<config service-class="com.top_logic.tl.ai.core.provider.LLMProviderService"
					config:override="true"
				>
					<instance class="com.top_logic.tl.ai.core.provider.LLMProviderService">
						<providers>
							<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
								models="claude-sonnet-4-6"
								name="anthropic"
							>
								<api-key name="test_provider_api_key_storage"/>
							</provider>
							<provider class="com.top_logic.tl.ai.core.provider.mistral.MistralProviderConfig"
								name="mistral"
							>
								<api-key name="test_provider_api_key_storage_unset"/>
							</provider>
						</providers>
						<model-catalog/>
					</instance>
				</config>
			</services>
		</application>""";

	private File _file;

	/**
	 * Parsing provider configurations resolves their nested configuration types through the
	 * <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestProviderApiKeyStorage.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty(VARIABLE, API_KEY);
		_file = BasicTestCase.createNamedTestFile(getName() + InAppServiceConfigStore.FILE_SUFFIX);
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		_file.delete();
		super.tearDown();
	}

	/**
	 * Reading and writing the stored configuration again produces the same text, which names the
	 * variable and never contains the API key.
	 */
	public void testEnvironmentApiKeyRoundTrip() throws IOException, ConfigurationException {
		FileUtilities.writeStringToFile(STORED, _file, StringServices.UTF8);

		ApplicationConfig.Config config = InAppServiceConfigStore.read(_file);
		String written = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG, config,
			InAppServiceConfigStore.OVERRIDE_SERVICE);

		assertEquals(STORED.strip(), written.strip());
		assertFalse(written, written.contains(API_KEY));

		AnthropicProviderConfig.Config provider =
			(AnthropicProviderConfig.Config) service(config).getProviders().get(ANTHROPIC);
		assertEquals(API_KEY, secretSource(provider.getApiKey()).getSecret());
		assertEquals(VARIABLE, ((EnvironmentSecretSource.Config) provider.getApiKey()).getName());
	}

	/**
	 * A provider whose variable is not set does not keep the service from starting; reading its
	 * key fails, naming the variable.
	 */
	public void testUnsetVariableFailsOnUse() throws IOException, ConfigurationException {
		assertNull(System.getProperty(UNSET_VARIABLE));
		assertNull(System.getenv(UNSET_VARIABLE));
		FileUtilities.writeStringToFile(STORED, _file, StringServices.UTF8);

		@SuppressWarnings("unchecked")
		LLMProviderService.Config<LLMProviderService> config =
			(LLMProviderService.Config<LLMProviderService>) service(InAppServiceConfigStore.read(_file));
		ModelCatalogConfig catalog = config.getModelCatalog();
		catalog.update(catalog.descriptor().getProperty(ModelCatalogConfig.FETCH_REMOTE_CATALOG), false);

		LLMProviderService service =
			new LLMProviderService(new DefaultInstantiationContext(TestProviderApiKeyStorage.class), config);
		service.startUp();
		try {
			assertEquals(List.of(ANTHROPIC), service.getProvidersOf(MODEL));
			assertNotNull(service.buildProvider(MODEL, ANTHROPIC));
		} finally {
			service.shutDown();
		}

		MistralProviderConfig.Config mistral = (MistralProviderConfig.Config) config.getProviders().get(MISTRAL);
		SecretSource unset = secretSource(mistral.getApiKey());
		try {
			unset.getSecret();
			fail("An unset variable has no secret.");
		} catch (I18NRuntimeException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(UNSET_VARIABLE));
		}
	}

	private static SecretSource secretSource(PolymorphicConfiguration<? extends SecretSource> config) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static LLMProviderService.Config<?> service(ApplicationConfig.Config config) {
		return (LLMProviderService.Config<?>) config.getServices().get(LLMProviderService.class).getInstance();
	}

}
