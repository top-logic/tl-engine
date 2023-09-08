/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure.blob;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Internationalization constants for this package.
 *
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	static {
		initConstants(I18NConstants.class);
	}

	public static ResKey ERROR_INIT_BASE_DIRECTORY;

	public static ResKey ERROR_CONNECT_CONTAINER;

	public static ResKey ERROR_CONNECT_STRING;
}