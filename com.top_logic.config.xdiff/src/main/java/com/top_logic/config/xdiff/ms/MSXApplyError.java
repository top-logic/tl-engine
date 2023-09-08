/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

/**
 * Error during {@link MSXApply}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MSXApplyError extends RuntimeException {

	/**
	 * Creates a {@link MSXApplyError}.
	 */
	public MSXApplyError() {
		super();
	}

	/**
	 * Creates a {@link MSXApplyError}.
	 */
	public MSXApplyError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link MSXApplyError}.
	 */
	public MSXApplyError(String message) {
		super(message);
	}

	/**
	 * Creates a {@link MSXApplyError}.
	 */
	public MSXApplyError(Throwable cause) {
		super(cause);
	}

}
