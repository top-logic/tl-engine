/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.UserVerificationRequirement;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * A started login with a passkey, see {@link PasskeyService#startAuthentication(Person)} and
 * {@link PasskeyService#finishAuthentication(PendingAuthentication, String)}.
 */
public final class PendingAuthentication extends PendingCeremony {

	private final AssertionRequest _request;

	PendingAuthentication(RelyingParty relyingParty, Person account, long timeout, AssertionRequest request) {
		super(relyingParty, account, timeout);
		_request = request;
	}

	/**
	 * Whether the login is passwordless: the user picks a discoverable passkey of any account, and
	 * the passkey is the only proof of the login.
	 */
	public boolean isPasswordless() {
		return getAccount() == null;
	}

	/**
	 * Whether the authenticator must verify the user (PIN, biometrics) for this login.
	 */
	public boolean requiresUserVerification() {
		return _request.getPublicKeyCredentialRequestOptions().getUserVerification()
			.map(UserVerificationRequirement.REQUIRED::equals)
			.orElse(false);
	}

	/**
	 * The request of the library, as sent to the browser.
	 */
	public AssertionRequest getRequest() {
		return _request;
	}

	@Override
	protected String optionsJson() throws JsonProcessingException {
		return _request.toCredentialsGetJson();
	}

}
