/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Iterator;


/**
 * The {@link CumulatedEventReader} is a holder for some inner
 * {@link EventReader} and reads successively the events of them, i.e. first it
 * calls {@link EventReader#readEvent()} of the first inner reader until it
 * returns <code>null</code>, then it finishes the second one, and so on.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CumulatedEventReader<E> extends AbstractEventReader<E> {

	// storage of the reader to process
	private final Iterator<? extends EventReader<? extends E>> readers;
	// the current reader to finish
	private EventReader<? extends E> currentReader;

	public CumulatedEventReader(Iterable<? extends EventReader<? extends E>> readers) {
		this.readers = readers.iterator();
		if (this.readers.hasNext()) {
			this.currentReader = this.readers.next();
		}
	}

	@Override
	public E readEvent() {
		if (currentReader == null) {
			return null;
		}
		E readEvent = currentReader.readEvent();
		if (readEvent != null) {
			return readEvent;
		} else {
			currentReader.close();
			if (readers.hasNext()) {
				currentReader = readers.next();
				return readEvent();
			} else {
				currentReader = null;
				return null;
			}
		}
	}

	@Override
	public void close() {
		if (currentReader != null) {
			currentReader.close();

			while (readers.hasNext()) {
				readers.next().close();
			}
		}
	}

}
