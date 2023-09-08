/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.commandhandler;

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

	public static ResKey SWITCH_ASSISTENT_BACKWARD = legacyKey("assistent.switchAssistentBackward");

	public static ResKey SWITCH_ASSISTENT_FORWARD = legacyKey("assistent.switchAssistentForward");

	public static ResKey SWITCH_ASSISTENT_SHOW = legacyKey("assistent.switchAssistentShow");

	static {
		initConstants(I18NConstants.class);
	}
}
