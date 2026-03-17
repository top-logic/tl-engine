/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Logout
	 */
	public static ResKey LOGOUT;

	/**
	 * @en Login
	 */
	public static ResKey LOGIN;

	/**
	 * @en Name
	 */
	public static ResKey LOGIN_DIALOG_USERNAME_FIELD;

	/**
	 * @en Password
	 */
	public static ResKey LOGIN_DIALOG_PASSWORD_FIELD;

	/**
	 * @en Change password
	 */
	public static ResKey CHANGE_PASSWORD;

	/**
	 * @en Too much unsuccessful login attempts. The account has been temporarily deactivated.
	 *     Please wait {0} seconds before retry.
	 */
	public static ResKey1 ERROR_TOO_MANY_LOGIN_ATTEMPS__TIMEOUT;

	public static ResKey1 RELOAD_FAILED__NAMES;

	public static ResKey1 RESTART_OF_THEME_FACTORY_FAILED__PROBLEM;

	public static ResKey1 NO_CONTENT_GIVEN__VIEW_NAME;

	/**
	 * @en A QR code could not be generated to initialize multi-factor authentication.
	 */
	public static ResKey UNABLE_TO_CREATE_QR_CODE;

	/**
	 * @en Please scan the QR code below with your preferred authenticator app to set up
	 *     multi-factor authentication.
	 */
	public static ResKey INIT_AUTHENTICATOR_MESSAGE;

	/**
	 * @en MFA password updated
	 */
	public static ResKey MFA_PASSWORD_UPDATED;

	/**
	 * @en Enable multi-factor authentication
	 */
	public static ResKey ENABLE_MFA_AUTHENTICATION;

	/**
	 * @en The one-time password is wrong.
	 */
	public static ResKey INVALID_OTP;

	/**
	 * @en Check one-time password
	 */
	public static ResKey CHECK_OTP_DIALOG_TITLE;

	/**
	 * @en The one-time password
	 */
	public static ResKey CHECK_OTP_FIELD_LABEL;

	/**
	 * @en Please enter the one-time password from your authenticator app.
	 */
	public static ResKey CHECK_OTP_FIELD_MESSAGE;

	/**
	 * @en Too much unsuccessful attempts to confirm one-time password. Please wait {0} seconds for
	 *     the next attempt.
	 */
	public static ResKey1 CHECK_OTP_TOO_MANY_ATTEMPTS__TIMEOUT;

	/**
	 * @en Multi-factor authentication is required for your account. Please confirm the message to
	 *     begin setting up multi-factor authentication.
	 */
	public static ResKey MFA_REQUIRED_MESSAGE;

	/**
	 * @en Multi-factor authentication is optional for your account. You can start setting it up now
	 *     by confirming this message, or you can do it later. You can enable multi-factor
	 *     authentication at any time in your personal settings.
	 */
	public static ResKey MFA_OPTIONAL_MESSAGE;

	static {
		initConstants(I18NConstants.class);
	}
}
