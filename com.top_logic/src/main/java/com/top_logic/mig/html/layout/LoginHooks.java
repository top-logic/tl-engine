/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.mig.html.layout;

import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;

/**
 * Util class for {@link LoginHook}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LoginHooks {

	/**
	 * Executes the given callback when the given dialog is closed.
	 */
	public static void runOnClose(DialogModel dialogModel, Command closeCallback) {
		runOnClose(dialogModel, () -> closeCallback.executeCommand(DefaultDisplayContext.getDisplayContext()));
	}

	/**
	 * Executes the given callback when the given dialog is closed.
	 */
	public static void runOnClose(DialogModel dialogModel, Runnable closeCallback) {
		dialogModel.addListener(DialogModel.CLOSED_PROPERTY, runOnDialogClose(closeCallback));
	}

	private static DialogClosedListener runOnDialogClose(Runnable callback) {
		return (Object sender, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				callback.run();
			}
		};
	}

}

