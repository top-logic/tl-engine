/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.pbe;

/**
 * {@link Exception} that reports an invalid (application/encryption) password.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InvalidPasswordException extends Exception {

	/**
	 * Creates a {@link InvalidPasswordException}.
	 */
	public InvalidPasswordException() {
		super();
	}

}
