package com.top_logic.ai.service.jsonschema.model;

/**
 * Boolean schema that always fails validation.
 *
 * <p>
 * Equivalent to {"not": {}}.
 * </p>
 */
public class FalseSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.ai.service.jsonschema.model.FalseSchema} instance.
	 */
	public static com.top_logic.ai.service.jsonschema.model.FalseSchema create() {
		return new com.top_logic.ai.service.jsonschema.model.FalseSchema();
	}

	/** Identifier for the {@link com.top_logic.ai.service.jsonschema.model.FalseSchema} type in JSON format. */
	public static final String FALSE_SCHEMA__TYPE = "FalseSchema";

	/**
	 * Creates a {@link FalseSchema} instance.
	 *
	 * @see com.top_logic.ai.service.jsonschema.model.FalseSchema#create()
	 */
	protected FalseSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.FALSE_SCHEMA;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setDefinitions(java.util.Map<String, com.top_logic.ai.service.jsonschema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema putDefinition(String key, com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.FalseSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return FALSE_SCHEMA__TYPE;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.jsonschema.model.FalseSchema readFalseSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.jsonschema.model.FalseSchema result = new com.top_logic.ai.service.jsonschema.model.FalseSchema();
		result.readContent(in);
		return result;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.ai.service.jsonschema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
