/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.content;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.content.mouth.Mouth;
import com.top_logic.graphic.blocks.model.content.mouth.MouthType;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;
import com.top_logic.graphic.blocks.model.content.row.BlockRowType;

/**
 * Description of a {@link BlockContent}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class BlockContentType implements JsonSerializable<BlockSchema> {

	/**
	 * A {@link BlockRow}.
	 */
	protected static final String ROW_KIND = "row";

	/**
	 * A {@link Mouth}.
	 */
	protected static final String MOUTH_KIND = "mouth";

	/**
	 * Instantiates this {@link BlockContentType} to a {@link BlockContent}.
	 */
	public abstract BlockContent newInstance(Block owner);

	@Override
	public void writeTo(JsonWriter json) throws IOException {
		json.beginArray();
		json.value(kind());
		JsonSerializable.super.writeTo(json);
		json.endArray();
	}

	/**
	 * The well-known name of this {@link BlockContentType} for serialization to JSON.
	 */
	protected abstract String kind();

	/**
	 * Reads a polymorphic {@link BlockContentType} from the given {@link JsonReader}.
	 */
	public static BlockContentType read(BlockSchema context, JsonReader json) throws IOException {
		BlockContentType result;

		json.beginArray();
		String kind = json.nextString();
		{
			switch (kind) {
				case BlockContentType.ROW_KIND:
					result = new BlockRowType();
					break;
				case BlockContentType.MOUTH_KIND:
					result = new MouthType();
					break;
				default:
					throw new IllegalArgumentException("No such part type '" + kind + "'.");
			}
			result.readFrom(context, json);
		}
		json.endArray();
		return result;
	}

}
