/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Default {@link TestFactory} implementation that creates a {@link TestSuite}
 * with the {@link TestSuite#TestSuite(Class, String)} constructor for the given
 * test case class.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DefaultTestFactory implements TestFactory {
	
	/**
	 * Singleton {@link DefaultTestFactory}.
	 */
	public static final TestFactory INSTANCE = new DefaultTestFactory();

	private DefaultTestFactory() {
		// Singleton constructor.
	}
	
	@Override
	public Test createSuite(Class testCase, String suiteName) {
		return new TestSuite(testCase, suiteName);
	}
}