/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.check;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey5;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey5 CONSTRAINT_VIOLATION_WARNING__INTERFACE_PROPERTY_VALUE_CONSTRAINT_LOCATION;

	public static ResKey5 CONSTRAINT_VIOLATION_ERROR__INTERFACE_PROPERTY_VALUE_CONSTRAINT_LOCATION;

	static {
		initConstants(I18NConstants.class);
	}

}
