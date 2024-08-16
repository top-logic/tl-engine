/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail.script;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Invalid address "{0}": {1}
	 */
	public static ResKey2 ERROR_INVALID_ADDRESS__VAL_MSG;

	/**
	 * @en Failed to send e-mail to "{0}": {1}
	 */
	public static ResKey2 ERROR_SENDING_MAIL__TO_MSG;

	static {
		initConstants(I18NConstants.class);
	}
}
