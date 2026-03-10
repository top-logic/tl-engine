/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.output;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Output handler that pushes background agent results as notifications.
 *
 * <p>
 * The notification handling logic is provided by the module registering a handler factory via
 * {@link com.top_logic.ai.service.agents.background.BackgroundAgentExecutor#registerOutputHandlerFactory}.
 * </p>
 */
public interface NotificationOutputHandler extends OutputHandler {

	/** @see #getSeverity() */
	String SEVERITY = "severity";

	/**
	 * The default severity level for notifications produced by this handler.
	 *
	 * @return The severity string, defaults to {@code "warning"}.
	 */
	@Name(SEVERITY)
	@StringDefault("warning")
	String getSeverity();
}
