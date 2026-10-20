/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * A minimal evaluation harness: the same business tasks, put to several models through our own
 * agent loop, graded against the database they left behind.
 *
 * <p>
 * The question this answers is not "which model is best". It is "which models work <em>with our
 * harness</em>" - our tool descriptions, our system prompts, our skills, our approval flow. A model
 * that scores well on a public function-calling benchmark can still fail in front of ninety tools
 * it has never seen described this way, and that is the failure this finds.
 * </p>
 *
 * <p>
 * {@link com.top_logic.tl.ai.eval.EvalConfig} is what an eval is written as,
 * {@link com.top_logic.tl.ai.eval.EvalMatrix} runs it,
 * {@link com.top_logic.tl.ai.eval.Graders} decides it,
 * {@link com.top_logic.tl.ai.eval.EvalCsv} records it and
 * {@link com.top_logic.tl.ai.eval.EvalReport} aggregates it. The module README says how to add a
 * scenario, how to run the matrix and what the numbers may be used for.
 * </p>
 */
package com.top_logic.tl.ai.eval;
