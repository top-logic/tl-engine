/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * {@link ChangeSetReader} that uses the events from an {@link EventReader} to create
 * {@link ChangeSet}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultChangeSetReader implements ChangeSetReader {

	private final EventReader<? extends KnowledgeEvent> _eventReader;

	private KnowledgeEvent _nextEvent;

	/**
	 * Creates a new {@link DefaultChangeSetReader}.
	 * 
	 * @param eventReader
	 *        {@link EventReader} to get events to build {@link ChangeSet} from.
	 */
	public DefaultChangeSetReader(EventReader<? extends KnowledgeEvent> eventReader) {
		_eventReader = eventReader;
	}

	@Override
	public ChangeSet read() {
		if (_nextEvent == null) {
			_nextEvent = _eventReader.readEvent();
		}
		if (_nextEvent == null) {
			return null;
		}
		long revision = _nextEvent.getRevision();
		ChangeSet result = new ChangeSet(revision);
		do {
			result.add(_nextEvent);
			_nextEvent = _eventReader.readEvent();
			if (_nextEvent == null) {
				break;
			}
		} while (_nextEvent.getRevision() == revision);
		return result;
	}

	@Override
	public void close() {
		_eventReader.close();
	}

}

