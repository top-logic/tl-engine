/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 * 
 * @see ResPrefix
 * @see ResKey
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Format ''{0}'' in parameter ''{1}'' is not supported.
	 */
	public static ResKey2 UNSUPPORTED_PARAMETER_FORMAT__FORMAT__PARAMETER;

	/**
	 * @en Type ''{0}'' in parameter ''{1}'' is not supported.
	 */
	public static ResKey2 UNSUPPORTED_PARAMETER_TYPE__TYPE__PARAMETER;

	/**
	 * @en No type found in schema for parameter {0}.
	 */
	public static ResKey1 MISSING_PARAMETER_TYPE__PARAMETER;

	/**
	 * @en Implementation for {1}:{0} is invalid: {2}
	 */
	public static ResKey3 INVALID_IMPLEMENTATION_FOR_OPERATION___PATH__METHOD__MSG;

	/**
	 * @en Invalid implementation type
	 */
	public static ResKey INVALID_IMPLEMENTATION_TYPE;

	/**
	 * @en The access to the <i>OpenAPI</i> server configuration failed.
	 */
	public static ResKey ACCESSING_SERVER_CONFIGURATION_FAILED;

	static {
		initConstants(I18NConstants.class);
	}
}
