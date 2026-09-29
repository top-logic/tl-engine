/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.query;

import com.top_logic.basic.annotation.FrameworkInternal;
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
 * <p>
 * A {@link QueryExecutor} is the interface through which an application executes a
 * <i>TL-Script</i>. It therefore secures the result of an execution: unless the security is
 * {@link #disableSecurity() switched off}, objects that the current user is not allowed to read are
 * removed from the result, see {@link #executeWith(EvalContext, Args)}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class QueryExecutor {

	private boolean _securityEnabled = true;

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
	 * @param kb
	 *        The {@link KnowledgeBase} to execute in.
	 * @param model
	 *        The {@link TLModel} the given search is formulated in.
	 * @param expr
	 *        The textual XML representation of the search, or <code>null</code>.
	 * @return The {@link QueryExecutor} that can execute the search, or <code>null</code>, if the
	 *         argument was null.
	 * 
	 * @see #compile(KnowledgeBase, TLModel, SearchExpression)
	 */
	public static QueryExecutor compileOptional(KnowledgeBase kb, TLModel model, Expr expr) {
		if (expr == null) {
			return null;
		}
		return compile(kb, model, expr);
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

	/**
	 * Creates a default {@link EvalContext} without {@link DisplayContext} and output.
	 */
	public EvalContext context() {
		return context(null, null);
	}

	/**
	 * Creates a default rendering {@link EvalContext}.
	 */
	public EvalContext context(DisplayContext displayContext, TagWriter out) {
		return context(false, displayContext, out);
	}

	/**
	 * Creates a default rendering {@link EvalContext}.
	 * 
	 * @param interactive
	 *        See {@link EvalContext#isInteractive()}.
	 */
	public EvalContext context(boolean interactive, DisplayContext displayContext, TagWriter out) {
		return new EvalContext(interactive, getKnowledgeBase(), getTLModel(), displayContext, out);
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
	 * Disables the security check for the {@link #getSearch() compiled expression}.
	 *
	 * <p>
	 * By default, every executed expression applies security, i.e. elements that the current user
	 * is not allowed to see are removed from the (intermediate) results, modifying operations
	 * require the corresponding write permission, and the {@link #executeWith(EvalContext, Args)
	 * result} of an execution is filtered for the current user's read rights. Calling this method
	 * permanently switches security off for this {@link QueryExecutor}. It must therefore only be
	 * used for internal queries that must not be subject to the user's access rights.
	 * </p>
	 */
	public final void disableSecurity() {
		_securityEnabled = false;
		internalDisableSecurity();
	}

	/**
	 * Implementation of {@link #disableSecurity()} switching the security check off in the
	 * {@link #getSearch() executed expression}.
	 */
	protected abstract void internalDisableSecurity();

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
	public final Object execute(Object arg) {
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
	public final Object execute(Object... args) {
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
	public final Object executeWith(Args args) {
		return executeWith(context(), args);
	}

	/**
	 * Executes the expression with the given arguments.
	 * 
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The result of the expression.
	 */
	public final Object executeWith(DisplayContext displayContext, TagWriter out, Args args) {
		return executeWith(context(displayContext, out), args);
	}

	/**
	 * Executes the expression with the given arguments.
	 *
	 * <p>
	 * Unless the security is {@link #disableSecurity() disabled}, the result is filtered for the
	 * current user's read rights, see {@link SearchExpression#filterSecurity(Object)}. A
	 * {@link QueryExecutor} is the interface through which an application executes a
	 * <i>TL-Script</i>, therefore its result must not contain objects that the current user is not
	 * allowed to read - the caller does not have to remember to secure it.
	 * </p>
	 *
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The result of the expression.
	 *
	 * @see #executeIntermediate(EvalContext, Args)
	 *      Execution from within an ongoing evaluation, whose result is an intermediate one.
	 */
	public final Object executeWith(EvalContext definitions, Args args) {
		Object result = internalExecuteWith(definitions, args);
		if (_securityEnabled) {
			return SearchExpression.filterSecurity(result);
		}
		return result;
	}

	/**
	 * Executes the expression with the given arguments <em>without</em> filtering the result for the
	 * current user's read rights.
	 *
	 * <p>
	 * This is the execution of an expression whose result is an <em>intermediate</em> value of a
	 * larger operation and is not handed to the user: a configured function called from another
	 * script, or a step of a technical pipeline such as the XML importer resolving the object to link
	 * to. Such a result must not be filtered, since an object that the operation navigates through,
	 * links to, or only counts must not be dropped just because the user may not read it. Securing
	 * the value that the operation finally delivers is the job of that operation - for a script, of
	 * the {@link #executeWith(EvalContext, Args) execution} that started it.
	 * </p>
	 *
	 * <p>
	 * The security of the executed expression itself (denying access to the attributes of an object
	 * the user must not read, requiring write permissions for modifications) is unaffected: it is
	 * part of the {@link #getSearch() expression} and applies here as well, unless it was
	 * {@link #disableSecurity() switched off}.
	 * </p>
	 *
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The unfiltered result of the expression.
	 */
	@FrameworkInternal
	public final Object executeIntermediate(EvalContext definitions, Args args) {
		return internalExecuteWith(definitions, args);
	}

	/**
	 * Executes the expression with the given arguments <em>without</em> filtering the result for the
	 * current user's read rights.
	 * <p>
	 * Uses the {@link QueryExecutor#context() evaluation context}.
	 * </p>
	 *
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The unfiltered result of the expression.
	 *
	 * @see #executeIntermediate(EvalContext, Args)
	 */
	@FrameworkInternal
	public final Object executeIntermediate(Object... args) {
		return executeIntermediate(context(), Args.some(args));
	}

	/**
	 * Implementation of {@link #executeWith(EvalContext, Args)} evaluating the
	 * {@link #getSearch() expression}.
	 *
	 * @param args
	 *        The arguments to pass to the expression evaluation.
	 * @return The raw (unfiltered) result of the expression.
	 */
	protected abstract Object internalExecuteWith(EvalContext definitions, Args args);

}