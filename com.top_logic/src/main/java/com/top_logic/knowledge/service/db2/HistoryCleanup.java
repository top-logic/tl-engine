/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.column;
import static com.top_logic.basic.db.sql.SQLFactory.parameter;
import static com.top_logic.basic.db.sql.SQLFactory.parameterDef;
import static com.top_logic.basic.db.sql.SQLFactory.table;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.parameter;
import static com.top_logic.dob.sql.SQLFactory.parameterDef;
import static com.top_logic.dob.sql.SQLFactory.table;
import static com.top_logic.knowledge.service.db2.HistoryCleanupTable.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFun;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sched.SchedulerServiceHandle;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.SystemContextRunnable;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * Removes historic data of given tables up to the last revision of the {@link KnowledgeBase}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HistoryCleanup implements Runnable {

	private static final String MIN_CLEANUP_REVISION = "min";

	private static final String REVISION_PARAM = "revision";

	/** Contains for a each type to clean up a delete statement. */
	private final CompiledStatement[] _deleteStatements;

	/** CompiledStatement that removes old flex data entries. */
	private final CompiledStatement _flexAttributeCleanup;

	/**
	 * {@link CompiledStatement} that fetches the minimum of all revisions that are stored in the
	 * cleanup table.
	 */
	private final CompiledStatement _minRevisionFetch;

	private final ConnectionPool _connectionPool;

	private final HistoryManager _history;

	private final String _name;

	private long _lastRevision;

	private final CompiledStatement _updateRevision;

	/**
	 * Creates a new {@link HistoryCleanup}.
	 * 
	 * @param log
	 *        {@link Protocol} to notify messages.
	 * @param kb
	 *        The {@link KnowledgeBase} to clean up.
	 * @param cleanupTypes
	 *        The types to clean up.
	 */
	public HistoryCleanup(Protocol log, DBKnowledgeBase kb, Iterable<? extends DBTableMetaObject> cleanupTypes) {
		_history = kb.getHistoryManager();
		_name = kb.getName();
		_connectionPool = kb.getConnectionPool();
		DBHelper sqlDialect = kb.dbHelper;
		_lastRevision = _history.getLastRevision();
		ArrayList<CompiledStatement> delStmts = new ArrayList<>();
		for (DBTableMetaObject cleanupType : cleanupTypes) {
			CompiledStatement deleteStmt = createDeleteStatement(log, sqlDialect, cleanupType);
			if (deleteStmt == null) {
				// No delete statement for type
				continue;
			}
			delStmts.add(deleteStmt);
		}
		_flexAttributeCleanup = createFlexAttributeCleanup(sqlDialect, cleanupTypes);
		_deleteStatements = delStmts.toArray(new CompiledStatement[delStmts.size()]);
		ClusterManager cm = ClusterManager.getInstance();
		if (!inClusterMode(cm)) {
			_minRevisionFetch = null;
			_updateRevision = null;
		} else {
			_minRevisionFetch = createRevisionFetch(sqlDialect);
			_updateRevision = createUpdateRevision(cm, sqlDialect);
		}
	}

	private static boolean inClusterMode(ClusterManager cm) {
		return cm.isClusterMode();
	}

	/** @see #_minRevisionFetch */
	private CompiledStatement createRevisionFetch(DBHelper sqlDialect) {
		String alias = NO_TABLE_ALIAS;
		SQLTableReference from = table(CLEANUP_TABLE, alias);
		SQLExpression minumim = function(SQLFun.min, column(alias, REVISION, NOT_NULL));
		List<SQLColumnDefinition> columns = Collections.singletonList(columnDef(minumim, MIN_CLEANUP_REVISION));
		SQLExpression where = SQLBoolean.TRUE;
		SQLQuery<?> query = query(select(false, columns, from, where, NO_ORDERS, limitFirstRow()));
		return query.toSql(sqlDialect);
	}

	/** @see #_updateRevision */
	private CompiledStatement createUpdateRevision(ClusterManager cm, DBHelper sqlDialect) {
		// Must not use table alias in update statement.
		String alias = NO_TABLE_ALIAS;
		SQLQuery<?> query = query(
			Arrays.asList(parameterDef(REVISION, "rev")),
			update(
				table(CLEANUP_TABLE, alias),
				and(
					eq(
						column(alias, NODE_ID, NOT_NULL),
						literalLong(cm.getNodeId())),
					eq(
						column(alias, KB_NAME, NOT_NULL),
						literalString(_name))),
				Collections.singletonList(REVISION.getDBName()),
				Collections.<SQLExpression> singletonList(parameter(REVISION, "rev"))));
		return query.toSql(sqlDialect);
	}

	/** @see #_flexAttributeCleanup */
	private CompiledStatement createFlexAttributeCleanup(DBHelper sqlDialect,
			Iterable<? extends MetaObject> cleanupTypes) {
		List<String> typeNames = new ArrayList<>();
		for (MetaObject cleanupType : cleanupTypes) {
			typeNames.add(cleanupType.getName());
		}
		DBType referenceDBType = IdentifierTypes.TYPE_REFERENCE_MO_TYPE.getDefaultSQLType();
		String alias = NO_TABLE_ALIAS;
		SQLQuery<?> query = query(
			Arrays.asList(parameterDef(IdentifierTypes.REVISION_REFERENCE_MO_TYPE.getDefaultSQLType(), "rev")),
			delete(
				table(AbstractFlexDataManager.FLEX_DATA_DB_NAME, alias),
				and(
					inSet(column(alias, AbstractFlexDataManager.TYPE_DBNAME), typeNames, referenceDBType),
					le(
						column(alias, BasicTypes.REV_MAX_DB_NAME, NOT_NULL),
						parameter(IdentifierTypes.REVISION_REFERENCE_MO_TYPE.getDefaultSQLType(), "rev")))));
		return query.toSql(sqlDialect);
	}

	/** @see #_deleteStatements */
	private CompiledStatement createDeleteStatement(Protocol log, DBHelper sqlDialect, DBTableMetaObject cleanupType) {
		String alias = NO_TABLE_ALIAS;
		DBAttribute revMax = (DBAttribute) cleanupType.getAttributeOrNull(BasicTypes.REV_MAX_ATTRIBUTE_NAME);
		if (revMax == null) {
			StringBuilder noRevMax = new StringBuilder();
			noRevMax.append("Type '");
			noRevMax.append(cleanupType.getName());
			noRevMax.append("' is not a item type to cleanup. (It has no revision column.)");
			log.error(noRevMax.toString());
			return null;
		}
		SQLQuery<?> query = query(
			Arrays.asList(parameterDef(revMax, "rev")),
			delete(
				table(cleanupType, alias),
				le(
					column(alias, revMax, NOT_NULL),
					parameter(revMax, "rev"))));
		return query.toSql(sqlDialect);
	}

	@Override
	public void run() {
		long lastRevision = _history.getLastRevision();
		if (lastRevision > _lastRevision) {
			PooledConnection writeConnection = _connectionPool.borrowWriteConnection();
			try {
				updateCleanupRevision(writeConnection, Long.valueOf(lastRevision));
				long minRev = fetchMinCleanupRevision(writeConnection, lastRevision);
				if (minRev >= lastRevision) {
					// minRev is still needed. all previous not.
					cleanup(writeConnection, minRev - 1);
				}
				writeConnection.commit();
			} catch (SQLException ex) {
				Logger.error("Unable to cleanup unversioned types.", ex, HistoryCleanup.class);
			} finally {
				_lastRevision = lastRevision;
				_connectionPool.releaseWriteConnection(writeConnection);
			}
		}
	}

	/**
	 * Removes all data up to (including) the given revision, i.e. no data for the given revision
	 * can be accessed.
	 */
	private void cleanup(PooledConnection connection, long upToRevision) throws SQLException {
		Long rev = Long.valueOf(upToRevision);
		for (CompiledStatement deleteStatement : _deleteStatements) {
			deleteStatement.executeUpdate(connection, rev);
		}
		_flexAttributeCleanup.executeUpdate(connection, rev);
	}

	/**
	 * Fetches the minimum revision which is still needed this node or a different cluster node.
	 */
	private long fetchMinCleanupRevision(PooledConnection readConnection, long myMinRevision) throws SQLException {
		if (_minRevisionFetch == null) {
			return myMinRevision;
		}
		ResultSet result = _minRevisionFetch.executeQuery(readConnection);
		try {
			if (result.next()) {
				return result.getLong(MIN_CLEANUP_REVISION);
			} else {
				// at least the revision of this node is contained.
				Logger.error("Unable to fetch minimum of all cleanup revisions: No cleanup revision found.",
					HistoryCleanup.class);
				return myMinRevision;
			}
		} finally {
			result.close();
		}
	}

	/**
	 * Updates the cleanup revision in database with the given revision.
	 * 
	 * @param revision
	 *        The new revision to store in database. If <code>null</code> the former set value is
	 *        removed.
	 */
	private void updateCleanupRevision(PooledConnection connection, Long revision) throws SQLException {
		if (!inClusterMode(ClusterManager.getInstance())) {
			return;
		}
		_updateRevision.executeUpdate(connection, revision);
	}

	/**
	 * Initializes the table to store entries in.
	 */
	void initTable() {
		ClusterManager cm = ClusterManager.getInstance();
		if (!inClusterMode(cm)) {
			return;
		}
		try {
			removeDeadNodeEntries(cm);
			insertLastRevision(cm);
		} catch (SQLException ex) {
			Logger.error("Unable to clean up old entries", ex, HistoryCleanup.class);
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private void insertLastRevision(ClusterManager cm) throws SQLException {
		PooledConnection connection = _connectionPool.borrowWriteConnection();
		try {
			CompiledStatement insertRevision = createInsertRevision(cm, connection.getSQLDialect());
			insertRevision.executeUpdate(connection, Long.valueOf(_history.getLastRevision()));
			connection.commit();
		} finally {
			_connectionPool.releaseWriteConnection(connection);
		}
	}

	private CompiledStatement createInsertRevision(ClusterManager cm, DBHelper sqlDialect) {
		String alias = NO_TABLE_ALIAS;
		SQLQuery<?> query = query(
			Arrays.asList(parameterDef(REVISION, "rev")),
			insert(
				table(CLEANUP_TABLE, alias),
				Arrays.asList(NODE_ID.getDBName(), KB_NAME.getDBName(), REVISION.getDBName()),
				Arrays.asList(literalLong(cm.getNodeId()), literalString(_name), parameter(REVISION, "rev"))));
		return query.toSql(sqlDialect);
	}

	private void removeDeadNodeEntries(ClusterManager cm) throws SQLException {
		PooledConnection connection = _connectionPool.borrowWriteConnection();
		try {
			createRemoveDeadNodeEntries(_connectionPool.getSQLDialect(), cm.getActiveNodes()).executeUpdate(connection);
			Long stillStoredRevision = getRevision(connection, cm);
			if (stillStoredRevision != null) {
				/* There is still an entry for "this" knowledge base in this node. This means that
				 * the application was not correctly shut down. If the application uses more than
				 * one knowledge base. There are two possible cleanup strategy. If each knowlege
				 * base cleans up its own entry entry, an entry may survive if a knowledge base was
				 * not started again. Removing all entries may solve this problem, because the
				 * knowledge bases are started sequentially and only the fist started knowledg base
				 * does clean up. Actually this is also not completely correct, because if a
				 * potential new knowledge base is started, it may already added its entry which is
				 * now removed. But shit happens... */
				createRemoveThisNodesEntries(_connectionPool.getSQLDialect(), cm.getNodeId()).executeUpdate(connection);
			}
			connection.commit();
		} finally {
			_connectionPool.releaseWriteConnection(connection);
		}
	}

	/**
	 * The currently stored revision for the {@link KnowledgeBase} in this node, or
	 * <code>null</code> if not yet set.
	 */
	private Long getRevision(PooledConnection readConnection, ClusterManager cm) throws SQLException {
		CompiledStatement getRevisionStmt = createGetRevision(cm, readConnection.getSQLDialect());
		ResultSet result = getRevisionStmt.executeQuery(readConnection);
		try {
			if (result.next()) {
				Long rev = Long.valueOf(result.getLong(REVISION_PARAM));
				assert !result.next() : "Unique key";
				return rev;
			} else {
				// not yet set
				return null;
			}
		} finally {
			result.close();
		}
	}

	private CompiledStatement createGetRevision(ClusterManager cm, DBHelper sqlDialect) {
		String alias = NO_TABLE_ALIAS;
		SQLQuery<?> query = query(
			select(true,
				Arrays.<SQLColumnDefinition> asList(columnDef(column(alias, REVISION, true), REVISION_PARAM)),
				table(CLEANUP_TABLE, alias),
				and(
					eq(
						column(alias, NODE_ID, NOT_NULL),
						literalLong(cm.getNodeId())),
					eq(
						column(alias, KB_NAME, NOT_NULL),
						literalString(_name))), NO_ORDERS));
		return query.toSql(sqlDialect);
	}

	private CompiledStatement createRemoveDeadNodeEntries(DBHelper sqlDialect, List<Long> activeNodes) {
		String alias = NO_TABLE_ALIAS;
		SQLQuery<?> query = query(
			delete(
				table(CLEANUP_TABLE, alias),
				not(inSet(column(NO_TABLE_ALIAS, NODE_ID, NOT_NULL), activeNodes, NODE_ID.getSQLType())))
			);
		return query.toSql(sqlDialect);
	}

	private CompiledStatement createRemoveThisNodesEntries(DBHelper sqlDialect, Long nodeId) {
		String alias = NO_TABLE_ALIAS;
		SQLQuery<?> query = query(
			delete(
				table(CLEANUP_TABLE, alias),
				eq(
					column(NO_TABLE_ALIAS, NODE_ID, NOT_NULL),
					literalLong(nodeId))));
		return query.toSql(sqlDialect);
	}

	/**
	 * Removes entries in the revision table.
	 * 
	 */
	void cleanupTable() {
		ClusterManager cm = ClusterManager.getInstance();
		if (!inClusterMode(cm)) {
			return;
		}

		try {
			removeState(cm);
		} catch (SQLException ex) {
			Logger.error("Unable to remove revision from database.", ex, HistoryCleanup.class);
		}
	}

	private void removeState(ClusterManager cm) throws SQLException {
		PooledConnection connection = _connectionPool.borrowWriteConnection();
		try {
			CompiledStatement deleteStmt = createDeleteRevision(cm, connection.getSQLDialect());
			deleteStmt.executeUpdate(connection);
			connection.commit();
		} finally {
			_connectionPool.releaseWriteConnection(connection);
		}
	}

	private CompiledStatement createDeleteRevision(ClusterManager cm, DBHelper sqlDialect) {
		// Must not use table alias in delete statement.
		String alias = NO_TABLE_ALIAS;
		SQLQuery<?> query = query(
			delete(
				table(CLEANUP_TABLE, alias),
				and(
					eq(
						column(alias, NODE_ID, NOT_NULL),
						literalLong(cm.getNodeId())),
					eq(
						column(alias, KB_NAME, NOT_NULL),
						literalString(_name)))));
		return query.toSql(sqlDialect);
	}

	/**
	 * Creates a {@link SchedulerServiceHandle} for the {@link HistoryCleanup}. Moreover it
	 * initialises the table storing the cleanup revisions and clears it after stopping.
	 */
	public static SchedulerServiceHandle newCleanupHandle(Protocol log,
			DBKnowledgeBase kb, Iterable<? extends DBTableMetaObject> cleanupTypes) {
		final HistoryCleanup historyCleanup = new HistoryCleanup(log, kb, cleanupTypes);
		Runnable runnable = new SystemContextRunnable<Runnable>(historyCleanup);
		return new SchedulerServiceHandle(runnable) {

			@Override
			public void start(long interval, TimeUnit timeUnit) {
				assert ThreadContextManager.getInteraction() != null : "Start during start of KnowledgeBase. This occurs in interaction.";
				historyCleanup.initTable();
				super.start(interval, timeUnit);
			}

			@Override
			public void stop(long time, TimeUnit timeUnit) {
				super.stop(time, timeUnit);
				assert ThreadContextManager.getInteraction() != null : "Stop during teardown of KnowledgeBase. This occurs in interaction.";
				historyCleanup.cleanupTable();
			}
		};
	}

}
