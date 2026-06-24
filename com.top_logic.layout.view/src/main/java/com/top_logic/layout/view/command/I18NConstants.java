/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Confirmation
	 */
	public static ResKey CONFIRM_TITLE;

	/**
	 * @en Action ''{0}'' may not suspend inside a transaction. Place the guard (e.g. a confirmation)
	 *     before the surrounding &lt;with-transaction&gt;.
	 */
	public static ResKey1 ERROR_SUSPEND_NOT_ALLOWED_IN_TRANSACTION__ACTION;

	/**
	 * @en Unknown command reference: ''{0}''
	 */
	public static ResKey1 ERROR_UNKNOWN_COMMAND_REF__NAME;

	/**
	 * @en No command configured for this button.
	 */
	public static ResKey ERROR_NO_COMMAND_CONFIGURED;

	/**
	 * @en Please fix the validation errors before saving.
	 */
	public static ResKey ERROR_FORM_HAS_VALIDATION_ERRORS;

	/**
	 * @en Recording started. Interact with the main window, then stop.
	 */
	public static ResKey RECORDER_STARTED;

	/**
	 * @en Recording stopped. Captured {0} step(s).
	 */
	public static ResKey1 RECORDER_STOPPED__COUNT;

	/**
	 * @en No window to record.
	 */
	public static ResKey ERROR_NO_RECORDER;

	static {
		initConstants(I18NConstants.class);
	}
}
