/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 TASK_COMPLETED_SUCCESSFULLY__DURATION;

	public static ResKey TASK_COMMIT_FAILED;

	/**
	 * @en Task started: {0}
	 */
	public static ResKey1 TASK_STARTED__TASK;

	static {
		initConstants(I18NConstants.class);
	}

}
