/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Conversation memory and context management.
 *
 * <p>{@link com.top_logic.tl.ai.core.memory.ConversationMemory} wraps a
 * {@link com.top_logic.tl.ai.core.message.Conversation}, adding token tracking
 * and compaction. The agent loop uses it to run indefinitely without exceeding
 * the context window.</p>
 *
 * @see com.top_logic.tl.ai.core.memory.ConversationMemory
 * @see com.top_logic.tl.ai.core.memory.TokenEstimator
 * @see com.top_logic.tl.ai.core.memory.CompactionStrategy
 */
package com.top_logic.tl.ai.core.memory;
