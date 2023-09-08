/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.IOException;
import java.rmi.server.UID;

/**
 * String based {@link TLID}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class StringID extends TLID {

	private String _id;

	private StringID(String id) {
		assert id != null && !id.isEmpty();

		_id = id;
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof StringID)) {
			return false;
		}

		return _id.equals(((StringID) obj)._id);
	}

	@Override
	public int compareTo(TLID o) {
		return _id.compareTo(((StringID) o)._id);
	}

	@Override
	public Object toStorageValue() {
		return stringValue();
	}

	@Override
	public String toExternalForm() {
		return stringValue();
	}

	@Override
	public void appendExternalForm(Appendable out) throws IOException {
		out.append(stringValue());
	}

	/**
	 * This identifier as Java built-in value.
	 */
	public String stringValue() {
		return _id;
	}

	/**
	 * Parses a representation created with {@link #toExternalForm()}.
	 */
	public static TLID fromExternalForm(String externalId) {
		if (StringServices.isEmpty(externalId)) {
			return null;
		}
		return mkId(externalId);
	}

	/**
	 * Creates a new identifier form the given Java built-in value.
	 */
	public static TLID valueOf(String id) {
		if (id == null || id.length() == 0) {
			return null;
		}
		return mkId(id);
	}

	/**
	 * Creates a new random identifier.
	 */
	public static TLID createRandomID() {
		return mkId(new UID().toString());
	}

	private static TLID mkId(String id) {
		return new StringID(id);
	}

}