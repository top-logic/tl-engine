/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.logging;

/**
 * A {@link RuntimeException} which is used by {@link LogUtil#withLogMark(String, String, Runnable)}
 * to annotate a "mark" to a stack trace.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogMarkRuntimeException extends RuntimeException {

	/** Creates a {@link LogMarkRuntimeException} with the given mark. */
	public LogMarkRuntimeException(String key, String value, Throwable cause) {
		super("LOG MARK: '" + key + "' = '" + value + "'. Cause: " + cause.getMessage(), cause);
	}

}
