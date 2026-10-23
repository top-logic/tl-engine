/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;
import com.top_logic.tl.ai.core.provider.AgentModelAssignment;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * {@link ModeSelector} that controls field/group visibility based on the agent's selected model.
 *
 * <p>
 * Resolves the model name to a provider ID and checks model capabilities from the catalog.
 * A field or group is shown only when the provider matches AND the required capability is present.
 * </p>
 */
public class ModelCapabilityModeSelector implements ModeSelector {

	/**
	 * Configuration for {@link ModelCapabilityModeSelector}.
	 */
	@TagName("model-capability-mode")
	public interface Config extends PolymorphicConfiguration<ModelCapabilityModeSelector> {

		/** Configuration property name for {@link #getProviders()}. */
		String PROVIDERS = "providers";

		/** Configuration property name for {@link #getCapability()}. */
		String CAPABILITY = "capability";

		/**
		 * Provider IDs this field/group applies to (e.g. "anthropic", "openai").
		 *
		 * <p>
		 * When empty, the field is shown for all providers (only capability is checked).
		 * </p>
		 */
		@Name(PROVIDERS)
		@Format(CommaSeparatedStrings.class)
		List<String> getProviders();

		/**
		 * The capability that must be supported by the model (e.g. "reasoning", "temperature").
		 *
		 * <p>
		 * When {@code null}, no capability check is performed (only provider is checked).
		 * </p>
		 */
		@Name(CAPABILITY)
		@Nullable
		String getCapability();
	}

	private final List<String> _providers;

	private final String _capability;

	/**
	 * Creates a new {@link ModelCapabilityModeSelector} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public ModelCapabilityModeSelector(InstantiationContext context, Config config) {
		_providers = config.getProviders();
		_capability = config.getCapability();
	}

	@Override
	public FormVisibility getMode(TLObject object, TLStructuredTypePart attribute, boolean editMode) {
		LLMProviderService registry = LLMProviderService.getInstance();
		if (registry == null) {
			return FormVisibility.HIDDEN;
		}

		String agentName = (String) object.tValueByName("name");
		AgentModelAssignment assignment;
		try {
			AgentRuntimeService runtime = AgentRuntimeService.getInstance();
			if (runtime == null) {
				return FormVisibility.HIDDEN;
			}
			assignment = runtime.resolveModelAssignment(agentName != null ? agentName : "");
		} catch (IllegalStateException e) {
			// No model configured for this agent and no default — hide provider-specific fields.
			return FormVisibility.HIDDEN;
		}
		String model = assignment.model();

		// Provider check
		if (!_providers.isEmpty()) {
			String providerId = registry.getProviderId(model, assignment.provider());
			if (providerId == null || !_providers.contains(providerId)) {
				return FormVisibility.HIDDEN;
			}
		}

		// Capability check
		if (_capability != null) {
			ModelInfo info = registry.getModel(model);
			if (info == null || !hasCapability(info, _capability)) {
				return FormVisibility.HIDDEN;
			}
		}

		return FormVisibility.DEFAULT;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, boolean editMode,
			Sink<Pointer> trace, OverlayLookup overlays) {
		// Model is resolved from AgentRuntimeService, not from the agent object.
		// No form field dependency to trace.
	}

	private static boolean hasCapability(ModelInfo info, String capability) {
		ModelInfo.ModelCapabilities caps = info.capabilities();
		return switch (capability) {
			case "reasoning" -> caps.reasoning();
			case "toolCall" -> caps.toolCall();
			case "temperature" -> caps.temperature();
			case "attachment" -> caps.attachment();
			case "structuredOutput" -> caps.structuredOutput();
			default -> false;
		};
	}
}
