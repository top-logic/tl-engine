/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.search.InternalExpressionFactory.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.MOReferenceInternal;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.model.TLObject;

/**
 * Common API for all association queries to build {@link AbstractAssociationCache}s from.
 * 
 * @param <T>
 *        The type of the resolved objects.
 * @param <C>
 *        The collection type managed by the cache.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AssociationQueryImpl<T extends TLObject, C> implements AbstractAssociationQuery<T, C> {

	/**
	 * @see #getCacheKey()
	 */
	private final NamedConstant _cacheKey;

	/**
	 * @see #getAssociationTypeName()
	 */
	private final String associationTypeName;

	/**
	 * see {@link #getReferenceAttribute()}
	 */
	private final String _referenceAttribute;

	/**
	 * @see #getExpectedType()
	 */
	private final Class<T> _expectedType;

	/**
	 * Creates a {@link AssociationQueryImpl}.
	 * 
	 * @param cacheKey
	 *        See {@link #getCacheKey()}.
	 * @param expectedType
	 *        See {@link #getExpectedType()}.
	 * @param associationTypeName
	 *        See {@link #getAssociationTypeName()}.
	 * @param referenceAttribute
	 *        See {@link #getReferenceAttribute()}.
	 */
	public AssociationQueryImpl(NamedConstant cacheKey, Class<T> expectedType, String associationTypeName,
			String referenceAttribute) {
		_cacheKey = cacheKey;
		_expectedType = expectedType;

		this.associationTypeName = associationTypeName;
		if (referenceAttribute == null) {
			throw new NullPointerException("'referenceAttribute' must not be 'null'.");
		}
		_referenceAttribute = referenceAttribute;
	}

	@Override
	public final NamedConstant getCacheKey() {
		return _cacheKey;
	}

	@Override
	public final String getAssociationTypeName() {
		return associationTypeName;
	}

	@Override
	public final String getReferenceAttribute() {
		return _referenceAttribute;
	}

	@Override
	public final Class<T> getExpectedType() {
		return _expectedType;
	}

	@Override
	public Map<String, ?> getAttributeQuery() {
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		return _cacheKey.asString() + ' ' + associationTypeName + ':' + _referenceAttribute;
	}

	/**
	 * Executes this {@link AssociationQuery} with the given {@link KnowledgeItem}. The current
	 * transaction is not included in the search.
	 * 
	 * @param kb
	 *        the {@link KnowledgeBase} to use
	 * @param baseItem
	 *        the item which serves as target for the association
	 * @param dataRevision
	 *        The revision of the data which the found elements must have.
	 * 
	 * @return list of objects which points to the given item via the represented association.
	 */
	public final List<T> search(DBKnowledgeBase kb, KnowledgeItem baseItem, long dataRevision) {
		CompiledQuery<T> searchQuery = getSearchQuery(kb, baseItem.tTable());
		RevisionQueryArguments searchArgs = createSearchArguments(kb, baseItem, dataRevision);
		return searchQuery.search(searchArgs);
	}

	/**
	 * The {@link CompiledQuery} that can be used to fetch all links matching this query on a
	 * certain base object.
	 * 
	 * @param kb
	 *        The context {@link KnowledgeBase}.
	 * @param metaObject
	 *        The type of the base item.
	 * @return The query to execute.
	 * 
	 * @see #createSearchArguments(DBKnowledgeBase, KnowledgeItem, long)
	 */
	protected abstract CompiledQuery<T> getSearchQuery(DBKnowledgeBase kb, MetaObject metaObject);

	/**
	 * Creates an arguments object for resolving
	 * {@link #getSearchQuery(DBKnowledgeBase, MetaObject)}.
	 * 
	 * @param kb
	 *        The context {@link KnowledgeBase}.
	 * @param baseItem
	 *        The base item this query is resolved on.
	 * @param dataRevision
	 *        The revision of the data of the found items.
	 * @return The argument object for {@link CompiledQuery#search(RevisionQueryArguments)}.
	 */
	protected abstract RevisionQueryArguments createSearchArguments(DBKnowledgeBase kb, KnowledgeItem baseItem, long dataRevision);

	/**
	 * Creates a search expression that find all links matching this query and have a value of the
	 * attribute {@link #getReferenceAttribute()} whose name is in the given collection.
	 * <p>
	 * <b>This method is only possible for branch local references</b>
	 * </p>
	 */
	public final SetExpression createSearch(MORepository scope, Collection<Object> baseObjectNames) {
		assert !getReferenceAttributeInternal(scope).isBranchGlobal();
		return createSearch(scope, inSet(createReferenceExpr(scope), setLiteral(baseObjectNames)));
	}

	/**
	 * Creates an search expression to search objects matching this query and the given base search
	 * expression.
	 */
	protected abstract SetExpression createSearch(MORepository scope, Expression baseSearch);

	/**
	 * Creates an expression which describes the name of the object referenced by
	 * {@link #getReferenceAttribute()}.
	 */
	public final Expression createReferenceExpr(MORepository scope) {
		MOReference referenceAttribute = getReferenceAttributeInternal(scope);
		Expression attributeExpr = referenceTyped(referenceAttribute, ReferencePart.name);
		return attributeExpr;
	}

	/**
	 * The {@link MOReferenceInternal} implementation of the {@link #getReferenceAttribute()} in the
	 * given scope.
	 */
	protected final MOReferenceInternal getReferenceAttributeInternal(MORepository scope) {
		return (MOReferenceInternal) getAttribute(scope, getReferenceAttribute());
	}

	/**
	 * The {@link MOAttribute} of the {@link #getAssociationTypeName()} with the given name in the
	 * given scope.
	 */
	protected final MOAttribute getAttribute(MORepository scope, String attributeName) {
		try {
			MOClass association = getAssociationType(scope);
			return association.getAttribute(attributeName);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException("Query with wrong attribute.", ex);
		}
	}

	/**
	 * The {@link MOClass} implementation of the {@link #getAssociationTypeName()} in the given
	 * scope.
	 */
	protected final MOClass getAssociationType(MORepository scope) {
		MOClass association;
		try {
			association = (MOClass) scope.getMetaObject(this.getAssociationTypeName());
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Query for unknown type.", ex);
		}
		return association;
	}

	/**
	 * Creates a corresponding cache implementation.
	 * 
	 * @param baseObject
	 *        The base object of the cache.
	 * @return The new cache instance.
	 */
	abstract AbstractAssociationCache<T, C> createCache(KnowledgeItemInternal baseObject);

}
