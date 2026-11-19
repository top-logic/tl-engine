package com.top_logic.html.template.entities;

/**
 * A single entity of the {@link Entities} table.
 */
public class Entity extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.html.template.entities.Entity} instance.
	 */
	public static com.top_logic.html.template.entities.Entity create() {
		return new com.top_logic.html.template.entities.Entity();
	}

	/** Identifier for the {@link com.top_logic.html.template.entities.Entity} type in JSON format. */
	public static final String ENTITY__TYPE = "Entity";

	/** @see #getCharacters() */
	private static final String CHARACTERS__PROP = "characters";

	private String _characters = "";

	/**
	 * Creates a {@link Entity} instance.
	 *
	 * @see com.top_logic.html.template.entities.Entity#create()
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
	public com.top_logic.html.template.entities.Entity setCharacters(String value) {
		internalSetCharacters(value);
		return this;
	}

	/** Internal setter for {@link #getCharacters()} without chain call utility. */
	protected final void internalSetCharacters(String value) {
		_characters = value;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.html.template.entities.Entity readEntity(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.html.template.entities.Entity result = new com.top_logic.html.template.entities.Entity();
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
		out.name(CHARACTERS__PROP);
		out.value(getCharacters());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CHARACTERS__PROP: setCharacters(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
