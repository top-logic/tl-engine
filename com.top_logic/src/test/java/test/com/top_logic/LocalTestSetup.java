/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import test.com.top_logic.basic.DecoratedTestSetup;
import test.com.top_logic.basic.TestSetupDecorator;

/**
 * Interface for a {@link TestSetup} with public {@link #setUp()} and
 * {@link #tearDown()} that can be used locally from within test cases.
 * 
 * <p>
 * {@link TestSetup}s implementing this interface can be explicitly triggered
 * from within the {@link TestCase#setUp()} and {@link TestCase#tearDown()}
 * methods to reset the test date before each test case.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocalTestSetup extends DecoratedTestSetup {

	/**
	 * Creates a undecorated {@link LocalTestSetup}.
	 * 
	 * @param test
	 *        The test to wrap.
	 */
	public LocalTestSetup(Test test) {
		this(null, test);
	}

	/**
	 * Creates a decorated {@link LocalTestSetup}.
	 * 
	 * @param decorator
	 *        The {@link TestSetupDecorator} to use.
	 * @param test
	 *        The test to wrap.
	 */
	public LocalTestSetup(TestSetupDecorator decorator, Test test) {
		super(decorator, test);
	}

	/**
	 * Public {@link TestSetup#setUp()}
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Public {@link TestSetup#tearDown()}
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Override
	protected final void doSetUp() throws Exception {
		setUpLocal();
		setUpInner();
	}

	protected void setUpLocal() throws Exception {
		// Override me.
	}

	private void setUpInner() throws Exception {
		if (fTest instanceof LocalTestSetup) {
			((LocalTestSetup) fTest).setUp();
		}
	}
	
	@Override
	protected final void doTearDown() throws Exception {
		tearDownInner();
		tearDownLocal();
	}

	protected void tearDownLocal() throws Exception {
		// Override me.
	}

	private void tearDownInner() throws Exception {
		if (fTest instanceof LocalTestSetup) {
			((LocalTestSetup) fTest).tearDown();
		}
	}

	@Override
	public final void basicRun(TestResult result) {
		if (fTest instanceof LocalTestSetup) {
			((LocalTestSetup) fTest).basicRun(result);
		} else {
			super.basicRun(result);
		}
	}
	
}
