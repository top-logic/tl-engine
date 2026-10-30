/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.webauthn;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactWebAuthnControl#CMD_CREDENTIAL} command: the credential the
 * browser produced.
 */
@Label("Complete the WebAuthn ceremony of '{target}'")
public interface WebAuthnCredentialArguments extends ReactCommand {

	/** @see #getCredential() */
	String CREDENTIAL = "credential";

	/**
	 * The credential as JSON text, in the shape of {@code PublicKeyCredential.toJSON()}.
	 *
	 * @see WebAuthnListener#onCredential(com.top_logic.layout.react.ReactContext, String)
	 */
	@Name(CREDENTIAL)
	@Mandatory
	String getCredential();

	/** @see #getCredential() */
	void setCredential(String value);

}
