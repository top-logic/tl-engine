/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.update;

import static java.util.Objects.*;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.json.JsonSerializable;
import com.top_logic.common.remote.shared.ObjectData;
import com.top_logic.common.remote.shared.ObjectScope;

/**
 * Base class for a change in an {@link ObjectScope}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Change implements JsonSerializable {

	private final String _id;

	/**
	 * Creates a {@link Change}.
	 *
	 * @param id
	 *        See {@link #getId()}.
	 */
	public Change(String id) {
		_id = requireNonNull(id);
	}

	/**
	 * The object ID of the affected object.
	 * 
	 * @see ObjectData#id()
	 */
	public String getId() {
		return _id;
	}

	@Override
	public void writeTo(JsonWriter writer) throws IOException {
		writer.beginObject();
		serializeContent(writer);
		writer.endObject();
	}

	/**
	 * Implementation of {@link #writeTo(JsonWriter)}.
	 * 
	 * <p>
	 * Writes the contents of the JSON object literal.
	 * </p>
	 */
	protected void serializeContent(JsonWriter writer) throws IOException {
		writer.name("id");
		writer.value(getId());
	}

}
