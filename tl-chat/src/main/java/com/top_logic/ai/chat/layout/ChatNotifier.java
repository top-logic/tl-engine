/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;

/**
 * A lightweight notification message for chat progress updates.
 *
 * Chat notifiers are distinct from regular chat messages and have
 * minimalistic styling (grey, light appearance) to show progress
 * during long-running operations like workflow execution.
 *
 * @author jhu
 */
public class ChatNotifier {

	/**
	 * Type of notification for different styling and icons.
	 */
	public enum NotificationType {
		/** General information message */
		INFO,
		/** Progress update during operation */
		PROGRESS,
		/** Successful completion */
		SUCCESS,
		/** Error or failure message */
		ERROR
	}

	private final String message;
	private final NotificationType type;
	private final Instant timestamp;
	private final String source;

	/**
	 * Creates a new {@link ChatNotifier}.
	 *
	 * @param message The notification message
	 * @param type The type of notification
	 * @param source The source of the notification (e.g., "Agent")
	 */
	public ChatNotifier(String message, NotificationType type, String source) {
		this.message = message;
		this.type = type;
		this.source = source;
		this.timestamp = Instant.now();
	}

	/**
	 * Creates a new {@link ChatNotifier} with "Agent" as the source.
	 *
	 * @param message The notification message
	 * @param type The type of notification
	 */
	public ChatNotifier(String message, NotificationType type) {
		this(message, type, "Agent");
	}

	/**
	 * The notification message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * The type of this notification.
	 */
	public NotificationType getType() {
		return type;
	}

	/**
	 * The timestamp when this notification was created.
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * The source of this notification.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Gets the icon for this notification type.
	 */
	public String getIcon() {
		switch (type) {
			case INFO:
				return "ℹ️";
			case PROGRESS:
				return "⏳";
			case SUCCESS:
				return "✅";
			case ERROR:
				return "❌";
			default:
				return "•";
		}
	}

	/**
	 * Checks if this is a minimalistic notification that should be styled differently.
	 */
	public boolean isMinimalistic() {
		return true; // All notifiers are minimalistic by design
	}
}
