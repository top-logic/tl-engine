/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import junit.framework.Test;
import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.module.ModuleUtil;

/**
 * The class {@link AbstractModuleTest} installs a new {@link ModuleUtil}
 * instance to be able to start and shutdown arbitrary modules without side
 * effects for other tests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractModuleTest extends TestCase {

	
	protected ModuleUtil moduleUtil = ModuleUtil.INSTANCE;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		this.moduleUtil.shutDownAll();
		this.moduleUtil = null;
		super.tearDown();
	}

	/**
	 * Wraps into a {@link ModuleUtilTestSetup} that stores the current modules for the test, stops
	 * them and re-start them after all test.
	 */
	protected static Test wrap(Test test) {
		return new ModuleUtilTestSetup(BasicTestSetup.createBasicTestSetup(test));
	}
	
}
