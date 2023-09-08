/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;


/**
 * Reader of {@link ChangeSet}s.
 * 
 * <p>
 * Must be {@link ChangeSetReader#close() closed} after usage.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ChangeSetReader extends AutoCloseable {

	/**
	 * Reads the next {@link ChangeSet} from this reader.
	 * 
	 * @return The next read {@link ChangeSet}, or <code>null</code>, if no more {@link ChangeSet}s
	 *         are available.
	 */
	ChangeSet read();

	/**
	 * Closes this {@link ChangeSetReader} and releases resources allocated during construction.
	 */
	@Override
	void close();
}

