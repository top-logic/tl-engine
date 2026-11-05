/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * What the passkeys of an application are for, see {@link PasskeyService.Config#getUsage()}.
 *
 * <p>
 * An application uses its passkeys either for a login with a passkey alone or as second factor
 * after the password, never for both: a passkey that logs in by itself would otherwise also be
 * demanded after the password, and the second factor would not add a second proof.
 * </p>
 */
public enum PasskeyUsage implements ExternallyNamed {

	/**
	 * A passkey alone logs its account in, without the password.
	 *
	 * <p>
	 * Passkeys are created discoverable, so that the user picks one without giving a user name,
	 * and the authenticator always verifies its user (PIN, biometrics), since the passkey is the
	 * only proof of the login. Passkeys are no second factor.
	 * </p>
	 *
	 * @see PasskeyService#startAuthentication(Person)
	 */
	LOGIN("login"),

	/**
	 * A passkey confirms the login of an account after its password, as second factor.
	 *
	 * <p>
	 * Passkeys are created non-discoverable where the authenticator allows it, and the
	 * authenticator verifies its user as {@link PasskeyService.Config#getUserVerification()
	 * configured}. No login with a passkey alone is offered.
	 * </p>
	 */
	SECOND_FACTOR("second-factor"),

	;

	private final String _externalName;

	private PasskeyUsage(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
