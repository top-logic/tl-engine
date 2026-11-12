/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * The request of a person without account to create one for an e-mail address, after proving with
 * a {@link VerificationCode} that the person reads the mails of the address.
 *
 * <p>
 * The code dialog {@link CheckAccountCode} and the account dialog {@link CreateAccountDialog} are
 * shared by all ways to an account. They leave to the request what differs: the text of the code
 * dialog, sending a new code, creating the account and what follows.
 * </p>
 *
 * @see InvitationRequest
 * @see RegistrationRequest
 */
public interface AccountRequest {

	/**
	 * The e-mail address the account is created for, its user name.
	 */
	String getEmail();

	/**
	 * The text of the code dialog, above the field for the code.
	 */
	HTMLTemplateFragment getCodeMessage();

	/**
	 * The key under which requests for a code are throttled, see
	 * {@link VerificationCode#throttleRequest(String)}; a correct code lifts the throttling.
	 */
	String getThrottleKey();

	/**
	 * Sends a new verification code, or leads the person to where a new code is requested.
	 *
	 * @param context
	 *        The context of the command asking for the new code.
	 * @param dialog
	 *        The code dialog.
	 * @return The result of the command.
	 */
	HandlerResult sendNewCode(DisplayContext context, CheckAccountCode dialog);

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
	 *        The context of the command that created the account.
	 * @param dialog
	 *        The account dialog.
	 * @param account
	 *        The account {@link #createAccount(String, String, char[]) created}.
	 * @return The result of the command.
	 */
	HandlerResult accountCreated(DisplayContext context, CreateAccountDialog dialog, Person account);

}
