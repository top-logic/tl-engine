/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.basic.db.sql.SQLFactory.and;
import static com.top_logic.basic.db.sql.SQLFactory.eq;
import static com.top_logic.basic.db.sql.SQLFactory.inSet;
import static com.top_logic.basic.db.sql.SQLFactory.isNull;
import static com.top_logic.basic.db.sql.SQLFactory.not;
import static com.top_logic.basic.db.sql.SQLFactory.or;
import static com.top_logic.dob.sql.SQLFactory.column;
import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.search.ExpressionFactory.and;
import static com.top_logic.knowledge.search.ExpressionFactory.inSet;
import static com.top_logic.knowledge.search.ExpressionFactory.isNull;
import static com.top_logic.knowledge.search.ExpressionFactory.le;
import static com.top_logic.knowledge.search.ExpressionFactory.literal;
import static com.top_logic.knowledge.search.ExpressionFactory.not;
import static com.top_logic.knowledge.search.ExpressionFactory.or;
import static com.top_logic.knowledge.search.InternalExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.KIReferenceStorage;
import com.top_logic.knowledge.MOReferenceInternal;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.ConcatenatedCompiledQuery;
import com.top_logic.knowledge.search.EmptyCompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.QueryArguments;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * Holder class of expressions used in {@link KnowledgeBase}.
 * 
 *          com.top_logic.knowledge.service.db2.DBKnowledgeBase.Expressions
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class Expressions {

	private static final MetaObject REFERENCE_TYPE = IdentifierTypes.REFERENCE_MO_TYPE;

	final CompiledQuery<KnowledgeAssociation> anyAssociations;

	final CompiledQuery<KnowledgeObject> anyObjects;

	private final DBKnowledgeBase kb;

	private final CompiledQuery<Revision> revisionAt;

	private final CompiledQuery<Branch> childBranches;

	private final CompiledQuery<KnowledgeItemInternal> dataBranchByType;

	private final CompiledQuery<KnowledgeItemInternal> branchedTypes;

	private final HashMap<Object, CompiledQuery<KnowledgeItem>> refetchChunk =
		new HashMap<>();

	private final ConcurrentHashMap<Object, CompiledQuery<KnowledgeItem>> branchCrossingReferences =
		new ConcurrentHashMap<>();

	private final ConcurrentHashMap<Object, CompiledQuery<KnowledgeItem>> branchCrossingValues =
		new ConcurrentHashMap<>();

	private final ConcurrentHashMap<Object, CompiledQuery<?>> allOfType =
		new ConcurrentHashMap<>();

	private final ConcurrentHashMap<Object, Object> anyReferee = new ConcurrentHashMap<>();

	/**
	 * Storage for {@link CompiledQuery} in {@link #refereeQuery(String, boolean, boolean)}.
	 */
	private final ConcurrentHashMap<Object, CompiledQuery<KnowledgeAssociation>> refereeQueries =
		new ConcurrentHashMap<>();

	/**
	 * Storage for {@link CompiledQuery} in {@link #associationsBetweenQuery(String)}.
	 */
	private final ConcurrentHashMap<Object, CompiledQuery<KnowledgeAssociation>> associationsBetween =
			new ConcurrentHashMap<>();
	
	Expressions(DBKnowledgeBase dbKnowledgeBase) {
		kb = dbKnowledgeBase;

		SimpleQuery<KnowledgeAssociation> simpleKAQuery =
			SimpleQuery.queryUnresolved(KnowledgeAssociation.class, kb.getAssociationType(), literal(Boolean.TRUE))
				.includeSubType();
		anyAssociations = kb.compileSimpleQuery(simpleKAQuery);

		SimpleQuery<KnowledgeObject> simpleKOQuery =
			SimpleQuery.queryUnresolved(KnowledgeObject.class, kb.lookupType(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME), literal(Boolean.TRUE)).includeSubType();
		anyObjects = kb.compileSimpleQuery(simpleKOQuery);

		revisionAt = createRevisionAtQuery();
		childBranches = createChildBranchesQuery();
		dataBranchByType = createDataBranchByTypeQuery();
		branchedTypes = createBranchedTypesQuery();
	}

	/**
	 * query to get all child branches of a given branch.
	 * 
	 * @see #childBranchArguments(Long) arguments for the returned query
	 */
	CompiledQuery<Branch> childBranchQuery() {
		return childBranches;
	}

	private CompiledQuery<Branch> createChildBranchesQuery() {
		String branchParamName = "_baseBranch";
		MOKnowledgeItemImpl branchType = kb.getBranchType();
		MOAttribute baseBranchAttr = BranchSupport.getBaseBranchAttr(branchType).getAttribute();
		Expression baseBranchCheck = eqBinary(attributeTyped(baseBranchAttr), param(branchParamName));
		SetExpression search = filter(allOf(branchType), baseBranchCheck);
		List<ParameterDeclaration> parameters =
			params(paramDecl(baseBranchAttr.getMetaObject(), branchParamName));
		RevisionQuery<Branch> query = queryUnresolved(BranchParam.all, RangeParam.complete, parameters, search, NO_ORDER, Branch.class);
		CompiledQuery<Branch> compiledQuery = kb.compileQuery(query);
		return compiledQuery;
	}

	/**
	 * @param branchId
	 *        the id of the branch to get child branches for
	 * 
	 * @return arguments to deliver to {@link #childBranchQuery()}
	 * 
	 * @see #childBranchQuery() query for the returned arguments
	 */
	RevisionQueryArguments childBranchArguments(Long branchId) {
		return revisionArgs().setArguments(branchId);
	}

	/**
	 * query to get the revision corresponding to a specific date.
	 * 
	 * @see #revisionAtArguments(long) arguments for the returned query
	 */
	CompiledQuery<Revision> revisionAtQuery() {
		return revisionAt;
	}

	private CompiledQuery<Revision> createRevisionAtQuery() {
		String dateParam = "_date";
		MOKnowledgeItem revisionType = kb.getRevisionType();
		MOAttribute dateAttribute = RevisionType.getDateAttribute(revisionType).getAttribute();
		MOAttribute revAttribute = RevisionType.getRevisionAttribute(revisionType).getAttribute();
		Expression dateCheck = le(attributeTyped(dateAttribute), param(dateParam));
		SetExpression search = ExpressionFactory.filter(anyOfTyped(kb.getRevisionType()), dateCheck);
		Order revisionOrder = orderDesc(attributeTyped(revAttribute));
		List<ParameterDeclaration> queryParameters = params(paramDecl(revAttribute.getMetaObject(), dateParam));
		RevisionQuery<Revision> query =
			queryUnresolved(BranchParam.single, RangeParam.first, queryParameters, search, revisionOrder, Revision.class);
		return kb.compileQuery(query);
	}

	/**
	 * @param date
	 *        the date for which a revision is searched
	 * 
	 * @return arguments to deliver to {@link #revisionAtQuery()}
	 * 
	 * @see #revisionAtQuery() query for the returned arguments
	 */
	RevisionQueryArguments revisionAtArguments(long date) {
		RevisionQueryArguments arguments = revisionArgs();
		arguments.setRequestedBranch(kb.getTrunk());
		arguments.setArguments(date);
		return arguments;
	}

	/**
	 * query to get all links which have a specific object as source.
	 * 
	 * @see #refereeQueryArguments(KnowledgeItem) arguments for the returned query
	 * @see #destRefereeQuery()
	 */
	CompiledQuery<KnowledgeAssociation> sourceRefereeQuery() {
		return refereeQuery(BasicTypes.ASSOCIATION_TYPE_NAME, true, true);
	}

	/**
	 * query to get all links which have a specific object as destination.
	 * 
	 * @see #refereeQueryArguments(KnowledgeItem) arguments for the returned query
	 * @see #sourceRefereeQuery()
	 */
	CompiledQuery<KnowledgeAssociation> destRefereeQuery() {
		return refereeQuery(BasicTypes.ASSOCIATION_TYPE_NAME, true, false);
	}

	/**
	 * @param associationTypeName
	 *        Name of the requested association type.
	 *        
	 * @return A query to get all links of a specific type (without sub types) which have a specific object as source.
	 * 
	 * @see #refereeQueryArguments(KnowledgeItem) arguments for the returned query
	 * @see #incomingRefereeQuery(String)
	 */
	CompiledQuery<KnowledgeAssociation> outgoingRefereeQuery(String associationTypeName) {
		return refereeQuery(associationTypeName, false, true);
	}
	
	/**
	 * @param associationTypeName
	 *        Name of the requested association type.
	 * 
	 * @return A query to get all links of a specific type (without sub types) which have a specific
	 *         object as destination.
	 * 
	 * @see #refereeQueryArguments(KnowledgeItem) arguments for the returned query
	 * @see #outgoingRefereeQuery(String)
	 */
	CompiledQuery<KnowledgeAssociation> incomingRefereeQuery(String associationTypeName) {
		return refereeQuery(associationTypeName, false, false);
	}
	
	/**
	 * Creates a query which searches for all associations of the given type which references an
	 * specific object.
	 * 
	 * @param associationTypeName
	 *        Name of the requested association type.
	 * @param includeSubType
	 *        Whether also sub types must be considered.
	 * @param outgoing
	 *        Determines which reference is searched: If <code>true</code> then the associations are
	 *        searched which have the given object as source, if <code>false</code> then all
	 *        associations are searched which have the given object as destination.
	 */
	private CompiledQuery<KnowledgeAssociation> refereeQuery(String associationTypeName, boolean includeSubType, boolean outgoing){
		Object key = TupleFactory.newTuple(associationTypeName, includeSubType, outgoing);
		CompiledQuery<KnowledgeAssociation> query = refereeQueries.get(key);
		if (query == null) {
			CompiledQuery<KnowledgeAssociation> newQuery = createRefereeQuery(associationTypeName, includeSubType, outgoing);
			query = MapUtil.putIfAbsent(refereeQueries, key, newQuery);
		}
		return query;
	}

	private CompiledQuery<KnowledgeAssociation> createRefereeQuery(String associationTypeName, boolean includeSubType,
			boolean outgoing) {
		MetaObject associationType = kb.lookupType(associationTypeName);
		MOReference referenceAttr;
		if (outgoing) {
			referenceAttr = DBKnowledgeAssociation.getSourceAttribute(associationType);
		} else {
			referenceAttr = DBKnowledgeAssociation.getDestinationAttribute(associationType);
		}
		Expression nameReference = reference(referenceAttr, ReferencePart.name);
		String parameterName = "_targetName";
		Expression nameCheck = eqBinary(nameReference, param(parameterName));
		List<ParameterDeclaration> params = params(paramDecl(REFERENCE_TYPE, parameterName));
		SimpleQuery<KnowledgeAssociation> query =
			SimpleQuery.queryUnresolved(KnowledgeAssociation.class, associationType, nameCheck);
		query.setBranchParam(BranchParam.single);
		if (includeSubType) {
			query.includeSubType();
		}
		query.includeSubType();
		query.setQueryParameters(params);
		return kb.compileSimpleQuery(query);
	}
	
	/**
	 * @param target
	 *        The {@link KnowledgeItem} to serve as base object for the query 
	 * 
	 * @return arguments to deliver to {@link #sourceRefereeQuery()}, {@link #destRefereeQuery()},
	 *         {@link #outgoingRefereeQuery(String)}, or {@link #incomingRefereeQuery(String)}.
	 * 
	 * @see #sourceRefereeQuery() query for the returned arguments
	 * @see #destRefereeQuery() query for the returned arguments
	 * @see #outgoingRefereeQuery(String) query for the returned arguments
	 * @see #incomingRefereeQuery(String) query for the returned arguments
	 */
	RevisionQueryArguments refereeQueryArguments(KnowledgeItem target) {
		ObjectKey identifier = target.tId();
		RevisionQueryArguments queryArguments = createArgumentWithBranch(requestedBranch(identifier));
		queryArguments.setArguments(identifier.getObjectName());
		queryArguments.setRequestedRevision(identifier.getHistoryContext());
		return queryArguments;
	}

	private Branch requestedBranch(ObjectKey identifier) {
		return kb.getBranch(identifier.getBranchContext());
	}

	/**
	 * @param targetType
	 *        the type of the target object later filled into the query.
	 * @param policy
	 *        the policy a reference must have to be included in the search, or <code>null</code>
	 *        iff all references should be included in the search.
	 * @param multipleTargets
	 *        whether the query shall find items that point to a specific target (<code>false</code>
	 *        ) or to a target that is contained in a set of elements (<code>true</code>).
	 * @return queries that finds all items which refer via an reference with the given policy to a
	 *         specific object or (if multiple) whose reference is contained in a given set of
	 *         objects. The queries are indexed by the concrete type of a referrer.
	 * 
	 * @see #anyRefereeArguments(Revision, KnowledgeItem) arguments for the returned query
	 */
	<T> Map<MetaObject, CompiledQuery<T>> anyRefereeQuery(MetaObject targetType, DeletionPolicy policy,
			Boolean multipleTargets, Class<T> expectedType) {
		Object key = TupleFactory.newTuple(targetType, policy, multipleTargets, expectedType);
		Object cachedQuery = anyReferee.get(key);
		if (cachedQuery == null) {
			Map<MetaObject, CompiledQuery<T>> newQueries =
				createAnyRefereeQuery(targetType, policy, multipleTargets, expectedType);
			cachedQuery = MapUtil.putIfAbsent(anyReferee, key, newQueries);
		}
		@SuppressWarnings("unchecked")
		Map<MetaObject, CompiledQuery<T>> castedQuery = (Map<MetaObject, CompiledQuery<T>>) cachedQuery;
		return castedQuery;
	}

	private <T> Map<MetaObject, CompiledQuery<T>> createAnyRefereeQuery(MetaObject targetType, DeletionPolicy policy,
			Boolean multipleTargets, Class<T> expectedType) {
		Map<MOClass, Collection<MOReferenceInternal>> typesWithReferenceAttribute =
			kb.moRepository.getTypesWithReferenceAttribute();
		return createMonomorphicQueriesForTypeAndAttribute(targetType, policy, typesWithReferenceAttribute,
			multipleTargets, expectedType);
	}

	/**
	 * Creates one query for each type. The query for the type is the concatenation of one query for
	 * each reference attribute of the type.
	 * 
	 * @see #createMonomorphicQueriesForType(MetaObject, DeletionPolicy, Map)
	 */
	private <T> Map<MetaObject, CompiledQuery<T>> createMonomorphicQueriesForTypeAndAttribute(MetaObject targetType,
			DeletionPolicy policy, Map<MOClass, Collection<MOReferenceInternal>> typesWithReferenceAttribute,
			Boolean multipleTargets, Class<T> expectedType) {
		Map<MetaObject, CompiledQuery<T>> result = new HashMap<>();
		String itemParam = "_item";
		for (Entry<MOClass, Collection<MOReferenceInternal>> entry : typesWithReferenceAttribute.entrySet()) {
			List<CompiledQuery<T>> queries = new ArrayList<>();
			MOClass refererType = entry.getKey();
			for (MOReferenceInternal reference : entry.getValue()) {
				Expression searchExpression;
				if (policy == null || reference.getDeletionPolicy() == policy) {
					KIReferenceStorage storage = reference.getStorage();
					if (multipleTargets.booleanValue()) {
						searchExpression =
							storage.getReferenceInSetExpression(reference, refererType, targetType, itemParam);
					} else {
						searchExpression =
							storage.getRefererExpression(reference, refererType, targetType, itemParam);
					}
				} else {
					continue;
				}
				if (isLiteralFalse(searchExpression)) {
					continue;
				}
				MetaObject paramType;
				if (multipleTargets.booleanValue()) {
					paramType = MOCollectionImpl.createCollectionType(targetType);
				} else {
					paramType = targetType;
				}
				List<ParameterDeclaration> queryParameters = params(paramDecl(paramType, itemParam));
				SimpleQuery<T> simpleQuery =
					SimpleQuery.queryUnresolved(expectedType, refererType, searchExpression);
				// Referencing object can live everywhere
				simpleQuery.setBranchParam(BranchParam.all);
				simpleQuery.setQueryParameters(queryParameters);
				CompiledQuery<T> compiledQuery = kb.compileSimpleQuery(simpleQuery);
				queries.add(compiledQuery);
			}
			switch (queries.size()) {
				case 0:
					break;
				case 1:
					result.put(refererType, queries.get(0));
					break;
				default:
					result.put(refererType, new ConcatenatedCompiledQuery<>(kb.getConnectionPool(), queries));
					break;
			}
		}
		switch (result.size()) {
			case 0:
				result = Collections.emptyMap();
				break;
			case 1:
				Entry<MetaObject, CompiledQuery<T>> singleEntry = result.entrySet().iterator().next();
				result = Collections.singletonMap(singleEntry.getKey(), singleEntry.getValue());
				break;
		}
		return result;
	}

	/**
	 * Create one query for each type which checks all References for that type. This results in
	 * OR-SQL's which can not be handled by MySQL (5.1) with good performance (wrong indexes are
	 * used).
	 * 
	 * @see #createMonomorphicQueriesForTypeAndAttribute(MetaObject, DeletionPolicy, Map, Boolean, Class)
	 */
	@SuppressWarnings("unused")
	private List<CompiledQuery<KnowledgeItem>> createMonomorphicQueriesForType(MetaObject targetType,
			DeletionPolicy policy, Map<MOClass, Collection<MOReferenceInternal>> typesWithReferenceAttribute) {
		List<CompiledQuery<KnowledgeItem>> queries = new ArrayList<>();
		for (Entry<MOClass, Collection<MOReferenceInternal>> entry : typesWithReferenceAttribute.entrySet()) {
			String itemParam = "_item";
			Expression searchExpression = literal(false);
			for (MOReferenceInternal reference : entry.getValue()) {
				if (policy == null || reference.getDeletionPolicy() == policy) {
					Expression matchReference =
						reference.getStorage().getRefererExpression(reference, entry.getKey(), targetType, itemParam);
					searchExpression = or(searchExpression, matchReference);
				}
			}
			if (isLiteralFalse(searchExpression)) {
				continue;
			}

			List<ParameterDeclaration> queryParameters = params(paramDecl(targetType, itemParam));
			SimpleQuery<KnowledgeItem> simpleQuery =
				SimpleQuery.queryUnresolved(KnowledgeItem.class, entry.getKey(), searchExpression);
			// Referencing object can live everywhere
			simpleQuery.setBranchParam(BranchParam.all);
			simpleQuery.setQueryParameters(queryParameters);
			CompiledQuery<KnowledgeItem> compiledQuery = kb.compileSimpleQuery(simpleQuery);
			queries.add(compiledQuery);
		}
		return queries;
	}

	/**
	 * @param requestedRevision
	 *        the revision to search in
	 * @param target
	 *        the object to which a reference must point
	 * 
	 * @return arguments to deliver to
	 *         {@link #anyRefereeQuery(MetaObject, DeletionPolicy, Boolean, Class)}
	 * 
	 * @see #anyRefereeQuery(MetaObject, DeletionPolicy, Boolean, Class) query for the returned
	 *      arguments with <code>false</code> for multiple targets.
	 */
	RevisionQueryArguments anyRefereeArguments(Revision requestedRevision, KnowledgeItem target) {
		RevisionQueryArguments revisionArgs = revisionArgs();
		revisionArgs.setArguments(target);
		revisionArgs.setRequestedRevision(requestedRevision.getCommitNumber());
		return revisionArgs;
	}

	/**
	 * @param requestedRevision
	 *        the revision to search in
	 * @param targets
	 *        Only references that point to an object in the given targets will be found.
	 * 
	 * @return arguments to deliver to
	 *         {@link #anyRefereeQuery(MetaObject, DeletionPolicy, Boolean, Class)}
	 * 
	 * @see #anyRefereeQuery(MetaObject, DeletionPolicy, Boolean, Class) query for the returned
	 *      arguments with <code>true</code> for multiple targets.
	 */
	RevisionQueryArguments anyRefereeArguments(Revision requestedRevision, Iterable<? extends KnowledgeItem> targets) {
		RevisionQueryArguments revisionArgs = revisionArgs();
		revisionArgs.setArguments(targets);
		revisionArgs.setRequestedRevision(requestedRevision.getCommitNumber());
		return revisionArgs;
	}

	/**
	 * <b>Note:</b> method is not thread-safe. Therefore it must be synchronized externally.
	 * 
	 * @param type
	 *        the type to create a refetch query for
	 * 
	 * @return a query that fetches all object of the given type which have a special id. The id's
	 *         of the object to fetch are given in
	 *         {@link #refetchChunkArguments(Branch, long, List)}
	 * 
	 * @see #refetchChunkArguments(Branch, long, List) arguments for the returned query
	 */
	CompiledQuery<KnowledgeItem> refetchChunkQuery(MOKnowledgeItemImpl type) {
		CompiledQuery<KnowledgeItem> query = refetchChunk.get(type);
		if (query == null) {
			query = createRefetchChunkQuery(type);
			refetchChunk.put(type, query);
		}
		return query;
	}

	private CompiledQuery<KnowledgeItem> createRefetchChunkQuery(MOKnowledgeItemImpl type) {
		String idParamName = "ids";
		ParameterDeclaration paramDecl = paramDecl(MOCollectionImpl.createListType(REFERENCE_TYPE), idParamName);
		Expression search =
			inSet(attribute(BasicTypes.ITEM_TYPE_NAME, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME), setParam(idParamName));
		RevisionQuery<KnowledgeItem> query =
			queryUnresolved(Collections.singletonList(paramDecl),
				filter(allOfTyped(type), search),
				NO_ORDER, KnowledgeItem.class).setFullLoad();
		return kb.compileQuery(query);
	}

	/**
	 * @param requestedBranch
	 *        the branch on which the elements to fetch live
	 * @param updateRevision
	 *        The revision which serves as data revision for the update.
	 * @param chunkIds
	 *        the ids of the elements to fetch
	 * @return arguments to deliver to {@link #refetchChunkQuery(MOKnowledgeItemImpl)}
	 * 
	 * @see #refetchChunkQuery(MOKnowledgeItemImpl) query for the returned arguments
	 */
	RevisionQueryArguments refetchChunkArguments(Branch requestedBranch, long updateRevision, List<TLID> chunkIds) {
		RevisionQueryArguments queryArguments = createArgumentWithBranch(requestedBranch);
		queryArguments.setDataRevision(updateRevision);
		queryArguments.setArguments(chunkIds);
		return queryArguments;
	}

	/**
	 * @param type
	 *        type to get elements for
	 * 
	 * @return query to get all elements of a specific type on a given branch.
	 * 
	 * @see #allOfTypeArguments(Branch) arguments for the returned query
	 */
	CompiledQuery<?> allOfTypeQuery(MOKnowledgeItem type) {
		CompiledQuery<?> query = allOfType.get(type);
		if (query == null) {
			query = MapUtil.putIfAbsent(allOfType, type, createAllOfTypeQuery(type));
		}
		return query;
	}

	private CompiledQuery<?> createAllOfTypeQuery(MOKnowledgeItem type) {
		return kb.compileSimpleQuery(SimpleQuery.queryUnresolved(KnowledgeItem.class, type, literal(Boolean.TRUE)));
	}

	/**
	 * @param requestedBranch
	 *        the branch of the searched elements
	 * 
	 * @return arguments to deliver to {@link #allOfTypeQuery(MOKnowledgeItem)}
	 * 
	 * @see #allOfTypeQuery(MOKnowledgeItem) query for the returned arguments
	 */
	RevisionQueryArguments allOfTypeArguments(Branch requestedBranch) {
		return createArgumentWithBranch(requestedBranch);
	}

	/**
	 * a query that fetches all branch switch elements of the branch with a specific ID
	 * 
	 * @see #dataBranchByTypeArguments(Long) arguments for the returned query
	 */
	CompiledQuery<KnowledgeItemInternal> dataBranchByTypeQuery() {
		return dataBranchByType;
	}

	private CompiledQuery<KnowledgeItemInternal> createDataBranchByTypeQuery() {
		String branchIdParamName = "_branchId";
		MOKnowledgeItem branchSwitchType = kb.getBranchSwitchType();
		MOAttribute linkBranchAttr = BranchSupport.getLinkBranchAttr(branchSwitchType).getAttribute();
		Expression branchEquality = eqBinary(attributeTyped(linkBranchAttr), param(branchIdParamName));
		SetExpression search = filter(allOf(branchSwitchType), branchEquality);

		/* must use all as if set to single the context branch is used. This leads to an error when
		 * trunk is initialised */
		BranchParam branchParam = BranchParam.all;
		List<ParameterDeclaration> queryParameters =
			params(paramDecl(linkBranchAttr.getMetaObject(), branchIdParamName));
		RevisionQuery<KnowledgeItemInternal> query =
			queryUnresolved(branchParam, RangeParam.complete, queryParameters, search, NO_ORDER, KnowledgeItemInternal.class);
		return kb.compileQuery(query);
	}

	/**
	 * @param branchId
	 *        the id of the branch for which the branch switch elements are requested
	 * 
	 * @return arguments to deliver to {@link #dataBranchByTypeQuery()}
	 * 
	 * @see #dataBranchByTypeQuery() query for the returned arguments
	 */
	RevisionQueryArguments dataBranchByTypeArguments(Long branchId) {
		return revisionArgs().setArguments(branchId);
	}

	/**
	 * a query that fetches all branch switch elements for a specific branch that states
	 *         that also the data branch of the represented type is the specific branch
	 * 
	 * @see #branchedTypesArguments(Long) arguments for the returned query
	 */
	CompiledQuery<KnowledgeItemInternal> branchedTypesQuery() {
		return branchedTypes;
	}

	private CompiledQuery<KnowledgeItemInternal> createBranchedTypesQuery() {
		String branchIdParamName = "_branchId";
		MOKnowledgeItem branchSwitchType = kb.getBranchSwitchType();
		MOAttribute linkBranch = BranchSupport.getLinkBranchAttr(branchSwitchType).getAttribute();
		MOAttribute linkDataBranch = BranchSupport.getLinkDataBranchAttr(branchSwitchType).getAttribute();
		MetaObject branchDataType = linkBranch.getMetaObject();
		assert branchDataType == linkDataBranch.getMetaObject() : "Expected both attribute have same data type.";
		Expression attributeCheck =
			and(
				eqBinary(attributeTyped(linkBranch), param(branchIdParamName)),
				eqBinary(attributeTyped(linkDataBranch), param(branchIdParamName)));
		SetExpression search = filter(allOf(branchSwitchType), attributeCheck);

		/* must use all as if set to single the context branch is used. This leads to error when
		 * trunk is initialised */
		BranchParam branchParam = BranchParam.all;
		List<ParameterDeclaration> queryParameters = params(paramDecl(branchDataType, branchIdParamName));
		RevisionQuery<KnowledgeItemInternal> query =
			queryUnresolved(branchParam, RangeParam.complete, queryParameters, search, NO_ORDER, KnowledgeItemInternal.class);
		return kb.compileQuery(query);
	}

	/**
	 * @param branchId
	 *        id of the branch for which the branched types are requested.
	 * 
	 * @return arguments to deliver to {@link #branchedTypesQuery()}
	 * 
	 * @see #branchedTypesQuery() query for the returned arguments
	 */
	RevisionQueryArguments branchedTypesArguments(Long branchId) {
		return revisionArgs().setArguments(branchId);
	}

	/**
	 * creates {@link RevisionQueryArguments} containing the given branch
	 * 
	 * @param requestedBranch
	 *        the single branch returned by {@link QueryArguments#getRequestedBranches()}
	 */
	static RevisionQueryArguments createArgumentWithBranch(Branch requestedBranch) {
		RevisionQueryArguments queryArguments = revisionArgs();
		queryArguments.setRequestedBranch(requestedBranch);
		return queryArguments;
	}
	
	/** 
	 * Returns a query that searches for all associations of a given type between two given objects.
	 * 
	 * @param associationTypeName Name of the association to search. 
	 * 
	 * @see #associationsBetweenArguments(KnowledgeItem, KnowledgeItem) arguments for the returned query
	 */
	CompiledQuery<KnowledgeAssociation> associationsBetweenQuery(String associationTypeName){
		Object key = associationTypeName;
		CompiledQuery<KnowledgeAssociation> query = associationsBetween.get(key);
		if (query == null) {
			CompiledQuery<KnowledgeAssociation> newQuery = createAssociationsBetweenQuery(associationTypeName);
			query = MapUtil.putIfAbsent(associationsBetween, key, newQuery);
		}
		return query;
	}

	private CompiledQuery<KnowledgeAssociation> createAssociationsBetweenQuery(String associationTypeName) {
		MetaObject associationType = kb.lookupType(associationTypeName);
		
		MOReference source = DBKnowledgeAssociation.getSourceAttribute(associationType);
		Expression sourceNameReference = reference(source, ReferencePart.name);
		String sourceParameterName = "_sourceName";
		Expression sourceNameCheck = eqBinary(sourceNameReference, param(sourceParameterName));

		MOReference dest = DBKnowledgeAssociation.getDestinationAttribute(associationType);
		Expression destNameReference = reference(dest, ReferencePart.name);
		String destParameterName = "_destName";
		Expression destNameCheck = eqBinary(destNameReference, param(destParameterName));
		Expression search = and(sourceNameCheck, destNameCheck);
		
		ParameterDeclaration sourceParamDecl = paramDecl(REFERENCE_TYPE, sourceParameterName);
		ParameterDeclaration destParamDecl = paramDecl(REFERENCE_TYPE, destParameterName);
		List<ParameterDeclaration> params = params(sourceParamDecl, destParamDecl);

		SimpleQuery<KnowledgeAssociation> query =
			SimpleQuery.queryUnresolved(KnowledgeAssociation.class, associationType, search);
		query.setBranchParam(BranchParam.single);
		query.setQueryParameters(params);
		return kb.compileSimpleQuery(query);
	}

	/**
	 * @param source
	 *        {@link KnowledgeItem} that serves as source object of the query.
	 * @param destination
	 *        {@link KnowledgeItem} that serves as destination object of the query.
	 * 
	 * @return Arguments to deliver to {@link #associationsBetweenQuery(String)}
	 * 
	 * @see #associationsBetweenQuery(String) query for the returned arguments.
	 */
	RevisionQueryArguments associationsBetweenArguments(KnowledgeItem source, KnowledgeItem destination) {
		ObjectKey sourceIdentifier = source.tId();
		RevisionQueryArguments queryArguments = createArgumentWithBranch(requestedBranch(sourceIdentifier));
		queryArguments.setArguments(sourceIdentifier.getObjectName(), destination.tId().getObjectName());
		queryArguments.setRequestedRevision(sourceIdentifier.getHistoryContext());
		return queryArguments;
	}

	/**
	 * query to get all items at a given revision which have an end with a special type.
	 * 
	 * @see #branchCrossingValuesArguments(Branch, long, Collection) arguments for the returned
	 *      query
	 */
	CompiledQuery<KnowledgeItem> branchCrossingValuesQuery(MetaObject type) {
		Object key = type;
		CompiledQuery<KnowledgeItem> compiledQuery = branchCrossingValues.get(key);
		if (compiledQuery == null) {
			CompiledQuery<KnowledgeItem> newQuery = createBranchCrossingValuesQuery(type);
			compiledQuery = MapUtil.putIfAbsent(branchCrossingValues, key, newQuery);
		}
		return compiledQuery;
	}

	private CompiledQuery<KnowledgeItem> createBranchCrossingValuesQuery(MetaObject type) {
		List<? extends MOReference> allReferences = MetaObjectUtils.getReferences(type);
		if (allReferences.isEmpty()) {
			return EmptyCompiledQuery.getInstance();
		}

		String branchedObjectTypeNamesParam = "_branchedTypes";
		Expression allEndBranched = literal(Boolean.TRUE);
		Expression allEndUnranched = literal(Boolean.TRUE);
		for (MOReference reference : allReferences) {
			if (reference.isBranchGlobal()) {
				// branch global references don't care
				continue;
			}
			Expression endIsBranched;
			Expression endIsUnbranched;
			switch (reference.getHistoryType()) {
				case HISTORIC:
					// historic values are not moved by branch operation, so values doesn't matter.
					continue;
				case CURRENT: {
					{
						Expression typeReference = typeOfReference(reference);
						Expression hasBranchedType = inSet(typeReference, setParam(branchedObjectTypeNamesParam));
						Expression isNull = isNull(referenceTyped(reference, ReferencePart.name));
						endIsBranched = or(isNull, hasBranchedType);
					}
					{
						Expression typeReference = typeOfReference(reference);
						Expression hasBranchedType = inSet(typeReference, setParam(branchedObjectTypeNamesParam));
						Expression isNull = isNull(referenceTyped(reference, ReferencePart.name));
						endIsUnbranched = or(isNull, not(hasBranchedType));
					}
					break;
				}
				case MIXED: {
					{
						Expression currentValue = isCurrent(referenceTyped(reference));
						Expression typeReference = typeOfReference(reference);
						Expression correctType = inSet(typeReference, setParam(branchedObjectTypeNamesParam));
						Expression isNull = isNull(referenceTyped(reference, ReferencePart.name));
						endIsBranched = or(isNull, or(correctType, not(currentValue)));
					}
					{
						Expression currentValue = isCurrent(referenceTyped(reference));
						Expression typeReference = typeOfReference(reference);
						Expression correctType = inSet(typeReference, setParam(branchedObjectTypeNamesParam));
						Expression isNull = isNull(referenceTyped(reference, ReferencePart.name));
						endIsUnbranched = or(isNull, or(not(correctType), not(currentValue)));
					}
					break;
				}
				default:
					throw HistoryType.noSuchType(reference.getHistoryType());
			}
			allEndBranched = and(allEndBranched, endIsBranched);
			allEndUnranched = and(allEndUnranched, endIsUnbranched);
		}
		if (ExpressionFactory.isLiteralTrue(allEndBranched)) {
			assert ExpressionFactory.isLiteralTrue(allEndUnranched) : "Variables are modified synchronous.";
			return EmptyCompiledQuery.getInstance();
		}

		Expression notAllEndsBranched = not(allEndBranched);
		Expression notAllEndsUnbranched = not(allEndUnranched);

		MOCollection parameterType = MOCollectionImpl.createCollectionType(MOPrimitive.STRING);
		SetExpression searchExpr = filter(allOf(type.getName()), and(notAllEndsBranched, notAllEndsUnbranched));
		List<ParameterDeclaration> queryParameters = params(paramDecl(parameterType, branchedObjectTypeNamesParam));
		RevisionQuery<KnowledgeItem> query =
			queryUnresolved(BranchParam.single, RangeParam.first, queryParameters, searchExpr, NO_ORDER, KnowledgeItem.class);
		return kb.compileQuery(query);
	}

	/**
	 * @param requestedBranch
	 *        the branch that a link must have
	 * @param requestedRevision
	 *        the revision to search links in
	 * @param branchedTypeNames
	 *        the names of the types an end must have
	 * 
	 * @return arguments to deliver to {@link #branchCrossingValuesQuery(MetaObject)}
	 * 
	 * @see #branchCrossingValuesQuery(MetaObject) query for the returned arguments
	 */
	RevisionQueryArguments branchCrossingValuesArguments(Branch requestedBranch, long requestedRevision,
			Collection<String> branchedTypeNames) {
		RevisionQueryArguments arguments = revisionArgs();
		arguments.setArguments(branchedTypeNames);
		arguments.setRequestedBranch(requestedBranch);
		arguments.setRequestedRevision(requestedRevision);
		return arguments;
	}

	/**
	 * Creates an {@link Expression} that matches all elements of the given type that can be
	 * branched.
	 * 
	 * @param type
	 *        Name of the type to branch.
	 * @param branchedObjectTypeNames
	 *        Names of the types that are branched.
	 */
	SQLExpression createBranchFilter(MetaObject type, Collection<String> branchedObjectTypeNames) {
		List<? extends MOReference> allReferences = MetaObjectUtils.getReferences(type);
		if (allReferences.isEmpty()) {
			return literalTrueLogical();
		}

		SQLExpression allEndsBranched = literalTrueLogical();
		for (MOReference reference : allReferences) {
			if (reference.isBranchGlobal()) {
				// branch global references don't care
				continue;
			}
			SQLExpression endIsBranched;
			switch (reference.getHistoryType()) {
				case HISTORIC:
					// historic values are not moved by branch operation, so values doesn't matter.
					continue;
				case CURRENT: {
					SQLExpression typeReference = sqlTypeOfReference(reference);
					DBType referenceDBType = IdentifierTypes.TYPE_REFERENCE_MO_TYPE.getDefaultSQLType();
					SQLExpression hasBranchedType = inSet(typeReference, branchedObjectTypeNames, referenceDBType);
					endIsBranched = hasBranchedType;
					break;
				}
				case MIXED: {
					SQLExpression revisionReference =
						column(NO_TABLE_ALIAS, reference.getColumn(ReferencePart.revision), NOT_NULL);
					SQLExpression currentValue = eq(revisionReference, literalLong(Revision.CURRENT_REV));
					SQLExpression typeReference = sqlTypeOfReference(reference);
					DBType referenceDBType = IdentifierTypes.TYPE_REFERENCE_MO_TYPE.getDefaultSQLType();
					SQLExpression correctType = inSet(typeReference, branchedObjectTypeNames, referenceDBType);
					endIsBranched = or(correctType, not(currentValue));
					break;
				}
				default:
					throw HistoryType.noSuchType(reference.getHistoryType());
			}
			SQLExpression isNull =
				isNull(column(NO_TABLE_ALIAS, reference.getColumn(ReferencePart.name), NOT_NULL));
			allEndsBranched = and(allEndsBranched, or(isNull, endIsBranched));
		}
		return allEndsBranched;
	}

	private SQLExpression sqlTypeOfReference(MOReference reference) {
		SQLExpression typeExpression;
		if (reference.isMonomorphic()) {
			typeExpression = literalString(reference.getMetaObject().getName());
		} else {
			typeExpression = column(NO_TABLE_ALIAS, reference.getColumn(ReferencePart.type), NOT_NULL);
		}
		return typeExpression;
	}

	private Expression typeOfReference(MOReference reference) {
		Expression typeExpression;
		if (reference.isMonomorphic()) {
			typeExpression = literal(reference.getMetaObject().getName());
		} else {
			typeExpression = referenceTyped(reference, ReferencePart.type);
		}
		return typeExpression;
	}

	/**
	 * query to get all items at a given revision which have an end with a special type.
	 * 
	 * @see #branchCrossingRefererenceArguments(Branch, long, Collection) arguments for the returned
	 *      query
	 */
	CompiledQuery<KnowledgeItem> branchCrossingReferenceQuery(MetaObject type) {
		Object key = type;
		CompiledQuery<KnowledgeItem> compiledQuery = branchCrossingReferences.get(key);
		if (compiledQuery == null) {
			CompiledQuery<KnowledgeItem> newQuery = createBranchCrossingReferenceQuery(type);
			compiledQuery = MapUtil.putIfAbsent(branchCrossingReferences, key, newQuery);
		}
		return compiledQuery;
	}

	private CompiledQuery<KnowledgeItem> createBranchCrossingReferenceQuery(MetaObject type) {
		List<? extends MOReference> allReferences = MetaObjectUtils.getReferences(type);
		if (allReferences.isEmpty()) {
			return EmptyCompiledQuery.getInstance();
		}

		Expression hasCorrectEnd = literal(Boolean.TRUE);
		String branchedObjectTypeNamesParam = "_branchedTypes";
		for (MOReference reference : allReferences) {
			if (reference.isBranchGlobal()) {
				// branch global references don't care
				continue;
			}
			switch (reference.getHistoryType()) {
				case HISTORIC:
					// historic values are not moved by branch operation, so values doesn't matter.
					continue;
				case CURRENT: {
					Expression isNull = isNull(referenceTyped(reference, ReferencePart.name));
					Expression typeReference = typeOfReference(reference);
					Expression correctType = inSet(typeReference, setParam(branchedObjectTypeNamesParam));
					hasCorrectEnd = and(hasCorrectEnd, or(isNull, correctType));
					break;
				}
				case MIXED: {
					Expression isNull = isNull(referenceTyped(reference, ReferencePart.name));
					Expression currentValue = isCurrent(referenceTyped(reference));
					Expression typeReference = typeOfReference(reference);
					Expression correctType = inSet(typeReference, setParam(branchedObjectTypeNamesParam));
					hasCorrectEnd = and(hasCorrectEnd, or(isNull, or(correctType, not(currentValue))));
					break;
				}
			}
		}
		if (ExpressionFactory.isLiteralTrue(hasCorrectEnd)) {
			return EmptyCompiledQuery.getInstance();
		}

		MOCollection parameterType = MOCollectionImpl.createCollectionType(MOPrimitive.STRING);
		SetExpression searchExpr = filter(allOf(type.getName()), not(hasCorrectEnd));
		List<ParameterDeclaration> queryParameters = params(paramDecl(parameterType, branchedObjectTypeNamesParam));
		RevisionQuery<KnowledgeItem> query =
			queryUnresolved(BranchParam.single, RangeParam.first, queryParameters, searchExpr, NO_ORDER, KnowledgeItem.class);
		return kb.compileQuery(query);
	}

	/**
	 * @param requestedBranch
	 *        the branch that a link must have
	 * @param requestedRevision
	 *        the revision to search links in
	 * @param branchedTypeNames
	 *        the names of the types an end must have
	 * 
	 * @return arguments to deliver to {@link #branchCrossingReferenceQuery(MetaObject)}
	 * 
	 * @see #branchCrossingReferenceQuery(MetaObject) query for the returned arguments
	 */
	RevisionQueryArguments branchCrossingRefererenceArguments(Branch requestedBranch, long requestedRevision,
			Collection<String> branchedTypeNames) {
		RevisionQueryArguments arguments = revisionArgs();
		arguments.setArguments(branchedTypeNames);
		arguments.setRequestedBranch(requestedBranch);
		arguments.setRequestedRevision(requestedRevision);
		return arguments;
	}

}
