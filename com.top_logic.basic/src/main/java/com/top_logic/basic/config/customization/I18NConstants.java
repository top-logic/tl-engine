/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.customization;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey2 ERROR_NO_SUCH_PROPERTY__TYPE_NAME;

	public static ResKey1 ERROR_MISSING_PROPERTY_NAME_SEPARATOR__VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
