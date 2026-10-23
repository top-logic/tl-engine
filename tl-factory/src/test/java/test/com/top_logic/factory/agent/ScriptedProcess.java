/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.top_logic.basic.sched.DaemonThreadFactory;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;

/**
 * An {@link ExecProcess} answering each line of its input with lines of output, written from a
 * thread of its own in chunks that do not follow the lines, as a provider delivers them.
 */
public class ScriptedProcess implements ExecProcess {

	/**
	 * Output line ending the process with {@link #CRASH_EXIT_CODE}.
	 */
	public static final String CRASH = "<crash>";

	/** Exit code of a process ending with {@link #CRASH}. */
	public static final int CRASH_EXIT_CODE = 1;

	/** Exit code of a terminated process. */
	public static final int TERMINATED_EXIT_CODE = 143;

	/** Size of the chunks the output is delivered in. */
	private static final int CHUNK = 37;

	private final ExecCommand _command;

	private final ExecOutput _output;

	private final Function<String, List<String>> _answers;

	private final ExecutorService _writer = Executors.newSingleThreadExecutor(DaemonThreadFactory.daemonThreadFactory());

	private final StringBuilder _pendingInput = new StringBuilder();

	private final List<String> _input = new CopyOnWriteArrayList<>();

	private final CountDownLatch _ended = new CountDownLatch(1);

	private volatile int _exitCode;

	private volatile boolean _stdinClosed;

	private volatile boolean _terminated;

	private volatile boolean _closed;

	/**
	 * Creates a {@link ScriptedProcess}.
	 *
	 * @param command
	 *        The command it was started with.
	 * @param output
	 *        Receives its output.
	 * @param answers
	 *        The output for a line of input, possibly ending with {@link #CRASH}.
	 */
	public ScriptedProcess(ExecCommand command, ExecOutput output, Function<String, List<String>> answers) {
		_command = command;
		_output = output;
		_answers = answers;
	}

	/**
	 * The command the process was started with.
	 */
	public ExecCommand command() {
		return _command;
	}

	/**
	 * The lines written to the process.
	 */
	public List<String> input() {
		return _input;
	}

	/**
	 * Whether the input was closed.
	 */
	public boolean isStdinClosed() {
		return _stdinClosed;
	}

	/**
	 * Whether the process was terminated.
	 */
	public boolean isTerminated() {
		return _terminated;
	}

	/**
	 * Whether the handle was closed.
	 */
	public boolean isClosed() {
		return _closed;
	}

	/**
	 * Waits until the process has ended.
	 */
	public boolean awaitEnd(Duration timeout) throws InterruptedException {
		return _ended.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public void write(byte[] data) throws IOException {
		write(new String(data, java.nio.charset.StandardCharsets.UTF_8));
	}

	@Override
	public synchronized void write(String text) throws IOException {
		if (_stdinClosed || _ended.getCount() == 0) {
			throw new IOException("Input closed.");
		}
		_pendingInput.append(text);
		int end;
		while ((end = _pendingInput.indexOf("\n")) >= 0) {
			String line = _pendingInput.substring(0, end);
			_pendingInput.delete(0, end + 1);
			_input.add(line);
			List<String> answer = _answers.apply(line);
			_writer.execute(() -> deliver(answer));
		}
	}

	private void deliver(List<String> lines) {
		StringBuilder output = new StringBuilder();
		for (String line : lines) {
			if (CRASH.equals(line)) {
				emit(output);
				end(CRASH_EXIT_CODE);
				return;
			}
			output.append(line).append('\n');
		}
		emit(output);
	}

	private void emit(StringBuilder output) {
		for (int n = 0; n < output.length(); n += CHUNK) {
			if (_ended.getCount() == 0) {
				return;
			}
			_output.stdout(output.substring(n, Math.min(output.length(), n + CHUNK)));
		}
	}

	private synchronized void end(int exitCode) {
		if (_ended.getCount() > 0) {
			_exitCode = exitCode;
			_ended.countDown();
		}
	}

	@Override
	public void flush() {
		// Delivered on write.
	}

	@Override
	public void closeStdin() {
		_stdinClosed = true;
		// Claude Code ends at the end of its input.
		_writer.execute(() -> end(0));
	}

	@Override
	public boolean waitFor(Duration timeout) throws InterruptedException {
		return _ended.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean isAlive() {
		return _ended.getCount() > 0;
	}

	@Override
	public int exitCode() {
		if (isAlive()) {
			throw new IllegalStateException("Running.");
		}
		return _exitCode;
	}

	@Override
	public void terminate() {
		_terminated = true;
		end(TERMINATED_EXIT_CODE);
	}

	@Override
	public void close() {
		_closed = true;
		end(TERMINATED_EXIT_CODE);
		_writer.shutdown();
	}

}
