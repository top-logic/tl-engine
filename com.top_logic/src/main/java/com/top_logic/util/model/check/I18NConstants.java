/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 ERROR_CONSTRAINT_VIOLATED__OBJECT_ATTRIBUTE_MESSAGE;

	public static ResKey2 ERROR_STRING_TO_SMALL__LENGTH_MIN;

	public static ResKey2 ERROR_STRING_TO_LARGE__LENGTH_MAX;

	public static ResKey2 ERROR_NUMBER_TO_SMALL__VALUE_MIN;

	public static ResKey2 ERROR_NUMBER_TO_HIGH__VALUE_MAX;

	public static ResKey OBJECT_WITHOUT_NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
