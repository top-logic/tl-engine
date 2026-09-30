/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.Enumeration;

import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.NamedTestDecorator;

/**
 * Group of {@link ScriptedTestMarker scripted tests} that must run together in one JVM, because the
 * scripts depend on their order and on the application state they share.
 *
 * <p>
 * A unit is the granularity in which a {@link ShardSelection} distributes scripted tests among
 * shards. A unit is identified by a {@link #getKey() key} that is the same in every JVM (a
 * module-relative script directory or a test class name).
 * </p>
 *
 * <p>
 * A unit is a transient grouping node: {@link ShardSelection#apply(Test)} replaces each selected
 * unit by its contents in the enclosing {@link TestSuite}, so that the executed test tree has the
 * same structure as without the grouping.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptedTestUnit extends TestSuite {

	private final String _key;

	/**
	 * Creates a {@link ScriptedTestUnit}.
	 *
	 * @param key
	 *        See {@link #getKey()}.
	 */
	public ScriptedTestUnit(String key) {
		super(key);
		_key = key;
	}

	/**
	 * Identifier of this unit that is stable across JVMs.
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * Wraps the given test into a {@link ScriptedTestUnit}, if it contains a
	 * {@link ScriptedTestMarker scripted test}.
	 *
	 * @param key
	 *        See {@link #getKey()}.
	 * @param test
	 *        The test to group.
	 * @return A {@link ScriptedTestUnit} containing the given test, or the given test itself, if it
	 *         does not contain scripted tests.
	 */
	public static Test markIfScripted(String key, Test test) {
		if (!containsScripted(test)) {
			return test;
		}
		ScriptedTestUnit unit = new ScriptedTestUnit(key);
		unit.addTest(test);
		return unit;
	}

	/**
	 * Whether the given test tree contains a {@link ScriptedTestMarker scripted test}.
	 */
	public static boolean containsScripted(Test test) {
		if (test instanceof ScriptedTestMarker) {
			return true;
		}
		Test inner = innerTest(test);
		if (inner != null) {
			return containsScripted(inner);
		}
		if (test instanceof TestSuite) {
			for (Enumeration<Test> it = ((TestSuite) test).tests(); it.hasMoreElements();) {
				if (containsScripted(it.nextElement())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The single test wrapped by the given decorator test, or <code>null</code> if the given test
	 * is no decorator.
	 * 
	 * @see SingleTestWrapper
	 */
	static Test innerTest(Test test) {
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
