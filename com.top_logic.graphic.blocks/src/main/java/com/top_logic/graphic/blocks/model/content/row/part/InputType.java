/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.graphic.blocks.model.BlockSchema;

/**
 * {@link RowPartType} describing an input field.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class InputType extends RowPartType {
	private String _id;

	/**
	 * Creates a {@link InputType}.
	 */
	public InputType(String kind) {
		super(kind);
	}

	/**
	 * The identifier for the input field.
	 */
	public String getInputId() {
		return _id;
	}

	/**
	 * @see #getInputId()
	 */
	public void setInputId(String id) {
		_id = id;
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "id":
				setInputId(json.nextString());
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}
}