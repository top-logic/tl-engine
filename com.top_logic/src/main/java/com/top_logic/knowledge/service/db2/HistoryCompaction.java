/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.table;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.KnowledgeReferenceStorageImpl;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.util.TLContext;

/**
 * Collapses the history of a {@link DBKnowledgeBase} below a given revision into that revision.
 *
 * <p>
 * {@link #compactRevisions(long, long, Log) Compacting} the revision range {@code (lower, upper)}
 * discards every intermediate state a versioned object had in a revision {@code R} with
 * {@code lower < R < upper}. The state visible at {@code lower} and the state visible at
 * {@code upper} are preserved exactly, revision numbers are never renumbered, and revisions above
 * {@code upper} are untouched. {@link #compactHistory(long, Log) Compacting the history} before a
 * date is the special case {@code lower == 0}, where {@code upper} is the newest revision committed
 * at or before that date.
 * </p>
 *
 * <p>
 * The operation works with plain SQL on the connection pool of the {@link KnowledgeBase}, not
 * through a {@link KnowledgeBase} transaction. Each step commits on its own and each step is
 * idempotent, so an aborted run can simply be repeated. A {@link KnowledgeBase} instance that was
 * running while its database was compacted keeps outdated revisions in its caches and must be
 * restarted.
 * </p>
 *
 * <p>
 * Rows are physically deleted, so the operation must not run while another node of a cluster still
 * needs the discarded revisions.
 * </p>
 * 
 * <p>
 * A reference pinned to a discarded revision is moved to the compaction revision. When its target
 * object has no state there, because it was deleted within the compacted range, the reference is
 * cleared to the value a reference without a target has. Reading such an attribute then answers
 * nothing instead of failing.
 * </p>
 *
 * @see HistoryCleanup Dropping the complete history of unversioned tables.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoryCompaction {

	/**
	 * Default number of revisions whose rows are deleted within a single transaction.
	 *
	 * @see #setDeleteWindowSize(long)
	 */
	public static final long DEFAULT_DELETE_WINDOW_SIZE = 5000;

	/**
	 * The author written to the compaction revision.
	 *
	 * <p>
	 * The same value the {@link KnowledgeBase} writes for a commit that happens in a system
	 * context.
	 * </p>
	 *
	 * @see ThreadContextManager#systemContextId(Class)
	 */
	public static final String COMPACTION_AUTHOR = ThreadContextManager.systemContextId(HistoryCompaction.class);

	private static final String PARAM_LOWER = "lower";

	private static final String PARAM_UPPER = "upper";

	private static final String PARAM_UPPER_LAST = "upperLast";

	private static final String PARAM_WINDOW_START = "windowStart";

	private static final String PARAM_WINDOW_STOP = "windowStop";

	private static final String PARAM_DATE = "date";

	private static final String PARAM_AUTHOR = "author";

	private static final String PARAM_IDS = "ids";

	private static final String PARAM_ID = "id";

	private static final String PARAM_BRANCH = "branch";

	private static final String PARAM_REV_MAX = "revMax";

	private static final String PARAM_LOG = "log";

	private static final String RESULT_REVISION = "rev";

	private static final String RESULT_COUNT = "cnt";

	private static final String RESULT_AUTHOR = "author";

	private static final String RESULT_BRANCH = "branch";

	private static final String RESULT_ID = "id";

	private static final String RESULT_REV_MAX = "revMax";

	private static final String RESULT_TARGET_BRANCH = "targetBranch";

	private static final String RESULT_TARGET_ID = "targetId";

	private static final String RESULT_TARGET_TYPE = "targetType";

	/** Result column index of {@link #RESULT_BRANCH} in the pin candidate query. */
	private static final int CANDIDATE_RESULT_BRANCH = 1;

	/** Result column index of {@link #RESULT_ID} in the pin candidate query. */
	private static final int CANDIDATE_RESULT_ID = 2;

	/** Result column index of {@link #RESULT_REV_MAX} in the pin candidate query. */
	private static final int CANDIDATE_RESULT_REV_MAX = 3;

	/** Result column index of {@link #RESULT_TARGET_BRANCH} in the pin candidate query. */
	private static final int CANDIDATE_RESULT_TARGET_BRANCH = 4;

	/** Result column index of {@link #RESULT_TARGET_ID} in the pin candidate query. */
	private static final int CANDIDATE_RESULT_TARGET_ID = 5;

	/** Result column index of {@link #RESULT_TARGET_TYPE} in the pin candidate query. */
	private static final int CANDIDATE_RESULT_TARGET_TYPE = 6;

	/** Result column index of {@link #RESULT_BRANCH} in the target existence query. */
	private static final int TARGET_RESULT_BRANCH = 1;

	/** Result column index of {@link #RESULT_ID} in the target existence query. */
	private static final int TARGET_RESULT_ID = 2;

	private final DBHelper _sqlDialect;

	private final ConnectionPool _pool;

	private final MORepository _repository;

	private final ItemTables _tables;

	private final MOClass _revisionType;

	private final DBAttribute _revisionRev;

	private final DBAttribute _revisionDate;

	private final DBAttribute _revisionAuthor;

	private final DBAttribute _revisionLog;

	private final DBTableMetaObject _xrefType;

	private final DBAttribute _xrefRev;

	private final DBAttribute _xrefBranch;

	private final DBAttribute _xrefTypeName;

	private final MOClass _branchType;

	private final DBAttribute _branchBaseRev;

	private final DBAttribute _branchCreateRev;

	private final List<TableAccess> _itemTableAccess;

	private final Map<String, TableAccess> _itemTableByName;

	private final TableAccess _flexDataAccess;

	private final CompiledStatement _resolveRevision;

	private final CompiledStatement _revisionExists;

	private final CompiledStatement _deleteRevisions;

	private final CompiledStatement _countRevisions;

	private final CompiledStatement _fetchAuthor;

	private final CompiledStatement _updateRevisionLog;

	private final CompiledStatement _deleteXrefBelow;

	private final CompiledStatement _countXrefBelow;

	private final CompiledStatement _deleteXrefAt;

	private final CompiledStatement _updateBranchBaseRev;

	private final CompiledStatement _countBranchBaseRev;

	private final CompiledStatement _updateBranchCreateRev;

	private final CompiledStatement _countBranchCreateRev;

	private long _deleteWindowSize = DEFAULT_DELETE_WINDOW_SIZE;

	/**
	 * Creates a {@link HistoryCompaction}.
	 *
	 * @param sqlDialect
	 *        The dialect to compile the statements for.
	 * @param pool
	 *        The pool to get connections from.
	 * @param repository
	 *        The types of the {@link KnowledgeBase} whose history is compacted.
	 */
	public HistoryCompaction(DBHelper sqlDialect, ConnectionPool pool, MORepository repository) {
		_sqlDialect = sqlDialect;
		_pool = pool;
		_repository = repository;
		_tables = new ItemTables(repository);

		_revisionType = BasicTypes.getRevisionType(repository);
		_revisionRev = RevisionType.getRevisionAttribute(_revisionType);
		_revisionDate = RevisionType.getDateAttribute(_revisionType);
		_revisionAuthor = RevisionType.getAuthorAttribute(_revisionType);
		_revisionLog = RevisionType.getLogAttribute(_revisionType);

		_xrefType = (DBTableMetaObject) repository.getTypeOrNull(RevisionXref.REVISION_XREF_TYPE_NAME);
		_xrefRev = dbColumn(_xrefType, RevisionXref.XREF_REV_ATTRIBUTE);
		_xrefBranch = dbColumn(_xrefType, RevisionXref.XREF_BRANCH_ATTRIBUTE);
		_xrefTypeName = dbColumn(_xrefType, RevisionXref.XREF_TYPE_ATTRIBUTE);

		_branchType = BasicTypes.getBranchType(repository);
		_branchBaseRev = BranchSupport.getBaseRevAttr(_branchType);
		_branchCreateRev = BranchSupport.getCreateRevAttr(_branchType);

		List<TableAccess> itemTables = new ArrayList<>();
		Map<String, TableAccess> itemTableByName = new HashMap<>();
		for (ItemTables.Table table : _tables.getItemTables()) {
			TableAccess access = new TableAccess(table);
			itemTables.add(access);
			itemTableByName.put(table.getType().getName(), access);
		}
		_itemTableAccess = Collections.unmodifiableList(itemTables);
		_itemTableByName = itemTableByName;
		ItemTables.Table flexData = _tables.getFlexData();
		_flexDataAccess = flexData == null ? null : new TableAccess(flexData);

		_resolveRevision = createResolveRevision();
		_revisionExists = createRevisionExists();
		_deleteRevisions = createDeleteRevisions();
		_countRevisions = createCountRevisions();
		_fetchAuthor = createFetchAuthor();
		_updateRevisionLog = createUpdateRevisionLog();
		_deleteXrefBelow = createDeleteXrefBelow();
		_countXrefBelow = createCountXrefBelow();
		_deleteXrefAt = createDeleteXrefAt();
		_updateBranchBaseRev = createRaiseRevision((DBTableMetaObject) _branchType, _branchBaseRev);
		_countBranchBaseRev = createCountInRange((DBTableMetaObject) _branchType, _branchBaseRev);
		_updateBranchCreateRev = createRaiseRevision((DBTableMetaObject) _branchType, _branchCreateRev);
		_countBranchCreateRev = createCountInRange((DBTableMetaObject) _branchType, _branchCreateRev);
	}

	/**
	 * Creates a {@link HistoryCompaction} for the given {@link KnowledgeBase}.
	 *
	 * @param kb
	 *        The {@link KnowledgeBase} whose connection pool, SQL dialect and types the operation
	 *        works on.
	 */
	public static HistoryCompaction newInstance(DBKnowledgeBase kb) {
		return new HistoryCompaction(kb.dbHelper, kb.getConnectionPool(), kb.getMORepository());
	}

	private static DBAttribute dbColumn(DBTableMetaObject table, String attributeName) {
		return table.getAttributeOrNull(attributeName).getDbMapping()[0];
	}

	/**
	 * Number of revisions whose rows are deleted within a single transaction.
	 *
	 * @see #DEFAULT_DELETE_WINDOW_SIZE
	 */
	public long getDeleteWindowSize() {
		return _deleteWindowSize;
	}

	/**
	 * Setter for {@link #getDeleteWindowSize()}.
	 *
	 * <p>
	 * Deleting the rows of a long history in a single statement can make the commit of the deleting
	 * transaction run for hours. The delete therefore advances in windows over the last revision a
	 * row is valid in, committing after each window.
	 * </p>
	 *
	 * @param windowSize
	 *        The number of revisions to process per transaction, must be positive.
	 */
	public void setDeleteWindowSize(long windowSize) {
		if (windowSize < 1) {
			throw new IllegalArgumentException("A delete window must span at least one revision: " + windowSize);
		}
		_deleteWindowSize = windowSize;
	}

	/**
	 * The tables processed by this operation.
	 */
	public ItemTables getTables() {
		return _tables;
	}

	/**
	 * The newest revision that was committed at or before the given point in time.
	 *
	 * @param beforeDate
	 *        Point in time in milliseconds since the epoch.
	 * @return The commit number of that revision, or {@code 0} if no revision was committed that
	 *         early.
	 */
	public long resolveCompactionRevision(long beforeDate) throws SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try {
			try (ResultSet result = _resolveRevision.executeQuery(connection, Long.valueOf(beforeDate))) {
				if (!result.next()) {
					return 0;
				}
				// The maximum is null when no revision matches.
				long revision = result.getLong(RESULT_REVISION);
				return result.wasNull() ? 0 : revision;
			}
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Collapses all revisions committed before the given point in time into the newest revision
	 * committed at or before that point in time.
	 *
	 * @param beforeDate
	 *        Point in time in milliseconds since the epoch.
	 * @param log
	 *        Receives progress information.
	 * @return What was changed.
	 *
	 * @see #resolveCompactionRevision(long)
	 * @see #compactRevisions(long, long, Log)
	 */
	public Report compactHistory(long beforeDate, Log log) throws SQLException {
		return compactRevisions(0, resolveCompactionRevision(beforeDate), log);
	}

	/**
	 * Counts what {@link #compactHistory(long, Log)} would change, without changing anything.
	 */
	public Report analyzeHistory(long beforeDate, Log log) throws SQLException {
		return analyzeRevisions(0, resolveCompactionRevision(beforeDate), log);
	}

	/**
	 * Collapses all revisions between the given bounds into the upper bound.
	 *
	 * <p>
	 * Every revision {@code R} with {@code lower < R < upper} is discarded: rows that were created
	 * and outdated within that range are deleted, rows that survive are stretched to start at
	 * {@code upper}, rows that were visible at {@code lower} and outdated within the range end at
	 * {@code upper - 1}, and references pinned to a discarded revision are moved to {@code upper}.
	 * The state visible at {@code lower} and at {@code upper} is preserved exactly.
	 * </p>
	 *
	 * @param lower
	 *        The exclusive lower bound, {@code 0} to compact from the beginning of the history.
	 * @param upper
	 *        The compaction revision, an existing revision.
	 * @param log
	 *        Receives progress information.
	 * @return What was changed.
	 */
	public Report compactRevisions(long lower, long upper, Log log) throws SQLException {
		return run(lower, upper, log, false);
	}

	/**
	 * Counts what {@link #compactRevisions(long, long, Log)} would change, without changing
	 * anything.
	 *
	 * <p>
	 * The counts are the ones the corresponding compaction reports when run on the same database
	 * state.
	 * </p>
	 */
	public Report analyzeRevisions(long lower, long upper, Log log) throws SQLException {
		return run(lower, upper, log, true);
	}

	private Report run(long lower, long upper, Log log, boolean dryRun) throws SQLException {
		if (lower < 0) {
			throw new IllegalArgumentException("Lower bound must not be negative: " + lower);
		}
		Report report = new Report(dryRun, lower, upper);
		if (upper <= lower + 1) {
			log.info("Nothing to compact in revision range (" + lower + ", " + upper + ").");
			return report;
		}
		PooledConnection connection = _pool.borrowWriteConnection();
		try {
			checkRevisionExists(connection, upper);

			log.info((dryRun ? "Analyzing" : "Compacting") + " revisions (" + lower + ", " + upper + ").");
			for (TableAccess table : _itemTableAccess) {
				table.pinReferences(connection, report, lower, upper, dryRun, log);
			}
			for (TableAccess table : _itemTableAccess) {
				table.compact(connection, report, lower, upper, dryRun, log);
			}
			if (_flexDataAccess != null) {
				_flexDataAccess.compact(connection, report, lower, upper, dryRun, log);
			}
			compactBranches(connection, report, lower, upper, dryRun);
			compactRevisionTable(connection, report, lower, upper, dryRun);
			rebuildXref(connection, report, lower, upper, dryRun);
			log.info(report.toString());
		} finally {
			_pool.releaseWriteConnection(connection);
		}
		return report;
	}

	private void checkRevisionExists(PooledConnection connection, long upper) throws SQLException {
		try (ResultSet result = _revisionExists.executeQuery(connection, Long.valueOf(upper))) {
			if (!result.next()) {
				throw new IllegalArgumentException("No such revision: " + upper);
			}
		}
	}

	/**
	 * Raises the revisions a {@link Branch} refers to out of the discarded range.
	 * 
	 * <p>
	 * The columns of the branch table are single revisions, not a revision range, so this table
	 * needs a step of its own.
	 * </p>
	 */
	private void compactBranches(PooledConnection connection, Report report, long lower, long upper, boolean dryRun)
			throws SQLException {
		TableReport tableReport = report.table(((DBTableMetaObject) _branchType).getDBName());
		Long lowerArg = Long.valueOf(lower);
		Long upperArg = Long.valueOf(upper);
		if (dryRun) {
			tableReport.addRewrittenRows(countRows(connection, _countBranchBaseRev, lowerArg, upperArg));
			tableReport.addRewrittenRows(countRows(connection, _countBranchCreateRev, lowerArg, upperArg));
		} else {
			tableReport.addRewrittenRows(_updateBranchBaseRev.executeUpdate(connection, lowerArg, upperArg));
			tableReport.addRewrittenRows(_updateBranchCreateRev.executeUpdate(connection, lowerArg, upperArg));
			connection.commit();
		}
	}

	private void compactRevisionTable(PooledConnection connection, Report report, long lower, long upper,
			boolean dryRun) throws SQLException {
		TableReport tableReport = report.table(((DBTableMetaObject) _revisionType).getDBName());
		Long lowerArg = Long.valueOf(lower);
		Long upperArg = Long.valueOf(upper);
		if (dryRun) {
			tableReport.addDeletedRows(countRows(connection, _countRevisions, lowerArg, upperArg));
			if (!COMPACTION_AUTHOR.equals(fetchAuthor(connection, upper))) {
				tableReport.addRewrittenRows(1);
			}
		} else {
			tableReport.addDeletedRows(_deleteRevisions.executeUpdate(connection, lowerArg, upperArg));
			if (!COMPACTION_AUTHOR.equals(fetchAuthor(connection, upper))) {
				String encodedLog = ResKey.encode(I18NConstants.HISTORY_COMPACTED);
				tableReport
					.addRewrittenRows(_updateRevisionLog.executeUpdate(connection, upperArg, COMPACTION_AUTHOR,
						encodedLog));
			}
			connection.commit();
		}
	}

	private String fetchAuthor(PooledConnection connection, long revision) throws SQLException {
		try (ResultSet result = _fetchAuthor.executeQuery(connection, Long.valueOf(revision))) {
			if (!result.next()) {
				return null;
			}
			return result.getString(RESULT_AUTHOR);
		}
	}

	/**
	 * Drops the cross reference entries of the discarded revisions and re-computes the entries of
	 * the compaction revision.
	 *
	 * <p>
	 * The rows of the item tables are sufficient to reconstruct the cross reference: a change of a
	 * dynamic attribute value touches its object, so the object's row is rewritten in the same
	 * revision as its flexible data.
	 * </p>
	 */
	private void rebuildXref(PooledConnection connection, Report report, long lower, long upper, boolean dryRun)
			throws SQLException {
		TableReport tableReport = report.table(_xrefType.getDBName());
		Long lowerArg = Long.valueOf(lower);
		Long upperArg = Long.valueOf(upper);
		if (dryRun) {
			tableReport.addDeletedRows(countRows(connection, _countXrefBelow, lowerArg, upperArg));
			return;
		}
		tableReport.addDeletedRows(_deleteXrefBelow.executeUpdate(connection, lowerArg, upperArg));
		_deleteXrefAt.executeUpdate(connection, upperArg);
		int inserted = 0;
		for (TableAccess table : _itemTableAccess) {
			inserted += createXrefInsert(table.getTable(), upper).executeUpdate(connection);
		}
		report.addXrefRowsRebuilt(inserted);
		connection.commit();
	}

	private int countRows(PooledConnection connection, CompiledStatement statement, Object... arguments)
			throws SQLException {
		try (ResultSet result = statement.executeQuery(connection, arguments)) {
			if (!result.next()) {
				return 0;
			}
			return result.getInt(RESULT_COUNT);
		}
	}

	/**
	 * Access to a single table of the {@link ItemTables}, with all statements compiled once.
	 */
	private final class TableAccess {

		private final ItemTables.Table _table;

		private final PinAccess[] _pins;

		private final CompiledStatement _targetExists;

		private final CompiledStatement _deleteWindow;

		private final CompiledStatement _countDeleted;

		private final CompiledStatement _raiseRevMin;

		private final CompiledStatement _countRevMin;

		private final CompiledStatement _lowerRevMax;

		private final CompiledStatement _countRevMax;

		private final CompiledStatement _raiseRevCreate;

		private final CompiledStatement _countRevCreate;

		TableAccess(ItemTables.Table table) {
			_table = table;
			DBTableMetaObject type = table.getType();
			List<ItemTables.Reference> references = table.getPinnedReferences();
			_pins = new PinAccess[references.size()];
			for (int n = 0, cnt = references.size(); n < cnt; n++) {
				_pins[n] = new PinAccess(table, references.get(n));
			}
			_targetExists = createTargetExists(table);
			_deleteWindow = createDeleteWindow(table);
			_countDeleted = createCountDeleted(table);
			_raiseRevMin = createRaiseRevMin(table);
			_countRevMin = createCountRevMin(table);
			_lowerRevMax = createLowerRevMax(table);
			_countRevMax = createCountRevMax(table);
			DBAttribute revCreate = table.getRevCreate();
			_raiseRevCreate = revCreate == null ? null : createRaiseRevision(type, revCreate);
			_countRevCreate = revCreate == null ? null : createCountRevCreate(table);
		}

		ItemTables.Table getTable() {
			return _table;
		}

		/**
		 * Moves references pinned to a discarded revision to the compaction revision and clears
		 * those that then point to an object that does not exist there.
		 *
		 * <p>
		 * The markers that a pin column can hold besides a revision number lie outside of every
		 * range by construction and therefore need no special treatment:
		 * {@link KnowledgeReferenceStorageImpl#MIXED_REFERENCE_CURRENT_REPRESENTATION} marks a
		 * {@link HistoryType#MIXED} reference that points to the current object and is
		 * {@link Revision#CURRENT_REV}, which is above every existing revision, while
		 * {@link KnowledgeReferenceStorageImpl#NULL_REPLACEMENT} marks a reference without a value
		 * and is {@code 0}, which is not above the lower bound.
		 * </p>
		 */
		void pinReferences(PooledConnection connection, Report report, long lower, long upper, boolean dryRun,
				Log log) throws SQLException {
			if (_pins.length == 0) {
				return;
			}
			TableReport tableReport = report.table(_table.getDBName());
			Long lowerArg = Long.valueOf(lower);
			Long upperArg = Long.valueOf(upper);
			for (PinAccess pin : _pins) {
				if (dryRun) {
					tableReport.addRewrittenPins(countRows(connection, pin.countRaise(), lowerArg, upperArg));
				} else {
					tableReport.addRewrittenPins(pin.raise().executeUpdate(connection, lowerArg, upperArg));
				}
			}
			for (PinAccess pin : _pins) {
				tableReport.addClearedPins(pin.clearDangling(connection, lower, upper, dryRun, log));
			}
			if (!dryRun) {
				connection.commit();
			}
		}

		/**
		 * The subset of the given identifiers that have a row of this table valid in the given
		 * revision, as {@link ObjectBranchId}s of this table's type.
		 */
		Set<ObjectBranchId> existingAt(PooledConnection connection, List<TLID> ids, long upper) throws SQLException {
			Set<ObjectBranchId> result = new HashSet<>();
			Long upperArg = Long.valueOf(upper);
			int maxSetSize = _sqlDialect.getMaxSetSize();
			for (int start = 0, size = ids.size(); start < size; start += maxSetSize) {
				List<TLID> chunk = ids.subList(start, Math.min(start + maxSetSize, size));
				try (ResultSet dbResult = _targetExists.executeQuery(connection, chunk, upperArg)) {
					while (dbResult.next()) {
						result.add(new ObjectBranchId(dbResult.getLong(TARGET_RESULT_BRANCH), _table.getType(),
							IdentifierUtil.getId(dbResult, TARGET_RESULT_ID)));
					}
				}
			}
			return result;
		}

		void compact(PooledConnection connection, Report report, long lower, long upper, boolean dryRun, Log log)
				throws SQLException {
			TableReport tableReport = report.table(_table.getDBName());
			Long lowerArg = Long.valueOf(lower);
			Long upperArg = Long.valueOf(upper);
			Long upperLastArg = Long.valueOf(upper - 1);
			if (dryRun) {
				tableReport.addDeletedRows(countRows(connection, _countDeleted, lowerArg, upperArg));
				tableReport.addRewrittenRows(countRows(connection, _countRevMin, lowerArg, upperArg));
				tableReport.addRewrittenRows(countRows(connection, _countRevMax, lowerArg, upperArg));
				if (_countRevCreate != null) {
					tableReport.addRewrittenRows(countRows(connection, _countRevCreate, lowerArg, upperArg));
				}
				return;
			}

			// Delete in windows over the last revision a row is valid in, committing after each
			// window: a single delete over a long history can make the commit run for hours.
			for (long windowStart = lower + 1; windowStart < upper; windowStart += _deleteWindowSize) {
				long windowStop = Math.min(windowStart + _deleteWindowSize, upper);
				int deleted = _deleteWindow.executeUpdate(connection, lowerArg,
					Long.valueOf(windowStart), Long.valueOf(windowStop));
				connection.commit();
				if (deleted > 0) {
					tableReport.addDeletedRows(deleted);
					log.info("Deleted " + deleted + " rows of '" + _table.getDBName() + "' in revisions ["
						+ windowStart + ", " + windowStop + ").", Log.VERBOSE);
				}
			}

			tableReport.addRewrittenRows(_raiseRevMin.executeUpdate(connection, lowerArg, upperArg));
			tableReport.addRewrittenRows(_lowerRevMax.executeUpdate(connection, lowerArg, upperArg, upperLastArg));
			if (_raiseRevCreate != null) {
				tableReport.addRewrittenRows(_raiseRevCreate.executeUpdate(connection, lowerArg, upperArg));
			}
			connection.commit();
		}
	}

	/**
	 * Access to a single revision-pinning reference of an item table.
	 * 
	 * @see ItemTables.Table#getPinnedReferences()
	 */
	private final class PinAccess {

		private final ItemTables.Table _table;

		private final ItemTables.Reference _reference;

		private final String _monomorphicTarget;

		private final CompiledStatement _raise;

		private final CompiledStatement _countRaise;

		private final CompiledStatement _selectCandidates;

		private final CompiledStatement _clear;

		PinAccess(ItemTables.Table table, ItemTables.Reference reference) {
			_table = table;
			_reference = reference;
			_monomorphicTarget = reference.getMonomorphicTargetType();
			DBAttribute revColumn = reference.getRevisionColumn();
			_raise = createRaiseRevision(table.getType(), revColumn);
			_countRaise = createCountInRange(table.getType(), revColumn);
			_selectCandidates = createSelectPinCandidates(table, reference);
			_clear = createClearPin(table, reference);
		}

		CompiledStatement raise() {
			return _raise;
		}

		CompiledStatement countRaise() {
			return _countRaise;
		}

		/**
		 * Clears the pins of the surviving rows that point to an object without a state in the
		 * compaction revision.
		 * 
		 * <p>
		 * Rows that lie completely inside the compacted range are left alone, because the delete
		 * step removes them.
		 * </p>
		 * 
		 * <p>
		 * The candidate rows are held in memory while their targets are looked up. Their number is
		 * the number of surviving values of this reference that point to the compaction revision,
		 * which is bounded by the number of pins this run moves plus the ones that already pointed
		 * there.
		 * </p>
		 * 
		 * @return The number of pins that were (or would be) cleared.
		 */
		int clearDangling(PooledConnection connection, long lower, long upper, boolean dryRun, Log log)
				throws SQLException {
			List<PinCandidate> candidates = fetchCandidates(connection, lower, upper);
			if (candidates.isEmpty()) {
				return 0;
			}
			List<PinCandidate> dangling = selectDangling(connection, candidates, upper, log);
			if (dangling.isEmpty() || dryRun) {
				return dangling.size();
			}
			clear(connection, dangling);
			log.info("Cleared " + dangling.size() + " dangling values of '" + _reference.getName() + "' in '"
				+ _table.getDBName() + "'.", Log.VERBOSE);
			return dangling.size();
		}

		/**
		 * The rows that outlive the run and whose pin points to the compaction revision after the
		 * raise.
		 * 
		 * <p>
		 * The range form of the predicate lets the dry run see the same rows as the run that raises
		 * the pins first: after the raise, no pin lies strictly between the bounds any more.
		 * </p>
		 */
		private List<PinCandidate> fetchCandidates(PooledConnection connection, long lower, long upper)
				throws SQLException {
			List<PinCandidate> result = new ArrayList<>();
			try (ResultSet dbResult =
				_selectCandidates.executeQuery(connection, Long.valueOf(lower), Long.valueOf(upper))) {
				while (dbResult.next()) {
					String targetType = _monomorphicTarget != null ? _monomorphicTarget
						: dbResult.getString(CANDIDATE_RESULT_TARGET_TYPE);
					result.add(new PinCandidate(
						dbResult.getLong(CANDIDATE_RESULT_BRANCH),
						IdentifierUtil.getId(dbResult, CANDIDATE_RESULT_ID),
						dbResult.getLong(CANDIDATE_RESULT_REV_MAX),
						dbResult.getLong(CANDIDATE_RESULT_TARGET_BRANCH),
						IdentifierUtil.getId(dbResult, CANDIDATE_RESULT_TARGET_ID),
						targetType));
				}
			}
			return result;
		}

		private List<PinCandidate> selectDangling(PooledConnection connection, List<PinCandidate> candidates,
				long upper, Log log) throws SQLException {
			Map<String, List<PinCandidate>> byTargetType = new LinkedHashMap<>();
			for (PinCandidate candidate : candidates) {
				byTargetType.computeIfAbsent(candidate.getTargetType(), name -> new ArrayList<>()).add(candidate);
			}
			List<PinCandidate> result = new ArrayList<>();
			for (Entry<String, List<PinCandidate>> entry : byTargetType.entrySet()) {
				List<PinCandidate> group = entry.getValue();
				TableAccess target = _itemTableByName.get(entry.getKey());
				if (target == null) {
					log.info("Values of '" + _reference.getName() + "' in '" + _table.getDBName()
						+ "' name the unknown type '" + entry.getKey() + "' and cannot resolve.", Log.VERBOSE);
					result.addAll(group);
					continue;
				}
				List<TLID> ids = new ArrayList<>(group.size());
				for (PinCandidate candidate : group) {
					ids.add(candidate.getTargetId());
				}
				Set<ObjectBranchId> existing = target.existingAt(connection, ids, upper);
				MetaObject targetType = target.getTable().getType();
				for (PinCandidate candidate : group) {
					ObjectBranchId id =
						new ObjectBranchId(candidate.getTargetBranch(), targetType, candidate.getTargetId());
					if (!existing.contains(id)) {
						result.add(candidate);
					}
				}
			}
			return result;
		}

		private void clear(PooledConnection connection, List<PinCandidate> dangling) throws SQLException {
			boolean withBranch = _table.hasBranchColumn();
			int maxBatchSize = _sqlDialect.getMaxBatchSize(withBranch ? 3 : 2);
			try (Batch batch = _clear.createBatch(connection)) {
				int pending = 0;
				for (PinCandidate candidate : dangling) {
					if (withBranch) {
						batch.addBatch(Long.valueOf(candidate.getBranch()), candidate.getId(),
							Long.valueOf(candidate.getRevMax()));
					} else {
						batch.addBatch(candidate.getId(), Long.valueOf(candidate.getRevMax()));
					}
					if (++pending >= maxBatchSize) {
						batch.executeBatch();
						pending = 0;
					}
				}
				if (pending > 0) {
					batch.executeBatch();
				}
			}
		}
	}

	/**
	 * A row whose reference is pinned to the compaction revision, with the identity of the row and
	 * of its reference target.
	 */
	private static final class PinCandidate {

		private final long _branch;

		private final TLID _id;

		private final long _revMax;

		private final long _targetBranch;

		private final TLID _targetId;

		private final String _targetType;

		PinCandidate(long branch, TLID id, long revMax, long targetBranch, TLID targetId, String targetType) {
			_branch = branch;
			_id = id;
			_revMax = revMax;
			_targetBranch = targetBranch;
			_targetId = targetId;
			_targetType = targetType;
		}

		long getBranch() {
			return _branch;
		}

		TLID getId() {
			return _id;
		}

		long getRevMax() {
			return _revMax;
		}

		long getTargetBranch() {
			return _targetBranch;
		}

		TLID getTargetId() {
			return _targetId;
		}

		String getTargetType() {
			return _targetType;
		}
	}

	/**
	 * {@code SELECT BRANCH, IDENTIFIER, REV_MAX, R_BRC, R_ID, R_TYPE FROM t WHERE R_REV > lower AND
	 * R_REV <= upper AND (REV_MIN <= lower OR REV_MAX >= upper)}
	 * 
	 * <p>
	 * The second condition restricts the result to the rows that outlive the run: a row with
	 * {@code REV_MIN > lower} and {@code REV_MAX < upper} lies completely inside the compacted range
	 * and is deleted.
	 * </p>
	 */
	private CompiledStatement createSelectPinCandidates(ItemTables.Table table, ItemTables.Reference reference) {
		DBAttribute revColumn = reference.getRevisionColumn();
		DBAttribute targetType = reference.getTypeColumn();
		List<SQLColumnDefinition> columns = new ArrayList<>();
		columns.add(columnDef(table.branchExpression(), RESULT_BRANCH));
		columns.add(columnDef(column(NO_TABLE_ALIAS, table.getIdentifier(), NOT_NULL), RESULT_ID));
		columns.add(columnDef(column(NO_TABLE_ALIAS, table.getRevMax(), NOT_NULL), RESULT_REV_MAX));
		columns.add(columnDef(table.viewBranchExpression(reference), RESULT_TARGET_BRANCH));
		columns.add(columnDef(column(NO_TABLE_ALIAS, reference.getIdColumn(), NOT_NULL),
			RESULT_TARGET_ID));
		if (targetType != null) {
			columns.add(columnDef(column(NO_TABLE_ALIAS, targetType, !NOT_NULL), RESULT_TARGET_TYPE));
		}
		return query(
			rangeParameters(revColumn),
			select(
				columns,
				table(table.getType(), NO_TABLE_ALIAS),
				and(
					and(
						gt(column(NO_TABLE_ALIAS, revColumn, NOT_NULL), parameter(revColumn, PARAM_LOWER)),
						le(column(NO_TABLE_ALIAS, revColumn, NOT_NULL), parameter(revColumn, PARAM_UPPER))),
					or(
						le(column(NO_TABLE_ALIAS, table.getRevMin(), NOT_NULL),
							parameter(revColumn, PARAM_LOWER)),
						ge(column(NO_TABLE_ALIAS, table.getRevMax(), NOT_NULL),
							parameter(revColumn, PARAM_UPPER))))))
								.toSql(_sqlDialect);
	}

	/**
	 * {@code UPDATE t SET R_ID = <null id>, R_TYPE = NULL, R_REV = 0, R_BRC = 0 WHERE BRANCH =
	 * branch AND IDENTIFIER = id AND REV_MAX = revMax}
	 * 
	 * <p>
	 * The values written are the ones a reference without a value holds.
	 * </p>
	 *
	 * @see NullReference
	 */
	private CompiledStatement createClearPin(ItemTables.Table table, ItemTables.Reference reference) {
		NullReference nullValue = NullReference.create(reference);
		List<String> columnNames = nullValue.getColumnNames();
		List<SQLExpression> values = nullValue.getValues();

		DBAttribute identifier = table.getIdentifier();
		DBAttribute revMax = table.getRevMax();
		DBAttribute branch = table.getBranch();
		List<Parameter> parameters = new ArrayList<>();
		SQLExpression key = and(
			eq(column(NO_TABLE_ALIAS, identifier, NOT_NULL), parameter(identifier, PARAM_ID)),
			eq(column(NO_TABLE_ALIAS, revMax, NOT_NULL), parameter(revMax, PARAM_REV_MAX)));
		if (branch != null) {
			parameters.add(parameterDef(branch, PARAM_BRANCH));
			key = and(eq(column(NO_TABLE_ALIAS, branch, NOT_NULL), parameter(branch, PARAM_BRANCH)), key);
		}
		parameters.add(parameterDef(identifier, PARAM_ID));
		parameters.add(parameterDef(revMax, PARAM_REV_MAX));
		return query(parameters, update(table(table.getType(), NO_TABLE_ALIAS), key, columnNames, values))
			.toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT BRANCH, IDENTIFIER FROM t WHERE IDENTIFIER IN (ids) AND REV_MIN <= upper AND
	 * REV_MAX >= upper}
	 */
	private CompiledStatement createTargetExists(ItemTables.Table table) {
		DBAttribute identifier = table.getIdentifier();
		DBAttribute revMin = table.getRevMin();
		DBAttribute revMax = table.getRevMax();
		List<Parameter> parameters = parameters(
			setParameterDef(PARAM_IDS, identifier.getSQLType()),
			parameterDef(revMin, PARAM_UPPER));
		return query(parameters,
			select(
				columns(
					columnDef(table.branchExpression(), RESULT_BRANCH),
					columnDef(column(NO_TABLE_ALIAS, identifier, NOT_NULL), RESULT_ID)),
				table(table.getType(), NO_TABLE_ALIAS),
				and(
					inSet(column(NO_TABLE_ALIAS, identifier, NOT_NULL),
						setParameter(PARAM_IDS, identifier.getSQLType())),
					and(
						le(column(NO_TABLE_ALIAS, revMin, NOT_NULL), parameter(revMin, PARAM_UPPER)),
						ge(column(NO_TABLE_ALIAS, revMax, NOT_NULL), parameter(revMin, PARAM_UPPER))))))
							.toSql(_sqlDialect);
	}

	/**
	 * {@code UPDATE t SET col = upper WHERE col > lower AND col < upper}
	 */
	private CompiledStatement createRaiseRevision(DBTableMetaObject table, DBAttribute column) {
		return query(
			rangeParameters(column),
			update(
				table(table, NO_TABLE_ALIAS),
				inRange(column),
				Collections.singletonList(column.getDBName()),
				Collections.<SQLExpression> singletonList(parameter(column, PARAM_UPPER)))).toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT count(1) FROM t WHERE col > lower AND col < upper}
	 */
	private CompiledStatement createCountInRange(DBTableMetaObject table, DBAttribute column) {
		return createCount(table, rangeParameters(column), inRange(column));
	}

	/**
	 * {@code DELETE FROM t WHERE REV_MIN > lower AND REV_MAX >= windowStart AND REV_MAX < windowStop}
	 */
	private CompiledStatement createDeleteWindow(ItemTables.Table table) {
		DBAttribute revMin = table.getRevMin();
		DBAttribute revMax = table.getRevMax();
		List<Parameter> parameters = parameters(
			parameterDef(revMin, PARAM_LOWER),
			parameterDef(revMax, PARAM_WINDOW_START),
			parameterDef(revMax, PARAM_WINDOW_STOP));
		return query(parameters,
			delete(
				table(table.getType(), NO_TABLE_ALIAS),
				and(
					gt(column(NO_TABLE_ALIAS, revMin, NOT_NULL), parameter(revMin, PARAM_LOWER)),
					and(
						ge(column(NO_TABLE_ALIAS, revMax, NOT_NULL), parameter(revMax, PARAM_WINDOW_START)),
						lt(column(NO_TABLE_ALIAS, revMax, NOT_NULL), parameter(revMax, PARAM_WINDOW_STOP))))))
									.toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT count(1) FROM t WHERE REV_MIN > lower AND REV_MAX < upper}
	 */
	private CompiledStatement createCountDeleted(ItemTables.Table table) {
		DBAttribute revMin = table.getRevMin();
		DBAttribute revMax = table.getRevMax();
		return createCount(table.getType(), rangeParameters(revMax),
			and(
				gt(column(NO_TABLE_ALIAS, revMin, NOT_NULL), parameter(revMin, PARAM_LOWER)),
				lt(column(NO_TABLE_ALIAS, revMax, NOT_NULL), parameter(revMax, PARAM_UPPER))));
	}

	/**
	 * {@code UPDATE t SET REV_MIN = upper WHERE REV_MIN > lower AND REV_MIN < upper}
	 */
	private CompiledStatement createRaiseRevMin(ItemTables.Table table) {
		return createRaiseRevision(table.getType(), table.getRevMin());
	}

	/**
	 * The rows {@link #createRaiseRevMin(ItemTables.Table)} hits after the delete.
	 */
	private CompiledStatement createCountRevMin(ItemTables.Table table) {
		DBAttribute revMin = table.getRevMin();
		DBAttribute revMax = table.getRevMax();
		return createCount(table.getType(), rangeParameters(revMin),
			and(
				inRange(revMin),
				ge(column(NO_TABLE_ALIAS, revMax, NOT_NULL), parameter(revMin, PARAM_UPPER))));
	}

	/**
	 * {@code UPDATE t SET REV_MAX = upperLast WHERE REV_MAX > lower AND REV_MAX < upper}
	 *
	 * <p>
	 * After the delete, the only rows left in that range are the ones that were already visible at
	 * the lower bound. Those objects must disappear at the compaction revision, hence the row ends
	 * one revision earlier.
	 * </p>
	 */
	private CompiledStatement createLowerRevMax(ItemTables.Table table) {
		DBAttribute revMax = table.getRevMax();
		List<Parameter> parameters = parameters(
			parameterDef(revMax, PARAM_LOWER),
			parameterDef(revMax, PARAM_UPPER),
			parameterDef(revMax, PARAM_UPPER_LAST));
		return query(parameters,
			update(
				table(table.getType(), NO_TABLE_ALIAS),
				inRange(revMax),
				Collections.singletonList(revMax.getDBName()),
				Collections.<SQLExpression> singletonList(parameter(revMax, PARAM_UPPER_LAST)))).toSql(_sqlDialect);
	}

	/**
	 * The rows {@link #createLowerRevMax(ItemTables.Table)} hits after the delete.
	 */
	private CompiledStatement createCountRevMax(ItemTables.Table table) {
		DBAttribute revMin = table.getRevMin();
		DBAttribute revMax = table.getRevMax();
		return createCount(table.getType(), rangeParameters(revMax),
			and(
				inRange(revMax),
				le(column(NO_TABLE_ALIAS, revMin, NOT_NULL), parameter(revMax, PARAM_LOWER))));
	}

	/**
	 * The rows the create revision update hits after the delete.
	 */
	private CompiledStatement createCountRevCreate(ItemTables.Table table) {
		DBAttribute revMin = table.getRevMin();
		DBAttribute revMax = table.getRevMax();
		DBAttribute revCreate = table.getRevCreate();
		return createCount(table.getType(), rangeParameters(revCreate),
			and(
				inRange(revCreate),
				or(
					le(column(NO_TABLE_ALIAS, revMin, NOT_NULL), parameter(revCreate, PARAM_LOWER)),
					ge(column(NO_TABLE_ALIAS, revMax, NOT_NULL), parameter(revCreate, PARAM_UPPER)))));
	}

	/**
	 * {@code SELECT max(rev) AS rev FROM REVISION WHERE date <= date}
	 */
	private CompiledStatement createResolveRevision() {
		return query(
			parameters(parameterDef(_revisionDate, PARAM_DATE)),
			select(
				Collections.singletonList(columnDef(max(column(NO_TABLE_ALIAS, _revisionRev, NOT_NULL)),
					RESULT_REVISION)),
				table((DBTableMetaObject) _revisionType, NO_TABLE_ALIAS),
				le(column(NO_TABLE_ALIAS, _revisionDate, NOT_NULL), parameter(_revisionDate, PARAM_DATE))))
					.toSql(_sqlDialect);
	}

	/**
	 * {@code SELECT rev FROM REVISION WHERE rev = upper}
	 */
	private CompiledStatement createRevisionExists() {
		return query(
			parameters(parameterDef(_revisionRev, PARAM_UPPER)),
			select(
				Collections.singletonList(columnDef(column(NO_TABLE_ALIAS, _revisionRev, NOT_NULL), RESULT_REVISION)),
				table((DBTableMetaObject) _revisionType, NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, _revisionRev, NOT_NULL), parameter(_revisionRev, PARAM_UPPER))))
					.toSql(_sqlDialect);
	}

	/**
	 * {@code DELETE FROM REVISION WHERE rev > lower AND rev < upper}
	 */
	private CompiledStatement createDeleteRevisions() {
		return query(
			rangeParameters(_revisionRev),
			delete(
				table((DBTableMetaObject) _revisionType, NO_TABLE_ALIAS),
				inRange(_revisionRev))).toSql(_sqlDialect);
	}

	/** @see #createDeleteRevisions() */
	private CompiledStatement createCountRevisions() {
		return createCountInRange((DBTableMetaObject) _revisionType, _revisionRev);
	}

	/**
	 * {@code SELECT author FROM REVISION WHERE rev = upper}
	 */
	private CompiledStatement createFetchAuthor() {
		return query(
			parameters(parameterDef(_revisionRev, PARAM_UPPER)),
			select(
				Collections.singletonList(columnDef(column(NO_TABLE_ALIAS, _revisionAuthor, NOT_NULL), RESULT_AUTHOR)),
				table((DBTableMetaObject) _revisionType, NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, _revisionRev, NOT_NULL), parameter(_revisionRev, PARAM_UPPER))))
					.toSql(_sqlDialect);
	}

	/**
	 * {@code UPDATE REVISION SET author = author, log = log WHERE rev = upper}
	 */
	private CompiledStatement createUpdateRevisionLog() {
		List<Parameter> parameters = parameters(
			parameterDef(_revisionRev, PARAM_UPPER),
			parameterDef(_revisionAuthor, PARAM_AUTHOR),
			parameterDef(_revisionLog, PARAM_LOG));
		return query(parameters,
			update(
				table((DBTableMetaObject) _revisionType, NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, _revisionRev, NOT_NULL), parameter(_revisionRev, PARAM_UPPER)),
				columnNames(_revisionAuthor.getDBName(), _revisionLog.getDBName()),
				expressions(parameter(_revisionAuthor, PARAM_AUTHOR), parameter(_revisionLog, PARAM_LOG))))
					.toSql(_sqlDialect);
	}

	/**
	 * {@code DELETE FROM XREF WHERE rev > lower AND rev < upper}
	 */
	private CompiledStatement createDeleteXrefBelow() {
		return query(
			rangeParameters(_xrefRev),
			delete(
				table(_xrefType, NO_TABLE_ALIAS),
				inRange(_xrefRev))).toSql(_sqlDialect);
	}

	/** @see #createDeleteXrefBelow() */
	private CompiledStatement createCountXrefBelow() {
		return createCountInRange(_xrefType, _xrefRev);
	}

	/**
	 * {@code DELETE FROM XREF WHERE rev = upper}
	 */
	private CompiledStatement createDeleteXrefAt() {
		return query(
			parameters(parameterDef(_xrefRev, PARAM_UPPER)),
			delete(
				table(_xrefType, NO_TABLE_ALIAS),
				eq(column(NO_TABLE_ALIAS, _xrefRev, NOT_NULL), parameter(_xrefRev, PARAM_UPPER))))
					.toSql(_sqlDialect);
	}

	/**
	 * {@code INSERT INTO XREF (rev, branch, type) SELECT DISTINCT upper, BRANCH, 'type' FROM t WHERE
	 * REV_MIN = upper OR REV_MAX = upper - 1}
	 *
	 * <p>
	 * The two alternatives describe the rows created or changed in the compaction revision and the
	 * rows of the objects deleted in it. The bounds are literals, because the statement is built
	 * once per run.
	 * </p>
	 */
	private CompiledStatement createXrefInsert(ItemTables.Table table, long upper) {
		SQLExpression branch = table.hasBranchColumn()
			? column(NO_TABLE_ALIAS, BasicTypes.BRANCH_DB_NAME, NOT_NULL)
			: literalLong(TLContext.TRUNK_ID);
		return query(
			insert(
				table(_xrefType, NO_TABLE_ALIAS),
				columnNames(_xrefRev.getDBName(), _xrefBranch.getDBName(), _xrefTypeName.getDBName()),
				selectDistinct(
					columns(
						columnDef(literalLong(upper), _xrefRev.getDBName()),
						columnDef(branch, _xrefBranch.getDBName()),
						columnDef(literal(DBType.STRING, table.getType().getName()), _xrefTypeName.getDBName())),
					table(table.getType(), NO_TABLE_ALIAS),
					or(
						eq(column(NO_TABLE_ALIAS, table.getRevMin(), NOT_NULL), literalLong(upper)),
						eq(column(NO_TABLE_ALIAS, table.getRevMax(), NOT_NULL), literalLong(upper - 1))))))
							.toSql(_sqlDialect);
	}

	private CompiledStatement createCount(DBTableMetaObject table, List<Parameter> parameters, SQLExpression where) {
		return query(parameters,
			select(
				Collections.singletonList(columnDef(count(literalInteger(1)), RESULT_COUNT)),
				table(table, NO_TABLE_ALIAS),
				where)).toSql(_sqlDialect);
	}

	/**
	 * The parameter declaration shared by all statements that address a revision range.
	 */
	private static List<Parameter> rangeParameters(DBAttribute column) {
		return parameters(
			parameterDef(column, PARAM_LOWER),
			parameterDef(column, PARAM_UPPER));
	}

	/**
	 * {@code col > lower AND col < upper}
	 */
	private static SQLExpression inRange(DBAttribute column) {
		return and(
			gt(column(NO_TABLE_ALIAS, column, NOT_NULL), parameter(column, PARAM_LOWER)),
			lt(column(NO_TABLE_ALIAS, column, NOT_NULL), parameter(column, PARAM_UPPER)));
	}

	/**
	 * What a {@link HistoryCompaction} run changed, or would change.
	 */
	public static final class Report {

		private final boolean _dryRun;

		private final long _lowerRevision;

		private final long _compactionRevision;

		private final List<TableReport> _tables = new ArrayList<>();

		private long _xrefRowsRebuilt;

		Report(boolean dryRun, long lowerRevision, long compactionRevision) {
			_dryRun = dryRun;
			_lowerRevision = lowerRevision;
			_compactionRevision = compactionRevision;
		}

		/**
		 * Whether the run only counted rows instead of changing them.
		 */
		public boolean isDryRun() {
			return _dryRun;
		}

		/**
		 * The exclusive lower bound of the compacted revision range.
		 */
		public long getLowerRevision() {
			return _lowerRevision;
		}

		/**
		 * The revision all discarded revisions were collapsed into.
		 */
		public long getCompactionRevision() {
			return _compactionRevision;
		}

		/**
		 * The per-table numbers, in the order the tables were processed.
		 */
		public List<TableReport> getTables() {
			return Collections.unmodifiableList(_tables);
		}

		/**
		 * The numbers of the table with the given database name, created on first access.
		 */
		TableReport table(String dbName) {
			for (TableReport table : _tables) {
				if (table.getTableName().equals(dbName)) {
					return table;
				}
			}
			TableReport result = new TableReport(dbName);
			_tables.add(result);
			return result;
		}

		/**
		 * Total number of physically deleted rows.
		 */
		public long getDeletedRows() {
			return _tables.stream().mapToLong(TableReport::getDeletedRows).sum();
		}

		/**
		 * Total number of rows whose revision range or create revision was rewritten.
		 */
		public long getRewrittenRows() {
			return _tables.stream().mapToLong(TableReport::getRewrittenRows).sum();
		}

		/**
		 * Total number of reference pins moved to the compaction revision.
		 */
		public long getRewrittenPins() {
			return _tables.stream().mapToLong(TableReport::getRewrittenPins).sum();
		}

		/**
		 * Total number of references cleared because their target has no state in the compaction
		 * revision.
		 */
		public long getClearedPins() {
			return _tables.stream().mapToLong(TableReport::getClearedPins).sum();
		}

		/**
		 * Number of cross reference rows written for the compaction revision.
		 *
		 * <p>
		 * The cross reference of the compaction revision is recomputed on every run and is
		 * therefore no indication of a change.
		 * </p>
		 *
		 * @see RevisionXref
		 */
		public long getXrefRowsRebuilt() {
			return _xrefRowsRebuilt;
		}

		void addXrefRowsRebuilt(long rows) {
			_xrefRowsRebuilt += rows;
		}

		/**
		 * Whether nothing was (or would be) changed.
		 */
		public boolean isEmpty() {
			return getDeletedRows() == 0 && getRewrittenRows() == 0 && getRewrittenPins() == 0
				&& getClearedPins() == 0;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(_dryRun ? "Analysis of compacting" : "Compacted");
			buffer.append(" revisions (");
			buffer.append(_lowerRevision);
			buffer.append(", ");
			buffer.append(_compactionRevision);
			buffer.append("): ");
			buffer.append(getDeletedRows());
			buffer.append(" rows deleted, ");
			buffer.append(getRewrittenRows());
			buffer.append(" rows rewritten, ");
			buffer.append(getRewrittenPins());
			buffer.append(" references re-pinned, ");
			buffer.append(getClearedPins());
			buffer.append(" references cleared, ");
			buffer.append(getXrefRowsRebuilt());
			buffer.append(" cross reference rows rebuilt.");
			for (TableReport table : _tables) {
				if (table.isEmpty()) {
					continue;
				}
				buffer.append("\n  ");
				buffer.append(table.toString());
			}
			return buffer.toString();
		}
	}

	/**
	 * The part of a {@link Report} that concerns a single database table.
	 */
	public static final class TableReport {

		private final String _tableName;

		private long _deletedRows;

		private long _rewrittenRows;

		private long _rewrittenPins;

		private long _clearedPins;

		TableReport(String tableName) {
			_tableName = tableName;
		}

		/**
		 * The database name of the table.
		 */
		public String getTableName() {
			return _tableName;
		}

		/**
		 * Number of physically deleted rows.
		 */
		public long getDeletedRows() {
			return _deletedRows;
		}

		void addDeletedRows(long rows) {
			_deletedRows += rows;
		}

		/**
		 * Number of rows whose revision range or create revision was rewritten.
		 */
		public long getRewrittenRows() {
			return _rewrittenRows;
		}

		void addRewrittenRows(long rows) {
			_rewrittenRows += rows;
		}

		/**
		 * Number of reference pins moved to the compaction revision.
		 */
		public long getRewrittenPins() {
			return _rewrittenPins;
		}

		void addRewrittenPins(long pins) {
			_rewrittenPins += pins;
		}

		/**
		 * Number of references cleared because their target has no state in the compaction
		 * revision.
		 */
		public long getClearedPins() {
			return _clearedPins;
		}

		void addClearedPins(long pins) {
			_clearedPins += pins;
		}

		/**
		 * Whether this table was not touched at all.
		 */
		public boolean isEmpty() {
			return _deletedRows == 0 && _rewrittenRows == 0 && _rewrittenPins == 0 && _clearedPins == 0;
		}

		@Override
		public String toString() {
			return _tableName + ": " + _deletedRows + " deleted, " + _rewrittenRows + " rewritten, "
				+ _rewrittenPins + " re-pinned, " + _clearedPins + " cleared";
		}
	}

	/**
	 * The types of the {@link KnowledgeBase} whose history is compacted.
	 */
	public MORepository getRepository() {
		return _repository;
	}

}
