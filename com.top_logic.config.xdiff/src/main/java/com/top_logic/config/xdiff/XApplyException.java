/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff;

import com.top_logic.config.xdiff.ms.MSXDiff;

/**
 * {@link Exception} describing a problem during applying an {@link MSXDiff}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XApplyException extends RuntimeException {

	/**
	 * Creates a {@link XApplyException}.
	 */
	public XApplyException() {
		super();
	}

	/**
	 * Creates a {@link XApplyException}.
	 * 
	 * @param message
	 *        See {@link #getMessage()}.
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public XApplyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link XApplyException}.
	 * 
	 * @param message
	 *        See {@link #getMessage()}.
	 */
	public XApplyException(String message) {
		super(message);
	}

	/**
	 * Creates a {@link XApplyException}.
	 * 
	 * @param cause
	 *        See {@link #getCause()}.
	 */
	public XApplyException(Throwable cause) {
		super(cause);
	}

}
