/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link ViewAction} that closes the topmost dialog.
 *
 * <p>
 * A dialog does not close while a command of it is still running (see {@link SuspendedCommands}).
 * That includes the command running this action, when it was taken up again after an action that
 * held it - the end of a background job, the answer to a question. The dialog is then closed as
 * soon as no command of it is running any more, which is right after the command running this
 * action has settled.
 * </p>
 */
@InApp
public class CloseDialogAction implements ViewAction {

	/**
	 * Configuration for {@link CloseDialogAction}.
	 */
	@TagName("close-dialog")
	public interface Config extends PolymorphicConfiguration<CloseDialogAction> {

		@Override
		@ClassDefault(CloseDialogAction.class)
		Class<? extends CloseDialogAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link CloseDialogAction}.
	 */
	@CalledByReflection
	public CloseDialogAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		DialogManager mgr = context.getDialogManager();
		if (mgr != null) {
			SuspendedCommands suspended = context instanceof ViewContext viewContext
				? viewContext.getScope(SuspendedCommands.class)
				: null;
			if (suspended != null && suspended.hasSuspended()) {
				closeWhenSettled(mgr, suspended);
			} else {
				close(mgr);
			}
		}
		return input;
	}

	private static void close(DialogManager mgr) {
		mgr.closeTopDialog(DialogResult.cancelled());
	}

	/**
	 * Closes the topmost dialog once the given region has no running command any more.
	 */
	private static void closeWhenSettled(DialogManager mgr, SuspendedCommands suspended) {
		Runnable[] stop = new Runnable[1];
		stop[0] = suspended.observe(() -> {
			if (!suspended.hasSuspended()) {
				stop[0].run();
				close(mgr);
			}
		});
	}

	/**
	 * Closes a dialog.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
