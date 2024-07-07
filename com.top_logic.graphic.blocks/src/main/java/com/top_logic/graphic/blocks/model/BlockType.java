/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.Connector;
import com.top_logic.graphic.blocks.model.connector.ConnectorType;
import com.top_logic.graphic.blocks.model.content.BlockContentType;

/**
 * Description of the structure of a {@link Block}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockType implements JsonSerializable<BlockSchema> {

	private ConnectorType _topType;

	private ConnectorType _bottomType;

	private final List<BlockContentType> _parts = new ArrayList<>();

	private String _cssClass;

	private String _id;

	/**
	 * Creates a {@link BlockType}.
	 */
	public BlockType() {
		super();
	}

	/**
	 * The ID for this {@link BlockType}.
	 * 
	 * @see BlockSchema#defineBlockType(String, String, String, String, BlockContentType...)
	 * @see BlockSchema#block(String)
	 */
	public String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public void setId(String value) {
		_id = value;
	}

	/**
	 * The CSS class to use for {@link Block}s using this {@link BlockType}.
	 */
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * @see #getCssClass()
	 */
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * The {@link BlockContentType}s defining the {@link Block#getParts()} of {@link Block}s created
	 * from this {@link BlockType}.
	 */
	public List<BlockContentType> getPartTypes() {
		return _parts;
	}

	/**
	 * @see #getPartTypes()
	 */
	public void addPartType(BlockContentType type) {
		_parts.add(type);
	}

	/**
	 * {@link ConnectorType} of the top {@link Connector}s of a created {@link Block}.
	 */
	public ConnectorType getTopType() {
		return _topType;
	}

	/**
	 * @see #getTopType()
	 */
	public void setTopType(ConnectorType topType) {
		_topType = topType;
	}

	/**
	 * {@link ConnectorType} of the top {@link Connector}s of a created {@link Block}.
	 */
	public ConnectorType getBottomType() {
		return _bottomType;
	}

	/**
	 * @see #getBottomType()
	 */
	public void setBottomType(ConnectorType bottomType) {
		_bottomType = bottomType;
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("id");
		json.value(getId());
		json.name("cssClass");
		json.value(getCssClass());
		json.name("topType");
		getTopType().writeTo(json);
		json.name("bottomType");
		getBottomType().writeTo(json);
		json.name("parts");
		JsonSerializable.writeArray(json, getPartTypes());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "id":
				setId(json.nextString());
				break;
			case "cssClass":
				setCssClass(json.nextString());
				break;
			case "topType":
				setTopType(ConnectorType.read(context, json));
				break;
			case "bottomType":
				setBottomType(ConnectorType.read(context, json));
				break;
			case "parts":
				json.beginArray();
				while (json.hasNext()) {
					addPartType(BlockContentType.read(context, json));
				}
				json.endArray();
				break;

			default:
				JsonSerializable.super.readPropertyFrom(context, json, name);
		}
	}

	/**
	 * Reads a {@link BlockType} from the given {@link JsonReader}.
	 */
	public static BlockType read(BlockSchema context, JsonReader json) throws IOException {
		BlockType result = new BlockType();
		result.readFrom(context, json);
		return result;
	}

}
