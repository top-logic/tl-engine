/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * {@link IllegalStateException} that is throw when a deleted object is accessed.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeletedObjectAccess extends IllegalStateException {

	/**
	 * Creates a new {@link DeletedObjectAccess}.
	 * 
	 * @see IllegalStateException#IllegalStateException(String, Throwable)
	 */
	public DeletedObjectAccess(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link DeletedObjectAccess}.
	 * 
	 * @see IllegalStateException#IllegalStateException(String)
	 */
	public DeletedObjectAccess(String message) {
		super(message);
	}


}

