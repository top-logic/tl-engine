/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.background;

import java.time.Instant;

import com.top_logic.ai.service.agents.background.AgentResult;
import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.output.OutputHandler;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Output handler that pushes background agent results as notifications.
 *
 * <p>
 * Results with status {@link AgentResult.Status#PASS PASS} are skipped since no notification is
 * needed for passing results.
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

	/**
	 * Handles a background agent result by creating a notification.
	 *
	 * <p>
	 * If the result status is {@link AgentResult.Status#PASS}, no notification is created. Otherwise,
	 * a {@link BackgroundAgentNotification} is created and pushed to the
	 * {@link GlobalNotificationStore}.
	 * </p>
	 *
	 * @param config
	 *        The handler configuration providing the severity.
	 * @param result
	 *        The agent execution result.
	 * @param context
	 *        The background execution context.
	 */
	static void handle(NotificationOutputHandler config, AgentResult result, BackgroundContext context) {
		if (result.status() == AgentResult.Status.PASS) {
			return;
		}

		String title = result.message();
		if (title != null && title.length() > 200) {
			title = title.substring(0, 197) + "...";
		}

		String category = result.status() == AgentResult.Status.BLOCK ? "block" : "review";

		BackgroundAgentNotification notification = new BackgroundAgentNotification(
			title,
			context.getAgentName(),
			category,
			config.getSeverity(),
			Instant.now());

		GlobalNotificationStore.getInstance().add(notification);
	}
}
