package com.top_logic.basic.json.schema.model;

/**
 * Schema for boolean type validation.
 *
 * <p>
 * Validates that an instance is a boolean (true or false).
 * Serializes as {"type": "boolean"}.
 * </p>
 */
public class BooleanSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.BooleanSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.BooleanSchema create() {
		return new com.top_logic.basic.json.schema.model.BooleanSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.BooleanSchema} type in JSON format. */
	public static final String BOOLEAN_SCHEMA__TYPE = "BooleanSchema";

	/**
	 * Creates a {@link BooleanSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.BooleanSchema#create()
	 */
	protected BooleanSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.BOOLEAN_SCHEMA;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.BooleanSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return BOOLEAN_SCHEMA__TYPE;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.BooleanSchema readBooleanSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.BooleanSchema result = new com.top_logic.basic.json.schema.model.BooleanSchema();
		result.readContent(in);
		return result;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
