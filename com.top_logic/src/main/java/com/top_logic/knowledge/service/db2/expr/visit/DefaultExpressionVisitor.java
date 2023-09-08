/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.ContextExpression;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.Operation;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.TypeCheck;
import com.top_logic.knowledge.search.TypedExpression;
import com.top_logic.knowledge.search.UnaryOperation;

/**
 * Base class for {@link ExpressionVisitor}s.
 * 
 * <p>
 * All visit methods delegate to default implementations along the visited type
 * hierarchy.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultExpressionVisitor<R,A> implements ExpressionVisitor<R,A> {

	@Override
	public R visitAttribute(Attribute expr, A arg) {
		return visitContextExpression(expr, arg);
	}
	
	@Override
	public R visitGetEntry(GetEntry expr, A arg) {
		return visitContextExpression(expr, arg);
	}

	@Override
	public R visitBinaryOperation(BinaryOperation expr, A arg) {
		return visitOperation(expr, arg);
	}

	@Override
	public R visitInstanceOf(InstanceOf expr, A arg) {
		return visitTypeCheck(expr, arg);
	}

	@Override
	public R visitHasType(HasType expr, A arg) {
		return visitTypeCheck(expr, arg);
	}
	
	@Override
	public R visitLiteral(Literal expr, A arg) {
		return visitExpression(expr, arg);
	}
	
	@Override
	public R visitParameter(Parameter expr, A arg) {
		return visitExpression(expr, arg);
	}
	
	@Override
	public R visitUnaryOperation(UnaryOperation expr, A arg) {
		return visitOperation(expr, arg);
	}

	@Override
	public R visitTuple(ExpressionTuple expr, A arg) {
		return visitExpression(expr, arg);
	}
	
	@Override
	public R visitIsCurrent(IsCurrent expr, A arg) {
		return visitContextExpression(expr, arg);
	}

	/**
	 * Default action for all {@link Operation} implementations.
	 */
	protected R visitOperation(Operation expr, A arg) {
		return visitExpression(expr, arg);
	}

	@Override
	public R visitFlex(Flex expr, A arg) {
		return visitTypedExpression(expr, arg);
	}

	@Override
	public R visitMatches(Matches expr, A arg) {
		return visitExpression(expr, arg);
	}

	@Override
	public R visitReference(Reference expr, A arg) {
		return visitContextExpression(expr, arg);
	}
	
	@Override
	public R visitEval(Eval expr, A arg) {
		return visitContextExpression(expr, arg);
	}

	@Override
	public R visitContext(ContextAccess expr, A arg) {
		return visitExpression(expr, arg);
	}
	
	@Override
	public R visitInSet(InSet expr, A arg) {
		return visitContextExpression(expr, arg);
	}
	
	@Override
	public R visitRequestedHistoryContext(RequestedHistoryContext expr, A arg) {
		return visitExpression(expr, arg);
	}

	/**
	 * Default action for implementations of {@link TypeCheck}
	 * 
	 * @param expr
	 *        the visited {@link TypeCheck}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected R visitTypeCheck(TypeCheck expr, A arg) {
		return visitTypedExpression(expr, arg);
	}

	/**
	 * Default action for implementations of {@link TypedExpression}
	 * 
	 * @param expr
	 *        the visited {@link TypedExpression}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected R visitTypedExpression(TypedExpression expr, A arg) {
		return visitContextExpression(expr, arg);
	}

	/**
	 * Default action for implementations of {@link ContextExpression}
	 * 
	 * @param expr
	 *        the visited {@link ContextExpression}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected R visitContextExpression(ContextExpression expr, A arg) {
		return visitExpression(expr, arg);
	}

	/**
	 * Default action for all {@link Expression} implementations.
	 * 
	 * @param expr
	 *        the visited {@link Expression}
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return the final result of the visit
	 */
	protected R visitExpression(Expression expr, A arg) {
		return null;
	}

}
