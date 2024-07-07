/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.content.row.part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.content.row.BlockRow;

/**
 * Description of a {@link SelectInput}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectInputType extends InputType {
	private final List<Option> _options = new ArrayList<>();

	/**
	 * Creates a {@link SelectInputType}.
	 *
	 */
	public SelectInputType() {
		super(RowParts.SELECT_KIND);
	}

	@Override
	public RowPart newInstance(BlockRow owner) {
		return new SelectInput(owner, this);
	}

	/**
	 * The {@link Option}s to choose from.
	 */
	public List<Option> getOptions() {
		return _options;
	}

	/**
	 * @see #getOptions()
	 */
	public void setOptions(Iterable<Option> options) {
		_options.clear();
		for (Option option : options) {
			addOption(option);
		}
	}

	/**
	 * Appends the given Option to the {@link #getOptions() options}.
	 */
	public void addOption(Option option) {
		_options.add(option);
	}

	/**
	 * The {@link Option} selected by default.
	 */
	public Option getDefaultOption() {
		return getOptions().get(0);
	}

	/**
	 * The {@link Option} with the given technical {@link Option#getName()}.
	 */
	public Option getOption(String name) {
		for (Option option : getOptions()) {
			if (option.getName().equals(name)) {
				return option;
			}
		}
		return null;
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("options");
		JsonSerializable.writeArray(json, getOptions());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "options":
				json.beginArray();
				while (json.hasNext()) {
					addOption(Option.read(context, json));
				}
				json.endArray();
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}

}