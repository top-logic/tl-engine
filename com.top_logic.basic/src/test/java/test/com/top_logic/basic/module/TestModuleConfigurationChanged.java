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
import com.top_logic.basic.module.ConfigurationBaseline;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.RestartException;

/**
 * Test of the life cycle of the {@link BasicRuntimeModule#getConfigurationBaseline() configuration
 * baseline} and {@link BasicRuntimeModule#isConfigurationChanged()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestModuleConfigurationChanged extends AbstractModuleTest {

	/**
	 * A stored configuration held in a field.
	 */
	static class Storage {

		String _value;

		Storage(String value) {
			_value = value;
		}

		ConfigurationBaseline capture() {
			String captured = _value;
			return new ConfigurationBaseline() {
				@Override
				public boolean isChanged() {
					return !captured.equals(_value);
				}

				@Override
				public void restore() {
					_value = captured;
				}
			};
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		F.Module.INSTANCE.discardConfigurationBaseline();
		G.Module.INSTANCE.discardConfigurationBaseline();
	}

	/**
	 * A module is changed only while the stored configuration differs from the recorded baseline.
	 */
	public void testChangedComparesToBaseline() throws Exception {
		F.Module module = F.Module.INSTANCE;
		moduleUtil.startUp(module);
		assertNull(module.getConfigurationBaseline());
		assertFalse(module.isConfigurationChanged());

		Storage storage = new Storage("a");
		change(module, storage, "b");
		assertTrue(module.isConfigurationChanged());

		change(module, storage, "c");
		assertTrue(module.isConfigurationChanged());

		change(module, storage, "a");
		assertFalse("Back to the state the module was started with.", module.isConfigurationChanged());
		assertNotNull(module.getConfigurationBaseline());

		change(module, storage, "d");
		module.getConfigurationBaseline().restore();
		module.discardConfigurationBaseline();
		assertEquals("a", storage._value);
		assertFalse(module.isConfigurationChanged());
	}

	/**
	 * Only the first recording after a start captures a baseline.
	 */
	public void testRecordKeepsFirstBaseline() {
		F.Module module = F.Module.INSTANCE;
		Storage storage = new Storage("a");
		ConfigurationBaseline first = module.recordConfigurationBaseline(storage::capture);
		ConfigurationBaseline second = module.recordConfigurationBaseline(() -> {
			throw new AssertionError("Baseline must not be captured twice.");
		});
		assertSame(first, second);
		assertSame(first, module.getConfigurationBaseline());
	}

	/**
	 * A start drops the baseline, a stop keeps it.
	 */
	public void testStartDropsBaseline() throws ModuleException {
		F.Module module = F.Module.INSTANCE;
		Storage storage = new Storage("a");

		change(module, storage, "b");
		assertTrue("Change is reported while the module is not running.", module.isConfigurationChanged());

		moduleUtil.startUp(module);
		assertNull("Starting applies the stored configuration.", module.getConfigurationBaseline());
		assertFalse(module.isConfigurationChanged());

		change(module, storage, "c");
		moduleUtil.shutDown(module);
		assertTrue("Stopping does not apply the stored configuration.", module.isConfigurationChanged());

		moduleUtil.startUp(module);
		assertFalse(module.isConfigurationChanged());
	}

	/**
	 * A restart drops the baseline of the restarted module and of the restarted dependents.
	 */
	public void testRestartDropsBaseline() throws ModuleException, RestartException {
		moduleUtil.startUp(G.Module.INSTANCE);
		assertTrue(F.Module.INSTANCE.isActive());

		change(F.Module.INSTANCE, new Storage("a"), "b");
		change(G.Module.INSTANCE, new Storage("a"), "b");

		moduleUtil.restart(F.Module.INSTANCE, null);
		assertTrue(G.Module.INSTANCE.isActive());
		assertFalse(F.Module.INSTANCE.isConfigurationChanged());
		assertFalse("Dependent has been restarted, too.", G.Module.INSTANCE.isConfigurationChanged());
	}

	/**
	 * A restart of a dependent does not drop the baseline of a module that is not restarted.
	 */
	public void testRestartKeepsBaselineOfUnaffectedModule() throws ModuleException, RestartException {
		moduleUtil.startUp(G.Module.INSTANCE);

		change(F.Module.INSTANCE, new Storage("a"), "b");

		moduleUtil.restart(G.Module.INSTANCE, null);
		assertTrue("Dependency is not restarted.", F.Module.INSTANCE.isConfigurationChanged());

		moduleUtil.restart(F.Module.INSTANCE, null);
		assertFalse(F.Module.INSTANCE.isConfigurationChanged());
	}

	private static void change(BasicRuntimeModule<?> module, Storage storage, String value) {
		module.recordConfigurationBaseline(storage::capture);
		storage._value = value;
	}

	/**
	 * Tests of this suite.
	 */
	public static Test suite() {
		return wrap(new TestSuite(TestModuleConfigurationChanged.class));
	}

}
