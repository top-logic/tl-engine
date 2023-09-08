/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.event.AbstractEventReader;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * Base class for {@link KnowledgeEvent} readers.
 * 
 * @see #readEvent() Reading events.
 * @see #close() Closing the reader.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractKnowledgeEventReader<E> extends AbstractEventReader<E> {

	protected final DBKnowledgeBase kb;
	
	protected final long startRev;
	protected final long stopRev;
	
	private PooledConnection readConnection;

	/**
	 * Creates an {@link AbstractKnowledgeEventReader} for the given
	 * {@link KnowledgeBase} and the given range of revisions.
	 * 
	 * @param kb
	 *        See {@link #getKnowledgeBase()}
	 * @param startRev
	 *        See {@link #getStartRev()}
	 * @param stopRev
	 *        See {@link #getStopRev()}
	 */
	public AbstractKnowledgeEventReader(DBKnowledgeBase kb, long startRev, long stopRev) throws SQLException {
		if (startRev < Revision.FIRST_REV) {
			throw new IllegalArgumentException("Start revision must not be lower than " + Revision.FIRST_REV);
		}
		
		this.kb = kb;
		this.startRev = startRev;
		this.stopRev = stopRev;
		
		boolean success = false;
		try {
			init();
			success = true;
		} finally {
			if (! success) {
				// Free potentially allocated resources, caller has no chance to
				// close reader, because the object construction fails.
				cleanup();
			}
		}
	}
	
	/**
	 * The {@link KnowledgeBase} to read events from.
	 */
	public DBKnowledgeBase getKnowledgeBase() {
		return kb;
	}
	
	/**
	 * The revision to start reading events (inclusive).
	 */
	public long getStartRev() {
		return startRev;
	}
	
	/**
	 * The revision to stop reading events (exclusive).
	 */
	public long getStopRev() {
		return stopRev;
	}

	protected PooledConnection getReadConnection() {
		return readConnection;
	}

	private void init() {
		ConnectionPool connectionPool = kb.getConnectionPool();
		this.readConnection = connectionPool.borrowReadConnection();
	}

	private void cleanup() {
		if (readConnection == null) {
			// nothing to clean up or cleanup twice.
			return;
		}
		PooledConnection connection = this.readConnection;
		this.readConnection = null;
		kb.getConnectionPool().releaseReadConnection(connection);
	}

	@Override
	public void close() {
		cleanup();
	}

}
