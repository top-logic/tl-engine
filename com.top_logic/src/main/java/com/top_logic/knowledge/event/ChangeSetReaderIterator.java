/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Iterator;

import com.top_logic.basic.col.CloseableIteratorBase;

/**
 * {@link Iterator} build from a {@link ChangeSetReader}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetReaderIterator extends CloseableIteratorBase<ChangeSet> {

	private final ChangeSetReader _reader;

	/**
	 * Creates a new {@link ChangeSetReaderIterator} based on the given reader.
	 * 
	 * @param reader
	 *        The {@link ChangeSetReader} to produce the {@link #next() next change set}.
	 */
	public ChangeSetReaderIterator(ChangeSetReader reader) {
		_reader = reader;
	}

	@Override
	protected void internalClose() {
		_reader.close();
	}

	@Override
	protected boolean findNext() {
		ChangeSet cs = _reader.read();
		if (cs == null) {
			return false;
		} else {
			setNext(cs);
			return true;
		}
	}

}

