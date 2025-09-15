/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.exporter;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Cannot create value binding for "{0}": {1}
	 */
	public static ResKey2 FAILED_TO_CREATE_BINDING__ATTR_MSG;

	/**
	 * @en Failed to serialize value "{0}" of attribute {1}: {2}
	 */
	public static ResKey3 FAILED_TO_SERIALIZE__VAL_ATTR_MSG;

	/**
	 * @en Cannot read value of attribute "{0}" from xml: {1}
	 */
	public static ResKey2 FAILED_READING_VALUE__ATR_MSG;

	/**
	 * @en Cannot resolve type "{0}": {1}
	 */
	public static ResKey2 FAILED_RESOLVING_TYPE__KIND_MSG;

	static {
		initConstants(I18NConstants.class);
	}
}
