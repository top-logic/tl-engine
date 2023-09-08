/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

/**
 * {@link Exception} reporting an invalid value in a request argument.
 * 
 * @see ConcreteRequestParameter#getValue(javax.servlet.http.HttpServletRequest, java.util.Map)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InvalidValueException extends Exception {

	/**
	 * Creates a {@link InvalidValueException}.
	 */
	public InvalidValueException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@link InvalidValueException}.
	 */
	public InvalidValueException(String message) {
		super(message);
	}

}
