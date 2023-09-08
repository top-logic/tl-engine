/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

/**
 * Exception that aborts an operation due to an inconsistent {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InvalidModelException extends RuntimeException {

	/**
	 * Creates a {@link InvalidModelException}.
	 */
	public InvalidModelException() {
		super();
	}

	/**
	 * Creates a {@link InvalidModelException}.
	 */
	public InvalidModelException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link InvalidModelException}.
	 */
	public InvalidModelException(String message) {
		super(message);
	}

	/**
	 * Creates a {@link InvalidModelException}.
	 */
	public InvalidModelException(Throwable cause) {
		super(cause);
	}

}
