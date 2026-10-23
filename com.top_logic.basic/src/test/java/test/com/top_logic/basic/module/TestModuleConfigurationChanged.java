/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.TestModuleUtil.F;
import test.com.top_logic.basic.module.TestModuleUtil.G;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.RestartException;

/**
 * Test of the life cycle of {@link BasicRuntimeModule#isConfigurationChanged()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestModuleConfigurationChanged extends AbstractModuleTest {

	/**
	 * A module is unchanged until it is marked, and a start clears the mark.
	 */
	public void testStartClearsMark() throws ModuleException {
		F.Module module = F.Module.INSTANCE;
		assertFalse(module.isConfigurationChanged());

		module.markConfigurationChanged();
		assertTrue("Mark is kept while the module is not running.", module.isConfigurationChanged());

		moduleUtil.startUp(module);
		assertFalse("Starting applies the stored configuration.", module.isConfigurationChanged());

		module.markConfigurationChanged();
		assertTrue("Mark is set on a running module.", module.isConfigurationChanged());

		moduleUtil.shutDown(module);
		assertTrue("Stopping does not apply the stored configuration.", module.isConfigurationChanged());

		moduleUtil.startUp(module);
		assertFalse(module.isConfigurationChanged());
	}

	/**
	 * A restart clears the mark of the restarted module and of the restarted dependents.
	 */
	public void testRestartClearsMark() throws ModuleException, RestartException {
		moduleUtil.startUp(G.Module.INSTANCE);
		assertTrue(F.Module.INSTANCE.isActive());

		F.Module.INSTANCE.markConfigurationChanged();
		G.Module.INSTANCE.markConfigurationChanged();

		moduleUtil.restart(F.Module.INSTANCE, null);
		assertTrue(G.Module.INSTANCE.isActive());
		assertFalse(F.Module.INSTANCE.isConfigurationChanged());
		assertFalse("Dependent has been restarted, too.", G.Module.INSTANCE.isConfigurationChanged());
	}

	/**
	 * A restart of a dependency does not clear the mark of a module that is not restarted.
	 */
	public void testRestartKeepsMarkOfUnaffectedModule() throws ModuleException, RestartException {
		moduleUtil.startUp(G.Module.INSTANCE);

		F.Module.INSTANCE.markConfigurationChanged();

		moduleUtil.restart(G.Module.INSTANCE, null);
		assertTrue("Dependency is not restarted.", F.Module.INSTANCE.isConfigurationChanged());

		moduleUtil.restart(F.Module.INSTANCE, null);
		assertFalse(F.Module.INSTANCE.isConfigurationChanged());
	}

	/**
	 * Tests of this suite.
	 */
	public static Test suite() {
		return wrap(new TestSuite(TestModuleConfigurationChanged.class));
	}

}
