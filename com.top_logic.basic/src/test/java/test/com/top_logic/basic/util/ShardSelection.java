/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.TestUtils;

/**
 * Selection of the {@link ScriptedTestMarker scripted tests} that a test run executes, configured
 * by the system property {@link #PROPERTY}.
 *
 * <p>
 * Values:
 * </p>
 * <dl>
 * <dt>{@value #ALL_VALUE} (default, also when the property is not set)</dt>
 * <dd>All tests run.</dd>
 * <dt>{@value #NONE_VALUE}</dt>
 * <dd>No scripted test runs, all other tests run.</dd>
 * <dt><code>i</code>{@value #SHARD_SEPARATOR}<code>n</code> (e.g. <code>2/4</code>)</dt>
 * <dd>The run is shard <code>i</code> (1-based) of <code>n</code> shards that together execute all
 * tests: Shard <code>i</code> runs the {@link ScriptedTestUnit}s assigned to it. Additionally,
 * shard 1 runs all tests that are not scripted (module independent tests and the module's
 * non-scripted tests). Shards 2 to <code>n</code> run nothing but their scripted units.</dd>
 * </dl>
 *
 * <p>
 * Scripted tests are distributed in {@link ScriptedTestUnit units}: the scripts directly contained
 * in one script directory, or all scripted tests of one test class. A unit is never split, because
 * its scripts depend on their order and on shared application state.
 * </p>
 *
 * <p>
 * Assignment rule (see {@link #distribute(List, Function, ToIntFunction, int)}): the units are
 * sorted by decreasing weight (number of test cases) and, for equal weight, by increasing
 * {@link ScriptedTestUnit#getKey() key}. In this order, each unit is assigned to the shard with the
 * currently smallest total weight (the shard with the lowest number on a tie). The assignment
 * depends only on the keys and weights of all units, so every shard JVM computes the same
 * assignment.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ShardSelection {

	/**
	 * Name of the system property selecting the scripted tests to run.
	 */
	public static final String PROPERTY = "TestAll.scripted";

	/**
	 * Value of {@link #PROPERTY} running all tests.
	 */
	public static final String ALL_VALUE = "all";

	/**
	 * Value of {@link #PROPERTY} running no scripted tests.
	 */
	public static final String NONE_VALUE = "none";

	/**
	 * Separator between shard number and shard count in a value of {@link #PROPERTY}.
	 */
	public static final String SHARD_SEPARATOR = "/";

	/**
	 * Selection running all tests.
	 */
	public static final ShardSelection ALL = new ShardSelection(0, 0);

	/**
	 * Selection running no scripted tests but all others.
	 */
	public static final ShardSelection NONE = new ShardSelection(0, -1);

	/**
	 * 1-based shard number, 0 for {@link #ALL} and {@link #NONE}.
	 */
	private final int _shard;

	/**
	 * Number of shards, 0 for {@link #ALL}, -1 for {@link #NONE}.
	 */
	private final int _count;

	private ShardSelection(int shard, int count) {
		_shard = shard;
		_count = count;
	}

	/**
	 * Creates the selection of shard <code>shard</code> of <code>count</code> shards.
	 *
	 * @param shard
	 *        The 1-based shard number.
	 * @param count
	 *        The number of shards.
	 */
	public static ShardSelection shard(int shard, int count) {
		if (count < 1 || shard < 1 || shard > count) {
			throw new IllegalArgumentException(
				"Invalid shard " + shard + SHARD_SEPARATOR + count + ", expected 1 <= shard <= count.");
		}
		return new ShardSelection(shard, count);
	}

	/**
	 * The selection configured in the system property {@link #PROPERTY}.
	 */
	public static ShardSelection fromSystemProperty() {
		return parse(System.getProperty(PROPERTY));
	}

	/**
	 * Parses a value of {@link #PROPERTY}.
	 *
	 * @param value
	 *        The value to parse, <code>null</code> or empty for {@link #ALL}.
	 * @throws IllegalArgumentException
	 *         If the value is invalid.
	 */
	public static ShardSelection parse(String value) {
		if (value == null) {
			return ALL;
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT);
		if (normalized.isEmpty() || normalized.equals(ALL_VALUE)) {
			return ALL;
		}
		if (normalized.equals(NONE_VALUE)) {
			return NONE;
		}
		int separator = normalized.indexOf(SHARD_SEPARATOR);
		if (separator > 0) {
			try {
				int shard = Integer.parseInt(normalized.substring(0, separator).trim());
				int count = Integer.parseInt(normalized.substring(separator + SHARD_SEPARATOR.length()).trim());
				if (count >= 1 && shard >= 1 && shard <= count) {
					return new ShardSelection(shard, count);
				}
			} catch (NumberFormatException ex) {
				// Reported below.
			}
		}
		throw new IllegalArgumentException("Invalid value '" + value + "' of system property '" + PROPERTY
			+ "', expected '" + ALL_VALUE + "', '" + NONE_VALUE + "', or '<shard>" + SHARD_SEPARATOR
			+ "<count>' with 1 <= shard <= count.");
	}

	/**
	 * Whether this selection runs all tests.
	 */
	public boolean isAll() {
		return _count == 0;
	}

	/**
	 * Whether this selection runs the tests that are not scripted.
	 */
	public boolean includesNonScripted() {
		return _count <= 0 || _shard == 1;
	}

	/**
	 * Selects the units run by this selection.
	 *
	 * @param units
	 *        All units, in any order.
	 * @param key
	 *        Stable identifier of a unit.
	 * @param weight
	 *        Estimated cost of a unit.
	 * @return The units selected by this selection, in the order of the given units.
	 */
	public <U> List<U> select(List<U> units, Function<? super U, String> key, ToIntFunction<? super U> weight) {
		if (_count == 0) {
			return new ArrayList<>(units);
		}
		if (_count < 0) {
			return new ArrayList<>();
		}
		Set<U> selected = identitySet(distribute(units, key, weight, _count).get(_shard - 1));
		List<U> result = new ArrayList<>();
		for (U unit : units) {
			if (selected.contains(unit)) {
				result.add(unit);
			}
		}
		return result;
	}

	/**
	 * Distributes units among shards.
	 *
	 * <p>
	 * The units are sorted by decreasing weight and, for equal weight, by increasing key. In this
	 * order, each unit is added to the shard with the smallest total weight so far (the first such
	 * shard on a tie).
	 * </p>
	 *
	 * @param units
	 *        The units to distribute.
	 * @param key
	 *        Stable identifier of a unit.
	 * @param weight
	 *        Estimated cost of a unit.
	 * @param shardCount
	 *        The number of shards.
	 * @return For each shard (index 0 is shard 1), the units assigned to it.
	 */
	public static <U> List<List<U>> distribute(List<U> units, Function<? super U, String> key,
			ToIntFunction<? super U> weight, int shardCount) {
		List<U> sorted = new ArrayList<>(units);
		Comparator<U> byWeight = Comparator.<U> comparingInt(weight::applyAsInt).reversed();
		Collections.sort(sorted, byWeight.thenComparing(key::apply));

		List<List<U>> result = new ArrayList<>(shardCount);
		long[] load = new long[shardCount];
		for (int n = 0; n < shardCount; n++) {
			result.add(new ArrayList<>());
		}
		for (U unit : sorted) {
			int target = 0;
			for (int n = 1; n < shardCount; n++) {
				if (load[n] < load[target]) {
					target = n;
				}
			}
			result.get(target).add(unit);
			load[target] += weight.applyAsInt(unit);
		}
		return result;
	}

	/**
	 * Reduces the given test tree to the tests selected by this selection.
	 *
	 * <p>
	 * Selected {@link ScriptedTestUnit}s are replaced by their contents in their enclosing
	 * {@link TestSuite}, unselected ones are removed. A {@link ScriptedTestMarker scripted test}
	 * outside of any unit forms a unit of its own. Tests that are not scripted are kept, if
	 * {@link #includesNonScripted()}. Suites and decorators (e.g. application setups) that lose all
	 * their tests are removed.
	 * </p>
	 *
	 * @param root
	 *        The test tree to reduce in place.
	 * @return The given root test. It may be empty, if nothing is selected.
	 */
	public Test apply(Test root) {
		List<Test> units = new ArrayList<>();
		collectUnits(root, units);
		List<Test> selected = select(units, ShardSelection::unitKey, Test::countTestCases);
		if (!isAll()) {
			report(units, selected);
		}
		new Pruner(identitySet(selected), includesNonScripted()).prune(root);
		return root;
	}

	private void report(List<Test> units, List<Test> selected) {
		int allCases = units.stream().mapToInt(Test::countTestCases).sum();
		int selectedCases = selected.stream().mapToInt(Test::countTestCases).sum();
		StringBuilder message = new StringBuilder();
		message.append("Scripted tests ").append(PROPERTY).append('=').append(this).append(": ");
		message.append(selected.size()).append(" of ").append(units.size()).append(" units, ");
		message.append(selectedCases).append(" of ").append(allCases).append(" scripted test cases.");
		for (Test unit : selected) {
			message.append("\n  Unit '").append(unitKey(unit)).append("' (").append(unit.countTestCases())
				.append(')');
		}
		System.out.println(message);
	}

	private static void collectUnits(Test test, List<Test> units) {
		if (test instanceof ScriptedTestUnit || test instanceof ScriptedTestMarker) {
			units.add(test);
			return;
		}
		Test inner = ScriptedTestUnit.innerTest(test);
		if (inner != null) {
			collectUnits(inner, units);
			return;
		}
		if (test instanceof TestSuite) {
			for (Test child : Collections.list(((TestSuite) test).tests())) {
				collectUnits(child, units);
			}
		}
	}

	private static String unitKey(Test unit) {
		if (unit instanceof ScriptedTestUnit) {
			return ((ScriptedTestUnit) unit).getKey();
		}
		return String.valueOf(unit);
	}

	private static <T> Set<T> identitySet(List<T> elements) {
		Set<T> result = Collections.newSetFromMap(new IdentityHashMap<>());
		result.addAll(elements);
		return result;
	}

	@Override
	public String toString() {
		if (_count == 0) {
			return ALL_VALUE;
		}
		if (_count < 0) {
			return NONE_VALUE;
		}
		return _shard + SHARD_SEPARATOR + _count;
	}

	/**
	 * Removes unselected tests from a test tree.
	 */
	private static final class Pruner {

		private final Set<Test> _selectedUnits;

		private final boolean _includeNonScripted;

		Pruner(Set<Test> selectedUnits, boolean includeNonScripted) {
			_selectedUnits = selectedUnits;
			_includeNonScripted = includeNonScripted;
		}

		/**
		 * Reduces the given test in place.
		 *
		 * @return Whether the given test must be kept in its parent.
		 */
		boolean prune(Test test) {
			if (test instanceof ScriptedTestUnit || test instanceof ScriptedTestMarker) {
				return _selectedUnits.contains(test);
			}
			Test inner = ScriptedTestUnit.innerTest(test);
			if (inner != null) {
				return prune(inner);
			}
			if (test instanceof TestSuite) {
				TestSuite suite = (TestSuite) test;
				List<Test> children = TestUtils.removeTestsFromSuite(suite);
				for (Test child : children) {
					if (child instanceof ScriptedTestUnit) {
						if (_selectedUnits.contains(child)) {
							for (Test content : TestUtils.removeTestsFromSuite((TestSuite) child)) {
								suite.addTest(content);
							}
						}
					} else if (prune(child)) {
						suite.addTest(child);
					}
				}
				return suite.testCount() > 0 || (children.isEmpty() && _includeNonScripted);
			}
			return _includeNonScripted;
		}
	}

}
