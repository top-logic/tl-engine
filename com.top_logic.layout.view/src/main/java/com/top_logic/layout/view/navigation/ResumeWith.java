/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.layout.view.command.Continuation;

/**
 * The {@link Continuation} of a command chain, resumed with the value the chain had before
 * something was displayed.
 *
 * <p>
 * Displaying is a side effect of the chain: what the display request answers stays with the
 * display, and the action after the one that displayed sees the value its predecessor produced.
 * </p>
 */
final class ResumeWith implements Continuation {

	private final Continuation _chain;

	private final Object _input;

	/**
	 * Creates a {@link ResumeWith}.
	 *
	 * @param chain
	 *        The continuation of the surrounding chain.
	 * @param input
	 *        The value to continue the chain with.
	 */
	ResumeWith(Continuation chain, Object input) {
		_chain = chain;
		_input = input;
	}

	@Override
	public void resume(Object value) {
		_chain.resume(_input);
	}

	@Override
	public void abort() {
		_chain.abort();
	}

	@Override
	public void onAbort(Runnable compensation) {
		_chain.onAbort(compensation);
	}
}
