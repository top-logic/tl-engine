/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants of the UI-independent self-service operations.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en There is no invitation for this link. The link may be incomplete, or the invitation may
	 *     have been sent again with a new link or revoked.
	 */
	public static ResKey ERROR_UNKNOWN_INVITATION;

	/**
	 * @en The invitation has expired. Please ask for a new invitation.
	 */
	public static ResKey ERROR_INVITATION_EXPIRED;

	/**
	 * @en The invitation has already been used to create an account.
	 */
	public static ResKey ERROR_INVITATION_ACCEPTED;

	/**
	 * @en The authentication device "{0}" for accounts created from invitations does not exist.
	 */
	public static ResKey1 ERROR_NO_AUTHENTICATION_DEVICE__ID;

	/**
	 * @en No password was entered.
	 */
	public static ResKey ERROR_PASSWORD_EMPTY;

	/**
	 * @en The account cannot be created.
	 */
	public static ResKey ERROR_ACCOUNT_CREATION_DENIED;

	/**
	 * @en The public address of the application is not configured and there is no current request,
	 *     so no invitation link can be built. Set it with the variable "tl_host" (alias %HOST%), e.g.
	 *     "https://app.example.com".
	 */
	public static ResKey ERROR_NO_PUBLIC_ADDRESS;

	/**
	 * @en The verification code has expired or was entered wrongly too often. Please request a new
	 *     code.
	 */
	public static ResKey ERROR_VERIFICATION_CODE_EXPIRED;

	/**
	 * @en The verification code is not correct.
	 */
	public static ResKey ERROR_VERIFICATION_CODE_MISMATCH;

	/**
	 * @en The verification code was entered wrongly too often. Please request a new code.
	 */
	public static ResKey ERROR_VERIFICATION_CODE_TOO_MANY_MISMATCHES;

	/**
	 * @en A new verification code can be requested in {0} seconds.
	 */
	public static ResKey1 ERROR_VERIFICATION_CODE_REQUESTED_TOO_OFTEN__SECONDS;

	/**
	 * @en The verification code could not be sent.
	 */
	public static ResKey ERROR_VERIFICATION_CODE_NOT_SENT;

	/**
	 * @en The verification code cannot be sent because the application cannot send mails. Please
	 *     ask the administrator.
	 */
	public static ResKey ERROR_MAIL_NOT_CONFIGURED;

	/**
	 * @en Created invitation for {0}.
	 */
	public static ResKey1 CREATED_INVITATION__EMAIL;

	/**
	 * @en Renewed invitation for {0}.
	 */
	public static ResKey1 RENEWED_INVITATION__EMAIL;

	/**
	 * @en Accepted invitation for {0}.
	 */
	public static ResKey1 ACCEPTED_INVITATION__EMAIL;

	static {
		initConstants(I18NConstants.class);
	}
}
