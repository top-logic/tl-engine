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
 * PendingAuthentication, String)} established it.
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
		return service().startAuthentication(account, request());
	}

	@Override
	protected Person finish(PendingAuthentication pending, String credentialJson) {
		return service().finishAuthentication(pending, credentialJson);
	}

}
