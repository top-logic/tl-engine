/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Base class for tests that can be named and initialized explicitly.
 * 
 * <p>
 * Tests conforming to the JUnit method naming convention are constructed be the
 * {@link TestSuite}. There is no way parameterize them upon construction. A
 * {@link GenericTest} can be constructed explicitly with its constructor and
 * must implement the {@link #executeTest()} method.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericTest extends TestCase {
	public GenericTest(String name) {
		super(name);
	}

	@Override
	protected final void runTest() throws Throwable {
		executeTest();
	}

	/**
	 * Implementation of the actual test code.
	 * 
	 * @throws Throwable
	 *         If the test terminates abnormally.
	 */
	protected abstract void executeTest() throws Throwable;
	
}