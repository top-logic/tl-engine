/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import com.top_logic.factory.workspace.ExecCommand;

/**
 * A coding agent that runs as a process in a workspace and talks to the control application
 * through its standard input and output, e.g. Claude Code.
 *
 * <p>
 * The harness knows the agent's command line and its protocol; the process itself - starting it
 * lazily, keeping it between messages, its token, its idle timeout, stopping it - is managed by
 * {@link WorkspaceAgent}, the same for every harness.
 * </p>
 *
 * <p>
 * One process serves the messages of one conversation, one after the other. The agent keeps the
 * conversation under a session ID of its own; a new process continues it from that ID, see
 * {@link AgentLaunch#resumeSessionId()}.
 * </p>
 */
public interface AgentHarness {

	/**
	 * The agent name the runs of this agent are recorded under, e.g. {@code claude-code}.
	 */
	String agentName();

	/**
	 * The command starting the agent process.
	 *
	 * @param launch
	 *        Where the process runs, the conversation it continues, and its environment with the
	 *        token for the workspace proxy, which the command passes to the process only.
	 */
	ExecCommand command(AgentLaunch launch);

	/**
	 * A new translator of the protocol of one process.
	 *
	 * <p>
	 * A process gets a protocol of its own, which may keep state across the messages of the
	 * process.
	 * </p>
	 */
	AgentProtocol newProtocol();

}
