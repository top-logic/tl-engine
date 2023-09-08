/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Function;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.MaxFunction;
import com.top_logic.knowledge.search.MinFunction;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.OrderSpec;
import com.top_logic.knowledge.search.OrderTuple;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.QueryVisitor;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.SumFunction;
import com.top_logic.knowledge.search.Union;

/**
 * Visitor that first descends into the parts of a {@link QueryPart} and then handles the result of
 * that descent.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DescendingQueryVisitor<RQ, RE extends RQ, RS extends RQ, RF extends RQ, RO extends RQ, A> extends DescendingExpressionVisitor<RE, RS, A> implements QueryVisitor<RQ, RE, RS, RF, RO, A> {

	/**
	 * Default constructor.
	 */
	protected DescendingQueryVisitor() {
		// Default constructor.
	}
	
	@Override
	public RS visitCrossProduct(CrossProduct expr, A arg) {
		List<RS> expressions = descendSets(expr, expr.getExpressions(), arg);
		return process(expr, expressions);
	}

	/**
	 * Processes the result of the descent within a {@link CrossProduct}
	 * 
	 * @param expr
	 *        the expression that was visited
	 * @param expressions
	 *        the results of the descent in {@link CrossProduct#getExpressions()}
	 * 
	 * @return final result of {@link #visitCrossProduct(CrossProduct, Object)}
	 */
	protected abstract RS process(CrossProduct expr, List<RS> expressions);

	@Override
	public RS visitFilter(Filter expr, A arg) {
		RS source = descendSet(expr, expr.getSource(), arg);
		RE filter = descendExpr(expr, expr.getFilter(), arg);
		return process(expr, source, filter);
	}

	/**
	 * Processes the result of the descent within a {@link Filter}
	 * 
	 * @param expr
	 *        the expression that was visited
	 * @param source
	 *        the results of the descent in {@link Filter#getSource()}
	 * @param filter
	 *        the results of the descent in {@link Filter#getFilter()}
	 * 
	 * @return final result of {@link #visitFilter(Filter, Object)}
	 */
	protected abstract RS process(Filter expr, RS source, RE filter);

	@Override
	public RS visitIntersection(Intersection expr, A arg) {
		RS left = descendSet(expr, expr.getLeftExpr(), arg);
		RS right = descendSet(expr, expr.getRightExpr(), arg);
		return process(expr, left, right);
	}

	/**
	 * Processes the result of the descent within an {@link Intersection}
	 * 
	 * @param expr
	 *        the expression that was visited
	 * @param left
	 *        the results of the descent in {@link Intersection#getLeftExpr()}
	 * @param right
	 *        the results of the descent in {@link Intersection#getRightExpr()}
	 * 
	 * @return final result of {@link #visitIntersection(Intersection, Object)}
	 */
	protected abstract RS process(Intersection expr, RS left, RS right);

	@Override
	public RS visitMapTo(MapTo expr, A arg) {
		RS source = descendSet(expr, expr.getSource(), arg);
		RE mapping = descendExpr(expr, expr.getMapping(), arg);
		return process(expr, source, mapping);
	}

	/**
	 * Processes the result of the descent within a {@link MapTo}
	 * 
	 * @param expr
	 *        the expression that was visited
	 * @param source
	 *        the results of the descent in {@link MapTo#getSource()}
	 * @param mapping
	 *        the results of the descent in {@link MapTo#getMapping()}
	 * 
	 * @return final result of {@link #visitMapTo(MapTo, Object)}
	 */
	protected abstract RS process(MapTo expr, RS source, RE mapping);

	@Override
	public RS visitPartition(Partition expr, A arg) {
		RS source = descendSet(expr, expr.getSource(), arg);
		RE equivalence = descendExpr(expr, expr.getEquivalence(), arg);
		RF representative = descendFun(expr, expr.getRepresentative(), arg);
		return process(expr, source, equivalence, representative);
	}

	/**
	 * Processes the result of the descent within a {@link Partition}
	 * 
	 * @param expr
	 *        the expression that was visited
	 * @param source
	 *        the results of the descent in {@link Partition#getSource()}
	 * @param equivalence
	 *        the results of the descent in {@link Partition#getEquivalence()}
	 * @param representative
	 *        the results of the descent in {@link Partition#getRepresentative()}
	 * 
	 * @return final result of {@link #visitPartition(Partition, Object)}
	 */
	protected abstract RS process(Partition expr, RS source, RE equivalence, RF representative);

	@Override
	public RS visitSubstraction(Substraction expr, A arg) {
		RS left = descendSet(expr, expr.getLeftExpr(), arg);
		RS right = descendSet(expr, expr.getRightExpr(), arg);
		return process(expr, left, right);
	}

	/**
	 * Processes the result of the descent within a {@link Substraction}
	 * 
	 * @param expr
	 *        the expression that was visited
	 * @param left
	 *        the results of the descent in {@link Substraction#getLeftExpr()}
	 * @param right
	 *        the results of the descent in {@link Substraction#getRightExpr()}
	 * 
	 * @return final result of {@link #visitSubstraction(Substraction, Object)}
	 */
	protected abstract RS process(Substraction expr, RS left, RS right);

	@Override
	public RS visitUnion(Union expr, A arg) {
		RS left = descendSet(expr, expr.getLeftExpr(), arg);
		RS right = descendSet(expr, expr.getRightExpr(), arg);
		return process(expr, left, right);
	}

	/**
	 * Processes the result of the descent within a {@link Union}
	 * 
	 * @param expr
	 *        the expression that was visited
	 * @param left
	 *        the results of the descent in {@link Union#getLeftExpr()}
	 * @param right
	 *        the results of the descent in {@link Union#getRightExpr()}
	 * 
	 * @return final result of {@link #visitUnion(Union, Object)}
	 */
	protected abstract RS process(Union expr, RS left, RS right);

	@Override
	public RF visitMax(MaxFunction expr, A arg) {
		RE expression = descendExpr(expr, expr.getExpr(), arg);
		return process(expr, expression);
	}

	/**
	 * Processes the result of the descent within a {@link MaxFunction}
	 * 
	 * @param orig
	 *        the function that was visited
	 * @param expression
	 *        the results of the descent in {@link MaxFunction#getExpr()}
	 * 
	 * @return final result of {@link #visitMax(MaxFunction, Object)}
	 */
	protected abstract RF process(MaxFunction orig, RE expression);

	@Override
	public RF visitMin(MinFunction expr, A arg) {
		RE expression = descendExpr(expr, expr.getExpr(), arg);
		return process(expr, expression);
	}

	/**
	 * Processes the result of the descent within a {@link MinFunction}
	 * 
	 * @param orig
	 *        the function that was visited
	 * @param expression
	 *        the results of the descent in {@link MinFunction#getExpr()}
	 * 
	 * @return final result of {@link #visitMin(MinFunction, Object)}
	 */
	protected abstract RF process(MinFunction orig, RE expression);

	@Override
	public RF visitSum(SumFunction expr, A arg) {
		RE expression = descendExpr(expr, expr.getExpr(), arg);
		return process(expr, expression);
	}

	/**
	 * Processes the result of the descent within a {@link SumFunction}
	 * 
	 * @param orig
	 *        the function that was visited
	 * @param expression
	 *        the results of the descent in {@link SumFunction#getExpr()}
	 * 
	 * @return final result of {@link #visitSum(SumFunction, Object)}
	 */
	protected abstract RF process(SumFunction orig, RE expression);

	@Override
	public RO visitOrderSpec(OrderSpec expr, A arg) {
		RE expression = descendExpr(expr, expr.getOrderExpr(), arg);
		return process(expr, expression);
	}

	/**
	 * Processes the result of the descent within an {@link OrderSpec}
	 * 
	 * @param expr
	 *        the {@link OrderSpec} that was visited
	 * @param expression
	 *        the results of the descent in {@link OrderSpec#getOrderExpr()}
	 * 
	 * @return final result of {@link #visitOrderSpec(OrderSpec, Object)}
	 */
	protected abstract RO process(OrderSpec expr, RE expression);

	@Override
	public RO visitOrderTuple(OrderTuple expr, A arg) {
		List<OrderSpec> expressions = descendOrders(expr, expr.getOrderSpecs(), arg);
		return process(expr, expressions);
	}

	/**
	 * Processes the result of the descent within an {@link OrderTuple}
	 * 
	 * @param orig
	 *        the {@link OrderTuple} that was visited
	 * @param expressions
	 *        the results of the descent in {@link OrderTuple#getOrderSpecs()}
	 * 
	 * @return final result of {@link #visitOrderTuple(OrderTuple, Object)}
	 */
	protected abstract RO process(OrderTuple orig, List<OrderSpec> expressions);

	/**
	 * Helper method to descend into any expression in the given list and adds the result to a
	 * resulting list.
	 * 
	 * @param parent
	 *        queryPart which defines the given list as content.
	 * @param expressions
	 *        the expression to descent into
	 * @param arg
	 *        the visit argument
	 * 
	 * @return list of the results of the decent
	 */
	protected List<RS> descendSets(QueryPart parent, List<SetExpression> expressions, A arg) {
		ArrayList<RS> result = new ArrayList<>(expressions.size());
		for (SetExpression expr : expressions) {
			result.add(descendSet(parent, expr, arg));
		}
		return result;
	}

	/**
	 * Helper method to descend into any OrderSpec in the given list and adds the result to a
	 * resulting list.
	 * 
	 * @param parent
	 *        queryPart which defines the given list as content.
	 * @param originalOrderSpecs
	 *        the {@link Order}s to descent into
	 * @param arg
	 *        the visit argument
	 * 
	 * @return list of the results of the decent
	 */
	protected List<OrderSpec> descendOrders(QueryPart parent, List<OrderSpec> originalOrderSpecs, A arg) {
		ArrayList<OrderSpec> clonedOrderSpecs = new ArrayList<>(originalOrderSpecs.size());
		for (OrderSpec orderSpec : originalOrderSpecs) {
			clonedOrderSpecs.add((OrderSpec) orderSpec.visitOrder(this, arg));
		}
		return clonedOrderSpecs;
	}

	@Override
	protected RS descendSet(QueryPart parent, SetExpression expr, A arg) {
		return expr.visitSetExpr(this, arg);
	}

	/**
	 * Descends into the given function.
	 * 
	 * @param parent
	 *        queryPart which defines the given list as content.
	 * @param fun
	 *        the {@link Function} to descent into
	 * @param arg
	 *        the visit argument
	 * 
	 * @return result of the decent
	 */
	protected RF descendFun(QueryPart parent, Function fun, A arg) {
		return fun.visitFunction(this, arg);
	}
	
	/**
	 * Descends into the given order.
	 * 
	 * @param parent
	 *        queryPart which defines the given list as content.
	 * @param expr
	 *        the {@link Expression} to descent into
	 * @param arg
	 *        the visit argument
	 * 
	 * @return result of the decent
	 */
	protected RO descendOrder(QueryPart parent, Order expr, A arg) {
		return expr.visitOrder(this, arg);
	}
	
	@Override
	public RQ visitHistoryQuery(HistoryQuery expr, A arg) {
		List<RQ> paramsResult = descendParts(expr, expr.getSearchParams(), arg);
		RS search = descendSet(expr, expr.getSearch(), arg);
		
		return processHistoryQuery(expr, paramsResult, search);
	}

	/**
	 * Descends into the given query part.
	 * 
	 * @param parent
	 *        queryPart which defines the given list as content.
	 * @param expr
	 *        the {@link QueryPart} to descent into
	 * @param arg
	 *        the visit argument
	 * 
	 * @return result of the decent
	 */
	protected RQ descendPart(QueryPart parent, QueryPart expr, A arg) {
		return expr.visitQuery(this, arg);
	}

	/**
	 * Helper method to descend into any query part in the given list and adds the result to a
	 * resulting list.
	 * 
	 * @param parent
	 *        queryPart which defines the given list as content.
	 * @param originalParts
	 *        the {@link QueryPart}s to descent into
	 * @param arg
	 *        the visit argument
	 * 
	 * @return list of the results of the decent
	 */
	protected List<RQ> descendParts(QueryPart parent, List<? extends QueryPart> originalParts, A arg) {
		ArrayList<RQ> clonedParts = new ArrayList<>(originalParts.size());
		for (QueryPart part : originalParts) {
			clonedParts.add(part.visitQuery(this, arg));
		}
		return clonedParts;
	}
	
	@Override
	public RQ visitRevisionQuery(RevisionQuery<?> expr, A arg) {
		List<RQ> paramsResult = descendParts(expr, expr.getSearchParams(), arg);
		RS searchResult = descendSet(expr, expr.getSearch(), arg);
		Order order = expr.getOrder();
		RO orderResult;
		if (order != null) {
			orderResult = descendOrder(expr, order, arg);
		} else {
			orderResult = null;
		}
		return processRevisionQuery(expr, paramsResult, searchResult, orderResult);
	}

	/**
	 * Processes the results of the descent into the parts within a {@link HistoryQuery}
	 * 
	 * @param expr
	 *        the visited query
	 * @param paramsResult
	 *        the results of the descent into {@link AbstractQuery#getSearchParams()}
	 * @param search
	 *        the result of the descent into {@link AbstractQuery#getSearch()}
	 * 
	 * @return the final result of {@link #visitHistoryQuery(HistoryQuery, Object)}
	 */
	protected abstract RQ processHistoryQuery(HistoryQuery expr, List<RQ> paramsResult, RS search);

	/**
	 * Processes the results of the descent into the parts within a {@link RevisionQuery}
	 * 
	 * @param expr
	 *        the visited query
	 * @param paramsResult
	 *        the results of the descent into {@link AbstractQuery#getSearchParams()}
	 * @param search
	 *        the result of the descent into {@link AbstractQuery#getSearch()}
	 * @param order
	 *        the result of the descent into {@link RevisionQuery#getOrder()}
	 * 
	 * @return the final result of {@link #visitHistoryQuery(HistoryQuery, Object)}
	 */
	protected abstract RQ processRevisionQuery(RevisionQuery<?> expr, List<RQ> paramsResult, RS search, RO order);
	
}
