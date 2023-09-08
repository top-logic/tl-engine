/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.module.BasicRuntimeModule;

/**
 * The class {@link AbstractBasicRuntimeModuleTest} is a super class for tests of
 * {@link BasicRuntimeModule}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractBasicRuntimeModuleTest<M extends BasicRuntimeModule<?>> extends AbstractModuleTest {

	protected M module;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.module = getModule();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (module.isActive()) {
			shutDown(module);
		}
		module = null;
	}

	/**
	 * returns the {@link BasicRuntimeModule} under Test. must not be <code>null</code>.
	 */
	protected abstract M getModule();
	
	public void testImplementationInstanceNotNull() {
		startUp(module);
		
		assertNotNull("Module must not create null implementation instance", module.getImplementationInstance());
	}

	protected static final void startUp(BasicRuntimeModule<?> module) {
		ReflectionUtils.executeMethod(module, "startUp", null, null);
	}

	protected static final void shutDown(BasicRuntimeModule<?> module) {
		ReflectionUtils.executeMethod(module, "shutDown", null, null);
	}

}
