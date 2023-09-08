/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.search.QueryVisitor;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
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
 * {@link QueryVisitor} that clones a given {@link QueryPart} deeply.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ExpressionTransformer<A> extends DescendingQueryVisitor<QueryPart, Expression, SetExpression, Function, Order, A> {

	/**
	 * creates a new {@link ExpressionTransformer}
	 */
	protected ExpressionTransformer() {
		// Default constructor.
	}
	
	@Override
	public SetExpression visitAllOf(AllOf expr, A arg) {
		return allOf(expr.getTypeName());
	}

	@Override
	public SetExpression visitAnyOf(AnyOf expr, A arg) {
		return anyOf(expr.getTypeName());
	}
	
	@Override
	protected SetExpression process(CrossProduct expr, List<SetExpression> expressions) {
		return crossProduct(expressions);
	}

	@Override
	protected SetExpression process(Filter expr, SetExpression source, Expression filter) {
		return filter(source, filter);
	}

	@Override
	protected SetExpression process(Intersection expr, SetExpression left, SetExpression right) {
		return intersection(left, right);
	}

	@Override
	protected SetExpression process(MapTo expr, SetExpression source, Expression mapping) {
		return map(source, mapping);
	}

	@Override
	public SetExpression visitNone(None expr, A arg) {
		return none();
	}

	@Override
	protected SetExpression process(Partition expr, SetExpression source, Expression equivalence, Function representative) {
		return partition(source, equivalence, representative);
	}

	@Override
	public SetExpression visitSetLiteral(SetLiteral expr, A arg) {
		return setLiteral(expr.getValues());
	}
	
	@Override
	public SetExpression visitSetParameter(SetParameter expr, A arg) {
		return setParam(expr.getName());
	}

	@Override
	protected SetExpression process(Substraction expr, SetExpression left, SetExpression right) {
		return substraction(left, right);
	}

	@Override
	protected SetExpression process(Union expr, SetExpression left, SetExpression right) {
		return union(left, right);
	}

	@Override
	protected Expression processAttribute(Attribute attribute, Expression context, A arg) {
		return attribute(context, attribute.getOwnerTypeName(), attribute.getAttributeName());

	}

	@Override
	protected Expression processBinary(BinaryOperation expr, Expression left, Expression right, A arg) {
		return binaryOperation(expr.getOperator(), left, right);
	}

	@Override
	public Expression visitContext(ContextAccess expr, A arg) {
		return context();
	}

	@Override
	protected Expression process(Eval orig, Expression context, Expression expression) {
		return eval(context, expression);
	}

	@Override
	protected Expression processFlex(Flex expr, Expression context, A arg) {
		return flex(context, expr.getTypeName(), expr.getAttributeName());
	}

	@Override
	protected Expression processGetEntry(GetEntry getEntry, Expression context, A arg) {
		return getEntry(context, getEntry.getIndex());
	}

	@Override
	protected Expression processHasType(HasType expr, Expression context, A arg) {
		return hasType(context, expr.getTypeName());
	}

	@Override
	protected Expression process(InSet orig, Expression expr, SetExpression set) {
		return inSet(expr, set);
	}

	@Override
	protected Expression processInstanceOf(InstanceOf expr, Expression context, A arg) {
		return instanceOf(context, expr.getTypeName());
	}

	@Override
	public Expression visitLiteral(Literal expr, A arg) {
		return literal(expr.getValue());
	}
	
	@Override
	public QueryPart visitParameterDeclaration(ParameterDeclaration expr, A arg) {
		return paramDecl(expr.getTypeName(), expr.getName());
	}

	@Override
	public Expression visitParameter(Parameter expr, A arg) {
		return param(expr.getName());
	}
	
	@Override
	protected Expression process(Matches expr, Expression expression) {
		return matches(expr.getRegex(), expression);
	}

	@Override
	protected Expression processReference(Reference expr, Expression context, A arg) {
		return reference(context, expr.getOwnerTypeName(), expr.getAttributeName(), expr.getAccessType());
	}

	@Override
	protected Expression process(ExpressionTuple orig, List<Expression> entries) {
		return tuple(entries);
	}

	@Override
	protected Expression process(UnaryOperation expr, Expression expression, A arg) {
		return unaryOperation(expr.getOperator(), expression);
	}

	@Override
	protected Expression processIsCurrent(IsCurrent expr, Expression context, A arg) {
		return isCurrent(context);
	}

	@Override
	public Expression visitRequestedHistoryContext(RequestedHistoryContext expr, A arg) {
		return requestedHistoryContext();
	}

	@Override
	public Function visitCount(CountFunction fun, A arg) {
		return count();
	}

	@Override
	protected Function process(MaxFunction orig, Expression expression) {
		return max(expression);
	}

	@Override
	protected Function process(MinFunction orig, Expression expression) {
		return min(expression);
	}

	@Override
	protected Function process(SumFunction orig, Expression expression) {
		return sum(expression);
	}

	@Override
	protected Order process(OrderSpec expr, Expression expression) {
		return order(expression, expr.isDescending());
	}

	@Override
	protected Order process(OrderTuple orig, List<OrderSpec> expressions) {
		return orders(expressions);
	}

	@Override
	protected QueryPart processHistoryQuery(HistoryQuery expr, List<QueryPart> paramsResult, SetExpression search) {
		return historyQuery(expr.getBranchParam(), expr.getRevisionParam(), expr.getRangeParam(), toParameterDeclarations(paramsResult), search);
	}
	
	@Override
	protected QueryPart processRevisionQuery(RevisionQuery<?> expr, List<QueryPart> paramsResult, SetExpression search,
			Order order) {
		return newRevisionQuery(expr.getBranchParam(), expr.getRangeParam(), toParameterDeclarations(paramsResult), search, order,
			expr.getExpectedType(), expr.getResolve(), expr.getLoadStrategy());
	}

	protected final List<ParameterDeclaration> toParameterDeclarations(List<QueryPart> paramsResult) {
		ArrayList<ParameterDeclaration> params = new ArrayList<>(paramsResult.size());
		for (QueryPart part : paramsResult) {
			params.add((ParameterDeclaration) part);
		}
		return params;
	}
	
}
