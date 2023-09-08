/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.IOException;

/**
 * {@link RuntimeException} wrapping an {@link IOException} that cannot be
 * declared to be thrown at the point where it occurs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrappedIOException extends RuntimeException {

	/**
	 * Creates a {@link WrappedIOException}.
	 * 
	 * @param ex
	 *        See {@link #getIOException()}.
	 */
	public WrappedIOException(IOException ex) {
		initCause(ex);
	}

	/**
	 * Re-throws the wrapped {@link IOException}.
	 * 
	 * <p>
	 * To be called at a point where the original exception can be declared.
	 * </p>
	 * 
	 * @throws IOException
	 *         Unconditionally throws the wrapped exception.
	 */
	public void unwrap() throws IOException {
		throw getIOException();
	}

	/**
	 * The wrapped exception.
	 * 
	 * <p>
	 * Type-safe variant of {@link #getCause()}.
	 * </p>
	 */
	public IOException getIOException() {
		return (IOException) getCause();
	}

}
