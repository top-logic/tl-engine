/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Internationalization constants for the agent configuration constraints.
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en Thinking budget is required when thinking type is ENABLED. */
	public static ResKey THINKING_BUDGET_REQUIRED_WHEN_ENABLED;

	/** @en Maximum tokens is required when thinking type is ENABLED. */
	public static ResKey MAX_TOKENS_REQUIRED_WHEN_THINKING_ENABLED;

	static {
		initConstants(I18NConstants.class);
	}
}
