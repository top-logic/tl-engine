/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

/**
 * Super class of Exceptions thrown when using MetaElements.
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class MetaElementException extends RuntimeException {

	/**
	 * Standard CTor
	 */
	public MetaElementException() {
		super();
	}

	/**
	 * CTor with a massage
	 * 
	 * @param message the message
	 */
	public MetaElementException(String message) {
		super(message);
	}

	/**
	 * CTor with a cause
	 * 
	 * @param cause	the cause
	 */
	public MetaElementException(Throwable cause) {
		super(cause);
	}

	/**
	 * CTor with a message and a cause
	 * 
	 * @param message	the message
	 * @param cause		the cause
	 */
	public MetaElementException(String message, Throwable cause) {
		super(message, cause);
	}

}
