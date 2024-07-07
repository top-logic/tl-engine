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
 * Description of a {@link TextInput}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextInputType extends InputType {

	private String _defaultValue;

	/**
	 * Creates a {@link TextInputType}.
	 */
	public TextInputType() {
		super(RowParts.TEXT_KIND);
	}

	@Override
	public RowPart newInstance(BlockRow owner) {
		return new TextInput(owner, this);
	}

	/**
	 * The default value to display when the {@link TextInput} is initially displayed.
	 */
	public String getDefaultValue() {
		return _defaultValue;
	}

	/**
	 * @see #getDefaultValue()
	 */
	public void setDefaultValue(String value) {
		_defaultValue = value;
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("defaultValue");
		json.value(getDefaultValue());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "defaultValue":
				setDefaultValue(json.nextString());
				break;

			default:
				super.readPropertyFrom(context, json, name);
				break;
		}
	}

}
