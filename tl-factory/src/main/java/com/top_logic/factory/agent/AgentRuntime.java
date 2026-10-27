/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import com.top_logic.basic.util.I18NBundle;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * What the {@link WorkspaceAgent}s of the control application share.
 *
 * @param harness
 *        The agent that runs in the workspaces.
 * @param provider
 *        The provider of the workspaces.
 * @param tokens
 *        Issues the tokens of the agent processes; {@code null} when the workspace proxy is
 *        switched off.
 * @param idleTimeout
 *        How long an agent process is kept after its last run.
 * @param tokenLifetime
 *        The lifetime of the token of an agent process; a process whose token has less than half
 *        of it left is replaced by a new one before its next run.
 * @param interruptTimeout
 *        How long a run waits for the agent to stop after it was asked to, before its process is
 *        terminated.
 * @param stopTimeout
 *        How long a process that is no longer needed has to end after its input was closed,
 *        before it is terminated.
 * @param scheduler
 *        Runs the idle timeouts.
 * @param watchers
 *        Runs the threads waiting for the end of the agent processes.
 * @param clock
 *        The time source.
 * @param messages
 *        The messages to the user a run is done for, in the language of the user; the user is
 *        {@code null} when the run names none.
 */
public record AgentRuntime(AgentHarness harness, WorkspaceProvider provider, ProxyTokens tokens,
		Duration idleTimeout, Duration tokenLifetime, Duration interruptTimeout, Duration stopTimeout,
		ScheduledExecutorService scheduler, Executor watchers, Clock clock,
		Function<Person, ? extends I18NBundle> messages) {

	// Plain data.

}
