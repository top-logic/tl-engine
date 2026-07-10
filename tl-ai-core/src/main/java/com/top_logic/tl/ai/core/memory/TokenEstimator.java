/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.util.List;

import com.top_logic.tl.ai.core.message.Message;

/**
 * Estimates the token cost of messages for budget tracking and compaction decisions.
 *
 * <p>Implementations are stateless — the same message always produces the same estimate.
 * Estimates need not be exact (real tokenization is expensive); they must be consistent
 * and good enough to trigger compaction at the right time.</p>
 *
 * @see ByteBasedTokenEstimator
 */
public interface TokenEstimator {

	/**
	 * Estimate the token cost of a single message.
	 */
	int estimateTokens(Message message);

	/**
	 * Estimate the total token cost of a list of messages.
	 */
	int estimateTokens(List<Message> messages);
}
