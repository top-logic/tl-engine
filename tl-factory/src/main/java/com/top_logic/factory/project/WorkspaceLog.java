/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.function.Consumer;

import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.model.TLObject;

/**
 * The output of an operation on a workspace, stored in its {@link FactoryModel#WORKSPACE_LOG log}.
 *
 * <p>
 * The output arrives in small chunks while the operation runs. It is collected and stored by
 * {@link #flush()}, which the owner calls periodically, so that a fast producer costs one commit
 * per period, not one per chunk. Only the end of the output is kept, at most
 * {@link #getMaxSize()} characters.
 * </p>
 *
 * <p>
 * All changes to the workspace while the operation runs go through {@link #update(Consumer)},
 * which stores the collected output with them, so that the writes of the operation and of the
 * periodic flush never interleave.
 * </p>
 */
public class WorkspaceLog implements ExecOutput {

	/**
	 * Runs a change of the model in a transaction of its own.
	 */
	public interface ModelWriter {

		/**
		 * Runs the given change in a transaction and commits it.
		 */
		void commit(Runnable change);

	}

	private static final String LINE_END = "\n";

	private static final String TRUNCATED = "[...]" + LINE_END;

	private final TLObject _workspace;

	private final int _maxSize;

	private final ModelWriter _writer;

	private final StringBuilder _text = new StringBuilder();

	private boolean _dirty;

	/**
	 * Creates a {@link WorkspaceLog} starting empty.
	 *
	 * @param workspace
	 *        The workspace whose log is written.
	 * @param maxSize
	 *        See {@link #getMaxSize()}.
	 * @param writer
	 *        Commits the changes of the workspace.
	 */
	public WorkspaceLog(TLObject workspace, int maxSize, ModelWriter writer) {
		_workspace = workspace;
		_maxSize = maxSize;
		_writer = writer;
	}

	/**
	 * The maximum number of characters stored; older output is dropped.
	 */
	public int getMaxSize() {
		return _maxSize;
	}

	/**
	 * The workspace whose log is written.
	 */
	public TLObject getWorkspace() {
		return _workspace;
	}

	@Override
	public void stdout(String text) {
		append(text);
	}

	@Override
	public void stderr(String text) {
		append(text);
	}

	/**
	 * Appends a line of its own, starting it on a new line if the output so far ends in the middle
	 * of one.
	 */
	public synchronized void line(String message) {
		if (_text.length() > 0 && _text.charAt(_text.length() - 1) != '\n') {
			append(LINE_END);
		}
		append(message + LINE_END);
	}

	/**
	 * The text collected so far, at most {@link #getMaxSize()} characters.
	 */
	public synchronized String getText() {
		return _text.toString();
	}

	private synchronized void append(String text) {
		_text.append(text);
		int excess = _text.length() - _maxSize;
		if (excess > 0) {
			int cut = _text.indexOf(LINE_END, excess + TRUNCATED.length());
			_text.delete(0, cut < 0 ? excess + TRUNCATED.length() : cut + 1);
			_text.insert(0, TRUNCATED);
		}
		_dirty = true;
	}

	/**
	 * Stores the collected output, if it changed since it was stored last.
	 */
	public void flush() {
		update(null);
	}

	/**
	 * Stores the collected output together with the given change of the workspace.
	 *
	 * @param change
	 *        The change, applied in the same transaction; {@code null} for none.
	 */
	public synchronized void update(Consumer<TLObject> change) {
		if (change == null && !_dirty) {
			return;
		}
		String text = _text.toString();
		_writer.commit(() -> {
			if (!_workspace.tValid()) {
				return;
			}
			_workspace.tUpdateByName(FactoryModel.WORKSPACE_LOG, text);
			if (change != null) {
				change.accept(_workspace);
			}
		});
		_dirty = false;
	}

}
