/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.Definition;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.visit.DefaultDescendingVisitor;

/**
 * Inline transformation that inlines {@link Call} expressions with a single usage of their argument
 * variable.
 * 
 * <p>
 * Also removes unused local variables.
 * </p>
 * 
 * <p>
 * Precondition: {@link DefResolver}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InlineLocals {

	/**
	 * Transforms the given expression.
	 * 
	 * @param expr
	 *        The expression to transform. Must no longer be used after this method returns, since
	 *        parts may have been reused in the result.
	 * @return The transformed expression.
	 */
	public static SearchExpression transform(SearchExpression expr) {
		Usage usage = new Usage();
		expr.visit(usage, null);
		return expr.visit(Inline.INSTANCE, usage);
	}

	static final class Usage extends DefaultDescendingVisitor<Void, Void> {

		private final Map<Definition, Integer> _data = new HashMap<>();

		private final Map<NamedConstant, SearchExpression> _values = new HashMap<>();

		public void add(Definition definition) {
			Integer clash = _data.put(definition, 1);
			if (clash != null) {
				_data.put(definition, clash + 1);
			}
		}

		@Override
		public Void visitVar(Var expr, Void arg) {
			add(expr.getDef());
			return super.visitVar(expr, arg);
		}

		/**
		 * The function argument is used at least once.
		 */
		public boolean canInline(Lambda function) {
			return usageCount(function) <= 1;
		}

		/**
		 * The function argument is not used at all.
		 */
		public boolean isArgumentUnused(Lambda expr) {
			return usageCount(expr) == 0;
		}

		private int usageCount(Lambda expr) {
			Integer result = _data.get(expr);
			if (result == null) {
				return 0;
			}
			return result;
		}

		public void bind(NamedConstant key, SearchExpression value) {
			_values.put(key, value);
		}

		public void unbind(NamedConstant key) {
			_values.remove(key);
		}

		public boolean canInline(Var expr) {
			return _values.containsKey(expr.getKey());
		}

		public SearchExpression getValue(NamedConstant key) {
			SearchExpression result = _values.get(key);
			if (result == null) {
				throw new IllegalArgumentException("Unbound variable " + key + ".");
			}
			return result;
		}

	}

	static final class Inline extends Rewriter<Usage> {

		/**
		 * Singleton {@link InlineLocals.Inline} instance.
		 */
		public static final Inline INSTANCE = new Inline();

		private Inline() {
			// Singleton constructor.
		}

		@Override
		public SearchExpression visitCall(Call expr, Usage arg) {
			SearchExpression function = expr.getFunction();
			if (function instanceof Lambda) {
				Lambda lambda = (Lambda) function;
				if (arg.canInline(lambda)) {
					NamedConstant key = lambda.getKey();
					arg.bind(key, expr.getArgument());
					SearchExpression result = lambda.getBody().visit(this, arg);
					arg.unbind(key);
					return result;
				} else {
					return super.visitCall(expr, arg);
				}
			} else {
				// Drop unused argument.
				return function.visit(this, arg);
			}
		}

		@Override
		public SearchExpression visitLambda(Lambda expr, Usage arg) {
			if (arg.isArgumentUnused(expr)) {
				return expr.getBody().visit(this, arg);
			} else {
				return super.visitLambda(expr, arg);
			}
		}

		@Override
		public SearchExpression visitVar(Var expr, Usage arg) {
			if (arg.canInline(expr)) {
				return arg.getValue(expr.getDef().getKey()).visit(this, arg);
			} else {
				return super.visitVar(expr, arg);
			}
		}

	}

}
