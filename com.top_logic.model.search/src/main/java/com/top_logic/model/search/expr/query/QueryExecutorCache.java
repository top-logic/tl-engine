/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.query;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;

import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * The {@link QueryExecutor}s of TL-Script expressions held in configurations, compiled once per
 * expression and shared by all callers asking for the same expression.
 *
 * <p>
 * For an expression of a configuration that lives longer than its uses, such as the expression of
 * a model annotation evaluated for each field or each table cell showing an attribute. Expressions
 * are compared by identity (configuration items do not override {@link Object#equals(Object)}), so
 * a changed configuration yields a freshly compiled executor. An expression is held weakly and its
 * executor softly, so an entry is dropped once the configuration is no longer used.
 * </p>
 *
 * <p>
 * The executors of a cache are all {@link QueryExecutor#disableSecurity() without security} or all
 * with it, as the cache is created.
 * </p>
 */
public final class QueryExecutorCache {

	private final boolean _secured;

	private final Map<Expr, SoftReference<QueryExecutor>> _executors = new WeakHashMap<>();

	/**
	 * Creates a {@link QueryExecutorCache}.
	 *
	 * @param secured
	 *        Whether the executors apply security; {@code false} {@link QueryExecutor#disableSecurity()
	 *        switches it off} for each of them.
	 */
	public QueryExecutorCache(boolean secured) {
		_secured = secured;
	}

	/**
	 * The compiled executor of the given expression, compiled on the first request.
	 *
	 * @param expr
	 *        The expression to execute, or {@code null} for none.
	 * @return The executor of the expression, {@code null} for no expression.
	 */
	public QueryExecutor get(Expr expr) {
		if (expr == null) {
			return null;
		}
		synchronized (_executors) {
			SoftReference<QueryExecutor> reference = _executors.get(expr);
			QueryExecutor executor = reference == null ? null : reference.get();
			if (executor == null) {
				executor = QueryExecutor.compile(expr);
				if (!_secured) {
					executor.disableSecurity();
				}
				_executors.put(expr, new SoftReference<>(executor));
			}
			return executor;
		}
	}

}
