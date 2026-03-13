/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.BackgroundConfig;
import com.top_logic.ai.service.agents.background.I18NConstants;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Constraint that produces an error when a {@link ChatContextInput} is configured with
 * {@link com.top_logic.ai.service.agents.background.BackgroundConfig.Scope#APPLICATION}.
 *
 * <p>
 * Application-scoped agents operate outside of any chat session, so there is no chat context to
 * read from.
 * </p>
 */
public class ChatContextInputNotAllowedForApplicationConstraint
		extends GenericValueDependency<InputConfig, BackgroundConfig.Scope> {

	/**
	 * Creates a new {@link ChatContextInputNotAllowedForApplicationConstraint}.
	 */
	public ChatContextInputNotAllowedForApplicationConstraint() {
		super(InputConfig.class, BackgroundConfig.Scope.class);
	}

	@Override
	protected void checkValue(PropertyModel<InputConfig> input,
			PropertyModel<BackgroundConfig.Scope> scope) {
		if (scope.getValue() == BackgroundConfig.Scope.APPLICATION
				&& input.getValue() instanceof ChatContextInput) {
			input.setProblemDescription(I18NConstants.CHAT_CONTEXT_INPUT_NOT_ALLOWED_FOR_APPLICATION);
		}
	}
}
