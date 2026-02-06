package com.top_logic.basic.json.schema.model;

/**
 * Dynamic reference resolved at runtime.
 *
 * <p>
 * Particularly useful for recursive schemas that reference themselves.
 * Works with <code>$dynamicAnchor</code>.
 * </p>
 */
public class DynamicRefSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.DynamicRefSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.DynamicRefSchema create() {
		return new com.top_logic.basic.json.schema.model.DynamicRefSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.DynamicRefSchema} type in JSON format. */
	public static final String DYNAMIC_REF_SCHEMA__TYPE = "DynamicRefSchema";

	/** @see #getDynamicRef() */
	public static final String DYNAMIC_REF__PROP = "$dynamicRef";

	private String _dynamicRef = "";

	/**
	 * Creates a {@link DynamicRefSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.DynamicRefSchema#create()
	 */
	protected DynamicRefSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.DYNAMIC_REF_SCHEMA;
	}

	/**
	 * Dynamic reference URI.
	 */
	public final String getDynamicRef() {
		return _dynamicRef;
	}

	/**
	 * @see #getDynamicRef()
	 */
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setDynamicRef(String value) {
		internalSetDynamicRef(value);
		return this;
	}

	/** Internal setter for {@link #getDynamicRef()} without chain call utility. */
	protected final void internalSetDynamicRef(String value) {
		_dynamicRef = value;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.DynamicRefSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DYNAMIC_REF_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			DYNAMIC_REF__PROP);
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
			case DYNAMIC_REF__PROP: return getDynamicRef();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case DYNAMIC_REF__PROP: internalSetDynamicRef((String) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.DynamicRefSchema readDynamicRefSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.DynamicRefSchema result = new com.top_logic.basic.json.schema.model.DynamicRefSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(DYNAMIC_REF__PROP);
		out.value(getDynamicRef());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DYNAMIC_REF__PROP: setDynamicRef(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
