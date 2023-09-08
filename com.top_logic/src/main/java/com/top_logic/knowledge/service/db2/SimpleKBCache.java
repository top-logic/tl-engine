/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.util.Computation;
import com.top_logic.dob.MOAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * Simple implementation of {@link KBCache}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SimpleKBCache<T> extends KBCache<T> {

	private final static Object INVALID_CACHE = new Object();

	/**
	 * Marker exception to mark a local cache value as invalid after an operation.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public final static class InvalidCacheException extends Exception {
		// Nothing special here
	}

	@Override
	protected T handleAttributeValueChanged(T localCacheValue, KnowledgeItem item, MOAttribute attribute,
			Object oldValue, Object newValue) {
		T invalidCacheValue = invalidCacheValue();
		if (localCacheValue != invalidCacheValue) {
			try {
				handleChange(localCacheValue, item, attribute.getName(), oldValue, newValue);
			} catch (InvalidCacheException ex) {
				localCacheValue = invalidCacheValue;
			}
		}
		return localCacheValue;
	}

	@Override
	protected T handleDynamicAttributeValueChanged(T localCacheValue, KnowledgeItem item, String attributeName,
			Object oldValue, Object newValue) {
		T invalidCacheValue = invalidCacheValue();
		if (localCacheValue != invalidCacheValue) {
			try {
				handleChange(localCacheValue, item, attributeName, oldValue, newValue);
			} catch (InvalidCacheException ex) {
				localCacheValue = invalidCacheValue;
			}
		}
		return localCacheValue;
	}

	/**
	 * Adapts the cache value due to the given change.
	 * 
	 * @param cacheValue
	 *        The cache value to adapt.
	 * @param item
	 *        The changed item.
	 * @param attributeName
	 *        The name of the attribute that has changed.
	 * @param oldValue
	 *        The former value.
	 * @param newValue
	 *        the new value.
	 * @throws InvalidCacheException
	 *         When the cache value is invalid after the modification. See
	 *         {@link SimpleKBCache#invalidCacheValue()}.
	 */
	protected void handleChange(T cacheValue, KnowledgeItem item, String attributeName, Object oldValue,
			Object newValue) throws InvalidCacheException {
		handleChanges(cacheValue, Collections.singletonList(item));
	}

	@Override
	protected KBCacheValue<T> newCache(long minValidity, T cacheValue) {
		return new SimpleKBCacheValue<>(minValidity, cacheValue);
	}

	@Override
	protected final T internalNewGlobalCacheValue(T cacheValue, UpdateEvent event) {
		try {
			return handleEvent(cacheValue, event, true);
		} catch (InvalidCacheException ex) {
			return invalidCacheValue();
		}
	}

	@Override
	protected T mergeUpdateEvent(UpdateEvent event, T localCacheValue) {
		T invalidCacheValue = invalidCacheValue();
		if (localCacheValue != invalidCacheValue) {
			try {
				handleEvent(localCacheValue, event, false);
			} catch (InvalidCacheException ex) {
				localCacheValue = invalidCacheValue;
			}
		}
		return localCacheValue;
	}

	/**
	 * Adapts the given cache value according to the given update event.
	 * 
	 * @param cacheValue
	 *        The value to adapt.
	 * @param event
	 *        The committed changes.
	 * @param copyOnChange
	 *        Whether the cached value must be copied when it is changed. If <code>true</code>, the
	 *        cache value must <b>not</b> be modified. Instead a copy must be created and returned.
	 * 
	 * @return Either <code>null</code> if no changes occurred, or the changed valid value.
	 * 
	 * @throws InvalidCacheException
	 *         When the cache value is invalid after the modification. See
	 *         {@link SimpleKBCache#invalidCacheValue()}.
	 */
	protected abstract T handleEvent(T cacheValue, UpdateEvent event, boolean copyOnChange)
			throws InvalidCacheException;

	@Override
	protected T deriveCacheValue(T cacheValue, Collection<? extends KnowledgeItem> newObjects,
			Collection<? extends KnowledgeItem> changedObjects,
			Collection<? extends KnowledgeItem> droppedObjects) {
		T localCacheValue = copy(cacheValue);
		T invalidCacheValue = invalidCacheValue();
		for (KnowledgeItem item : newObjects) {
			localCacheValue = handleItemCreated(localCacheValue, item);
			if (localCacheValue == invalidCacheValue) {
				return invalidCacheValue;
			}
		}
		try {
			handleChanges(localCacheValue, changedObjects);
		} catch (InvalidCacheException ex) {
			return invalidCacheValue;
		}
		for (KnowledgeItem item : droppedObjects) {
			localCacheValue = handleItemDeleted(localCacheValue, item);
			if (localCacheValue == invalidCacheValue) {
				return invalidCacheValue;
			}
		}
		return localCacheValue;
	}

	/**
	 * Adapts the given cache value to changes in the given objects.
	 * 
	 * @param cacheValue
	 *        Copy of the global cache value consistent with the unchanged version of the given
	 *        changed objects to be adapted to changes in the given objects.
	 * @param changedObjects
	 *        The objects changed in the current transaction.
	 * @throws InvalidCacheException
	 *         When the cache value is invalid after the modification. See
	 *         {@link SimpleKBCache#invalidCacheValue()}.
	 */
	protected abstract void handleChanges(T cacheValue, Collection<? extends KnowledgeItem> changedObjects)
			throws InvalidCacheException;

	@Override
	protected T handleItemCreated(T localCacheValue, KnowledgeItem item) {
		T invalidCacheValue = invalidCacheValue();
		if (localCacheValue != invalidCacheValue) {
			try {
				handleCreation(localCacheValue, item);
			} catch (InvalidCacheException ex) {
				localCacheValue = invalidCacheValue;
			}
		}
		return localCacheValue;
	}

	/**
	 * Adapts the given cache value due to creation of the given item.
	 * 
	 * @param localCacheValue
	 *        The cache value to adapt.
	 * @param item
	 *        The created {@link KnowledgeItem}.
	 * @throws InvalidCacheException
	 *         When the cache value is invalid after the modification. See
	 *         {@link SimpleKBCache#invalidCacheValue()}.
	 */
	protected abstract void handleCreation(T localCacheValue, KnowledgeItem item) throws InvalidCacheException;

	@Override
	protected T handleItemDeleted(T localCacheValue, KnowledgeItem item) {
		T invalidCacheValue = invalidCacheValue();
		if (localCacheValue != invalidCacheValue) {
			try {
				handleDeletion(localCacheValue, item);
			} catch (InvalidCacheException ex) {
				localCacheValue = invalidCacheValue;
			}
		}
		return localCacheValue;
	}

	/**
	 * Calls {@link #newLocalCacheValue()} without modification.
	 * 
	 * @see com.top_logic.knowledge.service.db2.AbstractKBCache#newGlobalCacheValue()
	 * @see KnowledgeBase#withoutModifications(com.top_logic.basic.util.ComputationEx2)
	 */
	@Override
	protected T newGlobalCacheValue() {
		return kb().withoutModifications(new Computation<T>() {

			@Override
			public T run() {
				return SimpleKBCache.this.newLocalCacheValue();
			}
		});
	}

	/**
	 * Adapts the given cache value due to deletion of the given item.
	 * 
	 * @param localCacheValue
	 *        The cache value to adapt.
	 * @param item
	 *        The created {@link KnowledgeItem}.
	 * @throws InvalidCacheException
	 *         When the cache value is invalid after the modification. See
	 *         {@link SimpleKBCache#invalidCacheValue()}.
	 */
	protected abstract void handleDeletion(T localCacheValue, KnowledgeItem item) throws InvalidCacheException;

	/**
	 * Creates a new instance to use as cache object which is equal to the given value.
	 * 
	 * @param cacheValue
	 *        The value to copy.
	 * @return A new cache object with the same values.
	 */
	protected abstract T copy(T cacheValue);

	@SuppressWarnings("unchecked")
	private static final <T> T invalidCacheValue() {
		return (T) INVALID_CACHE;
	}

	@Override
	protected T validate(T result) throws DeletedObjectAccess {
		if (invalidCacheValue() == result) {
			StringBuilder error = new StringBuilder();
			error.append("Cache for revision ");
			error.append(kb().getSessionRevision());
			error.append(" is invalid.");
			throw new DeletedObjectAccess(error.toString());
		}
		return result;

	}

}
