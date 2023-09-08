/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

/**
 * Error during loading a layout definition.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResolveFailure extends Exception {

	public ResolveFailure() {
		super();
	}

	public ResolveFailure(String message, Throwable cause) {
		super(message, cause);
	}

	public ResolveFailure(String message) {
		super(message);
	}

	public ResolveFailure(Throwable cause) {
		super(cause);
	}

}
