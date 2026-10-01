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

import test.com.top_logic.basic.util.TestPruner.Decision;

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
 * scripted tests: Shard <code>i</code> runs nothing but the {@link ScriptedTestUnit}s assigned to
 * it, neither module independent tests nor the module's tests that are not scripted.</dd>
 * </dl>
 *
 * <p>
 * The system property {@link #MODULES_PROPERTY} lists the modules whose scripted tests are
 * distributed over the same shards, see {@link #moduleOffset(String, String)}.
 * </p>
 *
 * <p>
 * The values partition the tests: a run with {@value #NONE_VALUE} together with the runs of all
 * shards <code>1/n</code> to <code>n/n</code> executes each test of {@value #ALL_VALUE} exactly
 * once.
 * </p>
 *
 * <p>
 * Scripted tests are distributed in {@link ScriptedTestUnit units}: the scripts directly contained
 * in one script directory, or all scripted tests of one test class. A unit is never split, because
 * its scripts depend on their order and on shared application state.
 * </p>
 *
 * <p>
 * Assignment rule (see {@link #distribute(List, Function, ToIntFunction, int, int)}): the units
 * of a module are sorted by decreasing weight (number of test cases) and, for equal weight, by
 * increasing {@link ScriptedTestUnit#getKey() key}. In this order, each unit is assigned to the
 * shard with the currently smallest total weight. On a tie, the shards are preferred in the order
 * <code>offset</code>, <code>offset + 1</code>, ... (modulo the shard count, 0-based), where the
 * offset is the index of the module in the list {@link #MODULES_PROPERTY} (0, if the module is not
 * listed, see {@link #moduleOffset(String, String)}). Since each module distributes its units
 * independently, the offset spreads the units of modules with few units over different shards
 * instead of putting all of them into shard 1. The assignment depends only on the module list, the
 * module name, and the keys and weights of all units, so every shard JVM computes the same
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
	 * Name of the system property listing the modules that distribute their scripted tests over
	 * the same shards, in a fixed order.
	 *
	 * <p>
	 * The value is a list of module directory names or paths separated by
	 * {@link #MODULE_SEPARATOR}, e.g. <code>com.top_logic.demo,test-migrate-apps/test-app-rewrite</code>.
	 * </p>
	 *
	 * @see #moduleOffset(String, String)
	 */
	public static final String MODULES_PROPERTY = "TestAll.shardModules";

	/**
	 * Separator of the entries in a value of {@link #MODULES_PROPERTY}.
	 */
	public static final String MODULE_SEPARATOR = ",";

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
	 * Whether this selection runs the tests that are not scripted, which is the case for
	 * {@link #ALL} and {@link #NONE} but not for a shard.
	 */
	public boolean includesNonScripted() {
		return _count <= 0;
	}

	/**
	 * Selects the units run by this selection.
	 *
	 * @param offset
	 *        The 0-based index of the shard preferred on a tie, see
	 *        {@link #moduleOffset(String, String)}.
	 * @param units
	 *        All units of the module, in any order.
	 * @param key
	 *        Stable identifier of a unit.
	 * @param weight
	 *        Estimated cost of a unit.
	 * @return The units selected by this selection, in the order of the given units.
	 */
	public <U> List<U> select(int offset, List<U> units, Function<? super U, String> key,
			ToIntFunction<? super U> weight) {
		if (_count == 0) {
			return new ArrayList<>(units);
		}
		if (_count < 0) {
			return new ArrayList<>();
		}
		Set<U> selected = identitySet(distribute(units, key, weight, _count, offset).get(_shard - 1));
		List<U> result = new ArrayList<>();
		for (U unit : units) {
			if (selected.contains(unit)) {
				result.add(unit);
			}
		}
		return result;
	}

	/**
	 * The index of the given module in a list of modules, used as offset for
	 * {@link #distribute(List, Function, ToIntFunction, int, int)}.
	 *
	 * @param modules
	 *        A value of {@link #MODULES_PROPERTY}, see {@link #parseModules(String)}. May be
	 *        <code>null</code>.
	 * @param module
	 *        The name of the module directory.
	 * @return The 0-based index of the module in the list, 0 if the module is not listed.
	 */
	public static int moduleOffset(String modules, String module) {
		int index = parseModules(modules).indexOf(module);
		return index < 0 ? 0 : index;
	}

	/**
	 * The module offset configured in the system property {@link #MODULES_PROPERTY}.
	 *
	 * @see #moduleOffset(String, String)
	 */
	public static int moduleOffset(String module) {
		return moduleOffset(System.getProperty(MODULES_PROPERTY), module);
	}

	/**
	 * Parses a value of {@link #MODULES_PROPERTY}.
	 *
	 * <p>
	 * Entries are separated by {@link #MODULE_SEPARATOR}. Each entry is reduced to its last path
	 * segment, the name of the module directory. Blank entries are ignored.
	 * </p>
	 *
	 * @param value
	 *        The value to parse, may be <code>null</code>.
	 * @return The module directory names in the given order.
	 */
	public static List<String> parseModules(String value) {
		List<String> result = new ArrayList<>();
		if (value == null) {
			return result;
		}
		for (String entry : value.split(MODULE_SEPARATOR)) {
			String path = entry.trim().replace('\\', '/');
			while (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			String name = path.substring(path.lastIndexOf('/') + 1).trim();
			if (!name.isEmpty()) {
				result.add(name);
			}
		}
		return result;
	}

	/**
	 * Distributes units among shards.
	 *
	 * <p>
	 * The units are sorted by decreasing weight and, for equal weight, by increasing key. In this
	 * order, each unit is added to the shard with the smallest total weight so far. On a tie, the
	 * first such shard in the order <code>offset</code>, <code>offset + 1</code>, ... (modulo
	 * <code>shardCount</code>) is chosen.
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
	 * @param offset
	 *        The 0-based index of the shard preferred on a tie, see
	 *        {@link #moduleOffset(String, String)}.
	 * @return For each shard (index 0 is shard 1), the units assigned to it.
	 */
	public static <U> List<List<U>> distribute(List<U> units, Function<? super U, String> key,
			ToIntFunction<? super U> weight, int shardCount, int offset) {
		List<U> sorted = new ArrayList<>(units);
		Comparator<U> byWeight = Comparator.<U> comparingInt(weight::applyAsInt).reversed();
		Collections.sort(sorted, byWeight.thenComparing(key::apply));

		List<List<U>> result = new ArrayList<>(shardCount);
		long[] load = new long[shardCount];
		for (int n = 0; n < shardCount; n++) {
			result.add(new ArrayList<>());
		}
		for (U unit : sorted) {
			int target = Math.floorMod(offset, shardCount);
			for (int k = 1; k < shardCount; k++) {
				int n = Math.floorMod(offset + k, shardCount);
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
	 * @param offset
	 *        The 0-based index of the shard preferred on a tie, see
	 *        {@link #moduleOffset(String, String)}.
	 * @param root
	 *        The test tree to reduce in place.
	 * @return The given root test. It may be empty, if nothing is selected.
	 */
	public Test apply(int offset, Test root) {
		List<Test> units = new ArrayList<>();
		collectUnits(root, units);
		List<Test> selected = select(offset, units, ShardSelection::unitKey, Test::countTestCases);
		if (!isAll()) {
			report(units, selected);
		}
		Set<Test> selectedUnits = identitySet(selected);
		boolean includeNonScripted = includesNonScripted();
		new TestPruner(test -> decide(test, selectedUnits, includeNonScripted)).prune(root);
		return root;
	}

	private static Decision decide(Test test, Set<Test> selectedUnits, boolean includeNonScripted) {
		if (test instanceof ScriptedTestUnit) {
			return selectedUnits.contains(test) ? Decision.INLINE : Decision.DROP;
		}
		if (test instanceof ScriptedTestMarker) {
			return selectedUnits.contains(test) ? Decision.KEEP : Decision.DROP;
		}
		if (TestPruner.hasInnerTests(test)) {
			return Decision.DESCEND;
		}
		return includeNonScripted ? Decision.KEEP : Decision.DROP;
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
		Test inner = TestPruner.innerTest(test);
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

}
