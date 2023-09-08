/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.model.TLObject;

/**
 * {@link AbstractAssociationCache} bound to a certain {@link AssociationQueryImpl} type.
 * 
 * @param <T>
 *        The entry type of the cache.
 * @param <C>
 *        The collection type managed by the cache.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AssociationCacheBase<T extends TLObject, C> extends AbstractAssociationCache<T, C> {

	/* package protected */final AssociationQueryImpl<T, C> _query;

	/**
	 * Creates a {@link AssociationCacheBase}.
	 * 
	 * @param baseItem
	 *        See {@link #getBaseItem()}.
	 * @param query
	 *        See {@link #getQuery()}.
	 */
	AssociationCacheBase(KnowledgeItemInternal baseItem, AssociationQueryImpl<T, C> query) {
		super(baseItem, query);

		_query = query;
	}

	/**
	 * The query, all cached items match.
	 */
	public AbstractAssociationQuery<T, C> getQuery() {
		return _query;
	}

}
