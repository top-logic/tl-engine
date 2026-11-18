/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dev.tools.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the model-editor developer tool.
 *
 * @see ModelEditAction
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Please enter a name.
	 */
	public static ResKey ERROR_MISSING_NAME;

	/**
	 * @en No target selected. Select the module or type to create the element in first.
	 */
	public static ResKey ERROR_NO_TARGET;

	/**
	 * @en Please select the value type of the new attribute.
	 */
	public static ResKey ERROR_MISSING_TYPE;

	/**
	 * @en Attributes of type "{0}" cannot hold multiple values.
	 */
	public static ResKey1 ERROR_MULTIPLE_NOT_SUPPORTED__TYPE;

	/**
	 * @en The type "{0}" cannot hold references; only classes can refer to classes and enumerations.
	 */
	public static ResKey1 ERROR_REFERENCE_NOT_SUPPORTED__OWNER;

	/**
	 * @en Only a reference to a class can be a composition or an aggregation; "{0}" is no class.
	 */
	public static ResKey1 ERROR_KIND_REQUIRES_CLASS__TYPE;

	/**
	 * @en The type "{0}" already has an attribute named "{1}".
	 */
	public static ResKey2 ERROR_PART_EXISTS__TYPE_NAME;

	/**
	 * @en Please select the class to add as generalization.
	 */
	public static ResKey ERROR_MISSING_GENERALIZATION;

	/**
	 * @en The class "{0}" is final and cannot be specialized.
	 */
	public static ResKey1 ERROR_FINAL_GENERALIZATION__CLASS;

	/**
	 * @en The class "{1}" cannot become a generalization of "{0}", because "{0}" already is a
	 *     generalization of "{1}".
	 */
	public static ResKey2 ERROR_CYCLIC_GENERALIZATION__CLASS_GENERALIZATION;

	static {
		initConstants(I18NConstants.class);
	}
}
