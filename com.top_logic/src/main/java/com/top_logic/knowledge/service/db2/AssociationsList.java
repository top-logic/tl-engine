/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Comparator;
import java.util.List;

import com.top_logic.model.TLObject;

/**
 * {@link Associations} buffer that provides an ordered set of links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AssociationsList<T extends TLObject> extends Associations<T, List<T>> {

	/**
	 * {@link Comparator} according to which the cache should be ordered.
	 */
	protected final Comparator<? super T> _linkOrder;

	/**
	 * Attribute holding the link index value according to which ordering occurs.
	 */
	protected final String _orderAttribute;

	/**
	 * Creates a {@link AssociationsList}.
	 * 
	 * @param associationCache
	 *        The owning cache.
	 * @param items
	 *        The initial link items to put into the buffer.
	 * @param localCache
	 *        Whether this {@link AssociationsList} is for global or local (with modification)
	 *        access.
	 */
	public AssociationsList(OrderedLinkCache<T> associationCache, long minValidity, long maxValidity, List<T> items,
			boolean localCache) {
		super(associationCache, minValidity, maxValidity, localCache);

		_orderAttribute = associationCache.getQuery().getOrderAttribute();
		_linkOrder = associationCache.getQuery().getLinkOrder();
	}

}
