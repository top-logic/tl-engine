/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.mouth;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.connector.ConnectorType;
import com.top_logic.graphic.blocks.model.content.BlockContent;
import com.top_logic.graphic.blocks.model.content.BlockContentType;

/**
 * Description of a {@link Mouth}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MouthType extends BlockContentType {

	private ConnectorType _topType;

	private ConnectorType _bottomType;

	/**
	 * Creates a {@link MouthType}.
	 *
	 */
	public MouthType() {
		super();
	}

	@Override
	protected String kind() {
		return BlockContentType.MOUTH_KIND;
	}

	@Override
	public BlockContent newInstance(Block owner) {
		return new Mouth(owner, this);
	}

	/**
	 * {@link ConnectorType} of the top connector.
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
	 * {@link ConnectorType} of the bottom connector.
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
		json.name("topType");
		getTopType().writeTo(json);
		json.name("bottomType");
		getBottomType().writeTo(json);
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "topType":
				setTopType(ConnectorType.read(context, json));
				break;
			case "bottomType":
				setBottomType(ConnectorType.read(context, json));
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}

}