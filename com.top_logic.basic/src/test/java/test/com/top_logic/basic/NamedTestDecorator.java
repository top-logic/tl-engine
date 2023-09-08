/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Named variant of {@link TestDecorator} based on a {@link TestSuite}.
 * 
 * <p>
 * This {@link TestDecorator} can be used to have concrete name in Eclipse JUnit view. If none is
 * set the name of the class is the name.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class NamedTestDecorator extends TestSuite {

	// content is taken from junit.extensions.TestDecorator

	protected Test fTest;

	public NamedTestDecorator(Test test) {
		fTest = test;
		setName(getClass().getName());
	}

	/**
	 * The basic run behaviour.
	 */
	public void basicRun(TestResult result) {
		fTest.run(result);
	}

	@Override
	public int countTestCases() {
		return fTest.countTestCases();
	}

	@Override
	public void run(TestResult result) {
		basicRun(result);
	}

	@Override
	public String toString() {
		return fTest.toString();
	}

	public Test getTest() {
		return fTest;
	}

	@Override
	public void setName(String name) {
		super.setName(BasicTestCase.sanitizeForEclipse(name));
	}

	/**
	 * Adds a test to the suite.
	 */
	@Override
	public void addTest(Test test) {
		throw new IllegalStateException("This testsuite has exactly one inner test.");
	}

	@Override
	public Test testAt(int index) {
		switch (index) {
			case 0:
				return fTest;
			default:
				throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public int testCount() {
		return 1;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration tests() {
		return new SingletonEnumeration(fTest);
	}

	private static final class SingletonEnumeration<T> implements Enumeration<T> {

		private boolean _hasMoreElements;

		private final T _element;

		public SingletonEnumeration(T element) {
			_element = element;
			_hasMoreElements = true;
		}

		@Override
		public boolean hasMoreElements() {
			return _hasMoreElements;
		}

		@Override
		public T nextElement() {
			if (!hasMoreElements()) {
				throw new NoSuchElementException();
			}
			_hasMoreElements = false;
			return _element;
		}
	}

}

