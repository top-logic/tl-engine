package com.top_logic.ai.service.jsonschema.model;

/**
 * Negation schema.
 *
 * <p>
 * Instance must NOT validate against the subschema.
 * </p>
 */
public class NotSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.ai.service.jsonschema.model.NotSchema} instance.
	 */
	public static com.top_logic.ai.service.jsonschema.model.NotSchema create() {
		return new com.top_logic.ai.service.jsonschema.model.NotSchema();
	}

	/** Identifier for the {@link com.top_logic.ai.service.jsonschema.model.NotSchema} type in JSON format. */
	public static final String NOT_SCHEMA__TYPE = "NotSchema";

	/** @see #getNot() */
	public static final String NOT__PROP = "not";

	private com.top_logic.ai.service.jsonschema.model.Schema _not = null;

	/**
	 * Creates a {@link NotSchema} instance.
	 *
	 * @see com.top_logic.ai.service.jsonschema.model.NotSchema#create()
	 */
	protected NotSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.NOT_SCHEMA;
	}

	/**
	 * Schema that must not validate.
	 */
	public final com.top_logic.ai.service.jsonschema.model.Schema getNot() {
		return _not;
	}

	/**
	 * @see #getNot()
	 */
	public com.top_logic.ai.service.jsonschema.model.NotSchema setNot(com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalSetNot(value);
		return this;
	}

	/** Internal setter for {@link #getNot()} without chain call utility. */
	protected final void internalSetNot(com.top_logic.ai.service.jsonschema.model.Schema value) {
		_not = value;
	}

	/**
	 * Checks, whether {@link #getNot()} has a value.
	 */
	public final boolean hasNot() {
		return _not != null;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setDefinitions(java.util.Map<String, com.top_logic.ai.service.jsonschema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema putDefinition(String key, com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NotSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return NOT_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NOT__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.ai.service.jsonschema.model.Schema.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.ai.service.jsonschema.model.Schema.TRANSIENT_PROPERTIES);
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
			case NOT__PROP: return getNot();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NOT__PROP: internalSetNot((com.top_logic.ai.service.jsonschema.model.Schema) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.jsonschema.model.NotSchema readNotSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.jsonschema.model.NotSchema result = new com.top_logic.ai.service.jsonschema.model.NotSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasNot()) {
			out.name(NOT__PROP);
			getNot().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NOT__PROP: setNot(com.top_logic.ai.service.jsonschema.model.Schema.readSchema(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.ai.service.jsonschema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
