/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

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

	public static ResKey CLOSE_DIALOG = legacyKey("close");

	public static ResKey GUI_INSPECTOR = legacyKey("menu.GuiInspector");

	public static ResKey MAXIMIZE_DIALOG = legacyKey("maximise");

	public static ResKey RESTORE_DIALOG = legacyKey("restore");

	static {
		initConstants(I18NConstants.class);
	}
}
