/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.layout;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en The parameter type {1} in parameter {0} is not supported. */
	public static ResKey2 UNSUPPORTED_PARAMETER_TYPE__PARAMETER__TYPE;

	/** @en Schema definition of parameter ''{0}'' is invalid: {1} */
	public static ResKey2 INVALID_SCHEMA_DEFINITION__PARAMETER_PROBLEM;

	/** @en Global schema definition ''{2}'' in parameter ''{0}'' is invalid: {1} */
	public static ResKey3 INVALID_GLOBAL_SCHEMA_DEFINITION__PARAMETER_NAME__PROBLEM__SCHEMA_NAME;

	/** @en Missing global schema reference ''{1}'' in parameter ''{0}''. */
	public static ResKey2 MISSING_GLOBAL_SCHEMA_DEFINITION__PARAMETER_SCHEMA;

	/** @en Invalid schema reference ''{1}'' in parameter ''{0}''. */
	public static ResKey2 INVALID_GLOBAL_SCHEMA_DEFINITION__PARAMETER_SCHEMA;

	static {
		initConstants(I18NConstants.class);
	}
}
