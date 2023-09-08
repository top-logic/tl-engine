/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

/**
 * {@link Exception} signaling that no implementation class could be generated.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see ConfigCompiler#compileImplementation(Class)
 */
public class GenerationFailedException extends Exception {

	/**
	 * Creates a {@link GenerationFailedException}.
	 */
	public GenerationFailedException(Throwable ex) {
		super(ex);
	}

	/**
	 * Creates a {@link GenerationFailedException}.
	 */
	public GenerationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
