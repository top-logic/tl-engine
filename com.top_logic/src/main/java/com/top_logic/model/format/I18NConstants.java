/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.format;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * There is no classifier with name "{0}".
	 */
	public static ResKey2 ERROR_NO_SUCH_CLASSIFIER__NAME_OPTIONS;

	/**
	 * @en Cannot resolve reference ''{0}'' to object.
	 */
	public static ResKey1 ERROR_CAN_NOT_RESOLVE_REFERENCE__REFERENCE;

	/**
	 * @en Cannot resolve type in object reference ''{0}''.
	 */
	public static ResKey1 ERROR_CAN_NOT_RESOLVE_STATIC_TYPE__REFERENCE;

	/**
	 * @en Cannot parse object reference ''{0}''.
	 */
	public static ResKey1 ERROR_CAN_NOT_PARSE_REFERENCE__REFERENCE;

	static {
		initConstants(I18NConstants.class);
	}
}
