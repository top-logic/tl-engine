/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.translation;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 HTTP_REQUEST_ERROR__REASON;

	public static ResKey REQUEST_EXCEEDS_MAX_ACCUMULATED_SIZE;

	/**
	 * @en Expected source and target language separated by white space.
	 */
	public static ResKey ERROR_EXPECTED_SOURCE_AND_TARGET_LANGUAGE;

	static {
		initConstants(I18NConstants.class);
	}
}
