/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.message.Message;
import com.top_logic.basic.sql.AbstractCommitContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.TLObject;

/**
 * {@link AbstractCommitContext} for a {@link DBKnowledgeBase} that holds local modifications.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class DBContext extends AbstractCommitContext {

	/**
	 * Locks this context to provide adding any changes.
	 * 
	 * @see #unlock()
	 */
	abstract void lock();

	/**
	 * Removes the lock from this context to allow adding any changes.
	 * 
	 * @see #lock()
	 */
	abstract void unlock();

	/**
	 * Checks that this {@link DBContext} is currently modifiable.
	 * 
	 * @throws IllegalStateException
	 *         iff this {@link DBContext} is currently not modifiable.
	 */
	abstract void checkModifiable();

	/**
	 * @see CommitHandler#addCommittable(Committable)
	 */
	abstract boolean addCommittable(Committable commitable);

	/**
	 * @see CommitHandler#removeCommittable(Committable)
	 */
	abstract boolean removeCommittable(Committable committable);

	/**
	 * @see CommitHandler#addCommittableDelete(Committable)
	 */
	abstract boolean addCommittableDelete(Committable commitable);

	abstract Transaction begin(boolean anonymous, Message commitMessage);

	/**
	 * Forget all about the transaction.
	 */
	abstract void rollback();

	/**
	 * Commits all the changes to the DB.
	 */
	abstract void commitAnonymous() throws KnowledgeBaseException;

	/**
	 * Completely rolls back the current transaction including potentially open pseudo-nested
	 * transactions.
	 * 
	 * @return Whether the rollback succeeded.
	 */
	abstract boolean rollbackComplete(boolean success);

	/**
	 * Record all items Object for a later commit.
	 * 
	 * <p>
	 * The items may not be completely initialised. In each case after complete initialisation
	 * {@link #finishPutNewObjects(Iterable)} must be called.
	 * </p>
	 * 
	 * @param items
	 *        the items to mark for commit
	 * 
	 * @see #finishPutNewObjects(Iterable)
	 * @see #putNewObject(DBKnowledgeItem)
	 */
	abstract void putNewObjects(Iterable<? extends DBKnowledgeItem> items);

	/**
	 * Finishes adding of new items to this context.
	 * 
	 * @param items
	 *        The items formerly added via {@link #putNewObjects(Iterable)}.
	 * 
	 * @see #putNewObjects(Iterable)
	 */
	void finishPutNewObjects(Iterable<? extends DBKnowledgeItem> items) {
		for (DBKnowledgeItem item : items) {
			finishPutNewObject(item);
		}
	}

	/**
	 * Record some completely initialised object for a later commit.
	 * 
	 * <p>
	 * The item may not be completely initialised. In each case after complete initialisation
	 * {@link #finishPutNewObject(DBKnowledgeItem)} must be called.
	 * 
	 * @param item
	 *        the item to mark for commit
	 * 
	 * @see #finishPutNewObject(DBKnowledgeItem)
	 * @see #putNewObjects(Iterable)
	 */
	abstract void putNewObject(DBKnowledgeItem item);

	/**
	 * Finishes adding of new item to this context.
	 * 
	 * @param item
	 *        The item formerly added via {@link #putNewObject(DBKnowledgeItem)}.
	 * 
	 * @see #putNewObject(DBKnowledgeItem)
	 */
	abstract void finishPutNewObject(DBKnowledgeItem item);

	/**
	 * Removes an item formerly added via {@link #putNewObject(DBKnowledgeItem)} or
	 * {@link #putNewObjects(Iterable)} due to an exception during initialisation of new object.
	 * 
	 * @param item
	 *        The item to drop. Must former by added.
	 */
	abstract void dropNewItem(DBKnowledgeItem item);

	/**
	 * Mark Object as changed for later update and commit
	 */
	abstract void changedObject(DBKnowledgeItem item);

	/**
	 * Record the given object to be removed in this transaction.
	 */
	abstract void removeObject(DBKnowledgeItem item) throws DataObjectException;

	/**
	 * Record the given objects to be removed in this transaction.
	 * 
	 * @items Array containing the items to delete.
	 * @size Number of element to delete. Must be <code>&lt;=items.length</code>. The first
	 *       <code>size</code> entries in the array will be deleted and must therefore be be non
	 *       <code>null</code>.
	 */
	abstract void removeObjects(DBKnowledgeItem[] items, int size) throws DataObjectException;

	abstract void notifyAttributeValueChange(KnowledgeItemInternal item, MOAttribute changedAttribute, Object oldValue,
			Object newValue);

	abstract void notifyDynamicAttributeValueChange(KnowledgeItemInternal item, String dynamicAttribute,
			Object oldValue, Object newValue);


	abstract Collection<DBKnowledgeItem> getChangedObjects();

	abstract Collection<DBKnowledgeItem> getNewObjects();

	abstract Collection<DBKnowledgeItem> getDroppedObjects();

	/** Lookup an object scheduled for insertion. */
	abstract KnowledgeItemInternal getNew(ObjectKey identity);

	abstract boolean isRemovedKey(ObjectKey identity);

	/**
	 * Whether any local changes have been done within this context on the given persistent object,
	 * i.e. whether the item was deleted or any attribute was changed.
	 */
	abstract boolean isPersistentItemModified(KnowledgeItem item);

	/**
	 * Lookup all touched association links in this context that have a base object with the given
	 * key.
	 * 
	 * @param baseKey
	 *        The key of the base object.
	 * @return A set of {@link TLObject}s. <code>null</code> indicates that no changes are done for
	 *         the desired object.
	 */
	abstract Set<TLObject> getAssociationChanges(ObjectKey baseKey);

	/**
	 * Returns the local values of the given {@link KnowledgeItem} or <code>null</code> when no
	 * values were stored before.
	 * 
	 * @see #initLocalValues(KnowledgeItem, Object[])
	 */
	abstract Object[] getLocalValues(KnowledgeItem item);

	/**
	 * Returns the local dynamic values of the given {@link DynamicKnowledgeItem} or
	 * <code>null</code>, when no values were stored before.
	 * 
	 * @see #initLocalDynamicValues(DynamicKnowledgeItem, FlexData)
	 */
	abstract FlexData getLocalDynamicValues(DynamicKnowledgeItem dynamicKnowledgeItem);

	/**
	 * Initializes the local dynamic values for the given {@link DynamicKnowledgeItem}. Must not be
	 * called twice.
	 * 
	 * @see #getLocalDynamicValues(DynamicKnowledgeItem)
	 */
	abstract void initLocalDynamicValues(DynamicKnowledgeItem dynamicKnowledgeItem, FlexData localDynamicValues);

	/**
	 * Initializes the local values for the given {@link KnowledgeItem}. Must not be called twice.
	 * 
	 * @see #getLocalValues(KnowledgeItem)
	 */
	abstract void initLocalValues(KnowledgeItem item, Object[] localValues);

	abstract Throwable getAllocationStackTrace();

	/**
	 * Checks whether the given item was created in this context.
	 * 
	 * @param item
	 *        The object to check.
	 * @return <code>true</code> iff the given object was created in this context.
	 */
	abstract boolean isCreationContext(DBKnowledgeItem item);

	abstract <T extends TLObject, C> Associations<T, C> getLocalCache(AbstractAssociationCache<T, C> globalCache);

	abstract <T extends TLObject, C> Associations<T, C> dropLocalCache(AbstractAssociationCache<T, C> globalCache);

	abstract <T extends TLObject, C> void setLocalCache(AbstractAssociationCache<T, C> globalCache,
			Associations<T, C> localCache);
	
	/**
	 * Merges the current changes with the incoming update event.
	 * 
	 * <p>
	 * This method must not throw an exception, because it is called multiple times within global
	 * {@link DBKnowledgeBase#refetch() refetch} method.
	 * </p>
	 * 
	 * @param evt
	 *        An incoming {@link UpdateEvent}.
	 * 
	 * @see #checkMergeConflict()
	 */
	abstract void merge(UpdateEvent evt);

	/**
	 * Checks that no merge has produced conflicts since last check.
	 * 
	 * @throws MergeConflictException
	 *         Exception describing the merge problem.
	 * 
	 * @see #merge(UpdateEvent)
	 */
	abstract void checkMergeConflict() throws MergeConflictException;

	abstract boolean hasChanges();

	/**
	 * Must override hashCode to use identity.
	 */
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	/**
	 * Must override equals to use identity.
	 */
	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	abstract void initLocalValue(KBCache<?> cache, Object localValue);

	abstract Object getLocalValue(KBCache<?> cache);

}
