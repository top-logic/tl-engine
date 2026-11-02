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
 * Typed arguments of the {@link ReactWebAuthnControl#CMD_ORIGIN} command: the origin of the page
 * that displays the control.
 */
@Label("Report the page origin of the WebAuthn button '{target}'")
public interface WebAuthnOriginArguments extends ReactCommand {

	/** @see #getOrigin() */
	String ORIGIN = "origin";

	/**
	 * The origin of the page as the browser sees it ({@code window.location.origin}), e.g.
	 * {@code https://app.example.com}: the origin a ceremony on this page reports to the relying
	 * party.
	 *
	 * @see WebAuthnListener#onPageOrigin(com.top_logic.layout.react.ReactContext, String)
	 */
	@Name(ORIGIN)
	@Mandatory
	String getOrigin();

	/** @see #getOrigin() */
	void setOrigin(String value);

}
