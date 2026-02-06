package com.top_logic.basic.json.schema.model;

/**
 * Root JSON Schema document (JSON Schema 2020-12).
 *
 * <p>
 * Represents a complete JSON Schema document with dialect identifier
 * and vocabulary declarations. Can contain any schema type as its content.
 * </p>
 */
public class JsonSchemaDocument extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.JsonSchemaDocument} instance.
	 */
	public static com.top_logic.basic.json.schema.model.JsonSchemaDocument create() {
		return new com.top_logic.basic.json.schema.model.JsonSchemaDocument();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.JsonSchemaDocument} type in JSON format. */
	public static final String JSON_SCHEMA_DOCUMENT__TYPE = "JsonSchemaDocument";

	/** @see #getSchemaDialectUri() */
	public static final String SCHEMA_DIALECT_URI__PROP = "$schema";

	/** @see #getVocabulary() */
	public static final String VOCABULARY__PROP = "$vocabulary";

	/** @see #getSchema() */
	public static final String SCHEMA__PROP = "schema";

	private String _schemaDialectUri = null;

	private final java.util.Map<String, Boolean> _vocabulary = new java.util.LinkedHashMap<>();

	private com.top_logic.basic.json.schema.model.Schema _schema = null;

	/**
	 * Creates a {@link JsonSchemaDocument} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.JsonSchemaDocument#create()
	 */
	protected JsonSchemaDocument() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.JSON_SCHEMA_DOCUMENT;
	}

	/**
	 * Dialect identifier (e.g., "https://json-schema.org/draft/2020-12/schema").
	 *
	 * <p>
	 * Should be used in the document root schema object.
	 * </p>
	 */
	public final String getSchemaDialectUri() {
		return _schemaDialectUri;
	}

	/**
	 * @see #getSchemaDialectUri()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setSchemaDialectUri(String value) {
		internalSetSchemaDialectUri(value);
		return this;
	}

	/** Internal setter for {@link #getSchemaDialectUri()} without chain call utility. */
	protected final void internalSetSchemaDialectUri(String value) {
		_schemaDialectUri = value;
	}

	/**
	 * Checks, whether {@link #getSchemaDialectUri()} has a value.
	 */
	public final boolean hasSchemaDialectUri() {
		return _schemaDialectUri != null;
	}

	/**
	 * Vocabulary declarations (used in meta-schemas only).
	 *
	 * <p>
	 * Maps vocabulary URIs to boolean indicating if required (true) or optional (false).
	 * </p>
	 */
	public final java.util.Map<String, Boolean> getVocabulary() {
		return _vocabulary;
	}

	/**
	 * @see #getVocabulary()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setVocabulary(java.util.Map<String, Boolean> value) {
		internalSetVocabulary(value);
		return this;
	}

	/** Internal setter for {@link #getVocabulary()} without chain call utility. */
	protected final void internalSetVocabulary(java.util.Map<String, Boolean> value) {
		if (value == null) throw new IllegalArgumentException("Property 'vocabulary' cannot be null.");
		_vocabulary.clear();
		_vocabulary.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getVocabulary()} map.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument putVocabulary(String key, boolean value) {
		internalPutVocabulary(key, value);
		return this;
	}

	/** Implementation of {@link #putVocabulary(String, boolean)} without chain call utility. */
	protected final void  internalPutVocabulary(String key, boolean value) {
		if (_vocabulary.containsKey(key)) {
			throw new IllegalArgumentException("Property 'vocabulary' already contains a value for key '" + key + "'.");
		}
		_vocabulary.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getVocabulary()} map.
	 */
	public final void removeVocabulary(String key) {
		_vocabulary.remove(key);
	}

	/**
	 * The actual schema content.
	 *
	 * <p>
	 * Can be any schema type (StringSchema, ObjectSchema, AllOfSchema, etc.).
	 * </p>
	 */
	public final com.top_logic.basic.json.schema.model.Schema getSchema() {
		return _schema;
	}

	/**
	 * @see #getSchema()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setSchema(com.top_logic.basic.json.schema.model.Schema value) {
		internalSetSchema(value);
		return this;
	}

	/** Internal setter for {@link #getSchema()} without chain call utility. */
	protected final void internalSetSchema(com.top_logic.basic.json.schema.model.Schema value) {
		_schema = value;
	}

	/**
	 * Checks, whether {@link #getSchema()} has a value.
	 */
	public final boolean hasSchema() {
		return _schema != null;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.JsonSchemaDocument addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return JSON_SCHEMA_DOCUMENT__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SCHEMA_DIALECT_URI__PROP, 
			VOCABULARY__PROP, 
			SCHEMA__PROP);
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
			case SCHEMA_DIALECT_URI__PROP: return getSchemaDialectUri();
			case VOCABULARY__PROP: return getVocabulary();
			case SCHEMA__PROP: return getSchema();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SCHEMA_DIALECT_URI__PROP: internalSetSchemaDialectUri((String) value); break;
			case VOCABULARY__PROP: internalSetVocabulary((java.util.Map<String, Boolean>) value); break;
			case SCHEMA__PROP: internalSetSchema((com.top_logic.basic.json.schema.model.Schema) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.JsonSchemaDocument readJsonSchemaDocument(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.JsonSchemaDocument result = new com.top_logic.basic.json.schema.model.JsonSchemaDocument();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasSchemaDialectUri()) {
			out.name(SCHEMA_DIALECT_URI__PROP);
			out.value(getSchemaDialectUri());
		}
		out.name(VOCABULARY__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,Boolean> entry : getVocabulary().entrySet()) {
			out.name(entry.getKey());
			out.value(entry.getValue());
		}
		out.endObject();
		if (hasSchema()) {
			out.name(SCHEMA__PROP);
			getSchema().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SCHEMA_DIALECT_URI__PROP: setSchemaDialectUri(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VOCABULARY__PROP: {
				java.util.Map<String, Boolean> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), in.nextBoolean());
				}
				in.endObject();
				setVocabulary(newValue);
				break;
			}
			case SCHEMA__PROP: setSchema(com.top_logic.basic.json.schema.model.Schema.readSchema(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
