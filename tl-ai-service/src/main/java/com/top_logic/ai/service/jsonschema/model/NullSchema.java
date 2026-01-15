package com.top_logic.ai.service.jsonschema.model;

/**
 * Schema for null type validation.
 *
 * <p>
 * Validates that an instance is null (JSON null value).
 * Serializes as {"type": "null"}.
 * </p>
 */
public class NullSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.ai.service.jsonschema.model.NullSchema} instance.
	 */
	public static com.top_logic.ai.service.jsonschema.model.NullSchema create() {
		return new com.top_logic.ai.service.jsonschema.model.NullSchema();
	}

	/** Identifier for the {@link com.top_logic.ai.service.jsonschema.model.NullSchema} type in JSON format. */
	public static final String NULL_SCHEMA__TYPE = "NullSchema";

	/**
	 * Creates a {@link NullSchema} instance.
	 *
	 * @see com.top_logic.ai.service.jsonschema.model.NullSchema#create()
	 */
	protected NullSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.NULL_SCHEMA;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setDefinitions(java.util.Map<String, com.top_logic.ai.service.jsonschema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema putDefinition(String key, com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.NullSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return NULL_SCHEMA__TYPE;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.jsonschema.model.NullSchema readNullSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.jsonschema.model.NullSchema result = new com.top_logic.ai.service.jsonschema.model.NullSchema();
		result.readContent(in);
		return result;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.ai.service.jsonschema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
