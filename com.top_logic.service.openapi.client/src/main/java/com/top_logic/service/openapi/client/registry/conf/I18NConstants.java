/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.conf;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The status code ''{0}'' is invalid. It must either be an integer in the range [100-599]
	 *     or correspond to one of the special status codes ranges 1XX, 2XX, 3XX, 4XX or 5XX.
	 */
	public static ResKey1 ERROR_INVALID_STATUS_CODE__CODE;

	static {
		initConstants(I18NConstants.class);
	}
}
