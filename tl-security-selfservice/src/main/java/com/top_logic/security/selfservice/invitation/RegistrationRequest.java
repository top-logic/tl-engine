/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Locale;

import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AccountRequest} of a visitor registering an account without invitation.
 *
 * <p>
 * The code has been requested in the {@link RegisterDialog}. A new code needs a new captcha, so the
 * visitor is led back to that dialog with the address filled in. The account is created by
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
	public HTMLTemplateFragment getCodeMessage() {
		return fragment(
			tag(HTMLConstants.PARAGRAPH,
				resource(com.top_logic.security.selfservice.I18NConstants.REGISTRATION_CODE_SENT__EMAIL.fill(_email))),
			tag(HTMLConstants.PARAGRAPH,
				resource(I18NConstants.REGISTRATION_ENTER_CODE)));
	}

	@Override
	public String getThrottleKey() {
		return SelfRegistration.THROTTLE_ADDRESS_PREFIX + _email.toLowerCase(Locale.ROOT);
	}

	@Override
	public HandlerResult sendNewCode(DisplayContext context, CheckAccountCode dialog) {
		dialog.getDiscardClosure().executeCommand(context);
		return new RegisterDialog(_email).open(context);
	}

	@Override
	public Person createAccount(String surname, String givenName, char[] password) {
		return SelfRegistration.getInstance().register(_email, surname, givenName, password);
	}

	@Override
	public HandlerResult accountCreated(DisplayContext context, CreateAccountDialog dialog, Person account) {
		if (!account.isLocked()) {
			return dialog.logIn(context, account);
		}
		dialog.getDiscardClosure().executeCommand(context);
		return MessageBox.confirm(context, MessageType.INFO,
			com.top_logic.security.selfservice.I18NConstants.REGISTRATION_AWAITS_APPROVAL,
			MessageBox.button(ButtonType.OK));
	}

}
