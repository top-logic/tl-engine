/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

/**
 * This Exception is thrown whenever en Entry in a Repository cannot be found. 
 *
 * @author  <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class NoEntryException extends RepositoryException {

	/** 
	 * Construct a NoEntryException with a String message.
	 */
	public NoEntryException (String aMessage) {
		super (aMessage);
	}
    
	/**
	 * Constructor with a Message and some embedded Exception.
	 *
	 * @param aMessage  a message string
	 * @param rootCause original reason for the Exception
	 */
	public NoEntryException(String aMessage, Throwable rootCause) {
	    super(aMessage, rootCause);
	}
}
