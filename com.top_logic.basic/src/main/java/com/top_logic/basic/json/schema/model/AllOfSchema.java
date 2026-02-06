package com.top_logic.basic.json.schema.model;

/**
 * All-of composition schema.
 *
 * <p>
 * Instance must validate against ALL subschemas.
 * </p>
 */
public class AllOfSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.AllOfSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.AllOfSchema create() {
		return new com.top_logic.basic.json.schema.model.AllOfSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.AllOfSchema} type in JSON format. */
	public static final String ALL_OF_SCHEMA__TYPE = "AllOfSchema";

	/** @see #getAllOf() */
	public static final String ALL_OF__PROP = "allOf";

	private final java.util.List<com.top_logic.basic.json.schema.model.Schema> _allOf = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AllOfSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.AllOfSchema#create()
	 */
	protected AllOfSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ALL_OF_SCHEMA;
	}

	/**
	 * Array of schemas - all must validate.
	 */
	public final java.util.List<com.top_logic.basic.json.schema.model.Schema> getAllOf() {
		return _allOf;
	}

	/**
	 * @see #getAllOf()
	 */
	public com.top_logic.basic.json.schema.model.AllOfSchema setAllOf(java.util.List<? extends com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetAllOf(value);
		return this;
	}

	/** Internal setter for {@link #getAllOf()} without chain call utility. */
	protected final void internalSetAllOf(java.util.List<? extends com.top_logic.basic.json.schema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'allOf' cannot be null.");
		_allOf.clear();
		_allOf.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAllOf()} list.
	 */
	public com.top_logic.basic.json.schema.model.AllOfSchema addAllOf(com.top_logic.basic.json.schema.model.Schema value) {
		internalAddAllOf(value);
		return this;
	}

	/** Implementation of {@link #addAllOf(com.top_logic.basic.json.schema.model.Schema)} without chain call utility. */
	protected final void internalAddAllOf(com.top_logic.basic.json.schema.model.Schema value) {
		_allOf.add(value);
	}

	/**
	 * Removes a value from the {@link #getAllOf()} list.
	 */
	public final void removeAllOf(com.top_logic.basic.json.schema.model.Schema value) {
		_allOf.remove(value);
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.AllOfSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ALL_OF_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ALL_OF__PROP);
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
			case ALL_OF__PROP: return getAllOf();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ALL_OF__PROP: internalSetAllOf(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.json.schema.model.Schema.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.AllOfSchema readAllOfSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.AllOfSchema result = new com.top_logic.basic.json.schema.model.AllOfSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ALL_OF__PROP);
		out.beginArray();
		for (com.top_logic.basic.json.schema.model.Schema x : getAllOf()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ALL_OF__PROP: {
				java.util.List<com.top_logic.basic.json.schema.model.Schema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.basic.json.schema.model.Schema.readSchema(in));
				}
				in.endArray();
				setAllOf(newValue);
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
