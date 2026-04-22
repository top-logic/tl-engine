/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Internationalization constants for the background agent configuration constraints.
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en Chat context input is not available for pre-turn triggers. Use a custom message, TL-Script, or provider input instead. */
	public static ResKey CHAT_CONTEXT_INPUT_NOT_ALLOWED_FOR_PRE_TURN;

	static {
		initConstants(I18NConstants.class);
	}
}
