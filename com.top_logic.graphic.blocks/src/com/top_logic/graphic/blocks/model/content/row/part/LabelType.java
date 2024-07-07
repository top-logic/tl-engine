/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;

/**
 * Description of a {@link LabelDisplay}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelType extends RowPartType {
	private String _text;

	/**
	 * Creates a {@link LabelType}.
	 */
	public LabelType() {
		super(RowParts.LABEL_KIND);
	}

	/**
	 * The displayed label text.
	 */
	public String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public void setText(String text) {
		_text = text;
	}

	@Override
	public RowPart newInstance(BlockRow owner) {
		return new LabelDisplay(owner, this);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("text");
		json.value(getText());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "text":
				setText(json.nextString());
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}

}