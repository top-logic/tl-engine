/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AccountRequest} of an invited person accepting an {@link Invitation}.
 *
 * <p>
 * The invited person requests the code in the code dialog; it is mailed through
 * {@link InvitationModule#sendVerificationMail(Invitation, String)} to the address of the
 * invitation. The account is created by
 * {@link InvitationModule#acceptInvitation(Invitation, String, String, char[])} and logged in
 * afterwards.
 * </p>
 */
public class InvitationRequest implements AccountRequest {

	private final Invitation _invitation;

	/**
	 * Creates an {@link InvitationRequest}.
	 *
	 * @param invitation
	 *        The invitation to accept.
	 */
	public InvitationRequest(Invitation invitation) {
		_invitation = invitation;
	}

	@Override
	public String getEmail() {
		return _invitation.getEmail();
	}

	@Override
	public HTMLTemplateFragment getCodeMessage() {
		return fragment(
			tag(HTMLConstants.PARAGRAPH,
				resource(I18NConstants.MESSAGE_WELCOME_TO_APPLICATION__APPLICATION
					.fill(InvitationModule.applicationTitle()))),
			tag(HTMLConstants.PARAGRAPH,
				resource(I18NConstants.MESSAGE_REQUEST_VERIFICATION_CODE)));
	}

	@Override
	public String getThrottleKey() {
		return _invitation.getId() + "@" + "code";
	}

	@Override
	public HandlerResult sendNewCode(DisplayContext context, CheckAccountCode dialog) {
		return dialog.updateCode(this::sendCode, this::getThrottleKey);
	}

	private void sendCode(String code) {
		if (InvitationModule.getInstance().sendVerificationMail(_invitation, code)) {
			InfoService.showInfo(I18NConstants.MESSAGE_VERIFICATION_CODE_SENT);
		} else {
			InfoService.showError(I18NConstants.ERROR_VERIFICATION_CODE_NOT_SENT);
		}
	}

	@Override
	public Person createAccount(String surname, String givenName, char[] password) {
		return InvitationModule.getInstance().acceptInvitation(_invitation, surname, givenName, password);
	}

	@Override
	public HandlerResult accountCreated(DisplayContext context, CreateAccountDialog dialog, Person account) {
		return dialog.logIn(context, account);
	}

}
