/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the configuration editor module.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Edit
	 */
	public static ResKey EDIT;

	/**
	 * @en Apply
	 */
	public static ResKey APPLY;

	/**
	 * @en Cancel
	 */
	public static ResKey CANCEL;

	/**
	 * @en {0} (empty)
	 */
	public static ResKey1 LIST_ELEMENT_EMPTY_TITLE__TYPE;

	/**
	 * @en Invalid value "{0}" for "{1}".
	 */
	public static ResKey2 ERROR_INVALID_VALUE__VALUE_PROPERTY;

	/**
	 * @en A value is required for "{0}".
	 */
	public static ResKey1 ERROR_VALUE_REQUIRED__PROPERTY;

	/**
	 * @en An entry with {0} "{1}" already exists.
	 */
	public static ResKey2 ERROR_DUPLICATE_KEY__PROPERTY_VALUE;

	/**
	 * @en The configuration cannot be applied because it still contains errors.
	 */
	public static ResKey ERROR_CANNOT_APPLY;

	/**
	 * @en The configuration cannot be applied because an entry could not be read.
	 */
	public static ResKey ERROR_INPUT_NOT_READABLE;

	static {
		initConstants(I18NConstants.class);
	}
}
