/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

/**
 * One model of a failover chain, together with the provider that must serve it.
 *
 * @param model
 *        The model name; never <code>null</code>.
 * @param provider
 *        The name of the configured provider entry that serves it, or <code>null</code> for an
 *        unqualified reference that the model's only provider serves.
 */
public record ModelTarget(String model, String provider) {

	/** Whether this target names the same model and provider as the given one. */
	public boolean sameAs(ModelTarget other) {
		if (other == null) {
			return false;
		}
		return model.equals(other.model())
			&& (provider == null ? other.provider() == null : provider.equals(other.provider()));
	}

	/** The model, qualified by its provider where one is named. */
	public String label() {
		return provider == null ? model : model + " (" + provider + ")";
	}
}
