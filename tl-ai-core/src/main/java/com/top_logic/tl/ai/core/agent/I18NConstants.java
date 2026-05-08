/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;

/**
 * I18N constants for the agent package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message when an agent or skill name uses the reserved {@code TL_} prefix.
	 *
	 * @en The prefix 'TL_' is reserved for TopLogic platform definitions: {0}
	 */
	public static ResKey1 RESERVED_PREFIX_ERROR__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
