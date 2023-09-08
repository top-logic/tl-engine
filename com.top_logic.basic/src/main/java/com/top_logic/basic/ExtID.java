/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Representation of an external ID.
 * 
 * <p>
 * The ID consists of two identifiers, first, the identifier of the external system and, second, the
 * internal identifier of the object within the external system.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ExtID implements Comparable<ExtID> {

	private final long _systemId;

	private final long _objectId;

	/**
	 * Creates a new {@link ExtID}.
	 * 
	 * @param systemId
	 *        See {@link #systemId()}.
	 * @param objectId
	 *        See {@link #objectId()}.
	 */
	public ExtID(long systemId, long objectId) {
		_systemId = systemId;
		_objectId = objectId;
	}

	/**
	 * The identifier of the external system. It must be unique within the network using
	 *         {@link ExtID}.
	 */
	public long systemId() {
		return _systemId;
	}

	/**
	 * The identifier of an object within the external system. It must be unique within the
	 *         external system with identifier {@link #systemId()}.
	 */
	public long objectId() {
		return _objectId;
	}

	@Override
	public int hashCode() {
		final int prime = 1566871;
		int result = 68741;
		result = prime * result + (int) (_objectId ^ (_objectId >>> 32));
		result = prime * result + (int) (_systemId ^ (_systemId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		ExtID other = (ExtID) obj;
		if (_objectId != other._objectId)
			return false;
		if (_systemId != other._systemId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(32);
		b.append("(sysId:");
		b.append(systemId());
		b.append(",objId:");
		b.append(objectId());
		b.append(')');
		return b.toString();
	}

	@Override
	public int compareTo(ExtID o) {
		int objectCompare = Long.compare(_objectId, o._objectId);
		if (objectCompare != 0) {
			return objectCompare;
		}
		return Long.compare(_systemId, o._systemId);
	}

}

