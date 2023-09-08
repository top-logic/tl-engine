/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends com.top_logic.element.layout.grid.I18NConstants {

    public static ResKey FILE_NULL;

    public static ResKey MODEL_NOT_SUPPORTED;
    public static ResKey EXCEPTION;
    public static ResKey EXCEPTION_PARAM;
    public static ResKey WRONG_FILE_EXT;

    public static ResKey IMPORT_FAILED;
    public static ResKey IMPORT_FAILED_MSG;

    public static ResKey FINISHED_CREATED;
    public static ResKey FINISHED_CREATED_UPDATED;
    public static ResKey FINISHED_UPDATED;

	public static ResKey1 OBJECT_CREATED;

	public static ResKey1 OBJECT_UPDATED;
    public static ResKey START_IMPORT;

    public static ResKey ROWS_PROCESSED;

	public static ResKey IMPORT_COMMIT;

    static {
		initConstants(I18NConstants.class);
	}
}
