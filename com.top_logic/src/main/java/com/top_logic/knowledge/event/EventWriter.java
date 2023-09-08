/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * Sink of {@link ChangeSet}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EventWriter extends AutoCloseable {
	
	/**
	 * Writes the given {@link ChangeSet} to this {@link EventWriter}.
	 * 
	 * @param cs
	 *        The {@link ChangeSet} to write.
	 */
	void write(ChangeSet cs);

	/**
	 * Makes sure that all {@link ChangeSet} have been processed.
	 */
	void flush();

	/**
	 * Finishes the write operation.
	 * 
	 * <p>
	 * Subsumes {@link #flush()}.
	 * </p>
	 */
	@Override
	void close();
}
