/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey2 ERROR_FOLDER_CREATE;

	public static ResKey2 ERROR_OBJECTS_GET;

	public static ResKey2 ERROR_FOLDER_CREATE_EXISTS;

	public static ResKey1 ERROR_FOLDER_CREATE_CONNECT;

	public static ResKey1 ERROR_FOLDER_CREATE_OTHER;

	static {
		initConstants(I18NConstants.class);
	}

}
