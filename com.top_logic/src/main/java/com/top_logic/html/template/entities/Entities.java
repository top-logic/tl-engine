package com.top_logic.html.template.entities;

/**
 * The format of the HTML5 named entity table.
 */
public class Entities extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.html.template.entities.Entities} instance.
	 */
	public static com.top_logic.html.template.entities.Entities create() {
		return new com.top_logic.html.template.entities.Entities();
	}

	/** Identifier for the {@link com.top_logic.html.template.entities.Entities} type in JSON format. */
	public static final String ENTITIES__TYPE = "Entities";

	/** @see #getEntities() */
	private static final String ENTITIES__PROP = "entities";

	private final java.util.Map<String, com.top_logic.html.template.entities.Entity> _entities = new java.util.LinkedHashMap<>();

	/**
	 * Creates a {@link Entities} instance.
	 *
	 * @see com.top_logic.html.template.entities.Entities#create()
	 */
	protected Entities() {
		super();
	}

	/**
	 * All entities indexed by their name including an optional tailing semicolon.
	 */
	public final java.util.Map<String, com.top_logic.html.template.entities.Entity> getEntities() {
		return _entities;
	}

	/**
	 * @see #getEntities()
	 */
	public com.top_logic.html.template.entities.Entities setEntities(java.util.Map<String, com.top_logic.html.template.entities.Entity> value) {
		internalSetEntities(value);
		return this;
	}

	/** Internal setter for {@link #getEntities()} without chain call utility. */
	protected final void internalSetEntities(java.util.Map<String, com.top_logic.html.template.entities.Entity> value) {
		if (value == null) throw new IllegalArgumentException("Property 'entities' cannot be null.");
		_entities.clear();
		_entities.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getEntities()} map.
	 */
	public com.top_logic.html.template.entities.Entities putEntity(String key, com.top_logic.html.template.entities.Entity value) {
		internalPutEntity(key, value);
		return this;
	}

	/** Implementation of {@link #putEntity(String, com.top_logic.html.template.entities.Entity)} without chain call utility. */
	protected final void  internalPutEntity(String key, com.top_logic.html.template.entities.Entity value) {
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
	public static com.top_logic.html.template.entities.Entities readEntities(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.html.template.entities.Entities result = new com.top_logic.html.template.entities.Entities();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ENTITIES__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.html.template.entities.Entity> entry : getEntities().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ENTITIES__PROP: {
				java.util.Map<String, com.top_logic.html.template.entities.Entity> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.html.template.entities.Entity.readEntity(in));
				}
				in.endObject();
				setEntities(newValue);
				break;
			}
			default: super.readField(in, field);
		}
	}

}
