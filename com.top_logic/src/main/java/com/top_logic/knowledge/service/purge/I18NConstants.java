/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.purge;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Analyzing what the removal of the given objects would delete failed.
	 */
	public static ResKey ERROR_ANALYSIS_FAILED;

	/**
	 * @en Removing the given objects failed.
	 */
	public static ResKey ERROR_PURGE_FAILED;

	static {
		initConstants(I18NConstants.class);
	}
}
