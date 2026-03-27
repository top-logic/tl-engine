/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for {@link MessageButtons}.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Save
	 */
	public static ResKey BUTTON_SAVE;

	/**
	 * @en Discard
	 */
	public static ResKey BUTTON_DISCARD;

	static {
		initConstants(I18NConstants.class);
	}
}
