/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.conditional;

import java.util.Map;

import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * An execution step of a {@link PreconditionCommandHandler}.
 * 
 * @see PreconditionCommandHandler#prepare(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CommandStep {

	/**
	 * Actually executes the command.
	 * 
	 * @see #doPrepare(DisplayContext)
	 * @see #doCommit(DisplayContext)
	 * @see #doFinally(DisplayContext)
	 * 
	 * @param context
	 *        See {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * @return See
	 *         {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 */
	public final HandlerResult execute(DisplayContext context) {
		HandlerResult result;

		try {
			result = doPrepare(context);

			if (result.isSuccess()) {
				doExecute(context);

				result = doCommit(context);
			}
		} finally {
			doFinally(context);
		}

		return result;
	}

	/**
	 * Called before actually starting with the command execution in
	 * {@link #doExecute(DisplayContext)}.
	 * 
	 * @param context
	 *        See
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected HandlerResult doPrepare(DisplayContext context) {
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Actually performs the operations of the command.
	 * 
	 * @see #doPrepare(DisplayContext)
	 * @see #doCommit(DisplayContext)
	 * 
	 * @param context
	 *        See
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected abstract void doExecute(DisplayContext context);

	/**
	 * Called after the command execution in {@link #doExecute(DisplayContext)} has completed
	 * successfully.
	 * 
	 * @param context
	 *        See
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * @see #doExecute(DisplayContext)
	 * @see #doFinally(DisplayContext)
	 */
	protected HandlerResult doCommit(DisplayContext context) {
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Called unconditionally at the end of the command invocation (no matter whether successful or
	 * not).
	 * 
	 * @param context
	 *        See
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected void doFinally(DisplayContext context) {
		// Hook for sub-classes.
	}

	/**
	 * Whether command execution can start.
	 */
	public abstract ExecutableState getExecutability();

	/**
	 * Combines this {@link CommandStep} with an additional given step.
	 * 
	 * <p>
	 * {@link #doPrepare(DisplayContext) Prepare} and {@link #doExecute(DisplayContext) execute} are
	 * executed in order (first the implementation of this step and second the implementation of the
	 * given step). The {@link #doCommit(DisplayContext) commit} and
	 * {@link #doFinally(DisplayContext) finally phase} are executed in reverse order to allow to
	 * spawn a transaction in the initial step and add additional operations to this transaction in
	 * added steps.
	 * </p>
	 * 
	 * @param next
	 *        The next step to build a {@link CommandStep} sequence.
	 * @return The combined step.
	 */
	public CommandStep and(final CommandStep next) {
		final CommandStep first = this;
		return new CommandStep() {
			@Override
			public ExecutableState getExecutability() {
				return first.getExecutability().combine(next.getExecutability());
			}

			@Override
			protected HandlerResult doPrepare(DisplayContext context) {
				HandlerResult result;
				result = first.doPrepare(context);
				if (result.isSuccess()) {
					result = next.doPrepare(context);
				}
				return result;
			}

			@Override
			protected void doExecute(DisplayContext context) {
				first.doExecute(context);
				next.doExecute(context);
			}

			@Override
			protected HandlerResult doCommit(DisplayContext context) {
				HandlerResult result;
				result = next.doCommit(context);
				if (result.isSuccess()) {
					result = first.doCommit(context);
				}
				return result;
			}

			@Override
			protected void doFinally(DisplayContext context) {
				try {
					next.doFinally(context);
				} finally {
					first.doFinally(context);
				}
			}

		};
	}

}