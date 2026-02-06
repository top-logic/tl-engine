package com.top_logic.basic.json.schema.model;

/**
 * Enumeration schema constraining instance to specific values.
 *
 * <p>
 * Instance must equal one of the values in the enum array.
 * </p>
 */
public class EnumSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.EnumSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.EnumSchema create() {
		return new com.top_logic.basic.json.schema.model.EnumSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.EnumSchema} type in JSON format. */
	public static final String ENUM_SCHEMA__TYPE = "EnumSchema";

	/** @see #getEnumLiterals() */
	public static final String ENUM_LITERALS__PROP = "enum";

	private final java.util.List<String> _enumLiterals = new java.util.ArrayList<>();

	/**
	 * Creates a {@link EnumSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.EnumSchema#create()
	 */
	protected EnumSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ENUM_SCHEMA;
	}

	/**
	 * Allowed values (JSON-serialized strings).
	 */
	public final java.util.List<String> getEnumLiterals() {
		return _enumLiterals;
	}

	/**
	 * @see #getEnumLiterals()
	 */
	public com.top_logic.basic.json.schema.model.EnumSchema setEnumLiterals(java.util.List<? extends String> value) {
		internalSetEnumLiterals(value);
		return this;
	}

	/** Internal setter for {@link #getEnumLiterals()} without chain call utility. */
	protected final void internalSetEnumLiterals(java.util.List<? extends String> value) {
		_enumLiterals.clear();
		_enumLiterals.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getEnumLiterals()} list.
	 */
	public com.top_logic.basic.json.schema.model.EnumSchema addEnumLiteral(String value) {
		internalAddEnumLiteral(value);
		return this;
	}

	/** Implementation of {@link #addEnumLiteral(String)} without chain call utility. */
	protected final void internalAddEnumLiteral(String value) {
		_enumLiterals.add(value);
	}

	/**
	 * Removes a value from the {@link #getEnumLiterals()} list.
	 */
	public final void removeEnumLiteral(String value) {
		_enumLiterals.remove(value);
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.EnumSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ENUM_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ENUM_LITERALS__PROP);
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
			case ENUM_LITERALS__PROP: return getEnumLiterals();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ENUM_LITERALS__PROP: internalSetEnumLiterals(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.EnumSchema readEnumSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.EnumSchema result = new com.top_logic.basic.json.schema.model.EnumSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ENUM_LITERALS__PROP);
		out.beginArray();
		for (String x : getEnumLiterals()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ENUM_LITERALS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setEnumLiterals(newValue);
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
