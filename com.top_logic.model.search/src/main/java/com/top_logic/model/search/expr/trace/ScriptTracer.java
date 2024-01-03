/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.trace;

import com.top_logic.basic.col.Sink;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.Pointer;
import com.top_logic.util.model.ModelService;

/**
 * Executor of a <i>TL-Script</i> expression that traces all accesses done by the script.
 * 
 * <p>
 * Tracing the evaluation can be used to observe a script result for change. If the script has no
 * side-effects and does only use deterministic functions, the script result can only change, if the
 * model values it accesses change. By observing all model values a script accesses one can observe
 * the script result for change.
 * </p>
 * 
 * @see #compile(Expr)
 * @see #execute(KnowledgeBase, Sink, Object...)
 * @see QueryExecutor
 */
public class ScriptTracer {

	/**
	 * Creates a {@link ScriptTracer} for the given expression.
	 */
	public static ScriptTracer compile(Expr expr) {
		return compile(ModelService.getApplicationModel(), expr);
	}

	/**
	 * Creates a {@link ScriptTracer} for the given expression.
	 * 
	 * @param model
	 *        The context model, the script is compiled for.
	 */
	public static ScriptTracer compile(TLModel model, Expr expr) {
		return new ScriptTracer(model, expr);
	}

	private SearchExpression _debugExpr;

	private TLModel _model;

	/**
	 * Creates a {@link ScriptTracer}.
	 */
	private ScriptTracer(TLModel model, Expr expr) {
		_model = model;

		SearchExpression rawExpr = SearchBuilder.toSearchExpression(_model, expr);
		_debugExpr = QueryExecutor.resolve(_model, rawExpr.visit(TracingAccessRewriter.INSTANCE, null));
	}

	/**
	 * Evaluates the the script of this {@link ScriptTracer} and reports all accesses to the given
	 * {@link Sink}.
	 * 
	 * @param trace
	 *        The callback to report all model accesses during evaluation.
	 * @param args
	 *        The arguments to the script.
	 * @return The evaluation result returned by the script.
	 */
	public Object execute(Sink<Pointer> trace, Object... args) {
		return execute(PersistencyLayer.getKnowledgeBase(), trace, args);
	}

	/**
	 * Evaluates the the script of this {@link ScriptTracer} and reports all accesses to the given
	 * {@link Sink}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} of the context.
	 * @param trace
	 *        The callback to report all model accesses during evaluation.
	 * @param args
	 *        The arguments to the script.
	 * @return The evaluation result returned by the script.
	 */
	public Object execute(KnowledgeBase kb, Sink<Pointer> trace, Object... args) {
		return _debugExpr.evalWith(ScriptTracer.tracingContext(kb, _model, trace), Args.some(args));
	}

	private static EvalContext tracingContext(KnowledgeBase kb, TLModel model, Sink<Pointer> trace) {
		EvalContext context = new EvalContext(kb, model, null, null);
		context.defineVar(TracingAccessRewriter.TRACE, trace);
		return context;
	}

}
