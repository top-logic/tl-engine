/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.id.factory.IdFactory;

/**
 * Factory for cluster-safe fast identifier creation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentIdFactory implements IdFactory {

	/** The ids are created in chunks this large. */
	public static final int CHUNK_SIZE = 1000;

	private final ConnectionPool _connectionPool;

	private final SequenceManager _sequenceManager = new RowLevelLockingSequenceManager();

	private final Object _idLock;

	private long _stopId;

	private long _nextId;

	private String _sequence;

	private int _lastInc = 1;

	private long _lastIncTime;

	/**
	 * Creates a {@link PersistentIdFactory}.
	 * 
	 * @param connectionPool
	 *        The {@link ConnectionPool} for cluster synchronization.
	 * @param sequence
	 *        The sequence name to create identifiers from. Uniqueness is guaranteed only for
	 *        {@link PersistentIdFactory factories} using the same sequence.
	 */
	public PersistentIdFactory(ConnectionPool connectionPool, String sequence) {
		_connectionPool = connectionPool;
		_sequence = sequence;

		_idLock = new Object();
	}

	@Override
	public long createId() {
		synchronized (_idLock) {
			if (_nextId == _stopId) {
				long now = System.currentTimeMillis();

				// The number of ID chunks consumed in the next minute.
				int chunksAllocated =
					(int) Math.min(1024, Math.max(1, (60 * 1000 * _lastInc) / Math.max(1, now - _lastIncTime)));
				if (chunksAllocated > 1) {
					// Limit increase of allocation speed.
					if (chunksAllocated > _lastInc * 2) {
						chunksAllocated = _lastInc * 2;
					}

					Logger.info("Allocating " + chunksAllocated + " identifier chunks at once due to high demand.",
						PersistentIdFactory.class);
				}
				_lastInc = chunksAllocated;
				_lastIncTime = now;

				long nextChunk;
				PooledConnection connection = _connectionPool.borrowWriteConnection();
				try {
					DBHelper sqlDialect = _connectionPool.getSQLDialect();
					nextChunk = _sequenceManager.nextSequenceNumber(
						sqlDialect, connection, sqlDialect.retryCount(), _sequence, chunksAllocated);
					connection.commit();
				} catch (SQLException ex) {
					throw new KnowledgeBaseRuntimeException(ex);
				} finally {
					_connectionPool.releaseWriteConnection(connection);
				}
				_stopId = 1 + nextChunk * CHUNK_SIZE;
				_nextId = _stopId - CHUNK_SIZE * chunksAllocated;
			}
			return _nextId++;
		}
	}
}