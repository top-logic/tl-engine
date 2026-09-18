/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.job} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The operation failed: {0}
	 */
	public static ResKey1 ERROR_JOB_FAILED__MESSAGE;

	/**
	 * @en The operation reported the step "{0}", which is none of its steps: {1}
	 */
	public static ResKey2 ERROR_UNKNOWN_JOB_PHASE__NAME_PHASES;

	/**
	 * @en Reporting the progress of an operation is only possible from within a running one.
	 */
	public static ResKey ERROR_NO_JOB_TO_REPORT_TO;

	static {
		initConstants(I18NConstants.class);
	}
}
