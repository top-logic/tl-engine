/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

/**
 * Request body for POST /chat/send.
 */
public class ChatSendRequest {

	private String _text;

	/** User message text. */
	public String getText() {
		return _text;
	}

	/** @see #getText() */
	public void setText(String text) {
		_text = text;
	}
}
