/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

/**
 * Base class for template nodes parsed from a HTML template with embedded expressions.
 */
public abstract class TemplateNode {

	private final int _line;

	private final int _column;

	/**
	 * Creates a {@link TemplateNode}.
	 */
	public TemplateNode(int line, int column) {
		_line = line;
		_column = column;
	}

	/**
	 * The line in the source template file.
	 */
	public int getLine() {
		return _line;
	}

	/**
	 * The column in the source template file.
	 */
	public int getColumn() {
		return _column;
	}

}
