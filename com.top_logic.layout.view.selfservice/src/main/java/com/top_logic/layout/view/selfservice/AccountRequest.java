/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.security.selfservice.invitation.VerificationCode;
import com.top_logic.util.error.TopLogicException;

/**
 * The request of a person without account to create one for an e-mail address, after proving with
 * a {@link VerificationCode} that the person reads the mails of the address.
 *
 * <p>
 * The code dialog ({@link CheckVerificationCodeAction#CODE_VIEW}) and the account dialog
 * ({@link CheckVerificationCodeAction#CREATE_ACCOUNT_VIEW}) are shared by all ways to an account:
 * they carry the request on their channel {@link #REQUEST_CHANNEL}, which stays on the server, and
 * leave to it what differs - sending a new code, creating the account and what follows.
 * </p>
 *
 * @see InvitationRequest
 * @see RegistrationRequest
 */
public interface AccountRequest {

	/** Dialog channel carrying the {@link AccountRequest}. */
	String REQUEST_CHANNEL = "request";

	/** Dialog channel carrying the {@link VerificationCode} that was mailed. */
	String VERIFICATION_CHANNEL = "verification";

	/**
	 * The e-mail address the account is created for, its user name.
	 */
	String getEmail();

	/**
	 * Checks that the account can still be created.
	 *
	 * @throws TopLogicException
	 *         With the reason why the request cannot be continued.
	 */
	void checkOpen();

	/**
	 * Sends a new verification code, or leads the person to where a new code is requested.
	 *
	 * @param context
	 *        The context of the dialog asking for the new code.
	 */
	void sendNewCode(ReactContext context);

	/**
	 * Creates the account.
	 *
	 * @param surname
	 *        The surname of the person.
	 * @param givenName
	 *        The given name of the person, may be <code>null</code>.
	 * @param password
	 *        The password of the account; <code>null</code> if the authentication device does not
	 *        manage passwords. The caller clears it after the call.
	 * @return The created account.
	 * @throws TopLogicException
	 *         If the account is refused.
	 */
	Person createAccount(String surname, String givenName, char[] password);

	/**
	 * Continues after the account has been created, e.g. logs it in.
	 *
	 * @param context
	 *        The context of the account dialog.
	 * @param account
	 *        The account {@link #createAccount(String, String, char[]) created}.
	 */
	void accountCreated(ReactContext context, Person account);

}
