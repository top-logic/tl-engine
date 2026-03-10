/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.layout.LoginHook;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.TLContext;

/**
 * {@link LoginHook} opening a login window for the anonymous user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenLoginDialogHook implements LoginHook {

	@Override
	public void handleLogin(DisplayContext context, MainLayout mainLayout, Runnable callback) {
		if (!TLContext.isAnonymous()) {
			callback.run();
			return;
		}
		LoginViewDialog dialog = new LoginViewDialog();
		dialog.getDialogModel().addListener(DialogModel.CLOSED_PROPERTY, runOnDialogClose(callback));
		dialog.open(context);
	}

	private static DialogClosedListener runOnDialogClose(Runnable callback) {
		return (Object sender, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				callback.run();
			}
		};
	}

}

