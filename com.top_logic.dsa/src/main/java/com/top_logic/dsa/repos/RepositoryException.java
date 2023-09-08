/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos;

import com.top_logic.dsa.DatabaseAccessException;

/**
 * Base exception for Repositories. 
 *
 * Extends DatabaseAccessException since repositories are mostly
 * used by {@link com.top_logic.dsa.DataSourceAdaptor}s
 *
 * @author  <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class RepositoryException extends DatabaseAccessException {

	/** 
	 * Construct a RepositoryException with a String message.
	 */
	public RepositoryException (String aMessage) {
		super (aMessage);
	}
    
	/** 
	 * Construct a RepositoryException from some other Exception.
	 *
	 * @param rootCause - original reason for the Exception
	 */
	public RepositoryException(Throwable rootCause) {
	    super(rootCause);
	}
	
	/**
	 * Constructor with a Message and some embedded Exception.
	 *
	 * @param aMessage a message string
	 * @param rootCause - original reason for the Exception
	 */
	public RepositoryException(String aMessage, Throwable rootCause) {
	    super(aMessage, rootCause);
	}
}
