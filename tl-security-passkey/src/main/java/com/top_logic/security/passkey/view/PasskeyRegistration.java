/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import java.util.Date;
import java.util.function.BiConsumer;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.webauthn.WebAuthnCeremony;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.security.passkey.PendingRegistration;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.util.Resources;

/**
 * {@link PasskeyCeremony} creating a passkey for the account it is {@link #offer(Person) offered}
 * for.
 *
 * <p>
 * The passkey is named after the day of its creation (see {@link #newLabel()}); the user can rename
 * it later. The success handler receives the stored passkey.
 * </p>
 */
public class PasskeyRegistration extends PasskeyCeremony<PendingRegistration, Passkey> {

	/**
	 * Creates a {@link PasskeyRegistration}.
	 *
	 * @param onRegistered
	 *        Receives the stored passkey.
	 */
	public PasskeyRegistration(BiConsumer<ReactContext, Passkey> onRegistered) {
		super(onRegistered);
	}

	@Override
	protected WebAuthnCeremony kind() {
		return WebAuthnCeremony.CREATE;
	}

	@Override
	protected PendingRegistration start(Person account) {
		return service().startRegistration(account, request());
	}

	@Override
	protected Passkey finish(PendingRegistration pending, String credentialJson) {
		return service().finishRegistration(pending, credentialJson, newLabel());
	}

	/**
	 * The name of a passkey created now: "Passkey" and the date.
	 */
	protected String newLabel() {
		String date = HTMLFormatter.getInstance().formatDate(new Date());
		return Resources.getInstance().getString(I18NConstants.PASSKEY_LABEL__DATE.fill(date));
	}

}
