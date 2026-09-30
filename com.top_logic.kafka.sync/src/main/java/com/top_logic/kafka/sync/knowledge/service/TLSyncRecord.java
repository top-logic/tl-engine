/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.service.Revision;

/**
 * Record send by an external TL system.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLSyncRecord<T> {

	/**
	 * The value of {@link #getLastMessageRevision()}, when no revision number has been <em>sent</em>, yet.
	 * <p>
	 * This is the case either when the system was just started for the first time, or when it
	 * previously used protocol version 1.0.0.
	 * </p>
	 */
	public static final long LAST_MESSAGE_REVISION_NONE_SEND = -1;

	/**
	 * The value of {@link #getLastMessageRevision()}, when no revision number has been <em>received</em>,
	 * yet.
	 * <p>
	 * This is the case either when the system was just started for the first time, or when it
	 * previously used protocol version 1.0.0.
	 * </p>
	 */
	public static final long LAST_MESSAGE_REVISION_NONE_RECEIVED = -2;

	private final long _systemId;

	private final long _lastMessageRevision;

	private final T _record;

	/**
	 * Creates a new {@link TLSyncRecord}.
	 * 
	 * @param systemId
	 *        See {@link #getSystemId()}.
	 * @param record
	 *        See {@link #getRecord()}.
	 */
	public TLSyncRecord(long systemId, long lastMessageRevision, T record) {
		_systemId = systemId;
		_lastMessageRevision = lastMessageRevision;
		if (record == null) {
			throw new NullPointerException("'record' must not be 'null'.");
		}
		_record = record;
	}

	/**
	 * The identifier of the external system
	 */
	public long getSystemId() {
		return _systemId;
	}

	/**
	 * The {@link Revision} number of the last {@link ChangeSet} which caused a message to be sent.
	 * <p>
	 * That means, it is not incremented for a {@link ChangeSet} which is being filtered out as it
	 * contains no relevant data.
	 * </p>
	 * <p>
	 * See {@link #LAST_MESSAGE_REVISION_NONE_SEND} and {@link #LAST_MESSAGE_REVISION_NONE_RECEIVED}
	 * for special values.
	 * </p>
	 */
	public long getLastMessageRevision() {
		return _lastMessageRevision;
	}

	/**
	 * The record sent by the external system.
	 */
	public T getRecord() {
		return _record;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _record.hashCode();
		result = prime * result + (int) (_systemId ^ (_systemId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TLSyncRecord<?> other = (TLSyncRecord<?>) obj;
		if (!_record.equals(other._record))
			return false;
		if (_systemId != other._systemId)
			return false;
		return true;
	}

}

