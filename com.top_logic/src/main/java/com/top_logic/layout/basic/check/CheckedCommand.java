/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import java.util.Collection;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Command} that checks a {@link CheckScope} before continuing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CheckedCommand implements Command {
	final Command impl;
	private final CheckScope checkScope;

	/**
	 * Creates a {@link CheckedCommand}.
	 * 
	 * @param impl
	 *        The {@link Command} to execute after the check.
	 * @param checkScope
	 *        The {@link CheckScope} to check before continuing.
	 */
	public CheckedCommand(Command impl, CheckScope checkScope) {
		this.impl = impl;
		this.checkScope = checkScope;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		DirtyHandling dirtyHandling = DirtyHandling.getInstance();
		Collection<? extends ChangeHandler> handlers = checkScope.getAffectedFormHandlers();
		if (dirtyHandling.checkDirty(handlers)) {
			Command continuation = new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext continuationContext) {
					return impl.executeCommand(continuationContext);
				}
			};
			dirtyHandling.openConfirmDialog(continuation, handlers, context.getWindowScope());
			return HandlerResult.DEFAULT_RESULT;
		} else {
			return impl.executeCommand(context);
		}
	}
}