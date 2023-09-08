/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link Protocol} that ignores all events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoProtocol implements Protocol {

	/**
	 * Singleton {@link NoProtocol} instance.
	 */
	public static final NoProtocol INSTANCE = new NoProtocol();

	private NoProtocol() {
		// Singleton constructor.
	}
	
	@Override
	public void checkErrors() {
		// Ignore.
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
	public void info(String message) {
		// Ignore.
	}

	@Override
	public void info(String message, int verbosityLevel) {
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
	public RuntimeException fatal(String message) {
		return new RuntimeException(message);
	}

	@Override
	public RuntimeException fatal(String message, Throwable ex) {
		return new RuntimeException(message, ex);
	}

}
