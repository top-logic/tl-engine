/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.List;

import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.ContextExpression;
import com.top_logic.knowledge.search.CountFunction;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Flex;
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
import com.top_logic.knowledge.search.OrderSpec;
import com.top_logic.knowledge.search.OrderTuple;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.SumFunction;
import com.top_logic.knowledge.search.TypeCheck;
import com.top_logic.knowledge.search.TypedExpression;
import com.top_logic.knowledge.search.TypedSetExpression;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.search.Union;

/**
 * Simple implementation of {@link DescendingQueryVisitor} which ignores all processed results and
 * returns <code>null</code> in each visit case.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDescendingQueryVisitor<RQ, RE extends RQ,RS extends RQ,RF extends RQ,RO extends RQ, A> extends DescendingQueryVisitor<RQ, RE, RS, RF, RO, A> {

	@Override
	protected RE processBinary(BinaryOperation expr, RE left, RE right, A arg) {
		return null;
	}

	@Override
	protected RE process(Eval orig, RE context, RE expression) {
		return null;
	}

	@Override
	protected RE process(InSet orig, RE expr, RS set) {
		return null;
	}

	@Override
	protected RE process(Matches expr, RE expression) {
		return null;
	}

	@Override
	protected RS process(CrossProduct expr, List<RS> expressions) {
		return null;
	}

	@Override
	protected RS process(Filter expr, RS source, RE filter) {
		return null;
	}

	@Override
	protected RS process(Intersection expr, RS left, RS right) {
		return null;
	}

	@Override
	protected RS process(MapTo expr, RS source, RE mapping) {
		return null;
	}

	@Override
	protected RF process(MaxFunction orig, RE expression) {
		return null;
	}

	@Override
	protected RF process(MinFunction orig, RE expression) {
		return null;
	}

	@Override
	protected RO process(OrderSpec expr, RE expression) {
		return null;
	}

	@Override
	protected RO process(OrderTuple orig, List<OrderSpec> expressions) {
		return null;
	}

	@Override
	protected RS process(Partition expr, RS source, RE equivalence, RF representative) {
		return null;
	}

	@Override
	protected RS process(Substraction expr, RS left, RS right) {
		return null;
	}

	@Override
	protected RF process(SumFunction orig, RE expression) {
		return null;
	}

	@Override
	protected RE process(ExpressionTuple orig, List<RE> entries) {
		return null;
	}

	@Override
	protected RE process(UnaryOperation expr, RE expression, A arg) {
		return null;
	}

	@Override
	protected RS process(Union expr, RS left, RS right) {
		return null;
	}

	@Override
	public RS visitAllOf(AllOf expr, A arg) {
		return visitTypedSetExpression(expr, arg);
	}
	
	@Override
	public RS visitAnyOf(AnyOf expr, A arg) {
		return visitTypedSetExpression(expr, arg);
	}

	@Override
	public RS visitNone(None expr, A arg) {
		return null;
	}

	@Override
	public RS visitSetLiteral(SetLiteral expr, A arg) {
		return null;
	}
	
	@Override
	public RS visitSetParameter(SetParameter expr, A arg) {
		return null;
	}

	@Override
	public RE visitContext(ContextAccess expr, A arg) {
		return null;
	}

	@Override
	public RE visitParameterDeclaration(ParameterDeclaration expr, A arg) {
		return null;
	}

	@Override
	public RE visitLiteral(Literal expr, A arg) {
		return null;
	}

	@Override
	public RE visitParameter(Parameter expr, A arg) {
		return null;
	}

	@Override
	public RF visitCount(CountFunction fun, A arg) {
		return null;
	}

	@Override
	public RE visitRequestedHistoryContext(RequestedHistoryContext expr, A arg) {
		return null;
	}

	@Override
	protected RE processAttribute(Attribute attribute, RE context, A arg) {
		return processContextExpression(attribute, context, arg);
	}

	@Override
	protected RE processFlex(Flex expr, RE context, A arg) {
		return processTypedExpression(expr, context, arg);
	}

	@Override
	protected RE processGetEntry(GetEntry expr, RE context, A arg) {
		return processContextExpression(expr, context, arg);
	}
	
	@Override
	protected RE processHasType(HasType expr, RE context, A arg) {
		return processTypeCheck(expr, context, arg);
	}
	
	@Override
	protected RE processInstanceOf(InstanceOf expr, RE context, A arg) {
		return processTypeCheck(expr, context, arg);
	}

	@Override
	protected RE processReference(Reference expr, RE context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	@Override
	protected RE processIsCurrent(IsCurrent expr, RE context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	/**
	 * Common method to handle visits of instances of {@link TypeCheck}
	 * 
	 * @param expr
	 *        the visited {@link TypeCheck}
	 * @param context
	 *        the result of the visited context expression.
	 * @param arg
	 *        the argument of the visit
	 * @return the final result of the visit
	 */
	protected RE processTypeCheck(TypeCheck expr, RE context, A arg) {
		return processTypedExpression(expr, context, arg);
	}
	
	/**
	 * Common method to handle visits of instances of {@link TypedExpression}
	 * 
	 * @param expr
	 *        the visited {@link TypedExpression}
	 * @param context
	 *        the result of the visited context expression.
	 * @param arg
	 *        the argument of the visit
	 * @return the final result of the visit
	 */
	protected RE processTypedExpression(TypedExpression expr, RE context, A arg) {
		return processContextExpression(expr, context, arg);
	}

	/**
	 * Common method to handle visits of instances of {@link ContextExpression}
	 * 
	 * @param expr
	 *        the visited {@link ContextExpression}
	 * @param context
	 *        the result of the visited context expression.
	 * @param arg
	 *        the argument of the visit
	 * @return the final result of the visit
	 */
	protected RE processContextExpression(ContextExpression expr, RE context, A arg) {
		return null;
	}

	/**
	 * Common method to handle visits of instances of {@link TypedSetExpression}
	 * 
	 * @param expr
	 *        the visited {@link TypedSetExpression}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected RS visitTypedSetExpression(TypedSetExpression expr, A arg) {
		return null;
	}

	@Override
	protected RQ processHistoryQuery(HistoryQuery expr, List<RQ> paramsResult, RS search) {
		return null;
	}

	@Override
	protected RQ processRevisionQuery(RevisionQuery<?> expr, List<RQ> paramsResult, RS search, RO order) {
		return null;
	}
}
