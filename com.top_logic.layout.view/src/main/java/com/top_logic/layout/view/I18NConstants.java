/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the view system.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Field value changed.
	 */
	public static ResKey FIELD_VALUE_CHANGED;

	static {
		initConstants(I18NConstants.class);
	}
}
