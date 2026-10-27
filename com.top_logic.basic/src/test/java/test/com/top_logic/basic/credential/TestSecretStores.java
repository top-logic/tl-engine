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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.ChainSecretStore;
import com.top_logic.basic.credential.DirectorySecretStore;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.EnvironmentSecretStore;
import com.top_logic.basic.credential.I18NConstants;
import com.top_logic.basic.credential.LiteralSecretStore;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.basic.credential.SecretStore;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;

/**
 * Test case for {@link NamedSecretSource}, the {@link SecretStore}s and their configuration in
 * the {@link SecretStoreService}.
 */
@SuppressWarnings("javadoc")
public class TestSecretStores extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	private static final String STORES_TAG = "secret-stores";

	/** The implementation class of a {@link #STORES_TAG} configuration. */
	private static final String SERVICE_CLASS = " class='" + SecretStoreService.class.getName() + "'";

	private static final String SECRET = "test_secret_stores_secret";

	private static final String OTHER = "test_secret_stores_other";

	private static final String UNDEFINED = "test_secret_stores_undefined";

	private static final String PREFIX = "pfx_";

	/** The configuration of the engine holding the {@link SecretStoreService}. */
	private static final String ENGINE_CONFIG = "/WEB-INF/conf/tl_basic.config.xml";

	public interface Config extends ConfigurationItem {

		String API_KEY = "api-key";

		@Name(API_KEY)
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getApiKey();

	}

	private Path _dir;

	private PolymorphicConfiguration<? extends SecretStore> _savedStore;

	private Map<String, SecretStoreService.SecretOverride> _savedOverrides;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertTrue("Application configuration not available.", ApplicationConfig.Module.INSTANCE.isActive());
		_dir = Files.createTempDirectory("TestSecretStores");
		SecretStoreService.Config global = global();
		_savedStore = global.getStore();
		_savedOverrides = new LinkedHashMap<>(global.getOverrides());

		// The test configuration delivers the passwords of the test databases through a chain
		// with defaults; the tests start from the environment store the engine configures.
		global.setStore(TypedConfiguration.createConfigItemForImplementationClass(EnvironmentSecretStore.class));
		global.getOverrides().clear();
		restartService();
	}

	@Override
	protected void tearDown() throws Exception {
		SecretStoreService.Config global = global();
		global.setStore(_savedStore);
		global.getOverrides().clear();
		global.getOverrides().putAll(_savedOverrides);
		restartService();
		for (String name : new String[] { SECRET, OTHER, PREFIX + SECRET }) {
			System.clearProperty(name);
		}
		try (Stream<Path> files = Files.walk(_dir)) {
			files.sorted(Comparator.reverseOrder()).forEach(file -> file.toFile().delete());
		}
		super.tearDown();
	}

	public void testShortFormIsNamed() throws Exception {
		Config config = read("<config><api-key name='" + SECRET + "'/></config>");
		assertTrue(config.getApiKey() instanceof NamedSecretSource.Config);
		assertEquals(SECRET, ((NamedSecretSource.Config) config.getApiKey()).getName());

		String xml = toXML(config);
		assertFalse("Short form written with class: " + xml, xml.contains("class="));
		assertTrue("Not written as element: " + xml, xml.contains("<" + Config.API_KEY + " name=\"" + SECRET + "\""));

		Config copy = (Config) fromXML(xml);
		assertEquals(config, copy);
		assertTrue(copy.getApiKey() instanceof NamedSecretSource.Config);
	}

	public void testExplicitEnvironmentWrittenWithClass() throws Exception {
		Config config = read("<config><api-key class='" + EnvironmentSecretSource.class.getName()
			+ "' name='" + SECRET + "'/></config>");

		String xml = toXML(config);
		assertTrue("Environment source written without class: " + xml,
			xml.contains("class=\"" + EnvironmentSecretSource.class.getName() + "\""));

		Config copy = (Config) fromXML(xml);
		assertTrue(copy.getApiKey() instanceof EnvironmentSecretSource.Config);
		assertEquals(config, copy);
	}

	public void testDefaultStoreIsEnvironment() throws Exception {
		assertTrue(global().getStore() instanceof EnvironmentSecretStore.Config);

		SecretSource source = named(SECRET);
		System.setProperty(SECRET, "first");
		assertEquals("first", source.getSecret());

		System.setProperty(SECRET, "second");
		assertEquals("Variable must be read on each use.", "second", source.getSecret());
	}

	public void testDefaultStoreUndefined() throws Exception {
		assertFailure(I18NConstants.ERROR_SECRET_VARIABLE_UNDEFINED__NAME.fill(UNDEFINED), named(UNDEFINED));
	}

	public void testMatches() throws Exception {
		System.setProperty(SECRET, "s3cr3t");
		SecretSource source = named(SECRET);
		assertTrue(source.matches("s3cr3t"));
		assertFalse(source.matches("other"));
		assertFalse(source.matches(null));
	}

	public void testDirectoryStore() throws Exception {
		install("<store class='" + DirectorySecretStore.class.getName() + "' directory='" + _dir + "'/>");
		Path file = _dir.resolve(SECRET);
		write(file, "s3cr3t\n");
		SecretSource source = named(SECRET);

		assertEquals("Surrounding white space must be removed.", "s3cr3t", source.getSecret());

		write(file, "  rotated-secret \r\n");
		assertEquals("Changed file must be read again.", "rotated-secret", source.getSecret());
	}

	public void testDirectoryStoreInHome() throws Exception {
		String home = System.getProperty("user.home");
		System.setProperty("user.home", _dir.toString());
		try {
			Path secrets = Files.createDirectories(_dir.resolve(".app/secrets"));
			write(secrets.resolve(SECRET), "s3cr3t");
			install("<store class='" + DirectorySecretStore.class.getName() + "' directory='~/.app/secrets'/>");

			assertEquals("s3cr3t", named(SECRET).getSecret());
		} finally {
			System.setProperty("user.home", home);
		}
	}

	public void testDirectoryStoreMissing() throws Exception {
		install("<store class='" + DirectorySecretStore.class.getName() + "' directory='" + _dir + "'/>");

		assertFailure(I18NConstants.ERROR_SECRET_FILE_UNREADABLE__PATH.fill(_dir.resolve(UNDEFINED).toString()),
			named(UNDEFINED));
	}

	public void testDirectoryStoreEmpty() throws Exception {
		install("<store class='" + DirectorySecretStore.class.getName() + "' directory='" + _dir + "'/>");
		write(_dir.resolve(SECRET), " \n");

		assertFailure(I18NConstants.ERROR_SECRET_FILE_EMPTY__PATH.fill(_dir.resolve(SECRET).toString()),
			named(SECRET));
	}

	public void testDirectoryStoreRejectsPath() throws Exception {
		Path sub = Files.createDirectory(_dir.resolve("sub"));
		write(_dir.resolve(SECRET), "outside");
		install("<store class='" + DirectorySecretStore.class.getName() + "' directory='" + sub + "'/>");

		String name = "../" + SECRET;
		assertFailure(I18NConstants.ERROR_SECRET_NAME_INVALID__NAME.fill(name), named(name));
	}

	public void testLiteralStore() throws Exception {
		String encrypted = ConfigurationEncryption.encrypt("encrypted-secret");
		install("<store class='" + LiteralSecretStore.class.getName() + "'><secrets>"
			+ "<secret name='" + SECRET + "' value='" + encrypted + "'/>"
			+ "<secret name='" + OTHER + "' value='unencrypted:plain-secret'/>"
			+ "</secrets></store>");

		assertEquals("encrypted-secret", named(SECRET).getSecret());
		assertEquals("plain-secret", named(OTHER).getSecret());
		assertFailure(I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(UNDEFINED), named(UNDEFINED));
	}

	public void testLiteralStoreWrittenEncrypted() throws Exception {
		SecretStoreService.Config config = read("<" + STORES_TAG + SERVICE_CLASS + "><store class='"
			+ LiteralSecretStore.class.getName()
			+ "'><secrets><secret name='" + SECRET + "' value='unencrypted:plain-secret'/></secrets></store>"
			+ "</" + STORES_TAG + ">");

		String xml = toXML(config);
		assertFalse("Secret written in plain text: " + xml, xml.contains("plain-secret"));
		assertTrue("Secret not written encrypted: " + xml,
			xml.contains("value=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		SecretStoreService.Config copy = (SecretStoreService.Config) fromXML(xml);
		assertEquals(config, copy);
		installStore(copy);
		assertEquals("plain-secret", named(SECRET).getSecret());
	}

	public void testChain() throws Exception {
		System.setProperty(OTHER, "from-env");
		install("<store class='" + ChainSecretStore.class.getName() + "'><stores>"
			+ "<" + LiteralSecretStore.Config.TAG_NAME + "><secrets>"
			+ "<secret name='" + SECRET + "' value='unencrypted:from-literal'/>"
			+ "<secret name='" + OTHER + "' value='unencrypted:shadowing'/>"
			+ "</secrets></" + LiteralSecretStore.Config.TAG_NAME + ">"
			+ "<" + EnvironmentSecretStore.Config.TAG_NAME + "/>"
			+ "</stores></store>");

		assertEquals("from-literal", named(SECRET).getSecret());
		assertEquals("First store wins.", "shadowing", named(OTHER).getSecret());
	}

	public void testChainFallthrough() throws Exception {
		System.setProperty(OTHER, "from-env");
		install("<store class='" + ChainSecretStore.class.getName() + "'><stores>"
			+ "<" + DirectorySecretStore.Config.TAG_NAME + " directory='" + _dir + "'/>"
			+ "<" + EnvironmentSecretStore.Config.TAG_NAME + "/>"
			+ "</stores></store>");
		write(_dir.resolve(SECRET), "from-file");

		assertEquals("from-file", named(SECRET).getSecret());
		assertEquals("Missing file falls through to next store.", "from-env", named(OTHER).getSecret());
	}

	public void testChainAllMissing() throws Exception {
		install("<store class='" + ChainSecretStore.class.getName() + "'><stores>"
			+ "<" + DirectorySecretStore.Config.TAG_NAME + " directory='" + _dir + "'/>"
			+ "<" + EnvironmentSecretStore.Config.TAG_NAME + "/>"
			+ "</stores></store>");

		assertFailure(I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(UNDEFINED), named(UNDEFINED));
	}

	public void testChainBrokenDoesNotFallThrough() throws Exception {
		System.setProperty(SECRET, "from-env");
		install("<store class='" + ChainSecretStore.class.getName() + "'><stores>"
			+ "<" + DirectorySecretStore.Config.TAG_NAME + " directory='" + _dir + "'/>"
			+ "<" + EnvironmentSecretStore.Config.TAG_NAME + "/>"
			+ "</stores></store>");
		write(_dir.resolve(SECRET), "\n");

		assertFailure(I18NConstants.ERROR_SECRET_FILE_EMPTY__PATH.fill(_dir.resolve(SECRET).toString()),
			named(SECRET));
	}

	public void testPrefix() throws Exception {
		System.setProperty(SECRET, "unprefixed");
		System.setProperty(PREFIX + SECRET, "prefixed");
		install("<store class='" + EnvironmentSecretStore.class.getName() + "' prefix='" + PREFIX + "'/>");

		assertEquals("prefixed", named(SECRET).getSecret());
		assertFailure(I18NConstants.ERROR_SECRET_VARIABLE_UNDEFINED__NAME.fill(PREFIX + UNDEFINED), named(UNDEFINED));
	}

	public void testChainPrefix() throws Exception {
		write(_dir.resolve(PREFIX + SECRET), "prefixed-file");
		install("<store class='" + ChainSecretStore.class.getName() + "' prefix='" + PREFIX + "'><stores>"
			+ "<" + DirectorySecretStore.Config.TAG_NAME + " directory='" + _dir + "'/>"
			+ "</stores></store>");

		assertEquals("prefixed-file", named(SECRET).getSecret());
		assertFailure(I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(PREFIX + UNDEFINED), named(UNDEFINED));
	}

	public void testOverride() throws Exception {
		System.setProperty(SECRET, "from-env");
		System.setProperty(OTHER, "other-from-env");
		Path file = _dir.resolve("override");
		write(file, "from-override");
		install("<overrides><override name='" + SECRET + "'><source class='"
			+ "com.top_logic.basic.credential.FileSecretSource' path='" + file + "'/></override></overrides>");

		assertEquals("from-override", named(SECRET).getSecret());
		assertEquals("other-from-env", named(OTHER).getSecret());

		install("<overrides><override name='" + SECRET + "' source='unencrypted:changed-override'/></overrides>");
		assertEquals("Changed override must be used.", "changed-override", named(SECRET).getSecret());

		install("");
		assertEquals("Removed override must no longer be used.", "from-env", named(SECRET).getSecret());
	}

	public void testOverrideAlias() throws Exception {
		System.setProperty(OTHER, "aliased");
		install("<overrides><override name='" + SECRET + "'><source name='" + OTHER + "'/></override></overrides>");

		assertEquals("aliased", named(SECRET).getSecret());
	}

	public void testOverrideCycle() throws Exception {
		install("<overrides>"
			+ "<override name='" + SECRET + "'><source name='" + OTHER + "'/></override>"
			+ "<override name='" + OTHER + "'><source name='" + SECRET + "'/></override>"
			+ "</overrides>");

		assertFailure(I18NConstants.ERROR_SECRET_OVERRIDE_CYCLE__NAME.fill(SECRET), named(SECRET));
	}

	public void testErrorsNeverContainSecret() throws Exception {
		install("<store class='" + LiteralSecretStore.class.getName() + "' prefix='" + PREFIX + "'><secrets>"
			+ "<secret name='" + PREFIX + SECRET + "' value='unencrypted:TopSecretValue'/>"
			+ "<secret name='" + PREFIX + OTHER + "' value='unencrypted:'/>"
			+ "</secrets></store>");

		assertNoSecret(failure(named(UNDEFINED)), "TopSecretValue");
		assertNoSecret(failure(named(OTHER)), "TopSecretValue");

		Throwable parseFailure = null;
		try {
			read("<" + STORES_TAG + SERVICE_CLASS + "><store class='" + LiteralSecretStore.class.getName()
				+ "'><secrets>"
				+ "<secret name='" + SECRET + "' value='PlainTextPassword12345'/>"
				+ "</secrets></store></" + STORES_TAG + ">");
		} catch (Throwable ex) {
			parseFailure = ex;
		}
		assertNotNull("Plain text value accepted.", parseFailure);
		assertNoSecret(parseFailure, "PlainTextPassword12345");
	}

	/**
	 * The configuration of the {@link SecretStoreService} in an installation-specific fragment
	 * (loaded last, like a <code>tl_config</code> or <code>autoconf</code> fragment) is merged with
	 * the engine configuration by the reader of the application configuration: its store replaces
	 * the store of the engine, its overrides are added to the overrides of the configuration before
	 * it.
	 */
	public void testOverlayFragment() throws ConfigurationException, IOException {
		List<BinaryContent> engine = XMLProperties.Module.INSTANCE.config().getTypedConfigs();
		assertTrue("No engine configuration loaded.", !engine.isEmpty());

		SecretStoreService.Config engineOnly =
			serviceConfig(Collections.singletonList(FileManager.getInstance().getData(ENGINE_CONFIG)));
		assertTrue("Engine store: " + engineOnly.getStore(), engineOnly.getStore() instanceof EnvironmentSecretStore.Config);

		BinaryContent base = fragment("base.config.xml",
			"<store class='" + EnvironmentSecretStore.class.getName() + "'/>"
				+ "<overrides><override name='base_secret' source='unencrypted:from-base'/></overrides>");
		BinaryContent overlay = fragment("overlay.config.xml",
			"<store class='" + DirectorySecretStore.class.getName() + "' directory='" + _dir + "'/>"
				+ "<overrides><override name='overlay_secret' source='unencrypted:from-overlay'/></overrides>");
		List<BinaryContent> sources = new ArrayList<>(engine);
		sources.add(base);
		sources.add(overlay);

		SecretStoreService.Config merged = serviceConfig(sources);

		assertTrue("Store of the overlay replaces the store before it: " + merged.getStore(),
			merged.getStore() instanceof DirectorySecretStore.Config);
		assertEquals(_dir.toString(), ((DirectorySecretStore.Config) merged.getStore()).getDirectory());
		assertEquals("Overrides are added.", Set.of("base_secret", "overlay_secret"), merged.getOverrides().keySet());

		installStore(merged);
		write(_dir.resolve(SECRET), "from-directory");
		assertEquals("from-directory", named(SECRET).getSecret());
		assertEquals("from-base", named("base_secret").getSecret());
		assertEquals("from-overlay", named("overlay_secret").getSecret());
	}

	/**
	 * The development secrets of an application: a {@link ChainSecretStore} asking the environment
	 * first and a {@link DirectorySecretStore} with one file per secret after it. A variable takes
	 * precedence over the file, in plain form or with the prefix <code>unencrypted:</code>.
	 */
	public void testDevelopmentSecretsDirectory() throws Exception {
		install("<store class='" + ChainSecretStore.class.getName() + "'><stores>"
			+ "<" + EnvironmentSecretStore.Config.TAG_NAME + "/>"
			+ "<" + DirectorySecretStore.Config.TAG_NAME + " directory='" + _dir + "'/>"
			+ "</stores></store>");
		write(_dir.resolve(SECRET), "from-file\n");

		assertEquals("from-file", named(SECRET).getSecret());

		System.setProperty(SECRET, "from-env");
		assertEquals("from-env", named(SECRET).getSecret());

		System.setProperty(SECRET, "unencrypted:prefixed-env");
		assertEquals("prefixed-env", named(SECRET).getSecret());

		assertFailure(I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(OTHER), named(OTHER));
	}

	/**
	 * A variable read by the environment store holds the plain secret or, as accepted form, the
	 * secret with the prefix <code>unencrypted:</code>; the prefix alone counts as undefined.
	 */
	public void testEnvironmentStorePrefix() throws Exception {
		System.setProperty(SECRET, "plain-value");
		assertEquals("plain-value", named(SECRET).getSecret());

		System.setProperty(SECRET, "unencrypted:prefixed-value");
		assertEquals("prefixed-value", named(SECRET).getSecret());
		assertEquals("prefixed-value", named(SECRET).lookup());

		System.setProperty(SECRET, "unencrypted:");
		assertNull(named(SECRET).lookup());
		I18NRuntimeException ex = failure(named(SECRET));
		assertEquals(I18NConstants.ERROR_SECRET_VARIABLE_UNDEFINED__NAME.fill(SECRET), ex.getErrorKey());
		assertNoSecret(ex, "unencrypted:");
	}

	/**
	 * A named secret cannot be read while the {@link SecretStoreService} is not started.
	 */
	public void testServiceNotStarted() throws Exception {
		System.setProperty(SECRET, "from-env");
		SecretSource source = named(SECRET);
		assertEquals("from-env", source.getSecret());

		ModuleUtil.INSTANCE.shutDown(SecretStoreService.Module.INSTANCE);
		try {
			assertFailure(I18NConstants.ERROR_SECRET_STORE_NOT_STARTED__NAME.fill(SECRET), source);
		} finally {
			ModuleUtil.INSTANCE.startUp(SecretStoreService.Module.INSTANCE);
		}
		assertEquals("from-env", source.getSecret());
	}

	/**
	 * Reads the given typed configuration files the way the application configuration is loaded
	 * and returns the configuration of the {@link SecretStoreService}.
	 */
	private SecretStoreService.Config serviceConfig(List<BinaryContent> sources) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(ApplicationConfig.ROOT_TAG,
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		ApplicationConfig.Config config = (ApplicationConfig.Config) new ConfigurationReader(context, descriptors)
			.setSources(sources)
			.setVariableExpander(AliasManager.getInstance().getExpander())
			.read();
		context.checkErrors();
		return (SecretStoreService.Config) config.getServices().get(SecretStoreService.class).getInstance();
	}

	private static BinaryContent fragment(String name, String content) {
		String xml = "<?xml version='1.0' encoding='utf-8' ?>"
			+ "<application config:interface='com.top_logic.basic.config.ApplicationConfig$Config'"
			+ " xmlns:config='http://www.top-logic.com/ns/config/6.0'><services>"
			+ "<config service-class='" + SecretStoreService.class.getName() + "'>"
			+ "<instance class='" + SecretStoreService.class.getName() + "'>" + content + "</instance>"
			+ "</config></services></application>";
		return BinaryDataFactory.createBinaryData(xml.getBytes(StandardCharsets.UTF_8), "text/xml", name);
	}

	private SecretSource named(String name) throws ConfigurationException {
		Config config = read("<config><api-key name='" + name + "'/></config>");
		return getInstance(config.getApiKey());
	}

	/**
	 * Installs the given content of a {@link SecretStoreService.Config} as configuration of the
	 * running service.
	 */
	private void install(String content) throws ConfigurationException {
		installStore(read("<" + STORES_TAG + SERVICE_CLASS + ">" + content + "</" + STORES_TAG + ">"));
	}

	private void installStore(SecretStoreService.Config config) {
		SecretStoreService.Config global = global();
		global.setStore(TypedConfiguration.copy(config.getStore()));
		global.getOverrides().clear();
		for (SecretStoreService.SecretOverride override : config.getOverrides().values()) {
			SecretStoreService.SecretOverride copy = TypedConfiguration.copy(override);
			global.getOverrides().put(copy.getName(), copy);
		}
		restartService();
	}

	/**
	 * Starts the {@link SecretStoreService} again with its current configuration.
	 */
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

	private static I18NRuntimeException failure(SecretSource source) {
		try {
			source.getSecret();
			fail("Missing secret not reported.");
			return null;
		} catch (I18NRuntimeException ex) {
			return ex;
		}
	}

	private static void assertFailure(Object expectedKey, SecretSource source) {
		assertEquals(expectedKey, failure(source).getErrorKey());
	}

	private static void assertNoSecret(Throwable ex, String secret) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			assertTrue("Secret in message: " + message, message == null || !message.contains(secret));
		}
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		Map<String, ConfigurationDescriptor> result = new HashMap<>();
		result.put(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
		result.put(STORES_TAG, TypedConfiguration.getConfigurationDescriptor(SecretStoreService.Config.class));
		return result;
	}

	public static Test suite() {
		return suite(TestSecretStores.class, SecretStoreService.Module.INSTANCE);
	}

}
