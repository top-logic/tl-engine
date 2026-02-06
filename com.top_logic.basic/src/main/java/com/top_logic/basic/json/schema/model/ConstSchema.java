package com.top_logic.basic.json.schema.model;

/**
 * Constant value schema.
 *
 * <p>
 * Instance must equal exactly this value.
 * </p>
 */
public class ConstSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.ConstSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.ConstSchema create() {
		return new com.top_logic.basic.json.schema.model.ConstSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.ConstSchema} type in JSON format. */
	public static final String CONST_SCHEMA__TYPE = "ConstSchema";

	/** @see #getConstValue() */
	public static final String CONST_VALUE__PROP = "const";

	private String _constValue = "";

	/**
	 * Creates a {@link ConstSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.ConstSchema#create()
	 */
	protected ConstSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.CONST_SCHEMA;
	}

	/**
	 * Constant value (JSON-serialized string).
	 */
	public final String getConstValue() {
		return _constValue;
	}

	/**
	 * @see #getConstValue()
	 */
	public com.top_logic.basic.json.schema.model.ConstSchema setConstValue(String value) {
		internalSetConstValue(value);
		return this;
	}

	/** Internal setter for {@link #getConstValue()} without chain call utility. */
	protected final void internalSetConstValue(String value) {
		_constValue = value;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConstSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CONST_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONST_VALUE__PROP);
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
			case CONST_VALUE__PROP: return getConstValue();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONST_VALUE__PROP: internalSetConstValue((String) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.ConstSchema readConstSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.ConstSchema result = new com.top_logic.basic.json.schema.model.ConstSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CONST_VALUE__PROP);
		out.value(getConstValue());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONST_VALUE__PROP: setConstValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
