/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.time.Duration;
import java.util.concurrent.Executor;

import com.top_logic.basic.Logger;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.LogFollower;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.layout.view.channel.ChannelSink;
import com.top_logic.model.TLObject;

/**
 * The delivery of the log of one workspace into a {@link ChannelSink}: the last lines of the
 * output of its main process, published as one text after every chunk that arrives.
 *
 * <p>
 * Follows one workspace at a time, the one last {@link #follow(Object) given}. The delivery of a
 * workspace ends when its main process ends - it stops, it is updated -; a
 * {@link #refresh(TLObject) change} of the workspace afterwards starts the delivery anew with the
 * last lines of the new output. The calls to the provider run on the given executor, never on the
 * calling thread, which may be a committing one.
 * </p>
 *
 * @see WorkspaceLogSource
 */
public class WorkspaceLogFollow {

	private final WorkspaceProvider _provider;

	private final int _tailLines;

	private final ChannelSink _sink;

	private final Executor _executor;

	private final LogTail _tail;

	/** The workspace followed, {@code null} for none. */
	private TLObject _workspace;

	/** The delivery of the log of {@link #_workspace}, {@code null} while there is none. */
	private LogFollower _follower;

	/** Counts the deliveries, so that the output of a replaced one is dropped. */
	private int _generation;

	private boolean _closed;

	/**
	 * Creates a {@link WorkspaceLogFollow}.
	 *
	 * @param provider
	 *        The provider delivering the logs.
	 * @param tailLines
	 *        The number of lines kept and published.
	 * @param sink
	 *        Receiver of the text of the kept lines, the empty text while nothing is followed.
	 * @param executor
	 *        Where the provider is asked to deliver.
	 */
	public WorkspaceLogFollow(WorkspaceProvider provider, int tailLines, ChannelSink sink, Executor executor) {
		_provider = provider;
		_tailLines = tailLines;
		_sink = sink;
		_executor = executor;
		_tail = new LogTail(tailLines);
	}

	/**
	 * Follows the log of the given workspace instead of the one followed so far.
	 *
	 * @param value
	 *        The value of the workspace channel: a {@link FactoryModel#WORKSPACE}, anything else
	 *        follows nothing.
	 */
	public synchronized void follow(Object value) {
		TLObject workspace = isWorkspace(value) ? (TLObject) value : null;
		if (_closed || workspace == _workspace) {
			return;
		}
		_workspace = workspace;
		restart();
	}

	/**
	 * Starts the delivery of the log anew, if the given workspace is the followed one and its
	 * delivery has ended.
	 *
	 * @param changed
	 *        A workspace that was changed.
	 */
	public synchronized void refresh(TLObject changed) {
		if (_closed || changed == null || changed != _workspace) {
			return;
		}
		LogFollower follower = _follower;
		if (follower != null && !hasEnded(follower)) {
			return;
		}
		restart();
	}

	/**
	 * The followed workspace, {@code null} for none.
	 */
	public synchronized TLObject workspace() {
		return _workspace;
	}

	/**
	 * Ends the delivery for good.
	 */
	public synchronized void close() {
		_closed = true;
		_generation++;
		stopFollower();
	}

	private void restart() {
		int generation = ++_generation;
		stopFollower();
		_tail.clear();
		_sink.publish("");
		TLObject workspace = _workspace;
		if (workspace == null || !workspace.tValid()) {
			return;
		}
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		_executor.execute(() -> start(generation, workspaceId));
	}

	private void start(int generation, String workspaceId) {
		synchronized (this) {
			if (generation != _generation) {
				return;
			}
		}
		LogFollower follower;
		try {
			follower = _provider.followLog(workspaceId, _tailLines, null, output(generation));
		} catch (RuntimeException ex) {
			// A workspace without container has no log; the next change of the workspace tries again.
			Logger.debug("No log of workspace '" + workspaceId + "': " + ex.getMessage(), WorkspaceLogFollow.class);
			return;
		}
		synchronized (this) {
			if (generation == _generation) {
				_follower = follower;
				return;
			}
		}
		follower.close();
	}

	private ExecOutput output(int generation) {
		return new ExecOutput() {
			@Override
			public void stdout(String text) {
				append(generation, text);
			}

			@Override
			public void stderr(String text) {
				append(generation, text);
			}
		};
	}

	private synchronized void append(int generation, String text) {
		if (generation != _generation) {
			return;
		}
		_tail.append(text);
		_sink.publish(_tail.text());
	}

	private void stopFollower() {
		LogFollower follower = _follower;
		_follower = null;
		if (follower != null) {
			follower.close();
		}
	}

	private static boolean hasEnded(LogFollower follower) {
		try {
			return follower.awaitEnd(Duration.ZERO);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private static boolean isWorkspace(Object value) {
		return value instanceof TLObject object && object.tValid() && object.tType() == FactoryModel.workspaceType();
	}

}
