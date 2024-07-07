/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;

/**
 * Description of a {@link RowPart}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class RowPartType implements JsonSerializable<BlockSchema> {

	private final String _kind;

	/**
	 * Creates a {@link RowPartType}.
	 * 
	 * @param kind
	 *        See {@link #getKind()}.
	 */
	public RowPartType(String kind) {
		_kind = kind;
	}

	/**
	 * The well-known name of this {@link RowPartType}.
	 */
	public String getKind() {
		return _kind;
	}

	/**
	 * Instantiates a {@link RowPart} corresponding to this {@link RowPartType}.
	 */
	public abstract RowPart newInstance(BlockRow owner);

	@Override
	public void writeTo(JsonWriter json) throws IOException {
		json.beginArray();
		json.value(getKind());
		JsonSerializable.super.writeTo(json);
		json.endArray();
	}

	/**
	 * Reads a polymorphic {@link RowPartType} from the given {@link JsonReader}.
	 */
	public static RowPartType read(BlockSchema blockSchema, JsonReader json) throws IOException {
		RowPartType result;
		json.beginArray();
		{
			String kind = json.nextString();

			result = blockSchema.getRowPartType(kind).get();
			result.readFrom(blockSchema, json);
		}
		json.endArray();
		return result;
	}
}
