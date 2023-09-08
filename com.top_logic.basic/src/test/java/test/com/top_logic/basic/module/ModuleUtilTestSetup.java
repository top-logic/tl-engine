/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import junit.extensions.TestSetup;
import junit.framework.Test;

import test.com.top_logic.basic.RearrangableTestSetup;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;

/**
 * {@link TestSetup} for {@link AbstractModuleTest}.
 * 
 * <p>
 * This setup caches all active modules, stop them and restart them after all inner tests has been
 * finished.
 * </p>
 * 
 * @see AbstractModuleTest
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModuleUtilTestSetup extends RearrangableTestSetup {

	private static MutableInteger _setupCnt = new MutableInteger();

	private BasicRuntimeModule<?>[] _activeModules;

	/**
	 * Creates a new {@link ModuleUtilTestSetup}.
	 */
	public ModuleUtilTestSetup(Test test) {
		super(test, _setupCnt);
	}

	@Override
	protected void doSetUp() throws Exception {
		_activeModules = ModuleUtil.INSTANCE.getActiveModules().toArray(BasicRuntimeModule.NO_MODULES);
		ModuleUtil.INSTANCE.shutDownAll();
	}

	@Override
	protected void doTearDown() throws Exception {
		for (BasicRuntimeModule<?> module : _activeModules) {
			ModuleUtil.INSTANCE.startUp(module);
		}
	}

}

