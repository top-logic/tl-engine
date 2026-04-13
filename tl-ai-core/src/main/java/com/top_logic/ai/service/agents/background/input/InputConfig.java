/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;

/**
 * Base configuration for background agent input sources.
 *
 * <p>
 * An input configuration defines what data a background agent processes. Concrete implementations
 * include:
 * </p>
 * <ul>
 * <li>{@link ChatContextInput} - uses chat conversation context as input</li>
 * <li>{@link ScriptInput} - uses a TL-Script expression to produce input</li>
 * <li>{@link CustomInput} - uses a fixed custom message as input</li>
 * <li>{@link ProviderInput} - delegates to a pluggable {@link InputProvider}</li>
 * </ul>
 */
@Abstract
public interface InputConfig extends ConfigurationItem {

	// Marker interface — concrete input types add their own properties.
}
