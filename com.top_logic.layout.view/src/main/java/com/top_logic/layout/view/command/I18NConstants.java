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
	 * @en Changed language of "{0}".
	 */
	public static ResKey1 CHANGED_LANGUAGE__USER;

	/**
	 * @en Reset the personal configuration of "{0}".
	 */
	public static ResKey1 RESET_PERSONAL_CONFIGURATION__USER;

	/**
	 * @en This page is now the one you start on.
	 */
	public static ResKey START_PAGE_REMEMBERED;

	/**
	 * @en You no longer start on a particular page.
	 */
	public static ResKey START_PAGE_FORGOTTEN;

	/**
	 * @en This page has no address of its own, so it cannot be the one you start on.
	 */
	public static ResKey START_PAGE_NOT_ADDRESSABLE;

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

	static {
		initConstants(I18NConstants.class);
	}
}
