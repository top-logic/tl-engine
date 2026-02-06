package com.top_logic.basic.json.schema.model;

/**
 * Schema for string validation (JSON Schema 2020-12).
 *
 * <p>
 * Validates string instances with length, pattern, format, and content constraints.
 * </p>
 */
public class StringSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.StringSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.StringSchema create() {
		return new com.top_logic.basic.json.schema.model.StringSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.StringSchema} type in JSON format. */
	public static final String STRING_SCHEMA__TYPE = "StringSchema";

	/** @see #getMaxLength() */
	public static final String MAX_LENGTH__PROP = "maxLength";

	/** @see #getMinLength() */
	public static final String MIN_LENGTH__PROP = "minLength";

	/** @see #getPattern() */
	public static final String PATTERN__PROP = "pattern";

	/** @see #getFormat() */
	public static final String FORMAT__PROP = "format";

	/** @see #getContentEncoding() */
	public static final String CONTENT_ENCODING__PROP = "contentEncoding";

	/** @see #getContentMediaType() */
	public static final String CONTENT_MEDIA_TYPE__PROP = "contentMediaType";

	/** @see #getContentSchema() */
	public static final String CONTENT_SCHEMA__PROP = "contentSchema";

	private Integer _maxLength = null;

	private Integer _minLength = null;

	private String _pattern = null;

	private String _format = null;

	private String _contentEncoding = null;

	private String _contentMediaType = null;

	private com.top_logic.basic.json.schema.model.Schema _contentSchema = null;

	/**
	 * Creates a {@link StringSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.StringSchema#create()
	 */
	protected StringSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.STRING_SCHEMA;
	}

	/**
	 * Maximum string length (inclusive).
	 */
	public final Integer getMaxLength() {
		return _maxLength;
	}

	/**
	 * @see #getMaxLength()
	 */
	public com.top_logic.basic.json.schema.model.StringSchema setMaxLength(Integer value) {
		internalSetMaxLength(value);
		return this;
	}

	/** Internal setter for {@link #getMaxLength()} without chain call utility. */
	protected final void internalSetMaxLength(Integer value) {
		_maxLength = value;
	}

	/**
	 * Checks, whether {@link #getMaxLength()} has a value.
	 */
	public final boolean hasMaxLength() {
		return _maxLength != null;
	}

	/**
	 * Minimum string length (inclusive).
	 */
	public final Integer getMinLength() {
		return _minLength;
	}

	/**
	 * @see #getMinLength()
	 */
	public com.top_logic.basic.json.schema.model.StringSchema setMinLength(Integer value) {
		internalSetMinLength(value);
		return this;
	}

	/** Internal setter for {@link #getMinLength()} without chain call utility. */
	protected final void internalSetMinLength(Integer value) {
		_minLength = value;
	}

	/**
	 * Checks, whether {@link #getMinLength()} has a value.
	 */
	public final boolean hasMinLength() {
		return _minLength != null;
	}

	/**
	 * ECMA-262 regular expression pattern.
	 */
	public final String getPattern() {
		return _pattern;
	}

	/**
	 * @see #getPattern()
	 */
	public com.top_logic.basic.json.schema.model.StringSchema setPattern(String value) {
		internalSetPattern(value);
		return this;
	}

	/** Internal setter for {@link #getPattern()} without chain call utility. */
	protected final void internalSetPattern(String value) {
		_pattern = value;
	}

	/**
	 * Checks, whether {@link #getPattern()} has a value.
	 */
	public final boolean hasPattern() {
		return _pattern != null;
	}

	/**
	 * Format annotation for semantic validation.
	 *
	 * <p>
	 * Examples: date-time, email, hostname, ipv4, ipv6, uri, uuid, etc.
	 * </p>
	 */
	public final String getFormat() {
		return _format;
	}

	/**
	 * @see #getFormat()
	 */
	public com.top_logic.basic.json.schema.model.StringSchema setFormat(String value) {
		internalSetFormat(value);
		return this;
	}

	/** Internal setter for {@link #getFormat()} without chain call utility. */
	protected final void internalSetFormat(String value) {
		_format = value;
	}

	/**
	 * Checks, whether {@link #getFormat()} has a value.
	 */
	public final boolean hasFormat() {
		return _format != null;
	}

	/**
	 * Content encoding (e.g., base64, base32).
	 */
	public final String getContentEncoding() {
		return _contentEncoding;
	}

	/**
	 * @see #getContentEncoding()
	 */
	public com.top_logic.basic.json.schema.model.StringSchema setContentEncoding(String value) {
		internalSetContentEncoding(value);
		return this;
	}

	/** Internal setter for {@link #getContentEncoding()} without chain call utility. */
	protected final void internalSetContentEncoding(String value) {
		_contentEncoding = value;
	}

	/**
	 * Checks, whether {@link #getContentEncoding()} has a value.
	 */
	public final boolean hasContentEncoding() {
		return _contentEncoding != null;
	}

	/**
	 * MIME type of encoded content.
	 */
	public final String getContentMediaType() {
		return _contentMediaType;
	}

	/**
	 * @see #getContentMediaType()
	 */
	public com.top_logic.basic.json.schema.model.StringSchema setContentMediaType(String value) {
		internalSetContentMediaType(value);
		return this;
	}

	/** Internal setter for {@link #getContentMediaType()} without chain call utility. */
	protected final void internalSetContentMediaType(String value) {
		_contentMediaType = value;
	}

	/**
	 * Checks, whether {@link #getContentMediaType()} has a value.
	 */
	public final boolean hasContentMediaType() {
		return _contentMediaType != null;
	}

	/**
	 * Schema validating decoded content.
	 */
	public final com.top_logic.basic.json.schema.model.Schema getContentSchema() {
		return _contentSchema;
	}

	/**
	 * @see #getContentSchema()
	 */
	public com.top_logic.basic.json.schema.model.StringSchema setContentSchema(com.top_logic.basic.json.schema.model.Schema value) {
		internalSetContentSchema(value);
		return this;
	}

	/** Internal setter for {@link #getContentSchema()} without chain call utility. */
	protected final void internalSetContentSchema(com.top_logic.basic.json.schema.model.Schema value) {
		_contentSchema = value;
	}

	/**
	 * Checks, whether {@link #getContentSchema()} has a value.
	 */
	public final boolean hasContentSchema() {
		return _contentSchema != null;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.StringSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return STRING_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MAX_LENGTH__PROP, 
			MIN_LENGTH__PROP, 
			PATTERN__PROP, 
			FORMAT__PROP, 
			CONTENT_ENCODING__PROP, 
			CONTENT_MEDIA_TYPE__PROP, 
			CONTENT_SCHEMA__PROP);
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
			case MAX_LENGTH__PROP: return getMaxLength();
			case MIN_LENGTH__PROP: return getMinLength();
			case PATTERN__PROP: return getPattern();
			case FORMAT__PROP: return getFormat();
			case CONTENT_ENCODING__PROP: return getContentEncoding();
			case CONTENT_MEDIA_TYPE__PROP: return getContentMediaType();
			case CONTENT_SCHEMA__PROP: return getContentSchema();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MAX_LENGTH__PROP: internalSetMaxLength((Integer) value); break;
			case MIN_LENGTH__PROP: internalSetMinLength((Integer) value); break;
			case PATTERN__PROP: internalSetPattern((String) value); break;
			case FORMAT__PROP: internalSetFormat((String) value); break;
			case CONTENT_ENCODING__PROP: internalSetContentEncoding((String) value); break;
			case CONTENT_MEDIA_TYPE__PROP: internalSetContentMediaType((String) value); break;
			case CONTENT_SCHEMA__PROP: internalSetContentSchema((com.top_logic.basic.json.schema.model.Schema) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.StringSchema readStringSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.StringSchema result = new com.top_logic.basic.json.schema.model.StringSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasMaxLength()) {
			out.name(MAX_LENGTH__PROP);
			out.value(getMaxLength());
		}
		if (hasMinLength()) {
			out.name(MIN_LENGTH__PROP);
			out.value(getMinLength());
		}
		if (hasPattern()) {
			out.name(PATTERN__PROP);
			out.value(getPattern());
		}
		if (hasFormat()) {
			out.name(FORMAT__PROP);
			out.value(getFormat());
		}
		if (hasContentEncoding()) {
			out.name(CONTENT_ENCODING__PROP);
			out.value(getContentEncoding());
		}
		if (hasContentMediaType()) {
			out.name(CONTENT_MEDIA_TYPE__PROP);
			out.value(getContentMediaType());
		}
		if (hasContentSchema()) {
			out.name(CONTENT_SCHEMA__PROP);
			getContentSchema().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MAX_LENGTH__PROP: setMaxLength(in.nextInt()); break;
			case MIN_LENGTH__PROP: setMinLength(in.nextInt()); break;
			case PATTERN__PROP: setPattern(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FORMAT__PROP: setFormat(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_ENCODING__PROP: setContentEncoding(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_MEDIA_TYPE__PROP: setContentMediaType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_SCHEMA__PROP: setContentSchema(com.top_logic.basic.json.schema.model.Schema.readSchema(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
