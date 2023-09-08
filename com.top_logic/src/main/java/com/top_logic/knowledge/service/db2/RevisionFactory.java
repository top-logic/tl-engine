/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.ImmutableLongStorage;
import com.top_logic.knowledge.objects.identifier.MutableObjectKey;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.util.TLContext;

/**
 * {@link KnowledgeItemFactory} for {@link Revision} items.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public final class RevisionFactory implements KnowledgeItemFactory {

	/**
	 * Singleton {@link RevisionFactory} instance.
	 */
	public static final RevisionFactory INSTANCE = new RevisionFactory();

	private RevisionFactory() {
		// Singleton constructor.
	}

	@Override
	public AbstractDBKnowledgeItem newKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		return new RevisionImpl(kb, staticType);
	}

	@Override
	public AbstractDBKnowledgeItem newImmutableItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		return new RevisionImpl(kb, staticType);
	}

	@Override
	public DBObjectKey createIdentifier(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			long historyContext, MOKnowledgeItem staticType) throws SQLException {
		long branch = TLContext.TRUNK_ID;
		TLID id = id(sqlDialect, dbResult, resultOffset, staticType);
		return new DBObjectKey(branch, historyContext, staticType, id);
	}

	@Override
	public void loadIdentifier(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, long historyContext,
			MOKnowledgeItem staticType, MutableObjectKey<?> key) throws SQLException {
		long branch = TLContext.TRUNK_ID;
		TLID id = id(sqlDialect, dbResult, resultOffset, staticType);
		key.update(branch, historyContext, staticType, id);
	}

	private TLID id(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOKnowledgeItem revisionType) throws SQLException {
		MOAttribute revisionAttr = revisionType.getAttributeOrNull(RevisionType.REVISION_ATTRIBUTE_NAME);
		ImmutableLongStorage revStorage = (ImmutableLongStorage) revisionAttr.getStorage();
		long revNumber = revStorage.fetchLong(sqlDialect, dbResult, resultOffset, revisionAttr);
		return LongID.valueOf(revNumber);
	}
}