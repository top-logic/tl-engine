/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.provider;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency2;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.ai.core.agent.AgentRuntimeService;

/**
 * Constraint on the tool choice of an agent that reports a forced tool call
 * ({@link ToolChoice#REQUIRED}) the provider of the agent's model does not
 * {@link LLMProviderConfig#getToolChoices(String) accept}.
 *
 * <p>
 * The arguments are the model and the provider of the agent. Without a model, the agent runs the
 * default model of the {@link AgentRuntimeService}, served by its default provider.
 * </p>
 */
public class ModelToolChoiceConstraint extends GenericValueDependency2<String, String, String> {

	/** */
	@SuppressWarnings("unused")
	public ModelToolChoiceConstraint() {
		super(String.class, String.class, String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> toolChoiceProperty, PropertyModel<String> modelProperty,
			PropertyModel<String> providerProperty) {
		if (ToolChoice.parse(toolChoiceProperty.getValue()) != ToolChoice.REQUIRED) {
			return;
		}
		LLMProviderService registry = LLMProviderService.getInstance();
		if (registry == null) {
			return;
		}

		String model = modelProperty.getValue();
		String provider = providerProperty.getValue();
		if (model == null || model.isBlank()) {
			AgentRuntimeService runtime = AgentRuntimeService.getInstance();
			if (runtime == null) {
				return;
			}
			model = runtime.getDefaultModel();
			provider = runtime.getDefaultProvider();
		}
		if (model == null || model.isBlank()) {
			return;
		}

		Set<ToolChoice> accepted = registry.getToolChoices(model, provider);
		if (accepted.contains(ToolChoice.REQUIRED)) {
			return;
		}
		toolChoiceProperty.setProblemDescription(I18NConstants.WARNING_TOOL_CHOICE_NOT_ACCEPTED__MODEL_CHOICES
			.fill(model, ToolChoice.ListFormat.INSTANCE.getSpecification(List.copyOf(accepted))));
	}
}
