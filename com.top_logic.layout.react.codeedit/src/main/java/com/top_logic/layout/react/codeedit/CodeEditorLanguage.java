/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.codeedit;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.layout.form.values.edit.annotation.CodeLanguage;

/**
 * Source language offered by the {@link ReactCodeEditorControl}.
 *
 * <p>
 * Each constant carries the {@link #clientId() client language id} that selects the matching
 * {@code CodeMirror} language support in the {@code TLCodeEditor} React component.
 * </p>
 */
public enum CodeEditorLanguage {

	/** Extensible Markup Language. */
	XML("xml"),

	/** JavaScript Object Notation (with client-side syntax validation). */
	JSON("json"),

	/** Cascading Style Sheets. */
	CSS("css"),

	/** JavaScript. */
	JAVASCRIPT("javascript"),

	/** Hypertext Markup Language. */
	HTML("html"),

	/** Markdown. */
	MARKDOWN("markdown"),

	/** Plain text without syntax highlighting. */
	PLAIN("plain");

	private final String _clientId;

	CodeEditorLanguage(String clientId) {
		_clientId = clientId;
	}

	/**
	 * The {@link CodeEditorLanguage} for the given {@link CodeLanguage}.
	 */
	public static CodeEditorLanguage of(CodeLanguage language) {
		switch (language) {
			case PLAIN:
				return PLAIN;
			case XML:
				return XML;
			case HTML:
				return HTML;
			case JSON:
				return JSON;
			case CSS:
				return CSS;
			case JAVASCRIPT:
				return JAVASCRIPT;
			case MARKDOWN:
				return MARKDOWN;
		}
		throw new UnreachableAssertion("No such language: " + language);
	}

	/**
	 * The language id passed to the client, selecting the {@code CodeMirror} language support.
	 */
	public String clientId() {
		return _clientId;
	}

}
