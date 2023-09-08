/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

/**
 * Source code location.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextRange {

	/**
	 * The {@link TextRange} pointing to nowhere.
	 */
	public static final TextRange UNDEFINED = new TextRange(-1, -1, -1, -1);

	private int _beginLine;

	private int _beginColumn;

	private int _endLine;

	private int _endColumn;

	/**
	 * Creates a {@link TextRange}.
	 *
	 * @param beginLine
	 *        See {@link #beginLine()}.
	 * @param beginColumn
	 *        See {@link #beginColumn()}.
	 * @param endLine
	 *        See {@link #endLine()}.
	 * @param endColumn
	 *        See {@link #endColumn()}.
	 */
	public TextRange(int beginLine, int beginColumn, int endLine, int endColumn) {
		_beginLine = beginLine;
		_beginColumn = beginColumn;
		_endLine = endLine;
		_endColumn = endColumn;
	}

	/**
	 * Line where the source code starts.
	 */
	public int beginLine() {
		return _beginLine;
	}

	/**
	 * Column where the source code starts.
	 */
	public int beginColumn() {
		return _beginColumn;
	}

	/**
	 * Line where the source code ends.
	 */
	public int endLine() {
		return _endLine;
	}

	/**
	 * Column where the source code ends.
	 */
	public int endColumn() {
		return _endColumn;
	}

	/**
	 * Whether this range points to a defined location.
	 */
	public boolean isDefined() {
		return _beginLine >= 0;
	}

	/**
	 * String describing an (error) location.
	 */
	public String location() {
		if (isDefined()) {
			return " at line " + beginLine() + ", column " + beginColumn();
		} else {
			return "";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _beginColumn;
		result = prime * result + _beginLine;
		result = prime * result + _endColumn;
		result = prime * result + _endLine;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextRange other = (TextRange) obj;
		if (_beginColumn != other._beginColumn)
			return false;
		if (_beginLine != other._beginLine)
			return false;
		if (_endColumn != other._endColumn)
			return false;
		if (_endLine != other._endLine)
			return false;
		return true;
	}

}
