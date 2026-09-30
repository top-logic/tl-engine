/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.NestableTestSetup;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;

/**
 * {@link NestableTestSetup} that runs its test in a {@link ModuleUtil#isRunning() running} module
 * system.
 *
 * <p>
 * A test does not boot an application, so no application services are
 * {@link ModuleUtil#startApplicationServices(Collection) started}, the module system is not
 * running, and actions waiting for it, e.g. the dispatch of tasks in the scheduler, do not run. A
 * test depending on such an action wraps itself into this setup. The setup declares the services
 * that are active at its setup as the {@link ModuleUtil#setApplicationServices(Collection)
 * application services}, and restores the former application services afterwards.
 * </p>
 */
public class RunningModuleSystemSetup extends NestableTestSetup {

	private static final MutableInteger SETUP_CNT = new MutableInteger();

	private Set<BasicRuntimeModule<?>> _formerServices;

	/**
	 * Creates a {@link RunningModuleSystemSetup}.
	 *
	 * @param test
	 *        The test to run in the running module system.
	 */
	public RunningModuleSystemSetup(Test test) {
		super(test, SETUP_CNT);
	}

	/**
	 * Creates a {@link RunningModuleSystemSetup} for all tests of the given class.
	 */
	public static Test setup(Class<? extends Test> testClass) {
		return new RunningModuleSystemSetup(new TestSuite(testClass));
	}

	@Override
	protected void doSetUp() throws Exception {
		ModuleUtil moduleUtil = ModuleUtil.INSTANCE;
		_formerServices = moduleUtil.getApplicationServices();
		moduleUtil.setApplicationServices(new ArrayList<>(moduleUtil.getActiveModules()));
	}

	@Override
	protected void doTearDown() throws Exception {
		ModuleUtil.INSTANCE.setApplicationServices(_formerServices);
		_formerServices = null;
	}

}
