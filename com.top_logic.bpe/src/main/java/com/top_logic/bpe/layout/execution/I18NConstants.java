/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The form contains invalid values.
	 */
	public static ResKey FINISH_TASK_DISABLED;

	/**
	 * @en There is no form defined for process "{0}".
	 */
	public static ResKey1 NO_DISPLAY_DESCRIPTION__WF;

	/**
	 * @en The current task is flagged as non-editable.
	 */
	public static ResKey CANNOT_EDIT_TASK;


    static {
        initConstants(I18NConstants.class);
    }

}
