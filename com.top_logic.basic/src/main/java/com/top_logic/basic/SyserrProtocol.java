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
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SyserrProtocol extends AbstractPrintProtocol {

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
		System.out.println(s);
	}

	@Override
	protected void err(String s) {
		System.err.println(s);
	}

}