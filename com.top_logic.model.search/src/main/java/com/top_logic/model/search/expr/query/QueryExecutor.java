/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.query;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.SearchExpressionCompiler;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.interpreter.DefResolver;
import com.top_logic.model.search.expr.interpreter.TypeResolver;
import com.top_logic.util.model.ModelService;

/**
 * Execution of a configurable {@link Expr search expression}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class QueryExecutor {

	/**
	 * Creates a {@link QueryExecutor} from the textual XML representation of a search expression.
	 * 
	 * @param expr
	 *        The textual XML representation of the search, or <code>null</code>.
	 * @return The {@link QueryExecutor} that can execute the search, or <code>null</code>, if the
	 *         argument was null.
	 * 
	 * @see #compile(Expr)
	 */
	public static QueryExecutor compileOptional(Expr expr) {
		if (expr == null) {
			return null;
		}
		return compile(expr);
	}
	
	/**
	 * Creates a {@link QueryExecutor} from the textual XML representation of a search expression.
	 * 
	 * @param expr
	 *        The textual XML representation of the search.
	 * @return The {@link QueryExecutor} that can execute the search, see
	 *         {@link #executeWith(DisplayContext, TagWriter, Args)}.
	 * 
	 * @see #compile(KnowledgeBase, TLModel, SearchExpression)
	 */
	public static QueryExecutor compile(Expr expr) {
		if (PersistencyLayer.Module.INSTANCE.isActive() && ModelService.Module.INSTANCE.isActive()) {
			return compile(kb(), model(), expr);
		} else {
			return new DeferredQueryExecutor(expr);
		}
	}

	/**
	 * Creates a {@link QueryExecutor} from the textual XML representation of a search expression.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to execute in.
	 * @param model
	 *        The {@link TLModel} the given search is formulated in.
	 * @param expr
	 *        The textual XML representation of the search.
	 * @return The {@link QueryExecutor} that can execute the search, see
	 *         {@link #executeWith(DisplayContext, TagWriter, Args)}.
	 * 
	 * @see #compile(KnowledgeBase, TLModel, SearchExpression)
	 */
	public static QueryExecutor compile(KnowledgeBase kb, TLModel model, Expr expr) {
		return compile(kb, model, SearchBuilder.toSearchExpression(model, expr));
	}

	/**
	 * Creates a {@link QueryExecutor} for a search expression.
	 * <p>
	 * Uses the default {@link KnowledgeBase} and {@link TLModel}.
	 * </p>
	 * 
	 * @param expr
	 *        The search expression to execute.
	 * @return The {@link QueryExecutor} that can execute the search, see
	 *         {@link #executeWith(DisplayContext, TagWriter, Args)}.
	 * 
	 * @see #compile(KnowledgeBase, TLModel, SearchExpression)
	 */
	public static QueryExecutor compile(SearchExpression expr) {
		return compile(kb(), model(), expr);
	}

	/**
	 * Creates a {@link QueryExecutor} for a search expression.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to execute in.
	 * @param expr
	 *        The search expression to execute.
	 * @return The {@link QueryExecutor} that can execute the search, see
	 *         {@link #executeWith(DisplayContext, TagWriter, Args)}.
	 */
	public static QueryExecutor compile(KnowledgeBase kb, TLModel model, SearchExpression expr) {
		return interpret(kb, model, compileExpr(kb, model, expr));
	}

	/**
	 * Creates a {@link SearchExpression} from the textual XML representation of a search
	 * expression.
	 * 
	 * @param expr
	 *        The textual XML representation of the search, or <code>null</code>.
	 * @return The {@link SearchExpression} for the given {@link Expr} , or <code>null</code>, if
	 *         the argument was null.
	 * 
	 * @see #compileExpr(Expr)
	 */
	public static SearchExpression compileExprOptional(Expr expr) {
		if (expr == null) {
			return null;
		}
		return compileExpr(expr);
	}

	/**
	 * Builds and optimizes a {@link SearchExpression} from the given {@link Expr}.
	 */
	public static SearchExpression compileExpr(Expr expr) {
		return compileExpr(model(), expr);
	}

	/**
	 * Builds and optimizes a {@link SearchExpression} from the given {@link Expr}.
	 */
	public static SearchExpression compileExpr(TLModel model, Expr expr) {
		return compileExpr(kb(), model, expr);
	}

	/**
	 * Builds and optimizes a {@link SearchExpression} from the given {@link Expr}.
	 */
	public static SearchExpression compileExpr(KnowledgeBase kb, TLModel model, Expr expr) {
		return compileExpr(kb, model, SearchBuilder.toSearchExpression(model, expr));
	}

	/**
	 * Optimizes the given {@link SearchExpression}.
	 */
	public static SearchExpression compileExpr(SearchExpression expr) {
		return compileExpr(kb(), model(), expr);
	}

	/**
	 * Optimizes the given {@link SearchExpression}.
	 */
	public static SearchExpression compileExpr(KnowledgeBase kb, TLModel model, SearchExpression expr) {
		return QueryExecutor.resolve(model, compiler(kb).compile(expr));
	}

	private static SearchExpressionCompiler compiler(KnowledgeBase kb) {
		return new SearchExpressionCompiler(kb.getMORepository());
	}

	/**
	 * Creates a {@link QueryExecutor} for interpreting a search expression.
	 * 
	 * @param knowledgeBase
	 *        The {@link KnowledgeBase} for which the {@link SearchExpression} was compiled. Is
	 *        allowed to be null, if the {@link SearchExpression} won't use the
	 *        {@link KnowledgeBase}.
	 * @param model
	 *        The model of the data on which the search is performed.
	 * @param expr
	 *        The search expression to execute.
	 * @return The {@link QueryExecutor} that can execute the search, see
	 *         {@link #executeWith(DisplayContext, TagWriter, Args)}.
	 * 
	 * @see #compile(KnowledgeBase, TLModel, SearchExpression)
	 */
	public static QueryExecutor interpret(KnowledgeBase knowledgeBase, TLModel model, SearchExpression expr) {
		return executor(knowledgeBase, model, resolve(model, expr));
	}

	/**
	 * Resolves symbols and attaches type information to the given expression.
	 */
	public static SearchExpression resolve(TLModel model, SearchExpression expr) {
		expr.visit(new DefResolver(), null);
		expr.visit(new TypeResolver(model), null);
		return expr;
	}

	/**
	 * Creates a {@link QueryExecutor}.
	 *
	 * @param expr
	 *        The search expression to execute.
	 * @return A new {@link QueryExecutor}.
	 */
	public static QueryExecutor executor(KnowledgeBase knowledgeBase, TLModel tlModel, SearchExpression expr) {
		return new DirectQueryExecutor(knowledgeBase, tlModel, expr);
	}

	private EvalContext context() {
		return context(null, null);
	}

	/**
	 * Creates a default rendering {@link EvalContext}.
	 */
	private EvalContext context(DisplayContext displayContext, TagWriter out) {
		return new EvalContext(getKnowledgeBase(), getTLModel(), displayContext, out);
	}

	/** The default {@link TLModel}. */
	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	/** The default {@link KnowledgeBase}. */
	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The {@link KnowledgeBase} for which the {@link #getSearch() search} was compiled.
	 * 
	 * @return Can be null, if the {@link SearchExpression} should not need a {@link KnowledgeBase}.
	 */
	protected abstract KnowledgeBase getKnowledgeBase();

	/** The {@link TLModel} for which the {@link #getSearch() search} was compiled. */
	protected abstract TLModel getTLModel();

	/**
	 * The {@link SearchExpression} being executed.
	 */
	public abstract SearchExpression getSearch();

	/**
	 * Executes the expression with the given single argument.
	 * <p>
	 * Uses the {@link QueryExecutor#context() evaluation context}.
	 * </p>
	 * 
	 * @param arg
	 *        The single argument value to pass to the expression evaluation.
	 * @return The result of the expression evaluation.
	 * 
	 * @see #executeWith(DisplayContext, TagWriter, Args)
	 */
	public Object execute(Object arg) {
		return executeWith(context(), Args.some(SearchExpression.normalizeValue(arg)));
	}

	/**
	 * Executes the expression with the given arguments.
	 * <p>
	 * Uses the {@link QueryExecutor#context() evaluation context}.
	 * </p>
	 * 
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The result of the expression.
	 */
	public Object execute(Object... args) {
		return executeWith(context(), Args.some(args));
	}

	/**
	 * Executes the expression with the given arguments.
	 * <p>
	 * Uses the {@link QueryExecutor#context() evaluation context}.
	 * </p>
	 * 
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The result of the expression.
	 */
	public Object executeWith(Args args) {
		return executeWith(context(), args);
	}

	/**
	 * Executes the expression with the given arguments.
	 * 
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The result of the expression.
	 */
	public Object executeWith(DisplayContext displayContext, TagWriter out, Args args) {
		return executeWith(context(displayContext, out), args);
	}

	/**
	 * Executes the expression with the given arguments.
	 * 
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The result of the expression.
	 */
	protected abstract Object executeWith(EvalContext definitions, Args args);

}