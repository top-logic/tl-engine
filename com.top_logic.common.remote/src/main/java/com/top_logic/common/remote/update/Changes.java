/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.json.JsonSerializable;
import com.top_logic.common.remote.shared.ObjectScope;

/**
 * Collected {@link Change}s in an {@link ObjectScope} to be transported over the network.
 * 
 * @see #getCreates()
 * @see #getDeletes()
 * @see #getUpdates()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Changes implements JsonSerializable {

	private List<Create> _creates = new ArrayList<>();

	private List<Delete> _deletes = new ArrayList<>();

	private List<Update> _updates = new ArrayList<>();

	/**
	 * Object creations.
	 */
	public List<Create> getCreates() {
		return _creates;
	}

	/**
	 * Object deletions.
	 */
	public List<Delete> getDeletes() {
		return _deletes;
	}

	/**
	 * Object modifications.
	 */
	public List<Update> getUpdates() {
		return _updates;
	}

	@Override
	public void writeTo(JsonWriter writer) throws IOException {
		writer.beginObject();
		serializeList(writer, "creates", getCreates());
		serializeList(writer, "updates", getUpdates());
		serializeList(writer, "deletes", getDeletes());
		writer.endObject();
	}

	/**
	 * Reads {@link Changes} from the given reader.
	 * 
	 * @param reader
	 *        The JSON reader to read from.
	 * @return The read {@link Changes} instance.
	 * @throws IOException
	 *         If reading fails.
	 */
	public static Changes loadChanges(JsonReader reader) throws IOException {
		Changes changes = new Changes();
		reader.beginObject();
		while (reader.hasNext()) {
			String property = reader.nextName();
			switch (property) {
				case "creates":
					reader.beginArray();
					while (reader.hasNext()) {
						changes.getCreates().add(Create.readCreate(reader));
					}
					reader.endArray();
					break;
				case "deletes":
					reader.beginArray();
					while (reader.hasNext()) {
						changes.getDeletes().add(Delete.readDelete(reader));
					}
					reader.endArray();
					break;
				case "updates":
					reader.beginArray();
					while (reader.hasNext()) {
						changes.getUpdates().add(Update.readUpdate(reader));
					}
					reader.endArray();
					break;
				default:
					throw new IllegalArgumentException("Unexpected property: " + property);
			}
		}
		reader.endObject();
		return changes;
	}

	private static void serializeList(JsonWriter writer, String property, List<? extends JsonSerializable> creates)
			throws IOException {
		writer.name(property);
		writer.beginArray();
		for (JsonSerializable create : creates) {
			create.writeTo(writer);
		}
		writer.endArray();
	}

	@Override
	public String toString() {
		return "Changes("
			+ "creates = " + getCreates() + "; "
			+ "updates = " + getUpdates() + "; "
			+ "deletes = " + getDeletes() + ")";
	}

}
