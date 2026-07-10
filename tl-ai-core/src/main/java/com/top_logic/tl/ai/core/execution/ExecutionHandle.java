/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.tl.ai.core.execution.model.AgentExecution;

/**
 * Mutable holder for the {@link AgentExecution} of the currently running agent loop.
 *
 * <p>
 * The run id — and with it the {@link AgentExecution} row — only comes into existence when the
 * loop emits its {@code RunStarted} event, after the (immutable) tool context has been built.
 * The context therefore carries this holder by reference: the {@link ExecutionRecorder} fills it
 * at run start, and tools such as the delegation tool read it to attribute child executions to
 * their parent.
 * </p>
 */
public final class ExecutionHandle {

	private volatile AgentExecution _execution;

	/** The recorded execution of the current run, or <code>null</code> if not recorded. */
	public AgentExecution get() {
		return _execution;
	}

	/** Sets the recorded execution of the current run. */
	public void set(AgentExecution execution) {
		_execution = execution;
	}
}
