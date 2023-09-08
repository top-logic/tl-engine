/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey MAX_USERS_EXCEEDED;

	public static ResKey COMMIT_FAILED;

	public static ResKey ERROR_AUTHENTICATE = legacyKey("logout.messages.errorauthenticate");

	public static ResKey1 ERROR_AUTHENTICATE_MAINTENANCE_MODE =
		legacyKey1("logout.messages.errorauthenticatemaintenancemode");

	public static ResKey1 ERROR_AUTHENTICATE_MAINTENANCE_MODE_SOON =
		legacyKey1("logout.messages.errorauthenticatemaintenancemodesoon");

	public static ResKey SESSION_INVALID = legacyKey("logout.messages.invalidated");

	public static ResKey SESSION_NOT_FOUND = legacyKey("logout.messages.logout");

	/**
	 * @en Password change required
	 */
	public static ResKey PWD_CHANGE_TITLE;

	/**
	 * @en Passwords do not match.
	 */
	public static ResKey PWD_CHANGE_MSG_PASSWORDS_DONT_MATCH;

	/**
	 * @en Wrong old password.
	 */
	public static ResKey PWD_CHANGE_MSG_OLD_PASSWORD_WRONG;

	/**
	 * @en Wellcome {0}!
	 */
	public static ResKey1 PWD_CHANGE_WELLCOME__USER;

	/**
	 * @en Your password has expired.
	 */
	public static ResKey PWD_CHANGE_PASSWORD_EXPIRED;

	/**
	 * @en You need to change it.
	 */
	public static ResKey PWD_CHANGE_NEED_UPDATE;

	/**
	 * @en Password policy:
	 */
	public static ResKey PWD_CHANGE_POLICY;

	/**
	 * @en At least {0} characters in length
	 */
	public static ResKey1 PWD_CHANGE_MIN_LENGTH__LENGTH;

	/**
	 * @en Should contain at least {0} of the following:
	 */
	public static ResKey1 PWD_CHANGE_MIN_CRITERIA__CNT;

	/**
	 * @en upper case characters (A-B)
	 */
	public static ResKey PWD_CHANGE_CRITERIA_UPPER;

	/**
	 * @en lower case characters (a-b)
	 */
	public static ResKey PWD_CHANGE_CRITERIA_LOWER;

	/**
	 * @en numbers (0-9)
	 */
	public static ResKey PWD_CHANGE_CRITERIA_NUMBER;

	/**
	 * @en special characters (!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~)
	 */
	public static ResKey PWD_CHANGE_CRITERIA_SPECIAL;

	/**
	 * @en The password will expire after {0} days and has to be changed then.
	 */
	public static ResKey1 PWD_CHANGE_EXPIRY__DAYS;

	/**
	 * @en You may not reuse previously used passwords until after {0} change cycles.
	 */
	public static ResKey1 PWD_CHANGE_CYCLE__CNT;

	/**
	 * @en Current password
	 */
	public static ResKey PWD_CHANGE_CURRENT;

	/**
	 * @en New password
	 */
	public static ResKey PWD_CHANGE_NEW;

	/**
	 * @en Confirm password
	 */
	public static ResKey PWD_CHANGE_CONFIRM;

	/**
	 * @en Save and login
	 */
	public static ResKey PWD_CHANGE_SUBMIT;

	public static ResKey ERROR_NTLM_AUTHEMTICATION_FAILED = legacyKey("logout.messages.errorauthenticate.ntlm");

	public static ResKey ERROR_BASIC_AUTHENTICATION_FAILED = legacyKey("com.top_logic.base.accesscontrol.BasicAuthenticationServlet.noAuthorizationSent.redirectText");

	public static ResKey ERROR_SESSION_TIMED_OUT = legacyKey("logout.messages.timeout");

	/**
	 * @en The application {0} is buzzy. Please wait a moment and then try reloading the page.
	 */
	@CalledFromJSP
	public static ResKey1 APP_BUZZY__NAME;

	/**
	 * @en Close
	 */
	@CalledFromJSP
	public static ResKey APP_BUZZY_CLOSE;

	/**
	 * @en Reload
	 */
	@CalledFromJSP
	public static ResKey APP_BUZZY_RELOAD;

	static {
		initConstants(I18NConstants.class);
	}
}
