/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Objects.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import junit.extensions.TestDecorator;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.Filter;

/**
 * The class {@link TestUtils} is a util class for tests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TestUtils {

	/**
	 * A marker interface for {@link TestUtils#rearrange(Test)}. Every instance will be rearranged
	 * to the end of its {@link TestSuite}.
	 */
	public static interface RearrangeToEnd {
		// Pure marker Interface
	}

	private static final int MAX_DISPLAYED_TESTS = 3;

	/**
	 * Removes all Tests from the given test, except the tests matching the given filter and all
	 * "sub-tests", i.e. if e.g. the filter matches a {@link TestSuite}, all contained tests are
	 * also not removed.
	 */
	public static void filter(Test test, Filter<Test> filter) {
		boolean anyMatch = removeAllNotMatching(test, filter);
		if (!anyMatch) {
			throw new RuntimeException("No matching Test");
		}
	}

	private static boolean removeAllNotMatching(Test test, Filter<Test> matchingTests) {
		if (matchingTests.accept(test)) {
			return true;
		}
		if (test instanceof TestDecorator) {
			return removeAllNotMatching(((TestDecorator) test).getTest(), matchingTests);
		}
		if (test instanceof NamedTestDecorator) {
			return removeAllNotMatching(((NamedTestDecorator) test).getTest(), matchingTests);
		}
		if (test instanceof TestSuite) {
			TestSuite suite = (TestSuite) test;
			ArrayList<Test> tmp = removeTestsFromSuite(suite);
			boolean anyMatch = false;
			for (Test t : tmp) {
				if (removeAllNotMatching(t, matchingTests)) {
					anyMatch = true;
					suite.addTest(t);
				}
			}
			return anyMatch;
		}
		return false;
	}

	public static void rearrange(Test test) {
		rearrangeInternal(test);
	}

	private static void rearrangeInternal(Test test) {
		if (test instanceof RearrangableTestSetup) {
			RearrangableTestSetup rearrangable = (RearrangableTestSetup) test;
			final Test innerTest = rearrangable.getTest();
			if (isTestSuite(innerTest)) {
				final Enumeration innerSuiteTests = ((TestSuite) innerTest).tests();
				if (innerSuiteTests.hasMoreElements()) {
					final Test firstSuiteChild = (Test) innerSuiteTests.nextElement();
					if (innerSuiteTests.hasMoreElements()) {
						rearrangeInternal(innerTest);
					} else {
						rearrangable.setTest(firstSuiteChild);
						rearrangeInternal(rearrangable);
					}
				} else {
					// No innerTests;
				}
			} else {
				rearrangeInternal(innerTest);
			}
			return;
		}
		if (test instanceof TestDecorator) {
			rearrangeInternal(((TestDecorator) test).getTest());
			return;
		}
		if (test instanceof NamedTestDecorator) {
			rearrangeInternal(((NamedTestDecorator) test).getTest());
			return;
		}
		if (isIsolatingSuite(test)) {
			return;
		}
		if (isTestSuite(test)) {
			TestSuite suite = (TestSuite) test;
			ArrayList<Test> tmp = removeTestsFromSuite(suite);
			rearrangeList(new FlatIterator(tmp), suite);
		}
	}

	private static boolean isIsolatingSuite(Test test) {
		return test instanceof IsolatingTestSuite;
	}

	/**
	 * Wraps a {@link TestSetup} around the given {@link Test} that isolates inner
	 * {@link TestSuite}s and {@link TestSetup}s from the rearrange.
	 */
	public static Test createIsolatingSuite(Test test) {
		IsolatingTestSuite isolatingSuite = new IsolatingTestSuite();
		isolatingSuite.addTest(test);
		return isolatingSuite;
	}

	private static class IsolatingTestSuite extends TestSuite {

		IsolatingTestSuite() {
			super("Isolating Suite");
		}

	}

	private static ArrayList<Test> removeTestsFromSuite(TestSuite suite) {
		try {
			final Field declaredField = TestSuite.class.getDeclaredField("fTests");
			declaredField.setAccessible(true);
			final Collection<Test> innerTests = (Collection<Test>) declaredField.get(suite);
			final ArrayList<Test> tmp = new ArrayList<>(innerTests);
			innerTests.clear();
			return tmp;
		} catch (SecurityException ex) {
			throw BasicTestCase.fail("Unable to get Tests from test suite '" + suite.getName() + "'", ex);
		} catch (NoSuchFieldException ex) {
			throw BasicTestCase.fail("Unable to get Tests from test suite '" + suite.getName() + "'", ex);
		} catch (IllegalArgumentException ex) {
			throw BasicTestCase.fail("Unable to get Tests from test suite '" + suite.getName() + "'", ex);
		} catch (IllegalAccessException ex) {
			throw BasicTestCase.fail("Unable to get Tests from test suite '" + suite.getName() + "'", ex);
		}
	}

	private static int rearrangeList(Iterator<Test> tests, TestSuite targetSuite) {
		List<Test> rearrangedTests = rearrangeInternal(tests);
		rearrangeSpecialCasesToEnd(rearrangedTests);

		for (Test test : rearrangedTests) {
			targetSuite.addTest(test);
		}
		return targetSuite.testCount();
	}

	private static void rearrangeSpecialCasesToEnd(List<Test> rearrangedTests) {
		List<Test> specialCases = findSpecialCases(rearrangedTests);
		moveSpecialCasesToEnd(rearrangedTests, specialCases);
	}

	private static List<Test> findSpecialCases(List<Test> rearrangedTests) {
		List<Test> specialCases = new ArrayList<>();
		for (Test test : rearrangedTests) {
			if (test instanceof RearrangeToEnd) {
				specialCases.add(test);
			}
		}
		return specialCases;
	}

	private static void moveSpecialCasesToEnd(List<Test> rearrangedTests, List<Test> specialCases) {
		rearrangedTests.removeAll(specialCases);
		rearrangedTests.addAll(specialCases);
	}

	private static List<Test> rearrangeInternal(Iterator<Test> tests) {
		List<Test> rearrangedTests = new ArrayList<>();
		/*
		 * variable which stores for each key of an rearrangable test setup in
		 * the source tests a list of inner tests of rearrangable test setups
		 * with the same key
		 */
		Map<Object, List<Test>> innerTestsByKey = new LinkedHashMap<>();

		while (tests.hasNext()) {
			Test currentTest = tests.next();
			final Object configKey;
			if (currentTest instanceof RearrangableTestSetup) {
				final RearrangableTestSetup rearrangable = (RearrangableTestSetup) currentTest;
				configKey = rearrangable.configKey();
				if (innerTestsByKey.containsKey(configKey)) {
					innerTestsByKey.get(configKey).add(rearrangable.getTest());
				} else {
					final ArrayList<Test> testsForKey = new ArrayList<>();
					innerTestsByKey.put(configKey, testsForKey);
					// put the whole test in to have an instance of
					// RearrangableTestSetup to update
					testsForKey.add(currentTest);
					testsForKey.add(rearrangable.getTest());
				}
			} else {
				// test is not rearrangable but maybe potential inner tests
				rearrangeInternal(currentTest);

				rearrangedTests.add(currentTest);
			}
		}

		for (List<Test> innerTests : innerTestsByKey.values()) {
			// Remove the reference test
			final RearrangableTestSetup currentRearrangable = (RearrangableTestSetup) innerTests.remove(0);

			if (innerTests.size() == 1) {
				// There is just one test with the same test setup
				rearrangeInternal(currentRearrangable);
			} else {
				final TestSuite newInnerTestSuite = new TestSuite();
				// rearrange all inner tests of the same rearrangeble test setup
				final int addedTests = rearrangeList(new FlatIterator(innerTests), newInnerTestSuite);

				if (addedTests == 1) {
					// There is just one test. No need to put into suite
					final Test soleTest = (Test) newInnerTestSuite.tests().nextElement();
					currentRearrangable.setTest(soleTest);
				} else {
					currentRearrangable.setTest(newInnerTestSuite);
				}
			}
			rearrangedTests.add(currentRearrangable);
		}
		return rearrangedTests;
	}

	private static boolean isTestSuite(Test o) {
		return TestSuite.class.equals(o.getClass());
	}

	/**
	 * The {@link FlatIterator} is an iterator over a {@link Collection} of
	 * {@link Test tests} which removes all {@link TestSuite suites}, i.e. if
	 * the next possible result is a {@link TestSuite}, the contents of the test
	 * suite are reported.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class FlatIterator implements Iterator<Test> {

		private final Iterator<Test> tests;
		private final Stack<Enumeration<Test>> stack = new Stack<>();

		private Test currentTest = null;

		public FlatIterator(Collection<Test> tests) {
			this.tests = tests.iterator();
			computeNext();
		}

		/**
		 * Computes the next {@link Test} to be returned by {@link #next()}.
		 */
		private void computeNext() {
			if (!stack.isEmpty()) {
				if (stack.peek().hasMoreElements()) {
					handleTest(stack.peek().nextElement());
				} else {
					stack.pop();
					computeNext();
				}
			} else {
				if (tests.hasNext()) {
					handleTest(tests.next());
				} else {
					currentTest = null;
				}
			}
		}

		private void handleTest(Test possibleResult) {
			if (isTestSuite(possibleResult)) {
				stack.push(((TestSuite) possibleResult).tests());
				computeNext();
			} else {
				currentTest = possibleResult;
			}
		}

		@Override
		public boolean hasNext() {
			return currentTest != null;
		}

		@Override
		public Test next() {
			if (currentTest == null) {
				throw new NoSuchElementException();
			}
			Test result = currentTest;
			computeNext();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public static String getTestName(Test test) {
		String testName;
		if (test instanceof TestSuite) {
			testName = ((TestSuite) test).getName();
		} else {
			testName = test.getClass().getName();
		}
		return testName;
	}

	public static String computeTestName(Test test) {
		if (test instanceof TestCase) {
			return "Test case: " + test.getClass().getName() + "." + ((TestCase) test).getName();
		} else if (test instanceof TestDecorator) {
			return computeTestName(((TestDecorator) test).getTest());
		} else if (test instanceof NamedTestDecorator) {
			return computeTestName(((NamedTestDecorator) test).getTest());
		} else if (test instanceof TestSuite) {
			if(((TestSuite) test).getName() != null) {
				return "Test suite: " + ((TestSuite) test).getName();
			}
			StringBuilder result = new StringBuilder("Test suite:");
			Enumeration tests = ((TestSuite) test).tests();
			
			if (tests.hasMoreElements()) {
				FlatIterator iterator = new FlatIterator(Collections.list(tests));
				for(int i = 0; iterator.hasNext() && i < MAX_DISPLAYED_TESTS; i++) {
					Test currentTest = iterator.next();
					result.append(" [");
					result.append(computeTestName(currentTest));
					result.append("]");
				}
				
				if(iterator.hasNext()) {
					int allTests = MAX_DISPLAYED_TESTS;
					do {
						allTests++;
						iterator.next();
					} while (iterator.hasNext());
					result.append(" ... (First " + MAX_DISPLAYED_TESTS + " of the total " + allTests
						+ " tests displayed only)");
				}
			} else {
				result.append(" No Tests defined for Test suite");
			}
			return result.toString();
		} else {
			return "Class name: " + test.getClass().getName();
		}
	}

	/**
	 * Returns a test which is not merged within other tests, i.e. if the given
	 * test uses a test setup A and a different test also uses a test setup A it
	 * is guaranteed that {@link #rearrange(Test)} will not use the same setup
	 * for both tests.
	 * 
	 * @param innerTest
	 *        the test which must not be merged
	 */
	public static Test doNotMerge(Test innerTest) {
		return new SeparateTestSetup(innerTest);
	}

	/**
	 * A separate setup ensures that the inner test is not merged. It is actually a simple
	 * {@link TestSetup} but has a specific class name (e.g. in junit view it is better to
	 * identify).
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class SeparateTestSetup extends TestSetup {

		public SeparateTestSetup(Test test) {
			super(test);
		}
	}

	/**
	 * Tries to enrich the name of the given test with the given additionalInformation.
	 * 
	 * @return The given test case, (for convenience).
	 */
	public static <T extends Test> T tryEnrichTestnames(T test, String additionalInformation) {
		if (test instanceof BasicTestCase) {
			((BasicTestCase)test).appendDiscriminatingNameSuffix(" with " + additionalInformation);
		} else if (test instanceof TestDecorator) {
			tryEnrichTestnames(((TestDecorator) test).getTest(), additionalInformation);
		} else if (test instanceof NamedTestDecorator) {
			tryEnrichTestnames(((NamedTestDecorator) test).getTest(), additionalInformation);
		} else if (test instanceof TestSuite) {
			Enumeration<?> tests = ((TestSuite) test).tests();
			while (tests.hasMoreElements()) {
				tryEnrichTestnames((Test)tests.nextElement(), additionalInformation);
			}
		}
		return test;
	}

	/**
	 * Creates a {@link TestSuite} with the given name and the given {@link Test}s.
	 * 
	 * @param suiteName
	 *        The name of the suite.
	 * @param tests
	 *        Is not allowed to be or contain null.
	 * @return Never null.
	 */
	public static Test createSuite(String suiteName, Collection<? extends Test> tests) {
		TestSuite suite = new TestSuite(requireNonNull(suiteName));
		return addAll(suite, tests);
	}

	/**
	 * Add all the given {@link Test}s to the given {@link TestSuite}.
	 * 
	 * @param suite
	 *        Is not allowed to be null.
	 * @param tests
	 *        Is allowed to be null, which is interpreted as an empty {@link Collection}. Is not
	 *        allowed to contain null.
	 * @return The given {@link TestSuite}, for convenience.
	 */
	public static TestSuite addAll(TestSuite suite, Collection<? extends Test> tests) {
		requireNonNull(suite); // Always fail if "suite" is null, not just if "tests" is non-empty.
		for (Test test : nonNull(tests)) {
			suite.addTest(requireNonNull(test));
		}
		return suite;
	}

}
