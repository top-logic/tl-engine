/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service.importer;

import java.sql.SQLException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.DefaultEventWriter;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * {@link DefaultEventWriter} writing the last committed revision to the {@link DBProperties}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class KBDataWriter extends DefaultEventWriter {

	private String _revisionKey;

	/**
	 * Creates a new {@link KBDataWriter}.
	 * 
	 * @param kb
	 *        {@link KnowledgeBase} to write to.
	 */
	public KBDataWriter(KnowledgeBase kb) {
		super(kb);
	}

	/**
	 * Sets the key for the revision in the {@link DBProperties}.
	 */
	public void setRevisionKey(String revisionKey) {
		_revisionKey = revisionKey;
	}

	@Override
	public void write(ChangeSet cs) {
		super.write(cs);
		Logger.debug("Applied changeset.", KBDataWriter.class);
	}

	@Override
	protected State doCommit(CommitEvent event, State currentState) {
		writeProcessedRevision(event);
		return super.doCommit(event, currentState);
	}

	private void writeProcessedRevision(CommitEvent event) {
		assert !StringServices.isEmpty(_revisionKey) : "No revision key set";
		String processedRevision = Long.toString(event.getRevision());
		CommitContext commitContext = KBUtils.getCurrentContext(_kb);
		if (commitContext != null) {
			writeInExistingTransaction(commitContext, processedRevision);
		} else {
			/* The changeset did not contain relevant changes. But it has to be stored that it was
			 * processed. Otherwise it would be detected as "missing" when the next changeset is
			 * received. That would stop the TL-Sync receiver. */
			writeInOwnTransaction(processedRevision);
		}
	}

	private void writeInExistingTransaction(CommitContext commitContext, String processedRevision) {
		PooledConnection connection = commitContext.getConnection();
		try {
			DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY, _revisionKey, processedRevision);
		} catch (SQLException ex) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("Unable to set revision property '");
			errorMsg.append(_revisionKey);
			errorMsg.append("' to '");
			errorMsg.append(processedRevision);
			errorMsg.append("'.");
			throw _protocol.fatal(errorMsg.toString(), ex);
		}
	}

	private void writeInOwnTransaction(String processedRevision) {
		new DBProperties(getConnectionPool()).setProperty(DBProperties.GLOBAL_PROPERTY, _revisionKey, processedRevision);
	}

	private ConnectionPool getConnectionPool() {
		return ((DBKnowledgeBase) PersistencyLayer.getKnowledgeBase()).getConnectionPool();
	}

}

