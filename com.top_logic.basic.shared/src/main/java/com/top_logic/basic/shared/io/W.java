/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.io;

import java.io.IOException;

/**
 * Writer API.
 * 
 * <p>
 * Workaround for <code>java.io.Reader</code> and <code>java.io.Writer</code> not being supported by
 * GWT.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface W {

	/**
	 * Writes a string.
	 *
	 * @param str
	 *        A String
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	void write(String str) throws IOException;

	/**
	 * Writes a portion of a string.
	 *
	 * @param str
	 *        A String
	 * @param off
	 *        Offset from which to start writing characters
	 * @param len
	 *        Number of characters to write
	 * @throws IndexOutOfBoundsException
	 *         If <tt>off</tt> is negative, or <tt>len</tt> is negative, or <tt>off+len</tt> is
	 *         negative or greater than the length of the given string
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	void write(String str, int off, int len) throws IOException;

	/**
	 * Writes a character.
	 *
	 * @param ch
	 *        A character to write.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	void write(char ch) throws IOException;

	/**
	 * Flushes output.
	 * 
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	void flush() throws IOException;

	/**
	 * Closes this writer.
	 * 
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	void close() throws IOException;

}
