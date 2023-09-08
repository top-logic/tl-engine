/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import java.util.List;

import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.BinarySetExpression;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.ContextExpression;
import com.top_logic.knowledge.search.CountFunction;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.Function;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.MappingFunction;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.MaxFunction;
import com.top_logic.knowledge.search.MinFunction;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.OrderSpec;
import com.top_logic.knowledge.search.OrderTuple;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.SumFunction;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.visit.DescendingQueryVisitor;

/**
 * Visitor which adopts a {@link QueryPart} inline.
 * 
 * Result of the visit is the same java object of the visited object but maybe internally modified.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InlineExpressionTransformer<A> extends DescendingQueryVisitor<QueryPart, Expression, SetExpression, Function, Order, A> {

	@Override
	protected SetExpression process(CrossProduct expr, List<SetExpression> expressions) {
		expr.setExpressions(expressions);
		return expr;
	}

	@Override
	protected SetExpression process(Filter expr, SetExpression source, Expression filter) {
		expr.setSource(source);
		expr.setFilter(filter);
		return expr;
	}

	@Override
	protected SetExpression process(Intersection expr, SetExpression left, SetExpression right) {
		return processBinarySetExpression(expr, left, right);
	}

	/**
	 * Helper method to process the result of the visit of the parts of some
	 * {@link BinarySetExpression}
	 * 
	 * @param expr
	 *        the visited {@link BinarySetExpression}.
	 * @param left
	 *        the result of the visit of {@link BinarySetExpression#getLeftExpr()}
	 * @param right
	 *        the result of the visit of {@link BinarySetExpression#getRightExpr()}
	 * 
	 * @return the final visit result.
	 */
	protected SetExpression processBinarySetExpression(BinarySetExpression expr, SetExpression left, SetExpression right) {
		expr.setLeftExpr(left);
		expr.setRightExpr(right);
		return expr;
	}

	@Override
	protected SetExpression process(MapTo expr, SetExpression source, Expression mapping) {
		expr.setSource(source);
		expr.setMapping(mapping);
		return expr;
	}

	@Override
	protected SetExpression process(Partition expr, SetExpression source, Expression equivalence, Function representative) {
		expr.setSource(source);
		expr.setEquivalence(equivalence);
		expr.setRepresentative(representative);
		return expr;
	}

	@Override
	protected SetExpression process(Substraction expr, SetExpression left, SetExpression right) {
		return processBinarySetExpression(expr, left, right);
	}

	@Override
	protected SetExpression process(Union expr, SetExpression left, SetExpression right) {
		return processBinarySetExpression(expr, left, right);
	}

	@Override
	protected Expression processBinary(BinaryOperation expr, Expression left, Expression right, A arg) {
		expr.setLeft(left);
		expr.setRight(right);
		return expr;
	}

	@Override
	protected Expression process(Eval expr, Expression context, Expression expression) {
		expr.setContext(context);
		expr.setExpr(expression);
		return expr;
	}

	@Override
	protected Expression process(InSet expr, Expression testExpr, SetExpression set) {
		expr.setContext(testExpr);
		expr.setSetExpr(set);
		return expr;
	}

	@Override
	protected Expression process(Matches expr, Expression expression) {
		expr.setExpr(expression);
		return expr;
	}

	@Override
	protected Expression process(ExpressionTuple expr, List<Expression> entries) {
		expr.setExpressions(entries);
		return expr;
	}

	@Override
	protected Expression process(UnaryOperation expr, Expression expression, A arg) {
		expr.setExpr(expression);
		return expr;
	}

	@Override
	protected Function process(MaxFunction expr, Expression expression) {
		return processMappingFunction(expr, expression);
	}

	/**
	 * Processes the result of the visit of {@link MappingFunction#getExpr()}
	 * 
	 * @param expr
	 *        the visited {@link MappingFunction}
	 * @param expression
	 *        the result of the descent into {@link MappingFunction#getExpr()}
	 * 
	 * @return the final result of the visit
	 */
	protected Function processMappingFunction(MappingFunction expr, Expression expression) {
		expr.setExpr(expression);
		return expr;
	}

	@Override
	protected Function process(MinFunction expr, Expression expression) {
		return processMappingFunction(expr, expression);
	}

	@Override
	protected Function process(SumFunction expr, Expression expression) {
		return processMappingFunction(expr, expression);
	}

	@Override
	protected Order process(OrderSpec expr, Expression expression) {
		expr.setOrderExpr(expression);
		return expr;
	}

	@Override
	protected Order process(OrderTuple expr, List<OrderSpec> expressions) {
		expr.setOrderSpecs(expressions);
		return expr;
	}

	@Override
	public SetExpression visitAllOf(AllOf expr, A arg) {
		return expr;
	}

	@Override
	public SetExpression visitAnyOf(AnyOf expr, A arg) {
		return expr;
	}

	@Override
	public SetExpression visitNone(None expr, A arg) {
		return expr;
	}

	@Override
	public SetExpression visitSetLiteral(SetLiteral expr, A arg) {
		return expr;
	}

	@Override
	public SetExpression visitSetParameter(SetParameter expr, A arg) {
		return expr;
	}
	
	@Override
	protected Expression processAttribute(Attribute attribute, Expression context, A arg) {
		return processContextExpression(attribute, context, arg);
	}

	@Override
	protected Expression processIsCurrent(IsCurrent expr, Expression context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	@Override
	public Expression visitContext(ContextAccess expr, A arg) {
		return expr;
	}

	@Override
	protected Expression processFlex(Flex expr, Expression context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	@Override
	protected Expression processGetEntry(GetEntry expr, Expression context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	@Override
	protected Expression processHasType(HasType expr, Expression context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	@Override
	protected Expression processInstanceOf(InstanceOf expr, Expression context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	@Override
	public Expression visitLiteral(Literal expr, A arg) {
		return expr;
	}

	@Override
	public Expression visitParameter(Parameter expr, A arg) {
		return expr;
	}
	
	@Override
	public Expression visitRequestedHistoryContext(RequestedHistoryContext expr, A arg) {
		return expr;
	}

	@Override
	protected Expression processReference(Reference reference, Expression context, A arg) {
		return processContextExpression(reference, context, arg);
	}

	/**
	 * Sets the given context expression in the {@link ContextExpression} and returns it.
	 */
	protected Expression processContextExpression(ContextExpression expr, Expression context, A arg) {
		expr.setContext(context);
		return expr;
	}

	@Override
	public Function visitCount(CountFunction expr, A arg) {
		return expr;
	}

	@Override
	public QueryPart visitParameterDeclaration(ParameterDeclaration expr, A arg) {
		return expr;
	}
	
	@Override
	protected List<SetExpression> descendSets(QueryPart parent, List<SetExpression> expressions, A arg) {
		int storeIndex = 0;
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			SetExpression elementExpr = expressions.get(n);
			SetExpression transformedExpr = descendSet(parent, elementExpr, arg);
			if (transformedExpr != null) {
				expressions.set(storeIndex++, transformedExpr);
			}
		}
		if (storeIndex < expressions.size()) {
			return expressions.subList(0, storeIndex);
		} else {
			return expressions;
		}
	}
	
	@Override
	protected List<Expression> descendExprs(QueryPart parent, List<Expression> expressions, A arg) {
		int storeIndex = 0;
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			Expression elementExpr = expressions.get(n);
			Expression transformedExpr = descendExpr(parent, elementExpr, arg);
			if (transformedExpr != null) {
				expressions.set(storeIndex++, transformedExpr);
			}
		}
		if (storeIndex < expressions.size()) {
			return expressions.subList(0, storeIndex);
		} else {
			return expressions;
		}
	}
	
	@Override
	protected List<OrderSpec> descendOrders(QueryPart parent, List<OrderSpec> expressions, A arg) {
		int storeIndex = 0;
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			OrderSpec elementExpr = expressions.get(n);
			OrderSpec transformedExpr = (OrderSpec) descendOrder(parent, elementExpr, arg);
			if (transformedExpr != null) {
				expressions.set(storeIndex++, transformedExpr);
			}
		}
		if (storeIndex < expressions.size()) {
			return expressions.subList(0, storeIndex);
		} else {
			return expressions;
		}
	}

	@Override
	protected QueryPart processHistoryQuery(HistoryQuery expr, List<QueryPart> paramsResult, SetExpression search) {
		setParameterDeclarations(expr, paramsResult);
		expr.setSearch(search);
		return expr;
	}

	@Override
	protected QueryPart processRevisionQuery(RevisionQuery<?> expr, List<QueryPart> paramsResult, SetExpression search,
			Order order) {
		setParameterDeclarations(expr, paramsResult);
		expr.setSearch(search);
		expr.setOrder(order);
		return expr;
	}

	protected final void setParameterDeclarations(AbstractQuery expr, List<QueryPart> paramsResult) {
		for (int n = 0, cnt = paramsResult.size(); n < cnt; n++) {
			expr.getSearchParams().set(n, paramsResult.get(n));
		}
	}

}
