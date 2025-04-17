/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.haumacher.msgbuf.data.ReflectiveDataObject;

/**
 * Adapter interface of a {@link ReflectiveDataObject} to {@link Map}.
 */
public interface MapLike extends Map<String, Object> {

	ReflectiveDataObject self();

	@Override
	default Object get(Object key) {
		return self().get(key.toString());
	}

	@Override
	default void clear() {
		for (String prop : self().properties()) {
			self().set(prop, null);
		}
	}

	@Override
	default boolean containsKey(Object key) {
		return self().properties().contains(key);
	}

	@Override
	default boolean containsValue(Object value) {
		return self().properties().stream().map(k -> self().get(k)).filter(v -> Objects.equals(v, value)).findFirst()
			.isPresent();
	}

	@Override
	default boolean isEmpty() {
		return self().properties().isEmpty();
	}

	@Override
	default Object remove(Object key) {
		Object before = self().get(key.toString());
		self().set(key.toString(), null);
		return before;
	}

	@Override
	default int size() {
		return self().properties().size();
	}

	@Override
	default Set<String> keySet() {
		return new HashSet<>(self().properties());
	}

	@Override
	default Collection<Object> values() {
		return self().properties().stream().map(k -> self().get(k)).collect(Collectors.toList());
	}

	@Override
	default void putAll(Map<? extends String, ? extends Object> m) {
		for (Entry<? extends String, ? extends Object> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	default Object put(String key, Object value) {
		Object before = self().get(key);
		self().set(key, value);
		return before;
	}

	@Override
	default Set<Entry<String, Object>> entrySet() {
		return self().properties().stream().collect(Collectors.toMap(k -> k, k -> self().get(k))).entrySet();
	}

}
