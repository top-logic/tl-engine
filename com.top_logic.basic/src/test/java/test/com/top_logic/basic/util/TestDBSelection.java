/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DBBoundTest;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.util.TestShardSelection.Plain;
import test.com.top_logic.basic.util.TestShardSelection.Scripted;
import test.com.top_logic.basic.util.TestShardSelection.Wrapper;

/**
 * Test case for {@link DBSelection}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDBSelection extends TestCase {

	private static final int OFFSET = 0;

	/**
	 * Tests the default workers: all multi-database databases except the default database.
	 */
	public void testDefaultWorkers() {
		List<DBType> workers = DBSelection.defaultWorkers();
		assertFalse(workers.contains(DatabaseTestSetup.DEFAULT_DB));
		List<DBType> expected = new ArrayList<>(DatabaseTestSetup.MULTI_DB);
		expected.remove(DatabaseTestSetup.DEFAULT_DB);
		assertEquals(expected, workers);
		assertEquals(workers, DBSelection.parseWorkers(null));
		assertEquals(workers, DBSelection.parseWorkers(" "));
		assertEquals(workers, DBSelection.parse(null, null).getWorkers());
		assertEquals(workers, DBSelection.ALL.getWorkers());
	}

	/**
	 * Tests parsing of valid values.
	 */
	public void testParse() {
		assertTrue(DBSelection.parse(null, null).isAll());
		assertTrue(DBSelection.parse("", null).isAll());
		assertTrue(DBSelection.parse(" ALL ", null).isAll());
		assertTrue(DBSelection.parse(DBSelection.ALL_VALUE, null).includesUnbound());
		assertEquals(DBSelection.ALL_VALUE, DBSelection.parse(DBSelection.ALL_VALUE, null).toString());

		DBSelection none = DBSelection.parse(DBSelection.NONE_VALUE, null);
		assertFalse(none.isAll());
		assertTrue(none.includesUnbound());
		assertNull(none.getDB());
		assertEquals(DBSelection.NONE_VALUE, none.toString());

		for (DBType db : DBSelection.defaultWorkers()) {
			DBSelection selection = DBSelection.parse(" " + db.getExternalName().toUpperCase() + " ", null);
			assertFalse(selection.isAll());
			assertFalse(selection.includesUnbound());
			assertSame(db, selection.getDB());
			assertEquals(db.getExternalName(), selection.toString());
		}

		DBType first = worker(0);
		DBType second = worker(1);
		DBSelection explicit = DBSelection.parse(second.getExternalName(),
			second.getExternalName() + DBSelection.WORKER_SEPARATOR + " " + first.getExternalName() + " "
				+ DBSelection.WORKER_SEPARATOR);
		assertEquals(List.of(second, first), explicit.getWorkers());
		assertSame(second, explicit.getDB());
	}

	/**
	 * Tests that invalid values are rejected with a message naming the property.
	 */
	public void testParseInvalid() {
		assertInvalid("foo", null, DBSelection.PROPERTY);
		assertInvalid("1/2", null, DBSelection.PROPERTY);
		// The default database runs with the non-worker tests.
		assertInvalid(DatabaseTestSetup.DEFAULT_DB.getExternalName(), null, DBSelection.PROPERTY);
		// Not a worker.
		assertInvalid(worker(1).getExternalName(), worker(0).getExternalName(), DBSelection.PROPERTY);
		// DB2 is no default worker.
		assertInvalid(DBType.DB2_DB.getExternalName(), null, DBSelection.PROPERTY);

		assertInvalid(DBSelection.ALL_VALUE, "foo", DBSelection.WORKERS_PROPERTY);
		assertInvalid(DBSelection.NONE_VALUE, worker(0).getExternalName() + DBSelection.WORKER_SEPARATOR + "foo",
			DBSelection.WORKERS_PROPERTY);
		assertInvalid(DBSelection.NONE_VALUE, DatabaseTestSetup.DEFAULT_DB.getExternalName(),
			DBSelection.WORKERS_PROPERTY);
	}

	private static void assertInvalid(String value, String workers, String property) {
		try {
			DBSelection.parse(value, workers);
			fail("Invalid configuration accepted: " + value + ", workers " + workers);
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(property));
		}
	}

	/**
	 * Tests that a single database keeps only the tests bound to that database.
	 */
	public void testSingleDB() {
		for (DBType db : DBSelection.defaultWorkers()) {
			Test tree = select(db.getExternalName(), null, createTree());
			Set<String> names = names(tree);
			assertFalse(names.isEmpty());
			for (String name : names) {
				assertTrue(name, name.endsWith(" " + db.getExternalName()));
			}
			assertEquals(boundNames(db), names);
			assertNoEmptySetups(tree);
		}
	}

	/**
	 * Tests that {@link DBSelection#NONE_VALUE} keeps unbound, default database and non-worker
	 * database tests.
	 */
	public void testNone() {
		Test tree = select(DBSelection.NONE_VALUE, null, createTree());
		Set<String> names = names(tree);
		Set<String> expected = new HashSet<>(unboundNames());
		expected.addAll(boundNames(DatabaseTestSetup.DEFAULT_DB));
		expected.addAll(boundNames(DBType.DB2_DB));
		assertEquals(expected, names);
		assertNoEmptySetups(tree);

		// With an explicit worker list, the tests of the other databases run in the run "none".
		DBType worker = worker(0);
		Set<String> reduced = names(select(DBSelection.NONE_VALUE, worker.getExternalName(), createTree()));
		Set<String> expectedReduced = names(createTree());
		expectedReduced.removeAll(boundNames(worker));
		assertEquals(expectedReduced, reduced);
	}

	/**
	 * Tests that the run {@link DBSelection#NONE_VALUE} and one run per worker together run each
	 * test exactly once.
	 */
	public void testPartition() {
		List<String> all = nameList(select(DBSelection.ALL_VALUE, null, createTree()));
		assertEquals(createTree().countTestCases(), all.size());

		List<String> union = new ArrayList<>(nameList(select(DBSelection.NONE_VALUE, null, createTree())));
		for (DBType db : DBSelection.defaultWorkers()) {
			union.addAll(nameList(select(db.getExternalName(), null, createTree())));
		}
		Collections.sort(all);
		Collections.sort(union);
		assertEquals(all, union);
		assertEquals(new HashSet<>(all).size(), all.size());
	}

	/**
	 * Tests the combination with a {@link ShardSelection}.
	 */
	public void testWithShardSelection() {
		Set<String> scripted = scriptedNames();
		Set<String> notScripted = names(createTree());
		notScripted.removeAll(scripted);

		// Module tests: neither scripted tests nor worker database tests.
		Test moduleTests = ShardSelection.NONE.apply(OFFSET, createTree());
		select(DBSelection.NONE_VALUE, null, moduleTests);
		Set<String> expectedModuleTests = new HashSet<>(notScripted);
		for (DBType db : DBSelection.defaultWorkers()) {
			expectedModuleTests.removeAll(boundNames(db));
		}
		assertEquals(expectedModuleTests, names(moduleTests));

		// Shards: all scripted tests, none of them is bound to a database.
		Set<String> shardUnion = new HashSet<>();
		int count = 2;
		for (int shard = 1; shard <= count; shard++) {
			Test shardTests = ShardSelection.shard(shard, count).apply(OFFSET, createTree());
			select(DBSelection.NONE_VALUE, null, shardTests);
			Set<String> shardNames = names(shardTests);
			assertTrue(Collections.disjoint(shardUnion, shardNames));
			shardUnion.addAll(shardNames);
		}
		assertEquals(scripted, shardUnion);

		// A database run contains no scripted tests.
		for (DBType db : DBSelection.defaultWorkers()) {
			Test dbTests = ShardSelection.NONE.apply(OFFSET, createTree());
			select(db.getExternalName(), null, dbTests);
			assertEquals(boundNames(db), names(dbTests));
		}
	}

	/**
	 * Tests that a nested binding to another database is reported.
	 */
	public void testConflictingBinding() {
		DBType outer = worker(0);
		DBType inner = worker(1);
		TestSuite root = new TestSuite("root");
		root.addTest(new Bound(DatabaseTestSetup.getDBTest(new Plain("conflict"), inner), outer));
		try {
			select(DBSelection.NONE_VALUE, null, root);
			fail("Conflicting binding not detected.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(outer.getExternalName()));
			assertTrue(ex.getMessage(), ex.getMessage().contains(inner.getExternalName()));
		}

		// A nested binding to the same database is fine.
		TestSuite consistent = new TestSuite("root");
		consistent.addTest(new Bound(DatabaseTestSetup.getDBTest(new Plain("same " + outer), outer), outer));
		assertEquals(1, select(outer.getExternalName(), null, consistent).countTestCases());
	}

	private static DBType worker(int index) {
		return DBSelection.defaultWorkers().get(index);
	}

	private static Test select(String value, String workers, Test tree) {
		return DBSelection.parse(value, workers).apply(tree);
	}

	/**
	 * Test tree with tests bound to all databases (including one not tested by default), tests
	 * bound to no database, and scripted tests.
	 */
	private static TestSuite createTree() {
		TestSuite root = new TestSuite("root");
		root.addTest(new Plain("plain1"));

		// Multi-database test as created by DatabaseTestSetup.getDBTest(Class, TestFactory).
		TestSuite multi = new TestSuite("multi");
		for (DBType db : allDBs()) {
			TestSuite classSuite = new TestSuite("class with " + db);
			classSuite.addTest(new Plain("a " + db));
			classSuite.addTest(new Wrapper(new Plain("b " + db)));
			multi.addTest(DatabaseTestSetup.getDBTest(classSuite, db));
		}
		root.addTest(new TestSetup(multi));

		// Single-database tests in a package with unbound tests.
		TestSuite pkg = new TestSuite("pkg");
		pkg.addTest(new Plain("plain2"));
		pkg.addTest(new TestSuite("empty"));
		for (DBType db : allDBs()) {
			pkg.addTest(new TestSetup(new Bound(new Plain("c " + db), db)));
		}
		root.addTest(pkg);

		// Scripted tests.
		TestSuite scriptDirs = new TestSuite("dirs");
		for (int n = 0; n < 3; n++) {
			ScriptedTestUnit dir = new ScriptedTestUnit("dir" + n);
			for (int m = 0; m < 2; m++) {
				dir.addTest(new Scripted("dir" + n + "-" + m));
			}
			scriptDirs.addTest(dir);
		}
		root.addTest(new TestSetup(scriptDirs));
		return root;
	}

	private static List<DBType> allDBs() {
		List<DBType> result = new ArrayList<>(DatabaseTestSetup.MULTI_DB);
		result.add(DBType.DB2_DB);
		return result;
	}

	private static Set<String> boundNames(DBType db) {
		return Set.of("a " + db, "b " + db, "c " + db);
	}

	private static Set<String> unboundNames() {
		Set<String> result = new HashSet<>(Set.of("plain1", "plain2"));
		result.addAll(scriptedNames());
		return result;
	}

	private static Set<String> scriptedNames() {
		Set<String> result = new HashSet<>();
		for (int n = 0; n < 3; n++) {
			for (int m = 0; m < 2; m++) {
				result.add("dir" + n + "-" + m);
			}
		}
		return result;
	}

	private static Set<String> names(Test test) {
		List<String> list = nameList(test);
		Set<String> result = new HashSet<>(list);
		assertEquals("Duplicate tests: " + list, list.size(), result.size());
		return result;
	}

	private static List<String> nameList(Test test) {
		List<String> result = new ArrayList<>();
		collect(test, result);
		return result;
	}

	private static void collect(Test test, List<String> names) {
		if (test instanceof Wrapper) {
			collect(((Wrapper) test).getWrappedTest(), names);
		} else if (test instanceof TestCase) {
			names.add(((TestCase) test).getName());
		} else {
			for (Test inner : TestPruner.innerTests(test)) {
				collect(inner, names);
			}
		}
	}

	private static void assertNoEmptySetups(Test test) {
		if (TestPruner.innerTest(test) != null) {
			assertTrue(test.countTestCases() > 0);
		}
		for (Test inner : TestPruner.innerTests(test)) {
			assertNoEmptySetups(inner);
		}
	}

	/**
	 * Setup binding its test to a database.
	 */
	public static class Bound extends TestSetup implements DBBoundTest {

		private final DBType _db;

		/**
		 * Creates a {@link Bound}.
		 */
		public Bound(Test test, DBType db) {
			super(test);
			_db = db;
		}

		@Override
		public DBType getBoundDB() {
			return _db;
		}
	}

}
