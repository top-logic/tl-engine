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
	private String _time;

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	/** "user" or "agent" or "system" */
	public String getRole() {
		return _role;
	}

	public void setRole(String role) {
		_role = role;
	}

	public String getUserName() {
		return _userName;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		_content = content;
	}

	public String getTime() {
		return _time;
	}

	public void setTime(String time) {
		_time = time;
	}
}
