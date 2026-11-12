/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.mig.html.layout.LoginHook;
import com.top_logic.mig.html.layout.LoginHooks;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LoginHook} accepting the {@link Invitation} whose token is given in the request
 * parameter {@link InvitationLink#INVITATION_PARAMETER}.
 *
 * <p>
 * For an open invitation, the invited person confirms the address with a code
 * ({@link CheckAccountCode} of an {@link InvitationRequest}) and then creates the account
 * ({@link CreateAccountDialog}). For an
 * unknown, expired or accepted invitation, a message tells the reason.
 * </p>
 *
 * @see InvitationModule#findOpenInvitation(String)
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateLoginHook implements LoginHook {

	@Override
	public void handleLogin(MainLayout mainLayout, Runnable callback) {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();

		String token = context.asRequest().getParameter(InvitationLink.INVITATION_PARAMETER);
		if (StringServices.isEmpty(token)) {
			callback.run();
			return;
		}

		Invitation invitation;
		try {
			invitation = InvitationModule.getInstance().findOpenInvitation(token);
		} catch (TopLogicException ex) {
			DialogWindowControl dialog = MessageBox.newBuilder(MessageType.ERROR)
				.message(ex.getErrorKey())
				.buttons(MessageBox.button(ButtonType.OK))
				.toDialog();
			LoginHooks.runOnClose(dialog.getDialogModel(), callback);
			context.getWindowScope().openDialog(dialog);
			return;
		}

		CheckAccountCode checkCode = new CheckAccountCode(new InvitationRequest(invitation));
		LoginHooks.runOnClose(checkCode.getDialogModel(), callback);
		checkCode.open(context);
	}

}
