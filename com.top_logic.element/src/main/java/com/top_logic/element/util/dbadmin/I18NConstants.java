/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ERROR_CANNOT_DELETE_TYPE_IS_IN_USE;

	public static ResKey1 TYPE_USAGE__LOCATIONS;

	public static ResKey ERROR_SYSTEM_TABLE_CANNOT_BE_DELETED;

	public static ResKey UPDATE_SCHEMA_TITLE;

	public static ResKey UPDATE_SCHEMA_NOTICE;

	static {
		initConstants(I18NConstants.class);
	}
}
