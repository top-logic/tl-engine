/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.security.selfservice.invitation;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.mig.html.layout.LoginHook;
import com.top_logic.mig.html.layout.LoginHooks;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.security.selfservice.model.TlSecuritySelfserviceFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link LoginHooks} processing {@link Invitation} and creating persons..
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateLoginHoook implements LoginHook {

	/** Request parameter holding the {@link Invitation#getId() ID} of an {@link Invitation}. */
	public static final String INVITATION_ID = "invitation";

	@Override
	public void handleLogin(MainLayout mainLayout, Runnable callback) {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();

		String id = context.asRequest().getParameter(INVITATION_ID);
		if (StringServices.isEmpty(id)) {
			callback.run();
			return;
		}

		Invitation invitation = findInvitationWithID(id);
		if (invitation == null) {
			DialogWindowControl dialog = MessageBox.newBuilder(MessageType.ERROR)
				.message(I18NConstants.ERROR_NO_INVITATION__ID.fill(id))
				.buttons(MessageBox.button(ButtonType.OK))
				.toDialog();
			LoginHooks.runOnClose(dialog.getDialogModel(), callback);
			context.getWindowScope().openDialog(dialog);
			return;
		}
		CheckInvitationCode checkCode = new CheckInvitationCode(invitation, ctx -> {
			return new CreateAccountDialog(invitation, deleteInvitation(invitation)).open(ctx);
		});
		LoginHooks.runOnClose(checkCode.getDialogModel(), callback);
		checkCode.open(context);
		return;
	}

	private Command deleteInvitation(Invitation invitation) {
		return (ctx) -> {
			try(Transaction tx = invitation.tKnowledgeBase().beginTransaction(I18NConstants.DELETED_INVITATION)){
				invitation.tDelete();
				tx.commit();
			}
			return HandlerResult.DEFAULT_RESULT;
		};
	}

	private Invitation findInvitationWithID(String id) {
		try (CloseableIterator<Invitation> it =
			AttributeOperations.allInstancesIterator(TlSecuritySelfserviceFactory.getInvitationType(),
				Invitation.class)) {
			while (it.hasNext()) {
				Invitation invitation = it.next();
				if (id.equals(invitation.getId())) {
					return invitation;
				}
			}
		}
		return null;
	}

}

