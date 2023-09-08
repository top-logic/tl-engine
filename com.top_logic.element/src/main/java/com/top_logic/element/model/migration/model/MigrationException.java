/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

/**
 * {@link Exception} when Migration can not be executed.
 * 
 * @see Util
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrationException extends Exception {

	/**
	 * Creates a new {@link MigrationException}.
	 */
	public MigrationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new {@link MigrationException}.
	 */
	public MigrationException(String message) {
		super(message);
	}

}

