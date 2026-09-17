/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The light or dark character of a theme's appearance.
 *
 * <p>
 * A theme announces its scheme to the browser, so that the parts of the page the browser renders
 * itself - scrollbars, form controls, the canvas behind the document - match the theme. The
 * operating system expresses its appearance preference in the same two schemes, so a theme can be
 * nominated as the answer to that preference.
 * </p>
 */
public enum ColorScheme implements ExternallyNamed {

	/** A light appearance: dark content on a light background. */
	LIGHT("light"),

	/** A dark appearance: light content on a dark background. */
	DARK("dark"),

	;

	private final String _keyword;

	private ColorScheme(String keyword) {
		_keyword = keyword;
	}

	/**
	 * The CSS keyword naming this scheme, both as a value of the {@code color-scheme} property and
	 * as an operand of the {@code prefers-color-scheme} media query.
	 */
	public String cssKeyword() {
		return _keyword;
	}

	@Override
	public String getExternalName() {
		return _keyword;
	}

}
