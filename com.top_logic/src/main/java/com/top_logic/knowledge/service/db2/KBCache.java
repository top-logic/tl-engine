/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.MOAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * Abstract implementation of {@link AbstractKBCache} that modifies the cache values within the
 * current transaction one by one.
 * 
 * <p>
 * When a change in the transaction is made this cache is informed.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class KBCache<E> extends AbstractKBCache<E> {

	private static final Object NO_LOCAL_CACHE_VALUE = new Object();

	/**
	 * {@link Property} to temporarily store local cache value in commit process to publish it when
	 * the corresponding update event occurs.
	 * 
	 * <p>
	 * This property must not be static, because otherwise the value, i.e. the locally changed cache
	 * value to publish, is shared between different instances.
	 * </p>
	 */
	private final Property<Object> NEW_GLOBAL_CACHE_VALUE = TypedAnnotatable.property(Object.class,
		"new global cache value", NO_LOCAL_CACHE_VALUE);

	/**
	 * Constant to use when the cache is uninitialized yet.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T noLocalCacheValue() {
		return (T) NO_LOCAL_CACHE_VALUE;
	}

	@SuppressWarnings("unchecked")
	final Object updateLocalCache(UpdateEvent event, Object localCacheValue) {
		return mergeUpdateEvent(event, (E) localCacheValue);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected final E newGlobalCacheValue(E globalCacheValue, UpdateEvent event) {
		Object successfulCommittedTxCacheValue = interaction().reset(NEW_GLOBAL_CACHE_VALUE);
		if (successfulCommittedTxCacheValue != NO_LOCAL_CACHE_VALUE) {
			/* My transaction was successful committed. Therefore the value is different to cache
			 * value and must therefore be updated. */
			return (E) successfulCommittedTxCacheValue;
		} else {
			return internalNewGlobalCacheValue(globalCacheValue, event);
		}
	}

	/**
	 * Creates a new cache value.
	 * 
	 * @param cacheValue
	 *        The former global cache value.
	 * @param event
	 *        The event to update.
	 * 
	 * @return The new value adapted according to the given update event, or <code>null</code> when
	 *         the event does not change the cache value. It must be ensured that old values of this
	 *         cache are not changed.
	 */
	protected abstract E internalNewGlobalCacheValue(E cacheValue, UpdateEvent event);

	/**
	 * Merges the changes in the given update event into the local cache value.
	 * 
	 * @param event
	 *        A concurrent update event.
	 * @param localCacheValue
	 *        The locally valid cache value. The value may be modified.
	 * @return The new cache value. If the <code>localCacheValue</code> is modified, it must be
	 *         returned nevertheless. This value is the new local cache value.
	 */
	protected abstract E mergeUpdateEvent(UpdateEvent event, E localCacheValue);

	@Override
	protected E adaptToTransaction(E cacheValue) {
		DBContext context = kb().getCurrentDBContext();
		if (context == null || !context.hasChanges()) {
			return cacheValue;
		} else {
			return getLocalValue(cacheValue, context);
		}
	}

	private E getLocalValue(E globalCacheValue, DBContext context) {
		@SuppressWarnings("unchecked")
		E localValue = (E) context.getLocalValue(this);
		if (localValue != noLocalCacheValue()) {
			return localValue;
		} else {
			Collection<DBKnowledgeItem> newObjects = context.getNewObjects();
			Collection<DBKnowledgeItem> changedObjects = context.getChangedObjects();
			Collection<DBKnowledgeItem> droppedObjects = context.getDroppedObjects();
			E newLocalCacheValue = deriveCacheValue(globalCacheValue, newObjects, changedObjects, droppedObjects);
			context.initLocalValue(this, newLocalCacheValue);
			return newLocalCacheValue;
		}
	}

	/**
	 * Creates a cache value valid for the current session with the given modifications.
	 * 
	 * <p>
	 * The changes represent the current transaction. The returned value is also only visible for
	 * the current transaction.
	 * </p>
	 * 
	 * @param cacheValue
	 *        The global cache value.
	 * @param newObjects
	 *        Items created in current transaction.
	 * @param changedObjects
	 *        Items changed in current transaction.
	 * @param droppedObjects
	 *        Items deleted in current transaction.
	 * 
	 * @return A locally valid modifiable cache value.
	 */
	protected abstract E deriveCacheValue(E cacheValue, Collection<? extends KnowledgeItem> newObjects,
			Collection<? extends KnowledgeItem> changedObjects,
			Collection<? extends KnowledgeItem> droppedObjects);

	@SuppressWarnings("unchecked")
	final Object dynamicAttributeValueChanged(Object cacheValue, KnowledgeItem item, String attributeName,
			Object oldValue, Object newValue) {
		return handleDynamicAttributeValueChanged((E) cacheValue, item, attributeName, oldValue, newValue);
	}

	/**
	 * Handles the change of a dynamic attribute.
	 * 
	 * @param localCacheValue
	 *        Cache value for the current transaction. It may be modified.
	 * @param item
	 *        The changed item.
	 * @param attributeName
	 *        The name of the changed dynamic attribute.
	 * @param oldValue
	 *        The attribute value before modification.
	 * @param newValue
	 *        The attribute value after modification.
	 * 
	 * @return The new cache value. This value is used as new cache entry.
	 * 
	 * @see #handleAttributeValueChanged(Object, KnowledgeItem, MOAttribute, Object, Object)
	 */
	protected abstract E handleDynamicAttributeValueChanged(E localCacheValue, KnowledgeItem item, String attributeName,
			Object oldValue, Object newValue);

	@SuppressWarnings("unchecked")
	final Object attributeValueChanged(Object cacheValue, KnowledgeItem item, MOAttribute attribute,
			Object oldValue, Object newValue) {
		return handleAttributeValueChanged((E) cacheValue, item, attribute, oldValue, newValue);
	}

	/**
	 * Handles the change of an attribute.
	 * 
	 * @param localCacheValue
	 *        Cache value for the current transaction. It may be modified.
	 * @param item
	 *        The changed item.
	 * @param attribute
	 *        The changed attribute attribute.
	 * @param oldValue
	 *        The attribute value before modification.
	 * @param newValue
	 *        The attribute value after modification.
	 * 
	 * @return The new cache value. This value is used as new cache entry.
	 * 
	 * @see #handleDynamicAttributeValueChanged(Object, KnowledgeItem, String, Object, Object)
	 */
	protected abstract E handleAttributeValueChanged(E localCacheValue, KnowledgeItem item, MOAttribute attribute,
			Object oldValue, Object newValue);

	@SuppressWarnings("unchecked")
	final Object itemDeleted(Object cacheValue, KnowledgeItem item) {
		return handleItemDeleted((E) cacheValue, item);
	}

	/**
	 * Handles the deletion of an item.
	 * 
	 * @param localCacheValue
	 *        Cache value for the current transaction. It may be modified.
	 * @param item
	 *        The deleted item.
	 * 
	 * @return The new cache value. This value is used as new cache entry.
	 */
	protected abstract E handleItemDeleted(E localCacheValue, KnowledgeItem item);

	@SuppressWarnings("unchecked")
	final Object itemCreated(Object cacheValue, KnowledgeItem item) {
		return handleItemCreated((E) cacheValue, item);
	}

	/**
	 * Handles the creation of an item.
	 * 
	 * @param localCacheValue
	 *        Cache value for the current transaction. It may be modified.
	 * @param item
	 *        The newly created item.
	 * 
	 * @return The new cache value. This value is used as new cache entry.
	 */
	protected abstract E handleItemCreated(E localCacheValue, KnowledgeItem item);

	/**
	 * Informs about the commit of a transaction.
	 * 
	 * @param value
	 *        The value of the cache stored in the current transaction.
	 */
	protected void notifyCommit(Object value) {
		interaction().set(NEW_GLOBAL_CACHE_VALUE, value);
	}

	private InteractionContext interaction() {
		return ThreadContextManager.getInteraction();
	}

}

