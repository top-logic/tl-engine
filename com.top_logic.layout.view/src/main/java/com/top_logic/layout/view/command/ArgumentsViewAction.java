/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.ReactContext;

/**
 * Base class for {@link ViewAction}s that work on all {@link ActionArguments arguments} of their
 * chain rather than on a single value, e.g. an action running nested actions with the arguments of
 * the chain.
 *
 * <p>
 * Subclasses implement only {@link #execute(ReactContext, ActionArguments, Continuation)}. The
 * single-value forms are unavailable - the chain always passes the arguments.
 * </p>
 */
public abstract class ArgumentsViewAction implements ViewAction {

	@Override
	public final Object execute(ReactContext context, Object input) {
		throw unsupported();
	}

	@Override
	public final void execute(ReactContext context, Object input, Continuation continuation) {
		throw unsupported();
	}

	@Override
	public abstract void execute(ReactContext context, ActionArguments arguments, Continuation continuation);

	private UnsupportedOperationException unsupported() {
		return new UnsupportedOperationException(
			getClass().getName() + " works on the arguments of its chain and must be driven via "
				+ "execute(ReactContext, ActionArguments, Continuation).");
	}
}
