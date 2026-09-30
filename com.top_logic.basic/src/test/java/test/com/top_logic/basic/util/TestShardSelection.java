/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test case for {@link ShardSelection}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestShardSelection extends TestCase {

	private static final Function<Unit, String> KEY = Unit::key;

	private static final ToIntFunction<Unit> WEIGHT = Unit::weight;

	/**
	 * Unit to distribute in the tests.
	 * 
	 * @param key
	 *        Stable identifier.
	 * @param weight
	 *        Cost of the unit.
	 */
	record Unit(String key, int weight) {
		// Pure value.
	}

	/**
	 * Tests parsing of valid values.
	 */
	public void testParse() {
		assertSame(ShardSelection.ALL, ShardSelection.parse(null));
		assertSame(ShardSelection.ALL, ShardSelection.parse(""));
		assertSame(ShardSelection.ALL, ShardSelection.parse(ShardSelection.ALL_VALUE));
		assertSame(ShardSelection.ALL, ShardSelection.parse(" ALL "));
		assertSame(ShardSelection.NONE, ShardSelection.parse(ShardSelection.NONE_VALUE));

		ShardSelection shard = ShardSelection.parse("2" + ShardSelection.SHARD_SEPARATOR + "4");
		assertEquals("2" + ShardSelection.SHARD_SEPARATOR + "4", shard.toString());
		assertFalse(shard.isAll());
		assertFalse(shard.includesNonScripted());
		assertFalse(ShardSelection.parse("1" + ShardSelection.SHARD_SEPARATOR + "4").includesNonScripted());
		assertFalse(ShardSelection.parse("1" + ShardSelection.SHARD_SEPARATOR + "1").includesNonScripted());
		assertTrue(ShardSelection.ALL.includesNonScripted());
		assertTrue(ShardSelection.NONE.includesNonScripted());
	}

	/**
	 * Tests that invalid values are rejected with a message naming the property.
	 */
	public void testParseInvalid() {
		for (String invalid : Arrays.asList("x", "some", "2", "2/", "/4", "0/4", "5/4", "2/0", "-1/3", "a/b", "1/2/3")) {
			try {
				ShardSelection.parse(invalid);
				fail("Invalid value accepted: " + invalid);
			} catch (IllegalArgumentException ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains(ShardSelection.PROPERTY));
				assertTrue(ex.getMessage(), ex.getMessage().contains(invalid));
			}
		}
	}

	/**
	 * Tests that every unit is assigned to exactly one shard.
	 */
	public void testCompleteAndDisjoint() {
		List<Unit> units = randomUnits(100, 4711);
		for (int count = 1; count <= 9; count++) {
			List<List<Unit>> shards = ShardSelection.distribute(units, KEY, WEIGHT, count);
			assertEquals(count, shards.size());
			List<Unit> union = new ArrayList<>();
			shards.forEach(union::addAll);
			assertEquals(units.size(), union.size());
			assertEquals(new HashSet<>(units), new HashSet<>(union));

			List<Unit> selectedUnion = new ArrayList<>();
			for (int shard = 1; shard <= count; shard++) {
				selectedUnion.addAll(ShardSelection.shard(shard, count).select(units, KEY, WEIGHT));
			}
			assertEquals(units.size(), selectedUnion.size());
			assertEquals(new HashSet<>(units), new HashSet<>(selectedUnion));
		}
	}

	/**
	 * Tests that the assignment does not depend on the order of the units.
	 */
	public void testDeterministic() {
		List<Unit> units = randomUnits(60, 42);
		Map<String, Integer> expected = assignment(ShardSelection.distribute(units, KEY, WEIGHT, 5));
		for (int n = 0; n < 10; n++) {
			List<Unit> shuffled = new ArrayList<>(units);
			Collections.shuffle(shuffled, new Random(n));
			assertEquals(expected, assignment(ShardSelection.distribute(shuffled, KEY, WEIGHT, 5)));
		}
	}

	/**
	 * Tests that a skewed input is distributed evenly.
	 */
	public void testBalance() {
		List<Unit> units = new ArrayList<>();
		units.add(new Unit("huge", 90));
		units.add(new Unit("large", 40));
		units.add(new Unit("medium", 20));
		for (int n = 0; n < 150; n++) {
			units.add(new Unit("small" + n, 1 + n % 3));
		}
		int count = 4;
		List<List<Unit>> shards = ShardSelection.distribute(units, KEY, WEIGHT, count);
		int total = units.stream().mapToInt(Unit::weight).sum();
		int max = 0;
		int min = Integer.MAX_VALUE;
		for (List<Unit> shard : shards) {
			int load = shard.stream().mapToInt(Unit::weight).sum();
			max = Math.max(max, load);
			min = Math.min(min, load);
		}
		// Total 450: optimum is 112.5 per shard.
		assertEquals(450, total);
		assertTrue("Unbalanced: max=" + max + ", min=" + min, max - min <= 3);
	}

	/**
	 * Tests recognizing a test class with scripted tests.
	 */
	public void testMarkIfScripted() {
		Test plain = new TestSetup(new Plain("plain"));
		assertSame(plain, ScriptedTestUnit.markIfScripted("plain", plain));

		TestSuite wrapping = new TestSuite("wrapping");
		wrapping.addTest(new Plain("plain"));
		wrapping.addTest(new TestSetup(new Wrapper(new Scripted("scripted"))));
		Test unit = ScriptedTestUnit.markIfScripted("wrapping", wrapping);
		assertTrue(unit instanceof ScriptedTestUnit);
		assertEquals("wrapping", ((ScriptedTestUnit) unit).getKey());
		assertEquals(2, unit.countTestCases());
	}

	/**
	 * Tests reducing a test tree to a shard.
	 */
	public void testApply() {
		Set<String> scriptedAll = new HashSet<>();
		Set<String> nonScriptedAll = new HashSet<>();
		collect(createTree(), scriptedAll, nonScriptedAll);

		Set<String> scriptedUnion = new HashSet<>();
		int count = 3;
		for (int shard = 1; shard <= count; shard++) {
			Test tree = ShardSelection.shard(shard, count).apply(createTree());
			Set<String> scripted = new HashSet<>();
			Set<String> nonScripted = new HashSet<>();
			collect(tree, scripted, nonScripted);
			assertTrue(Collections.disjoint(scriptedUnion, scripted));
			scriptedUnion.addAll(scripted);
			assertEquals(Collections.emptySet(), nonScripted);
			assertNoUnitsAndNoEmptySetups(tree);
		}
		assertEquals(scriptedAll, scriptedUnion);

		Test none = ShardSelection.NONE.apply(createTree());
		Set<String> scripted = new HashSet<>();
		Set<String> nonScripted = new HashSet<>();
		collect(none, scripted, nonScripted);
		assertEquals(Collections.emptySet(), scripted);
		assertEquals(nonScriptedAll, nonScripted);
		assertNoUnitsAndNoEmptySetups(none);

		// NONE and all shards together run each test exactly once.
		assertEquals(createTree().countTestCases(), none.countTestCases() + shardCases(count));

		Test all = ShardSelection.ALL.apply(createTree());
		assertEquals(createTree().countTestCases(), all.countTestCases());
		assertNoUnitsAndNoEmptySetups(all);
	}

	private static int shardCases(int count) {
		int result = 0;
		for (int shard = 1; shard <= count; shard++) {
			result += ShardSelection.shard(shard, count).apply(createTree()).countTestCases();
		}
		return result;
	}

	private static TestSuite createTree() {
		TestSuite root = new TestSuite("root");
		root.addTest(new Plain("plain1"));
		TestSuite pkg = new TestSuite("pkg");
		pkg.addTest(new Plain("plain2"));
		for (int n = 0; n < 5; n++) {
			TestSuite classSuite = new TestSuite("class" + n);
			for (int m = 0; m <= n; m++) {
				classSuite.addTest(new Scripted("class" + n + "-" + m));
			}
			pkg.addTest(ScriptedTestUnit.markIfScripted("class" + n, new TestSetup(classSuite)));
		}
		pkg.addTest(ScriptedTestUnit.markIfScripted("plainClass", new TestSetup(new Plain("plain3"))));
		TestSuite wrappingClass = new TestSuite("wrappingClass");
		wrappingClass.addTest(new Wrapper(new Scripted("wrapped")));
		pkg.addTest(ScriptedTestUnit.markIfScripted("wrappingClass", wrappingClass));
		pkg.addTest(new Wrapper(new Scripted("strayWrapped")));
		root.addTest(pkg);

		TestSuite scriptDirs = new TestSuite("dirs");
		for (int n = 0; n < 4; n++) {
			ScriptedTestUnit dir = new ScriptedTestUnit("dir" + n);
			for (int m = 0; m < 3; m++) {
				dir.addTest(new Scripted("dir" + n + "-" + m));
			}
			scriptDirs.addTest(dir);
		}
		root.addTest(new TestSetup(scriptDirs));
		root.addTest(new Scripted("stray"));
		return root;
	}

	private static void collect(Test test, Set<String> scripted, Set<String> nonScripted) {
		if (test instanceof Scripted) {
			assertTrue(scripted.add(((TestCase) test).getName()));
		} else if (test instanceof Plain) {
			assertTrue(nonScripted.add(((TestCase) test).getName()));
		} else if (test instanceof TestSetup) {
			collect(((TestSetup) test).getTest(), scripted, nonScripted);
		} else if (test instanceof Wrapper) {
			collect(((Wrapper) test).getWrappedTest(), scripted, nonScripted);
		} else {
			for (Test child : Collections.list(((TestSuite) test).tests())) {
				collect(child, scripted, nonScripted);
			}
		}
	}

	private static void assertNoUnitsAndNoEmptySetups(Test test) {
		assertFalse(test instanceof ScriptedTestUnit);
		if (test instanceof TestSetup) {
			assertTrue(test.countTestCases() > 0);
			assertNoUnitsAndNoEmptySetups(((TestSetup) test).getTest());
		} else if (test instanceof TestSuite) {
			for (Test child : Collections.list(((TestSuite) test).tests())) {
				assertNoUnitsAndNoEmptySetups(child);
			}
		}
	}

	private static Map<String, Integer> assignment(List<List<Unit>> shards) {
		Map<String, Integer> result = new TreeMap<>();
		for (int n = 0; n < shards.size(); n++) {
			for (Unit unit : shards.get(n)) {
				result.put(unit.key(), n);
			}
		}
		return result;
	}

	private static List<Unit> randomUnits(int size, long seed) {
		Random random = new Random(seed);
		List<Unit> result = new ArrayList<>();
		for (int n = 0; n < size; n++) {
			result.add(new Unit("unit" + n, 1 + random.nextInt(random.nextInt(10) == 0 ? 50 : 5)));
		}
		return result;
	}

	/**
	 * Non-scripted test in a test tree.
	 */
	public static class Plain extends TestCase {

		/**
		 * Creates a {@link Plain}.
		 */
		public Plain(String name) {
			super(name);
		}

		@Override
		protected void runTest() {
			// Nothing to test.
		}
	}

	/**
	 * {@link SingleTestWrapper} in a test tree.
	 */
	public static class Wrapper extends TestCase implements SingleTestWrapper {

		private final Test _test;

		/**
		 * Creates a {@link Wrapper}.
		 */
		public Wrapper(Test test) {
			super("wrapper");
			_test = test;
		}

		@Override
		public Test getWrappedTest() {
			return _test;
		}

		@Override
		protected void runTest() {
			// Nothing to test.
		}
	}

	/**
	 * Scripted test in a test tree.
	 */
	public static class Scripted extends TestCase implements ScriptedTestMarker {

		/**
		 * Creates a {@link Scripted}.
		 */
		public Scripted(String name) {
			super(name);
		}

		@Override
		protected void runTest() {
			// Nothing to test.
		}
	}

}
