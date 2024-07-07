/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.connector.ConnectorType;
import com.top_logic.graphic.blocks.model.connector.ConnectorTypes;
import com.top_logic.graphic.blocks.model.content.BlockContentType;
import com.top_logic.graphic.blocks.model.content.mouth.MouthType;
import com.top_logic.graphic.blocks.model.content.row.part.RowPartType;
import com.top_logic.graphic.blocks.model.factory.BlockFactory;

/**
 * Definition of block types to use.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockSchema implements JsonSerializable<Void> {

	private Map<String, ConnectorType> _connectorTypes = new HashMap<>();

	private Map<String, Supplier<RowPartType>> _contentTypes = new HashMap<>();

	private Map<String, BlockType> _blockTypes = new HashMap<>();

	/**
	 * The {@link ConnectorType} with the given well-known name.
	 * 
	 * @see ConnectorTypes
	 */
	public ConnectorType getConnectorType(String name) {
		ConnectorType result = _connectorTypes.get(name);
		if (result == null) {
			throw new IllegalArgumentException("No such connector type '" + name + "'.");
		}
		return result;
	}

	/**
	 * Defines a new/custom {@link ConnectorType}.
	 */
	public BlockSchema defineConnectorType(ConnectorType type) {
		_connectorTypes.put(type.getName(), type);
		return this;
	}

	/**
	 * The {@link RowPartType} {@link Supplier} with the given well-known name.
	 */
	public Supplier<RowPartType> getRowPartType(String kind) {
		Supplier<RowPartType> result = _contentTypes.get(kind);
		if (result == null) {
			throw new IllegalArgumentException("No such content type '" + kind + "'.");
		}
		return result;
	}

	/**
	 * Defines a new/custom {@link RowPartType}.
	 * 
	 * @see #getRowPartType(String)
	 */
	public BlockSchema defineRowPartType(String kind, Supplier<RowPartType> type) {
		_contentTypes.put(kind, type);
		return this;
	}

	/**
	 * The {@link BlockType} with the given ID.
	 */
	public BlockType getBlockType(String typeId) {
		BlockType result = _blockTypes.get(typeId);
		if (result == null) {
			throw new IllegalArgumentException("No such block type '" + typeId + "'.");
		}
		return result;
	}

	private void defineBlockType(BlockType type) {
		_blockTypes.put(type.getId(), type);
	}

	/**
	 * Defines a new {@link BlockType} with the given properties.
	 * 
	 * @param id
	 *        See {@link BlockType#getId()}.
	 * @param cssClass
	 *        See {@link BlockType#getCssClass()}.
	 * @param topType
	 *        See {@link BlockType#getTopType()}, {@link #getConnectorType(String)}.
	 * @param bottomType
	 *        See {@link BlockType#getBottomType()}, {@link #getConnectorType(String)}.
	 */
	public void defineBlockType(String id, String cssClass, String topType, String bottomType,
			BlockContentType... partTypes) {
		defineBlockType(
			BlockFactory.blockType(
				id, cssClass, getConnectorType(topType), getConnectorType(bottomType), partTypes));
	}

	/**
	 * Creates a new {@link Block} with the {@link BlockType} with the given ID.
	 * 
	 * @see #defineBlockType(String, String, String, String, BlockContentType...)
	 */
	public Block block(String typeId) {
		return new Block(getBlockType(typeId));
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("types");
		JsonSerializable.writeArray(json, _blockTypes.values());
	}

	@Override
	public void readPropertyFrom(Void context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "types":
				json.beginArray();
				while (json.hasNext()) {
					defineBlockType(BlockType.read(this, json));
				}
				json.endArray();
				break;

			default:
				JsonSerializable.super.readPropertyFrom(context, json, name);
		}
	}

	/**
	 * Creates a new {@link MouthType} with the given properties.
	 */
	public BlockContentType mouthType(String topType, String bottomType) {
		return BlockFactory.mouthType(getConnectorType(topType), getConnectorType(bottomType));
	}

	/**
	 * Reads a complete {@link BlockSchema} from the given {@link JsonReader}.
	 */
	public static BlockSchema read(JsonReader json) throws IOException {
		BlockSchema schemaCopy = BlockFactory.createSchema();
		schemaCopy.readFrom(null, json);
		return schemaCopy;
	}

}
