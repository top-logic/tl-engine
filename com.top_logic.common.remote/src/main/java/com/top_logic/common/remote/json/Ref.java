/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.json;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.shared.ObjectData;
import com.top_logic.common.remote.shared.ObjectScope;

/**
 * The serialized form of an object reference.
 * 
 * @see #id()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Ref implements JsonSerializable {

	private String _id;

	/**
	 * Creates a {@link Ref}.
	 *
	 * @param id
	 *        See {@link #id()}.
	 */
	public Ref(String id) {
		_id = id;
	}

	/**
	 * The object ID of the referenced object as assigned by its {@link ObjectScope}.
	 * 
	 * @see ObjectData#id()
	 */
	public String id() {
		return _id;
	}

	@Override
	public void writeTo(JsonWriter writer) throws IOException {
		writer.beginObject();
		writer.name("id");
		writer.value(id());
		writer.endObject();
	}

	/**
	 * Reads a {@link Ref} object from the given reader.
	 * 
	 * @param reader
	 *        The reader to read from.
	 * @return The read {@link Ref}.
	 * @throws IOException
	 *         If reading fails.
	 */
	public static Object readRef(JsonReader reader) throws IOException {
		reader.beginObject();
		String id = null;
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
		return new Ref(id);
	}

}
