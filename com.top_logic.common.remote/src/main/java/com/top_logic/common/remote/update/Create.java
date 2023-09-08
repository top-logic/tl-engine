/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.update;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * An object creation {@link Change}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Create extends Change {

	private final String _networkType;

	/**
	 * Creates a {@link Create}.
	 *
	 * @param id
	 *        See {@link #getId()}.
	 * @param networkType
	 *        See {@link #getNetworkType()}.
	 */
	public Create(String id, String networkType) {
		super(id);
		_networkType = networkType;
	}

	/**
	 * The (network) type name of the newly created object.
	 */
	public String getNetworkType() {
		return _networkType;
	}

	@Override
	protected void serializeContent(JsonWriter writer) throws IOException {
		super.serializeContent(writer);
		writer.name("type");
		writer.value(getNetworkType());
	}

	static Create readCreate(JsonReader reader) throws IOException {
		String id = null;
		String type = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String property = reader.nextName();
			switch (property) {
				case "id":
					id = reader.nextString();
					break;
				case "type":
					type = reader.nextString();
					break;
				default:
					throw new IllegalArgumentException("Unexpected property: " + property);
			}
		}
		reader.endObject();
		return new Create(id, type);
	}

	@Override
	public String toString() {
		return "Create(id = " + getId() + "; type = " + getNetworkType() + ")";
	}

}
