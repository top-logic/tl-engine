/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;

/**
 * Internal object identifier.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class LongID extends TLID {

	private final long _id;

	private LongID(long id) {
		_id = id;
	}

	@Override
	public int hashCode() {
		return CollectionUtil.hashCodeLong(_id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LongID)) {
			return false;
		}

		return _id == ((LongID) obj)._id;
	}

	@Override
	public int compareTo(TLID o) {
		return CollectionUtil.compareLong(_id, ((LongID) o)._id);
	}

	@Override
	public Object toStorageValue() {
		return longValue();
	}

	@Override
	public String toExternalForm() {
		return Long.toString(longValue());
	}

	@Override
	public void appendExternalForm(Appendable out) throws IOException {
		StringServices.append(out, longValue());
	}

	/**
	 * This identifier as primitive value.
	 */
	public long longValue() {
		return _id;
	}

	/**
	 * Creates a new {@link Long} from the given primitive value.
	 */
	public static TLID valueOf(long id) {
		if (id == 0) {
			return null;
		}
		assert id > 0;
		return mkId(id);
	}

	static TLID mkId(long id) {
		return new LongID(id);
	}

	/**
	 * Parses a representation created with {@link #toExternalForm()}.
	 */
	public static TLID fromExternalForm(String externalId) {
		if (StringServices.isEmpty(externalId)) {
			return null;
		}
		return valueOf(Long.parseLong(externalId));
	}

}
