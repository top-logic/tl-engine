/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.content.row;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.content.BlockContent;
import com.top_logic.graphic.blocks.model.content.BlockContentType;
import com.top_logic.graphic.blocks.model.content.row.part.RowPart;
import com.top_logic.graphic.blocks.model.content.row.part.RowPartType;

/**
 * Description of a {@link BlockRow}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockRowType extends BlockContentType {

	private List<RowPartType> _contentTypes = new ArrayList<>();

	/** 
	 * Creates a {@link BlockRowType}.
	 *
	 */
	public BlockRowType() {
		super();
	}

	@Override
	protected String kind() {
		return BlockContentType.ROW_KIND;
	}

	/**
	 * The {@link RowPartType}s of this {@link BlockRow}.
	 */
	public List<RowPartType> getContentTypes() {
		return _contentTypes;
	}

	/**
	 * Adds the given {@link RowPartType} to this {@link BlockRowType}.
	 */
	public void append(RowPartType contentType) {
		_contentTypes.add(contentType);
	}

	@Override
	public BlockContent newInstance(Block owner) {
		return new BlockRow(owner, this);
	}

	/**
	 * Instantiates this {@link BlockRowType} to a list of {@link RowPart}s.
	 */
	public List<RowPart> createContents(BlockRow rowPart) {
		return _contentTypes.stream().map(t -> t.newInstance(rowPart)).collect(Collectors.toList());
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("contentTypes");
		JsonSerializable.writeArray(json, getContentTypes());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "contentTypes":
				json.beginArray();
				while (json.hasNext()) {
					append(RowPartType.read(context, json));
				}
				json.endArray();
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}

}