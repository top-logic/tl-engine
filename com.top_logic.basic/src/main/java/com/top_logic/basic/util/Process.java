/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;


/**
 * {@link Computation} that may throw {@link Exception}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Process implements Computation<Process> {
	private Exception problem;

	@Override
	public Process run() {
		try {
			process();
		} catch (Exception ex) {
			problem = ex;
		}
		return this;
	}

	/**
	 * Whether a problem occurred in {@link #process()}.
	 */
	public boolean hasProblem() {
		return getProblem() != null;
	}

	/**
	 * The problem that occurred during {@link #process()}, or <code>null</code>.
	 */
	public Exception getProblem() {
		return problem;
	}

	/**
	 * Throws the {@link #getProblem()} if there is one.
	 */
	public void reportProblem() throws Exception {
		if (hasProblem()) {
			throw getProblem();
		}
	}

	/**
	 * The operation to process.
	 */
	protected abstract void process() throws Exception;

}