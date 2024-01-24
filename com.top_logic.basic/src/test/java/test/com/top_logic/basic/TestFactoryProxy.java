/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Proxy for a TestFactory.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestFactoryProxy implements TestFactory {

	private final TestFactory _innerFactory;

	/**
	 * Creates a new {@link TestFactoryProxy}.
	 */
	public TestFactoryProxy(TestFactory innerFactory) {
		_innerFactory = innerFactory;
	}

	@Override
	public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
		return _innerFactory.createSuite(testCase, suiteName);
	}

}

