/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The call "{0}" with the arguments {1} failed: {2}
	 */
	public static ResKey3 ERROR_REQUEST_FAILED__METHOD_ARGS_MESSAGE;

	/**
	 * @en Syntax
	 */
	public static ResKey MESSAGE_DOC_SYNTAX_HEADER;

	/**
	 * @en Description
	 */
	public static ResKey MESSAGE_DOC_DESCRIPTION_HEADER;

	/**
	 * @en Parameters
	 */
	public static ResKey MESSAGE_DOC_PARAMETERS_HEADER;

	/**
	 * @en Name
	 */
	public static ResKey MESSAGE_DOC_PARAMETERS_COLUMN_NAME;

	/**
	 * @en Type
	 */
	public static ResKey MESSAGE_DOC_PARAMETERS_COLUMN_TYPE;

	/**
	 * @en Description
	 */
	public static ResKey MESSAGE_DOC_PARAMETERS_COLUMN_DESCRIPTION;

	/**
	 * @en Mandatory
	 */
	public static ResKey MESSAGE_DOC_PARAMETERS_COLUMN_MANDATORY;

	/**
	 * @en Default
	 */
	public static ResKey MESSAGE_DOC_PARAMETERS_COLUMN_DEFAULT;

	static {
		initConstants(I18NConstants.class);
	}
}
