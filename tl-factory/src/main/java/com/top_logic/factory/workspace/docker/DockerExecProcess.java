/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.docker;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.Logger;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;

/**
 * {@link ExecProcess} of a command run by a Docker exec instance.
 *
 * <p>
 * The command runs under the shell script {@link DockerWorkspaceProvider#PID_SCRIPT}, which writes
 * the process ID of the command as first line of its standard output and then replaces itself by
 * the command. A process started by Docker exec leads its own process group, so this ID also
 * identifies the group of the command and the processes it starts. The Docker API has no way to
 * signal an exec instance; {@link #terminate()} therefore runs {@code kill} in the container for
 * this process group: first {@link DockerWorkspaceProvider#SIGNAL_TERM}, after the grace period
 * {@link DockerWorkspaceProvider#SIGNAL_KILL}.
 * </p>
 *
 * <p>
 * The output arrives over a {@link HijackedConnection}, multiplexed into frames of standard output
 * and error output, and is decoded by a reader thread of this process.
 * </p>
 */
final class DockerExecProcess implements ExecProcess {

	/**
	 * Length of the header of a frame of the multiplexed output: the stream type, three unused
	 * bytes, the length of the payload as unsigned 32 bit big endian number.
	 */
	private static final int FRAME_HEADER_SIZE = 8;

	private static final int STREAM_STDOUT = 1;

	private static final int STREAM_STDERR = 2;

	private static final byte LINE_FEED = '\n';

	private static final int NO_PID = -1;

	/**
	 * Time to wait for the process ID before a command can be signalled.
	 */
	private static final Duration PID_TIMEOUT = Duration.ofSeconds(10);

	/**
	 * Time a killed command gets to disappear before its connection is dropped.
	 */
	private static final Duration KILL_TIMEOUT = Duration.ofSeconds(10);

	private final DockerWorkspaceProvider _provider;

	private final String _workspaceId;

	private final String _containerId;

	private final String _execId;

	private final ExecCommand _command;

	private final HijackedConnection _connection;

	private final ExecOutput _output;

	private final TextChunkDecoder _stdout = new TextChunkDecoder();

	private final TextChunkDecoder _stderr = new TextChunkDecoder();

	private final ByteArrayOutputStream _pidLine = new ByteArrayOutputStream();

	private final CountDownLatch _pidKnown = new CountDownLatch(1);

	private final CountDownLatch _ended = new CountDownLatch(1);

	private final Object _stdinLock = new Object();

	private volatile int _pid = NO_PID;

	private volatile Integer _exitCode;

	private boolean _stdinClosed;

	/**
	 * Whether the connection was closed by this handle, so that its end is no failure.
	 */
	private volatile boolean _closed;

	DockerExecProcess(DockerWorkspaceProvider provider, String workspaceId, String containerId, String execId,
			ExecCommand command, HijackedConnection connection, ExecOutput output) {
		_provider = provider;
		_workspaceId = workspaceId;
		_containerId = containerId;
		_execId = execId;
		_command = command;
		_connection = connection;
		_output = output;
	}

	/**
	 * Starts the thread reading the output of the command.
	 */
	void startReading() {
		Thread reader = new Thread(this::read, "tl-factory-exec-" + _workspaceId);
		reader.setDaemon(true);
		reader.start();
	}

	private void read() {
		InputStream in = _connection.input();
		byte[] header = new byte[FRAME_HEADER_SIZE];
		try {
			while (true) {
				int headerRead = in.readNBytes(header, 0, FRAME_HEADER_SIZE);
				if (headerRead < FRAME_HEADER_SIZE) {
					if (headerRead > 0) {
						throw new EOFException("Incomplete frame header.");
					}
					break;
				}
				long size = ((header[4] & 0xFFL) << 24) | ((header[5] & 0xFF) << 16) | ((header[6] & 0xFF) << 8)
					| (header[7] & 0xFF);
				byte[] payload = in.readNBytes((int) size);
				if (payload.length < size) {
					throw new EOFException("Incomplete frame.");
				}
				switch (header[0]) {
					case STREAM_STDOUT:
						stdout(payload);
						break;
					case STREAM_STDERR:
						emitStderr(_stderr.decode(payload));
						break;
					default:
						break;
				}
			}
		} catch (IOException ex) {
			if (!_closed) {
				Logger.info("Output of command '" + _command + "' in workspace '" + _workspaceId + "' broke off: "
					+ ex.getMessage(), DockerExecProcess.class);
			}
		} catch (RuntimeException ex) {
			Logger.error("Delivering the output of command '" + _command + "' in workspace '" + _workspaceId
				+ "' failed.", ex, DockerExecProcess.class);
		} finally {
			try {
				emitStdout(_stdout.finish());
				emitStderr(_stderr.finish());
			} finally {
				_pidKnown.countDown();
				_ended.countDown();
			}
		}
	}

	private void stdout(byte[] payload) {
		if (_pid != NO_PID) {
			emitStdout(_stdout.decode(payload));
			return;
		}
		int lineEnd = -1;
		for (int n = 0; n < payload.length; n++) {
			if (payload[n] == LINE_FEED) {
				lineEnd = n;
				break;
			}
		}
		if (lineEnd < 0) {
			_pidLine.write(payload, 0, payload.length);
			return;
		}
		_pidLine.write(payload, 0, lineEnd);
		String pid = _pidLine.toString(StandardCharsets.US_ASCII).trim();
		try {
			_pid = Integer.parseInt(pid);
		} catch (NumberFormatException ex) {
			Logger.error("No process ID reported by command '" + _command + "' in workspace '" + _workspaceId
				+ "': " + pid, DockerExecProcess.class);
		}
		_pidKnown.countDown();
		if (lineEnd + 1 < payload.length) {
			emitStdout(_stdout.decode(Arrays.copyOfRange(payload, lineEnd + 1, payload.length)));
		}
	}

	private void emitStdout(String text) {
		if (!text.isEmpty()) {
			_output.stdout(text);
		}
	}

	private void emitStderr(String text) {
		if (!text.isEmpty()) {
			_output.stderr(text);
		}
	}

	@Override
	public void write(byte[] data) throws IOException {
		synchronized (_stdinLock) {
			stdin().write(data);
		}
	}

	@Override
	public void write(String text) throws IOException {
		write(text.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void flush() throws IOException {
		synchronized (_stdinLock) {
			stdin().flush();
		}
	}

	private OutputStream stdin() throws IOException {
		if (_stdinClosed) {
			throw new IOException("The input of command '" + _command + "' is closed.");
		}
		return _connection.output();
	}

	@Override
	public void closeStdin() throws IOException {
		synchronized (_stdinLock) {
			if (_stdinClosed) {
				return;
			}
			_stdinClosed = true;
			_connection.shutdownOutput();
		}
	}

	@Override
	public boolean waitFor(Duration timeout) throws InterruptedException {
		if (_exitCode != null) {
			return true;
		}
		if (!_ended.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			return false;
		}
		_exitCode = _provider.awaitExitCode(_execId, _command, _workspaceId);
		return true;
	}

	@Override
	public boolean isAlive() throws InterruptedException {
		return !waitFor(Duration.ZERO);
	}

	@Override
	public int exitCode() {
		Integer exitCode = _exitCode;
		if (exitCode == null) {
			throw new IllegalStateException("Command '" + _command + "' in workspace '" + _workspaceId
				+ "' has not been found finished.");
		}
		return exitCode.intValue();
	}

	@Override
	public void terminate() throws InterruptedException {
		if (!isAlive()) {
			return;
		}
		int pid = awaitPid();
		if (pid != NO_PID) {
			_provider.signal(_containerId, pid, DockerWorkspaceProvider.SIGNAL_TERM, _command.getUser());
			if (waitFor(_provider.getTerminateTimeout())) {
				return;
			}
			_provider.signal(_containerId, pid, DockerWorkspaceProvider.SIGNAL_KILL, _command.getUser());
			if (waitFor(KILL_TIMEOUT)) {
				return;
			}
		}
		// A process outside the process group of the command still holds its output open.
		_closed = true;
		_connection.close();
		waitFor(KILL_TIMEOUT);
	}

	private int awaitPid() throws InterruptedException {
		_pidKnown.await(PID_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		return _pid;
	}

	@Override
	public void close() {
		if (_ended.getCount() > 0 && _pid != NO_PID) {
			boolean interrupted = Thread.interrupted();
			try {
				_provider.signal(_containerId, _pid, DockerWorkspaceProvider.SIGNAL_KILL, _command.getUser());
			} finally {
				if (interrupted) {
					Thread.currentThread().interrupt();
				}
			}
		}
		_closed = true;
		_connection.close();
	}

	@Override
	public String toString() {
		return _command + " (" + _workspaceId + ")";
	}

}
