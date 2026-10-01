/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.List;
import java.util.function.Function;

import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.NamedTestDecorator;
import test.com.top_logic.basic.TestUtils;

/**
 * Reduces a test tree in place according to a {@link Decision} for each node.
 *
 * <p>
 * The pruner visits the tree top-down and asks its classifier for a {@link Decision} for each
 * visited test. Suites and decorators (e.g. setups) that lose all their tests are removed from
 * their enclosing suite. The root itself is never removed, it may become empty.
 * </p>
 *
 * @see ShardSelection
 * @see DBSelection
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class TestPruner {

	/**
	 * What the {@link TestPruner} does with a test.
	 */
	public enum Decision {

		/**
		 * The test is kept with its complete subtree.
		 */
		KEEP,

		/**
		 * The test is removed with its complete subtree.
		 */
		DROP,

		/**
		 * The decision is taken for the inner tests of the test: the inner test of a decorator or
		 * the children of a suite. The test is kept, if any of its tests is kept. A test without
		 * inner tests is removed.
		 */
		DESCEND,

		/**
		 * The test is a {@link TestSuite} that is replaced by its children in its enclosing
		 * {@link TestSuite}. Without an enclosing suite (at the root or below a decorator), the
		 * test is kept as with {@link #KEEP}.
		 */
		INLINE;
	}

	private final Function<? super Test, Decision> _classifier;

	/**
	 * Creates a {@link TestPruner}.
	 *
	 * @param classifier
	 *        Decides for each visited test, what to do with it.
	 */
	public TestPruner(Function<? super Test, Decision> classifier) {
		_classifier = classifier;
	}

	/**
	 * Reduces the given test in place.
	 *
	 * @return Whether the given test must be kept in its parent.
	 */
	public boolean prune(Test test) {
		return apply(test, _classifier.apply(test));
	}

	private boolean apply(Test test, Decision decision) {
		switch (decision) {
			case KEEP:
			case INLINE:
				return true;
			case DROP:
				return false;
			case DESCEND:
				return descend(test);
		}
		throw new IllegalArgumentException("Unknown decision: " + decision);
	}

	private boolean descend(Test test) {
		Test inner = innerTest(test);
		if (inner != null) {
			return prune(inner);
		}
		if (test instanceof TestSuite) {
			TestSuite suite = (TestSuite) test;
			for (Test child : TestUtils.removeTestsFromSuite(suite)) {
				Decision decision = _classifier.apply(child);
				if (decision == Decision.INLINE && child instanceof TestSuite) {
					for (Test content : TestUtils.removeTestsFromSuite((TestSuite) child)) {
						suite.addTest(content);
					}
				} else if (apply(child, decision)) {
					suite.addTest(child);
				}
			}
			return suite.testCount() > 0;
		}
		return false;
	}

	/**
	 * Whether the given test has inner tests, which a {@link Decision#DESCEND} would visit.
	 */
	public static boolean hasInnerTests(Test test) {
		if (innerTest(test) != null) {
			return true;
		}
		return test instanceof TestSuite && ((TestSuite) test).testCount() > 0;
	}

	/**
	 * The direct inner tests of the given test: the inner test of a decorator, the children of a
	 * suite, or nothing.
	 */
	public static List<Test> innerTests(Test test) {
		Test inner = innerTest(test);
		if (inner != null) {
			return List.of(inner);
		}
		if (test instanceof TestSuite) {
			TestSuite suite = (TestSuite) test;
			Test[] children = new Test[suite.testCount()];
			for (int n = 0; n < children.length; n++) {
				children[n] = suite.testAt(n);
			}
			return List.of(children);
		}
		return List.of();
	}

	/**
	 * The single test wrapped by the given decorator test, or <code>null</code> if the given test
	 * is no decorator.
	 *
	 * @see SingleTestWrapper
	 */
	public static Test innerTest(Test test) {
		if (test instanceof SingleTestWrapper) {
			return ((SingleTestWrapper) test).getWrappedTest();
		}
		if (test instanceof TestDecorator) {
			return ((TestDecorator) test).getTest();
		}
		if (test instanceof NamedTestDecorator) {
			return ((NamedTestDecorator) test).getTest();
		}
		return null;
	}

}
