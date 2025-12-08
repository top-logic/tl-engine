/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * A {@link PathElement} that evaluates a TLExpression to compute target objects.
 *
 * <p>
 * The expression receives the source object as its argument and returns a collection (or single
 * object) representing the inheritance targets.
 * </p>
 *
 * <p>
 * This class uses lazy compilation and reflection to avoid a compile-time dependency on
 * com.top_logic.model.search, which would create a circular dependency.
 * </p>
 *
 * @author <a href="mailto:jhu@top-logic.com">jhu</a>
 */
public class ExpressionPathElement extends PathElement {

	private final String _expressionSource;

	private Object _compiledExecutor;

	private boolean _compilationAttempted;

	/**
	 * Creates an {@link ExpressionPathElement}.
	 *
	 * @param expressionSource
	 *        The TLScript expression source code (e.g., "x -> singleton(`Module#SINGLETON`)")
	 */
	public ExpressionPathElement(String expressionSource) {
		super((String) null, false);
		_expressionSource = expressionSource;
		_compilationAttempted = false;
	}

	@Override
	public Collection getValues(Wrapper aBase) {
		try {
			// Lazy compilation on first use
			if (!_compilationAttempted) {
				_compilationAttempted = true;
				_compiledExecutor = compileExpression(_expressionSource);
			}

			if (_compiledExecutor == null) {
				Logger.warn("Expression not compiled, returning empty set: " + _expressionSource,
					ExpressionPathElement.class);
				return Collections.emptySet();
			}

			// Execute expression with source object as argument
			// Uses reflection: QueryExecutor.execute(Object arg)
			Object result = executeExpression(_compiledExecutor, aBase);

			// Convert result to collection
			// Uses reflection: SearchExpression.asList(Object)
			return convertToList(result);

		} catch (Exception ex) {
			// Log error and return empty set to prevent security failures
			Logger.error("Failed to evaluate role inheritance expression for object: " + aBase
				+ ", expression: " + _expressionSource, ex, ExpressionPathElement.class);
			return Collections.emptySet();
		}
	}

	@Override
	public Collection getSources(Wrapper aDestination) {
		// Backward traversal not supported for expressions
		// Expression simply returns target objects for forward inheritance
		return Collections.emptySet();
	}

	/**
	 * Compiles the expression using reflection to avoid compile-time dependency.
	 *
	 * @return The compiled QueryExecutor, or null if compilation fails
	 */
	private Object compileExpression(String expressionSource) {
		try {
			// Use reflection to call: QueryExecutor.compile(Expr)
			// But Expr is also from model.search, so we need to parse the string first

			// Get SearchBuilder class
			Class<?> searchBuilderClass = Class.forName("com.top_logic.model.search.expr.config.SearchBuilder");

			// Get TLModel
			Class<?> modelServiceClass = Class.forName("com.top_logic.util.model.ModelService");
			Method getApplicationModelMethod = modelServiceClass.getMethod("getApplicationModel");
			Object tlModel = getApplicationModelMethod.invoke(null);

			// Parse expression string to SearchExpression
			// SearchBuilder.parseSearchExpression(TLModel model, String source)
			Method parseMethod = searchBuilderClass.getMethod("parseSearchExpression",
				Class.forName("com.top_logic.model.TLModel"), String.class);
			Object searchExpression = parseMethod.invoke(null, tlModel, expressionSource);

			// Compile to QueryExecutor
			// QueryExecutor.compile(KnowledgeBase, TLModel, SearchExpression)
			Class<?> queryExecutorClass = Class.forName("com.top_logic.model.search.expr.query.QueryExecutor");
			Class<?> persistencyLayerClass = Class.forName("com.top_logic.knowledge.service.PersistencyLayer");
			Method getKnowledgeBaseMethod = persistencyLayerClass.getMethod("getKnowledgeBase");
			Object knowledgeBase = getKnowledgeBaseMethod.invoke(null);

			Method compileMethod = queryExecutorClass.getMethod("compile",
				Class.forName("com.top_logic.knowledge.service.KnowledgeBase"),
				Class.forName("com.top_logic.model.TLModel"),
				Class.forName("com.top_logic.model.search.expr.SearchExpression"));

			return compileMethod.invoke(null, knowledgeBase, tlModel, searchExpression);

		} catch (ClassNotFoundException ex) {
			Logger.error("TLScript support not available (com.top_logic.model.search module not loaded). " +
				"Expression-based role inheritance requires the model.search module.", ex,
				ExpressionPathElement.class);
			return null;
		} catch (Exception ex) {
			Logger.error("Failed to compile expression: " + expressionSource, ex,
				ExpressionPathElement.class);
			return null;
		}
	}

	/**
	 * Executes the compiled expression using reflection.
	 */
	private Object executeExpression(Object executor, Object argument) throws Exception {
		// Call: executor.execute(argument)
		Method executeMethod = executor.getClass().getMethod("execute", Object.class);
		return executeMethod.invoke(executor, argument);
	}

	/**
	 * Converts the result to a list using reflection.
	 */
	private Collection convertToList(Object result) throws Exception {
		// Call: SearchExpression.asList(result)
		Class<?> searchExpressionClass = Class.forName("com.top_logic.model.search.expr.SearchExpression");
		Method asListMethod = searchExpressionClass.getMethod("asList", Object.class);
		Object listResult = asListMethod.invoke(null, result);
		return (Collection) listResult;
	}
}
