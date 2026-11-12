/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.security.selfservice.invitation.InvitationModule;
import com.top_logic.security.selfservice.model.Invitation;

/**
 * {@link AccountRequest} of an invited person accepting an {@link Invitation}.
 *
 * <p>
 * A new code is mailed to the address of the invitation, the account is created by
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

	/**
	 * The invitation to accept.
	 */
	public Invitation getInvitation() {
		return _invitation;
	}

	@Override
	public String getEmail() {
		return _invitation.getEmail();
	}

	@Override
	public void checkOpen() {
		InvitationModule.checkOpen(_invitation);
	}

	@Override
	public void sendNewCode(ReactContext context) {
		RequestInvitationCodeAction.sendCode(context, _invitation);
	}

	@Override
	public Person createAccount(String surname, String givenName, char[] password) {
		return InvitationModule.getInstance().acceptInvitation(_invitation, surname, givenName, password);
	}

	@Override
	public void accountCreated(ReactContext context, Person account) {
		SelfServiceSupport.logIn(context, account);
	}

}
