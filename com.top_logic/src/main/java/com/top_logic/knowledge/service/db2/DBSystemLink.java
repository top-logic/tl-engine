/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.knowledge.objects.identifier.MutableObjectKey;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.util.TLContext;

/**
 * Implementation of a knowledge base internal association link. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class DBSystemLink extends StaticSystemItem {

	static final KnowledgeItemFactory FACTORY = new KnowledgeItemFactory() {
		
		@Override
		public AbstractDBKnowledgeItem newKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
			return new DBSystemLink(kb, staticType);
		}
	
		@Override
		public AbstractDBKnowledgeItem newImmutableItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
			return new DBSystemLink(kb, staticType);
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

		private CompositeKey id(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOKnowledgeItem staticType)
				throws SQLException {
			List<MOAttribute> keyAttributes = staticType.getPrimaryKey().getAttributes();
			int size = keyAttributes.size();
			Object[] keyStorage = new Object[size];
			for (int i = 0; i < size; i++) {
				MOAttribute attribute = keyAttributes.get(i);
				Object attributeValue =
					attribute.getStorage().fetchValue(sqlDialect, dbResult, resultOffset, attribute,
						AttributeStorage.NO_CONTEXT);
				keyStorage[i] = attributeValue;
			}

			return new CompositeKey(keyStorage);
		}
	};

	private final Object[] _values;

	DBSystemLink(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
		_values = newEmptyStorage();
	}

	@Override
	protected Object[] getGlobalValues(long sessionRevision) {
		// There is only one storage for all sessions.
		return _values;
	}

	@Override
	public long getLastUpdate() {
		return Revision.FIRST_REV;
	}

	@Override
	public long getCreateCommitNumber() {
		return getLastUpdate();
	}
	
}