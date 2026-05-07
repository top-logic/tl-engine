/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * Constraint that warns when a model does not support tool calls but the agent has tools assigned.
 */
public class ModelToolCallConstraint extends ValueDependency<String> {

	/** */
	@SuppressWarnings("unused")
	public ModelToolCallConstraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> modelProperty, PropertyModel<String> nameProperty) {
		String model = modelProperty.getValue();
		String agentName = nameProperty.getValue();

		if (model == null || model.isBlank() || agentName == null || agentName.isBlank()) {
			return;
		}

		LLMProviderRegistry registry = LLMProviderRegistry.getInstance();
		if (registry == null) {
			return;
		}

		ModelInfo info = registry.getModel(model);
		if (info == null) {
			return;
		}

		if (info.capabilities().toolCall()) {
			return;
		}

		AgentDefinition agent = AgentDefinitionQuery.getInstance().findAgentByName(agentName);
		if (agent == null || agent.tools().isEmpty()) {
			return;
		}

		modelProperty.setProblemDescription(
			ResKey.text("Model '" + model + "' does not support tool calls, "
				+ "but agent '" + agentName + "' has tools assigned."));
	}
}
