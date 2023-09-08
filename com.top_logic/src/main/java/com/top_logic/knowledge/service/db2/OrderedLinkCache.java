/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.List;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * {@link FilteredAssociationCache} that manages an ordered set of links.
 * 
 * @see OrderedLinkQuery#getLinkOrder()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class OrderedLinkCache<T extends TLObject> extends FilteredAssociationCache<T, List<T>> {

	/**
	 * Creates a {@link OrderedLinkCache}.
	 * 
	 * @param baseItem
	 *        See {@link #getBaseItem()}.
	 * @param query
	 *        See {@link #getQuery()}.
	 */
	OrderedLinkCache(KnowledgeItemInternal baseItem, OrderedLinkQueryImpl<T> query) {
		super(baseItem, query);
	}

	@Override
	protected AssociationsList<T> newAssociations(long minValidity, boolean localCache) {
		return newAssociationsList(minValidity, Revision.CURRENT_REV, Collections.<T> emptyList(), localCache);
	}

	@Override
	protected AssociationsList<T> indexLinks(long revision, List<T> globalItems) {
		return newAssociationsList(revision, revision, globalItems, false);
	}

	private AssociationsList<T> newAssociationsList(long minValidity, long maxValidity, List<T> items,
			boolean localCache) {
		if (getQuery().hasLiveResult()) {
			if (getQuery().orderIsIndex()) {
				return new LiveIndexedAssociationsList<>(this, minValidity, maxValidity, items, localCache);
			} else {
				return new LiveOrderedAssociationsList<>(this, minValidity, maxValidity, items, localCache);
			}
		} else {
			return new StableAssociationsList<>(this, minValidity, maxValidity, items, localCache);
		}
	}

	@Override
	public OrderedLinkQuery<T> getQuery() {
		return (OrderedLinkQuery<T>) super.getQuery();
	}

}
