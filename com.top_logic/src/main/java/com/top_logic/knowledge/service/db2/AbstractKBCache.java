/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.lang.ref.SoftReference;

import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;

/**
 * Super class for {@link KnowledgeBase} caches.
 * 
 * <p>
 * An {@link AbstractKBCache} is a cache for things based on {@link KnowledgeItem}. This cache cares
 * about updating when a transaction was committed. Moreover it provides transaction local changes,
 * i.e. if a change like creating or deleting an object is made this change is included in the value
 * of this cache, but only for the session which created or deleted these objects.
 * </p>
 * 
 * <p>
 * This cache supposes a non <code>null</code> cache value.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractKBCache<E> {

	private static final SoftReference<Object> UNINITIALIZED = new SoftReference<>(null);

	/**
	 * {@link ValidityChainImpl} providing the global result of the cache.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected abstract static class KBCacheValue<E> extends ValidityChainImpl<KBCacheValue<E>> {

		/**
		 * Creates a new {@link AbstractKBCache.KBCacheValue}.
		 * 
		 * @param minValidity
		 *        From that revision on the cache is valid until end of time.
		 */
		public KBCacheValue(long minValidity) {
			super(minValidity, Revision.CURRENT_REV);
		}

		/**
		 * Returns the global cached result.
		 */
		public abstract E globalCacheValue();

	}

	/**
	 * {@link UpdateListener} to modify cached result.
	 * 
	 * <p>
	 * The listener will be initialized and attached during first access of cached value.
	 * </p>
	 */
	private UpdateListener _updateListener;

	/**
	 * Reference holding the cache.
	 * 
	 * <p>
	 * The {@link SoftReference} ensures, that no {@link OutOfMemoryError} occurs, when the cache is
	 * huge.
	 * </p>
	 * 
	 * <p>
	 * The access to this reference is not done with a synchronized block to ensure that processing
	 * an {@link UpdateEvent} from the {@link KnowledgeBase} does not block.
	 * </p>
	 */
	private volatile SoftReference<KBCacheValue<E>> _cacheReference = uninitialized();

	/**
	 * The {@link KnowledgeBase} this caches bases on.
	 */
	protected abstract DBKnowledgeBase kb();

	/**
	 * Returns the cache value. The returned value must not be changed.
	 * 
	 * <p>
	 * It is not defined whether the value is updated or not due to changes in the
	 * {@link KnowledgeBase} which would change the cache result.
	 * </p>
	 */
	public E getValue() {
		ensureActive();
		long sessionRevision = kb().getSessionRevision();
		KBCacheValue<E> responsibleCache = getCurrentCache();
		while (true) {
			if (sessionRevision >= responsibleCache.minValidity()) {
				/* This cache contains values for the revision. */
				E localValue = adaptToTransaction(responsibleCache.globalCacheValue());
				return validate(localValue);
			}
			responsibleCache = responsibleCache.formerValidity();
			if (responsibleCache == null) {
				/* requesting session was created before this cache. This may happen when the cache
				 * is created lazy and a later session was "faster". */
				return newLocalCacheValue();
			}
		}

	}

	/**
	 * Checks that the given cache value is valid.
	 * 
	 * @param result
	 *        The cache value (including potentially uncommitted changes) which is returned by
	 *        {@link #getValue()}.
	 * 
	 * @return The validated cache result.
	 * 
	 * @throws DeletedObjectAccess
	 *         iff the given result is invalid. Typically because the {@link KnowledgeItem} on which
	 *         the item based is invalid.
	 */
	protected E validate(E result) throws DeletedObjectAccess {
		return result;
	}

	/**
	 * Synchronise here to ensure that a thread do not use a cache, that is potentially invalid. See
	 * check for "no KnowledgeBase change" below.
	 */
	private synchronized KBCacheValue<E> getCurrentCache() {
		KBCacheValue<E> cache = _cacheReference.get();
		if (cache != null) {
			return cache;
		}

		DBKnowledgeBase kb = kb();
		initUpdateListener();
		while (true) {
			/* Check that no change occurs between searching and installation of cache. */
			UpdateChainView updateChain = (UpdateChainView) kb.getSessionUpdateChain();
			/* Current revision of the KnowledgeBase. Navigating the UpdateChain at least this
			 * revision must be reached. If this is not the case the session revision is corrupt. */
			long currentKBRevision = kb.getLastLocalRevision();

			E globalCacheValue = newGlobalCacheValue();

			long updateChainRevision = updateChain.current().getRevision();
			long sessionRevision = kb.getSessionRevision();
			if (updateChainRevision != sessionRevision) {
				/* Session revision does not match revision of session. This may happen when another
				 * interaction concurrently updates the session. */
				throw new KnowledgeBaseRuntimeException(
					"Session revision invalid: Expected " + updateChainRevision + " but got " + sessionRevision
							+ " in cache " + this
							+ ". Session revision may be the cached revision of the interaction which means that a different interaction for the same session has updated the session concurrently. This leads to inconsistently used data. Persistent data are not corrupted.");
			}

			long cacheRevision = sessionRevision;
			KBCacheValue<E> newCache = newCache(cacheRevision, globalCacheValue);
			while (updateChain.next()) {
				cacheRevision = updateChain.getRevision();
				Computation<KBCacheValue<E>> updater = newCacheUpdater(updateChain.getUpdateEvent(), newCache);
				KBCacheValue<E> updatedCache = kb.inRevision(cacheRevision, updater);
				if (updatedCache != null) {
					newCache = updatedCache;
				}
			}
			if (cacheRevision < currentKBRevision) {
				/* The chain must reach at least to the KB revision. It might be larger, when a
				 * commit occurs during navigation. */
				throw new KnowledgeBaseRuntimeException(
					"Session revision invalid. Unable to get all updates from revision " + cacheRevision
						+ " to revision " + currentKBRevision + " in cache " + this);
			}
			/* Install the cache. This might be an "old" value which is checked below. It does not
			 * matter to publish a potential old cache, because no one (except the updater) sees
			 * it. */
			_cacheReference = new SoftReference<>(newCache);
			long newCurrentKBRevision = kb.getLastLocalRevision();
			if (cacheRevision == newCurrentKBRevision) {
				/* No concurrent commit, i.e. cache is in up to date. Otherwise it could be possible
				 * that a change occurred between search and installation of cache, which is not
				 * updated by the update listener. */
				return newCache;
			} else if (cacheRevision > newCurrentKBRevision) {
				throw new IllegalStateException("Encountered cache revision " + cacheRevision
					+ " larger than the last local revision " + newCurrentKBRevision + ".");
			}
		}
	}

	private Computation<KBCacheValue<E>> newCacheUpdater(final UpdateEvent evt, final KBCacheValue<E> cache) {
		return new Computation<>() {

			@Override
			public KBCacheValue<E> run() {
				return AbstractKBCache.this.newCache(evt, cache);
			}
		};
	}

	/**
	 * Call within "synchronized(this)" block.
	 */
	private void initUpdateListener() {
		if (_updateListener == null) {
			_updateListener = new Updater(this);
			kb().addUpdateListenerHighPrio(_updateListener);
		}
	}

	/**
	 * Creates a cache value that is valid for session revision. Local changes must not be included.
	 * 
	 * @throws DeletedObjectAccess
	 *         if it is not possible to create a valid cache value.
	 * 
	 * @see AbstractKBCache#newLocalCacheValue()
	 */
	protected abstract E newGlobalCacheValue();

	private void ensureActive() {
		if (_cacheReference == AbstractKBCache.<E> invalid()) {
			throw new IllegalStateException("Cache was invalidated.");
		}
	}

	/**
	 * Adapts the given cache value to the current transaction, i.e. the result must respect
	 * uncommitted changes.
	 * 
	 * @param cacheValue
	 *        The former global cache value to adapt.
	 * 
	 * @return The cache value respecting uncommitted changes.
	 */
	protected abstract E adaptToTransaction(E cacheValue);

	void handleUpdateEvent(UpdateEvent event) {
		KBCacheValue<E> currentCache = _cacheReference.get();
		if (currentCache == null) {
			/* Cache was removed due to memory problems, or not yet requested. No update of cache
			 * necessary, because it will be reinstalled during next access. */
			return;
		}
		if (currentCache.minValidity() >= event.getCommitNumber()) {
			/* Cache is newer than the event. This may happen in following situation: This cache was
			 * not used. Then a commit (r15) occurs and before the listeners of the KB are informed,
			 * this cache is requested. In this case the cache is created for r15. The cache
			 * installs itself as listener such that it is informed about the change r15. In this
			 * case the cache is already up to date and must not be updated. */
			return;
		}
		KBCacheValue<E> newCache = newCache(event, currentCache);
		if (newCache == null) {
			return;
		}
		_cacheReference = new SoftReference<>(newCache);
	}

	/**
	 * Creates a new cache based on the given cache respecting the given changes.
	 * 
	 * @return A new cache or <code>null</code> when the event does not change the search result.
	 */
	KBCacheValue<E> newCache(UpdateEvent event, KBCacheValue<E> cache) {
		E adaptedResult = newGlobalCacheValue(cache.globalCacheValue(), event);
		if (adaptedResult == null) {
			// No changes
			return null;
		}
		long commitNumber = event.getCommitNumber();
		KBCacheValue<E> newCache = newCache(commitNumber, adaptedResult);
		cache.updateMaxValidity(commitNumber - 1);
		newCache.setFormerValidity(cache);

		kb().registerForCleanup(cache, newCache);
		return newCache;
	}

	/**
	 * Creates a cache value, based on the given global cache value, that respects the changes in
	 * the given update event, or <code>null</code> when event did not changed cache value.
	 * 
	 * <p>
	 * If it is not possible to create a valid cache value for the given event, return a replacement
	 * object. This value must result in {@link AbstractKBCache#validate(Object)} in a
	 * {@link DeletedObjectAccess}.
	 * </p>
	 * 
	 * @see AbstractKBCache#validate(Object)
	 */
	protected abstract E newGlobalCacheValue(E globalCacheValue, UpdateEvent event);

	/**
	 * Creates a cache value, that is valid for the given transaction. The returned value includes
	 * also currently uncommitted changes.
	 * 
	 * @throws DeletedObjectAccess
	 *         if it is not possible to create a valid cache value.
	 * 
	 * @see AbstractKBCache#newGlobalCacheValue()
	 */
	protected abstract E newLocalCacheValue();

	/**
	 * Creates a new {@link KBCacheValue holder} for the given cache value.
	 * 
	 * @param minValidity
	 *        The minimum revision from which the given cache value is valid. See
	 *        {@link KBCacheValue#minValidity()}.
	 * @param cacheValue
	 *        The actual cache value.
	 */
	protected abstract KBCacheValue<E> newCache(long minValidity, E cacheValue);

	/**
	 * Invalidates this cache. This cache is not longer usable.
	 */
	public synchronized void invalidate() {
		kb().removeUpdateListenerHighPrio(_updateListener);
		_cacheReference = invalid();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <E> SoftReference<E> uninitialized() {
		return (SoftReference) UNINITIALIZED;
	}

	private static <E> SoftReference<E> invalid() {
		return null;
	}

	// Note: This class must not be anonymous to prevent an implicit this-reference to the cache
	// resulting in a memory leak.
	private static final class Updater extends AbstractWeakUpdateListener<AbstractKBCache<?>> {
		public Updater(AbstractKBCache<?> referent) {
			super(referent);
		}
	
		@Override
		protected void internalUpdate(KnowledgeBase sender, AbstractKBCache<?> referent, UpdateEvent event) {
			referent.handleUpdateEvent(event);
		}
	}

}

