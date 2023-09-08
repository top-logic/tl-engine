/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war;

/**
 * Wrapper for tunneling an exception through code that cannot throw checked exceptions.
 */
public class MojoRuntimeException extends RuntimeException {

	/**
	 * Creates a {@link MojoRuntimeException}.
	 */
	public MojoRuntimeException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates a {@link MojoRuntimeException}.
	 */
	public MojoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
