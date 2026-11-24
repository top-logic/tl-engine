/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils.ReflectionException;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleException;

/**
 * The class {@link TestBasicRuntimeModule} tests the base function of an simple
 * {@link BasicRuntimeModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBasicRuntimeModule extends AbstractBasicRuntimeModuleTest<BasicRuntimeModule<?>> {

	@Override
	protected BasicRuntimeModule<?> getModule() {
		return new BasicRuntimeModule<TestedManagedClass>() {

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return NO_DEPENDENCIES;
			}

			@Override
			public Class<TestedManagedClass> getImplementation() {
				return TestedManagedClass.class;
			}

			@Override
			protected TestedManagedClass newImplementationInstance() throws ModuleException {
				return new TestedManagedClass();

			}
		};
	}

	public void testActivate() {
		assertFalse("newly created module must not be started", module.isActive());
		startUp(module);
		assertTrue("started module must be active", module.isActive());

		shutDown(module);
		assertFalse("stopped module must not be active", module.isActive());
	}

	public void testGetImplementationInstance() {
		try {
			module.getImplementationInstance();
			fail("not started module must not give an implementation instance");
		} catch (IllegalStateException ex) {
			// expected
		}

		startUp(module);
		try {
			module.getImplementationInstance();
		} catch (Exception ex) {
			throw BasicTestCase.fail("After starting module no exception must occur", ex);
		}

		shutDown(module);
		try {
			module.getImplementationInstance();
			fail("stopped module must not give an implementation instance");
		} catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testCreatingNewInstance() {
		startUp(module);
		assertTrue(module.isActive());
		Object implInstance = module.getImplementationInstance();

		shutDown(module);
		startUp(module);

		assertNotSame(implInstance, module.getImplementationInstance());
	}

	/**
	 * An {@link Error} thrown while starting the service leaves the module inactive, so that a
	 * later start tries again.
	 */
	public void testErrorOnStartUp() {
		boolean[] fail = { true };
		BasicRuntimeModule<?> failingModule = new BasicRuntimeModule<TestedManagedClass>() {

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return NO_DEPENDENCIES;
			}

			@Override
			public Class<TestedManagedClass> getImplementation() {
				return TestedManagedClass.class;
			}

			@Override
			protected TestedManagedClass newImplementationInstance() throws ModuleException {
				return new TestedManagedClass() {
					@Override
					protected void startUp() {
						super.startUp();
						if (fail[0]) {
							throw new Error("Startup failure for test.");
						}
					}
				};
			}
		};

		try {
			startUp(failingModule);
			fail("Start-up expected to fail.");
		} catch (ReflectionException ex) {
			assertTrue("Unexpected failure: " + ex, ex.getCause().getCause() instanceof Error);
		}
		assertFalse("Module must not be active after a failed start-up.", failingModule.isActive());

		fail[0] = false;
		startUp(failingModule);
		assertTrue("Module must be active after a successful start-up.", failingModule.isActive());
		shutDown(failingModule);
	}

	public static Test suite() {
		return wrap(new TestSuite(TestBasicRuntimeModule.class));
	}

}
