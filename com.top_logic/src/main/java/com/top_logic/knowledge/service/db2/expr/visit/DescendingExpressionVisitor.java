/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
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
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.UnaryOperation;

/**
 * Abstract implementation of {@link ExpressionVisitor} which handles combined {@link Expression}.
 * The {@link DescendingExpressionVisitor} fist visits the sub parts of the visited expression and
 * handles the results of that visit.
 * 
 * @param <RE>
 *        the result type of the visit of an expression
 * @param <RS>
 *        the type of the descent into {@link SetExpression} of visited expression
 * @param <A>
 *        the argument type of the visit of an expression
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DescendingExpressionVisitor<RE, RS, A> implements ExpressionVisitor<RE, A> {

	@Override
	public RE visitIsCurrent(IsCurrent expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		return processIsCurrent(expr, context, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitIsCurrent(IsCurrent, Object)}
	 * 
	 * @param expr
	 *        the visited {@link IsCurrent}
	 * @param context
	 *        the result of the visit of the {@link IsCurrent#getContext() context} of the given
	 *        <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitIsCurrent(IsCurrent, Object)}
	 */
	protected abstract RE processIsCurrent(IsCurrent expr, RE context, A arg);

	@Override
	public RE visitFlex(Flex expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		return processFlex(expr, context, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitFlex(Flex, Object)}
	 * 
	 * @param expr
	 *        the visited {@link Flex}
	 * @param context
	 *        the result of the visit of the {@link Flex#getContext() context} of the
	 *        given <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitFlex(Flex, Object)}
	 */
	protected abstract RE processFlex(Flex expr, RE context, A arg);

	@Override
	public RE visitHasType(HasType expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		return processHasType(expr, context, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitHasType(HasType, Object)}
	 * 
	 * @param expr
	 *        the visited {@link HasType}
	 * @param context
	 *        the result of the visit of the {@link HasType#getContext() context} of the
	 *        given <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitHasType(HasType, Object)}
	 */
	protected abstract RE processHasType(HasType expr, RE context, A arg);

	@Override
	public RE visitInstanceOf(InstanceOf expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		return processInstanceOf(expr, context, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitInstanceOf(InstanceOf, Object)}
	 * 
	 * @param expr
	 *        the visited {@link InstanceOf}
	 * @param context
	 *        the result of the visit of the {@link InstanceOf#getContext() context} of the
	 *        given <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitInstanceOf(InstanceOf, Object)}
	 */
	protected abstract RE processInstanceOf(InstanceOf expr, RE context, A arg);

	@Override
	public RE visitReference(Reference expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		return processReference(expr, context, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitReference(Reference, Object)}
	 * 
	 * @param reference
	 *        the visited {@link Reference}
	 * @param context
	 *        the result of the visit of the {@link Reference#getContext() context} of the
	 *        given <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitReference(Reference, Object)}
	 */
	protected abstract RE processReference(Reference reference, RE context, A arg);

	@Override
	public RE visitAttribute(Attribute expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		return processAttribute(expr, context, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitAttribute(Attribute, Object)}
	 * 
	 * @param attribute
	 *        the visited {@link Attribute}
	 * @param context
	 *        the result of the visit of the {@link Attribute#getContext() context} of the
	 *        given <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitAttribute(Attribute, Object)}
	 */
	protected abstract RE processAttribute(Attribute attribute, RE context, A arg);

	@Override
	public RE visitGetEntry(GetEntry expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		return processGetEntry(expr, context, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitGetEntry(GetEntry, Object)}
	 * 
	 * @param getEntry
	 *        the visited {@link GetEntry}
	 * @param context
	 *        the result of the visit of the {@link GetEntry#getContext() context} of the
	 *        given <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitGetEntry(GetEntry, Object)}
	 */
	protected abstract RE processGetEntry(GetEntry getEntry, RE context, A arg);

	@Override
	public RE visitBinaryOperation(BinaryOperation expr, A arg) {
		RE left = descendExpr(expr, expr.getLeft(), arg);
		RE right = descendExpr(expr, expr.getRight(), arg);
		return processBinary(expr, left, right, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in
	 * {@link #visitBinaryOperation(BinaryOperation, Object)}
	 * 
	 * @param expr
	 *        the visited {@link BinaryOperation}
	 * @param left
	 *        the result of the visit of the {@link BinaryOperation#getLeft() left part} of the
	 *        given <code>expr</code>.
	 * @param right
	 *        the result of the visit of the {@link BinaryOperation#getRight() right part} of the
	 *        given <code>expr</code>.
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the actual result of {@link #visitBinaryOperation(BinaryOperation, Object)}
	 */
	protected abstract RE processBinary(BinaryOperation expr, RE left, RE right, A arg);

	@Override
	public RE visitEval(Eval expr, A arg) {
		RE context = descendExpr(expr, expr.getContext(), arg);
		RE expression = descendExpr(expr, expr.getExpr(), arg);
		return process(expr, context, expression);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitEval(Eval, Object)}
	 * 
	 * @param orig
	 *        the visited {@link Eval}
	 * @param context
	 *        the result of the visit of the {@link Eval#getContext() context} of the given
	 *        <code>orig</code>.
	 * @param expression
	 *        the result of the visit of the {@link Eval#getExpr() expression} of the given
	 *        <code>orig</code>.
	 * 
	 * @return the actual result of {@link #visitEval(Eval, Object)}
	 */
	protected abstract RE process(Eval orig, RE context, RE expression);

	@Override
	public RE visitInSet(InSet expr, A arg) {
		RE transformedExpr = descendExpr(expr, expr.getContext(), arg);
		RS transformedSetExpr = descendSet(expr, expr.getSetExpr(), arg);
		return process(expr, transformedExpr, transformedSetExpr);
	}

	/**
	 * Performs the visiting of {@link InSet#getSetExpr()} in {@link #visitInSet(InSet, Object)}
	 * 
	 * @param parent
	 *        the visited {@link InSet}
	 * @param expr
	 *        the {@link SetExpression set expression} of the given <code>orig</code>.
	 * @param arg
	 *        the argument of {@link #visitInSet(InSet, Object)}
	 * 
	 * @return the input for {@link #process(InSet, Object, Object)}
	 * 
	 * @see #process(InSet, Object, Object)
	 */
	protected abstract RS descendSet(QueryPart parent, SetExpression expr, A arg);

	/**
	 * Subclass hook to create the result of the visit in {@link #visitInSet(InSet, Object)}
	 * 
	 * @param orig
	 *        the visited {@link InSet}
	 * @param set
	 *        the result of the visit of the {@link InSet#getSetExpr() set expression} of the given
	 *        <code>orig</code>.
	 * @return the actual result of {@link #visitInSet(InSet, Object)}
	 * 
	 * @see #descendSet(QueryPart, SetExpression, Object)
	 */
	protected abstract RE process(InSet orig, RE expr, RS set);

	@Override
	public RE visitMatches(Matches expr, A arg) {
		RE expression = descendExpr(expr, expr.getExpr(), arg);
		return process(expr, expression);
	}

	/**
	 * Subclass hook to create the result of the visit in {@link #visitMatches(Matches, Object)}
	 * 
	 * @param expr
	 *        the visited {@link Matches}
	 * @param expression
	 *        the result of {@link #descendExpr(QueryPart, Expression, Object)} of the
	 *        {@link Matches#getExpr() expression} the given <code>orig</code>.
	 * 
	 * @return the actual result of {@link #visitMatches(Matches, Object)}
	 * 
	 * @see #descendSet(QueryPart, SetExpression, Object)
	 */
	protected abstract RE process(Matches expr, RE expression);

	@Override
	public RE visitTuple(ExpressionTuple expr, A arg) {
		List<RE> entries = descendExprs(expr, expr.getExpressions(), arg);
		return process(expr, entries);
	}

	/**
	 * Subclass hook to create the result of the visit in
	 * {@link #visitTuple(ExpressionTuple, Object)}
	 * 
	 * @param orig
	 *        the visited {@link ExpressionTuple}
	 * @param entries
	 *        the list of results of {@link #descendExprs(QueryPart, List, Object)} of the
	 *        {@link ExpressionTuple#getExpressions() expressions} the given <code>orig</code>.
	 * 
	 * @return the actual result of {@link #visitTuple(ExpressionTuple, Object)}
	 * 
	 * @see #descendSet(QueryPart, SetExpression, Object)
	 */
	protected abstract RE process(ExpressionTuple orig, List<RE> entries);

	/**
	 * Visits all elements of the given expression list and adds the result to a result list (order
	 * preserving).
	 * 
	 * @param parent
	 *        the parent of each {@link Expression} in the given list
	 * @param expressions
	 *        the list of expressions to visit
	 * @param arg
	 *        the argument of the visit
	 * 
	 * @return a {@link List} containing the results of the visits of the expressions in the given
	 *         expression list.
	 */
	protected List<RE> descendExprs(QueryPart parent, List<Expression> expressions, A arg) {
		ArrayList<RE> result = new ArrayList<>(expressions.size());
		for (Expression expr : expressions) {
			result.add(descendExpr(parent, expr, arg));
		}
		return result;
	}

	@Override
	public RE visitUnaryOperation(UnaryOperation expr, A arg) {
		RE expression = descendExpr(expr, expr.getExpr(), arg);
		return process(expr, expression, arg);
	}

	/**
	 * Subclass hook to create the result of the visit in
	 * {@link #visitUnaryOperation(UnaryOperation, Object)}
	 * 
	 * @param expr
	 *        the visited {@link UnaryOperation}
	 * @param expression
	 *        the result of the visit of the {@link UnaryOperation#getExpr() expression} of the
	 *        given <code>orig</code>.
	 * @param arg
	 *        the argument of {@link #visitUnaryOperation(UnaryOperation, Object)}
	 * 
	 * @return the actual result of {@link #visitUnaryOperation(UnaryOperation, Object)}
	 */
	protected abstract RE process(UnaryOperation expr, RE expression, A arg);

	/**
	 * Visits the given <code>expr</code>.
	 * 
	 * @param parent
	 *        the direct parent of the given <code>expr</code>
	 * @param expr
	 *        the expression to visit
	 * @param arg
	 *        the visit argument
	 * 
	 * @return the result of the visit of the given <code>expr</code>
	 */
	protected RE descendExpr(QueryPart parent, Expression expr, A arg) {
		return expr.visit(this, arg);
	}

}
