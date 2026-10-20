/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Conversation memory and context management.
 *
 * <p>{@link com.top_logic.tl.ai.core.memory.ConversationMemory} wraps a
 * {@link com.top_logic.tl.ai.core.message.Conversation}, adding token tracking
 * and compaction. The agent loop uses it to run indefinitely without exceeding
 * the context window.</p>
 *
 * <p>The loop asks {@link com.top_logic.tl.ai.core.memory.ConversationMemory#shouldCompact()}
 * at the start of every turn, before that turn's request is built, so a conversation that no
 * longer fits its budget is compacted rather than sent and refused - see
 * {@code AgentLoop#compactIfOverBudget}. A provider that reports an overflow the estimate did
 * not see remains the fallback, not the normal path.</p>
 *
 * @see com.top_logic.tl.ai.core.memory.ConversationMemory
 * @see com.top_logic.tl.ai.core.memory.TokenEstimator
 * @see com.top_logic.tl.ai.core.memory.CompactionStrategy
 */
package com.top_logic.tl.ai.core.memory;
