/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Map;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.NamedConstant;
import com.top_logic.model.TLObject;

/**
 * {@link IndexedLinkQuery} implementation.
 */
class IndexedLinkQueryImpl<K, T extends TLObject> extends FilteredAssociationQuery<T, BidiMap<K, T>>
		implements IndexedLinkQuery<K, T> {

	private final String _keyAttribute;

	private final Class<K> _keyType;

	private final boolean _liveResult;

	IndexedLinkQueryImpl(NamedConstant cacheKey, Class<T> expectedType, String associationTypeName,
			String referenceAttribute, String keyAttribute, Class<K> keyType, Map<String, ?> attributeQuery,
			boolean liveResult) {
		super(cacheKey, expectedType, associationTypeName, referenceAttribute, attributeQuery);

		assert keyType != null : "Key type must not be null.";

		_keyAttribute = keyAttribute;
		_keyType = keyType;
		_liveResult = liveResult;
	}

	@Override
	AbstractAssociationCache<T, BidiMap<K, T>> createCache(KnowledgeItemInternal baseObject) {
		return new IndexedLinkCache<>(baseObject, this);
	}

	@Override
	public String getKeyAttribute() {
		return _keyAttribute;
	}

	@Override
	public Class<K> getKeyType() {
		return _keyType;
	}

	@Override
	public boolean hasLiveResult() {
		return _liveResult;
	}

}
