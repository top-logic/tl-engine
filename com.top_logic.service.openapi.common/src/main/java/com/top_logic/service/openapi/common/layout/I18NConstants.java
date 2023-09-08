/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey2 INVALID_URL__URL__SCHEMA;

	public static ResKey1 MISSING_CLIENT_CREDENTIALS_FLOW__SCHEMA;

	public static ResKey IMPORT_OPEN_API_CONFIG_DIALOG_TITLE;

	public static ResKey WARNING_WHEN_PARSING_OPEN_API_DOCUMENT;

	public static ResKey WARNING_WHEN_PROCESSING_OPEN_API_SPECIFICATION;

	/**
	 * @en No scheme (e.g. "Basic", "Bearer", or "Digest") given for security schema {0}.
	 */
	public static ResKey1 MISSING_HTTP_SCHEME__SCHEMA;

	/**
	 * @en The HTTP scheme {0} is currently not supported.
	 */
	public static ResKey2 MISSING_UNSUPPORTED_HTTP_SCHEME__SCHEME_SCHEMA;

	/**
	 * @en The given YAML file could not be transformed to JSON.
	 */
	public static ResKey ERROR_YAML_TO_JSON_FAILED;

	/**
	 * @en The reference {0} is not a reference to a globally defined parameter.
	 */
	public static ResKey1 REFERENCE_NOT_A_GLOBAL_PARAMETER__REFERENCE;

	/**
	 * @en The referenced parameter {0} could not be found. Available global parameters: {1}
	 */
	public static ResKey2 REFERENCED_PARAMETER_NOT_FOUND__REFERENCE__AVAILABLE;

	static {
		initConstants(I18NConstants.class);
	}
}
