/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * A started registration of a passkey for an account, see
 * {@link PasskeyService#startRegistration(Person)} and
 * {@link PasskeyService#finishRegistration(PendingRegistration, String, String)}.
 */
public final class PendingRegistration extends PendingCeremony {

	private final PublicKeyCredentialCreationOptions _request;

	PendingRegistration(RelyingParty relyingParty, Person account, long timeout,
			PublicKeyCredentialCreationOptions request) {
		super(relyingParty, account, timeout);
		_request = request;
	}

	/**
	 * The options of the library, as sent to the browser.
	 */
	public PublicKeyCredentialCreationOptions getRequest() {
		return _request;
	}

	@Override
	protected String optionsJson() throws JsonProcessingException {
		return _request.toCredentialsCreateJson();
	}

}
