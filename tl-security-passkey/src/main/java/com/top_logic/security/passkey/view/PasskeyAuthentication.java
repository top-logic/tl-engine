/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import java.util.function.BiConsumer;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.webauthn.WebAuthnCeremony;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PendingAuthentication;

/**
 * {@link PasskeyCeremony} proving the possession of a passkey: a login.
 *
 * <p>
 * {@link #offer(Person) Offered} for an account, only the passkeys of that account are accepted (the
 * passkey as second factor after the password); offered for no account, the user picks a passkey of
 * any account and the passkey is the only proof (a login with a passkey alone). The success handler
 * receives the account the passkey belongs to, as {@link PasskeyService#finishAuthentication(
 * PendingAuthentication, String)} established it. Before, the browser is told which passkeys the
 * relying party accepts for that account, see
 * {@link com.top_logic.layout.react.control.webauthn.ReactWebAuthnControl#signalAllAcceptedCredentials(String, String, java.util.Collection)}.
 * </p>
 */
public class PasskeyAuthentication extends PasskeyCeremony<PendingAuthentication, Person> {

	/**
	 * Creates a {@link PasskeyAuthentication}.
	 *
	 * @param onAuthenticated
	 *        Receives the account that proved itself with its passkey.
	 */
	public PasskeyAuthentication(BiConsumer<ReactContext, Person> onAuthenticated) {
		super(onAuthenticated);
	}

	@Override
	protected WebAuthnCeremony kind() {
		return WebAuthnCeremony.GET;
	}

	@Override
	protected PendingAuthentication start(Person account) {
		return service().startAuthentication(account);
	}

	@Override
	protected Person finish(PendingAuthentication pending, String credentialJson) {
		return service().finishAuthentication(pending, credentialJson);
	}

	/**
	 * Tells the browser which passkeys the relying party accepts for the account that logged in,
	 * so that the authenticator removes or hides the ones the application no longer has.
	 */
	@Override
	protected void succeeded(Person account) {
		PasskeyService service = service();
		String rpId = service.getRpId();
		if (rpId == null) {
			return;
		}
		getControl().signalAllAcceptedCredentials(rpId, service.getUserId(account), service.getCredentialIds(account));
	}

}
