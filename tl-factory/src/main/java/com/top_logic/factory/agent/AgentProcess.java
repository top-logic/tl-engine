/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.Logger;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.WorkspaceProvider;

/**
 * One running agent process of a {@link WorkspaceAgent}: the command in the workspace, its token,
 * the translator of its protocol, and the queue of what happened to it.
 *
 * <p>
 * The output of the process arrives on a thread of the provider; it is split into lines and put
 * on the {@link #take() queue}, which the run drains on its own thread. A watcher thread waits for
 * the end of the process, then puts an {@link Item.Exit} after its last line and revokes its token.
 * A stop of the user puts an {@link Item.Cancel} on the queue.
 * </p>
 */
final class AgentProcess {

	/**
	 * What the run reads from the queue of a process.
	 */
	sealed interface Item {

		/** A line the process wrote to its standard output. */
		record Line(String text) implements Item {
			// Plain data.
		}

		/** The process has ended; nothing follows. */
		record Exit(int exitCode) implements Item {
			// Plain data.
		}

		/** The user asked to stop the run. */
		record Cancel() implements Item {
			// Plain data.
		}
	}

	private static final Duration WATCH_INTERVAL = Duration.ofHours(1);

	private static final String HIDDEN = "***";

	private static final int UNKNOWN_EXIT_CODE = -1;

	private final BlockingQueue<Item> _queue = new LinkedBlockingQueue<>();

	private final LineBuffer _stdout = new LineBuffer(line -> _queue.add(new Item.Line(line)));

	private final LineBuffer _stderr;

	private final String _workspaceId;

	private final String _token;

	private final Instant _tokenExpires;

	private final ProxyTokens _tokens;

	private final AgentProtocol _protocol;

	private ExecProcess _process;

	private volatile boolean _ended;

	private AgentProcess(String workspaceId, String token, Instant tokenExpires, ProxyTokens tokens,
			AgentProtocol protocol) {
		_workspaceId = workspaceId;
		_token = token;
		_tokenExpires = tokenExpires;
		_tokens = tokens;
		_protocol = protocol;
		_stderr = new LineBuffer(this::logStderr);
	}

	/**
	 * Starts an agent process in a workspace.
	 *
	 * @param provider
	 *        The provider of the workspace.
	 * @param workspaceId
	 *        The workspace to run the process in.
	 * @param command
	 *        The command of the process, carrying the token in its environment.
	 * @param token
	 *        The token of the process, revoked when it ends.
	 * @param tokens
	 *        The issuer of the token.
	 * @param protocol
	 *        The translator of the protocol of the process.
	 * @param watchers
	 *        Runs the thread waiting for the end of the process.
	 */
	static AgentProcess start(WorkspaceProvider provider, String workspaceId, ExecCommand command, String token,
			ProxyTokens tokens, AgentProtocol protocol, Executor watchers) {
		AgentProcess result = new AgentProcess(workspaceId, token, tokens.expiresAt(token), tokens, protocol);
		try {
			result._process = provider.startExec(workspaceId, command, result.new Output());
		} catch (RuntimeException ex) {
			tokens.revoke(token);
			throw ex;
		}
		watchers.execute(result::watch);
		return result;
	}

	/**
	 * The translator of the protocol of this process.
	 */
	AgentProtocol protocol() {
		return _protocol;
	}

	/**
	 * Whether the process has ended.
	 */
	boolean isEnded() {
		return _ended;
	}

	/**
	 * Whether the token of the process is still valid at the given time.
	 */
	boolean isTokenValidAt(Instant time) {
		return _tokenExpires != null && time.isBefore(_tokenExpires);
	}

	/**
	 * Writes input to the process.
	 */
	void write(String input) throws IOException {
		_process.write(input);
		_process.flush();
	}

	/**
	 * Takes the next item from the queue, waiting until there is one.
	 */
	Item take() throws InterruptedException {
		return _queue.take();
	}

	/**
	 * Takes the next item from the queue, waiting at most the given time.
	 *
	 * @return The item, {@code null} if none came in time.
	 */
	Item poll(Duration timeout) throws InterruptedException {
		return _queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Drops what arrived after the previous run had ended: the lines the process wrote outside of
	 * a run and a stop that came too late.
	 */
	void discardStale() {
		_queue.removeIf(item -> {
			if (item instanceof Item.Line line) {
				Logger.debug("Agent in workspace '" + _workspaceId + "' wrote outside of a run: " + line.text(),
					AgentProcess.class);
				return true;
			}
			return item instanceof Item.Cancel;
		});
	}

	/**
	 * Signals the run that the user asked to stop it.
	 */
	void cancel() {
		_queue.add(new Item.Cancel());
	}

	/**
	 * Terminates the process and revokes its token; its {@link Item.Exit} follows on the queue.
	 */
	void terminate() {
		_tokens.revoke(_token);
		try {
			_process.terminate();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			_process.close();
		}
	}

	/**
	 * Ends the process when it is not needed any more: its input is closed, which ends it in an
	 * orderly way, and it is terminated if it does not end within the given time.
	 */
	void stop(Duration grace) {
		_tokens.revoke(_token);
		try {
			_process.closeStdin();
		} catch (IOException ex) {
			// Terminated below.
		}
		try {
			if (!_process.waitFor(grace)) {
				_process.terminate();
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} finally {
			_process.close();
		}
	}

	private void watch() {
		int exitCode = UNKNOWN_EXIT_CODE;
		try {
			while (!_process.waitFor(WATCH_INTERVAL)) {
				// Still running.
			}
			exitCode = _process.exitCode();
		} catch (InterruptedException ex) {
			// Shutdown: the process is closed by its owner.
		} finally {
			_stdout.finish();
			_stderr.finish();
			_ended = true;
			_tokens.revoke(_token);
			_queue.add(new Item.Exit(exitCode));
			_process.close();
		}
	}

	private void logStderr(String line) {
		if (line.isBlank()) {
			return;
		}
		String text = _token == null ? line : line.replace(_token, HIDDEN);
		Logger.info("Agent in workspace '" + _workspaceId + "': " + text, AgentProcess.class);
	}

	/**
	 * Receives the output of the process.
	 */
	private final class Output implements ExecOutput {

		@Override
		public void stdout(String text) {
			_stdout.append(text);
		}

		@Override
		public void stderr(String text) {
			_stderr.append(text);
		}
	}

}
