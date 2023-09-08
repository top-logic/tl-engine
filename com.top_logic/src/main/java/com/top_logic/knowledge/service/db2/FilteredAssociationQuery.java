/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.search.InternalExpressionFactory.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.MOReferenceInternal;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.CompiledQueryCache;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * {@link AssociationQueryImpl} that additionally specifies a key/value filter.
 * 
 * @see #getAttributeQuery()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class FilteredAssociationQuery<T extends TLObject, C> extends AssociationQueryImpl<T, C> {

	/**
	 * @see #getAttributeQuery()
	 */
	private final Map<String, ?> _attributeQuery;

	/**
	 * Creates a {@link FilteredAssociationQuery}.
	 * 
	 * @param cacheKey
	 *        See {@link #getCacheKey()}.
	 * @param expectedType
	 *        See {@link #getExpectedType()}.
	 * @param associationTypeName
	 *        See {@link #getAssociationTypeName()}.
	 * @param referenceAttribute
	 *        See {@link #getReferenceAttribute()}.
	 * @param attributeQuery
	 *        See {@link #getAttributeQuery()}.
	 */
	public FilteredAssociationQuery(NamedConstant cacheKey, Class<T> expectedType, String associationTypeName,
			String referenceAttribute, Map<String, ?> attributeQuery) {
		super(cacheKey, expectedType, associationTypeName, referenceAttribute);

		_attributeQuery = attributeQuery == null ? Collections.<String, Object> emptyMap() : attributeQuery;
	}

	@Override
	public Map<String, ?> getAttributeQuery() {
		return _attributeQuery;
	}

	@Override
	protected final CompiledQuery<T> getSearchQuery(DBKnowledgeBase kb, MetaObject targetType) {
		CompiledQueryCache cache = kb.getQueryCache();
		Pair<NamedConstant, MetaObject> cacheKey = new Pair<>(getCacheKey(), targetType);
		CompiledQuery<T> query = cache.getQuery(cacheKey);
		if (query == null) {
			CompiledQuery<T> newQuery = createSearchQuery(kb, targetType);
			query = cache.storeQuery(cacheKey, newQuery);
		}
		return query;
	}

	@Override
	protected final RevisionQueryArguments createSearchArguments(DBKnowledgeBase kb, KnowledgeItem baseItem, long dataRevision) {
		RevisionQueryArguments revisionArgs = revisionArgs();
		Branch requestedBranch = kb.getHistoryManager().getBranch(baseItem.getBranchContext());
		revisionArgs.setRequestedBranch(requestedBranch);
		MOReferenceInternal referenceAttr = getReferenceAttributeInternal(kb.getMORepository());
		switch (referenceAttr.getHistoryType()) {
			case CURRENT:
				revisionArgs.setRequestedRevision(baseItem.getHistoryContext());
				if (HistoryUtils.isCurrent(baseItem)) {
					revisionArgs.setDataRevision(dataRevision);
				}
				break;
			case HISTORIC: {
				revisionArgs.setRequestedRevision(Revision.CURRENT_REV);
				revisionArgs.setDataRevision(dataRevision);
				break;
			}
			case MIXED: {
				revisionArgs.setRequestedRevision(Revision.CURRENT_REV);
				if (HistoryUtils.isCurrent(baseItem)) {
					revisionArgs.setDataRevision(dataRevision);
				}
				break;
			}
		}
		revisionArgs.setArguments(baseItem);
		return revisionArgs;
	}

	private CompiledQuery<T> createSearchQuery(DBKnowledgeBase kb, MetaObject targetType) {
		MORepository scope = kb.getMORepository();
		String itemParameterName = "_item";
		SetExpression search = createSearch(scope, createBaseSearch(scope, targetType, itemParameterName));
		List<ParameterDeclaration> parameters = params(paramDecl(BasicTypes.ITEM_TYPE_NAME, itemParameterName));
		return kb.compileQuery(queryResolved(parameters, search, NO_ORDER, getExpectedType()));
	}

	/**
	 * Creates an expression which which finds all links of type {@link #getAssociationTypeName()}
	 * whose value of the attribute {@link #getReferenceAttribute()} is the item later filled into
	 * the parameter with the given parameter name.
	 * 
	 * @param targetType
	 *        the type pf the object later filled into the parameter
	 */
	private Expression createBaseSearch(MORepository scope, MetaObject targetType, String param) {
		MOReferenceInternal reference = getReferenceAttributeInternal(scope);
		return reference.getStorage().getRefererExpression(reference, getAssociationType(scope), targetType, param);
	}

	/**
	 * Enhances the given search by adding expressions that ensure that the found objects matches
	 * the given {@link #getAttributeQuery() arguments}.
	 * 
	 * @see AssociationQueryImpl#createSearch(MORepository, Expression)
	 */
	@Override
	protected SetExpression createSearch(MORepository scope, Expression baseSearch) {
		Map<String, ?> attributeQuery = this.getAttributeQuery();
		{
			for (Entry<String, ?> conditionEntry : attributeQuery.entrySet()) {

				String attributeName = conditionEntry.getKey();
				MOAttribute attribute = getAttribute(scope, attributeName);
				Expression attributeTyped;
				if (attribute instanceof MOReference) {
					attributeTyped = referenceTyped((MOReference) attribute);
				} else {
					attributeTyped = attributeTyped(attribute);
				}
				Object value = conditionEntry.getValue();
				if (value == null) {
					baseSearch = and(baseSearch, isNull(attributeTyped));
				} else {
					baseSearch = and(baseSearch, eqBinary(literal(value), attributeTyped));
				}
			}
		}
		return filter(anyOfTyped(getAssociationType(scope)), baseSearch);
	}

}
