/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The refusal of a credential the browser answered a passkey ceremony with, see
 * {@link PasskeyService#finishRegistration(PendingRegistration, String, String)} and
 * {@link PasskeyService#finishAuthentication(PendingAuthentication, String)}.
 *
 * <p>
 * Besides the message for the user, it tells {@link #getCategory() why} the credential was refused
 * and {@link #getCredentialId() which} credential it was, and whether the relying party does not
 * know that credential ({@link #isCredentialUnknown()}), so that the browser can be told to forget
 * it.
 * </p>
 */
public class PasskeyRefusedException extends TopLogicException {

	/**
	 * Why a credential was refused.
	 */
	public enum Category {

		/** The ceremony ran on a page whose origin the relying party does not accept. */
		WRONG_ORIGIN,

		/** No account of the application has a passkey with the credential ID. */
		UNKNOWN_PASSKEY,

		/** The passkey belongs to another account than the one the login was started for. */
		OTHER_ACCOUNT,

		/** The authenticator did not verify its user, although required. */
		USER_NOT_VERIFIED,

		/** The signature counter of the passkey did not increase: a sign of a cloned authenticator. */
		SIGNATURE_COUNTER,

		/** The ceremony was answered before or has expired. */
		EXPIRED,

		/** The credential could not be parsed. */
		INVALID_DATA,

		/** A registration created a credential that is already registered. */
		ALREADY_REGISTERED,

		/** The account must not use passkeys. */
		NOT_ALLOWED,

		/** The registration has no name for the passkey. */
		LABEL_MISSING,

		/** Another check of the relying party failed, see the cause. */
		FAILED;

	}

	private final Category _category;

	private final String _credentialId;

	private final boolean _credentialUnknown;

	/**
	 * Creates a {@link PasskeyRefusedException}.
	 *
	 * @param category
	 *        Why the credential was refused.
	 * @param message
	 *        The message for the user.
	 * @param credentialId
	 *        The base64url-encoded ID of the refused credential; <code>null</code> if it is not
	 *        known.
	 * @param credentialUnknown
	 *        Whether the relying party does not know the credential, so that an authenticator
	 *        holding it may forget it.
	 * @param cause
	 *        The refusal of the library, <code>null</code> if none.
	 */
	public PasskeyRefusedException(Category category, ResKey message, String credentialId,
			boolean credentialUnknown, Throwable cause) {
		super(message, cause);
		_category = category;
		_credentialId = credentialId;
		_credentialUnknown = credentialUnknown;
	}

	/**
	 * Why the credential was refused.
	 */
	public Category getCategory() {
		return _category;
	}

	/**
	 * The base64url-encoded ID of the refused credential, <code>null</code> if the answer of the
	 * browser could not be parsed.
	 */
	public String getCredentialId() {
		return _credentialId;
	}

	/**
	 * Whether the relying party does not know the {@link #getCredentialId() refused credential}: a
	 * login with a passkey no account has, or a refused registration, after which the authenticator
	 * holds a key the application never stored.
	 */
	public boolean isCredentialUnknown() {
		return _credentialUnknown && _credentialId != null;
	}

}
