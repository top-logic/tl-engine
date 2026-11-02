/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.webauthn;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactWebAuthnControl#CMD_FAILURE} command (the error the browser
 * reported for a ceremony) and of the {@link ReactWebAuthnControl#CMD_UNAVAILABLE} command (why the
 * page cannot run a ceremony).
 */
@Label("Fail the WebAuthn ceremony of '{target}' with '{errorName}'")
public interface WebAuthnFailureArguments extends ReactCommand {

	/** @see #getErrorName() */
	String ERROR_NAME = "errorName";

	/** @see #getMessage() */
	String MESSAGE = "message";

	/**
	 * The name of the {@code DOMException} the browser reported, e.g. {@code NotAllowedError}, or one
	 * of the names of the client, {@link ReactWebAuthnControl#INSECURE_CONTEXT_ERROR} and
	 * {@link ReactWebAuthnControl#WEB_AUTHN_UNAVAILABLE_ERROR}.
	 *
	 * @see WebAuthnListener#onFailure(com.top_logic.layout.react.ReactContext, String, String)
	 */
	@Name(ERROR_NAME)
	@Mandatory
	String getErrorName();

	/** @see #getErrorName() */
	void setErrorName(String value);

	/**
	 * The technical message of the browser or the client, not localized.
	 */
	@Name(MESSAGE)
	@Nullable
	String getMessage();

	/** @see #getMessage() */
	void setMessage(String value);

}
