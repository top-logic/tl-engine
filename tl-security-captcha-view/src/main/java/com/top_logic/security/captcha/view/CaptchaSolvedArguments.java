/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha.view;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link CaptchaControl#CMD_SOLVED} command: the solution of the challenge.
 */
@Label("Solve the captcha '{target}'")
public interface CaptchaSolvedArguments extends ReactCommand {

	/** @see #getPayload() */
	String PAYLOAD = "payload";

	/**
	 * The solution as the ALTCHA widget submits it, the Base64 encoded JSON of the challenge and its
	 * solution; <code>null</code> when the widget no longer holds a solution.
	 */
	@Name(PAYLOAD)
	@Nullable
	String getPayload();

	/** @see #getPayload() */
	void setPayload(String value);

}
