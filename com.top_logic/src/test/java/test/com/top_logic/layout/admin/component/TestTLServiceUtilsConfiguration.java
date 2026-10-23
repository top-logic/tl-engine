/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.admin.component;

import java.io.File;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
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
		 * Module of {@link ConfiguredService}.
		 */
		public static final class Module extends TestModule<ConfiguredService> {

			/** Singleton {@link Module} instance. */
			public static final Module INSTANCE = new Module();

			private Module() {
				super(ConfiguredService.class);
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
		}

	}

	/**
	 * Module of a test service that is never started.
	 */
	static class TestModule<M extends ManagedClass> extends BasicRuntimeModule<M> {

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

		@Override
		protected M newImplementationInstance() throws ModuleException {
			throw new UnsupportedOperationException("Test module is not started.");
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
	 * Storing and resetting a configuration changes the effective configuration and marks the
	 * module as changed.
	 */
	public void testStoreAndReset() {
		ConfiguredService.Module module = ConfiguredService.Module.INSTANCE;
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
		assertTrue(new File(_autoconf, InAppServiceConfigStore.fileName(module)).exists());
		assertEquals(42, effectiveCount(module));
		assertTrue(module.isConfigurationChanged());

		assertTrue(TLServiceUtils.resetServiceConfiguration(module));
		assertFalse(TLServiceUtils.hasInAppConfiguration(module));
		assertEquals(1, effectiveCount(module));
		assertTrue(module.isConfigurationChanged());
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
	}

	/**
	 * A service annotated with {@link InApp} <code>false</code> is not editable.
	 */
	public void testNotInAppEditable() {
		assertFalse(TLServiceUtils.isInAppEditable(FixedService.Module.INSTANCE));
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
