/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.TLObject;

/**
 * Implementation of {@link OrderedLinkQuery}.
 */
@FrameworkInternal
public class OrderedLinkQueryImpl<T extends TLObject> extends FilteredAssociationQuery<T, List<T>>
		implements OrderedLinkQuery<T> {

	private final Comparator<? super T> _linkOrder;

	private final String _orderAttribute;

	private final boolean _liveResult;

	private final boolean _orderIsIndex;

	/**
	 * Creates a {@link OrderedLinkQuery}.
	 * 
	 * @param cacheKey
	 *        See {@link #getCacheKey()}.
	 * @param expectedType
	 *        See {@link #getExpectedType()}.
	 * @param associationTypeName
	 *        See {@link #getAssociationTypeName()}.
	 * @param referenceAttribute
	 *        See {@link #getReferenceAttribute()}.
	 * @param orderAttribute
	 *        The attribute according to which links are ordered. See {@link #getLinkOrder()}.
	 * @param attributeQuery
	 *        See {@link #getAttributeQuery()}.
	 * @param liveResult
	 *        See {@link #hasLiveResult()}.
	 * @param orderIsIndex
	 *        See {@link #orderIsIndex()}.
	 */
	public OrderedLinkQueryImpl(NamedConstant cacheKey, Class<T> expectedType, String associationTypeName,
			String referenceAttribute, String orderAttribute, Map<String, ?> attributeQuery, boolean liveResult,
			boolean orderIsIndex) {
		super(cacheKey, expectedType, associationTypeName, referenceAttribute, attributeQuery);

		_orderAttribute = orderAttribute;
		_linkOrder = new OrderedLinkUtil.LinkOrder(orderAttribute);
		_liveResult = liveResult;
		_orderIsIndex = orderIsIndex;
	}

	@Override
	AbstractAssociationCache<T, List<T>> createCache(KnowledgeItemInternal baseObject) {
		return new OrderedLinkCache<>(baseObject, this);
	}

	@Override
	public String getOrderAttribute() {
		return _orderAttribute;
	}

	@Override
	public Comparator<? super T> getLinkOrder() {
		return _linkOrder;
	}

	@Override
	public boolean hasLiveResult() {
		return _liveResult;
	}

	@Override
	public boolean orderIsIndex() {
		return _orderIsIndex;
	}
}