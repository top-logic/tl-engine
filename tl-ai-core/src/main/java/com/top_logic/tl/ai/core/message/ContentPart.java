/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

/**
 * A piece of content within a {@link Message}.
 *
 * <p>Content parts represent the building blocks of messages: text, tool calls, and images.
 * They appear in {@link UserMessage} and {@link AssistantMessage} content lists.</p>
 *
 * @see TextContent
 * @see ToolCall
 * @see ImageContent
 */
public sealed interface ContentPart permits TextContent, ToolCall, ImageContent {
}
