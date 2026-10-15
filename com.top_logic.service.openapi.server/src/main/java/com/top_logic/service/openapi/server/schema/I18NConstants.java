/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.schema;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The schema is not valid JSON: {0}
	 */
	public static ResKey1 ERROR_SCHEMA_NOT_JSON__PROBLEM;

	/**
	 * @en The schema is not a JSON object.
	 */
	public static ResKey ERROR_SCHEMA_NOT_OBJECT;

	/**
	 * @en The global schema "{0}" is not valid JSON: {1}
	 */
	public static ResKey2 ERROR_GLOBAL_SCHEMA_NOT_JSON__NAME__PROBLEM;

	/**
	 * @en The schema cannot be compiled: {0}
	 */
	public static ResKey1 ERROR_SCHEMA_NOT_COMPILABLE__PROBLEM;

	static {
		initConstants(I18NConstants.class);
	}
}
