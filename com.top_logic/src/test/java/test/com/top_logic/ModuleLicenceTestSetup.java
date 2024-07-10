/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Settings;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.InitialTableSetup;

/**
 * {@link ModuleTestSetup} that also sets up a testing license.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModuleLicenceTestSetup extends ModuleTestSetup {
	private static MutableInteger setupCnt = new MutableInteger();

	private static boolean _tableSetup = false;

	private ModuleLicenceTestSetup(Test test) {
		super(test, setupCnt);
	}

	/**
	 * Activate automatic table setup and migration during test.
	 */
	public static void enableTableSetup() {
		_tableSetup = true;
	}
    
    @Override
    protected void doSetUp() throws Exception {
    	super.doSetUp();
    	
		// Deactivate automatic table setup during tests.
		System.setProperty(InitialTableSetup.NO_DATABASE_TABLE_SETUP, String.valueOf(!_tableSetup));
    }

    public static Test setupModule(Class testClass) {
		return setupModule(new TestSuite(testClass));
	}

	public static Test setupModule(Test test) {
		return new ModuleLicenceTestSetup(wrap(test));
	}

	public static Test setupModuleWithoutAdditionalServices(Test test) {
		return new ModuleLicenceTestSetup(test);
	}
	
	/**
	 * Wraps the given test with default modules that are always needed.
	 */
	public static Test wrap(Test test) {
		return ServiceTestSetup.createSetup(test,
			ResourcesModule.Module.INSTANCE,
			ThemeFactory.Module.INSTANCE,
			Settings.Module.INSTANCE,
			FormatterService.Module.INSTANCE);
	}
    
}