/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.trace;

import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.Sink;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.DynamicGet;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.interpreter.Rewriter;
import com.top_logic.model.util.Pointer;

/**
 * {@link Rewriter} replacing expressions accessing model properties with those that create an
 * access trace in the context variable {@link TracingAccessRewriter#TRACE}.
 */
final class TracingAccessRewriter extends Rewriter<Void> {

	/**
	 * Singleton {@link TracingAccessRewriter} instance.
	 */
	public static final TracingAccessRewriter INSTANCE =
		new TracingAccessRewriter();
	/**
	 * Variable identifier in the {@link EvalContext} that contains all accessed model references
	 * during an evaluation.
	 */
	public static final NamedConstant TRACE = new NamedConstant("trace");

	private TracingAccessRewriter() {
		// Singleton constructor.
	}

	@Override
	public SearchExpression visitAccess(Access expr, Void arg) {
		SearchExpression self = descendPart(expr, arg, expr.getSelf());
		return new TracingAccess(self, expr.getPart());
	}

	@Override
	public SearchExpression visitGenericMethod(GenericMethod expr, Void arg) {
		if (expr instanceof DynamicGet) {
			SearchExpression self = descendPart(expr, arg, expr.getSelf());
			List<SearchExpression> argumentsList = descendParts(expr, arg, expr.getArguments());
			SearchExpression[] arguments = argumentsList.toArray(new SearchExpression[0]);
			return new TracingDynamicGet(expr.getName(), self, arguments);
		} else {
			return super.visitGenericMethod(expr, arg);
		}
	}

	public static void traceAccess(EvalContext definitions, TLObject self, TLStructuredTypePart part) {
		@SuppressWarnings("unchecked")
		Sink<Pointer> trace = (Sink<Pointer>) definitions.getVar(TRACE);
		trace.add(Pointer.create(self, part));
	}
}