/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.db.schema.properties.DBPropertiesTableSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.encryption.pbe.PasswordBasedEncryptionSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.knowledge.service.encryption.SecurityService;
import com.top_logic.knowledge.service.migration.processors.DocumentRepositoryConfig;
import com.top_logic.knowledge.service.migration.processors.EncryptedRepositoryContentReader;
import com.top_logic.knowledge.service.migration.processors.FileRepositoryContentReader;
import com.top_logic.knowledge.service.migration.processors.RepositoryContentReader;

/**
 * Test of {@link EncryptedRepositoryContentReader}.
 *
 * <p>
 * The files of the repository are encrypted with the key of the {@link EncryptionService}, as an
 * encrypted application stored its documents.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestEncryptedRepositoryContentReader extends AbstractDBKnowledgeBaseTest {

	private static final String DOCUMENT = "folder/document.bin";

	/**
	 * The decrypted content of every version is delivered with its plain size.
	 */
	public void testRead() throws Exception {
		File root = createdCleanTestDir(getClass().getSimpleName());
		byte[] v1 = content(1000);
		byte[] v2 = content(3333);
		TestMigrateDocumentContent.createVersions(
			new File(new File(root, "folder"), FileRepositoryContentReader.ENTRY_PREFIX + "document.bin"),
			encrypt(v1), encrypt(v2));

		RepositoryContentReader reader = reader(root);
		assertTrue(reader.isVersioned());
		assertContent(v1, reader.read(DOCUMENT, 1));
		assertContent(v2, reader.read(DOCUMENT, 2));
		assertNull(reader.read(DOCUMENT, 3));
		assertNull(reader.read("folder/missing.bin", 1));
	}

	/**
	 * The configuration of an encrypted application reads the document repository with the
	 * {@link EncryptedRepositoryContentReader}.
	 */
	public void testEncryptedApplicationConfiguration() throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class);
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(ApplicationConfig.ROOT_TAG, descriptor));
		File conf = new File(ModuleLayoutConstants.WEBAPP_DIR, "WEB-INF/conf");
		ApplicationConfig.Config config = (ApplicationConfig.Config) reader.setSources(
			repositoryConfig(new File(conf, "top-logic.config.xml")),
			repositoryConfig(new File(conf, "top-logic.encrypted.config.xml"))).read();

		DocumentRepositoryConfig repository =
			(DocumentRepositoryConfig) config.getConfigs().get(DocumentRepositoryConfig.class);
		assertTrue(repository.getReader() instanceof EncryptedRepositoryContentReader.Config);
		assertTrue(((EncryptedRepositoryContentReader.Config<?>) repository.getReader())
			.getImpl() instanceof FileRepositoryContentReader.Config);
	}

	/**
	 * The {@link DocumentRepositoryConfig} of the given application configuration file as
	 * application configuration of its own.
	 */
	private static Content repositoryConfig(File file) throws Exception {
		String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
		Matcher matcher = Pattern.compile(
			"<config config:interface=\"" + Pattern.quote(DocumentRepositoryConfig.class.getName())
				+ "\">.*?</config>",
			Pattern.DOTALL).matcher(text);
		assertTrue("No document repository configured in " + file, matcher.find());
		return CharacterContents.newContent("<" + ApplicationConfig.ROOT_TAG
			+ " xmlns:config='http://www.top-logic.com/ns/config/6.0'><configs>" + matcher.group()
			+ "</configs></" + ApplicationConfig.ROOT_TAG + ">", file.getPath());
	}

	private static RepositoryContentReader reader(File root) {
		FileRepositoryContentReader.Config<?> impl =
			TypedConfiguration.newConfigItem(FileRepositoryContentReader.Config.class);
		impl.setPath(root.getAbsolutePath());

		EncryptedRepositoryContentReader.Config<?> config =
			TypedConfiguration.newConfigItem(EncryptedRepositoryContentReader.Config.class);
		config.setImpl(impl);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private static byte[] encrypt(byte[] plain) throws Exception {
		SecretKey key = EncryptionService.getInstance().getEncryptionKey();
		Cipher cipher = Cipher.getInstance(key.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, key);
		try (InputStream in = new CipherInputStream(new ByteArrayInputStream(plain), cipher)) {
			byte[] encrypted = StreamUtilities.readStreamContents(in);
			assertFalse(Arrays.equals(plain, encrypted));
			return encrypted;
		}
	}

	private static void assertContent(byte[] expected, BinaryData actual) throws Exception {
		assertNotNull(actual);
		assertEquals(expected.length, actual.getSize());
		try (InputStream in = actual.getStream()) {
			assertTrue(Arrays.equals(expected, StreamUtilities.readStreamContents(in)));
		}
	}

	private static byte[] content(int size) {
		byte[] result = new byte[size];
		new Random(size).nextBytes(result);
		return result;
	}

	public static Test suite() {
		return AbstractDBKnowledgeBaseTest.suite(TestEncryptedRepositoryContentReader.class, new TestFactory() {
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				Test test = suite;
				test = PasswordBasedEncryptionSetup.setup(test);
				test = ServiceTestSetup.createSetup(test, SecurityService.Module.INSTANCE);
				test = DBPropertiesTableSetup.setup(test);
				return test;
			}
		});
	}

}
