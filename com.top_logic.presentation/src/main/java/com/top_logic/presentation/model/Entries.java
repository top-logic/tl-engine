/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The entries of a dictionary in the data representation of a presentation description.
 *
 * <p>
 * A reader requests each entry it supports by {@link #get(String)}, also entries that are not
 * given. Afterwards, {@link #checkComplete()} reports a given entry that has not been requested
 * as unknown, e.g. a misspelled key.
 * </p>
 */
public final class Entries {

	private final Map<?, ?> _map;

	private final String _path;

	private final Set<String> _known = new LinkedHashSet<>();

	/**
	 * Creates {@link Entries}.
	 *
	 * @param map
	 *        The dictionary.
	 * @param path
	 *        The path of the dictionary in the description.
	 */
	public Entries(Map<?, ?> map, String path) {
		_map = map;
		_path = path;
	}

	/**
	 * The path of the dictionary in the description.
	 */
	public String path() {
		return _path;
	}

	/**
	 * Whether the dictionary contains the given key, without requesting the entry.
	 */
	public boolean contains(String key) {
		return _map.get(key) != null;
	}

	/**
	 * Whether the entry with the given key is given.
	 *
	 * <p>
	 * The key counts as supported.
	 * </p>
	 */
	public boolean has(String key) {
		_known.add(key);
		return _map.get(key) != null;
	}

	/**
	 * The value of the entry with the given key, a missing {@link Value} if the entry is not given.
	 *
	 * <p>
	 * The key counts as supported.
	 * </p>
	 */
	public Value get(String key) {
		_known.add(key);
		return new Value(_map.get(key), childPath(key));
	}

	/**
	 * The value of the entry with the given key, which must be given.
	 *
	 * @param expected
	 *        Description of the expected value for the error, if the entry is missing.
	 */
	public Value require(String key, ResKey expected) {
		return get(key).require(expected);
	}

	/**
	 * The values of all entries.
	 *
	 * <p>
	 * All keys count as supported.
	 * </p>
	 */
	public Map<String, Value> all() {
		Map<String, Value> result = new LinkedHashMap<>();
		for (Object key : _map.keySet()) {
			String name = String.valueOf(key);
			result.put(name, get(name));
		}
		return result;
	}

	/**
	 * Creates an error reporting that the given entries must not be given together.
	 */
	public TopLogicException exclusive(String key, String other) {
		return new TopLogicException(I18NConstants.ERROR_EXCLUSIVE_ENTRIES__PATH_OTHER.fill(childPath(key), other));
	}

	/**
	 * Fails, if an entry is given that has not been requested.
	 */
	public void checkComplete() {
		for (Map.Entry<?, ?> entry : _map.entrySet()) {
			String key = String.valueOf(entry.getKey());
			if (entry.getValue() != null && !_known.contains(key)) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_ENTRY__PATH_KEYS.fill(childPath(key), String.join(", ", _known)));
			}
		}
	}

	private String childPath(String key) {
		return _path.isEmpty() ? key : _path + "." + key;
	}

}
