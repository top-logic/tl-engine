/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * How thoroughly a {@link WebSearchEngine} should search.
 */
public enum SearchDepth {

	/** Faster, shallower search. */
	BASIC("basic"),

	/** Slower, more thorough search. */
	ADVANCED("advanced");

	private final String _apiValue;

	SearchDepth(String apiValue) {
		_apiValue = apiValue;
	}

	/**
	 * The token expected by the provider API for this depth (e.g. {@code "basic"}).
	 */
	public String apiValue() {
		return _apiValue;
	}
}
