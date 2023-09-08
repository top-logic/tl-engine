/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;

/**
 * Implementation of a concrete global or context local cache version.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Associations<T extends TLObject, C> extends ValidityChainImpl {

	private final Class<T> _expectedType;

	private volatile boolean _localCache;

	/**
	 * @param localCache
	 *        Whether this {@link Associations} represents a local cache.
	 */
	public Associations(AssociationCacheBase<T, C> associationCache, long minValidity, long maxValidity,
			boolean localCache) {
		super(minValidity, maxValidity);
		_localCache = localCache;
		_expectedType = associationCache.getQuery().getExpectedType();
	}

	public final Class<T> expectedType() {
		return _expectedType;
	}

	/**
	 * An immutable view of the association links in this cache.
	 * 
	 * <p>
	 * An {@link Iterable} version of this aggregation can be accessed through
	 * {@link #getAssociationItems()}.
	 * </p>
	 */
	public abstract C getAssociations();

	/**
	 * The underlying {@link Collection} that can be used to access underlying association links
	 * from {@link #getAssociations()}.
	 */
	protected abstract Iterable<T> getAssociationItems();

	public final boolean addLink(KnowledgeItem link) {
		checkModifiable();
		return addLinkResolved(CollectionUtil.dynamicCast(_expectedType, link.getWrapper()));
	}

	/**
	 * Adds a new link object to this buffer.
	 * 
	 * @param link
	 *        the link object.
	 * @return Whether the links was newly added, <code>false</code>, if the link was already
	 *         contained in this buffer.
	 */
	protected abstract boolean addLinkResolved(T link);

	public final boolean removeLink(KnowledgeItem link) {
		checkModifiable();
		return removeLinkResolved(link.getWrapper());
	}

	/**
	 * Removes a link from this buffer.
	 * 
	 * @param link
	 *        The link to remove.
	 * @return Whether the link was member of this buffer before this call.
	 */
	protected abstract boolean removeLinkResolved(TLObject link);

	public final void modifyLink(KnowledgeItem link) {
		checkModifiable();
		modifyLinkResolved(CollectionUtil.dynamicCast(_expectedType, link.getWrapper()));
	}

	/**
	 * Handles modification of a link in this buffer.
	 * 
	 * @param link
	 *        The modified link.
	 */
	protected void modifyLinkResolved(T link) {
		// Default implementation.
		addLinkResolved(link);
	}

	protected void markCacheGlobal() {
		_localCache = false;
	}

	protected void checkModifiable() {
		if (!isLocalCache()) {
			throw new IllegalStateException("This cache contains global, shared values. It must not be modified.");
		}
	}

	protected boolean isLocalCache() {
		return _localCache;
	}

}
