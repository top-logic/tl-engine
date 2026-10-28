/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.db.schema.BranchLastSetup;

import com.top_logic.basic.config.Decision;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.H2Helper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test that H2 uses an index for the queries of the knowledge base that search with a list of
 * identifiers, if {@link SchemaConfiguration#getBranchLast()} is set.
 *
 * <p>
 * H2 uses an <code>IN</code> list for an index lookup only if the list values are given for the
 * first column of the index. The test captures the SQL the knowledge base generates (from H2's
 * query statistics) and analyzes its execution with <code>EXPLAIN ANALYZE</code>: with the branch
 * column in front of the index, the whole table is scanned, with the branch last, only the matching
 * rows are read.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBranchLastH2IndexUsage extends AbstractDBKnowledgeBaseTest {

	/** Number of objects in the tables. */
	private static final int OBJECT_COUNT = 300;

	/** Number of objects in the identifier lists of the analyzed queries. */
	private static final int SELECTED_COUNT = 20;

	/** Value of {@link #A2_NAME} that marks the objects selected for the bulk load. */
	private static final String SELECTED = "selected";

	private static final Pattern INDEX_CONDITION = Pattern.compile("/\\* ([^:*]+): ([^*]*)\\*/");

	private static final Pattern SCAN_COUNT = Pattern.compile("/\\* scanCount: (\\d+) \\*/");

	private List<KnowledgeObject> _selected;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Transaction tx = begin();
		_selected = new ArrayList<>();
		for (int n = 0; n < OBJECT_COUNT; n++) {
			KnowledgeObject b = newB(getName() + "-b" + n);
			newAB(b, newD(getName() + "-d" + n));
			if (n % (OBJECT_COUNT / SELECTED_COUNT) == 0) {
				b.setAttributeValue(A2_NAME, SELECTED);
				_selected.add(b);
			}
		}
		commit(tx);
		assertEquals(SELECTED_COUNT, _selected.size());
	}

	@Override
	protected void tearDown() throws Exception {
		_selected = null;
		super.tearDown();
	}

	/**
	 * The search for the referrers of a list of objects, e.g. when deleting objects.
	 */
	public void testReferrerSearch() throws SQLException {
		if (!isH2()) {
			return;
		}

		startStatistics();
		try {
			List<KnowledgeItem> referers =
				kb().getAnyReferer(Revision.CURRENT, _selected.get(0).tTable(), _selected, null, KnowledgeItem.class);
			assertEquals(SELECTED_COUNT, referers.size());

			String plan = explain(sql -> sql.startsWith("SELECT") && sql.contains("FROM \"AB\"")
				&& sql.contains("\"SOURCE_ID\") IN ("));
			assertIndexUsage(plan, "SOURCE_ID");
		} finally {
			stopStatistics();
		}
	}

	/**
	 * Loading the data of objects found by a search, given as list of their identifiers.
	 */
	public void testBulkLoad() throws SQLException {
		if (!isH2()) {
			return;
		}

		// Objects of a historic revision are not in the cache and must be loaded.
		long revision = lastRevision().getCommitNumber();
		startStatistics();
		try {
			RevisionQuery<KnowledgeObject> query =
				queryUnresolved(filter(allOf(B_NAME), eqBinary(attribute(A_NAME, A2_NAME), literal(SELECTED))));
			List<KnowledgeObject> loaded = kb().search(query, revisionArgs().setRequestedRevision(revision));
			assertEquals(SELECTED_COUNT, loaded.size());

			String plan = explain(sql -> sql.startsWith("SELECT") && sql.contains("FROM \"B\"")
				&& sql.contains("\"IDENTIFIER\" IN ("));
			assertIndexUsage(plan, "IDENTIFIER");
		} finally {
			stopStatistics();
		}
	}

	private void assertIndexUsage(String plan, String listColumn) {
		Matcher indexMatcher = INDEX_CONDITION.matcher(plan);
		assertTrue("No index condition in plan: " + plan, indexMatcher.find());
		String indexCondition = indexMatcher.group(2);

		Matcher scanMatcher = SCAN_COUNT.matcher(plan);
		assertTrue("No scan count in plan: " + plan, scanMatcher.find());
		int scanCount = Integer.parseInt(scanMatcher.group(1));

		if (kb().getMORepository().branchLast()) {
			assertTrue("Identifier list not used as index condition: " + plan,
				indexCondition.contains(listColumn + " IN("));
			assertTrue("Table scanned instead of index lookup (scan count " + scanCount + "): " + plan,
				scanCount <= 2 * SELECTED_COUNT + 1);
		} else {
			assertFalse("Identifier list unexpectedly used as index condition: " + plan,
				indexCondition.contains(listColumn + " IN("));
			assertTrue("Expected a table scan (scan count " + scanCount + "): " + plan,
				scanCount >= OBJECT_COUNT);
		}
	}

	private boolean isH2() throws SQLException {
		return kb().getConnectionPool().getSQLDialect() instanceof H2Helper;
	}

	private void startStatistics() throws SQLException {
		// Disabling resets the statistics collected so far.
		execute("SET QUERY_STATISTICS FALSE");
		execute("SET QUERY_STATISTICS_MAX_ENTRIES 1000");
		execute("SET QUERY_STATISTICS TRUE");
	}

	private void stopStatistics() throws SQLException {
		execute("SET QUERY_STATISTICS FALSE");
	}

	private void execute(String sql) throws SQLException {
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			try (Statement stmt = connection.createStatement()) {
				stmt.execute(sql);
			}
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Finds the single statement executed by the knowledge base that matches the given filter and
	 * returns its <code>EXPLAIN ANALYZE</code> plan.
	 */
	private String explain(Predicate<String> filter) throws SQLException {
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			try (Statement stmt = connection.createStatement()) {
				List<String> statements = new ArrayList<>();
				try (ResultSet result =
					stmt.executeQuery("SELECT SQL_STATEMENT FROM INFORMATION_SCHEMA.QUERY_STATISTICS")) {
					while (result.next()) {
						String sql = result.getString(1);
						if (filter.test(sql)) {
							statements.add(sql);
						}
					}
				}
				assertEquals("Statements: " + statements, 1, statements.size());

				try (ResultSet plan = stmt.executeQuery("EXPLAIN ANALYZE " + statements.get(0))) {
					assertTrue(plan.next());
					return plan.getString(1);
				}
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Suite of {@link TestBranchLastH2IndexUsage}.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestBranchLastH2IndexUsage.class.getName());
		suite.addTest(new BranchLastSetup(suiteDefaultDB(TestBranchLastH2IndexUsage.class), Decision.FALSE));
		suite.addTest(new BranchLastSetup(suiteDefaultDB(TestBranchLastH2IndexUsage.class), Decision.TRUE));
		return suite;
	}

}
