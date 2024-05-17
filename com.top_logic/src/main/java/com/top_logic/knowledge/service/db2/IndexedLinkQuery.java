/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Map;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.NamedConstant;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.model.TLObject;

/**
 * {@link AbstractAssociationQuery} that indexes links by an attribute of the target.
 */
public interface IndexedLinkQuery<K, T extends TLObject> extends AbstractAssociationQuery<T, BidiMap<K, T>> {

	/**
	 * Creates a {@link IndexedLinkQuery} without filter.
	 * 
	 * @param cacheKey
	 *        See {@link #getCacheKey()}.
	 * @param expectedType
	 *        See {@link #getExpectedType()}.
	 * @param associationTypeName
	 *        See {@link #getAssociationTypeName()}.
	 * @param referenceAttribute
	 *        See {@link #getReferenceAttribute()}.
	 * @param keyAttribute
	 *        See {@link #getKeyAttribute()}.
	 * @param keyType
	 *        See {@link #getKeyType()}.
	 * 
	 * @see #indexedLinkQuery(NamedConstant, Class, String, String, String, Class, Map)
	 */
	public static <K, T extends TLObject> IndexedLinkQuery<K, T> indexedLinkQuery(NamedConstant cacheKey,
			Class<T> expectedType, String associationTypeName, String referenceAttribute, String keyAttribute,
			Class<K> keyType) {
		return indexedLinkQuery(cacheKey, expectedType, associationTypeName, referenceAttribute,
			keyAttribute, keyType, null);
	}

	/**
	 * Creates a {@link IndexedLinkQuery}.
	 * 
	 * @param cacheKey
	 *        See {@link #getCacheKey()}.
	 * @param expectedType
	 *        See {@link #getExpectedType()}.
	 * @param associationTypeName
	 *        See {@link #getAssociationTypeName()}.
	 * @param referenceAttribute
	 *        See {@link #getReferenceAttribute()}.
	 * @param keyAttribute
	 *        See {@link #getKeyAttribute()}.
	 * @param keyType
	 *        See {@link #getKeyType()}.
	 * @param attributeQuery
	 *        See {@link #getAttributeQuery()}.
	 */
	public static <K, T extends TLObject> IndexedLinkQuery<K, T> indexedLinkQuery(NamedConstant cacheKey,
			Class<T> expectedType, String associationTypeName, String referenceAttribute, String keyAttribute,
			Class<K> keyType, Map<String, ?> attributeQuery) {
		return indexedLinkQuery(cacheKey, expectedType, associationTypeName, referenceAttribute, keyAttribute, keyType,
			attributeQuery, false);
	}

	/**
	 * Creates a {@link IndexedLinkQuery}.
	 * 
	 * @param cacheKey
	 *        See {@link #getCacheKey()}.
	 * @param expectedType
	 *        See {@link #getExpectedType()}.
	 * @param associationTypeName
	 *        See {@link #getAssociationTypeName()}.
	 * @param referenceAttribute
	 *        See {@link #getReferenceAttribute()}.
	 * @param keyAttribute
	 *        See {@link #getKeyAttribute()}.
	 * @param keyType
	 *        See {@link #getKeyType()}.
	 * @param attributeQuery
	 *        See {@link #getAttributeQuery()}.
	 * @param liveResult
	 *        See {@link #hasLiveResult()}.
	 */
	public static <K, T extends TLObject> IndexedLinkQuery<K, T> indexedLinkQuery(NamedConstant cacheKey,
			Class<T> expectedType, String associationTypeName, String referenceAttribute, String keyAttribute,
			Class<K> keyType, Map<String, ?> attributeQuery, boolean liveResult) {
		attributeQuery = AssociationQuery.normaliseArguments(CollectionUtil.nonNull(attributeQuery));
		return new IndexedLinkQueryImpl<>(cacheKey, expectedType, associationTypeName, referenceAttribute,
			keyAttribute, keyType, attributeQuery, liveResult);
	}

	/**
	 * The attribute whose values are used to index link objects.
	 * 
	 * <p>
	 * When {@link #getKeyAttribute()} is <code>null</code>, the link itself is used as index.
	 * </p>
	 */
	String getKeyAttribute();

	/**
	 * The dynamic type of the {@link #getKeyAttribute()}.
	 */
	Class<K> getKeyType();

	/**
	 * Whether the query result should be modifiable.
	 */
	boolean hasLiveResult();

}
