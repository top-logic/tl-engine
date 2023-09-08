/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends com.top_logic.importer.base.I18NConstants {

    public static ResKey ACTIVATE_SHEET;
    public static ResKey CANNOT_READ_LABEL;
    public static ResKey CONTEXT_CLOSE_FAILED;
    public static ResKey DO_READ;
    public static ResKey NO_NODE_LEVEL;
    public static ResKey NO_PARENT_FOUND;

	public static ResKey1 KEY_WORD_NOT_FOUND;

	public static ResKey2 KEY_WORD_NOT_FOUND_IN_SHEET;

    static {
		initConstants(I18NConstants.class);
	}
}
