package com.top_logic.ai.service.jsonschema.model;

/**
 * Boolean schema that always passes validation.
 *
 * <p>
 * Equivalent to an empty schema object {}.
 * </p>
 */
public class TrueSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.ai.service.jsonschema.model.TrueSchema} instance.
	 */
	public static com.top_logic.ai.service.jsonschema.model.TrueSchema create() {
		return new com.top_logic.ai.service.jsonschema.model.TrueSchema();
	}

	/** Identifier for the {@link com.top_logic.ai.service.jsonschema.model.TrueSchema} type in JSON format. */
	public static final String TRUE_SCHEMA__TYPE = "TrueSchema";

	/**
	 * Creates a {@link TrueSchema} instance.
	 *
	 * @see com.top_logic.ai.service.jsonschema.model.TrueSchema#create()
	 */
	protected TrueSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.TRUE_SCHEMA;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setDefinitions(java.util.Map<String, com.top_logic.ai.service.jsonschema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema putDefinition(String key, com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.TrueSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TRUE_SCHEMA__TYPE;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.jsonschema.model.TrueSchema readTrueSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.jsonschema.model.TrueSchema result = new com.top_logic.ai.service.jsonschema.model.TrueSchema();
		result.readContent(in);
		return result;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.ai.service.jsonschema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
