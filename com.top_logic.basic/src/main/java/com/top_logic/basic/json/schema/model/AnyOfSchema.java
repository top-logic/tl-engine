package com.top_logic.basic.json.schema.model;

/**
 * Any-of composition schema.
 *
 * <p>
 * Instance must validate against AT LEAST ONE subschema.
 * </p>
 */
public class AnyOfSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.AnyOfSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.AnyOfSchema create() {
		return new com.top_logic.basic.json.schema.model.AnyOfSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.AnyOfSchema} type in JSON format. */
	public static final String ANY_OF_SCHEMA__TYPE = "AnyOfSchema";

	/** @see #getAnyOf() */
	public static final String ANY_OF__PROP = "anyOf";

	private final java.util.List<com.top_logic.basic.json.schema.model.Schema> _anyOf = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AnyOfSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.AnyOfSchema#create()
	 */
	protected AnyOfSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ANY_OF_SCHEMA;
	}

	/**
	 * Array of schemas - at least one must validate.
	 */
	public final java.util.List<com.top_logic.basic.json.schema.model.Schema> getAnyOf() {
		return _anyOf;
	}

	/**
	 * @see #getAnyOf()
	 */
	public com.top_logic.basic.json.schema.model.AnyOfSchema setAnyOf(java.util.List<? extends com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetAnyOf(value);
		return this;
	}

	/** Internal setter for {@link #getAnyOf()} without chain call utility. */
	protected final void internalSetAnyOf(java.util.List<? extends com.top_logic.basic.json.schema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'anyOf' cannot be null.");
		_anyOf.clear();
		_anyOf.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAnyOf()} list.
	 */
	public com.top_logic.basic.json.schema.model.AnyOfSchema addAnyOf(com.top_logic.basic.json.schema.model.Schema value) {
		internalAddAnyOf(value);
		return this;
	}

	/** Implementation of {@link #addAnyOf(com.top_logic.basic.json.schema.model.Schema)} without chain call utility. */
	protected final void internalAddAnyOf(com.top_logic.basic.json.schema.model.Schema value) {
		_anyOf.add(value);
	}

	/**
	 * Removes a value from the {@link #getAnyOf()} list.
	 */
	public final void removeAnyOf(com.top_logic.basic.json.schema.model.Schema value) {
		_anyOf.remove(value);
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AnyOfSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ANY_OF_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ANY_OF__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.basic.json.schema.model.Schema.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.basic.json.schema.model.Schema.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case ANY_OF__PROP: return getAnyOf();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ANY_OF__PROP: internalSetAnyOf(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.json.schema.model.Schema.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.AnyOfSchema readAnyOfSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.AnyOfSchema result = new com.top_logic.basic.json.schema.model.AnyOfSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ANY_OF__PROP);
		out.beginArray();
		for (com.top_logic.basic.json.schema.model.Schema x : getAnyOf()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ANY_OF__PROP: {
				java.util.List<com.top_logic.basic.json.schema.model.Schema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.basic.json.schema.model.Schema.readSchema(in));
				}
				in.endArray();
				setAnyOf(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
