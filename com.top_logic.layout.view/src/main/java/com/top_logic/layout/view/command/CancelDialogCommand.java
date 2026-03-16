/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that closes the topmost open dialog without propagating any results.
 *
 * <p>
 * This command is intended for "Cancel" or "Close" buttons inside dialog view XML files. It takes
 * no configuration beyond the standard {@link ViewCommand.Config} properties (label, image, etc.).
 * </p>
 */
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
	public HandlerResult execute(ReactContext context, Object input) {
		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		mgr.closeTopDialog(DialogResult.cancelled());
		return HandlerResult.DEFAULT_RESULT;
	}
}
