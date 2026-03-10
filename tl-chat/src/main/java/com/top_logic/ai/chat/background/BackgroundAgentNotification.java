/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.background;

import java.time.Instant;

/**
 * A notification produced by a background agent.
 */
public final class BackgroundAgentNotification {

	private final String _title;

	private final String _agentName;

	private final String _category;

	private final String _severity;

	private final Instant _timestamp;

	/**
	 * Creates a new {@link BackgroundAgentNotification}.
	 *
	 * @param title
	 *        Short description of the issue found.
	 * @param agentName
	 *        Name of the background agent that produced this notification.
	 * @param category
	 *        Category of the issue (e.g. "review", "block").
	 * @param severity
	 *        Severity level (e.g. "low", "medium", "high", "warning").
	 * @param timestamp
	 *        When the notification was created.
	 */
	public BackgroundAgentNotification(
			String title, String agentName, String category, String severity, Instant timestamp) {
		_title = title;
		_agentName = agentName;
		_category = category;
		_severity = severity;
		_timestamp = timestamp;
	}

	/**
	 * Short description of the issue found.
	 */
	public String title() {
		return _title;
	}

	/**
	 * Name of the background agent that produced this notification.
	 */
	public String agentName() {
		return _agentName;
	}

	/**
	 * Category of the issue.
	 */
	public String category() {
		return _category;
	}

	/**
	 * Severity level.
	 */
	public String severity() {
		return _severity;
	}

	/**
	 * When the notification was created.
	 */
	public Instant timestamp() {
		return _timestamp;
	}
}
