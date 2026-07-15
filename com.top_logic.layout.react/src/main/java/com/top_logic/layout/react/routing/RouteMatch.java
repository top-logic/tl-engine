/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.util.Collections;
import java.util.Map;

/**
 * Result of a successful {@link RoutePattern#match(String)} call.
 *
 * <p>
 * Captures the matched pattern, the associated item ID, extracted path parameters, and any
 * remaining (unconsumed) path segments.
 * </p>
 *
 * @see RoutePattern#match(String)
 */
public final class RouteMatch {

	private final RoutePattern _pattern;

	private final String _itemId;

	private final Map<String, String> _params;

	private final String _remainingPath;

	/**
	 * Creates a new {@link RouteMatch}.
	 *
	 * @param pattern
	 *        The pattern that produced this match.
	 * @param itemId
	 *        The logical item identifier associated with the matched pattern.
	 * @param params
	 *        Extracted path parameters (parameter name to value).
	 * @param remainingPath
	 *        Unconsumed trailing path segments, or empty string if the pattern consumed the entire
	 *        path.
	 */
	RouteMatch(RoutePattern pattern, String itemId, Map<String, String> params, String remainingPath) {
		_pattern = pattern;
		_itemId = itemId;
		_params = Collections.unmodifiableMap(params);
		_remainingPath = remainingPath;
	}

	/**
	 * The pattern that produced this match.
	 */
	public RoutePattern pattern() {
		return _pattern;
	}

	/**
	 * The logical item identifier associated with the matched pattern.
	 */
	public String itemId() {
		return _itemId;
	}

	/**
	 * Extracted path parameters (parameter name to value).
	 *
	 * <p>
	 * The returned map is unmodifiable.
	 * </p>
	 */
	public Map<String, String> params() {
		return _params;
	}

	/**
	 * Unconsumed trailing path segments after the matched portion.
	 *
	 * <p>
	 * Returns an empty string if the pattern consumed the entire path.
	 * </p>
	 */
	public String remainingPath() {
		return _remainingPath;
	}

}
