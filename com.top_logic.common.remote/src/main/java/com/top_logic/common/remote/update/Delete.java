/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.update;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;

/**
 * An object deletion {@link Change}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Delete extends Change {

	/**
	 * Creates a {@link Delete}.
	 *
	 * @param id
	 *        See {@link #getId()}.
	 */
	public Delete(String id) {
		super(id);
	}

	static Delete readDelete(JsonReader reader) throws IOException {
		String id = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String property = reader.nextName();
			switch (property) {
				case "id":
					id = reader.nextString();
					break;
				default:
					throw new IllegalArgumentException("Unexpected property: " + property);
			}
		}
		reader.endObject();
		return new Delete(id);
	}

	@Override
	public String toString() {
		return "Delete(id = " + getId() + ")";
	}

}
