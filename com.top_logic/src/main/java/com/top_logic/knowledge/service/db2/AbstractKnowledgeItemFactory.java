/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.identifier.MutableObjectKey;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * Abstract {@link KnowledgeItemFactory} that performs some checks and delegates to the actual
 * create method.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public abstract class AbstractKnowledgeItemFactory implements KnowledgeItemFactory {

	@Override
	public AbstractDBKnowledgeItem newKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		if (staticType.isAbstract()) {
			throw new IllegalArgumentException("Abstract types cannot be instantiated.");
		}
		return internalNewItem(kb, staticType);
	}

	@Override
	public AbstractDBKnowledgeItem newImmutableItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		if (staticType.isAbstract()) {
			throw new IllegalArgumentException("Abstract types cannot be instantiated.");
		}
		return internalNewImmutableItem(kb, staticType);
	}

	/**
	 * Creates the item returned in {@link #newKnowledgeItem(DBKnowledgeBase, MOKnowledgeItem)}
	 * 
	 * @param kb
	 *        see {@link #newKnowledgeItem(DBKnowledgeBase, MOKnowledgeItem)}
	 * @param staticType
	 *        The type to create item for. It is ensured that the type is not
	 *        {@link MOClass#isAbstract() abstract}.
	 * 
	 * @return see {@link #newKnowledgeItem(DBKnowledgeBase, MOKnowledgeItem)}
	 */
	protected abstract AbstractDBKnowledgeItem internalNewItem(DBKnowledgeBase kb, MOKnowledgeItem staticType);

	/**
	 * Creates the item returned in {@link #newImmutableItem(DBKnowledgeBase, MOKnowledgeItem)}
	 * 
	 * @param kb
	 *        see {@link #newImmutableItem(DBKnowledgeBase, MOKnowledgeItem)}
	 * @param staticType
	 *        The type to create item for. It is ensured that the type is not
	 *        {@link MOClass#isAbstract() abstract}.
	 * 
	 * @return see {@link #newImmutableItem(DBKnowledgeBase, MOKnowledgeItem)}
	 */
	protected abstract AbstractDBKnowledgeItem internalNewImmutableItem(DBKnowledgeBase kb, MOKnowledgeItem staticType);
	
	@Override
	public DBObjectKey createIdentifier(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			long historyContext, MOKnowledgeItem staticType) throws SQLException {
		long branch = branch(sqlDialect, dbResult, resultOffset, staticType);
		TLID id = id(sqlDialect, dbResult, resultOffset, staticType);
		return new DBObjectKey(branch, historyContext, staticType, id);
	}

	@Override
	public void loadIdentifier(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, long historyContext,
			MOKnowledgeItem staticType, MutableObjectKey<?> key) throws SQLException {
		long branch = branch(sqlDialect, dbResult, resultOffset, staticType);
		TLID id = id(sqlDialect, dbResult, resultOffset, staticType);
		key.update(branch, historyContext, staticType, id);
	}

	private TLID id(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOKnowledgeItem staticType)
			throws SQLException {
		MOAttribute idAttr = staticType.getAttributeOrNull(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		return (TLID) idAttr.getStorage().fetchValue(sqlDialect, dbResult, resultOffset, idAttr,
			AttributeStorage.NO_CONTEXT);
	}

	private long branch(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOKnowledgeItem staticType)
			throws SQLException {
		MOAttribute branchAttr = staticType.getAttributeOrNull(BasicTypes.BRANCH_ATTRIBUTE_NAME);
		IBranchStorage branchStorage = (IBranchStorage) branchAttr.getStorage();
		return branchStorage.getBranch(sqlDialect, dbResult, resultOffset, branchAttr);
	}

}
