/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.search.InternalExpressionFactory.*;

import java.util.List;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQuery.LoadStrategy;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.TypedSetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.model.TLObject;

/**
 * The class {@link SimpleQuery} holds informations to compile a Query which also searches in the
 * current transaction.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleQuery<E> {

	/**
	 * Creates a {@link SimpleQuery} with the given arguments. Other properties are default.
	 * 
	 * <p>
	 * The found elements are resolved to their wrapper.
	 * </p>
	 * 
	 * @param <E>
	 *        type of the implementation type of the results of the query with this argument
	 * @param implClass
	 *        wrapper type of the results of the query with this argument
	 * @param type
	 *        persistent type to search in
	 * @param search
	 *        the Expression to execute
	 * 
	 * @return argument for {@link DBKnowledgeBase#compileSimpleQuery(SimpleQuery)}
	 * 
	 * @see SimpleQuery#queryUnresolved(Class, MetaObject, Expression)
	 */
	public static <E extends TLObject> SimpleQuery<E> queryResolved(Class<E> implClass, MetaObject type,
			Expression search) {
		return queryResolved(implClass, type, search, RangeParam.complete);
	}

	/**
	 * Creates a {@link SimpleQuery} with the given arguments. Other properties are default.
	 * 
	 * <p>
	 * The found elements are resolved to their wrapper.
	 * </p>
	 * 
	 * @param <E>
	 *        type of the implementation type of the results of the query with this argument
	 * @param implClass
	 *        wrapper type of the results of the query with this argument
	 * @param type
	 *        persistent type to search in
	 * @param search
	 *        the Expression to execute
	 * @param range
	 *        What part of the result is of interest.
	 * 
	 * @return argument for {@link DBKnowledgeBase#compileSimpleQuery(SimpleQuery)}
	 * 
	 * @see SimpleQuery#queryUnresolved(Class, MetaObject, Expression, RangeParam)
	 */
	public static <E extends TLObject> SimpleQuery<E> queryResolved(Class<E> implClass, MetaObject type,
			Expression search, RangeParam range) {
		return new SimpleQuery<>(implClass, type, search, range, true);
	}

	/**
	 * Creates a {@link SimpleQuery} with the given arguments. Other properties are default.
	 * 
	 * <p>
	 * The found elements are not resolved to their wrapper.
	 * </p>
	 * 
	 * @param <E>
	 *        type of the implementation type of the results of the query with this argument
	 * @param implClass
	 *        implementation type of the results of the query with this argument
	 * @param type
	 *        persistent type to search in
	 * @param search
	 *        the Expression to execute
	 * 
	 * @return argument for {@link DBKnowledgeBase#compileSimpleQuery(SimpleQuery)}
	 * 
	 * @see SimpleQuery#queryResolved(Class, MetaObject, Expression)
	 */
	public static <E> SimpleQuery<E> queryUnresolved(Class<E> implClass, MetaObject type, Expression search) {
		return queryUnresolved(implClass, type, search, RangeParam.complete);
	}

	/**
	 * Creates a {@link SimpleQuery} with the given arguments. Other properties are default.
	 * 
	 * <p>
	 * The found elements are not resolved to their wrapper.
	 * </p>
	 * 
	 * @param <E>
	 *        type of the implementation type of the results of the query with this argument
	 * @param implClass
	 *        implementation type of the results of the query with this argument
	 * @param type
	 *        persistent type to search in
	 * @param search
	 *        the Expression to execute
	 * @param range
	 *        What part of the result is of interest.
	 * 
	 * @return argument for {@link DBKnowledgeBase#compileSimpleQuery(SimpleQuery)}
	 * 
	 * @see SimpleQuery#queryResolved(Class, MetaObject, Expression, RangeParam)
	 */
	public static <E> SimpleQuery<E> queryUnresolved(Class<E> implClass, MetaObject type, Expression search,
			RangeParam range) {
		return new SimpleQuery<>(implClass, type, search, range, false);
	}

	/**
	 * Creates a {@link SimpleQuery} with the given arguments. Other properties are default.
	 * 
	 * <p>
	 * The found elements are not resolved to their wrapper.
	 * </p>
	 * 
	 * @param type
	 *        persistent type to search in
	 * @param search
	 *        the Expression to execute
	 * 
	 * @return argument for {@link DBKnowledgeBase#compileSimpleQuery(SimpleQuery)}
	 */
	public static SimpleQuery<KnowledgeObject> queryUnresolved(MetaObject type, Expression search) {
		return queryUnresolved(KnowledgeObject.class, type, search);
	}

	final MOClass _type;

	final Expression _search;

	final Class<E> _implClass;

	final boolean _resolve;

	List<ParameterDeclaration> _queryParameters = ExpressionFactory.NO_QUERY_PARAMETERS;

	boolean _includeSubType = false;

	private BranchParam _branchParam = BranchParam.single;

	private RangeParam _rangeParam;

	private SimpleQuery(Class<E> implClass, MetaObject type, Expression search, RangeParam range, boolean resolve) {
		_implClass = implClass;
		_search = search;
		_rangeParam = range;
		_resolve = resolve;
		if (!(type instanceof MOClass)) {
			throw new IllegalArgumentException("Type must be a " + MOClass.class.getName());
		}
		_type = (MOClass) type;
	}

	/**
	 * @param queryParameters
	 *        sets the necessary parameters of the represented expression
	 * 
	 * @return a reference to this {@link SimpleQuery}
	 */
	public SimpleQuery<E> setQueryParameters(List<ParameterDeclaration> queryParameters) {
		_queryParameters = queryParameters;
		return this;
	}

	/**
	 * @param order
	 *        sets the necessary parameters of the represented expression
	 * 
	 * @return a reference to this {@link SimpleQuery}
	 */
	SimpleQuery<E> setOrder(Order order) {
		if (order != ExpressionFactory.NO_ORDER) {
			String error =
				"Order is currently not supported because the simple query also searches in current transaction. "
					+ "This is done by seaching the database and then adapting the result with the modified objects. "
					+ "But this update does not preserve order.";
			throw new UnsupportedOperationException(error);
		}
		return this;
	}

	/**
	 * Determines that the search includes all concrete subtypes of the specified type.
	 * 
	 * @return a reference to this {@link SimpleQuery}
	 */
	public SimpleQuery<E> includeSubType() {
		_includeSubType = true;
		return this;
	}

	/**
	 * @see RevisionQuery#getBranchParam()
	 */
	public BranchParam getBranchParam() {
		return _branchParam;
	}

	/**
	 * Sets the branch parameter for the search.
	 * 
	 * @return a reference to this {@link SimpleQuery}
	 */
	public SimpleQuery<E> setBranchParam(BranchParam branchParam) {
		_branchParam = branchParam;
		return this;
	}

	/**
	 * See {@link RevisionQuery#getRangeParam()}.
	 */
	public RangeParam getRangeParam() {
		return _rangeParam;
	}

	/**
	 * @see #getRangeParam()
	 */
	public SimpleQuery<E> setRangeParam(RangeParam rangeParam) {
		_rangeParam = rangeParam;
		return this;
	}

	RevisionQuery<E> toRevisionQuery(DBKnowledgeBase kb) {
		TypedSetExpression source;
		if (_includeSubType) {
			source = anyOfTyped(_type);
		} else {
			source = allOfTyped(_type);
		}
		SetExpression searchExpression = ExpressionFactory.filter(source, _search);
		RevisionQuery<E> query =
			newRevisionQuery(getBranchParam(), getRangeParam(), _queryParameters, searchExpression, NO_ORDER,
				_implClass, _resolve, LoadStrategy.DEFAULT);

		// make type analysis to have correct types during search in transaction.
		kb.typeAnalysis(query, new ExpressionCompileProtocol(new BufferingProtocol()));
		return query;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append('[');
		builder.append("type:").append(_type.getName());
		builder.append(", search:").append(_search);
		builder.append(", branchParam:").append(_branchParam);
		builder.append(", rangeParam:").append(_rangeParam);
		builder.append(']');
		return builder.toString();
	}

}
