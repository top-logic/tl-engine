/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

/**
 * A resolved client resource: a context-relative URL plus an optional cache-busting version suffix.
 *
 * @param url
 *        The context-relative resource path without the context path prefix, e.g.
 *        {@code "/script/tl-react-bridge.js"}.
 * @param version
 *        A cache-busting query suffix starting with {@code "?"} (e.g. {@code "?v=42"}), or the empty
 *        string when no version is attached.
 */
public record ResourceRef(String url, String version) {

	/**
	 * The full context-relative URL including the {@link #version()} suffix.
	 */
	public String full() {
		return url + version;
	}

}
