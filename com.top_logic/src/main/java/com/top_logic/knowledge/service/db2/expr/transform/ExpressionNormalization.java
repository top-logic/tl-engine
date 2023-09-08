/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultDescendingQueryVisitor;

/**
 * Create a disjunctive normal form for all top-level boolean {@link Expression}
 * s in a {@link SetExpression}.
 * 
 * <p>
 * Argument to the visit: Whether the expression should be negated.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionNormalization extends DefaultDescendingQueryVisitor<Object, List<List<Expression>>, Void, Void, Void, Boolean> {

	/**
	 * Singleton {@link ExpressionNormalization} instance.
	 */
	public static final ExpressionNormalization INSTANCE = new ExpressionNormalization();

	private ExpressionNormalization() {
		// Singleton constructor.
	}
	
	/**
	 * Normalizes all expressions within the given {@link QueryPart}.
	 * 
	 * @param expr
	 *        the expression to normalize.
	 * 
	 * @see ExpressionNormalization
	 */
	public static void normalizeExpressions(QueryPart expr) {
		expr.visitQuery(INSTANCE, false);
	}

	/**
	 * Normalizes all sub expressions of the given {@link Expression}
	 * 
	 * @param expr
	 *        the expression to normalize.
	 * 
	 * @return a normalized variant of the given {@link Expression}
	 * 
	 * @see ExpressionNormalization
	 */
	public static Expression normalizeExpression(Expression expr) {
		return compose(expr, expr.visit(INSTANCE, false));
	}
	
	@Override
	protected Void process(MapTo expr, Void source, List<List<Expression>> mapping) {
		expr.setMapping(compose(expr.getMapping(), mapping));
		return super.process(expr, source, mapping);
	}
	
	@Override
	protected Void process(Filter expr, Void source, List<List<Expression>> filter) {
		expr.setFilter(compose(expr.getFilter(), filter));
		return super.process(expr, source, filter);
	}
	
	@Override
	protected Void process(Partition expr, Void source, List<List<Expression>> equivalence, Void representative) {
		expr.setEquivalence(compose(expr.getEquivalence(), equivalence));
		return super.process(expr, source, equivalence, representative);
	}
	
	@Override
	public List<List<Expression>> visitBinaryOperation(BinaryOperation expr, Boolean arg) {
		switch (expr.getOperator()) {
		case AND:
		case OR:
			return super.visitBinaryOperation(expr, arg);
		default:
			return singletonExpr(expr, arg);
		}
	}

	@Override
	protected List<List<Expression>> processBinary(BinaryOperation expr, List<List<Expression>> left, List<List<Expression>> right, Boolean arg) {
		if (left == null) {
			left = singletonExpr(expr.getLeft(), arg);
		}
		if (right == null) {
			right = singletonExpr(expr.getRight(), arg);
		}
		switch (expr.getOperator()) {
		case AND:
			if (arg) {
				return add(left, right);
			} else {
				return multiply(left, right);
			}
		case OR:
			if (arg) {
				return multiply(left, right);
			} else {
				return add(left, right);
			}
		default:
			throw new UnreachableAssertion("Does not descend to non boolean expressions.");
		}
	}
	
	@Override
	public List<List<Expression>> visitUnaryOperation(UnaryOperation expr, Boolean arg) {
		switch (expr.getOperator()) {
		case NOT:
			return super.visitUnaryOperation(expr, ! arg);
			case IS_NULL:
			case BRANCH:
			case IDENTIFIER:
			case TYPE_NAME:
			case REVISION:
			case HISTORY_CONTEXT:
				return singletonExpr(expr, arg);
		default:
				throw Operator.noSuchOperator(expr.getOperator());
		}
	}
	
	@Override
	protected List<List<Expression>> process(UnaryOperation expr, List<List<Expression>> expression, Boolean arg) {
		if (expression == null) {
			expression = singletonExpr(expr.getExpr(), arg);
		}
		switch (expr.getOperator()) {
			case NOT:
				return expression;
			default:
				// descending only in case NOT; see #visitUnaryOperation
				throw new UnreachableAssertion("Does not descend to non boolean expressions.");
		}
	}
	
	@Override
	public List<List<Expression>> visitInSet(InSet expr, Boolean arg) {
		// Reset negation.
		return super.visitInSet(expr, false);
	}
	
	// Utilities.
	
	private static List<List<Expression>> singletonExpr(Expression expr, Boolean negate) {
		if (negate) {
			expr = not(expr);
		}
		List<Expression> and = Collections.<Expression>singletonList(expr);
		List<List<Expression>> or = Collections.singletonList(and);
		return or;
	}
	
	/**
	 * Creates a list which is equal to the concatenation of the given lists
	 * 
	 * @see CollectionUtil#concatNew(List, List)
	 */
	private static List<List<Expression>> add(List<List<Expression>> left, List<List<Expression>> right) {
		return CollectionUtil.concatNew(left, right);
	}

	/**
	 * Creates the cross product of the given Lists, i.e. for each pair <code>x_i</code> in
	 * <code>left</code>, <code>y_j</code>in <code>right</code> there will be a list entry in the
	 * resulting list which contains all elements in <code>x_i</code> and <code>y_j</code>.
	 * 
	 * All operations are order preserving.
	 */
	private static ArrayList<List<Expression>> multiply(List<List<Expression>> left, List<List<Expression>> right) {
		ArrayList<List<Expression>> result = new ArrayList<>();
		for (List<Expression> leftAnd : left) {
			for (List<Expression> rightAnd : right) {
				List<Expression> and = addCopy(leftAnd, rightAnd);
				result.add(and);
			}
		}
		return result;
	}

	/**
	 * Copies the expressions in the given lists and adds them to a new one.
	 * 
	 * @return the newly created list
	 */
	private static ArrayList<Expression> addCopy(List<Expression> leftAnd, List<Expression> rightAnd) {
		ArrayList<Expression> result = new ArrayList<>(leftAnd.size() + rightAnd.size());
		addCopy(result, leftAnd);
		addCopy(result, rightAnd);
		return result;
	}

	/**
	 * Copies the {@link Expression}s in the given <code>source</code> and adds them to the given
	 * result (in <code>source</code> order).
	 */
	private static void addCopy(ArrayList<Expression> result, List<Expression> source) {
		for (Expression expr : source) {
			result.add(ExpressionCopy.copy(expr));
		}
	}
	
	private static Expression compose(Expression orig, List<List<Expression>> or) {
		if (or == null) {
			return orig;
		}
		return createDisjunction(or);
	}

	private static Expression createDisjunction(List<List<Expression>> or) {
		Expression resultOr = null;
		for (List<Expression> and : or) {
			Expression resultAnd = createConjunction(and);
			resultOr = or(resultOr, resultAnd);
		}
		return resultOr;
	}

	private static Expression createConjunction(List<Expression> and) {
		Expression resultAnd = null;
		for (Expression expr : and) {
			resultAnd = and(resultAnd, expr);
		}
		return resultAnd;
	}
	
}
