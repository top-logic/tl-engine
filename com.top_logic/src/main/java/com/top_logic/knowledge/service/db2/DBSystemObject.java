/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Base class for {@link DBKnowledgeBase} internal immutable item
 * implementations that cannot be branched.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class DBSystemObject extends StaticSystemItem {

	public DBSystemObject(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
		if (staticType.getCacheSize() > 0) {
			throw new IllegalArgumentException(
				"System objects must not have generic attributes, i.e. cache size must be 0:" + staticType);
		}
	}
	
	/**
	 * The commit number that created this object.
	 * 
	 * @see com.top_logic.knowledge.service.db2.DBKnowledgeItem#getLastUpdate()
	 */
	@Override
	public final long getLastUpdate() {
		return getCreateCommitNumber();
	}

	/**
	 * Must not be {@link KnowledgeItem#NO_CREATE_REVISION} as needed in {@link #getLastUpdate()}.
	 * 
	 * @see KnowledgeItem#getCreateCommitNumber()
	 */
	@Override
	public abstract long getCreateCommitNumber();

	@Override
	protected Object[] getGlobalValues(long sessionRevision) {
		// There is only one storage for all sessions.
		return ArrayUtil.EMPTY_ARRAY;
	}

}