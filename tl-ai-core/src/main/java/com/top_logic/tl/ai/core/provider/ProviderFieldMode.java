/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.Set;

import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.form.model.FieldMode;

/**
 * Base class for {@link GenericFunction}s that show/hide config fields based on the selected
 * model's provider.
 *
 * <p>
 * Receives the model name as its single argument (via {@code @Ref(MODEL)}), resolves the
 * provider ID via {@link LLMService}, and returns {@link FieldMode#ACTIVE} if the
 * provider matches, {@link FieldMode#INVISIBLE} otherwise.
 * </p>
 */
abstract class ProviderFieldMode extends GenericFunction<FieldMode> {

	/**
	 * The provider IDs for which the field should be visible.
	 */
	protected abstract Set<String> providers();

	@Override
	public FieldMode invoke(Object... args) {
		String model = arg(0, args);
		if (model == null || model.isBlank()) {
			return FieldMode.INVISIBLE;
		}
		LLMService registry = LLMService.getInstance();
		if (registry == null) {
			return FieldMode.INVISIBLE;
		}
		String providerId = registry.getProviderId(model);
		if (providerId != null && providers().contains(providerId)) {
			return FieldMode.ACTIVE;
		}
		return FieldMode.INVISIBLE;
	}

	@Override
	public int getArgumentCount() {
		return 1;
	}

	@Override
	public boolean hasVarArgs() {
		return false;
	}

	/**
	 * Shows field when the model is from an Anthropic provider.
	 */
	public static class AnthropicFieldMode extends ProviderFieldMode {
		@Override
		protected Set<String> providers() {
			return Set.of("anthropic");
		}
	}

	/**
	 * Shows field when the model is from an OpenAI or xAI provider.
	 */
	public static class OpenAIFieldMode extends ProviderFieldMode {
		@Override
		protected Set<String> providers() {
			return Set.of("openai", "xai");
		}
	}

	/**
	 * Shows field when the model supports temperature (i.e. is not a reasoning-only model).
	 */
	public static class TemperatureFieldMode extends GenericFunction<FieldMode> {
		@Override
		public FieldMode invoke(Object... args) {
			String model = arg(0, args);
			if (model == null || model.isBlank()) {
				return FieldMode.INVISIBLE;
			}
			LLMService registry = LLMService.getInstance();
			if (registry == null) {
				return FieldMode.INVISIBLE;
			}
			var info = registry.getModel(model);
			if (info != null && info.capabilities().temperature()) {
				return FieldMode.ACTIVE;
			}
			return FieldMode.INVISIBLE;
		}

		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public boolean hasVarArgs() {
			return false;
		}
	}
}
