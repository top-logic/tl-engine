/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;

/**
 * The language a source code text of a configuration property is written in.
 *
 * <p>
 * A form edits a text in one of these languages with a code editor that highlights the syntax of
 * the language, if a code editor is available.
 * </p>
 *
 * @see Code
 */
public enum CodeLanguage implements ExternallyNamed {

	/** Text without a language of its own. */
	@Label("Plain text")
	PLAIN("plain"),

	/** Extensible Markup Language. */
	XML("xml"),

	/** Hypertext Markup Language. */
	HTML("html"),

	/** JavaScript Object Notation. */
	JSON("json"),

	/** Cascading Style Sheets. */
	@Label("CSS")
	CSS("css"),

	/** JavaScript. */
	JAVASCRIPT("javascript"),

	/** Markdown. */
	MARKDOWN("markdown"),

	;

	private final String _externalName;

	private CodeLanguage(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
