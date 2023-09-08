/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.dispatching;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends com.top_logic.importer.base.I18NConstants {

	public static ResPrefix IMPORTER_NAME;

    public static ResKey IMPORT_FINISHED;
    public static ResKey VALIDATION_MESSAGE;

    static {
		initConstants(I18NConstants.class);
	}
}
