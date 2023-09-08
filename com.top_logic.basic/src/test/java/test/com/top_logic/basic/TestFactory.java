/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;

/**
 * Factory for test suites.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TestFactory {

	/**
	 * Creates a test with the given name for the given test class.
	 * 
	 * @param testCase
	 *        The test case class.
	 * @param suiteName
	 *        the that should be given to the created test.
	 * 
	 * @return a test case
	 */
	Test createSuite(Class<? extends Test> testCase, String suiteName);

}
