/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Collection;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class for {@link CommandModel}s which implements all methods but the {@link Command} aspect
 * {@link CommandModel#executeCommand(com.top_logic.layout.DisplayContext)}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCommandModel extends DefaultButtonUIModel implements CommandModel {
	
	private CheckScope checkScope;

	@Override
	public CheckScope getCheckScope() {
		return this.checkScope;
	}
	
	/**
	 * @see #getCheckScope()
	 */
	public void setCheckClosure(CheckScope checkScope) {
		this.checkScope = checkScope;
	}

	/**
	 * Checks {@link #getCheckScope()} and delegates to actual implementation
	 * {@link #internalExecuteCommand(DisplayContext)}.
	 * 
	 * @see com.top_logic.layout.basic.Command#executeCommand(com.top_logic.layout.DisplayContext)
	 * @see #internalExecuteCommand(DisplayContext)
	 */
	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		if (getCheckScope() == null) {
			return internalExecuteCommand(context);
		}
		DirtyHandling dirtyHandling = DirtyHandling.getInstance();
		Collection<? extends ChangeHandler> handlers = getCheckScope().getAffectedFormHandlers();
		if (!dirtyHandling.checkDirty(handlers)) {
			return internalExecuteCommand(context);
		}
		Command continuation = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext continuationContext) {
				return internalExecuteCommand(continuationContext);
			}
		};
		dirtyHandling.openConfirmDialog(continuation, handlers, context.getWindowScope());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Actual implementation of {@link CommandModel#executeCommand(DisplayContext)}.
	 * 
	 * <p>
	 * This method is called by {@link #executeCommand(DisplayContext)} after the "dirty handling".
	 * </p>
	 * 
	 * @param context
	 *        The current execution context.
	 * @return Result of {@link #executeCommand(DisplayContext)}.
	 */
	protected abstract HandlerResult internalExecuteCommand(DisplayContext context);

}
