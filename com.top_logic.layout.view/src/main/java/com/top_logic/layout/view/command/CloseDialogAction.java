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
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link ViewAction} that closes the topmost dialog.
 *
 * <p>
 * A dialog does not close while a command of it is still running (see {@link SuspendedCommands}):
 * another command of the dialog that an action holds - a background job, a question waiting for its
 * answer. The command running this action does not count, since it runs, so its own dialog closes at
 * once also after it was taken up again. When another command of the dialog is held, the dialog is
 * closed as soon as no command of it is running any more. That close addresses the dialog this
 * action runs in (see {@link DialogScope}), not whatever dialog is on top by then.
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
			ViewContext viewContext = context instanceof ViewContext view ? view : null;
			SuspendedCommands suspended = viewContext != null ? viewContext.getScope(SuspendedCommands.class) : null;
			if (suspended != null && suspended.hasSuspended()) {
				closeWhenSettled(mgr, viewContext.getScope(DialogScope.class), suspended);
			} else {
				mgr.closeTopDialog(DialogResult.cancelled());
			}
		}
		return input;
	}

	/**
	 * Closes the given dialog once the given region has no running command any more; the topmost
	 * dialog when the dialog is not known.
	 */
	private static void closeWhenSettled(DialogManager mgr, DialogScope dialog, SuspendedCommands suspended) {
		Runnable[] stop = new Runnable[1];
		stop[0] = suspended.observe(() -> {
			if (!suspended.hasSuspended()) {
				stop[0].run();
				DialogHandle handle = dialog != null ? dialog.handle() : null;
				if (handle != null) {
					handle.close(DialogResult.cancelled());
				} else {
					mgr.closeTopDialog(DialogResult.cancelled());
				}
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
