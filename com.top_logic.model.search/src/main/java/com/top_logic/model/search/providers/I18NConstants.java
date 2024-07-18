/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 INVALID_REFERENCE_ELEMET__EXPECTED_FOUND_ATTR;

	public static ResKey2 UNEXPECTED_TEXT_CONTENT__TEXT__ATTR;

	public static ResKey3 AMBIGUOUS_RESULT_FROM_RESOLVER__KEY_RESULT_ATTR;

	public static ResKey2 CANNOT_RESOLVE_OBJECT__KEY__ATTR;

	public static ResKey ERROR_INVALID_VALUE;

	public static ResKey3 ERROR_NO_BOUND_OBJECT_RESULT__EXPR__SRC__VALUE;

	public static ResKey2 ERROR_SCRIPT_RESULT_IS_COLLECTION__ATTR_VALUE;

	public static ResKey3 ERROR_SCRIPT_RESULT_OF_INCOMPATIBLE_TYPE__ATTR_EXPECTED_ACTUAL;

	/**
	 * @en The calculation result of the mandatory attribute "{0}" is empty for object "{1}".
	 */
	public static ResKey2 ERROR_SCRIPT_DELIVERED_NO_RESULT_FOR_MANDATORY_ARRTIBUTE__ATTR_OBJ;

	public static ResKey1 TASK_MESSAGE__VALUE;

	/**
	 * @en New row
	 */
	@CalledByReflection
	public static ResKey NEW_LINE;

	/**
	 * Default label for opening a generic create dialog.
	 * 
	 * @en Create Object
	 */
	@CalledByReflection
	public static ResKey1 CREATE_OBJECT_DEFAULT_LABEL;

	/**
	 * @en Invalid field mode "{0}", possible values are: {1}
	 */
	public static ResKey2 ERROR_MODE__VALUE_OPTIONS;

	/**
	 * @en The model of a form must be a modeled instance (TLObject), but was: {0}
	 */
	public static ResKey1 ERROR_NO_TLOBJECT_FORM_MODEL__FORM;

	static {
		initConstants(I18NConstants.class);
	}
}
