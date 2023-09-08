/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.col.MutableInteger;

/**
 * Test setup that automatically detects configuration through the multiloader properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModuleTestSetup extends RearrangableTestSetup {

	private static MutableInteger setupCnt = new MutableInteger();

	protected ModuleTestSetup(Test test, MutableInteger setupCnt) {
		super(test, setupCnt);
	}
	
	@Override
	protected void doSetUp() throws Exception {
		ConfigLoaderTestUtil.INSTANCE.loadConfig();
	}

	@Override
	protected void doTearDown() throws Exception {
		ConfigLoaderTestUtil.INSTANCE.unloadConfig();
	}

	public static Test setupModule(Class<? extends Test> testClass) {
		return setupModule(new TestSuite(testClass));
	}
	
	public static Test setupModule(Test test) {
		return new ModuleTestSetup(test, setupCnt);
	}

}
