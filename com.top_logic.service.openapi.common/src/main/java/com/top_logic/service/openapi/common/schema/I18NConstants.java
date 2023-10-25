/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The value of {0} must not be an array schema.
	 */
	public static ResKey1 ITEMS_MUST_NOT_BE_AN_ARRAY_SCHEMA__PROPERTY;

	/**
	 * @en Value {0} for key {1} is not of expected type {2}.
	 */
	public static ResKey3 UNEXPECTED_VALUE_TYPE__VALUE__KEY__EXPECTED_TYPE;

	/**
	 * @en Not an object schema: {0}.
	 */
	public static ResKey1 NO_OBJECT_SCHEMA__SCHEMA;

	static {
		initConstants(I18NConstants.class);
	}
}
