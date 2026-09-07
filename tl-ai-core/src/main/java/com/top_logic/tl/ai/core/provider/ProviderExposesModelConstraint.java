/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;
import com.top_logic.basic.util.ResKey;

/**
 * Constraint that reports a provider/model pair the running service cannot route.
 *
 * <p>
 * The editor counterpart of the {@link LLMService#refusals refusals} that stop the service from
 * starting: a provider that does not exist, one that does not expose the chosen model, and a model
 * two providers expose with no provider chosen. Reported here so that an administrator sees the
 * problem in the form rather than in a failed restart.
 * </p>
 *
 * <p>
 * The check runs against the <em>running</em> service, so a provider added in the same, unsaved
 * edit is not yet known to it. Such a pair is left unreported rather than falsely rejected - the
 * start-up refusal remains the authority.
 * </p>
 */
public class ProviderExposesModelConstraint extends ValueDependency<String> {

	/** */
	@SuppressWarnings("unused")
	public ProviderExposesModelConstraint() {
		super(String.class);
	}

	@Override
	protected void checkValue(PropertyModel<String> providerProperty, PropertyModel<String> modelProperty) {
		LLMService service = LLMService.getInstance();
		if (service == null) {
			return;
		}
		String provider = providerProperty.getValue();
		String model = modelProperty.getValue();
		if (model == null || model.isBlank()) {
			// An agent override without a model of its own runs the default model.
			model = service.getDefaultModel();
		}
		if (model == null || model.isBlank()) {
			return;
		}
		List<String> exposedBy = service.getProvidersOf(model);
		if (exposedBy.isEmpty()) {
			// The model is unknown to the running service - not this constraint's business.
			return;
		}
		if (provider == null || provider.isBlank()) {
			if (exposedBy.size() > 1) {
				providerProperty.setProblemDescription(ResKey.text("Model '" + model
					+ "' is exposed by the providers " + exposedBy + ". Choose the one to use."));
			}
			return;
		}
		if (!exposedBy.contains(provider)) {
			providerProperty.setProblemDescription(ResKey.text("Provider '" + provider
				+ "' does not expose model '" + model + "'. Providers exposing it: " + exposedBy + "."));
		}
	}
}
