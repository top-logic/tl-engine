/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends com.top_logic.importer.excel.I18NConstants {

    public static ResKey COMPANY_NAME_NOT_FOUND;
    public static ResKey STRING_SHORTENED;
    public static ResKey VALUE_EMPTY;
    public static ResKey BOOLEAN_PARSE_ERROR;
    public static ResKey DATE_PARSE_ERROR;
    public static ResKey DOUBLE_PARSE_ERROR;
    public static ResKey LONG_PARSE_ERROR;
    public static ResKey FLE_CREATE_FAILED;
    public static ResKey FLE_NOT_FOUND;
    public static ResKey FLE_NO_MAPPING;
    public static ResKey ORG_UNIT_NOT_FOUND;
    public static ResKey PERSON_NAME_NOT_FOUND;
    public static ResKey NO_NUMBER;
    public static ResKey STRING_URL_INVALID;

    static {
		initConstants(I18NConstants.class);
	}
}
