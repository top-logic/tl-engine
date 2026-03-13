/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.I18NConstants;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger;
import com.top_logic.ai.service.agents.background.trigger.TriggerConfig;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Constraint that produces an error when a {@link ChatContextInput} is configured with a
 * {@link PreTurnTrigger}.
 *
 * <p>
 * Pre-turn agents run before the chat turn completes, so there is no completed turn to read from.
 * Other input types (custom message, script, provider) are allowed for pre-turn triggers.
 * </p>
 */
public class ChatContextInputNotAllowedForPreTurnConstraint extends GenericValueDependency<InputConfig, TriggerConfig> {

	/**
	 * Creates a new {@link ChatContextInputNotAllowedForPreTurnConstraint}.
	 */
	public ChatContextInputNotAllowedForPreTurnConstraint() {
		super(InputConfig.class, TriggerConfig.class);
	}

	@Override
	protected void checkValue(PropertyModel<InputConfig> input, PropertyModel<TriggerConfig> trigger) {
		if (trigger.getValue() instanceof PreTurnTrigger && input.getValue() instanceof ChatContextInput) {
			input.setProblemDescription(I18NConstants.CHAT_CONTEXT_INPUT_NOT_ALLOWED_FOR_PRE_TURN);
		}
	}
}
