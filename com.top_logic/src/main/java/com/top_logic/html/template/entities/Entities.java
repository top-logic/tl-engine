/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.entities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.haumacher.msgbuf.data.AbstractDataObject;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * The format of the HTML5 named entity table.
 */
public class Entities extends AbstractDataObject {

	/**
	 * Creates a {@link Entities} instance.
	 */
	public static Entities create() {
		return new Entities();
	}

	/** Identifier for the {@link Entities} type in JSON format. */
	public static final String ENTITIES__TYPE = "Entities";

	/** @see #getEntities() */
	private static final String ENTITIES__PROP = "entities";

	private final Map<String, Entity> _entities = new HashMap<>();

	/**
	 * Creates a {@link Entities} instance.
	 *
	 * @see Entities#create()
	 */
	protected Entities() {
		super();
	}

	/**
	 * All entities indexed by their name including an optional tailing semicolon.
	 */
	public final Map<String, Entity> getEntities() {
		return _entities;
	}

	/**
	 * @see #getEntities()
	 */
	public Entities setEntities(java.util.Map<String, Entity> value) {
		internalSetEntities(value);
		return this;
	}

	/** Internal setter for {@link #getEntities()} without chain call utility. */
	protected final void internalSetEntities(java.util.Map<String, Entity> value) {
		if (value == null) throw new IllegalArgumentException("Property 'entities' cannot be null.");
		_entities.clear();
		_entities.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getEntities()} map.
	 */
	public Entities putEntity(String key, Entity value) {
		internalPutEntity(key, value);
		return this;
	}

	/** Implementation of {@link #putEntity(String, Entity)} without chain call utility. */
	protected final void internalPutEntity(String key, Entity value) {
		if (_entities.containsKey(key)) {
			throw new IllegalArgumentException("Property 'entities' already contains a value for key '" + key + "'.");
		}
		_entities.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getEntities()} map.
	 */
	public final void removeEntity(String key) {
		_entities.remove(key);
	}

	/** Reads a new instance from the given reader. */
	public static Entities readEntities(JsonReader in) throws IOException {
		Entities result = new Entities();
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
		out.name(ENTITIES__PROP);
		out.beginObject();
		for (Entry<String, Entity> entry : getEntities().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
	}

	@Override
	protected void readField(JsonReader in, String field) throws IOException {
		switch (field) {
			case ENTITIES__PROP: {
				in.beginObject();
				while (in.hasNext()) {
					putEntity(in.nextName(), Entity.readEntity(in));
				}
				in.endObject();
				break;
			}
			default: super.readField(in, field);
		}
	}

}
