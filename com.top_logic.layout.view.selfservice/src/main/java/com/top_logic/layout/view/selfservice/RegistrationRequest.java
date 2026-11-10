/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.security.selfservice.invitation.SelfRegistration;
import com.top_logic.security.selfservice.invitation.SelfRegistrationMode;

/**
 * {@link AccountRequest} of a visitor registering an account without invitation.
 *
 * <p>
 * A new code needs a new captcha, so the visitor is led back to the registration dialog with the
 * address filled in. The account is created by
 * {@link SelfRegistration#register(String, String, String, char[])}. An account that can log in
 * ({@link SelfRegistrationMode#DIRECT}) is logged in afterwards; a locked account, which waits for
 * the approval of an administrator ({@link SelfRegistrationMode#APPROVAL}), is not: the dialog
 * closes and the visitor is told about the approval.
 * </p>
 */
public class RegistrationRequest implements AccountRequest {

	private final String _email;

	/**
	 * Creates a {@link RegistrationRequest}.
	 *
	 * @param email
	 *        The address a verification code has been requested for.
	 */
	public RegistrationRequest(String email) {
		_email = email;
	}

	@Override
	public String getEmail() {
		return _email;
	}

	@Override
	public void checkOpen() {
		// The registration checks the address and whether registration is enabled.
	}

	@Override
	public void sendNewCode(ReactContext context) {
		RequestRegistrationCodeAction.openRegistrationDialog(context, _email);
	}

	@Override
	public Person createAccount(String surname, String givenName, char[] password) {
		return SelfRegistration.getInstance().register(_email, surname, givenName, password);
	}

	@Override
	public void accountCreated(ReactContext context, Person account) {
		if (account.isLocked()) {
			SelfServiceSupport.closeTopDialog(context);
			InfoService.showInfo(I18NConstants.REGISTRATION_AWAITS_APPROVAL);
		} else {
			SelfServiceSupport.logIn(context, account);
		}
	}

}
