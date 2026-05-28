/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link Protocol} adaptor for {@link System#err}/{@link System#out} that aborts execution on
 * {@link #fatal(String, Throwable)} problems with an
 * {@link AbortExecutionException}.
 *
 * <p>
 * Each line is prefixed with a Maven-style severity marker ({@value #ERROR_PREFIX} for errors and
 * fatal problems, {@value #INFO_PREFIX} for info output) so that errors are clearly distinguishable
 * from info output and can be located with line-based tooling such as <code>grep</code>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SyserrProtocol extends AbstractPrintProtocol {

	/** Prefix prepended to error and fatal output lines. */
	private static final String ERROR_PREFIX = "[ERROR] ";

	/** Prefix prepended to info output lines. */
	private static final String INFO_PREFIX = "[INFO] ";

	/**
	 * Creates a {@link SyserrProtocol}.
	 */
	public SyserrProtocol() {
		super();
	}

	/**
	 * Creates a {@link SyserrProtocol}.
	 *
	 * @param chain
	 *        See {@link AbstractProtocol#AbstractProtocol(Protocol)}.
	 */
	public SyserrProtocol(Protocol chain) {
		super(chain);
	}

	@Override
	protected void out(String s) {
		System.out.println(INFO_PREFIX + s);
	}

	@Override
	protected void err(String s) {
		System.err.println(ERROR_PREFIX + s);
	}

}