/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog.model;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Reverted change: {0}
	 */
	public static ResKey1 REVERTED__MSG;

	/**
	 * @en Performed again: {0}
	 */
	public static ResKey1 REDO__MSG;

	/**
	 * @en The object that would be created already exists: {0}
	 */
	public static ResKey1 PROBLEM_OBJECT_TO_CREATE_ALREADY_EXISTS__OBJ;

	/**
	 * @en The object that should be deleted does no longer exist: {0}
	 */
	public static ResKey1 PROBLEM_OBJECT_TO_DELETE_NO_LONGER_EXISTS__OBJ;

	/**
	 * @en The object that should be changed does no longer exist: {0}
	 */
	public static ResKey1 PROBLEM_OBJECT_TO_MODIFY_NO_LONGER_EXISTS__OBJ;

	/**
	 * @en The property "{1}" of object "{0}" has been changed from its original value "{2}" to the
	 *     new value "{3}" in the meantime.
	 */
	public static ResKey4 PROBLEM_VALUE_CHANGED_IN_BETWEEN__OBJ_PART_ORIG_CURR;

	/**
	 * @en Values set to the property "{1}" of object "{0}" are no longer available (deleted in the
	 *     meantime): {2}
	 */
	public static ResKey3 PROBLEM_OBJECTS_NO_LONGER_EXIST__OBJ_PART_MISSING;

	static {
		initConstants(I18NConstants.class);
	}
}
