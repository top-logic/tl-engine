/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.struct;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.util.error.TopLogicException;

/**
 * Index of property names for constructing multiple {@link StructValue}s with the same property set.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructType extends AbstractList<Object> {

	private final Object[] _keys;

	private final Map<Object, Integer> _index;

	/**
	 * Creates a {@link StructType}.
	 * 
	 * @param expr
	 *        The expression defining this type (for error reporting only).
	 * @param keys
	 *        The names of the properties.
	 */
	public StructType(SearchExpression expr, Object[] keys) {
		_keys = keys;
		_index = createIndex(expr, keys);
	}

	private static Map<Object, Integer> createIndex(SearchExpression expr, Object[] keys) {
		Map<Object, Integer> result = new LinkedHashMap<>();
		for (int n = 0, cnt = keys.length; n < cnt; n++) {
			Object key = keys[n];
			Integer idx = Integer.valueOf(n);
			Integer clash = result.put(key, idx);
			if (clash != null) {
				throw new TopLogicException(
					I18NConstants.ERROR_DUPLICATE_KEYS__KEY_IDX1_IDX2_EXPR.fill(key, clash, idx, expr));
			}
		}
		return result;
	}

	final Integer indexOrNull(Object key) {
		return _index.get(key);
	}

	/**
	 * Whether this {@link StructType} defines a property with the given name.
	 */
	public boolean hasKey(Object key) {
		return _index.get(key) != null;
	}

	/**
	 * {@link Set} of defined property names.
	 */
	public Set<Object> getKeySet() {
		return Collections.unmodifiableSet(_index.keySet());
	}

	/**
	 * The property at the given index.
	 */
	@Override
	public Object get(int index) {
		return _keys[index];
	}

	/**
	 * Number of defined properties.
	 */
	@Override
	public int size() {
		return _keys.length;
	}

	@Override
	public String toString() {
		return Arrays.asList(_keys).stream().map(Objects::toString).collect(Collectors.joining(", ", "struct(", ")"));
	}

}
