/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.credential;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.FileSecretSource;
import com.top_logic.basic.credential.I18NConstants;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Test case for {@link SecretSource} and its configuration through the {@link SecretSourceFormat}.
 */
@SuppressWarnings("javadoc")
public class TestSecretSource extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	private static final String TEST_VARIABLE = "TL_TEST_SECRET_SOURCE_VARIABLE";

	private static final String UNDEFINED_VARIABLE = "TL_TEST_SECRET_SOURCE_UNDEFINED";

	public interface Config extends ConfigurationItem {

		String API_KEY = "api-key";

		String SOURCES = "sources";

		@Name(API_KEY)
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		void setApiKey(PolymorphicConfiguration<? extends SecretSource> value);

		@Name(SOURCES)
		List<PolymorphicConfiguration<? extends SecretSource>> getSources();

	}

	private Path _dir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = Files.createTempDirectory("TestSecretSource");
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(TEST_VARIABLE);
		try (Stream<Path> files = Files.walk(_dir)) {
			files.sorted(Comparator.reverseOrder()).forEach(file -> file.toFile().delete());
		}
		super.tearDown();
	}

	public void testEncryptedAttribute() throws ConfigurationException {
		String encrypted = ConfigurationEncryption.encrypt("s3cr3t");
		Config config = read("<config api-key='" + encrypted + "'/>");

		assertTrue(config.getApiKey() instanceof LiteralSecretSource.Config);
		assertEquals("s3cr3t", secret(config));
	}

	public void testUnencryptedAttribute() throws ConfigurationException {
		Config config = read("<config api-key='unencrypted:s3cr3t'/>");

		assertEquals("s3cr3t", secret(config));
	}

	public void testEmptyAttribute() throws ConfigurationException {
		Config config = read("<config api-key=''/>");

		assertNull(config.getApiKey());
		assertNull(getInstance(config.getApiKey()));
	}

	public void testNotConfigured() throws ConfigurationException {
		Config config = read("<config/>");

		assertNull(config.getApiKey());
	}

	public void testPropertyEncrypted() {
		assertTrue(TypedConfiguration.getConfigurationDescriptor(Config.class).getProperty(Config.API_KEY).isEncrypted());
	}

	public void testUndecodableAttribute() {
		Throwable failure = null;
		try {
			read("<config api-key='PlainTextPassword12345'/>");
		} catch (Throwable ex) {
			failure = ex;
		}
		assertNotNull("Plain text value accepted.", failure);
		assertNoSecret(failure, "PlainTextPassword12345");
	}

	public void testLiteralElement() throws ConfigurationException {
		Config config = read("<config><api-key class='" + LiteralSecretSource.class.getName()
			+ "' value='unencrypted:s3cr3t'/></config>");

		assertEquals("s3cr3t", secret(config));
	}

	public void testLiteralWithoutValue() throws ConfigurationException {
		Config config = read("<config><api-key class='" + LiteralSecretSource.class.getName() + "'/></config>");

		assertFailure(I18NConstants.ERROR_SECRET_NOT_CONFIGURED, getInstance(config.getApiKey()));
	}

	public void testEnvironment() throws ConfigurationException {
		Config config = read("<config><api-key class='" + EnvironmentSecretSource.class.getName()
			+ "' name='" + TEST_VARIABLE + "'/></config>");
		SecretSource source = getInstance(config.getApiKey());

		System.setProperty(TEST_VARIABLE, "first");
		assertEquals("first", source.getSecret());

		System.setProperty(TEST_VARIABLE, "second");
		assertEquals("Variable must be read on each use.", "second", source.getSecret());
	}

	public void testElementWithoutClassIsEnvironment() throws Exception {
		System.setProperty(TEST_VARIABLE, "from-env");
		Config config = read("<config><api-key name='" + TEST_VARIABLE + "'/></config>");

		assertTrue(config.getApiKey() instanceof EnvironmentSecretSource.Config);
		assertEquals("from-env", secret(config));

		Config copy = (Config) fromXML(toXML(config));
		assertTrue(copy.getApiKey() instanceof EnvironmentSecretSource.Config);
		assertEquals("from-env", secret(copy));
	}

	public void testEnvironmentUndefined() throws ConfigurationException {
		Config config = read("<config><api-key class='" + EnvironmentSecretSource.class.getName()
			+ "' name='" + UNDEFINED_VARIABLE + "'/></config>");

		assertFailure(I18NConstants.ERROR_SECRET_VARIABLE_UNDEFINED__NAME.fill(UNDEFINED_VARIABLE),
			getInstance(config.getApiKey()));
	}

	public void testFile() throws Exception {
		Path file = _dir.resolve("api-key");
		write(file, "s3cr3t\n");
		SecretSource source = fileSource(file);

		assertEquals("Surrounding white space must be removed.", "s3cr3t", source.getSecret());

		write(file, "  rotated-secret \r\n");
		assertEquals("Changed file must be read again.", "rotated-secret", source.getSecret());
	}

	public void testFileSymlinkSwap() throws Exception {
		// Layout of a Kubernetes secret volume: <dir>/api-key -> ..data/api-key,
		// ..data -> ..<timestamp>, swapped atomically on update.
		Path version1 = Files.createDirectory(_dir.resolve("..v1"));
		Path version2 = Files.createDirectory(_dir.resolve("..v2"));
		write(version1.resolve("api-key"), "secret-1");
		// Same length, written before the swap, so that only the file identity differs.
		write(version2.resolve("api-key"), "secret-2");
		Files.setLastModifiedTime(version2.resolve("api-key"), Files.getLastModifiedTime(version1.resolve("api-key")));

		Path data = _dir.resolve("..data");
		Files.createSymbolicLink(data, version1.getFileName());
		Path file = Files.createSymbolicLink(_dir.resolve("api-key"), Paths.get("..data", "api-key"));
		SecretSource source = fileSource(file);

		assertEquals("secret-1", source.getSecret());

		Path newData = Files.createSymbolicLink(_dir.resolve("..data_tmp"), version2.getFileName());
		Files.move(newData, data, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

		assertEquals("Swapped secret must be read.", "secret-2", source.getSecret());
	}

	public void testFileMissing() throws Exception {
		Path file = _dir.resolve("missing");
		SecretSource source = fileSource(file);

		assertFailure(I18NConstants.ERROR_SECRET_FILE_UNREADABLE__PATH.fill(file.toString()), source);

		write(file, "late-secret");
		assertEquals("late-secret", source.getSecret());
	}

	public void testFileEmpty() throws Exception {
		Path file = _dir.resolve("empty");
		write(file, " \n");

		assertFailure(I18NConstants.ERROR_SECRET_FILE_EMPTY__PATH.fill(file.toString()), fileSource(file));
	}

	public void testSourceList() throws ConfigurationException {
		System.setProperty(TEST_VARIABLE, "from-env");
		Config config = read("<config><sources>"
			+ "<" + EnvironmentSecretSource.Config.TAG_NAME + " name='" + TEST_VARIABLE + "'/>"
			+ "<" + LiteralSecretSource.Config.TAG_NAME + " value='unencrypted:literal'/>"
			+ "</sources></config>");

		List<SecretSource> sources = TypedConfiguration.getInstanceList(context, config.getSources());
		assertEquals(2, sources.size());
		assertEquals("from-env", sources.get(0).getSecret());
		assertEquals("literal", sources.get(1).getSecret());
	}

	public void testWriteLiteralAsEncryptedAttribute() throws Exception {
		Config config = read("<config api-key='unencrypted:s3cr3t'/>");

		String xml = toXML(config);
		assertFalse("Secret written in plain text: " + xml, xml.contains("s3cr3t"));
		assertTrue("Not written as encrypted attribute: " + xml,
			xml.contains(Config.API_KEY + "=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		Config copy = (Config) fromXML(xml);
		assertEquals(config, copy);
		assertEquals("s3cr3t", secret(copy));
	}

	public void testWriteFileSourceAsElement() throws Exception {
		Path file = _dir.resolve("api-key");
		write(file, "s3cr3t");
		Config config = read("<config><api-key class='" + FileSecretSource.class.getName()
			+ "' path='" + file + "'/></config>");

		String xml = toXML(config);
		assertTrue("Not written as element: " + xml, xml.contains("<" + Config.API_KEY + " "));

		Config copy = (Config) fromXML(xml);
		assertEquals(config, copy);
		assertEquals("s3cr3t", secret(copy));
	}

	public void testWriteEmptyLiteralAsElement() throws Exception {
		Config config = read("<config><api-key class='" + LiteralSecretSource.class.getName() + "'/></config>");

		Config copy = (Config) fromXML(toXML(config));
		assertEquals(config, copy);
		assertTrue(copy.getApiKey() instanceof LiteralSecretSource.Config);
	}

	public void testWriteNotConfigured() throws Exception {
		Config config = TypedConfiguration.newConfigItem(Config.class);

		Config copy = (Config) fromXML(toXML(config));
		assertNull(copy.getApiKey());
	}

	private SecretSource fileSource(Path file) throws ConfigurationException {
		Config config = read("<config><api-key class='" + FileSecretSource.class.getName()
			+ "' path='" + file + "'/></config>");
		return getInstance(config.getApiKey());
	}

	private String secret(Config config) {
		return getInstance(config.getApiKey()).getSecret();
	}

	private static void write(Path file, String content) throws IOException {
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
	}

	private static void assertFailure(Object expectedKey, SecretSource source) {
		try {
			source.getSecret();
			fail("Missing secret not reported.");
		} catch (I18NRuntimeException ex) {
			assertEquals(expectedKey, ex.getErrorKey());
		}
	}

	private static void assertNoSecret(Throwable ex, String secret) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			assertTrue("Secret in message: " + message, message == null || !message.contains(secret));
		}
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestSecretSource.class);
	}

}
