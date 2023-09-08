/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQueryImpl;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.DBObjectKey;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQueryImpl;
import com.top_logic.model.TLObject;

/**
 * Query that describes a set of {@link KnowledgeItem}s for
 * {@link KABasedCacheManager#resolveLinks(KnowledgeObject, AbstractAssociationQuery)} lookup} in a
 * {@link KABasedCacheManager}.
 * 
 * @param <T>
 *        The type of business object being retrieved.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssociationQuery<T extends TLObject> {

    /** Cache all Queries by type */
	private static final ConcurrentHashMap<Tuple, AbstractAssociationQuery<?, ?>> cachedQueries =
		new ConcurrentHashMap<>();

	/**
	 * Creates an outgoing {@link AssociationQuery} without query parameters, i.e. a query searching
	 * the {@link KnowledgeAssociation} that points to the base object via the
	 * {@link DBKnowledgeAssociation#REFERENCE_SOURCE_NAME source} attribute.
	 * 
	 * @param queryName
	 *        Descriptive name with no further semantic.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 */
	public static AssociationSetQuery<KnowledgeAssociation> createOutgoingQuery(String queryName,
			String associationTypeName) {
		return createQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_SOURCE_NAME);
	}

	/**
	 * Creates an outgoing {@link OrderedLinkQuery} without query parameters
	 * 
	 * @param queryName
	 *        Descriptive name with no further semantic.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param orderAttribute
	 *        The attribute that stores the link ordering criterion.
	 */
	public static OrderedLinkQuery<KnowledgeAssociation> createOutgoingOrderedQuery(String queryName,
			String associationTypeName, String orderAttribute) {
		return createOrderedLinkQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, orderAttribute);
	}

	/**
	 * Creates an incoming {@link AssociationQuery} without query parameters, i.e. a query searching
	 * the {@link KnowledgeAssociation} that points to the base object via the
	 * {@link DBKnowledgeAssociation#REFERENCE_DEST_NAME destination} attribute.
	 * 
	 * @param queryName
	 *        Descriptive name with no further semantic.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 */
	public static AssociationSetQuery<KnowledgeAssociation> createIncomingQuery(String queryName,
			String associationTypeName) {
		return createQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_DEST_NAME);
	}

	/**
	 * Creates an incoming {@link OrderedLinkQuery} without query parameters
	 * 
	 * @param queryName
	 *        Descriptive name with no further semantic.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param orderAttribute
	 *        The attribute that stores the link ordering criterion.
	 */
	public static OrderedLinkQuery<KnowledgeAssociation> createIncomingOrderedQuery(String queryName,
			String associationTypeName, String orderAttribute) {
		return createOrderedLinkQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_DEST_NAME, orderAttribute);
	}

	/**
	 * Creates an outgoing {@link AssociationQuery} with the given query parameters.
	 * 
	 * @param queryName
	 *        A name describing the requested query.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param attributeQuery
	 *        A map of (attribute name, attribute value) pairs that restrict the queried links.
	 */
	public static AssociationSetQuery<KnowledgeAssociation> createOutgoingQuery(String queryName,
			String associationTypeName, Map<String, ?> attributeQuery) {
		return createQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, attributeQuery);
	}

	/**
	 * Creates an outgoing {@link OrderedLinkQuery} with the given query parameters.
	 * 
	 * @param queryName
	 *        Descriptive name with no further semantic.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param orderAttribute
	 *        The attribute that stores the link ordering criterion.
	 * @param attributeQuery
	 *        A map of (attribute name, attribute value) pairs that restrict the queried links.
	 */
	public static OrderedLinkQuery<KnowledgeAssociation> createOutgoingOrderedQuery(String queryName,
			String associationTypeName, String orderAttribute, Map<String, ?> attributeQuery) {
		return createOrderedLinkQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, orderAttribute, attributeQuery, false);
	}

	/**
	 * Creates an incoming {@link AssociationQuery} with the given query parameters
	 * 
	 * @param queryName
	 *        A name describing the requested query.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param attributeQuery
	 *        A map of (attribute name, attribute value) pairs that restrict the queried links.
	 */
	public static AssociationSetQuery<KnowledgeAssociation> createIncomingQuery(String queryName,
			String associationTypeName,
			Map<String, ?> attributeQuery) {
		return createQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_DEST_NAME, attributeQuery);
	}

	/**
	 * Creates an outgoing {@link OrderedLinkQuery} with the given query parameters.
	 * 
	 * @param queryName
	 *        Descriptive name with no further semantic.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param orderAttribute
	 *        The attribute that stores the link ordering criterion.
	 * @param attributeQuery
	 *        A map of (attribute name, attribute value) pairs that restrict the queried links.
	 */
	public static OrderedLinkQuery<KnowledgeAssociation> createIncomingOrderedQuery(String queryName,
			String associationTypeName, String orderAttribute, Map<String, ?> attributeQuery) {
		return createOrderedLinkQuery(queryName, KnowledgeAssociation.class, associationTypeName,
			DBKnowledgeAssociation.REFERENCE_DEST_NAME, orderAttribute, attributeQuery, false);
	}

	/**
	 * Creates an {@link AssociationQuery} without query parameters
	 * 
	 * @param queryName
	 *        A name describing the requested query.
	 * @param expectedType
	 *        See {@link AbstractAssociationQuery#getExpectedType()}.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param referenceAttribute
	 *        the attribute to navigate backwards
	 */
	public static <T extends TLObject> AssociationSetQuery<T> createQuery(String queryName, Class<T> expectedType,
			String associationTypeName, String referenceAttribute) {
		return createQuery(queryName, expectedType, associationTypeName, referenceAttribute, null);
	}

	/**
	 * Creates an {@link AssociationQuery} with the given query parameters
	 * 
	 * @param queryName
	 *        A name describing the requested query.
	 * @param expectedType
	 *        See {@link AbstractAssociationQuery#getExpectedType()}.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param referenceAttribute
	 *        the attribute to navigate backwards
	 * @param attributeQuery
	 *        A map of (attribute name, attribute value) pairs that restrict the queried links.
	 */
	public static <T extends TLObject> AssociationSetQuery<T> createQuery(String queryName, Class<T> expectedType,
			String associationTypeName, String referenceAttribute, Map<String, ?> attributeQuery) {
		// Note: ensure that key does not collides with key in createOrderedLinkQuery(...)
		Tuple queryKind;
		if (attributeQuery == null || attributeQuery.isEmpty()) {
			queryKind = TupleFactory.newTuple(associationTypeName, referenceAttribute, expectedType);
		} else {
			attributeQuery = normaliseArguments(attributeQuery);
			queryKind = TupleFactory.newTuple(associationTypeName, referenceAttribute, expectedType, attributeQuery);
		}
		
		@SuppressWarnings("unchecked")
		AssociationSetQuery<T> existingQuery = (AssociationSetQuery<T>) cachedQueries.get(queryKind);
		if (existingQuery != null) {
			return existingQuery;
		}
		
		AssociationSetQueryImpl<T> newQuery =
			new AssociationSetQueryImpl<>(new NamedConstant(queryName), expectedType, associationTypeName,
				referenceAttribute, attributeQuery);
		@SuppressWarnings("unchecked")
		AssociationSetQuery<T> queryInCache =
			(AssociationSetQuery<T>) MapUtil.putIfAbsent(cachedQueries, queryKind, newQuery);
		return queryInCache;
	}

	/**
	 * Create an association query that retrieves ordered links pointing to a base object, where the
	 * ordering criterion is stored as integer attribute in the link instance.
	 * 
	 * @param queryName
	 *        A name describing the requested query.
	 * @param expectedType
	 *        See {@link AbstractAssociationQuery#getExpectedType()}.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param referenceAttribute
	 *        The reference attribute name that is navigated backwards.
	 * @param orderAttribute
	 *        The integer attribute of the resulting link objects that stores the ordering
	 *        criterion.
	 */
	public static <T extends TLObject> OrderedLinkQuery<T> createOrderedLinkQuery(String queryName,
			Class<T> expectedType, String associationTypeName, String referenceAttribute, String orderAttribute) {
		return createOrderedLinkQuery(queryName, expectedType, associationTypeName, referenceAttribute, orderAttribute,
			null, false);
	}

	/**
	 * Create an association query that retrieves ordered links pointing to a base object, where the
	 * ordering criterion is stored as integer attribute in the link instance.
	 * 
	 * @param queryName
	 *        A name describing the requested query.
	 * @param expectedType
	 *        See {@link AbstractAssociationQuery#getExpectedType()}.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param referenceAttribute
	 *        The reference attribute name that is navigated backwards.
	 * @param orderAttribute
	 *        The integer attribute of the resulting link objects that stores the ordering
	 *        criterion.
	 * @param attributeQuery
	 *        A map of (attribute name, attribute value) pairs that restrict the queried links.
	 * @param liveResult
	 *        Whether the query result should be a modifiable live view of the cache.
	 */
	public static <T extends TLObject> OrderedLinkQuery<T> createOrderedLinkQuery(String queryName,
			Class<T> expectedType, String associationTypeName, String referenceAttribute, String orderAttribute,
			Map<String, ?> attributeQuery, boolean liveResult) {
		return createOrderedLinkQuery(queryName, expectedType, associationTypeName, referenceAttribute, orderAttribute,
			attributeQuery, liveResult, false);
	}
	
	/**
	 * Create an association query that retrieves ordered links pointing to a base object, where the
	 * ordering criterion is stored as integer attribute in the link instance.
	 * 
	 * @param queryName
	 *        A name describing the requested query.
	 * @param expectedType
	 *        See {@link AbstractAssociationQuery#getExpectedType()}.
	 * @param associationTypeName
	 *        The association type name this query resolves links from.
	 * @param referenceAttribute
	 *        The reference attribute name that is navigated backwards.
	 * @param orderAttribute
	 *        The integer attribute of the resulting link objects that stores the ordering
	 *        criterion.
	 * @param attributeQuery
	 *        A map of (attribute name, attribute value) pairs that restrict the queried links.
	 * @param liveResult
	 *        Whether the query result should be a modifiable live view of the cache.
	 * @param orderIsIndexAttribute
	 *        Whether the order attribute is actually an index attribute. If <code>true</code> it is
	 *        expected that the order of the elements are 0,1,2,3,... etc.
	 */
	public static <T extends TLObject> OrderedLinkQuery<T> createOrderedLinkQuery(String queryName,
			Class<T> expectedType, String associationTypeName, String referenceAttribute, String orderAttribute,
			Map<String, ?> attributeQuery, boolean liveResult, boolean orderIsIndexAttribute) {
		// Note: ensure that key does not collides with key in createQuery(...)
		Tuple queryKind;
		if (attributeQuery == null || attributeQuery.isEmpty()) {
			queryKind = TupleFactory.newTuple(associationTypeName, referenceAttribute, expectedType, orderAttribute,
				liveResult, orderIsIndexAttribute);
		} else {
			attributeQuery = normaliseArguments(attributeQuery);
			queryKind = TupleFactory.newTuple(associationTypeName, referenceAttribute, expectedType, orderAttribute,
				attributeQuery, liveResult, orderIsIndexAttribute);
		}

		@SuppressWarnings("unchecked")
		OrderedLinkQuery<T> existingQuery = (OrderedLinkQuery<T>) cachedQueries.get(queryKind);
		if (existingQuery != null) {
			return existingQuery;
		}

		NamedConstant cacheKey = new NamedConstant(queryName);
		OrderedLinkQuery<T> newQuery = new OrderedLinkQueryImpl<>(cacheKey, expectedType, associationTypeName,
			referenceAttribute, orderAttribute, attributeQuery, liveResult, orderIsIndexAttribute);
		@SuppressWarnings("unchecked")
		OrderedLinkQuery<T> queryInCache =
			(OrderedLinkQuery<T>) MapUtil.putIfAbsent(cachedQueries, queryKind, newQuery);
		return queryInCache;
	}

	/**
	 * Replaces objects with their keys for storage in a query filter arguments map.
	 * 
	 * @see #fillInitialValuesFromQueryArguments(KnowledgeBase, KeyValueBuffer, Map)
	 */
	public static Map<String, ?> normaliseArguments(Map<String, ?> args) {
		boolean containsKBReference = containsKBReference(args);
		if (!containsKBReference) {
			return args;
		}
		int numberArguments = args.size();
		switch (numberArguments) {
			case 1: {
				Entry<String, ?> entry = args.entrySet().iterator().next();
				return Collections.singletonMap(entry.getKey(), normalizeValue(entry.getValue()));
			}
			default: {
				HashMap<String, Object> normalisedMap = MapUtil.newMap(numberArguments);
				for (Entry<String, ?> entry : args.entrySet()) {
					Object oldValue = entry.getValue();
					Object newValue;
					if (hasKBReference(oldValue)) {
						newValue = normalizeValue(oldValue);
					} else {
						newValue = oldValue;
					}
					normalisedMap.put(entry.getKey(), newValue);
				}
				return normalisedMap;
			}
		}
	}

	private static boolean hasKBReference(Object value) {
		return value instanceof KnowledgeItem || value instanceof DBObjectKey;
	}

	private static Object normalizeValue(Object value) {
		if (value instanceof KnowledgeItem) {
			value = ((KnowledgeItem) value).tId();
		}
		if (value instanceof DBObjectKey) {
			DBObjectKey key = (DBObjectKey) value;
			value = new DefaultObjectKey(key.getBranchContext(), key.getHistoryContext(), key.getObjectType(),
				key.getObjectName());
		}
		return value;
	}

	/**
	 * Inverse operation of {@link #normaliseArguments(Map)}.
	 * 
	 * <p>
	 * Puts the filter arguments to a given {@link KeyValueBuffer} for creating a new link matching
	 * the given filter arguments.
	 * </p>
	 */
	public static void fillInitialValuesFromQueryArguments(KnowledgeBase kb,
			KeyValueBuffer<String, Object> initialValues, Map<String, ?> args) {
		for (Entry<String, ?> entry : args.entrySet()) {
			Object oldValue = entry.getValue();
			Object newValue;
			if (oldValue instanceof ObjectKey) {
				newValue = kb.resolveObjectKey((ObjectKey) oldValue);
			} else {
				newValue = oldValue;
			}
			initialValues.put(entry.getKey(), newValue);
		}
	}

	private static boolean containsKBReference(Map<String, ?> args) {
		for (Object argument : args.values()) {
			if (hasKBReference(argument)) {
				return true;
			}
		}
		return false;
	}

}
