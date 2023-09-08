/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import com.top_logic.basic.TLID;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link DBAccess} implementation that controls the storage of unversioned
 * application objects.
 * 
 * @see VersionedDBAccess Management of verioned application objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class UnversionedDBAccess extends VersionedDBAccess {

	public UnversionedDBAccess(DBHelper sqlDialect, MOKnowledgeItemImpl table) {
		super(sqlDialect, table);
	}
	
	@Override
	public KnowledgeItemInternal fetch(DBKnowledgeBase knowledgeBase, PooledConnection readConnection, long branchContext,
			TLID id, long itemRevision, long dataRevision) throws SQLException {
		if (itemRevision != Revision.CURRENT_REV) {
			// Unversioned objects do not exist in any stable revision. This is
			// required, because stable revisions of unversioned objects would
			// become invalid with the next update of the object.
			return null;
		}

		return super.fetch(knowledgeBase, readConnection, branchContext, id, itemRevision, dataRevision);
	}

	@Override
	public void fetchAll(DBKnowledgeBase kb, PooledConnection db, long dataRevision, Object[] keys, int keyCnt) throws SQLException {
		if (((ObjectKey) keys[0]).getHistoryContext() != Revision.CURRENT_REV) {
			// Unversioned objects do not exist in any stable revision. This is
			// required, because stable revisions of unversioned objects would
			// become invalid with the next update of the object.
			for (int n = 0, cnt = keys.length; n < cnt; n++) {
				keys[n] = null;
			}
			return;
		}

		super.fetchAll(kb, db, dataRevision, keys, keyCnt);
	}
	
	@Override
	protected void internalBranch(PooledConnection db, long branchId, long createRev, long baseBranchId, long baseRevision, SQLExpression filterExpr) throws SQLException {
		//  TODO BHU: Remove method deactivation after implementing branching for unversioned types.
	}

}
