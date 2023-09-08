/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/** message that indicates that some constraint of a form Field is not fulfilled */
	public static ResKey ERROR_CONSTRAINTS_NOT_FULFILLED;

	/**
	 * Resources for {@link DocumentField}.
	 */
	public static ResPrefix DOCUMENT_FIELD;

	public static ResKey FIXED_OPTIONS_ADDED = legacyKey("layout.form.selection.fixedOptionsAdded");

	public static ResKey FIXED_OPTIONS_REMOVED = legacyKey("layout.form.selection.fixedOptionsRemoved");

	public static ResPrefix DATE_TIME_FIELD;

	/** @en The day value can not be parsed: {0} */
	public static ResKey1 DATE_TIME_FIELD_NO_DATE_VALUE__VALUE;

	/** @en The time value can not be parsed: {0} */
	public static ResKey1 DATE_TIME_FIELD_NO_TIME_VALUE__VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
