/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.identifier;

import java.util.Map;

import com.top_logic.basic.TLID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;

/**
 * Mutable {@link ObjectKey} implementation that can be used for multiple
 * lookups.
 * 
 * <p>
 * Note: An instance of this class must never be used as key in a {@link Map}
 * since its {@link #hashCode()} and {@link #equals(Object)} methods change
 * results.
 * </p>
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MutableObjectKey<E extends ObjectKey> extends ObjectKey {

	private long branchContext;
	private long historyContext;
	private MetaObject objectType;

	private TLID objectName;

	public MutableObjectKey() {
		// Uninitialized object for update through setters.
	}
	
	public MutableObjectKey(long branchContext, long historyContext, MetaObject objectType, TLID objectName) {
		this.branchContext = branchContext;
		this.historyContext = historyContext;
		this.objectType = objectType;
		this.objectName = objectName;
	}

	@Override
	public long getBranchContext() {
		return Long.valueOf(branchContext);
	}

	@Override
	public long getHistoryContext() {
		return historyContext;
	}
	
	@Override
	public TLID getObjectName() {
		return objectName;
	}
	
	@Override
	public MetaObject getObjectType() {
		return objectType;
	}
	
	/**
	 * Updates the {@link #getBranchContext()} of this {@link MutableObjectKey}.
	 * 
	 * @param branchContext
	 *        The new branch context to search in.
	 */
	public void setBranchContext(long branchContext) {
		this.branchContext = branchContext;
	}

	/**
	 * Updates the {@link #getHistoryContext()} of this {@link MutableObjectKey}.
	 * 
	 * @param historyContext
	 *        The new history context to search in.
	 */
	public void setHistoryContext(long historyContext) {
		this.historyContext = historyContext;
	}

	/**
	 * Updates the {@link #getObjectName()} of this {@link MutableObjectKey}.
	 * 
	 * @param objectName
	 *        The new object name to search for.
	 */
	public void setObjectName(TLID objectName) {
		this.objectName = objectName;
	}

	/**
	 * Updates the {@link #getObjectType()} of this {@link MutableObjectKey}.
	 * 
	 * @param objectType
	 *        The new object type to search for.
	 */
	public void setObjectType(MOKnowledgeItem objectType) {
		this.objectType = objectType;
	}
	
	/**
	 * Updates this {@link MutableObjectKey} by setting the given values.
	 * 
	 * @param branchContext
	 *        New value of {@link #getBranchContext()}.
	 * @param historyContext
	 *        New value of {@link #getHistoryContext()}.
	 * @param objectType
	 *        New value of {@link #getObjectType()}.
	 * @param objectName
	 *        New value of {@link #getObjectName()}.
	 */
	public void update(long branchContext, long historyContext, MOKnowledgeItem objectType, TLID objectName) {
		this.branchContext = branchContext;
		this.historyContext = historyContext;
		this.objectType = objectType;
		this.objectName = objectName;
	}
	
	/**
	 * Updates this {@link MutableObjectKey} by setting the given history context and setting values
	 * of given {@link ObjectBranchId}.
	 * 
	 * @param historyContext
	 *        New value of {@link #getHistoryContext()}.
	 * @param objectId
	 *        Provider of the values {@link #getBranchContext()}, {@link #getObjectType()}, and
	 *        {@link #getObjectName()}.
	 * 
	 * @see ObjectBranchId#getBranchId()
	 * @see ObjectBranchId#getObjectType()
	 * @see ObjectBranchId#getObjectName()
	 */
	public void update(long historyContext, ObjectBranchId objectId) {
		this.branchContext = objectId.getBranchId();
		this.historyContext = historyContext;
		this.objectType = objectId.getObjectType();
		this.objectName = objectId.getObjectName();

	}

	/**
	 * Creates a stable {@link ObjectKey} form this one that can be used
	 * as key in a {@link Map}.
	 * 
	 * @return a stable version with the current values of this
	 *         {@link MutableObjectKey}.
	 */
	public abstract E toStableKey();

}

