/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * Common base class for {@link BlockModel} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBlockModel implements BlockModel {

	private String _id;

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public void setId(String value) {
		_id = value;
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		String id = getId();
		if (id != null) {
			json.name("id");
			json.value(id);
		}
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "id":
				setId(json.nextString());
				break;

			default:
				BlockModel.super.readPropertyFrom(context, json, name);
		}
	}

}
