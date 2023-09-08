/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.conf;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
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

	public static ResKey1 ERROR_MULTIPLE_REQUEST_BODY_PARAMETERS__PARAMETERS;

	public static ResKey1 ERROR_LOCAL_GLOBAL_PARAMETER_CLASH__CLASHES;

	public static ResKey2 ERROR_BODY_PARAM_UNSUPPORTED_FOR_METHOD__PARAM__METHOD;

	/** @en Path parameters are not allowed at operation definitions: {0} */
	public static ResKey1 ERROR_PATH_PARAMETER_NOT_ALLOWED_AT_OPERATION__PARAMS;

	/**
	 * @en Path parameters not defined: {0}
	 */
	public static ResKey1 ERROR_PATH_PARAMETER_NOT_DECLARED__PARAMS;

	/**
	 * @en The path parameters ''{0}'' not used. The path must either contain all or no path
	 *     parameters.
	 */
	public static ResKey1 WARN_PATH_PARAMETER_NOT_USED__PARAMS;

	static {
		initConstants(I18NConstants.class);
	}
}
