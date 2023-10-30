/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.entities;

import java.io.IOException;

import de.haumacher.msgbuf.data.AbstractDataObject;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonUtil;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A single entity of the {@link Entities} table.
 */
public class Entity extends AbstractDataObject {

	/**
	 * Creates a {@link Entity} instance.
	 */
	public static Entity create() {
		return new Entity();
	}

	/** Identifier for the {@link Entity} type in JSON format. */
	public static final String ENTITY__TYPE = "Entity";

	/** @see #getCharacters() */
	private static final String CHARACTERS__PROP = "characters";

	private String _characters = "";

	/**
	 * Creates a {@link Entity} instance.
	 *
	 * @see Entity#create()
	 */
	protected Entity() {
		super();
	}

	/**
	 * The replacement string of the referenced entity.
	 */
	public final String getCharacters() {
		return _characters;
	}

	/**
	 * @see #getCharacters()
	 */
	public Entity setCharacters(String value) {
		internalSetCharacters(value);
		return this;
	}

	/** Internal setter for {@link #getCharacters()} without chain call utility. */
	protected final void internalSetCharacters(String value) {
		_characters = value;
	}

	/** Reads a new instance from the given reader. */
	public static Entity readEntity(JsonReader in) throws IOException {
		Entity result = new Entity();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(JsonWriter out) throws IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(JsonWriter out) throws IOException {
		super.writeFields(out);
		out.name(CHARACTERS__PROP);
		out.value(getCharacters());
	}

	@Override
	protected void readField(JsonReader in, String field) throws IOException {
		switch (field) {
			case CHARACTERS__PROP:
				setCharacters(JsonUtil.nextStringOptional(in));
				break;
			default: super.readField(in, field);
		}
	}

}
