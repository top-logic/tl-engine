/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Used for #599 Protocol interface for light-weight logging interface for service classes.
 * 
 * @author    <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public class AbortExecutionException extends RuntimeException {

	/**
	 * Creates a {@link AbortExecutionException}.
	 */
	public AbortExecutionException() {
		super();
	}

	/**
	 * Creates a {@link AbortExecutionException}.
	 * 
	 * @param cause
	 *        See {@link RuntimeException#RuntimeException(Throwable)}.
	 */
	public AbortExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a {@link AbortExecutionException}.
	 * 
	 * @param message
	 *        See {@link RuntimeException#RuntimeException(String)}.
	 * @param cause
	 *        See {@link RuntimeException#RuntimeException(Throwable)}.
	 */
	public AbortExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
