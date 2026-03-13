/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Input configuration that delegates to a pluggable {@link InputProvider}.
 *
 * <p>
 * This allows custom input gathering logic to be implemented as a {@link InputProvider} and
 * configured declaratively.
 * </p>
 */
@Hidden
public interface ProviderInput extends InputConfig {

	/** @see #getProvider() */
	String PROVIDER = "provider";

	/**
	 * The provider that gathers input for the background agent.
	 *
	 * @return The provider configuration, never {@code null}.
	 */
	@Name(PROVIDER)
	@Mandatory
	PolymorphicConfiguration<InputProvider> getProvider();
}
