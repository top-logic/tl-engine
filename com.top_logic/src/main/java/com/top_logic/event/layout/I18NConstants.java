/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey SWITCH_ON = legacyKey("tl.command.toggle.On");

	public static ResKey SWITCH_OFF = legacyKey("tl.command.toggle.Off");

	public static ResKey SAVE_LOGENTRY_CONFIG = legacyKey("tl.command.save");

	static {
		initConstants(I18NConstants.class);
	}
}
