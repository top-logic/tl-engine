/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.admin.component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.DirectorySecretStore;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Test that the {@link SecretStoreService} is edited in the service editor: its configuration is
 * stored in the <code>autoconf</code> folder and read back unchanged.
 *
 * <p>
 * The test redirects the autoconf folder to a temporary directory through the system property
 * {@link XMLProperties.Setting#AUTO_CONF}, so that no configuration is written to the source tree.
 * </p>
 */
public class TestSecretStoreServiceEditing extends BasicTestCase {

	private static final String SECRET_DIR = "/run/secrets";

	private static final String OVERRIDE = "db_password";

	private static final String OVERRIDE_VALUE = "override-secret-value";

	private File _autoconf;

	private String _formerAutoconf;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_autoconf = BasicTestCase.createNamedTestFile("autoconf");
		FileUtilities.deleteR(_autoconf);
		FileUtilities.enforceDirectory(_autoconf);

		_formerAutoconf = System.getProperty(XMLProperties.Setting.AUTO_CONF);
		System.setProperty(XMLProperties.Setting.AUTO_CONF, _autoconf.getAbsolutePath());
		TLServiceUtils.reloadConfigurations();

		assertEquals("Autoconf folder not redirected, test would write to the application.",
			_autoconf.getAbsoluteFile(), XMLProperties.Setting.resolveAutoconfFolder().getAbsoluteFile());
	}

	@Override
	protected void tearDown() throws Exception {
		FileUtilities.deleteR(_autoconf);
		if (_formerAutoconf == null) {
			System.clearProperty(XMLProperties.Setting.AUTO_CONF);
		} else {
			System.setProperty(XMLProperties.Setting.AUTO_CONF, _formerAutoconf);
		}
		TLServiceUtils.reloadConfigurations();
		super.tearDown();
	}

	/**
	 * The {@link SecretStoreService} is a service whose configuration is edited in the application.
	 */
	public void testEditable() {
		SecretStoreService.Module module = SecretStoreService.Module.INSTANCE;
		assertTrue(TLServiceUtils.hasServiceConfiguration(module));
		assertTrue(TLServiceUtils.isInAppEditable(module));
	}

	/**
	 * A store and an override written by the service editor are read back; the secret of a literal
	 * override is written encrypted.
	 */
	public void testStoreAndRead() throws Exception {
		SecretStoreService.Module module = SecretStoreService.Module.INSTANCE;
		SecretStoreService.Config config = (SecretStoreService.Config) TLServiceUtils.getServiceConfiguration(module);

		DirectorySecretStore.Config store = TypedConfiguration.newConfigItem(DirectorySecretStore.Config.class);
		store.setDirectory(SECRET_DIR);
		config.setStore(store);

		LiteralSecretSource.Config literal = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
		literal.setValue(OVERRIDE_VALUE);
		SecretStoreService.SecretOverride override =
			TypedConfiguration.newConfigItem(SecretStoreService.SecretOverride.class);
		override.setName(OVERRIDE);
		override.setSource(literal);
		config.getOverrides().put(OVERRIDE, override);

		TLServiceUtils.storeServiceConfiguration(module, config);

		File file = InAppServiceConfigStore.fileFor(module);
		assertTrue(file.exists());
		String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		assertFalse("Secret written in plain text: " + content, content.contains(OVERRIDE_VALUE));

		ApplicationConfig.Config stored = InAppServiceConfigStore.read(file);
		SecretStoreService.Config read =
			(SecretStoreService.Config) stored.getServices().get(SecretStoreService.class).getInstance();
		assertTrue(read.getStore() instanceof DirectorySecretStore.Config);
		assertEquals(SECRET_DIR, ((DirectorySecretStore.Config) read.getStore()).getDirectory());
		SecretStoreService.SecretOverride readOverride = read.getOverrides().get(OVERRIDE);
		assertNotNull(readOverride);
		assertEquals(OVERRIDE_VALUE, ((LiteralSecretSource.Config) readOverride.getSource()).getValue());

		SecretStoreService.Config effective =
			(SecretStoreService.Config) TLServiceUtils.getServiceConfiguration(module);
		assertTrue("Stored configuration is effective.", effective.getStore() instanceof DirectorySecretStore.Config);

		assertTrue(TLServiceUtils.resetServiceConfiguration(module));
		assertFalse(file.exists());
	}

	/**
	 * Tests of this suite.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(new TestSuite(TestSecretStoreServiceEditing.class));
	}

}
