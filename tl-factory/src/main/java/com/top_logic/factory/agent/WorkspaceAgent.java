/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.ai.chat.backend.BackendFeatures;
import com.top_logic.ai.chat.backend.BackendTurn;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.basic.Logger;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.error.TopLogicException;

/**
 * The {@link ConversationBackend} of one conversation with the coding agent of a workspace.
 *
 * <p>
 * The agent runs as one long-lived process per conversation, described by an
 * {@link AgentHarness}:
 * </p>
 * <ul>
 * <li>The process is started on the first message, in the {@link ConversationTarget#workingDir()
 * working directory} of the workspace, with a token of its own for the LLM proxy, issued for
 * {@link AgentRuntime#tokenLifetime()} and revoked when the process ends. It continues the
 * conversation stored under its {@link ConversationTarget#sessionId() session ID}, if there is one.
 * </li>
 * <li>It is kept between the messages and stopped after {@link AgentRuntime#idleTimeout()} without
 * a message; the next message starts a new process, which continues the conversation. A process
 * whose token has less than half of its lifetime left is replaced the same way before a run.</li>
 * <li>The session ID the process reports is stored as soon as it is known.</li>
 * <li>A stop of the user asks the agent to stop (see {@link AgentProtocol#interrupt()}); a process
 * that cannot be asked, or does not stop within {@link AgentRuntime#interruptTimeout()}, is
 * terminated. A process that ends during a run ends the run in an error.</li>
 * </ul>
 */
public class WorkspaceAgent implements ConversationBackend {

	/**
	 * Kind of the error that ends a run whose process ended.
	 */
	public static final String ERROR_PROCESS_EXIT = "PROCESS_EXIT";

	private final AgentRuntime _runtime;

	private final ConversationTarget _conversation;

	private final String _workspaceId;

	/** The context window the agent last reported, zero before it reported one. */
	private volatile int _contextWindow;

	private AgentProcess _process;

	private ScheduledFuture<?> _idleStop;

	private boolean _running;

	/**
	 * Creates a {@link WorkspaceAgent}.
	 *
	 * @param runtime
	 *        What the agents of the control application share.
	 * @param conversation
	 *        The conversation this agent holds.
	 */
	public WorkspaceAgent(AgentRuntime runtime, ConversationTarget conversation) {
		_runtime = runtime;
		_conversation = conversation;
		_workspaceId = conversation.workspaceId();
	}

	/**
	 * The workspace the agent works in.
	 */
	public String workspaceId() {
		return _workspaceId;
	}

	/**
	 * The conversation this agent holds.
	 */
	public ConversationTarget getConversation() {
		return _conversation;
	}

	@Override
	public String agentName() {
		return _runtime.harness().agentName();
	}

	@Override
	public BackendFeatures features() {
		return BackendFeatures.NONE;
	}

	/**
	 * The context window the agent reported with its last answer in this conversation, zero
	 * before it answered since the control application started.
	 */
	@Override
	public int contextWindow() {
		return _contextWindow;
	}

	@Override
	public String send(BackendTurn turn, AgentEventListener sink, CancellationToken cancel) throws Exception {
		AgentProcess process = begin();
		try {
			AgentProtocol protocol = process.protocol();
			protocol.startRun(UUID.randomUUID().toString(), sink);
			try (CancellationToken.Registration registration = cancel.onCancel(process::cancel)) {
				try {
					process.write(protocol.userMessage(turn.text()));
				} catch (IOException ex) {
					Logger.info("Agent in workspace '" + _workspaceId + "' takes no input: "
						+ ex.getMessage(), WorkspaceAgent.class);
					process.terminate();
				}
				drain(process, protocol);
			}
			int window = protocol.contextWindow();
			if (window > 0) {
				_contextWindow = window;
			}
			return protocol.answer();
		} finally {
			end();
		}
	}

	/**
	 * Delivers what the process does on this thread, until the run has ended.
	 */
	private void drain(AgentProcess process, AgentProtocol protocol) {
		long interruptDeadline = 0;
		boolean interrupting = false;
		boolean cancelled = false;
		try {
			while (true) {
				AgentProcess.Item item;
				if (!interrupting) {
					item = process.take();
				} else {
					long left = interruptDeadline - System.nanoTime();
					item = left <= 0 ? null : process.poll(Duration.ofNanos(left));
				}
				if (item == null) {
					Logger.info("Agent in workspace '" + _workspaceId
						+ "' did not stop when asked, terminating it.", WorkspaceAgent.class);
					interrupting = false;
					process.terminate();
					continue;
				}
				if (item instanceof AgentProcess.Item.Line line) {
					boolean done = protocol.onLine(line.text());
					storeSessionId(protocol);
					if (done) {
						return;
					}
				} else if (item instanceof AgentProcess.Item.Cancel) {
					if (cancelled) {
						continue;
					}
					cancelled = true;
					protocol.cancelRequested();
					String interrupt = protocol.interrupt();
					if (interrupt != null && writeQuietly(process, interrupt)) {
						interrupting = true;
						interruptDeadline = System.nanoTime() + _runtime.interruptTimeout().toNanos();
					} else {
						process.terminate();
					}
				} else if (item instanceof AgentProcess.Item.Exit exit) {
					if (cancelled) {
						protocol.endRun(StopReason.ABORTED, null, null);
					} else {
						protocol.endRun(StopReason.ERROR, ERROR_PROCESS_EXIT,
							"The agent process ended unexpectedly with exit code " + exit.exitCode() + ".");
					}
					return;
				}
			}
		} catch (InterruptedException ex) {
			process.terminate();
			protocol.endRun(StopReason.ABORTED, null, null);
			Thread.currentThread().interrupt();
		}
	}

	private boolean writeQuietly(AgentProcess process, String input) {
		try {
			process.write(input);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	private void storeSessionId(AgentProtocol protocol) {
		String sessionId = protocol.sessionId();
		if (sessionId != null && !Objects.equals(sessionId, _conversation.sessionId())) {
			_conversation.storeSessionId(sessionId);
		}
	}

	/**
	 * Takes the process for a run, starting one if there is none that can serve it.
	 */
	private synchronized AgentProcess begin() {
		if (_running) {
			throw new IllegalStateException("The agent of workspace '" + _workspaceId
				+ "' is answering another message.");
		}
		cancelIdleStop();
		AgentProcess process = _process;
		Instant now = _runtime.clock().instant();
		if (process != null
			&& (process.isEnded() || !process.isTokenValidAt(now.plus(_runtime.tokenLifetime().dividedBy(2))))) {
			stopProcess();
			process = null;
		}
		if (process == null) {
			process = startProcess();
			_process = process;
		} else {
			process.discardStale();
		}
		_running = true;
		return process;
	}

	private AgentProcess startProcess() {
		String workspaceId = _workspaceId;
		if (_runtime.provider().status(workspaceId).getState() != WorkspaceState.RUNNING) {
			throw new TopLogicException(I18NConstants.ERROR_WORKSPACE_NOT_RUNNING__WORKSPACE
				.fill(_conversation.displayName()));
		}
		if (_runtime.tokens() == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_LLM_ACCESS);
		}
		String token = _runtime.tokens().issue(workspaceId, _runtime.tokenLifetime());
		if (token == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_LLM_ACCESS);
		}
		AgentHarness harness = _runtime.harness();
		AgentLaunch launch = new AgentLaunch(_conversation.workingDir(), _conversation.sessionId(), token);
		Logger.info("Starting agent '" + harness.agentName() + "' in workspace '" + workspaceId + "': " + launch,
			WorkspaceAgent.class);
		return AgentProcess.start(_runtime.provider(), workspaceId, harness.command(launch), token,
			_runtime.tokens(), harness.newProtocol(), _runtime.watchers());
	}

	private synchronized void end() {
		_running = false;
		if (_process != null && _process.isEnded()) {
			_process = null;
		}
		if (_process != null) {
			long millis = _runtime.idleTimeout().toMillis();
			_idleStop = _runtime.scheduler().schedule(this::stopIdle, millis, TimeUnit.MILLISECONDS);
		}
	}

	private synchronized void stopIdle() {
		if (_running) {
			return;
		}
		_idleStop = null;
		if (_process != null) {
			Logger.info("Stopping the idle agent in workspace '" + _workspaceId + "'.",
				WorkspaceAgent.class);
			stopProcess();
		}
	}

	/**
	 * Stops the process of this agent, if there is one; the next message starts a new one.
	 *
	 * <p>
	 * A run in progress ends in an error.
	 * </p>
	 */
	public synchronized void stop() {
		cancelIdleStop();
		if (_process != null) {
			stopProcess();
		}
	}

	/**
	 * Whether a process of this agent runs.
	 */
	public synchronized boolean hasProcess() {
		return _process != null && !_process.isEnded();
	}

	private void stopProcess() {
		AgentProcess process = _process;
		_process = null;
		process.stop(_runtime.stopTimeout());
	}

	private void cancelIdleStop() {
		if (_idleStop != null) {
			_idleStop.cancel(false);
			_idleStop = null;
		}
	}

}
