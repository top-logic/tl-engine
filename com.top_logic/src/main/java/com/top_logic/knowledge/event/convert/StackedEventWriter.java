/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Collection;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * The class {@link StackedEventWriter} is an {@link EventWriter} which applies the event to a
 * given rewriting algorithm and {@link EventWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class StackedEventWriter implements EventWriter {

	/**
	 * The {@link EventWriter} to delegate to the {@link EventRewriter}
	 */
	private final EventWriter _out;

	private final EventRewriter _rewriter;

	/**
	 * Creates a new {@link StackedEventWriter}
	 * 
	 * @param rewriter
	 *        the {@link EventRewriter} to delegate to.
	 * @param out
	 *        the {@link EventWriter} argument of
	 *        {@link EventRewriter#rewrite(ChangeSet, EventWriter)} of the given
	 *        {@link EventRewriter}
	 */
	protected StackedEventWriter(EventRewriter rewriter, EventWriter out) {
		if (out == null) {
			throw new NullPointerException("The given output must not be null");
		}
		if (rewriter == null) {
			throw new NullPointerException("The given rewriter must not be null");
		}
		this._out = out;
		this._rewriter = rewriter;
	}

	@Override
	public void write(ChangeSet cs) {
		_rewriter.rewrite(cs, _out);
	}

	@Override
	public void flush() {
		_out.flush();
	}

	@Override
	public void close() {
		_out.close();
	}

	/**
	 * Creates an {@link EventWriter} which applies the given {@link EventRewriter} in the given
	 * order, i.e. if the writer is used with some event, the first {@link EventRewriter} is called.
	 * The events produced by that {@link EventRewriter} is given to the second and so on. At the
	 * end, all written events are written to the given {@link EventWriter}.
	 * 
	 * @param startIndex
	 *        the index of the first {@link EventRewriter} in the given array which must be
	 *        consulted
	 * @param out
	 *        the final {@link EventWriter} to which in the end all events are written
	 * @param eventRewriters
	 *        the {@link EventRewriter}s to consult in the given order
	 */
	public static EventWriter createWriter(int startIndex, EventWriter out, EventRewriter... eventRewriters) {
		// check for correct arguments
		if (eventRewriters == null) {
			throw new NullPointerException("eventRewriters must not be null");
		}
		if (startIndex < 0 || startIndex > eventRewriters.length) {
			throw new IndexOutOfBoundsException("startindex '" + startIndex
				+ "' must not be less than 0 or larger than array size '" + eventRewriters.length + "'");
		}

		return internalCreate(startIndex, out, eventRewriters);
	}

	/**
	 * Creates an {@link EventWriter} which applies the given {@link EventRewriter} in the given
	 * order, i.e. if the writer is used with some event, the first {@link EventRewriter} is called.
	 * The events produced by that {@link EventRewriter} is given to the second and so on. At the
	 * end, all written events are written to the given {@link EventWriter}.
	 * 
	 * @param startIndex
	 *        the index of the first {@link EventRewriter} in the given array which must be
	 *        consulted
	 * @param out
	 *        the final {@link EventWriter} to which in the end all events are written
	 * @param eventRewriters
	 *        the {@link EventRewriter}s to consult in the given order
	 */
	public static EventWriter createWriter(int startIndex, EventWriter out,
			Collection<? extends EventRewriter> eventRewriters) {
		// check for correct arguments
		if (eventRewriters == null) {
			throw new NullPointerException("eventRewriters must not be null");
		}
		final int numberRewriters = eventRewriters.size();
		if (startIndex < 0 || startIndex > numberRewriters) {
			throw new IndexOutOfBoundsException("startindex '" + startIndex
				+ "' must not be less than 0 or larger than array size '" + numberRewriters + "'");
		}

		final EventRewriter[] rewriters = new EventRewriter[numberRewriters];

		return internalCreate(startIndex, out, eventRewriters.toArray(rewriters));
	}

	/**
	 * Does the actual job of {@link #createWriter(int, EventWriter, EventRewriter...)}
	 */
	private static EventWriter internalCreate(int startIndex, EventWriter out, EventRewriter... eventRewriters) {
		if (startIndex == eventRewriters.length) {
			return out;
		} else {
			final EventWriter _stackedOut = internalCreate(startIndex + 1, out, eventRewriters);
			return new StackedEventWriter(eventRewriters[startIndex], _stackedOut);
		}
	}

	/**
	 * Creates an {@link EventWriter} which writes the event by rewriting it using the given
	 * {@link EventRewriter} to the given {@link EventWriter}.
	 * 
	 * @param rewriter
	 *        the {@link EventRewriter} to call
	 * @param out
	 *        the {@link EventWriter} which finally writes the event
	 */
	public static EventWriter createWriter(EventRewriter rewriter, EventWriter out) {
		return new StackedEventWriter(rewriter, out);
	}
}
