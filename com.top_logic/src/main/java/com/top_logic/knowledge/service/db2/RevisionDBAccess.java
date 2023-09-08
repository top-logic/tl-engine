/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;

/**
 * {@link DBAccess} to encapsulate access to revision table.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
@FrameworkInternal
public final class RevisionDBAccess extends SystemTypeDBAccess {

	/**
	 * {@link DBAccessFactory} for {@link RevisionDBAccess} implementations.
	 */
	@FrameworkInternal
	public static class Factory implements DBAccessFactory {

		/**
		 * Singleton {@link RevisionDBAccess.Factory} instance.
		 */
		public static final RevisionDBAccess.Factory INSTANCE = new RevisionDBAccess.Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		public DBAccess createDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl type, MORepository repository) {
			return new RevisionDBAccess(sqlDialect, type);
		}

	}

	RevisionDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		super(sqlDialect, table);
	}

	@Override
	protected String getIdAttributeName() {
		return RevisionType.REVISION_ATTRIBUTE_NAME;
	}

	@Override
	public SQLExpression createRevMinExpr(TableSymbol table) {
		return createColumn(RevisionType.getRevisionAttribute(type), table);
	}

	/**
	 * Method to update the given revision.
	 */
	void updateFirstRevision(PooledConnection db, RevisionImpl revision) throws SQLException {
		// use commit number of revision to simulate that the data were set in the correct
		// revision.
		long commitNumber = revision.getCommitNumber();
		update(db, commitNumber, getLocalData(revision), revision);
	}

}