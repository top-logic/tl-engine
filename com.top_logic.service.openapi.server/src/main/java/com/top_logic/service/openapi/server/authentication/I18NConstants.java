/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 AUTH_FAILED_NO_COOKIE__PARAMETER;

	public static ResKey1 AUTH_FAILED_NO_QUERY_PARAM__PARAMETER;

	public static ResKey1 AUTH_FAILED_NO_HEADER__PARAMETER;

	public static ResKey1 AUTH_FAILED_INVALID_API_KEY__PARAMETER;

	public static ResKey AUTH_FAILED_NO_TOKEN;

	public static ResKey3 AUTH_FAILED_TOKEN_DECLINED__STATUS_CODE__CODE__DESCRIPTION;

	public static ResKey AUTH_FAILED_TOKEN_INACTIVE;

	/** @en Authentication token is expired at {0}. */
	public static ResKey1 AUTH_FAILED_TOKEN_EXPIRED__DATE;

	public static ResKey1 AUTH_FAILED_ILLEGAL_TOKEN_VALIDATION_RESPONSE__MSG;

	/** @en Missing authentication secret. */
	public static ResKey ERROR_MISSING_AUTHENTICATION_SECRET;

	public static ResKey1 ERROR_MULTIPLE_CLIENT_CREDENTIAL_SECRETS__DOMAIN;

	static {
		initConstants(I18NConstants.class);
	}
}
