/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Error establishing connection to {0}.
	 */
	public static ResKey1 ERROR_ESTABLISH_CONNECTION__NAME;

	/**
	 * @en Error sending a message by producer {0}.
	 */
	public static ResKey1 ERROR_SENDING_MSG__NAME;

	/**
	 * @en Error receiving a message by consumer {0}.
	 */
	public static ResKey1 ERROR_RECEIVING_MSG__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
