/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import java.util.Collection;
import java.util.Properties;

import junit.framework.Test;

import test.com.top_logic.basic.CustomPropertiesSetup;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfiguredRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.sql.ConnectionPoolRegistry;

/**
 * Tests for {@link com.top_logic.basic.module.ConfiguredRuntimeModule}
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestConfiguredRuntimeModule extends AbstractModuleTest {

	static final boolean HAS_CONFIGURATION_CONSTRUCTOR = true;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		moduleUtil.startXMLProperties();
	}

	/**
	 * Test method for
	 * {@link com.top_logic.basic.module.ConfiguredRuntimeModule#newImplementationInstance(com.top_logic.basic.Configuration.IterableConfiguration)}
	 * .
	 */
	public void testNewImplementationInstance() throws ModuleException, ConfigurationException {
		TestedConfiguredRuntimeModule crm = new TestedConfiguredRuntimeModule(!HAS_CONFIGURATION_CONSTRUCTOR);

		IterableConfiguration iterableConf = Configuration.getConfiguration(TestConfiguredRuntimeModule.class);
		SimpleManagedClass smc = crm.newImplementationInstance(iterableConf);
		assertNull(smc.configUsed);
		assertEquals(iterableConf.getProperties(), smc.poropertiesUsed); // Cannot
																			// be
																			// same,
																			// well
		assertFalse(smc.shutDownCalled);
	}

	/**
	 * Test method for
	 * {@link com.top_logic.basic.module.ConfiguredRuntimeModule#newImplementationInstance(com.top_logic.basic.Configuration.IterableConfiguration)}
	 * .
	 */
	public void testNewImplementationInstanceWithConfig() throws ModuleException, ConfigurationException {
		TestedConfiguredRuntimeModule crm = new TestedConfiguredRuntimeModule(HAS_CONFIGURATION_CONSTRUCTOR);
		IterableConfiguration iterableConf = Configuration.getConfiguration(TestConfiguredRuntimeModule.class);
		SimpleManagedClass smc = crm.newImplementationInstance(iterableConf);
		assertSame(iterableConf, smc.configUsed);
		assertNull(smc.poropertiesUsed);
		assertFalse(smc.shutDownCalled);
	}

	public void testConfiguredImplementationClass() throws ModuleException {
		final SimpleManagedClass.Module module = SimpleManagedClass.Module.INSTANCE;
		moduleUtil.startUp(module);

		final SimpleManagedClass implementationInstance = module.getImplementationInstance();
		assertTrue("ExtendedSimpleManagedClass was configured as implementaion class",
				implementationInstance instanceof ExtendedSimpleManagedClass);
	}

	public void testDependencies() {
		final SimpleManagedClass.Module module = SimpleManagedClass.Module.INSTANCE;

		final Collection<? extends Class<? extends BasicRuntimeModule<?>>> dependencies = module.getDependencies();
		assertTrue("Does not contain anotated dependency", dependencies.contains(SimpleManagedClass2.Module.class));
		assertTrue("ConnectionPoolRegistry was configured as dependency",
				dependencies.contains(ConnectionPoolRegistry.Module.class));
	}

	public void testNonExistingDependency() {
		final NonExistingDependencyConfigured.Module module = NonExistingDependencyConfigured.Module.INSTANCE;
		if (module == null) {
			fail();
			return;
		}
		try {
			module.getDependencies();
			fail("Expected failure as of non existing dependency configuration");
		} catch (ConfigurationError ex) {
			// expected
		}

	}

	// /**
	// * Test method for {@link
	// com.top_logic.basic.module.ConfiguredRuntimeModule#getDependencies()}.
	// */
	// public void testGetDependencies() {
	// fail("Not yet implemented");
	// }
	//
	// /**
	// * Test method for {@link
	// com.top_logic.basic.module.ConfiguredRuntimeModule#hasConfigurationConstructor()}.
	// */
	// public void testHasConfigurationConstructor() {
	// fail("Not yet implemented");
	// }
	//
	// /**
	// * Test method for {@link
	// com.top_logic.basic.module.ConfiguredRuntimeModule#needsThreadContext()}.
	// */
	// public void testNeedsThreadContext() {
	// fail("Not yet implemented");
	// }
	//
	// /**
	// * Test method for {@link
	// com.top_logic.basic.module.ConfiguredRuntimeModule#getDefaultImplementationClass()}.
	// */
	// public void testGetDefaultImplementationClass() {
	// fail("Not yet implemented");
	// }
	//
	// /**
	// * Test method for {@link
	// com.top_logic.basic.module.ConfiguredRuntimeModule#getClassPropertyName()}.
	// */
	// public void testGetClassPropertyName() {
	// fail("Not yet implemented");
	// }
	//
	// /**
	// * Test method for {@link
	// com.top_logic.basic.module.ConfiguredRuntimeModule#addDependencies(java.util.Collection,
	// java.lang.Class, java.lang.String,
	// com.top_logic.basic.module.BasicRuntimeModule)}.
	// */
	// public void
	// testAddDependenciesCollectionOfClassOfQextendsBasicRuntimeModuleOfQClassOfQStringBasicRuntimeModuleOfQ()
	// {
	// fail("Not yet implemented");
	// }
	//
	// /**
	// * Test method for {@link
	// com.top_logic.basic.module.ConfiguredRuntimeModule#addDependencies(java.util.Collection,
	// java.lang.Class, java.lang.Class)}.
	// */
	// public void
	// testAddDependenciesCollectionOfClassOfQextendsBasicRuntimeModuleOfQClassOfQClassOfQ()
	// {
	// fail("Not yet implemented");
	// }
	//
	@ServiceDependencies(SimpleManagedClass2.Module.class)
	public static class SimpleManagedClass extends ManagedClass {

		Configuration configUsed;

		Properties poropertiesUsed;

		boolean shutDownCalled;

		public SimpleManagedClass() {
			super();
		}

		public SimpleManagedClass(Properties someProps) {
			poropertiesUsed = someProps;
		}

		public SimpleManagedClass(IterableConfiguration aConfig) {
			super(aConfig);
			
			configUsed = aConfig;
		}

		@Override
		protected void shutDown() {
			shutDownCalled = true;
			super.shutDown();
		}

		public static final class Module extends ConfiguredRuntimeModule<SimpleManagedClass> {

			public static final Module INSTANCE = new Module();

			@Override
			public Class<SimpleManagedClass> getImplementation() {
				return SimpleManagedClass.class;
			}
		}

	}

	// Extension which is configured as implementation class
	public static class ExtendedSimpleManagedClass extends SimpleManagedClass {

		public ExtendedSimpleManagedClass() {
			super();
		}

		public ExtendedSimpleManagedClass(IterableConfiguration aConfig) {
			super(aConfig);
		}

		public ExtendedSimpleManagedClass(Properties someProps) {
			super(someProps);
		}

	}

	public static class SimpleManagedClass2 extends ManagedClass {

		public static final class Module extends ConfiguredRuntimeModule<SimpleManagedClass2> {

			public static final Module INSTANCE = new Module();

			@Override
			public Class<SimpleManagedClass2> getImplementation() {
				return SimpleManagedClass2.class;
			}

			@Override
			protected Class<? extends SimpleManagedClass2> getDefaultImplementationClass() {
				return SimpleManagedClass2.class;
			}
		}
	}

	public static class NonExistingDependencyConfigured extends ManagedClass {

		public static final class Module extends ConfiguredRuntimeModule<NonExistingDependencyConfigured> {

			public static final Module INSTANCE = new Module();

			@Override
			public Class<NonExistingDependencyConfigured> getImplementation() {
				return NonExistingDependencyConfigured.class;
			}

			@Override
			protected Class<? extends NonExistingDependencyConfigured> getDefaultImplementationClass() {
				return NonExistingDependencyConfigured.class;
			}
		}
	}

	static class TestedConfiguredRuntimeModule extends ConfiguredRuntimeModule<SimpleManagedClass> {

		boolean hasConfigurationConstructor;

		public TestedConfiguredRuntimeModule(boolean hasAConfigurationConstructor) {
			this.hasConfigurationConstructor = hasAConfigurationConstructor;
		}

		@Override
		public Class<SimpleManagedClass> getImplementation() {
			return SimpleManagedClass.class;
		}

		@Override
		protected boolean hasConfigurationConstructor() {
			return hasConfigurationConstructor;
		}

		/**
		 * Allow testing.
		 */
		@Override
		public SimpleManagedClass newImplementationInstance(IterableConfiguration aConfig) throws ModuleException,
				ConfigurationException {
			return super.newImplementationInstance(aConfig);
		}

	}

	public static Test suite() {
		return wrap(new CustomPropertiesSetup(TestConfiguredRuntimeModule.class, false));
	}

}
