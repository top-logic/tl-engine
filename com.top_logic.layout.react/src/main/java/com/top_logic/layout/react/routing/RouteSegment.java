/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.util.Collections;
import java.util.Map;

/**
 * A concrete URL segment produced by an active route.
 *
 * <p>
 * Combines the path portion with optional query parameters. This is used to represent the
 * contribution of a single routing participant to the overall URL.
 * </p>
 */
public final class RouteSegment {

	private final String _path;

	private final Map<String, String> _queryParams;

	/**
	 * Creates a new {@link RouteSegment}.
	 *
	 * @param path
	 *        The path portion of this segment (without leading or trailing slashes).
	 * @param queryParams
	 *        Optional query parameters contributed by this segment.
	 */
	public RouteSegment(String path, Map<String, String> queryParams) {
		_path = path;
		_queryParams = Collections.unmodifiableMap(queryParams);
	}

	/**
	 * Creates a new {@link RouteSegment} with no query parameters.
	 *
	 * @param path
	 *        The path portion of this segment (without leading or trailing slashes).
	 */
	public RouteSegment(String path) {
		this(path, Collections.emptyMap());
	}

	/**
	 * The path portion of this segment.
	 */
	public String path() {
		return _path;
	}

	/**
	 * Optional query parameters contributed by this segment.
	 *
	 * <p>
	 * The returned map is unmodifiable.
	 * </p>
	 */
	public Map<String, String> queryParams() {
		return _queryParams;
	}

}
