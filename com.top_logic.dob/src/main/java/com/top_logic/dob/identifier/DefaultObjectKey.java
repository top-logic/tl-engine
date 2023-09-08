/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.identifier;

import com.top_logic.basic.TLID;
import com.top_logic.dob.MetaObject;

/**
 * Stable object identifier. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultObjectKey extends ObjectKey {

	private final long branchContext;
	private final long historyContext;
	private final MetaObject objectType;

	private final TLID objectName;
	
	/**
	 * Creates a {@link DefaultObjectKey}.
	 * 
	 * @param branchContext
	 *        See {@link #getBranchContext()}
	 * @param historyContext
	 *        See {@link #getHistoryContext()}
	 * @param objectType
	 *        See {@link #getObjectType()}
	 * @param objectName
	 *        See {@link #getObjectName()}
	 */
	public DefaultObjectKey(long branchContext, long historyContext, MetaObject objectType, TLID objectName) {
		assert objectType != null : "Type must not be null";
		assert objectName != null : "ID must not be null";
		
		this.branchContext = branchContext;
		this.historyContext = historyContext;
		this.objectType = objectType;
		this.objectName = objectName;
		
	}
	
	@Override
	public long getBranchContext() {
		return branchContext;
	}
	
	/**
	 * The history context, the identified object lives in.
	 * 
	 * <p>
	 * The history context is {@link Long#MAX_VALUE} for current modifyable objects.
	 * </p>
	 */
	@Override
	public final long getHistoryContext() {
		return historyContext;
	}
	
	/**
	 * The {@link MetaObject#getName() concrete type name} of the identified
	 * object.
	 */
	@Override
	public MetaObject getObjectType() {
		return objectType;
	}

	/**
	 * The internal identifier.
	 * 
	 * <p>
	 * This identifier may be shared among different versions of the same object.
	 * </p>
	 */
	@Override
	public TLID getObjectName() {
		return objectName;
	}
	
}
