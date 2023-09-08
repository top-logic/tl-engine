/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.ImmutableLongStorage;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * {@link AttributeStorage} retrieving the branch of an item by calling
 * {@link KnowledgeItem#getBranchContext()}.
 * 
 * @see BasicTypeProvider#newBranchAttribute(boolean)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BranchStorage extends ImmutableLongStorage implements IBranchStorage {

	/** Singleton {@link BranchStorage} instance. */
	public static final BranchStorage INSTANCE = new BranchStorage();

	private BranchStorage() {
		// singleton instance
	}


	@Override
	public long getLongValue(MOAttribute attribute, DataObject item, Object[] storage) {
		assert BasicTypes.BRANCH_ATTRIBUTE_NAME.equals(attribute.getName()) : "Expected the KB internal attribute '"
			+ BasicTypes.BRANCH_ATTRIBUTE_NAME + "' is used.";
		return ((KnowledgeItem) item).getBranchContext();
	}

	@Override
	public void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue) {
		assert ((KnowledgeItem) item).tId() != null : "Object key must be loaded before the branch attribute is initialised.";
		// the branch is taken from the Object key which is created directly.
	}

	@Override
	public long getBranch(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute branchAttribute)
			throws SQLException {
		return fetchLong(sqlDialect, dbResult, resultOffset, branchAttribute);
	}

}

