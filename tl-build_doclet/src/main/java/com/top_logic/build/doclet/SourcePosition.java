/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.net.URI;

import javax.tools.JavaFileObject;

/**
 * Representation of a line in a {@link JavaFileObject java source file}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SourcePosition {

	private JavaFileObject _sourceFile;

	private long _line;

	/**
	 * Creates a new {@link SourcePosition}.
	 * 
	 * @param sourceFile
	 *        See {@link #file()}.
	 * @param line
	 *        See {@link #line()}.
	 */
	public SourcePosition(JavaFileObject sourceFile, long line) {
		_sourceFile = sourceFile;
		_line = line;
	}

	/**
	 * The {@link JavaFileObject} for this {@link SourcePosition}.
	 */
	public JavaFileObject file() {
		return _sourceFile;
	}

	/**
	 * The line number for this {@link SourcePosition}.
	 */
	public long line() {
		return _line;
	}

	/**
	 * Service method to get a {@link URI} for {@link #file()}.
	 * 
	 * @implNote Calls {@link JavaFileObject#toUri()} on {@link #file()}.
	 */
	public URI uri() {
		return file().toUri();
	}

	@Override
	public String toString() {
		return uri() + ":" + line();
	}

}
