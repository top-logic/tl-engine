/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.BlockSchema;

/**
 * Option of a {@link SelectInput}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Option implements JsonSerializable<BlockSchema> {

	private String _name;

	private String _label;

	private Option() {
		this(null);
	}

	/**
	 * Creates a {@link Option}.
	 */
	public Option(String name) {
		this(name, name);
	}

	/**
	 * Creates a {@link Option}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param label
	 *        See {@link #getLabel()}.
	 */
	public Option(String name, String label) {
		_name = name;
		_label = label;
	}

	/**
	 * The technical name of the {@link Option}.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @see #setName(String)
	 */
	private void setName(String name) {
		_name = name;
	}

	/**
	 * The displayed label of this {@link Option}.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public void setLabel(String label) {
		_label = label;
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("name");
		json.value(getName());
		json.name("label");
		json.value(getLabel());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "name":
				setName(json.nextString());
				break;
			case "label":
				setLabel(json.nextString());
				break;

			default:
				JsonSerializable.super.readPropertyFrom(context, json, name);
		}
	}

	/**
	 * Reads an {@link Option} from the given {@link JsonReader}.
	 */
	public static Option read(BlockSchema context, JsonReader json) throws IOException {
		Option result = new Option();
		result.readFrom(context, json);
		return result;
	}

}
