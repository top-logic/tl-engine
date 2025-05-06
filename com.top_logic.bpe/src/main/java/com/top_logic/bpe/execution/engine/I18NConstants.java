/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey SELECTION_NOT_ALLOWED;

	/**
	 * @en Import failed: {0}
	 */
	public static ResKey1 ERROR_IMPORT_FAILED__DETAILS;

	/**
	 * @en Import failed.
	 */
	public static ResKey ERROR_IMPORT_FAILED;

	/**
	 * @en Imported workflows.
	 */
	public static ResKey IMPORTED_WORKFLOWS;

	/**
	 * @en Processed timer workflow tasks.
	 */
	public static ResKey PROCESSED_TIMER_WORKFLOW_TASKS;

    static {
        initConstants(I18NConstants.class);
    }

}
