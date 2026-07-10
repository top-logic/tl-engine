/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Agent lifecycle events emitted by {@link com.top_logic.tl.ai.core.agent.AgentLoop}.
 *
 * <p>Consumers implement {@link com.top_logic.tl.ai.core.agent.event.AgentEventListener}
 * and receive {@link com.top_logic.tl.ai.core.agent.event.AgentEvent} instances through
 * a single {@code onEvent(AgentEvent)} method. The framework serializes dispatch so
 * listeners never see concurrent calls even during parallel tool execution, and
 * swallows listener exceptions so a buggy consumer cannot abort a run.</p>
 */
package com.top_logic.tl.ai.core.agent.event;
