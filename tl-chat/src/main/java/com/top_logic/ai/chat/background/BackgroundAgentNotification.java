/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.background;

import java.time.Instant;

/**
 * A notification produced by a background agent.
 *
 * @param title
 *        Short description of the issue found.
 * @param agentName
 *        Name of the background agent that produced this notification.
 * @param category
 *        Category of the issue (e.g. "incorrect-answer", "bad-delegation", "tool-misuse").
 * @param severity
 *        Severity level (e.g. "low", "medium", "high").
 * @param timestamp
 *        When the notification was created.
 */
public record BackgroundAgentNotification(
	String title, String agentName, String category, String severity, Instant timestamp) {
}
