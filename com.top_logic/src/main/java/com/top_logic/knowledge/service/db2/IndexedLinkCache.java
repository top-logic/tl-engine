/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * {@link FilteredAssociationCache} that manages an ordered set of links.
 * 
 * @see OrderedLinkQuery#getLinkOrder()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class IndexedLinkCache<K, T extends TLObject> extends FilteredAssociationCache<T, BidiMap<K, T>> {

	/**
	 * Creates a {@link IndexedLinkCache}.
	 * 
	 * @param baseItem
	 *        See {@link #getBaseItem()}.
	 * @param query
	 *        See {@link #getQuery()}.
	 */
	IndexedLinkCache(KnowledgeItemInternal baseItem, IndexedLinkQueryImpl<K, T> query) {
		super(baseItem, query);
	}

	@Override
	protected AssociationsMap<K, T> newAssociations(long minValidity,
			boolean localCache) {
		if (getQuery().hasLiveResult()) {
			return new LiveAssociationsMap<>(this, minValidity, Revision.CURRENT_REV, noItems(), localCache,
				getBaseItem());
		} else {
			return new StableAssociationsMap<>(this, minValidity, Revision.CURRENT_REV, noItems(), localCache);
		}
	}

	private List<T> noItems() {
		return Collections.<T> emptyList();
	}

	@Override
	protected AssociationsMap<K, T> indexLinks(long revision, List<T> globalItems) {
		if (getQuery().hasLiveResult()) {
			return new LiveAssociationsMap<>(this, revision, revision, globalItems, false, getBaseItem());
		} else {
			return new StableAssociationsMap<>(this, revision, revision, globalItems, false);
		}
	}

	@Override
	public IndexedLinkQueryImpl<K, T> getQuery() {
		return (IndexedLinkQueryImpl<K, T>) super.getQuery();
	}

}
