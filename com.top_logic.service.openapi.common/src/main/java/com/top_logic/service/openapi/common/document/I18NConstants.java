/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ERROR_REQUIRE_MUST_BE_SET_ON_PATH_PARAMETER;

	/**
	 * @en The parameter is not an JSON object: {0}
	 */
	public static ResKey1 PARAMETER_IS_NOT_AN_OBJECT__PARAMETER;

	/**
	 * @en The reference is not a string: {0}
	 */
	public static ResKey1 REFERENCE_PARAMETER_IS_NOT_A_STRING__REFERENCE;
	
	static {
		initConstants(I18NConstants.class);
	}
}
