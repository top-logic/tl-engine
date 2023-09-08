/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link Protocol} that keeps all info and error output in buffers.
 * 
 * @see #getOutput() The info output buffer.
 * @see #getError() The error output buffer.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BufferingProtocol extends AbstractPrintProtocol {

	private List<String> infos = new ArrayList<>();
	private List<String> errors = new ArrayList<>();

	/**
	 * Creates a {@link BufferingProtocol}.
	 */
	public BufferingProtocol() {
		super();
	}

	/**
	 * Creates a {@link BufferingProtocol}.
	 *
	 * @param chain
	 *        See {@link AbstractProtocol#AbstractProtocol(Protocol)}.
	 */
	public BufferingProtocol(Protocol chain) {
		super(chain);
	}

	@Override
	protected RuntimeException createAbort() {
		return createAbort("Aborting due to previous errors: " + getError(), getFirstProblem());
	}

	@Override
	protected void out(String s) {
		infos.add(s);
	}
	
	@Override
	protected void err(String s) {
		errors.add(s);
	}
	
	/**
	 * The accumulated info output.
	 * 
	 * <p>
	 * Lines are delimited with <code>\n</code> characters.
	 * </p>
	 */
	public String getOutput() {
		return toString(infos);
	}

	/**
	 * The accumulated error output.
	 * 
	 * <p>
	 * Lines are delimited with <code>\n</code> characters.
	 * </p>
	 */
	public String getError() {
		return toString(errors);
	}
	
	/**
	 * All error messages that have been reported.
	 * 
	 * @return Unmodifiable list of error messages.
	 * 
	 * @see #error(String)
	 * @see #error(String, Throwable)
	 */
	public List<String> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	/**
	 * All info messages that have been reported.
	 * 
	 * @return Unmodifiable list of info messages.
	 * 
	 * @see #info(String)
	 * @see #info(String, int)
	 */
	public List<String> getInfos() {
		return Collections.unmodifiableList(infos);
	}
	
	private String toString(List<String> source) {
		StringBuilder result = new StringBuilder();
		for (String info : source) {
			result.append(info);
			result.append('\n');
		}
		return result.toString();
	}
	
}
