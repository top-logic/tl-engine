/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;

/**
 * {@link ViewAction} that closes the topmost dialog.
 *
 * <p>
 * Passes the input through as output so it can be used in a chain where a subsequent action needs
 * the result.
 * </p>
 */
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
			mgr.closeTopDialog(DialogResult.cancelled());
		}
		return input;
	}
}
