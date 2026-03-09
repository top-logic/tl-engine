/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;

/**
 * A single notification produced by the review agent.
 *
 * @param title
 *        Short description of the issue found.
 * @param category
 *        Category of the issue (e.g. "incorrect-answer", "bad-delegation", "tool-misuse").
 * @param severity
 *        Severity level (e.g. "low", "medium", "high").
 * @param timestamp
 *        When the notification was created.
 */
public record ReviewNotification(String title, String category, String severity, Instant timestamp) {
}
