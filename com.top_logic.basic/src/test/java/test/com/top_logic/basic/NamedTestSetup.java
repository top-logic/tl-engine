/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.extensions.TestSetup;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

/**
 * Named variant of {@link TestSetup}.
 * 
 * @see NamedTestDecorator
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class NamedTestSetup extends NamedTestDecorator {

	// Content is taken from junit.extensions.TestSetup

	public NamedTestSetup(Test test) {
		super(test);
	}

	@Override
	public void run(final TestResult result) {
		Protectable p = new Protectable() {
			@Override
			public void protect() throws Exception {
				setUp();
				basicRun(result);
				tearDown();
			}
		};
		result.runProtected(this, p);
	}

	/**
	 * Sets up the fixture. Override to set up additional fixture state.
	 */
	protected void setUp() throws Exception {
	}

	/**
	 * Tears down the fixture. Override to tear down the additional fixture state.
	 */
	protected void tearDown() throws Exception {
	}
}

