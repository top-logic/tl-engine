/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * The class {@link TestTypedRuntimeModule} contains test for {@link TypedRuntimeModule}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestTypedRuntimeModule extends AbstractModuleTest {

	static class TestedTypedRuntimeModule extends TypedRuntimeModule<TestedManagedClass> {

		@Override
		public Class<TestedManagedClass> getImplementation() {
			return TestedManagedClass.class;
		}
	}

	static class AbstractImplClassModule extends TypedRuntimeModule<AbstractTestedManagedClass> {

		@Override
		public Class<AbstractTestedManagedClass> getImplementation() {
			return AbstractTestedManagedClass.class;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		moduleUtil.startUp(ApplicationConfig.Module.INSTANCE);
	}
	
	public void testAbstractImplementationClass() {
		AbstractImplClassModule module = new AbstractImplClassModule();
		try {
			moduleUtil.startUp(module);
			fail("Module with abstract implementation class without configuration must not be started.");
		} catch (Exception ex) {
			// expected
		}
	}

	public void testDefaultImplementationClass() throws IllegalArgumentException, ModuleException {
		class ModuleWithDefaultClass extends AbstractImplClassModule {

			@Override
			protected Class<? extends AbstractTestedManagedClass> getDefaultImplementationClass() {
				return TestedManagedClass.class;
			}
		}
		ModuleWithDefaultClass module = new ModuleWithDefaultClass();
		moduleUtil.startUp(module);
		assertNotNull(module.getImplementationInstance());
		assertSame(module.getDefaultImplementationClass(), module.getImplementationInstance().getClass());
	}
	
	public void testPresenceOfThreadContext() throws ModuleException {
		class TestedTypedRuntimeModuleWithTreadContext extends TypedRuntimeModule<TestedManagedClassWithThreadContext> {

			@Override
			public Class<TestedManagedClassWithThreadContext> getImplementation() {
				return TestedManagedClassWithThreadContext.class;
			}

		}
		TestedTypedRuntimeModuleWithTreadContext module = new TestedTypedRuntimeModuleWithTreadContext();
		moduleUtil.startUp(module);
		TestedManagedClassWithThreadContext impl = module.getImplementationInstance();
		assertTrue(impl._threadContextOnCreation);
		assertTrue(impl._threadContextOnStartUp);
		moduleUtil.shutDown(module);
		assertTrue(impl._threadContextOnShutDown);

	}

	/**
	 * Tests that Creation via factory method is possible.
	 */
	public void testFactoryMethod() throws IllegalArgumentException, ModuleException {
		TestedTypedRuntimeModule module = new TestedTypedRuntimeModule();
		moduleUtil.startUp(module);
		TestedManagedClass impl = module.getImplementationInstance();
		assertTrue("factory method is not called.", impl._createdViaFactory);
	}

	/**
	 * Tests that {@link ApplicationConfig} is a dependency of a {@link TypedRuntimeModule}
	 */
	public void testDependency() {
		TestedTypedRuntimeModule module = new TestedTypedRuntimeModule();
		assertTrue("Ticket #8465: " + TypedRuntimeModule.class.getName() + " should declare "
			+ ApplicationConfig.Module.class.getName() + " as dependency.",
			module.getDependencies().contains(ApplicationConfig.Module.class));

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTypedRuntimeModule}.
	 */
	public static Test suite() {
		return wrap(new TestSuite(TestTypedRuntimeModule.class));
	}

}
