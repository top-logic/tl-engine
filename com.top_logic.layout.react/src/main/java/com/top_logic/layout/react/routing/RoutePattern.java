/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compiled URL route pattern that can match paths and produce concrete path strings.
 *
 * <p>
 * A pattern string uses a leading slash followed by static or parameterized segments:
 * </p>
 * <ul>
 * <li>{@code /explore} - matches the literal path segment "explore"</li>
 * <li>{@code /property/:estateId} - matches "property" followed by any segment, captured as
 * "estateId"</li>
 * <li>{@code /*} - wildcard that matches any path</li>
 * </ul>
 *
 * <p>
 * If the input path has more segments than the pattern, the remaining segments are available via
 * {@link RouteMatch#remainingPath()}.
 * </p>
 *
 * @see #compile(String, String)
 * @see #match(String)
 * @see #produce(Map)
 */
public final class RoutePattern {

	private static final String WILDCARD = "*";

	private static final char PARAM_PREFIX = ':';

	private final String _patternString;

	private final String _itemId;

	private final boolean _wildcard;

	private final List<String> _segments;

	private RoutePattern(String patternString, String itemId, boolean wildcard, List<String> segments) {
		_patternString = patternString;
		_itemId = itemId;
		_wildcard = wildcard;
		_segments = segments;
	}

	/**
	 * Compiles a pattern string into a {@link RoutePattern}.
	 *
	 * @param pattern
	 *        The pattern string (e.g., {@code /property/:id} or {@code /*}).
	 * @param itemId
	 *        A logical identifier for the route item associated with this pattern.
	 * @return A compiled pattern ready for matching.
	 * @throws IllegalArgumentException
	 *         If the pattern is null, empty, or does not start with {@code /}.
	 */
	public static RoutePattern compile(String pattern, String itemId) {
		if (pattern == null || pattern.isEmpty()) {
			throw new IllegalArgumentException("Pattern must not be null or empty.");
		}
		if (pattern.charAt(0) != '/') {
			throw new IllegalArgumentException("Pattern must start with '/': " + pattern);
		}

		String body = pattern.substring(1);

		if (WILDCARD.equals(body)) {
			return new RoutePattern(pattern, itemId, true, List.of());
		}

		List<String> segments = splitSegments(body);
		return new RoutePattern(pattern, itemId, false, segments);
	}

	/**
	 * Attempts to match the given path against this pattern.
	 *
	 * <p>
	 * The path must NOT have a leading slash. If the path has more segments than the pattern, the
	 * extra segments are captured in {@link RouteMatch#remainingPath()}.
	 * </p>
	 *
	 * @param path
	 *        The URL path to match (without leading slash).
	 * @return A {@link RouteMatch} if the path matches, or {@code null} if it does not.
	 */
	public RouteMatch match(String path) {
		if (path == null) {
			return null;
		}

		if (_wildcard) {
			return new RouteMatch(this, _itemId, Map.of(), path);
		}

		List<String> pathSegments = splitSegments(path);

		if (pathSegments.size() < _segments.size()) {
			return null;
		}

		Map<String, String> params = new HashMap<>();

		for (int i = 0; i < _segments.size(); i++) {
			String patternSeg = _segments.get(i);
			String pathSeg = pathSegments.get(i);

			if (patternSeg.charAt(0) == PARAM_PREFIX) {
				String paramName = patternSeg.substring(1);
				params.put(paramName, pathSeg);
			} else {
				if (!patternSeg.equals(pathSeg)) {
					return null;
				}
			}
		}

		String remaining = buildRemaining(pathSegments, _segments.size());
		return new RouteMatch(this, _itemId, params, remaining);
	}

	/**
	 * Produces a concrete path string by filling in parameter placeholders with the given values.
	 *
	 * @param params
	 *        Parameter name to value mapping.
	 * @return The produced path string (without leading slash).
	 * @throws IllegalArgumentException
	 *         If a required parameter is missing from the map.
	 */
	public String produce(Map<String, String> params) {
		if (_wildcard) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _segments.size(); i++) {
			if (i > 0) {
				sb.append('/');
			}
			String segment = _segments.get(i);
			if (segment.charAt(0) == PARAM_PREFIX) {
				String paramName = segment.substring(1);
				String value = params.get(paramName);
				if (value == null) {
					throw new IllegalArgumentException("Missing parameter: " + paramName);
				}
				sb.append(value);
			} else {
				sb.append(segment);
			}
		}
		return sb.toString();
	}

	/**
	 * The original pattern string.
	 */
	public String patternString() {
		return _patternString;
	}

	/**
	 * The logical item identifier associated with this pattern.
	 */
	public String itemId() {
		return _itemId;
	}

	@Override
	public String toString() {
		return _patternString;
	}

	private static List<String> splitSegments(String path) {
		if (path.isEmpty()) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		int start = 0;
		int len = path.length();
		while (start < len) {
			int slash = path.indexOf('/', start);
			if (slash < 0) {
				result.add(path.substring(start));
				break;
			}
			result.add(path.substring(start, slash));
			start = slash + 1;
		}
		return result;
	}

	private static String buildRemaining(List<String> pathSegments, int consumed) {
		if (consumed >= pathSegments.size()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = consumed; i < pathSegments.size(); i++) {
			if (i > consumed) {
				sb.append('/');
			}
			sb.append(pathSegments.get(i));
		}
		return sb.toString();
	}

}
