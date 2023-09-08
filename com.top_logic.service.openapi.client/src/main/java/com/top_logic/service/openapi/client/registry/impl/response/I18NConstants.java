/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.response;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en The call "{0}" with the arguments {1} failed: {2} (error code {3}): {4} */
	public static ResKey5 ERROR_CALL_FAILED__FUN_ARGS_REASON_CODE_CONTENT;

	/** @en No details available */
	public static ResKey ERROR_CALL_FAILED_NO_DETAILS;

	static {
		initConstants(I18NConstants.class);
	}
}
