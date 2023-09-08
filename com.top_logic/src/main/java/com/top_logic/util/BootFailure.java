/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

/**
 * {@link Exception} during {@link DeferredBootService#boot()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BootFailure extends Exception {

	/**
	 * Creates a {@link BootFailure}.
	 * 
	 * @see Exception#Exception(String, Throwable)
	 */
	public BootFailure(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link BootFailure}.
	 * 
	 * @see Exception#Exception(Throwable)
	 */
	public BootFailure(Throwable cause) {
		super(cause);
	}

}
