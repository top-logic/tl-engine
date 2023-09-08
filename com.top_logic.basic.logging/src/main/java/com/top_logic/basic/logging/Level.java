/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.logging;

/**
 * Possible logging levels for messages in <i>TopLogic</i>.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public enum Level {

	/** Debugging level. */
	DEBUG,

	/** Info level. */
	INFO,

	/** Warning level. */
	WARN,

	/** Error level. */
	ERROR,

	/** Fatal error level. */
	FATAL;

	/**
	 * Whether a message with this {@link Level} should be logged, if the log {@link Level} is the
	 * given one.
	 */
	public boolean is(Level logLevel) {
		return ordinal() >= logLevel.ordinal();
	}
}
