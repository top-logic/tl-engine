/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

/**
 * One message in the chat (for JSON API).
 */
public class MessageDto {

	private String _id;
	private String _role;
	private String _userName;
	private String _content;
	private String _contentHtml;
	private String _time;

	/** Message ID. */
	public String getId() {
		return _id;
	}

	/** @see #getId() */
	public void setId(String id) {
		_id = id;
	}

	/** "user" or "agent" or "system" */
	public String getRole() {
		return _role;
	}

	/** @see #getRole() */
	public void setRole(String role) {
		_role = role;
	}

	/** Display name of the user who sent this message. */
	public String getUserName() {
		return _userName;
	}

	/** @see #getUserName() */
	public void setUserName(String userName) {
		_userName = userName;
	}

	/** Message text content. */
	public String getContent() {
		return _content;
	}

	/** @see #getContent() */
	public void setContent(String content) {
		_content = content;
	}

	/** Pre-rendered HTML for the message content (agent messages only; null for user/system). */
	public String getContentHtml() {
		return _contentHtml;
	}

	/** @see #getContentHtml() */
	public void setContentHtml(String contentHtml) {
		_contentHtml = contentHtml;
	}

	/** Formatted timestamp string. */
	public String getTime() {
		return _time;
	}

	/** @see #getTime() */
	public void setTime(String time) {
		_time = time;
	}
}
