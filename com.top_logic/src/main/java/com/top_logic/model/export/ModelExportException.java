/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

/**
 * Exception reporting a fatal failure during model export.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelExportException extends RuntimeException {

	/**
	 * Creates a {@link ModelExportException}.
	 */
	public ModelExportException() {
		super();
	}

	/**
	 * Creates a {@link ModelExportException}.
	 * 
	 * @param message
	 *        See {@link #getMessage()}.
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public ModelExportException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link ModelExportException}.
	 * 
	 * @param message
	 *        See {@link #getMessage()}.
	 */
	public ModelExportException(String message) {
		super(message);
	}

	/**
	 * Creates a {@link ModelExportException}.
	 * 
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public ModelExportException(Throwable cause) {
		super(cause);
	}

	
}
