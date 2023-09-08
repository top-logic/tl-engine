/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.io.bindings;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 CANNOT_RESOLVE_MODEL_PART__REF__ATTR_DETAILS;

	public static ResKey3 INVALID_FORMAT__VALUE_ATTR_DETAILS;

	public static ResKey EXPECTED_END_OF_ELEMENT;

	public static ResKey3 NO_SUCH_CLASSIFIER__NAME_ATTR_ENUM;

	static {
		initConstants(I18NConstants.class);
	}
}
