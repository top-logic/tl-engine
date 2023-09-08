/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.exception;

/**
 * Classification of error severities that are communicated to the user.
 * 
 * @see I18NRuntimeException#getSeverity()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ErrorSeverity {
	/**
	 * An informative notice.
	 */
	INFO,

	/**
	 * A warning notice that a user operation could not be performed.
	 */
	WARNING,

	/**
	 * A notice that a user request was not valid.
	 */
	ERROR,

	/**
	 * An indicator that the system failed to perform an otherwise valid user request.
	 */
	SYSTEM_FAILURE;
}
