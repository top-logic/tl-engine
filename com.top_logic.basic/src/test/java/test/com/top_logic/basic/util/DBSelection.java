/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.basic.DBBoundTest;
import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.util.TestPruner.Decision;

/**
 * Selection of the database tests that a test run executes, configured by the system property
 * {@link #PROPERTY}.
 *
 * <p>
 * All modules share one set of external database schemas per database. A full-database test run
 * (all {@link DatabaseTestSetup#MULTI_DB databases}) can be split into concurrent runs, one per
 * external database (a worker), and one run for all other tests. The worker databases are listed in
 * the system property {@link #WORKERS_PROPERTY}.
 * </p>
 *
 * <p>
 * Values of {@link #PROPERTY}:
 * </p>
 * <dl>
 * <dt>{@value #ALL_VALUE} (default, also when the property is not set)</dt>
 * <dd>All tests run. Whether the multi-database tests run with all databases or only with the
 * {@link DatabaseTestSetup#DEFAULT_DB default database} is decided by
 * {@link DatabaseTestSetup#ONLY_DEFAULT_DB_PROPERTY}.</dd>
 * <dt>{@value #NONE_VALUE}</dt>
 * <dd>All tests run except the tests bound to one of the worker databases.</dd>
 * <dt>The {@link DBType#getExternalName() name} of a worker database (e.g. <code>mysql</code>)</dt>
 * <dd>Only the tests bound to that database run: neither module independent tests, nor tests bound
 * to no or another database, nor scripted tests.</dd>
 * </dl>
 *
 * <p>
 * A value other than {@value #ALL_VALUE} selects from the tests of all databases, as if
 * {@link DatabaseTestSetup#ONLY_DEFAULT_DB_PROPERTY} was <code>false</code>, see
 * {@link DatabaseTestSetup#useOnlyDefaultDB()}.
 * </p>
 *
 * <p>
 * The values partition the tests: a run with {@value #NONE_VALUE} together with one run for each
 * worker database executes each test of a full-database run exactly once. The selection composes
 * with the {@link ShardSelection}.
 * </p>
 *
 * <p>
 * A test is bound to a database, if it lies in the subtree of a {@link DBBoundTest} (e.g. a
 * {@link DatabaseTestSetup}). The outermost binding decides. A nested binding to a different
 * database is a misconfiguration that is reported as error.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DBSelection {

	/**
	 * Name of the system property selecting the database tests to run.
	 */
	public static final String PROPERTY = "TestAll.db";

	/**
	 * Value of {@link #PROPERTY} running all tests.
	 */
	public static final String ALL_VALUE = "all";

	/**
	 * Value of {@link #PROPERTY} running all tests except the ones bound to a worker database.
	 */
	public static final String NONE_VALUE = "none";

	/**
	 * Name of the system property listing the databases that run their tests in separate runs
	 * (workers).
	 *
	 * <p>
	 * The value is a list of {@link DBType#getExternalName() database names} separated by
	 * {@link #WORKER_SEPARATOR}, e.g. <code>mysql,oracle19</code>. When not set, all
	 * {@link DatabaseTestSetup#MULTI_DB databases} except the
	 * {@link DatabaseTestSetup#DEFAULT_DB default database} are workers, see
	 * {@link #defaultWorkers()}.
	 * </p>
	 */
	public static final String WORKERS_PROPERTY = "TestAll.dbWorkers";

	/**
	 * Separator of the entries in a value of {@link #WORKERS_PROPERTY}.
	 */
	public static final String WORKER_SEPARATOR = ",";

	/**
	 * Selection running all tests with the default worker databases.
	 */
	public static final DBSelection ALL = new DBSelection(true, null, defaultWorkers());

	/**
	 * Whether this is the {@link #ALL_VALUE} selection.
	 */
	private final boolean _all;

	/**
	 * The database whose tests run, <code>null</code> for {@link #ALL_VALUE} and
	 * {@link #NONE_VALUE}.
	 */
	private final DBType _db;

	/**
	 * The databases that run their tests in separate runs.
	 */
	private final List<DBType> _workers;

	private DBSelection(boolean all, DBType db, List<DBType> workers) {
		_all = all;
		_db = db;
		_workers = Collections.unmodifiableList(workers);
	}

	/**
	 * The selection configured in the system properties {@link #PROPERTY} and
	 * {@link #WORKERS_PROPERTY}.
	 *
	 * @throws IllegalArgumentException
	 *         If the configuration is invalid.
	 */
	public static DBSelection fromSystemProperties() {
		return parse(System.getProperty(PROPERTY), System.getProperty(WORKERS_PROPERTY));
	}

	/**
	 * Whether the system property {@link #PROPERTY} selects all tests.
	 *
	 * <p>
	 * In contrast to {@link #fromSystemProperties()}, the value is not validated.
	 * </p>
	 */
	public static boolean selectsAll() {
		return isAllValue(normalize(System.getProperty(PROPERTY)));
	}

	/**
	 * Parses values of {@link #PROPERTY} and {@link #WORKERS_PROPERTY}.
	 *
	 * @param value
	 *        The value of {@link #PROPERTY}, <code>null</code> or empty for
	 *        {@value #ALL_VALUE}.
	 * @param workers
	 *        The value of {@link #WORKERS_PROPERTY}, <code>null</code> or empty for the
	 *        {@link #defaultWorkers() default workers}.
	 * @throws IllegalArgumentException
	 *         If one of the values is invalid, or if the selected database is no worker.
	 */
	public static DBSelection parse(String value, String workers) {
		List<DBType> workerList = parseWorkers(workers);
		String normalized = normalize(value);
		if (isAllValue(normalized)) {
			return new DBSelection(true, null, workerList);
		}
		if (normalized.equals(NONE_VALUE)) {
			return new DBSelection(false, null, workerList);
		}
		DBType db = dbType(normalized);
		if (db == null) {
			throw new IllegalArgumentException("Invalid value '" + value + "' of system property '" + PROPERTY
				+ "', expected '" + ALL_VALUE + "', '" + NONE_VALUE + "', or one of the worker databases "
				+ names(workerList) + ".");
		}
		if (!workerList.contains(db)) {
			throw new IllegalArgumentException("Database '" + db + "' selected by system property '" + PROPERTY
				+ "' is not one of the worker databases " + names(workerList) + " (system property '"
				+ WORKERS_PROPERTY + "'). Its tests run in the run '" + NONE_VALUE + "'.");
		}
		return new DBSelection(false, db, workerList);
	}

	/**
	 * Parses a value of {@link #WORKERS_PROPERTY}.
	 *
	 * @param value
	 *        The value to parse, <code>null</code> or empty for the {@link #defaultWorkers()}.
	 * @return The worker databases in the given order.
	 * @throws IllegalArgumentException
	 *         If the value names an unknown database or the
	 *         {@link DatabaseTestSetup#DEFAULT_DB default database}.
	 */
	public static List<DBType> parseWorkers(String value) {
		String normalized = normalize(value);
		if (normalized.isEmpty()) {
			return defaultWorkers();
		}
		Set<DBType> result = new LinkedHashSet<>();
		for (String entry : normalized.split(WORKER_SEPARATOR)) {
			String name = entry.trim();
			if (name.isEmpty()) {
				continue;
			}
			DBType db = dbType(name);
			if (db == null) {
				throw new IllegalArgumentException("Invalid database '" + name + "' in system property '"
					+ WORKERS_PROPERTY + "', expected a list of " + names(List.of(DBType.values())) + ".");
			}
			if (db == DatabaseTestSetup.DEFAULT_DB) {
				throw new IllegalArgumentException("The default database '" + db + "' in system property '"
					+ WORKERS_PROPERTY + "' cannot be a worker, its tests run in the run '" + NONE_VALUE + "'.");
			}
			result.add(db);
		}
		return new ArrayList<>(result);
	}

	/**
	 * The worker databases, when {@link #WORKERS_PROPERTY} is not set: all
	 * {@link DatabaseTestSetup#MULTI_DB databases} except the
	 * {@link DatabaseTestSetup#DEFAULT_DB default database}.
	 */
	public static List<DBType> defaultWorkers() {
		List<DBType> result = new ArrayList<>(DatabaseTestSetup.MULTI_DB);
		result.remove(DatabaseTestSetup.DEFAULT_DB);
		return result;
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private static boolean isAllValue(String normalized) {
		return normalized.isEmpty() || normalized.equals(ALL_VALUE);
	}

	private static DBType dbType(String name) {
		for (DBType db : DBType.values()) {
			if (db.getExternalName().equalsIgnoreCase(name)) {
				return db;
			}
		}
		return null;
	}

	private static String names(List<DBType> dbs) {
		return dbs.stream().map(DBType::getExternalName).collect(Collectors.joining(WORKER_SEPARATOR, "'", "'"));
	}

	/**
	 * Whether this selection runs all tests.
	 */
	public boolean isAll() {
		return _all;
	}

	/**
	 * Whether this selection runs the tests not bound to a database, which is the case for
	 * {@value #ALL_VALUE} and {@value #NONE_VALUE} but not for a single database.
	 */
	public boolean includesUnbound() {
		return _db == null;
	}

	/**
	 * The database whose tests run, <code>null</code> for {@value #ALL_VALUE} and
	 * {@value #NONE_VALUE}.
	 */
	public DBType getDB() {
		return _db;
	}

	/**
	 * The databases that run their tests in separate runs.
	 */
	public List<DBType> getWorkers() {
		return _workers;
	}

	/**
	 * Reduces the given test tree to the tests selected by this selection.
	 *
	 * @param root
	 *        The test tree to reduce in place.
	 * @return The given root test. It may be empty, if nothing is selected.
	 * @throws IllegalStateException
	 *         If the tree contains a test bound to a database inside a test bound to another
	 *         database.
	 */
	public Test apply(Test root) {
		if (_all) {
			return root;
		}
		checkBindings(root, null);
		int allCases = root.countTestCases();
		new TestPruner(this::decide).prune(root);
		report(allCases, root.countTestCases());
		return root;
	}

	private Decision decide(Test test) {
		DBType bound = boundDB(test);
		if (bound != null) {
			if (_db != null) {
				return bound == _db ? Decision.KEEP : Decision.DROP;
			}
			return _workers.contains(bound) ? Decision.DROP : Decision.KEEP;
		}
		if (TestPruner.hasInnerTests(test)) {
			return Decision.DESCEND;
		}
		return includesUnbound() ? Decision.KEEP : Decision.DROP;
	}

	/**
	 * The database the given test is bound to, <code>null</code>, if the test itself is not
	 * bound.
	 */
	public static DBType boundDB(Test test) {
		if (test instanceof DBBoundTest) {
			return ((DBBoundTest) test).getBoundDB();
		}
		return null;
	}

	private static void checkBindings(Test test, DBBoundTest enclosing) {
		DBBoundTest binding = enclosing;
		if (test instanceof DBBoundTest) {
			DBBoundTest bound = (DBBoundTest) test;
			if (enclosing != null && enclosing.getBoundDB() != bound.getBoundDB()) {
				throw new IllegalStateException("Test '" + test + "' bound to database '" + bound.getBoundDB()
					+ "' is nested in test '" + enclosing + "' bound to database '" + enclosing.getBoundDB()
					+ "'.");
			}
			binding = bound;
		}
		for (Test inner : TestPruner.innerTests(test)) {
			checkBindings(inner, binding);
		}
	}

	private void report(int allCases, int selectedCases) {
		System.out.println("Database tests " + PROPERTY + "=" + this + " (" + WORKERS_PROPERTY + "="
			+ _workers.stream().map(DBType::getExternalName).collect(Collectors.joining(WORKER_SEPARATOR)) + "): "
			+ selectedCases + " of " + allCases + " test cases.");
	}

	@Override
	public String toString() {
		if (_all) {
			return ALL_VALUE;
		}
		if (_db == null) {
			return NONE_VALUE;
		}
		return _db.getExternalName();
	}

}
