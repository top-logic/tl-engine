/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

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

	public static ResKey WINDOW_CROSS_REFERENCE = legacyKey("layout.windowbar.crossreference");

	public static ResKey WINDOW_MAXIMIZE = legacyKey("layout.windowbar.maximize");

	public static ResKey WINDOW_RESTORE = legacyKey("layout.windowbar.restore");

	public static ResKey TOGGLE_SHOW_HELP = legacyKey("tl.command.help.toggle");

	public static ResKey VISIBLE_BUTTON_COUNT;

	public static ResKey NO_LAYOUT_CONFIGURED;

	static {
		initConstants(I18NConstants.class);
	}
}
