/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.documentation;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No description available.
	 */
	public static ResKey MESSAGE_DOC_NO_DESCRIPTION;

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
