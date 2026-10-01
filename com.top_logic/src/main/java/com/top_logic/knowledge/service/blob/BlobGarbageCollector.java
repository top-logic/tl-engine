/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.blob;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.dob.sql.SQLFactory.table;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.stream.Stream;

import com.top_logic.basic.Log;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.BlobReferenceAttribute;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;

/**
 * Deletes the blobs of a {@link BlobStore} that are no longer referenced from the database of a
 * {@link DBKnowledgeBase}.
 *
 * <p>
 * Referenced are all keys stored in the key column of a {@link BlobReferenceAttribute} of any table
 * of the {@link MORepository}, in all rows including historic revisions. This covers the declared
 * binary attributes and the table of dynamic binary attribute values. Keys are random and never
 * reused, so a key of one store never matches a blob of another store: the keys of all tables are
 * a safe over-approximation of the keys referencing a single store.
 * </p>
 *
 * <p>
 * The keys of the store and the referenced keys are both read in lexicographic order and merged
 * with constant memory. Both sequences must be non-decreasing in the order of
 * {@link String#compareTo(String)}. If either is not, for example because the collation of the
 * database orders the keys differently, the run is aborted without deleting anything.
 * </p>
 *
 * <p>
 * An unreferenced blob is deleted only if it was written before the end of the grace period. This
 * protects content that is uploaded but whose transaction has not committed yet. Deletion happens
 * after the merge has completed, outside of any transaction, blob by blob. A failed deletion is
 * logged and counted, the run continues with the next blob. Afterwards the temporary artifacts of
 * the store older than the grace period are {@link BlobStore#cleanup(Instant) cleaned up}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlobGarbageCollector {

	/**
	 * Default time span an unreferenced blob is kept after it was written.
	 */
	public static final Duration DEFAULT_GRACE_PERIOD = Duration.ofHours(24);

	private static final String RESULT_KEY = "blobKey";

	private static final String CANDIDATES_FILE_PREFIX = "blob-gc-";

	private static final String CANDIDATES_FILE_SUFFIX = ".keys";

	/**
	 * A database column holding blob keys.
	 *
	 * @param table
	 *        The table containing the column.
	 * @param attribute
	 *        The attribute whose {@link BlobReferenceAttribute#getKeyColumn() key column} is read.
	 */
	public record KeyColumn(MOKnowledgeItem table, BlobReferenceAttribute attribute) {

		/**
		 * The column holding the keys.
		 */
		public DBAttribute column() {
			return attribute.getKeyColumn();
		}

		@Override
		public String toString() {
			return table.getDBMapping().getDBName() + "." + column().getDBName();
		}
	}

	/**
	 * The outcome of a {@link BlobGarbageCollector#collect(BlobStore, Duration, Instant, Log)
	 * collection run} for a single store.
	 *
	 * @param storeName
	 *        The name of the store.
	 * @param aborted
	 *        Whether the run was aborted without deleting anything, because the keys of the store or
	 *        the referenced keys were not delivered in order.
	 * @param listed
	 *        The number of blobs listed in the store.
	 * @param referenced
	 *        The number of listed blobs that are referenced.
	 * @param deleted
	 *        The number of unreferenced blobs that were deleted.
	 * @param keptByGrace
	 *        The number of unreferenced blobs that were kept, because they are younger than the
	 *        grace period.
	 * @param failures
	 *        The number of deletions and cleanups that failed.
	 */
	public record Result(String storeName, boolean aborted, long listed, long referenced, long deleted,
			long keptByGrace, long failures) {

		@Override
		public String toString() {
			return "Blob garbage collection of store '" + storeName + "'" + (aborted ? " (aborted)" : "") + ": "
				+ listed + " listed, " + referenced + " referenced, " + deleted + " deleted, "
				+ keptByGrace + " kept by grace period, " + failures + " failures.";
		}
	}

	private final ConnectionPool _pool;

	private final List<KeyColumn> _keyColumns;

	/**
	 * Creates a {@link BlobGarbageCollector}.
	 *
	 * @param pool
	 *        The pool to get the connection for reading the referenced keys from.
	 * @param repository
	 *        The types whose tables reference blobs.
	 */
	public BlobGarbageCollector(ConnectionPool pool, MORepository repository) {
		_pool = pool;
		_keyColumns = Collections.unmodifiableList(findKeyColumns(repository));
	}

	/**
	 * Creates a {@link BlobGarbageCollector} for the given {@link DBKnowledgeBase}.
	 */
	public static BlobGarbageCollector newInstance(DBKnowledgeBase kb) {
		return new BlobGarbageCollector(kb.getConnectionPool(), kb.getMORepository());
	}

	private static List<KeyColumn> findKeyColumns(MORepository repository) {
		List<KeyColumn> result = new ArrayList<>();
		for (MetaObject type : repository.getMetaObjects()) {
			if (!(type instanceof MOKnowledgeItem)) {
				continue;
			}
			if (MetaObjectUtils.isAbstract(type)) {
				continue;
			}
			MOKnowledgeItem table = (MOKnowledgeItem) type;
			for (MOAttribute attribute : table.getAttributes()) {
				if (!(attribute instanceof BlobReferenceAttribute)) {
					continue;
				}
				BlobReferenceAttribute reference = (BlobReferenceAttribute) attribute;
				if (reference.getKeyColumn() == null) {
					continue;
				}
				result.add(new KeyColumn(table, reference));
			}
		}
		result.sort(Comparator.comparing(KeyColumn::toString));
		return result;
	}

	/**
	 * The columns whose values are the referenced keys.
	 */
	public List<KeyColumn> getKeyColumns() {
		return _keyColumns;
	}

	/**
	 * Deletes the unreferenced blobs of the given store.
	 *
	 * @param store
	 *        The store to collect.
	 * @param gracePeriod
	 *        The time span an unreferenced blob is kept after it was written.
	 * @param now
	 *        The current time, from which the grace period is computed.
	 * @param log
	 *        Receives the summary and the problems of the run.
	 * @return What was done.
	 */
	public Result collect(BlobStore store, Duration gracePeriod, Instant now, Log log)
			throws IOException, SQLException {
		PooledConnection connection = _pool.borrowReadConnection();
		try (ReferencedKeys referenced = new ReferencedKeys(connection, _keyColumns)) {
			return collect(store, referenced, gracePeriod, now, log);
		} catch (SQLFailure ex) {
			throw ex.getCause();
		} finally {
			_pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Deletes the blobs of the given store that are not contained in the given referenced keys.
	 *
	 * @param store
	 *        The store to collect.
	 * @param referencedKeys
	 *        The referenced keys in non-decreasing order of {@link String#compareTo(String)}. The
	 *        keys may contain duplicates and keys of other stores.
	 * @param gracePeriod
	 *        The time span an unreferenced blob is kept after it was written.
	 * @param now
	 *        The current time, from which the grace period is computed.
	 * @param log
	 *        Receives the summary and the problems of the run.
	 * @return What was done.
	 */
	public static Result collect(BlobStore store, Iterator<String> referencedKeys, Duration gracePeriod,
			Instant now, Log log) throws IOException {
		Instant deleteBefore = now.minus(gracePeriod);
		String storeName = store.getName();

		Path candidatesFile = Files.createTempFile(CANDIDATES_FILE_PREFIX, CANDIDATES_FILE_SUFFIX);
		try {
			Merge merge = new Merge(referencedKeys, deleteBefore);
			try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(candidatesFile));
					DataOutputStream candidates = new DataOutputStream(out);
					Stream<BlobInfo> blobs = store.list()) {
				merge.run(blobs.iterator(), candidates);
			} catch (OrderViolation ex) {
				log.error("Blob garbage collection of store '" + storeName
					+ "' aborted without deleting anything: " + ex.getMessage());
				Result result = new Result(storeName, true, merge._listed, merge._referenced, 0,
					merge._keptByGrace, 0);
				log.info(result.toString());
				return result;
			}

			long failures = 0;
			long deleted = 0;
			try (InputStream in = new BufferedInputStream(Files.newInputStream(candidatesFile));
					DataInputStream candidates = new DataInputStream(in)) {
				for (long n = 0; n < merge._candidates; n++) {
					String key = candidates.readUTF();
					try {
						store.delete(key);
						deleted++;
					} catch (IOException | RuntimeException ex) {
						failures++;
						log.error("Deleting unreferenced blob '" + key + "' from store '" + storeName + "' failed.",
							ex);
					}
				}
			}

			try {
				store.cleanup(deleteBefore);
			} catch (IOException | RuntimeException ex) {
				failures++;
				log.error("Cleaning up temporary artifacts of store '" + storeName + "' failed.", ex);
			}

			Result result = new Result(storeName, false, merge._listed, merge._referenced, deleted,
				merge._keptByGrace, failures);
			log.info(result.toString());
			return result;
		} finally {
			Files.deleteIfExists(candidatesFile);
		}
	}

	/**
	 * Sort-merge of the blobs of a store with the referenced keys.
	 */
	private static final class Merge {

		private final OrderedKeys _references;

		private final Instant _deleteBefore;

		long _listed;

		long _referenced;

		long _keptByGrace;

		long _candidates;

		Merge(Iterator<String> referenced, Instant deleteBefore) {
			_references = new OrderedKeys("referenced keys", referenced);
			_deleteBefore = deleteBefore;
		}

		/**
		 * Writes the keys of all unreferenced blobs older than the grace period to the given
		 * output.
		 *
		 * @throws OrderViolation
		 *         If one of the inputs is not in order.
		 */
		void run(Iterator<BlobInfo> blobs, DataOutputStream candidates) throws IOException {
			String lastBlobKey = null;
			String reference = _references.next();
			while (blobs.hasNext()) {
				BlobInfo blob = blobs.next();
				String key = blob.key();
				if (lastBlobKey != null && lastBlobKey.compareTo(key) > 0) {
					throw new OrderViolation("blob listing", lastBlobKey, key);
				}
				lastBlobKey = key;
				_listed++;

				while (reference != null && reference.compareTo(key) < 0) {
					reference = _references.next();
				}
				if (reference != null && reference.equals(key)) {
					_referenced++;
				} else if (blob.lastModified().isBefore(_deleteBefore)) {
					candidates.writeUTF(key);
					_candidates++;
				} else {
					_keptByGrace++;
				}
			}

			// A key out of order after the last blob may equal one of the deletion candidates.
			while (reference != null) {
				reference = _references.next();
			}
		}
	}

	/**
	 * Delivers the keys of an {@link Iterator}, checking that they are in non-decreasing order.
	 */
	private static final class OrderedKeys {

		private final String _description;

		private final Iterator<String> _keys;

		private String _last;

		OrderedKeys(String description, Iterator<String> keys) {
			_description = description;
			_keys = keys;
		}

		/**
		 * The next key, <code>null</code> at the end.
		 *
		 * @throws OrderViolation
		 *         If the next key is smaller than its predecessor.
		 */
		String next() {
			if (!_keys.hasNext()) {
				return null;
			}
			String key = _keys.next();
			if (_last != null && _last.compareTo(key) > 0) {
				throw new OrderViolation(_description, _last, key);
			}
			_last = key;
			return key;
		}
	}

	/**
	 * The referenced keys of all {@link KeyColumn}s in order.
	 *
	 * <p>
	 * Each column is read by a query of its own, ordered by key. The queries run concurrently on a
	 * single connection and are merged.
	 * </p>
	 */
	private static final class ReferencedKeys implements Iterator<String>, AutoCloseable {

		private final List<ColumnCursor> _cursors = new ArrayList<>();

		private final PriorityQueue<ColumnCursor> _queue =
			new PriorityQueue<>(Comparator.comparing(ColumnCursor::current));

		private boolean _started;

		ReferencedKeys(PooledConnection connection, List<KeyColumn> columns) throws SQLException {
			try {
				DBHelper sqlDialect = connection.getSQLDialect();
				for (KeyColumn column : columns) {
					_cursors.add(
						new ColumnCursor(column, selectKeys(sqlDialect, column).executeQuery(connection)));
				}
			} catch (SQLException | RuntimeException ex) {
				close();
				throw ex;
			}
		}

		/**
		 * {@code SELECT key FROM table WHERE key IS NOT NULL ORDER BY key}
		 */
		private static CompiledStatement selectKeys(DBHelper sqlDialect, KeyColumn column) {
			DBAttribute keyColumn = column.column();
			return query(
				select(false,
					columns(columnDef(column(NO_TABLE_ALIAS, keyColumn), RESULT_KEY)),
					table((DBTableMetaObject) column.table(), NO_TABLE_ALIAS),
					not(isNull(column(NO_TABLE_ALIAS, keyColumn))),
					orders(order(false, CollationHint.BINARY, column(NO_TABLE_ALIAS, keyColumn)))))
						.toSql(sqlDialect);
		}

		/**
		 * Positions all cursors on their first key.
		 *
		 * <p>
		 * Done on first access, so that an {@link OrderViolation} is reported to the merge.
		 * </p>
		 */
		private void start() {
			if (_started) {
				return;
			}
			_started = true;
			try {
				for (ColumnCursor cursor : _cursors) {
					if (cursor.advance()) {
						_queue.add(cursor);
					}
				}
			} catch (SQLException ex) {
				throw new SQLFailure(ex);
			}
		}

		@Override
		public boolean hasNext() {
			start();
			return !_queue.isEmpty();
		}

		@Override
		public String next() {
			start();
			ColumnCursor cursor = _queue.poll();
			if (cursor == null) {
				throw new NoSuchElementException();
			}
			String result = cursor.current();
			try {
				if (cursor.advance()) {
					_queue.add(cursor);
				}
			} catch (SQLException ex) {
				throw new SQLFailure(ex);
			}
			return result;
		}

		@Override
		public void close() throws SQLException {
			SQLException problem = null;
			for (ColumnCursor cursor : _cursors) {
				try {
					cursor.close();
				} catch (SQLException ex) {
					if (problem == null) {
						problem = ex;
					} else {
						problem.addSuppressed(ex);
					}
				}
			}
			_cursors.clear();
			_queue.clear();
			if (problem != null) {
				throw problem;
			}
		}
	}

	/**
	 * The keys of a single {@link KeyColumn}, checked to be in order.
	 */
	private static final class ColumnCursor {

		private final KeyColumn _column;

		private final ResultSet _result;

		private String _current;

		ColumnCursor(KeyColumn column, ResultSet result) {
			_column = column;
			_result = result;
		}

		String current() {
			return _current;
		}

		/**
		 * Moves to the next non-empty key.
		 *
		 * @return Whether there is a next key.
		 * @throws OrderViolation
		 *         If the next key is smaller than the current one.
		 */
		boolean advance() throws SQLException {
			while (_result.next()) {
				String key = _result.getString(1);
				if (key == null || key.isEmpty()) {
					continue;
				}
				if (_current != null && _current.compareTo(key) > 0) {
					throw new OrderViolation("keys of column " + _column, _current, key);
				}
				_current = key;
				return true;
			}
			return false;
		}

		void close() throws SQLException {
			_result.close();
		}
	}

	/**
	 * Signals that keys are not delivered in the order of {@link String#compareTo(String)}.
	 */
	private static final class OrderViolation extends RuntimeException {

		OrderViolation(String source, String before, String after) {
			super("The " + source + " are not in lexicographic order: '" + before + "' is followed by '" + after
				+ "'.");
		}
	}

	/**
	 * Transports an {@link SQLException} through {@link Iterator#next()}.
	 */
	private static final class SQLFailure extends RuntimeException {

		SQLFailure(SQLException cause) {
			super(cause);
		}

		@Override
		public synchronized SQLException getCause() {
			return (SQLException) super.getCause();
		}
	}

}
