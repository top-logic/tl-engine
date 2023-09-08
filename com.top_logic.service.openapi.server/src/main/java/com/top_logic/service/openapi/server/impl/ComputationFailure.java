/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

/**
 * {@link Exception} that is thrown when processing the request failed for internal, non-technical
 * failures. This exception is not logged as error; the stack is ignored.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComputationFailure extends Exception {

	/**
	 * Creates a new {@link ComputationFailure}.
	 * 
	 * @param message
	 *        see {@link Exception#Exception(String)}
	 */
	public ComputationFailure(String message) {
		super(message);
	}

}

