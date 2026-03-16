/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.trigger;

import com.top_logic.ai.service.agents.background.BackgroundConfig;
import com.top_logic.ai.service.agents.background.I18NConstants;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Constraint that produces an error when a chat-scoped trigger ({@link PreTurnTrigger} or
 * {@link PostTurnTrigger}) is used with
 * {@link com.top_logic.ai.service.agents.background.BackgroundConfig.Scope#APPLICATION}.
 *
 * <p>
 * Pre-turn and post-turn triggers operate within the context of a chat session and are not
 * compatible with application-scoped agents. Use a {@link ScheduledTrigger} for application scope.
 * </p>
 */
public class ChatTriggerNotAllowedForApplicationConstraint
		extends GenericValueDependency<TriggerConfig, BackgroundConfig.Scope> {

	/**
	 * Creates a new {@link ChatTriggerNotAllowedForApplicationConstraint}.
	 */
	public ChatTriggerNotAllowedForApplicationConstraint() {
		super(TriggerConfig.class, BackgroundConfig.Scope.class);
	}

	@Override
	protected void checkValue(PropertyModel<TriggerConfig> trigger,
			PropertyModel<BackgroundConfig.Scope> scope) {
		if (scope.getValue() == BackgroundConfig.Scope.APPLICATION
				&& (trigger.getValue() instanceof PreTurnTrigger
					|| trigger.getValue() instanceof PostTurnTrigger)) {
			trigger.setProblemDescription(I18NConstants.CHAT_TRIGGER_NOT_ALLOWED_FOR_APPLICATION);
		}
	}
}
