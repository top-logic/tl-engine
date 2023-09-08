/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.Collections;
import java.util.Map;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Revision;

/**
 * Evaluates an {@link Expression} in the context of a {@link DataObject}.
 * 
 * <p>
 * The evaluator expects the context object as argument to the visit methods.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SimpleExpressionEvaluator extends ObjectExpressionEvaluator<ObjectExpressionEvaluator.ExpressionContext> {

	/** Constant to use when no arguments are given. */
	public static final Map<String, ?> NO_ARGUMENTS = Collections.emptyMap();

	/**
	 * Singleton instance of this class.
	 * 
	 * @see #evaluate(Expression, DataObject) Evaluating an expression on a
	 *      base object.
	 */
	static final SimpleExpressionEvaluator INSTANCE = new SimpleExpressionEvaluator();
	
	private SimpleExpressionEvaluator() {
		// Singleton constructor.
	}

	@Override
	protected final ObjectExpressionEvaluator.ExpressionContext createContext(
			ObjectExpressionEvaluator.ExpressionContext parent, ObjectContext objectContext, Object contextObject) {
		return new ExpressionContext(parent.getEvaluationRevision(), objectContext, contextObject,
			parent.getParameterValues());
	}

	/**
	 * Evaluates the given expression on the given base object in {@link Revision#CURRENT}.
	 * 
	 * @see SimpleExpressionEvaluator#evaluate(Expression, Revision, DataObject, Map)
	 */
	public static Object evaluate(Expression expr, DataObject baseObject, Map<String, ?> arguments) {
		return evaluate(expr, Revision.CURRENT, baseObject, arguments);
	}
	
	/**
	 * Evaluates the given expression on the given base object.
	 * 
	 * @param expr
	 *        The expression to evaluate.
	 * @param evaluationRevision
	 *        The revision in which the expression is evaluated.
	 * @param baseObject
	 *        The base object that serves the context for {@link Attribute} expressions.
	 * @return The evaluation result.
	 */
	public static Object evaluate(Expression expr, Revision evaluationRevision, DataObject baseObject,
			Map<String, ?> arguments) {
		ExpressionContext context = new ExpressionContext(evaluationRevision, baseObject, arguments);
		Object searchResult = expr.visit(SimpleExpressionEvaluator.INSTANCE, context);
		if (searchResult instanceof ObjectKey) {
			searchResult = context.getObjectContext().resolveObject((ObjectKey) searchResult);
		}
		return searchResult;
	}

	/**
	 * Evaluates the given expression without any arguments
	 * 
	 * @see SimpleExpressionEvaluator#evaluate(Expression, Revision, DataObject, Map)
	 */
	public static Object evaluate(Expression expr, Revision evaluationRevision, DataObject baseObject) {
		return evaluate(expr, evaluationRevision, baseObject, NO_ARGUMENTS);
	}

	/**
	 * Evaluates the given expression without any arguments in {@link Revision#CURRENT}.
	 * 
	 * @see SimpleExpressionEvaluator#evaluate(Expression, Revision, DataObject)
	 */
	public static Object evaluate(Expression expr, DataObject baseObject) {
		return evaluate(expr, Revision.CURRENT, baseObject);
	}

	/**
	 * Checks, whether the given search expression (of result type boolean) matches the given
	 * context object.
	 * 
	 * @param search
	 *        The search expression of type boolean.
	 * @param baseObject
	 *        The context object on which the expression is evaluated.
	 * @return Whether the search expression evaluated to <code>true</code>.
	 */
	public static boolean matches(Expression search, DataObject baseObject) {
		Object expressionResult = evaluate(search, baseObject);
		return ((Boolean) expressionResult).booleanValue();
	}
	
	/**
	 * Checks, whether the given search expression (of result type boolean) matches the given
	 * context object.
	 * 
	 * @param search
	 *        The search expression of type boolean.
	 * @param baseObject
	 *        The context object on which the expression is evaluated.
	 * @param arguments
	 *        arguments for the expression
	 * @return Whether the search expression evaluated to <code>true</code>.
	 */
	public static boolean matches(Expression search, DataObject baseObject, Map<String, ?> arguments) {
		Object expressionResult = evaluate(search, baseObject, arguments);
		return ((Boolean) expressionResult).booleanValue();
	}

	/**
	 * Checks, whether the given context object is contained in the given set expression when
	 * evaluated in the given {@link Revision}.
	 * 
	 * @param search
	 *        The {@link SetExpression} under test.
	 * @param baseObject
	 *        The object under test.
	 * @param arguments
	 *        Arguments for the {@link SetExpression}
	 * 
	 * @return Whether given set contains the object.
	 */
	public static boolean contains(SetExpression search, Revision evaluationRevision, DataObject baseObject,
			Map<String, ?> arguments) {
		ExpressionContext context = new ExpressionContext(evaluationRevision, baseObject, arguments);
		return search.visitSetExpr(CheckSetContains.IS_CONTAINED_INSTANCE, context);
	}

	/**
	 * Checks, whether the given context object is contained in the given set expression when
	 * evaluated in {@link Revision#CURRENT}.
	 * 
	 * @see #contains(SetExpression, Revision, DataObject, Map)
	 */
	public static boolean contains(SetExpression search, DataObject baseObject, Map<String, ?> arguments) {
		return contains(search, Revision.CURRENT, baseObject, arguments);
	}

	@Override
	public Object visitInSet(InSet expr, ExpressionContext arg) {
		ExpressionContext newContext = createNewContextForDescending(expr, arg);
		return expr.getSetExpr().visitSetExpr(CheckSetContains.IS_CONTAINED_INSTANCE, newContext);
	}

}
