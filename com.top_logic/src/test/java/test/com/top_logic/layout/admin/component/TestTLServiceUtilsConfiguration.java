/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.admin.component;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.util.autoconf.InAppServiceConfigStore;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of the service configuration operations of {@link TLServiceUtils}.
 *
 * <p>
 * The test redirects the autoconf folder to a temporary directory through the system property
 * {@link XMLProperties.Setting#AUTO_CONF}, so that no configuration is written to the source tree.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTLServiceUtilsConfiguration extends BasicTestCase {

	/**
	 * Service with a configuration to edit.
	 */
	public static class ConfiguredService extends ManagedClass {

		/**
		 * Configuration of {@link ConfiguredService}.
		 */
		public interface Config<I extends ConfiguredService> extends ServiceConfiguration<I> {

			/** Configuration name of {@link #getCount()}. */
			String COUNT = "count";

			/**
			 * An example value.
			 */
			@Name(COUNT)
			@IntDefault(1)
			@Constraint(NonNegative.class)
			int getCount();

			/**
			 * @see #getCount()
			 */
			void setCount(int value);

		}

		/**
		 * Creates a {@link ConfiguredService} from configuration.
		 */
		public ConfiguredService(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		/**
		 * Creates a {@link ConfiguredService} started by the test.
		 */
		ConfiguredService() {
			super();
		}

		/**
		 * Module of {@link ConfiguredService}.
		 */
		public static final class Module extends TestModule<ConfiguredService> {

			/** Singleton {@link Module} instance. */
			public static final Module INSTANCE = new Module();

			private Module() {
				super(ConfiguredService.class);
			}

			@Override
			protected ConfiguredService newImplementationInstance() throws ModuleException {
				return new ConfiguredService();
			}
		}

	}

	/**
	 * Service whose configuration must not be edited in the application.
	 */
	@InApp(false)
	public static class FixedService extends ManagedClass {

		/**
		 * Module of {@link FixedService}.
		 */
		public static final class Module extends TestModule<FixedService> {

			/** Singleton {@link Module} instance. */
			public static final Module INSTANCE = new Module();

			private Module() {
				super(FixedService.class);
			}

			@Override
			protected FixedService newImplementationInstance() throws ModuleException {
				throw new UnsupportedOperationException("Test module is not started.");
			}
		}

	}

	/**
	 * Service whose module creates it without a configuration.
	 */
	public static class PlainService extends ManagedClass {

		/**
		 * Creates a {@link PlainService}, there is no configuration constructor.
		 */
		protected PlainService() {
			super();
		}

		/**
		 * Module of {@link PlainService}.
		 */
		public static final class Module extends BasicRuntimeModule<PlainService> {

			/** Singleton {@link Module} instance. */
			public static final Module INSTANCE = new Module();

			private Module() {
				super();
			}

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return NO_DEPENDENCIES;
			}

			@Override
			public Class<PlainService> getImplementation() {
				return PlainService.class;
			}

			@Override
			protected PlainService newImplementationInstance() throws ModuleException {
				return new PlainService();
			}
		}

	}

	/**
	 * Module of a test service created from its service configuration.
	 */
	abstract static class TestModule<M extends ManagedClass> extends TypedRuntimeModule<M> {

		private final Class<M> _implementation;

		TestModule(Class<M> implementation) {
			_implementation = implementation;
		}

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return NO_DEPENDENCIES;
		}

		@Override
		public Class<M> getImplementation() {
			return _implementation;
		}
	}

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
		ConfiguredService.Module.INSTANCE.discardConfigurationBaseline();

		assertEquals("Autoconf folder not redirected, test would write to the application.",
			_autoconf.getAbsoluteFile(), XMLProperties.Setting.resolveAutoconfFolder().getAbsoluteFile());
	}

	@Override
	protected void tearDown() throws Exception {
		if (ConfiguredService.Module.INSTANCE.isActive()) {
			ModuleUtil.INSTANCE.shutDown(ConfiguredService.Module.INSTANCE);
		}
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
	 * A service configured with another configuration interface than a service configuration has
	 * no service configuration, which is no error.
	 */
	public void testServiceWithoutServiceConfiguration() throws ConfigurationException {
		assertFalse(ApplicationConfig.getInstance().hasServiceConfiguration(ApplicationConfig.class));
		assertFalse(TLServiceUtils.hasServiceConfiguration(ApplicationConfig.Module.INSTANCE));
		assertNull(TLServiceUtils.getServiceConfiguration(ApplicationConfig.Module.INSTANCE));
	}

	/**
	 * A service whose module creates it itself has no service configuration, also when it cannot
	 * be created from a configuration at all, which is no error.
	 */
	public void testServiceCreatedByModule() {
		assertFalse(TLServiceUtils.hasServiceConfiguration(PlainService.Module.INSTANCE));
		assertNull(TLServiceUtils.getServiceConfiguration(PlainService.Module.INSTANCE));
	}

	/**
	 * Storing and resetting a configuration changes the effective configuration; the module reports
	 * a change only while the stored configuration differs from the one it was started with.
	 */
	public void testStoreAndReset() throws ModuleException {
		ConfiguredService.Module module = startedModule();
		assertTrue(TLServiceUtils.hasServiceConfiguration(module));
		assertTrue(TLServiceUtils.isInAppEditable(module));
		assertFalse(TLServiceUtils.hasInAppConfiguration(module));
		assertFalse("Nothing to reset.", TLServiceUtils.resetServiceConfiguration(module));
		assertFalse(module.isConfigurationChanged());

		ConfiguredService.Config<?> config = editableConfig(module);
		assertEquals(1, config.getCount());
		config.setCount(42);
		assertEquals("Editing the copy does not change the effective configuration.", 1, effectiveCount(module));

		TLServiceUtils.storeServiceConfiguration(module, config);
		assertTrue(TLServiceUtils.hasInAppConfiguration(module));
		assertTrue(storedFile(module).exists());
		assertEquals(42, effectiveCount(module));
		assertTrue(module.isConfigurationChanged());

		assertTrue(TLServiceUtils.resetServiceConfiguration(module));
		assertFalse(TLServiceUtils.hasInAppConfiguration(module));
		assertEquals(1, effectiveCount(module));
		assertFalse("Back to the state the service was started with.", module.isConfigurationChanged());
		assertFalse("Nothing to discard.", TLServiceUtils.discardConfigurationChanges(module));
	}

	/**
	 * Discarding the changes of a service started without stored configuration removes the stored
	 * configuration.
	 */
	public void testDiscardToAbsent() throws ModuleException {
		ConfiguredService.Module module = startedModule();
		storeCount(module, 42);
		storeCount(module, 43);
		assertTrue(module.isConfigurationChanged());

		assertTrue(TLServiceUtils.discardConfigurationChanges(module));
		assertFalse(storedFile(module).exists());
		assertEquals(1, effectiveCount(module));
		assertFalse(module.isConfigurationChanged());
		assertNull(module.getConfigurationBaseline());
		assertFalse("Nothing left to discard.", TLServiceUtils.discardConfigurationChanges(module));
	}

	/**
	 * Discarding the changes of a service started with a stored configuration restores that
	 * configuration; a restart drops the baseline.
	 */
	public void testDiscardToContent() throws Exception {
		ConfiguredService.Module module = startedModule();
		storeCount(module, 5);
		assertTrue(module.isConfigurationChanged());

		ModuleUtil.INSTANCE.restart(module, null);
		assertNull("Restart applies the stored configuration.", module.getConfigurationBaseline());
		assertFalse(module.isConfigurationChanged());
		byte[] startContent = Files.readAllBytes(storedFile(module).toPath());

		storeCount(module, 42);
		assertTrue(module.isConfigurationChanged());
		assertTrue(TLServiceUtils.resetServiceConfiguration(module));
		assertTrue("Reset removes the configuration the service was started with.",
			module.isConfigurationChanged());

		assertTrue(TLServiceUtils.discardConfigurationChanges(module));
		assertTrue(Arrays.equals(startContent, Files.readAllBytes(storedFile(module).toPath())));
		assertEquals(5, effectiveCount(module));
		assertFalse(module.isConfigurationChanged());
	}

	/**
	 * Storing the configuration a service was started with is no change, even though a stored
	 * configuration did not exist before; a different value is a change, and discarding removes the
	 * stored configuration again.
	 */
	public void testStoreUnchangedValues() throws ModuleException {
		ConfiguredService.Module module = startedModule();
		assertFalse(storedFile(module).exists());

		storeCount(module, 1);
		assertTrue(storedFile(module).exists());
		assertFalse("The stored values are the ones in use.", module.isConfigurationChanged());

		storeCount(module, 2);
		assertTrue(module.isConfigurationChanged());

		assertTrue(TLServiceUtils.discardConfigurationChanges(module));
		assertFalse(storedFile(module).exists());
		assertFalse(module.isConfigurationChanged());
	}

	/**
	 * A configuration violating its constraints is not stored.
	 */
	public void testStoreInvalid() {
		ConfiguredService.Module module = ConfiguredService.Module.INSTANCE;
		ConfiguredService.Config<?> config = editableConfig(module);
		config.setCount(-1);

		try {
			TLServiceUtils.storeServiceConfiguration(module, config);
			fail("Constraint violation not detected.");
		} catch (TopLogicException ex) {
			// Expected.
		}
		assertFalse(TLServiceUtils.hasInAppConfiguration(module));
		assertEquals(1, effectiveCount(module));
		assertFalse("A failed store is no change.", module.isConfigurationChanged());
	}

	/**
	 * A service annotated with {@link InApp} <code>false</code> is not editable.
	 */
	public void testNotInAppEditable() {
		assertFalse(TLServiceUtils.isInAppEditable(FixedService.Module.INSTANCE));
	}

	private static ConfiguredService.Module startedModule() throws ModuleException {
		ConfiguredService.Module module = ConfiguredService.Module.INSTANCE;
		ModuleUtil.INSTANCE.startUp(module);
		return module;
	}

	private static void storeCount(ConfiguredService.Module module, int count) {
		ConfiguredService.Config<?> config = editableConfig(module);
		config.setCount(count);
		TLServiceUtils.storeServiceConfiguration(module, config);
	}

	private static File storedFile(ConfiguredService.Module module) {
		return InAppServiceConfigStore.fileFor(module);
	}

	private static ConfiguredService.Config<?> editableConfig(ConfiguredService.Module module) {
		return (ConfiguredService.Config<?>) TLServiceUtils.getServiceConfiguration(module);
	}

	private static int effectiveCount(ConfiguredService.Module module) {
		return editableConfig(module).getCount();
	}

	/**
	 * Tests of this suite.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(new TestSuite(TestTLServiceUtilsConfiguration.class));
	}

}
