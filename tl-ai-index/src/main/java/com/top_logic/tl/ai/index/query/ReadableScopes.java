/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;

/**
 * The scope objects a reader may search in.
 *
 * <p>
 * The application's {@code readable-scopes} script answers the candidates. It runs in the reader's
 * own interaction - never in a system context - so a script that asks for the current user gets the
 * reader. Its result is filtered by the reader's {@link ModelSecurity}, the same rights every hit is
 * checked against; the script itself does not filter, so the scopes never depend on two
 * implementations of the rights agreeing.
 * </p>
 *
 * <p>
 * A scope identifier does not change when somebody joins or leaves a project; membership is a fact of
 * the project, not of the indexed objects. That is why the index can hold scopes and the rights are
 * asked at query time.
 * </p>
 */
public final class ReadableScopes {

	private final QueryExecutor _script;

	/**
	 * Creates {@link ReadableScopes} from the configured script; <code>null</code> for none.
	 */
	public ReadableScopes(Expr script) {
		if (script == null) {
			_script = null;
		} else {
			_script = QueryExecutor.compile(script);
			_script.disableSecurity();
		}
	}

	/**
	 * The identifiers of the scope objects the reader may read; <code>null</code> when no script is
	 * configured.
	 */
	public Set<String> of(ModelSecurity security) {
		if (_script == null) {
			return null;
		}
		List<TLObject> candidates = new ArrayList<>();
		collect(candidates, _script.execute());
		Set<String> result = new LinkedHashSet<>();
		for (TLObject scope : security.filterReadable(candidates)) {
			String id = ReadFilter.idOf(scope);
			if (id != null) {
				result.add(id);
			}
		}
		return result;
	}

	private static void collect(List<TLObject> result, Object value) {
		if (value instanceof TLObject object) {
			result.add(object);
		} else if (value instanceof Collection<?> many) {
			for (Object element : many) {
				collect(result, element);
			}
		}
	}

}
