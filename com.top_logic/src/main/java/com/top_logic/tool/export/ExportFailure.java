/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

/**
 * {@link Exception} that announces a problem during export generation or {@link ExportRegistry}
 * access.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExportFailure extends Exception {

	/**
	 * Creates a {@link ExportFailure}.
	 */
	public ExportFailure() {
		super();
	}

	/**
	 * Creates a {@link ExportFailure}.
	 * 
	 * @param message
	 *        See {@link Throwable#Throwable(String, Throwable)}
	 * @param cause
	 *        See {@link Throwable#Throwable(String, Throwable)}
	 */
	public ExportFailure(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link ExportFailure}.
	 * 
	 * @param message
	 *        See {@link Throwable#Throwable(String, Throwable)}.
	 */
	public ExportFailure(String message) {
		super(message);
	}

	/**
	 * Creates a {@link ExportFailure}.
	 * 
	 * @param cause
	 *        See {@link Throwable#Throwable(String, Throwable)}
	 */
	public ExportFailure(Throwable cause) {
		super(cause);
	}

}
