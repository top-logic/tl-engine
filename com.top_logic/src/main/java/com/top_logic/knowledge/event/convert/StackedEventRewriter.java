/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.List;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * Factory class to create an {@link EventRewriter} which rewrites by rewriting through a sequence
 * of {@link EventRewriter}.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StackedEventRewriter {

	private static final class StackedRewriter implements EventRewriter, EventWriter {

		private final EventRewriter[] _rewriters;

		private int _index = 0;

		private EventWriter _out;

		public StackedRewriter(EventRewriter[] rewriters) {
			_rewriters = rewriters;
		}

		@Override
		public void write(ChangeSet cs) {
			if (_index == _rewriters.length) {
				_out.write(cs);
			} else {
				EventRewriter currentRewriter = _rewriters[_index];
				_index++;
				currentRewriter.rewrite(cs, this);
			}
		}

		@Override
		public void flush() {
			// Nothing cached, nothing to flush
			assert false : "No one has to use EventWriter API externally";
		}

		@Override
		public void close() {
			// Nothing special to release
			assert false : "No one has to use EventWriter API externally";
		}

		@Override
		public synchronized void rewrite(ChangeSet cs, EventWriter out) {
			_out = out;
			try {
				write(cs);
			} finally {
				_index = 0;
				_out = null;
			}
		}

	}

	/**
	 * Creates an {@link EventRewriter} which rewrites the {@link ChangeSet} by first rewriting
	 * using the first {@link EventRewriter}, then the second, and so on.
	 * 
	 * @param rewriters
	 *        The {@link EventRewriter} to use as inner {@link EventRewriter},
	 */
	public static EventRewriter getRewriter(List<? extends EventRewriter> rewriters) {
		switch (rewriters.size()) {
			case 0:
				return SimpleEventRewriter.INSTANCE;
			case 1:
				return rewriters.get(0);
			default:
				return new StackedRewriter(rewriters.toArray(new EventRewriter[rewriters.size()]));
		}
	}

	/**
	 * Creates an {@link EventRewriter} which rewrites the {@link ChangeSet} by first rewriting
	 * using the first {@link EventRewriter}, then the second, and so on.
	 * 
	 * @param rewriters
	 *        The {@link EventRewriter} to use as inner {@link EventRewriter},
	 */
	public static EventRewriter getRewriter(EventRewriter... rewriters) {
		switch (rewriters.length) {
			case 0:
				return SimpleEventRewriter.getInstance();
			case 1:
				return rewriters[0];
			default:
				return new StackedRewriter(rewriters);
		}
	}

}

