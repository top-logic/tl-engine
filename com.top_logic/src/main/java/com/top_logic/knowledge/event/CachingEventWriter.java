/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link EventWriter} writing all {@link ChangeSet}s to a cache.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CachingEventWriter implements EventWriter {

	/** all formerly written {@link ChangeSet}. */
	private final List<ChangeSet> _changeSets = new ArrayList<>();

	/** index of the first event in the cache added after the last flush */
	private int flushIndex = 0;

	/** whether the writer was closed */
	private boolean closed;

	/**
	 * Writes the given {@link ChangeSet}.
	 * 
	 * @throws IllegalStateException
	 *         iff the writer was closed before
	 * 
	 * @see EventWriter#write(ChangeSet)
	 */
	@Override
	public void write(ChangeSet cs) {
		checkOpen();
		_changeSets.add(cs);
	}

	/**
	 * Check that the writes was not closed.
	 * 
	 * @throws IllegalStateException
	 *         iff the writer was closed before
	 */
	private void checkOpen() {
		if (this.closed) {
			throw new IllegalStateException("EventWriter closed");
		}
	}

	/**
	 * Returns all events written to this writer in the written order, includes also events which
	 * were not flushed yet.
	 */
	public List<ChangeSet> getAllEvents() {
		return _changeSets;
	}

	/**
	 * Returns the events currently flushed
	 */
	public List<ChangeSet> getEvents() {
		return _changeSets.subList(0, flushIndex);
	}

	@Override
	public void flush() {
		checkOpen();
		flushIndex = _changeSets.size();
	}

	/**
	 * flushes and closes this {@link EventWriter}
	 * 
	 * @see com.top_logic.knowledge.event.EventWriter#close()
	 */
	@Override
	public void close() {
		if (closed) {
			return;
		}
		flush();
		closed = true;
	}

}
