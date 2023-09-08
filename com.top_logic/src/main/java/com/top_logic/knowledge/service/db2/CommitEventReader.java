/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.*;
import static com.top_logic.dob.sql.SQLFactory.column;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventReader;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link EventReader} that reports commits as {@link CommitEvent}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CommitEventReader extends AbstractKnowledgeEventReader<CommitEvent> {

	private ItemResult revisions;

	private DBAttribute _revisionAttr;

	private DBAttribute _authorAttr;

	private DBAttribute _dateAttr;

	private DBAttribute _logAttr;

	/**
	 * Creates a {@link CommitEventReader}.
	 *
	 * @param kb See {@link #getKnowledgeBase()}
	 * @param startRev See {@link #getStartRev()}
	 * @param stopRev See {@link #getStopRev()}
	 */
	public CommitEventReader(DBKnowledgeBase kb, long startRev, long stopRev) throws SQLException {
		super(kb, startRev, stopRev);
		
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

	private void init() throws SQLException {
		initRevisionQueue();
	}

	private void initRevisionQueue() throws SQLException {
		MOClass revisionType = BasicTypes.getRevisionType(kb);
		_revisionAttr = RevisionType.getRevisionAttribute(revisionType);
		_authorAttr = RevisionType.getAuthorAttribute(revisionType);
		_dateAttr = RevisionType.getDateAttribute(revisionType);
		_logAttr = RevisionType.getLogAttribute(revisionType);
		String tableAlias = NO_TABLE_ALIAS;
		SQLExpression revisionFilter = attributeRange(tableAlias, _revisionAttr, startRev, stopRev);
		List<SQLOrder> revisionOrder = orders(order(false, column(tableAlias, _revisionAttr, NOT_NULL)));
		
		ItemQuery revisionQuery =
			new ItemQuery(kb.dbHelper, revisionType, tableAlias, revisionFilter, revisionOrder);
		revisions = revisionQuery.query(getReadConnection());
	}

	@Override
	public CommitEvent readEvent() {
		try {
			return nextCommitEvent();
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private CommitEvent nextCommitEvent() throws SQLException {
		if (revisions.next()) {
			long nextRev = revisions.getLongValue(_revisionAttr);
			String nextAuthor = revisions.getStringValue(_authorAttr);
			long nextDate = revisions.getLongValue(_dateAttr);
			String nextLog = revisions.getStringValue(_logAttr);
			
			return new CommitEvent(nextRev, nextAuthor, nextDate, nextLog);
		} else {
			return null;
		}
	}
	
	@Override
	public void close() {
		try {
			try {
				super.close();
			} finally {
				cleanup();
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}
	
	private void cleanup() throws SQLException {
		if (revisions == null) {
			return;
		}
		ItemResult result = revisions;
		revisions = null;
		result.close();
	}
	
}
