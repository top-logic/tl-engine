/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.ReactContext;

/**
 * Base class for {@link ViewAction}s that may suspend the chain (e.g. to await a confirmation
 * dialog) and resume or abort it later.
 *
 * <p>
 * Subclasses implement only {@link #execute(ReactContext, Object, Continuation)}: do the work, then
 * call {@link Continuation#resume(Object)} to continue, {@link Continuation#abort()} to cancel, or
 * hold the continuation and do one of those later (e.g. from a dialog callback). The synchronous
 * {@link #execute(ReactContext, Object)} form is unavailable for interruptible actions - the chain
 * always uses the {@link Continuation}-based form.
 * </p>
 */
public abstract class InterruptibleViewAction implements ViewAction {

	@Override
	public final Object execute(ReactContext context, Object input) {
		throw new UnsupportedOperationException(
			getClass().getName() + " is interruptible and must be driven via the Continuation-based "
				+ "execute(...). This is never invoked by the action chain.");
	}

	@Override
	public abstract void execute(ReactContext context, Object input, Continuation continuation);
}
