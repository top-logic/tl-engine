/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.trigger;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;

/**
 * Base configuration for background agent triggers.
 *
 * <p>
 * A trigger defines when a background agent is activated. Concrete implementations include:
 * </p>
 * <ul>
 * <li>{@link PostTurnTrigger} - fires after a chat turn completes</li>
 * <li>{@link PreTurnTrigger} - fires before a chat turn is processed</li>
 * </ul>
 */
@Abstract
public interface TriggerConfig extends ConfigurationItem {

	// Marker interface — concrete trigger types add their own properties.
}
