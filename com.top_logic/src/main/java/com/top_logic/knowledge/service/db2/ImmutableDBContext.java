/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.TLObject;

/**
 * Immutable {@link DBContext}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImmutableDBContext extends DBContext {

	/** Singleton {@link ImmutableDBContext} instance. */
	public static final DBContext INSTANCE = new ImmutableDBContext();

	private ImmutableDBContext() {
		// singleton instance
	}

	@Override
	public TLID createID() {
		throw new UnsupportedOperationException();
	}

	@Override
	void lock() {
		// always locked
	}

	@Override
	void unlock() {
		// always locked.
	}

	@Override
	void checkModifiable() {
		throw unmodifiable();
	}

	private IllegalStateException unmodifiable(Object... args) {
		throw new IllegalStateException("This context '" + this + "' is not modifiable: calling arguments: "
			+ Arrays.deepToString(args) + ".");
	}

	@Override
	boolean addCommittable(Committable commitable) {
		throw unmodifiable(commitable);
	}

	@Override
	boolean removeCommittable(Committable committable) {
		throw unmodifiable(committable);
	}

	@Override
	boolean addCommittableDelete(Committable commitable) {
		throw unmodifiable(commitable);
	}

	@Override
	Transaction begin(boolean anonymous, Message commitMessage) {
		throw uncomittable();
	}

	@Override
	protected void startCommit() {
		throw uncomittable();
	}

	private IllegalStateException uncomittable() {
		return new IllegalStateException("This context '" + this + "' can not be committed.");
	}

	@Override
	void rollback() {
		// Nothing to rollback here.
	}

	@Override
	void commitAnonymous() throws KnowledgeBaseException {
		// empty commit
	}

	@Override
	boolean rollbackComplete(boolean success) {
		// Nothing to rollback here.
		return true;
	}

	@Override
	public boolean transactionStarted() {
		return false;
	}

	@Override
	protected PooledConnection internalGetConnection() {
		throw new NullPointerException("This context '" + this + "' has no connection.");
	}

	@Override
	void putNewObjects(Iterable<? extends DBKnowledgeItem> items) {
		throw unmodifiable(items);
	}

	@Override
	void putNewObject(DBKnowledgeItem item) {
		throw unmodifiable(item);
	}

	@Override
	void finishPutNewObject(DBKnowledgeItem item) {
		// nothing to finish here
	}

	@Override
	void dropNewItem(DBKnowledgeItem item) {
		throw new IllegalArgumentException("Item '" + item + "' was not added before because this context '" + this
			+ "' is unmodifiable.");
	}

	@Override
	void changedObject(DBKnowledgeItem item) {
		throw unmodifiable(item);
	}

	@Override
	void removeObject(DBKnowledgeItem item) throws DataObjectException {
		throw unmodifiable(item);
	}

	@Override
	void removeObjects(DBKnowledgeItem[] items, int size) throws DataObjectException {
		throw unmodifiable((Object[]) Arrays.copyOf(items, size));
	}

	@Override
	void notifyAttributeValueChange(KnowledgeItemInternal item, MOAttribute changedAttribute, Object oldValue,
			Object newValue) {
		throw unmodifiable(item, changedAttribute, oldValue, newValue);
	}

	@Override
	void notifyDynamicAttributeValueChange(KnowledgeItemInternal item, String dynamicAttribute, Object oldValue,
			Object newValue) {
		throw unmodifiable(item, dynamicAttribute, oldValue, newValue);
	}

	@Override
	Collection<DBKnowledgeItem> getChangedObjects() {
		return Collections.emptyList();
	}

	@Override
	Collection<DBKnowledgeItem> getNewObjects() {
		return Collections.emptyList();
	}

	@Override
	Collection<DBKnowledgeItem> getDroppedObjects() {
		return Collections.emptyList();
	}

	@Override
	KnowledgeItemInternal getNew(ObjectKey identity) {
		// No new objects in this context
		return null;
	}

	@Override
	boolean isRemovedKey(ObjectKey identity) {
		// No objects removed in this context
		return false;
	}

	@Override
	boolean isPersistentItemModified(KnowledgeItem item) {
		// No modifications in this context
		return false;
	}

	@Override
	boolean hasChanges() {
		return false;
	}

	@Override
	Set<TLObject> getAssociationChanges(ObjectKey baseKey) {
		return null;
	}

	@Override
	Object[] getLocalValues(KnowledgeItem item) {
		// No local changes for any object.
		return null;
	}

	@Override
	FlexData getLocalDynamicValues(DynamicKnowledgeItem dynamicKnowledgeItem) {
		// No local changes for any object.
		return null;
	}

	@Override
	void initLocalDynamicValues(DynamicKnowledgeItem dynamicKnowledgeItem, FlexData localDynamicValues) {
		throw unmodifiable(dynamicKnowledgeItem, localDynamicValues);
	}

	@Override
	void initLocalValues(KnowledgeItem item, Object[] localValues) {
		throw unmodifiable(item, localValues);
	}

	@Override
	void initLocalValue(KBCache<?> cache, Object localValue) {
		throw unmodifiable(cache, localValue);
	}

	@Override
	Object getLocalValue(KBCache<?> cache) {
		return KBCache.noLocalCacheValue();
	}

	@Override
	Throwable getAllocationStackTrace() {
		return new Exception("Note: This is an immutable context, therefore no allocation stack is available.");
	}

	@Override
	boolean isCreationContext(DBKnowledgeItem item) {
		return false;
	}

	@Override
	<T extends TLObject, C> Associations<T, C> getLocalCache(AbstractAssociationCache<T, C> globalCache) {
		return null;
	}

	@Override
	<T extends TLObject, C> Associations<T, C> dropLocalCache(AbstractAssociationCache<T, C> globalCache) {
		return null;
	}

	@Override
	<T extends TLObject, C> void setLocalCache(AbstractAssociationCache<T, C> globalCache,
			Associations<T, C> localCache) {
		throw unmodifiable(globalCache, localCache);
	}

	@Override
	void merge(UpdateEvent evt) {
		// this context has no changes, therefore no merge necessary.
	}

	@Override
	void checkMergeConflict() throws MergeConflictException {
		// no merge conflict, as no change here.
	}

}
