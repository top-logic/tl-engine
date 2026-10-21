/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.benchmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import junit.framework.Test;

import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.element.meta.TestWithModelExtension;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.StatementAnalyzer;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.ReferenceDeletionHelper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Benchmark comparing the import of a composition tree with its deletion.
 *
 * <p>
 * A tree of {@link #DEPTH} levels below the root with a fan-out of {@link #FAN_OUT} is created in a
 * single transaction (the "import") and afterwards deleted in a second transaction. Each child
 * additionally references its parent through a non-composite reference, so that the deletion also
 * has to clear references to deleted objects.
 * </p>
 *
 * <p>
 * For each phase (creating and linking the objects, committing the import, deleting the root,
 * committing the deletion) the wall-clock time, the number of SQL statements and the time spent in
 * the database are reported to the log and to standard output. The deletion is measured with
 * {@link TLObject#tDelete()} on the root (cascading through {@link ReferenceDeletionHelper}) and with
 * the bulk entry point {@link KBUtils#deleteAll(Iterable)}.
 * </p>
 *
 * <p>
 * Each phase is executed in a method of its own (<code>phase...</code>), so that the samples of a
 * profiler run can be attributed to the phase by the frames of their stack.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DeactivatedTest("Benchmark that must not be executed during the nightly build.")
public class TestBenchmarkDeletion extends TestWithModelExtension {

	private static final String MODULE = TestBenchmarkDeletion.class.getName();

	private static final String NODE_TYPE = "Node";

	private static final String NAME_ATTR = "name";

	private static final String INDEX_ATTR = "index";

	private static final String CHILDREN_ATTR = "children";

	private static final String RELATED_ATTR = "related";

	/**
	 * Number of levels below the root.
	 */
	private static final int DEPTH = 4;

	/**
	 * Number of children of each inner node.
	 */
	private static final int FAN_OUT = 10;

	/**
	 * Number of most expensive statements reported per phase.
	 */
	private static final int TOP_STATEMENTS = 5;

	/**
	 * Upper bound for a single phase that only detects a completely broken setup. Not a performance
	 * requirement.
	 */
	private static final long SANITY_MS = 10 * 60 * 1000;

	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private static final Pattern SELECT_COLUMNS = Pattern.compile("^SELECT .*? FROM ");

	private TLClass _nodeType;

	private TLStructuredTypePart _name;

	private TLStructuredTypePart _index;

	private TLStructuredTypePart _children;

	private TLStructuredTypePart _related;

	private TLFactory _factory;

	private ConnectionPool _pool;

	private LoggingDataSourceProxy _dataSource;

	private StatementAnalyzer _analyzerBefore;

	private final StatementStatistics _statistics = new StatementStatistics();

	private final List<Measurement> _measurements = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_nodeType = (TLClass) TLModelUtil.findType(TLModelUtil.findModule(MODULE), NODE_TYPE);
		_name = _nodeType.getPart(NAME_ATTR);
		_index = _nodeType.getPart(INDEX_ATTR);
		_children = _nodeType.getPart(CHILDREN_ATTR);
		_related = _nodeType.getPart(RELATED_ATTR);
		_factory = DynamicModelService.getFactoryFor(MODULE);

		_pool = ((DBKnowledgeBase) _kb).getConnectionPool();
		if (_pool.getDataSource() instanceof LoggingDataSourceProxy dataSource) {
			_dataSource = dataSource;
			_analyzerBefore = dataSource.getAnalyzer();
			dataSource.setAnalyzer(_statistics);
			// Only connections created while an analyzer is installed report their statements.
			_pool.clear();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (_dataSource != null) {
			_dataSource.setAnalyzer(_analyzerBefore);
			_pool.clear();
			_dataSource = null;
		}
		_measurements.clear();
		super.tearDown();
	}

	/**
	 * Imports and deletes the tree several times with the different deletion entry points.
	 */
	public void testDeletion() {
		int instancesBefore = instanceCount();

		for (int run = 1; run <= 2; run++) {
			String label = "tDelete #" + run;
			List<TLObject> nodes = importTree(label);
			try (Transaction tx = beginTX()) {
				measure(label, "tDelete", () -> phaseDelete(nodes.get(0)));
				measure(label, "delete-commit", () -> phaseDeleteCommit(tx));
			}
			assertDeleted(nodes, instancesBefore);
		}

		{
			String label = "deleteAll(children)";
			List<TLObject> nodes = importTree(label);
			try (Transaction tx = beginTX()) {
				measure(label, "deleteAll", () -> phaseDeleteAllChildren(nodes.get(0)));
				measure(label, "delete-commit", () -> phaseDeleteCommit(tx));
			}
			assertDeleted(nodes, instancesBefore);
		}

		{
			String label = "deleteAll(all nodes)";
			List<TLObject> nodes = importTree(label);
			try (Transaction tx = beginTX()) {
				measure(label, "deleteAll", () -> phaseDeleteAllNodes(nodes));
				measure(label, "delete-commit", () -> phaseDeleteCommit(tx));
			}
			assertDeleted(nodes, instancesBefore);
		}

		report();
	}

	private List<TLObject> importTree(String label) {
		try (Transaction tx = beginTX()) {
			List<TLObject> nodes = measure(label, "create", () -> phaseCreate(label));
			measure(label, "import-commit", () -> phaseImportCommit(tx));
			return nodes;
		}
	}

	private List<TLObject> phaseCreate(String label) {
		List<TLObject> nodes = new ArrayList<>();
		TLObject root = createNode(nodes, null, label, 0);
		List<TLObject> level = List.of(root);
		for (int depth = 1; depth <= DEPTH; depth++) {
			List<TLObject> nextLevel = new ArrayList<>(level.size() * FAN_OUT);
			for (TLObject parent : level) {
				String parentName = (String) parent.tValue(_name);
				List<TLObject> children = new ArrayList<>(FAN_OUT);
				for (int n = 0; n < FAN_OUT; n++) {
					children.add(createNode(nodes, parent, parentName + "." + n, n));
				}
				parent.tUpdate(_children, children);
				nextLevel.addAll(children);
			}
			level = nextLevel;
		}
		return nodes;
	}

	private TLObject createNode(List<TLObject> nodes, TLObject parent, String name, int index) {
		TLObject node = _factory.createObject(_nodeType);
		node.tUpdate(_name, name);
		node.tUpdate(_index, Long.valueOf(index));
		if (parent != null) {
			node.tUpdate(_related, parent);
		}
		nodes.add(node);
		return node;
	}

	private Void phaseImportCommit(Transaction tx) {
		tx.commit();
		return null;
	}

	private Void phaseDelete(TLObject root) {
		root.tDelete();
		return null;
	}

	private Void phaseDeleteAllChildren(TLObject root) {
		@SuppressWarnings("unchecked")
		List<TLObject> children = new ArrayList<>((List<TLObject>) root.tValue(_children));
		KBUtils.deleteAll(children);
		root.tDelete();
		return null;
	}

	private Void phaseDeleteAllNodes(List<TLObject> nodes) {
		KBUtils.deleteAll(nodes);
		return null;
	}

	private Void phaseDeleteCommit(Transaction tx) {
		tx.commit();
		return null;
	}

	private void assertDeleted(List<TLObject> nodes, int instancesBefore) {
		for (int n = 0; n < nodes.size(); n += 1000) {
			assertFalse("Node not deleted: " + n, nodes.get(n).tValid());
		}
		assertFalse(nodes.get(nodes.size() - 1).tValid());
		assertEquals("Instances left after deletion.", instancesBefore, instanceCount());
	}

	private int instanceCount() {
		return MetaElementUtil.getAllDirectInstancesOf(_nodeType, TLObject.class).size();
	}

	private <T> T measure(String run, String phase, Supplier<T> operation) {
		_statistics.reset();
		long start = System.nanoTime();
		T result = operation.get();
		long elapsed = System.nanoTime() - start;
		Measurement measurement = new Measurement(run, phase, elapsed / 1_000_000, _statistics.statements(),
			_statistics.dbNanos() / 1_000_000, _statistics.top(TOP_STATEMENTS));
		_measurements.add(measurement);
		output("Deletion benchmark " + run + " / " + phase + ": " + measurement.ms() + " ms");

		assertTrue("Phase too slow: " + measurement, measurement.ms() < SANITY_MS);
		return result;
	}

	private void report() {
		StringBuilder table = new StringBuilder();
		table.append("Deletion benchmark (" + nodeCount() + " nodes, depth " + DEPTH + ", fan-out " + FAN_OUT
			+ ", statements counted: " + (_dataSource != null) + ")\n");
		table.append(String.format("%-22s %-14s %10s %12s %10s%n", "run", "phase", "ms", "statements", "db ms"));
		for (Measurement measurement : _measurements) {
			table.append(String.format("%-22s %-14s %10d %12d %10d%n", measurement.run(), measurement.phase(),
				measurement.ms(), measurement.statements(), measurement.dbMs()));
		}
		for (Measurement measurement : _measurements) {
			table.append("\nTop statements of " + measurement.run() + " / " + measurement.phase() + ":\n");
			for (StatementStatistics.Entry entry : measurement.top()) {
				table.append(String.format("  %8d ms %8d calls  %s%n", entry.nanos() / 1_000_000, entry.calls(),
					entry.sql()));
			}
		}
		output(table.toString());
	}

	private static int nodeCount() {
		int result = 0;
		int levelSize = 1;
		for (int depth = 0; depth <= DEPTH; depth++) {
			result += levelSize;
			levelSize *= FAN_OUT;
		}
		return result;
	}

	private static void output(String message) {
		Logger.info(message, TestBenchmarkDeletion.class);
		System.out.println(message);
	}

	/**
	 * Result of a single benchmark phase.
	 *
	 * @param run
	 *        The benchmark run the phase belongs to.
	 * @param phase
	 *        The name of the phase.
	 * @param ms
	 *        The wall-clock time of the phase in milliseconds.
	 * @param statements
	 *        The number of SQL statements sent to the database.
	 * @param dbMs
	 *        The time spent in the database in milliseconds.
	 * @param top
	 *        The most expensive statements of the phase.
	 */
	private record Measurement(String run, String phase, long ms, long statements, long dbMs,
			List<StatementStatistics.Entry> top) {
		// Pure data.
	}

	/**
	 * {@link StatementAnalyzer} accumulating the statements of a benchmark phase.
	 */
	private static final class StatementStatistics implements StatementAnalyzer {

		private final AtomicLong _statements = new AtomicLong();

		private final AtomicLong _nanos = new AtomicLong();

		private final Map<String, Entry> _bySql = new ConcurrentHashMap<>();

		/**
		 * Accumulated statistics of statements with the same normalized SQL.
		 *
		 * @param sql
		 *        The normalized SQL.
		 * @param calls
		 *        The number of calls hitting the database.
		 * @param nanos
		 *        The accumulated time in nanoseconds.
		 */
		record Entry(String sql, long calls, long nanos) {
			// Pure data.
		}

		@Override
		public void log(String sql, long elapsed, long rows, int calls) {
			_statements.addAndGet(calls);
			_nanos.addAndGet(elapsed);
			String key = normalize(sql);
			_bySql.merge(key, new Entry(key, calls, elapsed),
				(a, b) -> new Entry(key, a.calls() + b.calls(), a.nanos() + b.nanos()));
		}

		void reset() {
			_statements.set(0);
			_nanos.set(0);
			_bySql.clear();
		}

		long statements() {
			return _statements.get();
		}

		long dbNanos() {
			return _nanos.get();
		}

		List<Entry> top(int count) {
			return _bySql.values().stream()
				.sorted(Comparator.comparingLong(Entry::nanos).reversed())
				.limit(count)
				.toList();
		}

		/**
		 * Statement source up to the start of its value or parameter lists, so that statements
		 * that only differ in the inlined identifiers are counted together.
		 */
		private static String normalize(String sql) {
			int end = Math.min(cut(sql, " IN ("), cut(sql, " VALUES "));
			String result = WHITESPACE.matcher(sql.substring(0, Math.min(end, 600))).replaceAll(" ").trim();
			result = SELECT_COLUMNS.matcher(result).replaceFirst("SELECT ... FROM ");
			return end < sql.length() ? result + " ..." : result;
		}

		private static int cut(String sql, String marker) {
			int index = sql.indexOf(marker);
			return index < 0 ? sql.length() : index + marker.length();
		}
	}

	/**
	 * The test suite with the element setup and the benchmark model.
	 */
	public static Test suite() {
		return suite(new ModelExtensionTestSetup(TestBenchmarkDeletion.class));
	}

}
