/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;
import java.util.Set;

import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeItem.State;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * Cache for association links that point to a base object through a
 * {@link #getReferenceAttribute()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AssociationCache<T extends TLObject, C> extends AssociationCacheBase<T, C> {

	/**
	 * The global (not context aware) top level version of this cache.
	 * 
	 * <p>
	 * This cache starts a cache chain ({@link Associations#formerValidity()}) with disjunct validity
	 * ranges and decreasing revisions.
	 * </p>
	 * 
	 * <p>
	 * May be <code>null</code> in case the associated query was not navigated before.
	 * </p>
	 */
	private Associations<T, C> _cache;

	/**
	 * Creates a {@link AssociationCache}.
	 * 
	 * @param baseItem
	 *        See {@link #getBaseItem()}
	 * @param query
	 *        The {@link AssociationSetQueryImpl} this cache holds the answer for.
	 */
	AssociationCache(KnowledgeItemInternal baseItem, AssociationQueryImpl<T, C> query) {
		super(baseItem, query);
	}
	
	@Override
	C getLinksDirect() {
		return lookup().getAssociations();
	}

	/**
	 * Lookup the local (context aware) version of this cache. 
	 */
	final Associations<T, C> lookup() {
		DBKnowledgeBase kb = getKnowledgeBase();
		DBContext context = kb.getCurrentDBContext();
		KnowledgeItemImpl.checkAccess(_baseItem, context);
		
		long sessionRevision = getKnowledgeBase().getSessionRevision();
		if (context == null) {
			/* Return the global values. There is no context and therefore no local modification. */
			return getOrCreateGlobalCache(sessionRevision);
		} else {
			if (_baseItem.getHistoryContext() != Revision.CURRENT_REV) {
				MOReference referenceAttribute = getReferenceAttribute();
				switch (referenceAttribute.getHistoryType()) {
					case HISTORIC: {
						/* Requested history context is current, therefore a context-local
						 * modification is possible. */
						return lookupContextDependentCache(context, sessionRevision);
					}
					case CURRENT: {
						/* Current reference means to search results in history context of base
						 * item. Since the past is immutable, there are no context-local
						 * modifications possible. */
						return getOrCreateGlobalCache(sessionRevision);
					}
					case MIXED:
						return lookupContextDependentCache(context, sessionRevision);
				}
				throw HistoryType.noSuchType(referenceAttribute.getHistoryType());
			} else {
				return lookupContextDependentCache(context, sessionRevision);
			}
		}
	}

	private Associations<T, C> lookupContextDependentCache(DBContext context, long sessionRevision) {
		Associations<T, C> globalCache = getOrCreateGlobalCache(sessionRevision);
		if (context.isRemovedKey(getBaseItem().tId())) {
			throw new IllegalStateException("Base object is already locally deleted.");
		}
		Associations<T, C> currentLocalCache = context.getLocalCache(this);
		Associations<T, C> localCache;
		if (currentLocalCache != null) {
			localCache = validateLocalCache(context, currentLocalCache, sessionRevision);
		} else {
			localCache = createLocalCache(context, sessionRevision, globalCache);
		}
		if (localCache != null) {
			return localCache;
		}
		return globalCache;
	}

	/**
	 * Searches for the global cache for the given revision. If none was found a new cache is
	 * installed.
	 */
	private Associations<T, C> getOrCreateGlobalCache(long revision) {
		Associations<T, C> globalCache = getGlobalCache(revision);
		if (globalCache == null) {
			// Fill global cache. Only if there is a global version of the
			// cache, context-local versions can exist.
			globalCache = setGlobalCache(query(getKnowledgeBase(), revision), revision);
		}
		return globalCache;
	}

	@Override
	public
	boolean isFilled() {
		return getGlobalCache(getKnowledgeBase().getSessionRevision()) != null;
	}

	/**
	 * Searches for valid cache for the given revision.
	 * 
	 * @return May be <code>null</code>, in case no valid data are found for the given revision.
	 */
	private synchronized Associations<T, C> getGlobalCache(long revision) {
		if (_cache == null) {
			return null;
		}
		Associations<T, C> currentValues = _cache;

		if (revision > currentValues.maxValidity()) {
			// requested revision is too new for the known cache.
			return null;
		}
		if (revision >= currentValues.minValidity()) {
			return currentValues;
		}
		while (true) {
			Associations<T, C> olderValues = (Associations<T, C>) currentValues.formerValidity();
			if (olderValues == null) {
				// only data newer than requested revision available
				return null;
			}
			if (revision > olderValues.maxValidity()) {
				// there is a gap for which no data are available. The given revision comes within
				// this gap.
				return null;
			}
			if (revision < olderValues.minValidity()) {
				// data are to new for the requested revision, find older
				currentValues = olderValues;
				continue;
			}
			return olderValues;
		}
	}

	final synchronized Associations<T, C> setGlobalCache(Associations<T, C> newCache, long cacheRevision) {
		if (_cache != null) {
			if (_cache.minValidity() < newCache.minValidity()) {
				// newer values
				long currentMaxValidity = _cache.maxValidity();
				long newMaxValidity = Math.min(currentMaxValidity, newCache.minValidity() - 1);
				if (newMaxValidity != currentMaxValidity) {
					_cache.updateMaxValidity(newMaxValidity);
				}
				newCache.setFormerValidity(_cache);
				getKnowledgeBase().registerForCleanup(_cache, newCache);
				_cache = newCache;
			} else if (_cache.minValidity() == newCache.minValidity()) {
				// Use result from concurrent lookup.
				newCache = _cache;
			} else {
				// new cache is older than the most recent cache.
				Associations<T, C> chain = _cache;
				while (true) {
					Associations<T, C> older = (Associations<T, C>) chain.formerValidity();
					if (older == null) {
						assert newCache.maxValidity() < chain.minValidity();
						chain.setFormerValidity(newCache);
						getKnowledgeBase().registerForCleanup(newCache, chain);
						break;
					}
					if (newCache.minValidity() > older.maxValidity()) {
						newCache.setFormerValidity(older);
						assert newCache.maxValidity() < chain.minValidity();
						chain.setFormerValidity(newCache);
						getKnowledgeBase().registerForCleanup(newCache, chain);
						break;
					}
					if (newCache.minValidity() == older.minValidity()) {
						// Use result from concurrent lookup.
						newCache = older;
						break;
					}
					assert newCache.minValidity() < older.minValidity();
					chain = older;
				}
			}
		} else {
			_cache = newCache;
		}
		if (cacheRevision == getKnowledgeBase().getLastLocalRevision()) {
			assert _cache == newCache;
			_cache.updateMaxValidity(Revision.CURRENT_REV);
		}
		return newCache;
	}

	/**
	 * Creates a local (context aware) version of this cache.
	 * 
	 * @param context
	 *        The context for which the cache is created.
	 * @param lookupRevision
	 *        The revision to found objects in. This may not be the session revision, e.g. during
	 *        commit.
	 * @param cacheRevision
	 *        The revision of the the new local {@link Associations buffer}, i.e. the value of
	 *        {@link Associations#minValidity()}.
	 * @param globalCache
	 *        The global (not context aware) version of this cache to use as starting point.
	 * @param localChanges
	 *        A set of locally (within the given context) changed association links that potentially
	 *        affect the cache value.
	 * @return The localized version of this cache.
	 */
	private Associations<T, C> createLocalCache(DBContext context, long lookupRevision, long cacheRevision,
			Associations<T, C> globalCache, Set<TLObject> localChanges) {
		// Local version of association cache does not exist or is not up to
		// date (the global cache is more recent than the context-local copy
		// due to an intermittent commit of another context), or there is
		// not yet a local version of this cache.
		Associations<T, C> result = newAssociations(cacheRevision, true);

		for (T globalItem : globalCache.getAssociationItems()) {
			
			if (localChanges.contains(globalItem)) {
				// Local changes are processed later on, because locally
				// changed items may either no longer match the query or now
				// match the query after the change. Therefore testing only
				// those items that matched the query before the change only
				// works for half the cases.
				continue;
			}
			
			result.addLinkResolved(globalItem);
		}
		
		for (TLObject localLink : localChanges) {
			// Decide about the the addition of the link.
			
			KnowledgeItem linkHandle = localLink.tHandle();

			if (!((DBKnowledgeItem) linkHandle).aliveState(context, lookupRevision).isAlive()) {
				// Object locally deleted, does no longer match.
				continue;
			}
			
			if (!hasCacheType(linkHandle)) {
				// Changed association link does not match the type filter.
				continue;
			}
			
			if (!matchesQuery(context, lookupRevision, (KnowledgeItemInternal) linkHandle)) {
				continue;
			}
			

			// Local link matches the criterion.
			result.addLink(linkHandle);
		}
		
		return result;
	}

	/**
	 * Create an empty {@link Associations} buffer which is valid until {@link Revision#CURRENT_REV}
	 * .
	 * 
	 * @param minValidity
	 *        See {@link Associations#minValidity()}.
	 * @param localCache
	 *        Whether the returned buffer is for local access.
	 * @return The new empty buffer.
	 */
	protected abstract Associations<T, C> newAssociations(long minValidity, boolean localCache);

	/**
	 * Create the global (not context aware) version of this cache by querying the
	 * {@link KnowledgeBase}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to query.
	 * @param revision
	 *        The revision to resolve query in.
	 * @return The global version of this cache.
	 */
	private Associations<T, C> query(DBKnowledgeBase kb, long revision) {
		if (getBaseItem().getState() == State.NEW) {
			return newAssociations(revision, false);
		}
		List<T> globalItems = _query.search(kb, _baseItem, revision);
		return indexLinks(revision, globalItems);
	}

	@Override
	final void update(long sessionRevision, List<T> globalItems) {
		setGlobalCache(indexLinks(sessionRevision, globalItems), sessionRevision);
	}

	/**
	 * Create a global {@link Associations} buffer with the given links filled in.
	 * 
	 * <p>
	 * The returned {@link Associations buffer} is valid for the given revision without any
	 * modifications.
	 * </p>
	 * 
	 * @param revision
	 *        The revision in which the {@link Associations} object is requested.
	 * @param globalItems
	 *        The links to add.
	 * @return The new {@link Associations} buffer.
	 */
	protected abstract Associations<T, C> indexLinks(long revision, List<T> globalItems);

	@Override
	void handleLocalAssociationChange(DBContext context, KnowledgeItemInternal link, int changeType) {
		Associations<T, C> localCache = context.getLocalCache(this);
		if (localCache == null) {
			// cache was not locally modified.
			return;
		}

		long sessionRevision = getKnowledgeBase().getSessionRevision();
		Associations<T, C> validatedCache = validateLocalCache(context, localCache, sessionRevision);
		if (validatedCache == null) {
			/* validation removed local cache. No need to reinstall it, because this is done during
			 * next navigation or commit. */
			return;
		}

		switch (changeType) {
			case KnowledgeObjectInternal.TYPE_CREATION: {
				if (matchesQuery(context, sessionRevision, link)) {
					localCache.addLink(link);
				}
				break;
			}
			case KnowledgeObjectInternal.TYPE_DELETION: {
				if (matchesQuery(context, sessionRevision, link)) {
					localCache.removeLink(link);
				}
				break;
			}
			case KnowledgeObjectInternal.TYPE_MODIFICATION: {
				if (matchesQuery(context, sessionRevision, link)) {
					localCache.modifyLink(link);
				} else {
					localCache.removeLink(link);
				}
				break;
			}
		}
	}

	/**
	 * May be <code>null</code> in case current local cache is not longer valid and no
	 *         changes are made.
	 */
	private Associations<T, C> validateLocalCache(DBContext context, Associations<T, C> currentLocalCache,
			long lookupRevision) {
		// minimum validity of a local cache is the "next" revision.
		long baseRevision = currentLocalCache.minValidity() - 1;
		assert baseRevision <= lookupRevision : "Downgrade of session: Formerly '" + baseRevision + "', now '"
			+ lookupRevision + "'";
		if (baseRevision == lookupRevision) {
			/* No update of the revision, therefore the local cache is still valid. */
			return currentLocalCache;
		}
		Associations<T, C> globalCache = getOrCreateGlobalCache(lookupRevision);
		assert globalCache.maxValidity() >= baseRevision : "Global cache was not valid at creation point of local cache.";
		if (baseRevision >= globalCache.minValidity()) {
			// update of current revision, but the global cache is still valid.
			return currentLocalCache;
		}
		return createLocalCache(context, lookupRevision, globalCache);
	}

	private Associations<T, C> createLocalCache(DBContext context, long lookupRevision,
			Associations<T, C> globalCache) {
		Set<TLObject> localChanges = context.getAssociationChanges(getBaseItem().tId());
		if (localChanges == null) {
			/* The local cache is outdated, but there are no longer any local changes in the
			 * context. */
			context.dropLocalCache(this);
			return null;
		}

		long cacheRevision = lookupRevision + 1;
		Associations<T, C> localCache =
			createLocalCache(context, lookupRevision, cacheRevision, globalCache, localChanges);
		context.setLocalCache(this, localCache);
		return localCache;
	}

	/**
	 * May be <code>null</code> in case current local cache is not longer valid and no
	 *         changes are made.
	 */
	private Associations<T, C> validateLocalCacheInCommit(DBContext context, Associations<T, C> currentLocalCache) {
		long commitRevision = context.getCommitNumber();

		// minimum validity of a local cache is the "next" revision.
		long expectedCommitRevision = currentLocalCache.minValidity();
		assert expectedCommitRevision <= commitRevision : "Downgrade of session: Formerly '" + expectedCommitRevision
			+ "', now '" + commitRevision + "'";
		if (expectedCommitRevision == commitRevision) {
			/* No interim commit, therefore the local cache is still valid. */
			return currentLocalCache;
		}
		
		// fetch current global cache.
		Associations<T, C> globalCache = getOrCreateGlobalCache(commitRevision - 1);
		assert globalCache.maxValidity() >= expectedCommitRevision - 1 : "Global cache was not valid at creation point of local cache.";
		if (expectedCommitRevision - 1 >= globalCache.minValidity()) {
			/* Concurrent commit occurred, but the actual value of the local cache is still valid.
			 * Therefore only the minimum validity of the cache must be adapted. */
			currentLocalCache.publishLocalValidity(commitRevision);
			return currentLocalCache;
		}
		
		Set<TLObject> localChanges = context.getAssociationChanges(getBaseItem().tId());
		if (localChanges == null) {
			/* The local cache is outdated, but there are no longer any local changes in the
			 * context. */
			return null;
		}
		
		return createLocalCache(context, commitRevision, commitRevision, globalCache, localChanges);
	}

	@Override
	synchronized void handleAssociationChange(long revision, KnowledgeItemInternal link) {
		if (_cache != null) {
			if (_cache.minValidity() < revision) {
				long maxValidity = _cache.maxValidity();
				if (maxValidity >= revision) {
					// Invalidate cache.
					_cache.updateMaxValidity(revision - 1);
				}
			}
		}
	}

	/**
	 * Whether the given link has the {@link #getBaseItem() base object} as its
	 * {@link #getReferenceAttribute() referenced attribute} and matches the query criterion of this
	 * cache.
	 * 
	 * <p>
	 * It is implicitly assumed that the given association link is of the type of this cache (
	 * {@link #getAssociationType()}).
	 * </p>
	 * 
	 * @param context
	 *        The context in which the check should happen.
	 * @param lookupRevision
	 *        The revision in which the query must match.
	 * @param link
	 *        the association link to check.
	 * 
	 * @return Whether the given link points to the base object and matches the query criterion of
	 *         this cache.
	 */
	protected abstract boolean matchesQuery(DBContext context, long lookupRevision, KnowledgeItemInternal link);

	@Override
	protected synchronized void internalCommit(DBContext context, Associations<T, C> currentLocalCache) {
		// Check for foreign commit during this transaction
		// validate cache because a concurrent commit may invalidated it.
		currentLocalCache = validateLocalCacheInCommit(context, currentLocalCache);
		if (currentLocalCache == null) {
			// may happen when local change was reverted.
			return;
		}
		assert currentLocalCache.minValidity() == context.getCommitNumber();
		currentLocalCache.markCacheGlobal();
		_cache.updateMaxValidity(currentLocalCache.minValidity() - 1);
		currentLocalCache.setFormerValidity(_cache);
		getKnowledgeBase().registerForCleanup(_cache, currentLocalCache);
		_cache = currentLocalCache;
	}
}
