/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;


/**
 * {@link RuntimeException} that is thrown when trying to get the content of an object failed.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ContentRetrievalFailedException extends RuntimeException {
	
	/** Creates a {@link ContentRetrievalFailedException}. */
	public ContentRetrievalFailedException(String message) {
		super(message);
	}

	/** Creates a {@link ContentRetrievalFailedException}. */
	public ContentRetrievalFailedException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
