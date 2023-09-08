/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.util;

import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix MODEL_PART = legacyPrefix("");

	static {
		initConstants(I18NConstants.class);
	}
}
