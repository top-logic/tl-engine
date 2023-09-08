/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

/**
 * Super class of Exception thrown when using MetaAttributes.
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class AttributeException extends RuntimeException {

	/**
	 * Standard CTor
	 */
	public AttributeException() {
		super();
	}

	/**
	 * CTor with a massage
	 * 
	 * @param message the message
	 */
	public AttributeException(String message) {
		super(message);
	}

	/**
	 * CTor with a cause
	 * 
	 * @param cause	the cause
	 */
	public AttributeException(Throwable cause) {
		super(cause);
	}

	/**
	 * CTor with a message and a cause
	 * 
	 * @param message	the message
	 * @param cause		the cause
	 */
	public AttributeException(String message, Throwable cause) {
		super(message, cause);
	}

}
