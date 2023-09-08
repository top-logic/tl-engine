/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element;

/**
 * Super class for exceptions in the handling of StructuredElements
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class ElementException extends RuntimeException {

	/**
	 * ElementException with a message.
	 * 
	 * @param aMessage the error message
	 */
	public ElementException(String aMessage) {
		super(aMessage);
	}

	/**
	 * ElementException with a message and a cause.
	 * @param aMessage 	the massage
	 * @param aCause	the cause
	 */
	public ElementException(String aMessage, Throwable aCause) {
		super(aMessage, aCause);
	}

}
