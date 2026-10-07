/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link ViewCommand} that closes the topmost open dialog without propagating any results.
 *
 * <p>
 * This command is intended for "Cancel" or "Close" buttons inside dialog view XML files. It takes
 * no configuration beyond the standard {@link ViewCommand.Config} properties (label, image, etc.).
 * </p>
 *
 * <p>
 * While a command of the dialog is still running, the dialog cannot be left and the button says so
 * instead of doing nothing, see {@link SuspendedCommands}.
 * </p>
 */
@InApp
public class CancelDialogCommand implements ViewCommand {

	/**
	 * Configuration for {@link CancelDialogCommand}.
	 */
	@TagName("cancel-dialog")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(CancelDialogCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link CancelDialogCommand}.
	 */
	@CalledByReflection
	public CancelDialogCommand(InstantiationContext context, Config config) {
		// No additional configuration.
	}

	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return new DialogSettled();
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		mgr.closeTopDialog(DialogResult.cancelled());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Offers leaving the dialog only while no command of it is still running.
	 *
	 * <p>
	 * The dialog refuses to close for as long as a command it started holds its chain - there would
	 * be nothing left to follow the work in, or to stop it. The button gives that as its reason,
	 * and becomes available again as soon as the command has settled.
	 * </p>
	 */
	private static class DialogSettled implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

		private SuspendedCommands _suspended;

		@Override
		public void bind(ViewContext context) {
			_suspended = context.getScope(SuspendedCommands.class);
		}

		@Override
		public ExecutableState isExecutable(Object input) {
			if (_suspended != null && _suspended.hasSuspended()) {
				return ExecutableState.createDisabledState(I18NConstants.ERROR_DIALOG_COMMAND_RUNNING);
			}
			return ExecutableState.EXECUTABLE;
		}

		@Override
		public Runnable observe(Runnable revalidate) {
			if (_suspended == null) {
				return () -> {
					// No region of commands to follow.
				};
			}
			return _suspended.observe(revalidate);
		}
	}
}
