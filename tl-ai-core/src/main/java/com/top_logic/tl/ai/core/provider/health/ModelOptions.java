/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;

/**
 * Carrying a run's sampling options across to another provider's model.
 *
 * <p>
 * The options a run was built with are the ones
 * {@code AgentFactory.buildModelOptions} produced for the model it was configured with, and they
 * are provider-specific: {@code thinking_effort} is Anthropic's word for it, {@code
 * reasoning_effort} is OpenAI's, and a model that takes no temperature is sent none. A request
 * builder reads only the keys it knows, so a stale key is not an error - but it is a silently
 * dropped setting, and a temperature sent to a model that refuses one <em>is</em> an error. So the
 * map is rebuilt for the target rather than carried over.
 * </p>
 *
 * @see ModelAvailability
 */
public final class ModelOptions {

	private ModelOptions() {
		// Static utility.
	}

	/**
	 * The given options as the target model's provider should receive them.
	 *
	 * @param options
	 *        What the run was using; must not be <code>null</code>.
	 * @param catalogProviderId
	 *        The target's catalog provider ID ({@code "anthropic"}, {@code "openai"}, ...), or
	 *        <code>null</code> when it could not be determined - then only the vendor-neutral keys
	 *        survive.
	 * @param target
	 *        The target model's catalog metadata, or <code>null</code> when the catalog does not
	 *        know it. An unknown model is given the vendor-neutral keys, which is what the
	 *        {@code AgentFactory} does with an unknown model too.
	 * @return An immutable map; never <code>null</code>.
	 */
	public static Map<String, Object> forTarget(Map<String, Object> options, String catalogProviderId,
			ModelInfo target) {
		Map<String, Object> result = new LinkedHashMap<>();
		boolean temperature = target == null || target.capabilities().temperature();
		if (temperature) {
			copy(options, result, "temperature");
			copy(options, result, "topP");
		}
		boolean reasoning = target == null || target.capabilities().reasoning();
		if ("anthropic".equals(catalogProviderId)) {
			copy(options, result, "cacheTtl");
			copy(options, result, "top_k");
			if (reasoning) {
				copy(options, result, "thinking_effort");
			}
		} else if ("openai".equals(catalogProviderId) && reasoning) {
			copy(options, result, "reasoning_effort");
			copy(options, result, "reasoning_summary");
		}
		return Map.copyOf(result);
	}

	private static void copy(Map<String, Object> from, Map<String, Object> to, String key) {
		Object value = from.get(key);
		if (value != null) {
			to.put(key, value);
		}
	}
}
