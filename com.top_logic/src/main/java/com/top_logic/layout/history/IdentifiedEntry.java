/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;

/**
 * {@link IdentifiedEntry} is a {@link HistoryEntry} which wraps another
 * {@link HistoryEntry} and has an identifier.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class IdentifiedEntry implements HistoryEntry {

	private final HistoryEntry impl;
	private final String identifier;
	private int stackDepth;
	private boolean endsStack, startsStack;

	/**
	 * Previous resp. next entry in the history queue. Will be updated in
	 * {@link HistoryQueue} if some structural changes occurs (adding new entry,
	 * remove entries).
	 */
	IdentifiedEntry previous, next;

	IdentifiedEntry(String id, HistoryEntry impl) {
		this.identifier = id;
		this.impl = impl;
	}

	public String getID() {
		return identifier;
	}

	@Override
	public <T extends Appendable> T appendTitle(DisplayContext context, T out) throws IOException {
		return impl.appendTitle(context, out);
	}

	@Override
	public void redo(DisplayContext context) throws HistoryException {
		impl.redo(context);
	}

	@Override
	public void undo(DisplayContext context) throws HistoryException {
		impl.undo(context);
	}

	IdentifiedEntry setStackDepth(int stackDepth) {
		this.stackDepth = stackDepth;
		return this;
	}

	int getStackDepth() {
		return stackDepth;
	}

	void setEndsStack(boolean endsStack) {
		this.endsStack = endsStack;
	}

	boolean endsStack() {
		return endsStack;
	}

	void setStartsStack(boolean startsStack) {
		this.startsStack = startsStack;
	}

	boolean startsStack() {
		return startsStack;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IdentifiedEntry [impl=");
		builder.append(impl);
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", stackDepth=");
		builder.append(stackDepth);
		builder.append(", endsStack=");
		builder.append(endsStack);
		builder.append(", startsStack=");
		builder.append(startsStack);
		builder.append("]");
		return builder.toString();
	}

}
