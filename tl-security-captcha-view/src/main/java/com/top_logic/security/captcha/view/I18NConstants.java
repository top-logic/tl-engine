/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.captcha.view;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants of the captcha element: the texts of the ALTCHA widget.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en I'm not a robot
	 */
	public static ResKey WIDGET_LABEL;

	/**
	 * @en Loading...
	 */
	public static ResKey WIDGET_LOADING;

	/**
	 * @en Verifying...
	 */
	public static ResKey WIDGET_VERIFYING;

	/**
	 * @en Verified
	 */
	public static ResKey WIDGET_VERIFIED;

	/**
	 * @en Verification required.
	 */
	public static ResKey WIDGET_VERIFICATION_REQUIRED;

	/**
	 * @en Verifying... please wait.
	 */
	public static ResKey WIDGET_WAIT_ALERT;

	/**
	 * @en Verification failed. Try again later.
	 */
	public static ResKey WIDGET_ERROR;

	/**
	 * @en Verification expired. Try again.
	 */
	public static ResKey WIDGET_EXPIRED;

	/**
	 * @en Reload
	 */
	public static ResKey WIDGET_RELOAD;

	/**
	 * @en Verify
	 */
	public static ResKey WIDGET_VERIFY;

	static {
		initConstants(I18NConstants.class);
	}
}
