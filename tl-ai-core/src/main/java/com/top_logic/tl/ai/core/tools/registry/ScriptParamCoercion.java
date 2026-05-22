/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.configured.ScriptParameter;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Translates an incoming named-argument map into the positional {@link Object} array expected by
 * a {@link QueryExecutor} compiled from a {@link com.top_logic.model.search.configured.ScriptConfiguration}.
 *
 * <p>
 * Orders arguments by the parameter declaration order so the {@code Define}-wrapped script binds
 * them to the right names; applies per-parameter default {@link Expr} values when absent; resolves
 * TID strings to {@link TLObject} wrappers when a parameter is typed as a {@link TLClass}.
 * </p>
 */
public final class ScriptParamCoercion {

	private final List<ScriptParameter> _parameters;

	private final Map<String, QueryExecutor> _defaultExecutors;

	/**
	 * @param parameters
	 *        Parameter declarations in their script-binding order (i.e. the order in which the
	 *        body's {@code Define} lambdas unwrap them).
	 */
	public ScriptParamCoercion(List<ScriptParameter> parameters) {
		// Aliased — callers (the factory) build the list once at boot and never mutate it.
		_parameters = parameters;
		_defaultExecutors = new HashMap<>();
		for (ScriptParameter p : parameters) {
			Expr def = p.getDefaultValue();
			if (!p.isMandatory() && def != null) {
				_defaultExecutors.put(p.getName(), QueryExecutor.compile(def));
			}
		}
	}

	/**
	 * Builds the positional argument array from a named map. Missing optional parameters with
	 * a configured default value are evaluated; missing mandatory parameters raise.
	 */
	public Object[] coerce(Map<String, Object> namedArgs) {
		Map<String, Object> args = namedArgs == null ? Map.of() : namedArgs;
		Object[] positional = new Object[_parameters.size()];
		for (int i = 0; i < _parameters.size(); i++) {
			ScriptParameter p = _parameters.get(i);
			Object value;
			if (args.containsKey(p.getName())) {
				value = args.get(p.getName());
			} else if (_defaultExecutors.containsKey(p.getName())) {
				value = _defaultExecutors.get(p.getName()).execute(new Object[0]);
			} else if (p.isMandatory()) {
				throw new IllegalArgumentException(
					"Missing required parameter '" + p.getName() + "'");
			} else {
				value = null;
			}
			positional[i] = resolveIfReference(p, value);
		}
		return positional;
	}

	private static Object resolveIfReference(ScriptParameter p, Object value) {
		if (value == null) {
			return null;
		}
		TLType type = p.getType().resolveType();
		if (!(type instanceof TLClass)) {
			return value;
		}
		// JSON arrays deserialise as List; Object[] and Stream are not produced by the LLM input path.
		if (p.isMultiple() && value instanceof Iterable<?> it) {
			List<TLObject> resolved = new ArrayList<>();
			for (Object element : it) {
				resolved.add(resolveTid(element));
			}
			return resolved;
		}
		return resolveTid(value);
	}

	/**
	 * Resolves a TID into a {@link TLObject} wrapper.
	 *
	 * @param raw
	 *        Expected to be a {@code String} TID in the format {@code "TypeName:id"}. Any other type
	 *        is converted via {@code String.valueOf} and must still produce a valid TID, otherwise
	 *        the lookup fails fast.
	 */
	private static TLObject resolveTid(Object raw) {
		String tid = String.valueOf(raw);
		String[] split = tid.split(":", 2);
		if (split.length != 2) {
			throw new IllegalArgumentException(
				"Invalid TID format. Expected 'TypeName:id', got: " + tid);
		}
		TLID id = IdentifierUtil.fromExternalForm(split[1]);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		KnowledgeObject ko = kb.getKnowledgeObject(split[0], id);
		if (ko == null) {
			throw new IllegalArgumentException("Object with TID '" + tid + "' not found");
		}
		return WrapperFactory.getWrapper(ko);
	}

}
