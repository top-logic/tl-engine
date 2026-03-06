/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Do you really want to execute ''{0}''?
	 */
	public static ResKey1 CONFIRM_EXECUTE__LABEL;

	/**
	 * @en Do you really want to delete ''{0}''?
	 */
	public static ResKey1 CONFIRM_DELETE__LABEL;

	/**
	 * @en Do you really want to delete {0} elements?
	 */
	public static ResKey1 CONFIRM_DELETE_MULTI__COUNT;

	static {
		initConstants(I18NConstants.class);
	}
}
