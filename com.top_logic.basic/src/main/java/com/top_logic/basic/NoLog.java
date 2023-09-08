/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link Log} that swallows all messages.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NoLog implements Log {

	/**
	 * Singleton {@link NoLog} instance.
	 */
	public static final NoLog INSTANCE = new NoLog();

	private NoLog() {
		// Singleton constructor.
	}

	@Override
	public void error(String message) {
		// Ignore.
	}

	@Override
	public void error(String message, Throwable ex) {
		// Ignore.
	}

	@Override
	public Throwable getFirstProblem() {
		return null;
	}

	@Override
	public boolean hasErrors() {
		return false;
	}

	@Override
	public void info(String message) {
		// Ignore.
	}

	@Override
	public void info(String message, int verbosityLevel) {
		// Ignore.
	}
}