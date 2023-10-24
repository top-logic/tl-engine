/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.json;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en Reading source failed. */
	public static ResKey ERROR_READING_SOURCE;

	/** @en Unexpected end of input. */
	public static ResKey ERROR_UNEXPECTED_END_OF_INPUT;

	/** @en Unexpected contents at end. */
	public static ResKey ERROR_UNEXPECTED_INPUT;

	/** @en Expected ''{0}'' but got ''{1}''. */
	public static ResKey2 ERROR_UNEXPECTED_VALUE__EXPECTED_ACUTAL;

	/** @en Invalid date format. */
	public static ResKey ERROR_INVALID_DATE_FORMAT;

	/** @en Invalid number format. */
	public static ResKey ERROR_INVALID_NUMBER_FORMAT;

	/** @en Unterminated string value. */
	public static ResKey ERROR_UNTERMINATED_STRING_VALUE;

	/** @en Unterminated string escape. */
	public static ResKey ERROR_UNTERMINATED_STRING_ESCAPE;

	/** @en Unterminated unicode escape. */
	public static ResKey ERROR_UNTERMINATED_UNICODE_ESCAPE;

	/** @en Invalid unicode escape. */
	public static ResKey ERROR_INVALID_UNICODE_ESCAPE;

	/** @en Invalid character ''{0}''. */
	public static ResKey1 ERROR_INVALID_CHARACTER__CHARACTER;

	/** @en Expected map key. */
	public static ResKey ERROR_MISSING_MAP_KEY;

	/** @en Expected map key name. */
	public static ResKey ERROR_MISSING_MAP_KEY_NAME;

	/** @en Parse error at position {1}: {0} */
	public static ResKey2 PARSE_ERROR__MESSAGE_POSITION;

	/** @en {0} Problematic characters around: ''{1} <> {2}''. */
	public static ResKey3 PARSE_ERROR__MESSAGE__BEFORE_PROBLEM__AFTER_PROBLEM;

    static {
        initConstants(I18NConstants.class);
    }

}
