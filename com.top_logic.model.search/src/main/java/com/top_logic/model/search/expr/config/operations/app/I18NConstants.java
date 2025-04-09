/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.app;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Either the <code>service</code> or the <code>config</code> argument must be given, not
	 *     both.
	 */
	public static ResKey ERROR_EITHER_SEVICE_OR_CONFIG;

	/**
	 * @en The given configuration type ''{0}'' does not exist.
	 */
	public static ResKey1 ERROR_NO_SUCH_CONFIGURATION_TYPE__NAME;

	/**
	 * @en Access to the configuration ''{0}'' failed: {1}
	 */
	public static ResKey2 ERROR_ACCESS_TO_CONFIGURATION_FAILED__NAME_MSG;

	/**
	 * @en Invalid configuration type ''{0}'', must be a sub-type of ''{1}''.
	 */
	public static ResKey2 ERROR_INVALID_CONFIGURATION_TYPE__NAME_EXPECTED;

	static {
		initConstants(I18NConstants.class);
	}
}
