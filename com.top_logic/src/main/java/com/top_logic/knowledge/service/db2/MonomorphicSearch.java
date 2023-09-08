/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.ResultSetBasedIterator;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.AbstractCompiledQuery;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.QueryArguments;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * Partial result of a {@link KnowledgeBase#search(RevisionQuery)} operation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class MonomorphicSearch<E> extends AbstractCompiledQuery<E> {

	private final DBKnowledgeBase _kb;

	private final MOKnowledgeItemImpl _resultType;

	private final RevisionQuery<E> _monomorphicQuery;

	private final CompiledStatement _sql;

	/**
	 * Whether the database result contains identifier and data.
	 */
	private final boolean _fullLoad;

	MonomorphicSearch(DBKnowledgeBase kb, MOKnowledgeItemImpl resultType, RevisionQuery<E> monomorphicQuery, CompiledStatement sql, boolean fullLoad) {
		super(kb.getConnectionPool());
		_kb = kb;
		_resultType = resultType;
		_monomorphicQuery = monomorphicQuery;
		_sql = sql;
		_fullLoad = fullLoad;
	}

	@Override
	public CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments queryArgs) {
		return createResult(connection, queryArgs);
	}

	@Override
	public List<E> search(PooledConnection connection, RevisionQueryArguments arguments) {
		try (BufferingCloseableIterator<E> loader = createResult(connection, arguments)) {
			return loader.getAll();
		}
	}

	private BufferingCloseableIterator<E> createResult(PooledConnection connection, RevisionQueryArguments queryArgs) {
		RangeParam rangeParam = _monomorphicQuery.getRangeParam();
		int startRowArg = queryArgs.getStartRow();
		int stopRowArg = queryArgs.getStopRow();
		checkRowArguments(rangeParam, startRowArg, stopRowArg);

		long itemRevision = queryArgs.getRequestedRevision();
		long dataRevision;
		if (itemRevision == Revision.CURRENT_REV) {
			dataRevision = _kb.getDataRevision(queryArgs.getDataRevision());
		} else {
			dataRevision = itemRevision;
		}

		BranchParam branchParam = _monomorphicQuery.getBranchParam();
		Branch requestedBranch = QueryArguments.resolveRequestedBranch(queryArgs, branchParam, _kb);
		
		int startRow;
		int stopRow;
		switch (rangeParam) {
			case complete:
				// But not actually used
				startRow = 0;
				stopRow = -1;
				break;
			case first:
				startRow = RevisionQueryArguments.FIRST_ROW;
				stopRow = RevisionQueryArguments.FIRST_ROW + 1;
				break;
			case head:
				startRow = RevisionQueryArguments.FIRST_ROW;
				stopRow = stopRowArg;
				break;
			case range:
				startRow = startRowArg;
				stopRow = stopRowArg;
				break;
			default:
				throw RangeParam.unknownRangeParam(rangeParam);
		}
		Object[] arguments =
			_kb.addInternalArguments(_monomorphicQuery, requestedBranch, itemRevision, dataRevision,
				startRow, stopRow, queryArgs.getArguments());

		try {
			return fetchData(connection, itemRevision, dataRevision, startRow, stopRow, requestedBranch, arguments);

		} catch (SQLException ex) {
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to execute query '");
			msg.append(_monomorphicQuery);
			msg.append("' with arguments ");
			msg.append(Arrays.toString(arguments));
			msg.append(".");
			throw new KnowledgeBaseRuntimeException(msg.toString(), ex);
		}
	}

	private void checkRowArguments(RangeParam rangeParam, int startRowArg, int stopRowArg) {
		switch (rangeParam) {
			case complete:
				// Neither start nor stop row must be set
				if (startRowArg != 0 || stopRowArg != -1) {
					break;
				}
				return;
			case first:
				// Neither start nor stop row must be set
				if (startRowArg != 0 || (stopRowArg != -1 && stopRowArg != 1)) {
					break;
				}
				return;
			case head:
				// start row must not be set, stop row must be set
				if (startRowArg != 0 || stopRowArg == -1) {
					break;
				}
				return;
			case range:
				// start and stop row must be set
				if (startRowArg == 0 || stopRowArg == -1) {
					break;
				}
				return;
			default:
				throw RangeParam.unknownRangeParam(rangeParam);
		}
		StringBuilder error = new StringBuilder();
		error.append("Query arguments does not match range parameter '");
		error.append(rangeParam);
		error.append("': ");
		switch (rangeParam) {
			case complete:
				error.append("Neither start nor stop row must be set.");
				break;
			case first:
				error.append("Start row must not be set or 0, stop row must not be set or 1.");
				break;
			case head:
				error.append("Start row must not be set or 0, stop row must be set.");
				break;
			case range:
				error.append("Start row and stop row must be set.");
				break;
			default:
				throw RangeParam.unknownRangeParam(rangeParam);
		}
		throw new IllegalArgumentException(error.toString());
	}

	private BufferingCloseableIterator<E> fetchData(final PooledConnection readConnection, final long historyContext,
			long dataRevision, int startRow, int stopRow, Branch requestedBranch, Object[] arguments) throws SQLException {

		Class<E> javaType = _monomorphicQuery.getExpectedType();
		boolean resolve = _monomorphicQuery.getResolve();

		final DBHelper sqlDialect = _kb.dbHelper;
		int retry = sqlDialect.retryCount();
		while (true) {
			try {
				boolean statementReturned = false;
				final ResultSet resultSet = _sql.executeQuery(readConnection, arguments);
				try {
					if (!sqlDialect.supportsLimitStart()) {
						// Manually skip first rows.
						for (int n = 0; n < startRow; n++) {
							if (!resultSet.next()) {
								return EmptyBufferingIterator.getInstance();
							}
						}
					}

					final int readCount = (stopRow >= 0) ? stopRow - startRow : Integer.MAX_VALUE;
					final DBKnowledgeBase kb = _kb;
					BufferingCloseableIterator<E> result;
					if (_fullLoad) {
						result = new FullObjectResult<>(resultSet, kb, readConnection, _resultType, resolve, javaType,
							historyContext, dataRevision, readCount);
					} else {
						result =
							new IdOnlyResult<>(resultSet, kb, readConnection, _resultType, requestedBranch, resolve,
								javaType, historyContext, dataRevision, readCount);
					}

					statementReturned = true;
					return result;
				} finally {
					if (!statementReturned) {
						resultSet.close();
					}
				}
			} catch (SQLException ex) {
				readConnection.closeConnection(ex);
				if ((retry <= 0) || (!sqlDialect.canRetry(ex))) {
					throw ex;
				}
				retry--;
			}
		}
	}

	/**
	 * {@link ResultSetBasedIterator} that reads object data from an underlying {@link ResultSet}.
	 */
	private static final class FullObjectResult<E> extends ResultSetBasedIterator<E> implements
			BufferingCloseableIterator<E> {

		private final DBKnowledgeBase _kb;

		private final PooledConnection _readConnection;

		/**
		 * The type of objects to read.
		 */
		private final MOKnowledgeItemImpl _resultType;
	
		/**
		 * Whether to resolve the resulting objects to their {@link TLObject} wrappers.
		 */
		private final boolean _resolve;
	
		/**
		 * The expected Java type of the resulting objects.
		 */
		private final Class<E> _javaType;
	
		/**
		 * The revision number of the desired objects.
		 */
		private final long _historyContext;
	
		/**
		 * The revision number in which to read the result.
		 */
		private final long _dataRevision;

		/**
		 * The number of items to retrieve.
		 */
		private int _readCount;
	
		FullObjectResult(ResultSet result, DBKnowledgeBase kb, PooledConnection readConnection, MOKnowledgeItemImpl resultType,
				boolean resolve, Class<E> javaType, long historyContext, long dataRevision, int readCount) {
			super(result);

			_kb = kb;
			_resultType = resultType;
			_resolve = resolve;
			_javaType = javaType;
			_readConnection = readConnection;
			_historyContext = historyContext;
			_dataRevision = dataRevision;

			_readCount = readCount;
		}
	
		@Override
		public List<E> getAll() {
			if (isClosed()) {
				throw new IllegalStateException("Already closed.");
			}

			ArrayList<E> buffer = new ArrayList<>();
			ResultSet resultSet = resultSet();
			while (findNext(resultSet)) {
				buffer.add(lookupNext());
			}
			resetNext();

			return buffer;
		}

		@Override
		protected boolean findNext(ResultSet resultSet2) {
			try {
				if (_readCount == 0) {
					return false;
				}
	
				boolean hasNextResult = resultSet2.next();
				if (!hasNextResult) {
					return false;
				}
	
				KnowledgeItemInternal item =
					_kb.findOrCreateItem(_readConnection, resultSet2, DBAttribute.DEFAULT_DB_OFFSET, _resultType,
						_historyContext, _dataRevision);
	
				setNext(_javaType.cast(_resolve ? item.getWrapper() : item));
	
				_readCount--;
				return true;
			} catch (SQLException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}
	}

	/**
	 * {@link ResultSetBasedIterator} that reads object data from an underlying {@link ResultSet}.
	 */
	private static final class IdOnlyResult<E> implements BufferingCloseableIterator<E> {

		private ResultSet _result;

		private final DBKnowledgeBase _kb;

		/**
		 * @see DBHelper#getMaxSetSize()
		 */
		private final int _maxSetSize;

		private final PooledConnection _db;

		private final MOKnowledgeItemImpl _resultType;

		/**
		 * Whether to resolve the resulting objects to their {@link TLObject} wrappers.
		 */
		private final boolean _resolve;

		/**
		 * The expected Java type of the resulting objects.
		 */
		private final Class<E> _javaType;

		/**
		 * The {@link KnowledgeItem#getHistoryContext()} of the items to return.
		 */
		private final long _historyContext;

		private final long _dataRevision;

		/**
		 * The current chunk of {@link DBObjectKey}s or {@link KnowledgeItem}s (if the key read from
		 * the search result could be directly resolved from the {@link KnowledgeBase} cache).
		 */
		private ArrayList<Object> _chunk;

		/**
		 * Number of unresolved objects in the current {@link #_chunk}.
		 */
		private int _idCnt;

		/**
		 * Index of next entry in {@link #_chunk} to retrieve.
		 */
		private int _pos = 0;

		/**
		 * Temporary array of {@link DBObjectKey}s for resolving in a single database call.
		 */
		private Object[] _resolveChunk;

		/**
		 * The overall limit on the number of items to return.
		 */
		private int _readLimit;

		private final Branch _requestedBranch;

		IdOnlyResult(ResultSet result, DBKnowledgeBase kb, PooledConnection readConnection,
				MOKnowledgeItemImpl resultType, Branch requestedBranch, boolean resolve, Class<E> javaType,
				long historyContext, long dataRevision, int readLimit) {

			_result = result;
			_kb = kb;
			_db = readConnection;
			_resultType = resultType;
			_requestedBranch = requestedBranch;
			_resolve = resolve;
			_javaType = javaType;
			_maxSetSize = kb.dbHelper.getMaxSetSize();
			_historyContext = historyContext;
			_dataRevision = dataRevision;
			_readLimit = readLimit;
		}

		@Override
		public List<E> getAll() {
			if (_chunk == null) {
				_chunk = new ArrayList<>();
			}
			tryFillChunk(Integer.MAX_VALUE);
			resolveAll();
			@SuppressWarnings("unchecked")
			List<E> buffer = (List<E>) _chunk;
			return buffer;
		}

		private void resolveAll() {
			int chunkSize = _chunk.size();
			while (_pos < chunkSize) {
				internalNext();
			}
		}

		private void nextChunk() {
			nextChunk(2 * _maxSetSize);
		}

		private void nextChunk(int maxChunkSize) {
			int chunkLimit = Math.min(_readLimit, maxChunkSize);
			if (chunkLimit > 0) {
				if (_chunk != null) {
					_chunk.clear();
					_pos = 0;
					_idCnt = 0;
				}
				tryFillChunk(chunkLimit);
			}
		}

		private void tryFillChunk(int chunkLimit) {
			try {
				fillChunk(chunkLimit);
			} catch (SQLException ex) {
				throw new KnowledgeBaseRuntimeException("Loading query result identifiers failed.", ex);
			}
		}

		private void fillChunk(int chunkLimit) throws SQLException {
			if (_result.next()) {
				if (_chunk == null) {
					_chunk = new ArrayList<>();
				}

				boolean singleBranch = _requestedBranch != null;

				boolean multipleBranches = _kb.getMORepository().multipleBranches();
				DBKey key = new DBKey();
				if (singleBranch) {
					key.setBranchContext(_requestedBranch.getBaseBranchId(_resultType));
				} else if (!multipleBranches) {
					key.setBranchContext(TLContext.TRUNK_ID);
				}

				key.setHistoryContext(_historyContext);
				key.setObjectType(_resultType);
				do {
					long id;
					if (singleBranch || !multipleBranches) {
						id = _result.getLong(1);
					} else {
						key.setBranchContext(_result.getLong(1));
						id = _result.getLong(2);
					}
					key.setObjectName(LongID.valueOf(id));

					KnowledgeItemInternal item = _kb.cleanupAndLookupCache(key, _dataRevision);
					if (item == null) {
						_chunk.add(key.toStableKey());
						_idCnt++;
					} else {
						_chunk.add(cast(item));
					}
				} while (_chunk.size() < chunkLimit && _result.next());

				if (lazyChunkSize() < chunkLimit) {
					// There is no more data available, prevent further reads.
					_readLimit = 0;
				} else {
					_readLimit -= chunkLimit;
				}
			}
		}

		@Override
		public boolean hasNext() {
			if (avail()) {
				return true;
			}
			nextChunk();
			return avail();
		}

		private boolean avail() {
			return _pos < lazyChunkSize();
		}

		/**
		 * Size of {@link #_chunk} at positions, where {@link #_chunk} may be <code>null</code>.
		 */
		private int lazyChunkSize() {
			return _chunk == null ? 0 : _chunk.size();
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return internalNext();
		}

		private E internalNext() {
			Object object = _chunk.get(_pos);
			if (object instanceof DBObjectKey) {
				// Unresolved item, resolve next chunk.
				if (_resolveChunk == null) {
					int resolveChunkSize = _maxSetSize;
					// Since the resolve chunk array is reused for all resolve operations, its size
					// must only be limited by the pending entries in the current chunk, if the
					// current chunk is the last chunk to resolve.
					if (_readLimit == 0) {
						if (_idCnt < resolveChunkSize) {
							resolveChunkSize = _idCnt;
						}
					}
					_resolveChunk = new Object[resolveChunkSize];
				}

				// The single branch, from which objects are being resolved in this chunk.
				long resolveBranch = ((DBObjectKey) object).getBranchContext();

				int keyCnt = 0;
				for (int lookahead = _pos, resolveChunkSize = _resolveChunk.length, stopLookahead =
					_chunk.size(); keyCnt < resolveChunkSize && lookahead < stopLookahead; lookahead++) {

					Object check = _chunk.get(lookahead);
					if (check instanceof DBObjectKey) {
						DBObjectKey key = (DBObjectKey) check;
						if (key.getBranchContext() == resolveBranch) {
							_resolveChunk[keyCnt++] = check;
						}
					}
				}

				try {
					_resultType.getDBAccess().fetchAll(_kb, _db, _dataRevision, _resolveChunk, keyCnt);
				} catch (SQLException ex) {
					throw new KnowledgeBaseRuntimeException("Cannot load object data.", ex);
				}

				// Pin result.
				for (int lookahead = _pos, resolved = 0; resolved < keyCnt; lookahead++) {
					Object check = _chunk.get(lookahead);
					if (check instanceof DBObjectKey) {
						DBObjectKey key = (DBObjectKey) check;
						// Note: Keys from other branches in the original chunk must be skipped.
						// While the resolve chunk is branch-uniform, this is not necessarily the
						// case for the original chunk.
						if (key.getBranchContext() == resolveBranch) {
							KnowledgeItem item = (KnowledgeItem) _resolveChunk[resolved++];
							assert item != null && item.tId().equals(key) : "Unexpected item resolved, expected key '"
								+ key + "', got item '" + item + "'.";
							_chunk.set(lookahead, cast(item));
						}
					}
				}

				@SuppressWarnings("unchecked")
				E result = (E) _chunk.get(_pos++);
				return result;
			} else {
				_pos++;
				@SuppressWarnings("unchecked")
				E result = (E) object;
				return result;
			}
		}

		private E cast(Object object) {
			KnowledgeItem foundItem = (KnowledgeItem) object;
			return _javaType.cast(_resolve ? foundItem.getWrapper() : foundItem);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			ResultSet resultSet = _result;
			_result = null;
			_readLimit = 0;
			try {
				resultSet.close();
			} catch (SQLException ex) {
				Logger.warn("Unable to close result set.", ex, MonomorphicSearch.class);
			}
		}
	}
}