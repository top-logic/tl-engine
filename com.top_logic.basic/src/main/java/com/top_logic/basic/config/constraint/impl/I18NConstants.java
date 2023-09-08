/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey POSITIVE_VALUE_EXPECTED;

	public static ResKey NON_NEGATIVE_VALUE_EXPECTED;

	public static ResKey NEGATIVE_VALUE_EXPECTED;

	public static ResKey NON_POSITIVE_VALUE_EXPECTED;

	public static ResKey1 MUST_ONLY_BE_SET_IF_OTHER_IS_UNSET__OTHER;

	/** @en The value {0} is not a valid URL: {1} */
	public static ResKey2 NO_URL_FORMAT__VALUE__MESSAGE;

	/** @en The value {0} is not a valid URL: No protocol given. */
	public static ResKey1 NO_URL_FORMAT_NO_PROTOCOL__VALUE;

	/** @en The value {0} is not a valid URL: Unknown protocol: {1} */
	public static ResKey2 NO_URL_FORMAT_UNKNOWN_PROTOCOL__VALUE__PROTOCOL;

	/** @en The value {0} is not a valid email address: No '@' sign. */
	public static ResKey1 NO_EMAIL_FORMAT_NO_AT_SIGN__VALUE;

	/** @en The value {0} is not a valid email address: Multiple '@' signs. */
	public static ResKey1 NO_EMAIL_FORMAT_MULTIPLE_AT_SIGNS__VALUE;

	/**
	 * @en The value {0} is not a valid email address: Allowed characters for the local part of an
	 *     email are {1}.
	 */
	public static ResKey2 NO_EMAIL_FORMAT_INVALID_LOCAL_PART__VALUE__PART_CHARS;

	/**
	 * @en The value {0} is not a valid email address: Allowed characters for the domain part of an
	 *     email are {1}.
	 */
	public static ResKey2 NO_EMAIL_FORMAT_INVALID_DOMAIN_PART__VALUE__PART_CHARS;

	static {
		initConstants(I18NConstants.class);
	}
}
