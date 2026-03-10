/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.trigger;

/**
 * Trigger that fires after a chat turn completes.
 *
 * <p>
 * The background agent runs asynchronously after the primary agent has finished its response. This
 * is useful for quality monitoring, content review, or follow-up actions that should not delay the
 * user's response.
 * </p>
 */
public interface PostTurnTrigger extends TriggerConfig {

	// No additional properties — presence of this trigger type is sufficient.
}
