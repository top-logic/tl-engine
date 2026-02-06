package com.top_logic.basic.json.schema.model;

/**
 * One-of composition schema.
 *
 * <p>
 * Instance must validate against EXACTLY ONE subschema.
 * </p>
 */
public class OneOfSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.OneOfSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.OneOfSchema create() {
		return new com.top_logic.basic.json.schema.model.OneOfSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.OneOfSchema} type in JSON format. */
	public static final String ONE_OF_SCHEMA__TYPE = "OneOfSchema";

	/** @see #getOneOf() */
	public static final String ONE_OF__PROP = "oneOf";

	private final java.util.List<com.top_logic.basic.json.schema.model.Schema> _oneOf = new java.util.ArrayList<>();

	/**
	 * Creates a {@link OneOfSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.OneOfSchema#create()
	 */
	protected OneOfSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ONE_OF_SCHEMA;
	}

	/**
	 * Array of schemas - exactly one must validate.
	 */
	public final java.util.List<com.top_logic.basic.json.schema.model.Schema> getOneOf() {
		return _oneOf;
	}

	/**
	 * @see #getOneOf()
	 */
	public com.top_logic.basic.json.schema.model.OneOfSchema setOneOf(java.util.List<? extends com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetOneOf(value);
		return this;
	}

	/** Internal setter for {@link #getOneOf()} without chain call utility. */
	protected final void internalSetOneOf(java.util.List<? extends com.top_logic.basic.json.schema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'oneOf' cannot be null.");
		_oneOf.clear();
		_oneOf.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getOneOf()} list.
	 */
	public com.top_logic.basic.json.schema.model.OneOfSchema addOneOf(com.top_logic.basic.json.schema.model.Schema value) {
		internalAddOneOf(value);
		return this;
	}

	/** Implementation of {@link #addOneOf(com.top_logic.basic.json.schema.model.Schema)} without chain call utility. */
	protected final void internalAddOneOf(com.top_logic.basic.json.schema.model.Schema value) {
		_oneOf.add(value);
	}

	/**
	 * Removes a value from the {@link #getOneOf()} list.
	 */
	public final void removeOneOf(com.top_logic.basic.json.schema.model.Schema value) {
		_oneOf.remove(value);
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.OneOfSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ONE_OF_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ONE_OF__PROP);
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
			case ONE_OF__PROP: return getOneOf();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ONE_OF__PROP: internalSetOneOf(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.json.schema.model.Schema.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.OneOfSchema readOneOfSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.OneOfSchema result = new com.top_logic.basic.json.schema.model.OneOfSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ONE_OF__PROP);
		out.beginArray();
		for (com.top_logic.basic.json.schema.model.Schema x : getOneOf()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ONE_OF__PROP: {
				java.util.List<com.top_logic.basic.json.schema.model.Schema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.basic.json.schema.model.Schema.readSchema(in));
				}
				in.endArray();
				setOneOf(newValue);
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
