/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link BufferingProtocol} that treats errors as infos.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ErrorIgnoringProtocol extends BufferingProtocol {

	/**
	 * Creates a {@link ErrorIgnoringProtocol}.
	 */
	public ErrorIgnoringProtocol() {
		super();
	}

	/**
	 * Creates a {@link ErrorIgnoringProtocol}.
	 * 
	 * @param chain
	 *        See {@link BufferingProtocol#BufferingProtocol(Protocol)}.
	 */
	public ErrorIgnoringProtocol(Protocol chain) {
		super(chain);
	}

	@Override
	protected void err(String s) {
		out(s);
	}

	@Override
	protected void setError(Throwable ex) {
		// Ignore.
	}
}

