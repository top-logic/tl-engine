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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.EnvironmentSecretStore;
import com.top_logic.basic.credential.FileSecretSource;
import com.top_logic.basic.credential.I18NConstants;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretReference;
import com.top_logic.basic.credential.SecretReference.Status;
import com.top_logic.basic.credential.SecretReferences;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.basic.credential.SecretStore;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.util.ResKey;

/**
 * Test case for {@link SecretReferences}: finding the secrets in a configuration and checking
 * whether they are available.
 */
@SuppressWarnings("javadoc")
public class TestSecretReferences extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	private static final String SEP = SecretReferences.PATH_SEPARATOR;

	private static final String SECRET = "test_secret_references_secret";

	private static final String OVERRIDDEN = "test_secret_references_overridden";

	private static final String UNDEFINED = "test_secret_references_undefined";

	private static final String ENV_VALUE = "env-value-4711";

	private static final String FILE_VALUE = "file-value-4712";

	private static final String LITERAL_VALUE = "literal-value-4713";

	private static final String[] SECRET_VALUES = { ENV_VALUE, FILE_VALUE, LITERAL_VALUE };

	public interface Config extends ConfigurationItem {

		@Name("api-key")
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

		@Name("nested")
		Nested getNested();

		@Name("list")
		@EntryTag("nested")
		List<Nested> getList();

		@Name("entries")
		@Key(Entry.NAME_ATTRIBUTE)
		@EntryTag("entry")
		List<Entry> getEntries();

		@Name("by-name")
		@Key(Entry.NAME_ATTRIBUTE)
		@EntryTag("entry")
		Map<String, Entry> getByName();

	}

	public interface Nested extends ConfigurationItem {

		@Name("password")
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getPassword();

	}

	public interface Entry extends NamedConfigMandatory {

		@Name("secret")
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getSecret();

	}

	private Path _dir;

	private PolymorphicConfiguration<? extends SecretStore> _savedStore;

	private Map<String, SecretStoreService.SecretOverride> _savedOverrides;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = Files.createTempDirectory("TestSecretReferences");
		SecretStoreService.Config global = global();
		_savedStore = global.getStore();
		_savedOverrides = new LinkedHashMap<>(global.getOverrides());
	}

	@Override
	protected void tearDown() throws Exception {
		SecretStoreService.Config global = global();
		global.setStore(_savedStore);
		global.getOverrides().clear();
		global.getOverrides().putAll(_savedOverrides);
		restartService();
		System.clearProperty(SECRET);
		try (Stream<Path> files = Files.walk(_dir)) {
			files.sorted(Comparator.reverseOrder()).forEach(file -> file.toFile().delete());
		}
		super.tearDown();
	}

	public void testReferences() throws Exception {
		System.setProperty(SECRET, ENV_VALUE);
		Path missing = _dir.resolve("missing");
		Path empty = _dir.resolve("empty");
		write(empty, " \n");
		Path present = _dir.resolve("present");
		write(present, FILE_VALUE + "\n");

		Config config = read("<config api-key='unencrypted:" + LITERAL_VALUE + "'>"
			+ "<nested><password name='" + SECRET + "'/></nested>"
			+ "<list><nested><password name='" + UNDEFINED + "'/></nested></list>"
			+ "<entries>"
			+ "<entry name='env'><secret class='" + EnvironmentSecretSource.class.getName() + "' name='" + UNDEFINED
			+ "'/></entry>"
			+ "<entry name='missing'><secret class='" + FileSecretSource.class.getName() + "' path='" + missing
			+ "'/></entry>"
			+ "</entries>"
			+ "<by-name>"
			+ "<entry name='empty'><secret class='" + FileSecretSource.class.getName() + "' path='" + empty
			+ "'/></entry>"
			+ "<entry name='present'><secret class='" + FileSecretSource.class.getName() + "' path='" + present
			+ "'/></entry>"
			+ "</by-name>"
			+ "</config>");

		List<SecretReference> references = SecretReferences.of(null, config);
		Map<String, SecretReference> byPath = byPath(references);
		assertEquals(byPath.keySet().toString(), 7, references.size());

		SecretReference literal = byPath.get("api-key");
		assertEquals(LiteralSecretSource.class, literal.getKind());
		assertEquals(Status.AVAILABLE, literal.getStatus());
		assertEquals(I18NConstants.SECRET_IN_CONFIGURATION, literal.getReference());
		assertNull(literal.getResolvedBy());
		assertNull(literal.getMessage());

		SecretReference named = byPath.get("nested" + SEP + "password");
		assertEquals(NamedSecretSource.class, named.getKind());
		assertEquals(Status.AVAILABLE, named.getStatus());
		assertEquals(ResKey.text(SECRET), named.getReference());
		assertEquals(SecretReferences.kindLabel(EnvironmentSecretStore.class), named.getResolvedBy());

		SecretReference namedMissing = byPath.get("list[0]" + SEP + "password");
		assertEquals(Status.MISSING, namedMissing.getStatus());
		assertEquals(I18NConstants.ERROR_SECRET_VARIABLE_UNDEFINED__NAME.fill(UNDEFINED), namedMissing.getMessage());

		SecretReference env = byPath.get("entries[env]" + SEP + "secret");
		assertEquals(EnvironmentSecretSource.class, env.getKind());
		assertEquals(Status.MISSING, env.getStatus());
		assertEquals(ResKey.text(UNDEFINED), env.getReference());
		assertNull(env.getResolvedBy());

		SecretReference fileMissing = byPath.get("entries[missing]" + SEP + "secret");
		assertEquals(FileSecretSource.class, fileMissing.getKind());
		assertEquals(Status.MISSING, fileMissing.getStatus());
		assertEquals(ResKey.text(missing.toString()), fileMissing.getReference());
		assertEquals(I18NConstants.ERROR_SECRET_FILE_UNREADABLE__PATH.fill(missing.toString()),
			fileMissing.getMessage());

		SecretReference fileEmpty = byPath.get("by-name[empty]" + SEP + "secret");
		assertEquals(Status.ERROR, fileEmpty.getStatus());
		assertEquals(I18NConstants.ERROR_SECRET_FILE_EMPTY__PATH.fill(empty.toString()), fileEmpty.getMessage());

		SecretReference filePresent = byPath.get("by-name[present]" + SEP + "secret");
		assertEquals(Status.AVAILABLE, filePresent.getStatus());

		assertNoSecret(references);
	}

	public void testOverride() throws Exception {
		Path file = _dir.resolve("overridden");
		write(file, FILE_VALUE);
		SecretStoreService.Config global = global();
		SecretStoreService.SecretOverride override =
			TypedConfiguration.newConfigItem(SecretStoreService.SecretOverride.class);
		override.setName(OVERRIDDEN);
		FileSecretSource.Config source = TypedConfiguration.newConfigItem(FileSecretSource.Config.class);
		source.setPath(file.toString());
		override.setSource(source);
		global.getOverrides().put(OVERRIDDEN, override);
		restartService();

		Config config = read("<config><api-key name='" + OVERRIDDEN + "'/></config>");
		List<SecretReference> references = SecretReferences.of(null, config);
		assertEquals(1, references.size());
		SecretReference reference = references.get(0);
		assertEquals(Status.AVAILABLE, reference.getStatus());
		assertEquals(I18NConstants.SECRET_RESOLVED_BY_OVERRIDE__SOURCE.fill(
			I18NConstants.SECRET_DESCRIPTION__KIND_DETAIL.fill(SecretReferences.kindLabel(FileSecretSource.class),
				ResKey.text(file.toString()))),
			reference.getResolvedBy());

		Files.delete(file);
		SecretReference missing = SecretReferences.of(null, config).get(0);
		assertEquals("Absent override file is a missing secret.", Status.MISSING, missing.getStatus());

		assertNoSecret(references);
	}

	public void testStoreNotStarted() throws Exception {
		Config config = read("<config><api-key name='" + SECRET + "'/></config>");
		List<SecretReference> whileStopped = new ArrayList<>();
		ModuleUtil.INSTANCE.restart(SecretStoreService.Module.INSTANCE,
			() -> whileStopped.addAll(SecretReferences.of(null, config)));

		assertEquals(1, whileStopped.size());
		SecretReference reference = whileStopped.get(0);
		assertEquals(Status.ERROR, reference.getStatus());
		assertEquals(I18NConstants.ERROR_SECRET_STORE_NOT_STARTED__NAME.fill(SECRET), reference.getMessage());
		assertNull(reference.getResolvedBy());
	}

	private static Map<String, SecretReference> byPath(List<SecretReference> references) {
		Map<String, SecretReference> result = new LinkedHashMap<>();
		for (SecretReference reference : references) {
			result.put(reference.getPath(), reference);
		}
		return result;
	}

	private static void assertNoSecret(List<SecretReference> references) {
		for (SecretReference reference : references) {
			for (Object part : new Object[] { reference, reference.getPath(), reference.getReference(),
				reference.getResolvedBy(), reference.getMessage() }) {
				String text = String.valueOf(part);
				for (String secret : SECRET_VALUES) {
					assertFalse("Secret in " + text, text.contains(secret));
				}
			}
		}
	}

	private static void restartService() {
		try {
			ModuleUtil.INSTANCE.restart(SecretStoreService.Module.INSTANCE);
		} catch (RestartException ex) {
			throw new AssertionError("Restart of the secret store service failed.", ex);
		}
	}

	private static SecretStoreService.Config global() {
		try {
			return (SecretStoreService.Config) ApplicationConfig.getInstance()
				.getServiceConfiguration(SecretStoreService.class);
		} catch (ConfigurationException ex) {
			throw new AssertionError(ex);
		}
	}

	private static void write(Path file, String content) throws IOException {
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		Map<String, ConfigurationDescriptor> result = new HashMap<>();
		result.put(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
		return result;
	}

	public static Test suite() {
		return suite(TestSecretReferences.class, SecretStoreService.Module.INSTANCE);
	}

}
