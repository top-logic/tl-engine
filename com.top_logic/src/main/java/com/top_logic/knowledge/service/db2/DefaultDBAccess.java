/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.lang.ref.Reference;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MappedList;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;

/**
 * Default {@link DBAccess} implementation that dispatches to abstract statement
 * creation methods.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class DefaultDBAccess extends AbstractDBAccess {

	private static final Comparator<Object> ID_ORDER = new Comparator<>() {
		@Override
		public int compare(Object o1, Object o2) {
			return ((ObjectKey) o1).getObjectName().compareTo(((ObjectKey) o2).getObjectName());
		}
	};

	private static final Mapping<Object, TLID> TO_ID = new Mapping<>() {
		@Override
		public TLID map(Object input) {
			return ((ObjectKey) input).getObjectName();
		}
	};

	/**
	 * Creates a {@link DefaultDBAccess}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect to use for accessing data.
	 * @param table
	 *        See {@link #getType()}
	 */
	public DefaultDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		super(sqlDialect, table);
	}

	@Override
	public KnowledgeItemInternal fetch(DBKnowledgeBase knowledgeBase, PooledConnection readConnection,
			long branchContext, TLID id, long itemRevision, long dataRevision) throws SQLException {

		/* For performance reasons following code is copied to other fetch method. */
		if (dataRevision > itemRevision) {
			throw new IllegalArgumentException("Data revision must not be greater than item revision.");
		}
		KnowledgeItemInternal result;
		int retry = sqlDialect.retryCount();
		while (true) {
			try {
				ResultSet resultSet = fetchData(readConnection, branchContext, id, dataRevision);
				try {
					if (resultSet.next()) {
						// Fetch the object.
						result =
							knowledgeBase.findOrCreateItem(readConnection, resultSet, DBAttribute.DEFAULT_DB_OFFSET,
								type, itemRevision, dataRevision);
					} else {
						// Object not found.
						result = null;
					}
					break;
				} finally {
					resultSet.close();
				}
			} catch (SQLException ex) {
				readConnection.closeConnection(ex);
				if ((retry <= 0) || (! sqlDialect.canRetry(ex))) {
					StringBuilder error = new StringBuilder();
					error.append("Unable to fetch knowledge item with id '");
					error.append(id);
					error.append("' of type '");
					error.append(type.getName());
					error.append("'.");
					throw new SQLException(error.toString(), ex);
				}
				retry--;
			}
		}
		
		return result;
	}

	@Override
	public Object[] fetch(PooledConnection readConnection, AbstractDBKnowledgeItem sourceItem, long dataRevision)
			throws SQLException {
		DBObjectKey key = sourceItem.tId();
		long itemRevision = key.getHistoryContext();
		long branchContext = key.getBranchContext();
		TLID id = key.getObjectName();

		/* For performance reasons following code is copied from other fetch method */
		if (dataRevision > itemRevision) {
			throw new IllegalArgumentException("Data revision must not be greater than item revision.");
		}
		Object[] result;
		int retry = sqlDialect.retryCount();
		while (true) {
			try {
				ResultSet resultSet = fetchData(readConnection, branchContext, id, dataRevision);
				try {
					if (resultSet.next()) {
						// Fetch the object.
						result = sourceItem.newEmptyStorage();
						sourceItem.loadAttributeValues(resultSet, DBAttribute.DEFAULT_DB_OFFSET, result);
					} else {
						// Object not found.
						result = null;
					}
					break;
				} finally {
					resultSet.close();
				}
			} catch (SQLException ex) {
				readConnection.closeConnection(ex);
				if ((retry <= 0) || (!sqlDialect.canRetry(ex))) {
					StringBuilder error = new StringBuilder();
					error.append("Unable to fetch knowledge item with id '");
					error.append(id);
					error.append("' of type '");
					error.append(type.getName());
					error.append("'.");
					throw new SQLException(error.toString(), ex);
				}
				retry--;
			}
		}

		return result;
	}

	/**
	 * Fetches the data for the object on the given branch with the given id and data in the given
	 * revision.
	 * 
	 * @param db
	 *        Database connection.
	 * @param branchContext
	 *        The branch of the object to fetch data for.
	 * @param id
	 *        The ID of the object to fetch data for.
	 * @param dataRevision
	 *        The revision of the data to fetch.
	 */
	protected abstract ResultSet fetchData(PooledConnection db, long branchContext, TLID id, long dataRevision)
			throws SQLException;

	/**
	 * Fetches the data for all object on the given branch with one of the the given ids and data in
	 * the given revision.
	 * 
	 * <p>
	 * The returned data are ordered ascending by the attribute containing the identifier.
	 * </p>
	 * 
	 * @param db
	 *        Database connection.
	 * @param branchContext
	 *        The branch of the object to fetch data for.
	 * @param ids
	 *        The IDs of the objects to fetch data for. Must not contain more than
	 *        {@link DBHelper#getMaxSetSize()} elements.
	 * @param dataRevision
	 *        The revision of the data to fetch.
	 */
	protected abstract ResultSet fetchDataOrdered(PooledConnection db, long branchContext, Collection<?> ids,
			long dataRevision) throws SQLException;

	@Override
	public void fetchAll(DBKnowledgeBase kb, PooledConnection db, long dataRevision, Object[] keys, int keyCnt) throws SQLException {
		DBObjectKey firstKey = (DBObjectKey) keys[0];
		long branchContext = firstKey.getBranchContext();
		long itemRevision = firstKey.getHistoryContext();
		if (keyCnt == 1) {
			KnowledgeItemInternal item =
				fetch(kb, db, branchContext, firstKey.getObjectName(), itemRevision, dataRevision);
			if (item != null) {
				firstKey.updateReference(item.tId().getReference());
			}
			keys[0] = item;
			return;
		}
		if (dataRevision > itemRevision) {
			throw new IllegalArgumentException("Data revision must not be greater than item revision.");
		}

		Object[] sortedKeys = new Object[keyCnt];
		System.arraycopy(keys, 0, sortedKeys, 0, keyCnt);
		Arrays.sort(sortedKeys, ID_ORDER);

		assert checkKeyConsistency(branchContext, itemRevision, sortedKeys) : "Keys violate API "
			+ Arrays.toString(sortedKeys);


		List<TLID> ids = new MappedList<>(TO_ID, Arrays.asList(sortedKeys));
		KnowledgeItemFactory implFactory = type.getImplementationFactory();
		DBKey dbResultKey = new DBKey();

		int retry = sqlDialect.retryCount();
		while (true) {
			try {
				try (ResultSet resultSet = fetchDataOrdered(db, branchContext, ids, dataRevision)) {
					int n = 0;
					while (resultSet.next()) {

						// Load identifier of the fetched object
						implFactory.loadIdentifier(sqlDialect, resultSet, DBAttribute.DEFAULT_DB_OFFSET,
							itemRevision, type, dbResultKey);

						// Skip keys that refer to not found item.
						while (!dbResultKey.equals(sortedKeys[n])) {
							sortedKeys[n++] = null;

							assert n < keyCnt : "Unexpected fetch result for item '" + dbResultKey
								+ "', or wrong order.";
						}

						DBObjectKey key = (DBObjectKey) sortedKeys[n];

						// Fetch the object.
						KnowledgeItemInternal item =
							kb.findOrCreateItem(db, resultSet, DBAttribute.DEFAULT_DB_OFFSET, key, dataRevision);

						// Install reference to fetched object to allow wrapping keys into items
						// without additional lookup in the global cache.
						key.updateReference(item.tId().getReference());

						sortedKeys[n] = item;
						n++;
					}

					// Skip keys that refer to not found item.
					while (n < keyCnt) {
						sortedKeys[n++] = null;
					}

					if (keyCnt > 1) {
						// Key array was copied, wrap keys to resolved objects in original
						// order.
						for (int i = 0; i < keyCnt; i++) {
							DBObjectKey key = (DBObjectKey) keys[i];
							Reference<KnowledgeItemInternal> reference = key.getReference();
							if (reference != null) {
								KnowledgeItemInternal item = reference.get();
								keys[i] = item;
							} else {
								keys[i] = null;
							}
						}
					}

					break;
				}
			} catch (SQLException ex) {
				db.closeConnection(ex);
				if ((retry <= 0) || (!sqlDialect.canRetry(ex))) {
					StringBuilder error = new StringBuilder();
					error.append("Unable to fetch knowledge item with id in  '");
					error.append(ids);
					error.append("' of type '");
					error.append(type.getName());
					error.append("'.");
					throw new SQLException(error.toString(), ex);
				}
				retry--;
			}
		}
	}

	private boolean checkKeyConsistency(long branchContext, long itemRevision, Object[] sortedKeys) {
		DBObjectKey lastKey = null;
		for (int n = 0, cnt = sortedKeys.length; n < cnt; n++) {
			DBObjectKey key = (DBObjectKey) sortedKeys[n];
			assert key.getBranchContext() == branchContext : "Branch context missmatch.";
			assert key.getHistoryContext() == itemRevision : "History context missmatch.";
			assert !key.equals(lastKey) : "Duplicate key to resolve.";
			lastKey = key;
		}
		return true;
	}

	@Override
	public final void branch(PooledConnection db, long branchId, long createRev, long baseBranchId, long baseRevision,
			SQLExpression filterExpr) throws SQLException {
		if (!type.multipleBranches()) {
			StringBuilder error = new StringBuilder();
			error.append("Type ");
			error.append(type);
			error.append(" can not be branched, because it does not support branches.");
			throw new IllegalStateException(error.toString());
		}
		int retry = sqlDialect.retryCount();
		while (true) {
			try {
				internalBranch(db, branchId, createRev, baseBranchId, baseRevision, filterExpr);
				break;
			} catch (SQLException ex) {
				db.closeConnection(ex);
				if ((retry <= 0) || (! sqlDialect.canRetry(ex))) {
					throw new SQLException("Unable to branch type '" + type.getName() + "'.", ex);
				}
				retry--;
			}
		}
	}

	/**
	 * Actual internal implementation of
	 * {@link #branch(PooledConnection, long, long, long, long, SQLExpression)}.
	 */
	protected abstract void internalBranch(PooledConnection db, long branchId, long createRev, long baseBranchId,
			long baseRevision, SQLExpression filterExpr) throws SQLException;

	@Override
	public void insertAll(PooledConnection db, long commitNumber, List<? extends AbstractDBKnowledgeItem> objects)
			throws SQLException {
		List<MOAttribute> attributes = type.getAttributes();
		
		int maxBatchSize = db.getSQLDialect().getMaxBatchSize(type.getDBColumnCount());
		CompiledStatement insertStatement = getInsertStatement();

		int n = 0;
		int cnt = objects.size();
		int batchSize = 0;
		try (Batch batch = insertStatement.createBatch(db)) {
			Object[] statementArgs = newPreparedStatementArgs();
			for (; n < cnt; n++) {
				AbstractDBKnowledgeItem object = objects.get(n);
				
				Object[] data = getLocalData(object);
				setAllValues(db, statementArgs, object, attributes, data, commitNumber);

				batch.addBatch(statementArgs);
				batchSize++;
				
				if (batchSize == maxBatchSize) {
					executeInsertBatch(objects, batch, n);
					batchSize = 0;
				}
			}
			
			if (batchSize > 0) {
				executeInsertBatch(objects, batch, objects.size() - 1);
			}
		} catch (SQLException ex) {
			if (Logger.isDebugEnabled(DefaultDBAccess.class)) {
				List<List<Object>> batchData = new ArrayList<>();
				for (int x = n - batchSize; x < n; x++) {
					batchData.add(Arrays.asList(getLocalData(objects.get(x))));
				}
				throw new SQLException("Faild to insert into table '" + type.getName() + "' data: " + batchData,
					ex.getSQLState(), ex.getErrorCode(), ex);
			} else {
				throw ex;
			}
		}
	}

	private Object[] newPreparedStatementArgs() {
		return new Object[type.getDBColumnCount()];
	}

	/**
	 * Execute the insert batch.
	 * 
	 * @param objects
	 *        The list of all objects that are inserted in all batches.
	 * @param batch
	 *        The batch to execute.
	 * @param endOffset
	 *        The offset of the object in the given list of all objects that is added as last insert
	 *        to the batch.
	 */
	private void executeInsertBatch(List<?> objects, Batch batch, int endOffset) throws SQLException {
		int[] updates = batch.executeBatch();
		for (int n = 0, cnt = updates.length; n < cnt; n++) {
			int updateCnt = updates[n];
			
			switch (updateCnt) {
			case PreparedStatement.SUCCESS_NO_INFO:
			case PreparedStatement.CLOSE_CURRENT_RESULT: {
				break;
			}
			case PreparedStatement.EXECUTE_FAILED:
			default: { 
				throw new SQLException("Insert failed: cnt=" + updateCnt + ", obj=" + objects.get(endOffset - updates.length + n + 1));
			}
			}
		}
	}

	@Override
	public void insert(PooledConnection db, long commitNumber, AbstractDBKnowledgeItem object) throws SQLException {
		insert(db, commitNumber, getLocalData(object), object);
	}

	/**
	 * Internal method to insert the given data.
	 * 
	 * @param db
	 *        Connection to database.
	 * @param commitNumber
	 *        New commit number.
	 * @param data
	 *        The data to insert.
	 * @param item
	 *        the holder object of the data (just used when error occurs).
	 */
	protected void insert(PooledConnection db, long commitNumber, Object[] data, KnowledgeItem item) throws SQLException {
		List<MOAttribute> attributes = type.getAttributes();
		Object[] statementArgs = newPreparedStatementArgs();

		setAllValues(db, statementArgs, item, attributes, data, commitNumber);

		int updateCount = getInsertStatement().executeUpdate(db, statementArgs);
		if (updateCount != 1) {
			throw new SQLException("Insert failed: " + item);
		}
	}

	/**
	 * Creates a {@link CompiledStatement} to insert objects of the represented
	 * {@link AbstractDBAccess#getType()} in the database.
	 * 
	 * <p>
	 * The result statement must work with
	 * {@link AbstractDBAccess#setAllValues(PooledConnection, Object[], KnowledgeItem, List, Object[], long)}, i.e.
	 * the parameters in the statement must represent all attributes of the represented
	 * {@link #getType()}.
	 * </p>
	 */
	protected abstract CompiledStatement getInsertStatement();
	
	
	@Override
	public void delete(PooledConnection db, long commitNumber, DBKnowledgeItem object) throws SQLException {
		delete(db, commitNumber, getGlobalData(object, commitNumber - 1), object);
	}

	/**
	 * Internal method to delete the given data.
	 * 
	 * @param db
	 *        Connection to database.
	 * @param commitNumber
	 *        New commit number.
	 * @param data
	 *        The data to delete.
	 * @param item
	 *        The holder object of the data (just used when error occurs).
	 */
	protected void delete(PooledConnection db, long commitNumber, Object[] data, KnowledgeItem item)
			throws SQLException {
		Object[] statementArgs = newPreparedStatementArgs();
		setKeyValues(db, statementArgs, item, data, STATEMENT_ARGS_OFFSET, commitNumber);
		int updateCount = getDeleteStatement().executeUpdate(db, statementArgs);
		if (updateCount != 1) {
			throw new SQLException("Update failed: " + item);
		}
	}

	/**
	 * Creates a {@link CompiledStatement} to delete objects of the represented
	 * {@link AbstractDBAccess#getType()} from the database.
	 * 
	 * <p>
	 * The result statement must work with
	 * {@link AbstractDBAccess#setKeyValues(PooledConnection, Object[], KnowledgeItem, Object[], int, long)}, i.e. the
	 * parameters in the statement must represent the key attributes of the represented
	 * {@link #getType()}.
	 * </p>
	 */
	protected abstract CompiledStatement getDeleteStatement();

	@Override
	public void update(PooledConnection db, long commitNumber, DBKnowledgeItem object)
			throws SQLException {
		update(db, commitNumber, getLocalData(object), object);
	}

	/**
	 * Internal method to update the given data.
	 * 
	 * @param db
	 *        Connection to database.
	 * @param commitNumber
	 *        New commit number.
	 * @param data
	 *        The new data.
	 * @param item
	 *        The holder object of the data (just used when error occurs).
	 */
	protected void update(PooledConnection db, long commitNumber, Object[] data, KnowledgeItem item)
			throws SQLException {
		Object[] statementArgs = newPreparedStatementArgs();
		int parameterIndex = STATEMENT_ARGS_OFFSET;

		/* data columns are placed at the start of the statement but the position in the table is
		 * behind the keys. so the number of keys must be substracted from the db offset. */
		setDataValues(db, statementArgs, item, data, parameterIndex - numberKeyColumns, commitNumber);
		/* for the same reason the number of keys must be added to the dboffset when setting the key
		 * values */
		setKeyValues(db, statementArgs, item, data, parameterIndex + numberDataColumns, commitNumber);
		int updateCount = getUpdateStatement().executeUpdate(db, statementArgs);
		if (updateCount != 1) {
			throw new SQLException("Update failed: updateCount=" + updateCount + ", object=" + item);
		}
	}

	/**
	 * Creates a {@link CompiledStatement} string to update objects of the represented
	 * {@link AbstractDBAccess#getType()} in the database.
	 * 
	 * <p>
	 * The result statement must work with
	 * {@link AbstractDBAccess#setKeyValues(PooledConnection, Object[], KnowledgeItem, Object[], int, long)} and
	 * {@link AbstractDBAccess#setDataValues(PooledConnection, Object[], KnowledgeItem, Object[], int, long)}, i.e.
	 * the first parameters in the statement must represent the data attributes, the following must
	 * represent the key attributes of the represented {@link #getType()}.
	 * </p>
	 */
	protected abstract CompiledStatement getUpdateStatement();

	@Override
	public SQLExpression createIdentifierExpr(TableSymbol table) {
		return createColumn(id(), table);
	}

	/**
	 * Returns the identifier column.
	 */
	protected abstract DBAttribute id();

	/**
	 * Returns the attribute with the given name of the represented type.
	 */
	protected DBAttribute attribute(String name) {
		return (DBAttribute) type.getAttributeOrNull(name);
	}

}
