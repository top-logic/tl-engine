/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.io;

import java.io.IOException;

/**
 * Reader API.
 * 
 * <p>
 * Workaround for <code>java.io.Reader</code> and <code>java.io.Writer</code> not being supported by
 * GWT.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface R {

	/**
	 * Reads characters into a portion of an array. This method will block until some input is
	 * available, an I/O error occurs, or the end of the stream is reached.
	 *
	 * @param cbuf
	 *        Destination buffer
	 * @param off
	 *        Offset at which to start storing characters
	 * @param len
	 *        Maximum number of characters to read
	 *
	 * @return The number of characters read, or -1 if the end of the stream has been reached
	 *
	 * @exception IOException
	 *            If an I/O error occurs
	 */
	int read(char[] cbuf, int off, int len) throws IOException;

	/**
	 * Closes this reader.
	 * 
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	void close() throws IOException;

}
