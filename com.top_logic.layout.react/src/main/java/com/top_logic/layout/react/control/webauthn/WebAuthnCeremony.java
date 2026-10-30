/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.webauthn;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The kind of ceremony a {@link ReactWebAuthnControl} runs in the browser.
 */
public enum WebAuthnCeremony implements ExternallyNamed {

	/**
	 * Registration: {@code navigator.credentials.create()} creates a new credential from
	 * {@code PublicKeyCredentialCreationOptions}.
	 */
	CREATE("create"),

	/**
	 * Authentication: {@code navigator.credentials.get()} proves the possession of an existing
	 * credential, from {@code PublicKeyCredentialRequestOptions}.
	 */
	GET("get");

	private final String _externalName;

	WebAuthnCeremony(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
