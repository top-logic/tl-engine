/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.vector.index.VectorIndex.VectorIndexingConfig;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.util.autoconf.InAppServiceConfigStore;
import com.top_logic.util.error.TopLogicException;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;

/**
 * Tests the Mistral API key of the {@link VectorIndex}: the attribute form and the reference to a
 * variable, and that the index is disabled instead of aborting the start when no key is available.
 */
public class TestVectorIndexApiKey extends TestCase {

	/** The system property holding the API key. */
	private static final String VARIABLE = "test_vector_index_api_key";

	/** The API key in {@link #VARIABLE}. */
	private static final String API_KEY = "mistral-0123456789";

	/**
	 * Parsing index configurations resolves the nested configuration types of the secret sources
	 * through the <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestVectorIndexApiKey.class)));
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** The attribute form of a key still loads, as encrypted value with the prefix for plain text. */
	public void testAttributeApiKey() throws Exception {
		VectorIndexingConfig config = config("mistral-api-key='unencrypted:x'", "");

		assertEquals("x", ((LiteralSecretSource.Config) config.getMistralApiKey()).getValue());
		assertTrue(VectorIndex.createEmbeddingModel(context(), config) instanceof MistralAiEmbeddingModel);
	}

	/** A key referenced by its variable builds the model, without contacting Mistral. */
	public void testEnvironmentApiKey() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		VectorIndexingConfig config = config("", environmentKey());

		assertTrue(VectorIndex.createEmbeddingModel(context(), config) instanceof MistralAiEmbeddingModel);
	}

	/** Without a configured key, the index is disabled with a warning. */
	public void testNoKeyDisablesIndex() throws Exception {
		assertDisabled(config("", ""));
	}

	/** A key referenced by an unset variable disables the index with a warning. */
	public void testUnsetVariableDisablesIndex() throws Exception {
		assertDisabled(config("", environmentKey()));
	}

	/** The local test model needs no key. */
	public void testLocalModelNeedsNoKey() throws Exception {
		EmbeddingModel model = VectorIndex.createEmbeddingModel(context(),
			config("embedding-model-type='" + VectorIndex.EMBEDDING_MODEL_LOCAL + "'", ""));

		assertNotNull(model);
	}

	/**
	 * The service editor stores a key referenced by its secret name in the form it was read,
	 * holding only the name of the secret.
	 */
	public void testEnvironmentApiKeyStorage() throws IOException, ConfigurationException {
		System.setProperty(VARIABLE, API_KEY);
		String stored = """
			<?xml version="1.0" encoding="utf-8" ?>

			<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
				xmlns:config="http://www.top-logic.com/ns/config/6.0"
			>
				<services>
					<config service-class="com.top_logic.ai.vector.index.VectorIndex"
						config:override="true"
					>
						<instance class="com.top_logic.ai.vector.index.VectorIndex">
							<mistral-api-key name="%s"/>
						</instance>
					</config>
				</services>
			</application>""".formatted(VARIABLE);
		File file = BasicTestCase.createNamedTestFile(getName() + InAppServiceConfigStore.FILE_SUFFIX);
		try {
			FileUtilities.writeStringToFile(stored, file, StringServices.UTF8);
			ApplicationConfig.Config config = InAppServiceConfigStore.read(file);
			String written = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG, config,
				InAppServiceConfigStore.OVERRIDE_SERVICE);

			assertTrue(written, written.contains("name=\"" + VARIABLE + "\""));
			// An element without class is a NamedSecretSource, the default secret source.
			assertTrue(written, written.contains("<mistral-api-key name=\"" + VARIABLE + "\"/>"));
			assertFalse(written, written.contains(API_KEY));

			FileUtilities.writeStringToFile(written, file, StringServices.UTF8);
			String rewritten = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG,
				InAppServiceConfigStore.read(file), InAppServiceConfigStore.OVERRIDE_SERVICE);
			assertEquals(written, rewritten);
		} finally {
			file.delete();
		}
	}

	/**
	 * The index of the given configuration starts disabled: no exception, a warning, contents are
	 * ignored and a search reports that the index is not available.
	 */
	private void assertDisabled(VectorIndexingConfig config) {
		CollectingLogListener log = new CollectingLogListener(Set.of(Level.WARN), true);
		VectorIndex index;
		try {
			index = new VectorIndex(context(), config);
		} finally {
			log.deactivate();
		}

		assertFalse(index.isAvailable());
		List<LogEntry> warnings = log.getLogEntries();
		assertEquals(warnings.toString(), 1, warnings.size());
		assertTrue(warnings.toString(), warnings.get(0).getMessage().contains("vector index is disabled"));

		index.addContent(null);
		index.deleteContent(null);
		assertTrue(index.isIdle());
		assertEquals(0, index.queueSizes());
		try {
			index.searchVectorIndex("q", null);
			fail("A disabled index must not search.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_VECTOR_INDEX_NOT_AVAILABLE, ex.getErrorKey());
		}
	}

	private static String environmentKey() {
		return "<mistral-api-key class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>";
	}

	private static InstantiationContext context() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	private static VectorIndexingConfig config(String attributes, String content) throws ConfigurationException {
		String xml = "<config xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + VectorIndexingConfig.class.getName() + "'"
			+ " class='" + VectorIndex.class.getName() + "' "
			+ attributes + ">" + content + "</config>";
		return (VectorIndexingConfig) TypedConfiguration.parse(CharacterContents.newContent(xml));
	}

}
