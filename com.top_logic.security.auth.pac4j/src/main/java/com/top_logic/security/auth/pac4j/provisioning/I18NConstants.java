/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.provisioning;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The identity provider has not confirmed your e-mail address. An account is created only
	 *     for a confirmed e-mail address.
	 */
	public static ResKey ERROR_EMAIL_NOT_VERIFIED;

	/**
	 * @en The identity provider has not reported an e-mail address. An account is created only for
	 *     a known e-mail address.
	 */
	public static ResKey ERROR_NO_EMAIL;

	/**
	 * @en An account is not created for e-mail addresses of the domain "{0}".
	 */
	public static ResKey1 ERROR_EMAIL_DOMAIN_NOT_ALLOWED__DOMAIN;

	/**
	 * @en The login name "{0}" is already taken by an account that is not connected with your
	 *     login. Ask the administrator to connect it.
	 */
	public static ResKey1 ERROR_NAME_TAKEN__NAME;

	/**
	 * @en The account "{0}" is already connected with another identity of this login.
	 */
	public static ResKey1 ERROR_OTHER_IDENTITY__NAME;

	/**
	 * @en The identity provider has not reported a login name.
	 */
	public static ResKey ERROR_NO_LOGIN_NAME;

	/**
	 * @en Login through an external identity provider.
	 */
	public static ResKey EXTERNAL_LOGIN;

	static {
		initConstants(I18NConstants.class);
	}
}
