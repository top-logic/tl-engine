/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.output;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;

/**
 * Base configuration for background agent output handlers.
 *
 * <p>
 * An output handler determines what happens with the results produced by a background agent.
 * Multiple output handlers can be configured to write results to different destinations.
 * </p>
 *
 * <p>
 * Concrete implementations include:
 * </p>
 * <ul>
 * <li>{@link OutputHandlerImplementation.Config} - custom Java output handler via
 * {@link com.top_logic.basic.config.PolymorphicConfiguration}</li>
 * <li>{@link ScriptOutputHandler} - inline TL-Script expression handler</li>
 * </ul>
 */
@Abstract
public interface OutputHandler extends ConfigurationItem {

	// Marker interface — concrete output handler types add their own properties.
}
