package com.top_logic.basic.json.schema.model;

/**
 * Abstract base class for all JSON Schema types (JSON Schema 2020-12).
 *
 * <p>
 * Contains the core vocabulary keywords that can appear in any schema,
 * including metadata annotations and reference mechanisms.
 * </p>
 */
public abstract class Schema extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/** Type codes for the {@link com.top_logic.basic.json.schema.model.Schema} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link com.top_logic.basic.json.schema.model.TrueSchema}. */
		TRUE_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.FalseSchema}. */
		FALSE_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.RefSchema}. */
		REF_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.DynamicRefSchema}. */
		DYNAMIC_REF_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.NullSchema}. */
		NULL_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.BooleanSchema}. */
		BOOLEAN_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.StringSchema}. */
		STRING_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.NumericSchema}. */
		NUMERIC_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.ArraySchema}. */
		ARRAY_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.ObjectSchema}. */
		OBJECT_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.EnumSchema}. */
		ENUM_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.ConstSchema}. */
		CONST_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.AllOfSchema}. */
		ALL_OF_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.AnyOfSchema}. */
		ANY_OF_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.OneOfSchema}. */
		ONE_OF_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.NotSchema}. */
		NOT_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.ConditionalSchema}. */
		CONDITIONAL_SCHEMA,

		/** Type literal for {@link com.top_logic.basic.json.schema.model.JsonSchemaDocument}. */
		JSON_SCHEMA_DOCUMENT,
		;

	}

	/** Visitor interface for the {@link com.top_logic.basic.json.schema.model.Schema} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> {

		/** Visit case for {@link com.top_logic.basic.json.schema.model.TrueSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.TrueSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.FalseSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.FalseSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.RefSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.RefSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.DynamicRefSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.DynamicRefSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.NullSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.NullSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.BooleanSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.BooleanSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.StringSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.StringSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.NumericSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.NumericSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.ArraySchema}.*/
		R visit(com.top_logic.basic.json.schema.model.ArraySchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.ObjectSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.ObjectSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.EnumSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.EnumSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.ConstSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.ConstSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.AllOfSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.AllOfSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.AnyOfSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.AnyOfSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.OneOfSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.OneOfSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.NotSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.NotSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.ConditionalSchema}.*/
		R visit(com.top_logic.basic.json.schema.model.ConditionalSchema self, A arg) throws E;

		/** Visit case for {@link com.top_logic.basic.json.schema.model.JsonSchemaDocument}.*/
		R visit(com.top_logic.basic.json.schema.model.JsonSchemaDocument self, A arg) throws E;

	}

	/** @see #getId() */
	public static final String ID__PROP = "$id";

	/** @see #getAnchor() */
	public static final String ANCHOR__PROP = "$anchor";

	/** @see #getDynamicAnchor() */
	public static final String DYNAMIC_ANCHOR__PROP = "$dynamicAnchor";

	/** @see #getComment() */
	public static final String COMMENT__PROP = "$comment";

	/** @see #getDefinitions() */
	public static final String DEFINITIONS__PROP = "$defs";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getDefaultValue() */
	public static final String DEFAULT_VALUE__PROP = "default";

	/** @see #isDeprecated() */
	public static final String DEPRECATED__PROP = "deprecated";

	/** @see #isReadOnly() */
	public static final String READ_ONLY__PROP = "readOnly";

	/** @see #isWriteOnly() */
	public static final String WRITE_ONLY__PROP = "writeOnly";

	/** @see #getExamples() */
	public static final String EXAMPLES__PROP = "examples";

	private String _id = null;

	private String _anchor = null;

	private String _dynamicAnchor = null;

	private String _comment = null;

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> _definitions = new java.util.LinkedHashMap<>();

	private String _title = null;

	private String _description = null;

	private String _defaultValue = null;

	private boolean _deprecated = false;

	private boolean _readOnly = false;

	private boolean _writeOnly = false;

	private final java.util.List<String> _examples = new java.util.ArrayList<>();

	/**
	 * Creates a {@link Schema} instance.
	 */
	protected Schema() {
		super();
	}

	/** The type code of this instance. */
	public abstract TypeKind kind();

	/**
	 * Canonical URI identifying this schema resource.
	 *
	 * <p>
	 * Establishes the base URI for resolving relative references.
	 * Subschemas with $id create distinct schema resources.
	 * </p>
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.basic.json.schema.model.Schema setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Checks, whether {@link #getId()} has a value.
	 */
	public final boolean hasId() {
		return _id != null;
	}

	/**
	 * Plain name fragment for referencing this schema.
	 *
	 * <p>
	 * Creates location-independent references that survive schema restructuring.
	 * Value must match XML's NCName pattern.
	 * </p>
	 */
	public final String getAnchor() {
		return _anchor;
	}

	/**
	 * @see #getAnchor()
	 */
	public com.top_logic.basic.json.schema.model.Schema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	/** Internal setter for {@link #getAnchor()} without chain call utility. */
	protected final void internalSetAnchor(String value) {
		_anchor = value;
	}

	/**
	 * Checks, whether {@link #getAnchor()} has a value.
	 */
	public final boolean hasAnchor() {
		return _anchor != null;
	}

	/**
	 * Dynamic anchor for runtime-resolved references.
	 *
	 * <p>
	 * Works cooperatively with <code>$dynamicRef</code> for recursive schemas.
	 * </p>
	 */
	public final String getDynamicAnchor() {
		return _dynamicAnchor;
	}

	/**
	 * @see #getDynamicAnchor()
	 */
	public com.top_logic.basic.json.schema.model.Schema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	/** Internal setter for {@link #getDynamicAnchor()} without chain call utility. */
	protected final void internalSetDynamicAnchor(String value) {
		_dynamicAnchor = value;
	}

	/**
	 * Checks, whether {@link #getDynamicAnchor()} has a value.
	 */
	public final boolean hasDynamicAnchor() {
		return _dynamicAnchor != null;
	}

	/**
	 * Comment for schema authors.
	 *
	 * <p>
	 * Explanatory text that doesn't affect validation or annotation results.
	 * </p>
	 */
	public final String getComment() {
		return _comment;
	}

	/**
	 * @see #getComment()
	 */
	public com.top_logic.basic.json.schema.model.Schema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	/** Internal setter for {@link #getComment()} without chain call utility. */
	protected final void internalSetComment(String value) {
		_comment = value;
	}

	/**
	 * Checks, whether {@link #getComment()} has a value.
	 */
	public final boolean hasComment() {
		return _comment != null;
	}

	/**
	 * Reusable schema definitions.
	 *
	 * <p>
	 * Reserved location for defining schemas that can be referenced via $ref.
	 * Does not directly affect validation.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> getDefinitions() {
		return _definitions;
	}

	/**
	 * @see #getDefinitions()
	 */
	public com.top_logic.basic.json.schema.model.Schema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	/** Internal setter for {@link #getDefinitions()} without chain call utility. */
	protected final void internalSetDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'definitions' cannot be null.");
		_definitions.clear();
		_definitions.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getDefinitions()} map.
	 */
	public com.top_logic.basic.json.schema.model.Schema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	/** Implementation of {@link #putDefinition(String, com.top_logic.basic.json.schema.model.Schema)} without chain call utility. */
	protected final void  internalPutDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		if (_definitions.containsKey(key)) {
			throw new IllegalArgumentException("Property 'definitions' already contains a value for key '" + key + "'.");
		}
		_definitions.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getDefinitions()} map.
	 */
	public final void removeDefinition(String key) {
		_definitions.remove(key);
	}

	/**
	 * Title annotation for UI display.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.basic.json.schema.model.Schema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	/**
	 * Checks, whether {@link #getTitle()} has a value.
	 */
	public final boolean hasTitle() {
		return _title != null;
	}

	/**
	 * Description annotation for documentation.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.basic.json.schema.model.Schema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Checks, whether {@link #getDescription()} has a value.
	 */
	public final boolean hasDescription() {
		return _description != null;
	}

	/**
	 * Default value (JSON-serialized string).
	 */
	public final String getDefaultValue() {
		return _defaultValue;
	}

	/**
	 * @see #getDefaultValue()
	 */
	public com.top_logic.basic.json.schema.model.Schema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	/** Internal setter for {@link #getDefaultValue()} without chain call utility. */
	protected final void internalSetDefaultValue(String value) {
		_defaultValue = value;
	}

	/**
	 * Checks, whether {@link #getDefaultValue()} has a value.
	 */
	public final boolean hasDefaultValue() {
		return _defaultValue != null;
	}

	/**
	 * Deprecated flag marking schema as obsolete.
	 */
	public final boolean isDeprecated() {
		return _deprecated;
	}

	/**
	 * @see #isDeprecated()
	 */
	public com.top_logic.basic.json.schema.model.Schema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	/** Internal setter for {@link #isDeprecated()} without chain call utility. */
	protected final void internalSetDeprecated(boolean value) {
		_deprecated = value;
	}

	/**
	 * Read-only flag for access control metadata.
	 */
	public final boolean isReadOnly() {
		return _readOnly;
	}

	/**
	 * @see #isReadOnly()
	 */
	public com.top_logic.basic.json.schema.model.Schema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	/** Internal setter for {@link #isReadOnly()} without chain call utility. */
	protected final void internalSetReadOnly(boolean value) {
		_readOnly = value;
	}

	/**
	 * Write-only flag for access control metadata.
	 */
	public final boolean isWriteOnly() {
		return _writeOnly;
	}

	/**
	 * @see #isWriteOnly()
	 */
	public com.top_logic.basic.json.schema.model.Schema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	/** Internal setter for {@link #isWriteOnly()} without chain call utility. */
	protected final void internalSetWriteOnly(boolean value) {
		_writeOnly = value;
	}

	/**
	 * Example values (JSON-serialized strings).
	 */
	public final java.util.List<String> getExamples() {
		return _examples;
	}

	/**
	 * @see #getExamples()
	 */
	public com.top_logic.basic.json.schema.model.Schema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	/** Internal setter for {@link #getExamples()} without chain call utility. */
	protected final void internalSetExamples(java.util.List<? extends String> value) {
		_examples.clear();
		_examples.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getExamples()} list.
	 */
	public com.top_logic.basic.json.schema.model.Schema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	/** Implementation of {@link #addExample(String)} without chain call utility. */
	protected final void internalAddExample(String value) {
		_examples.add(value);
	}

	/**
	 * Removes a value from the {@link #getExamples()} list.
	 */
	public final void removeExample(String value) {
		_examples.remove(value);
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			ANCHOR__PROP, 
			DYNAMIC_ANCHOR__PROP, 
			COMMENT__PROP, 
			DEFINITIONS__PROP, 
			TITLE__PROP, 
			DESCRIPTION__PROP, 
			DEFAULT_VALUE__PROP, 
			DEPRECATED__PROP, 
			READ_ONLY__PROP, 
			WRITE_ONLY__PROP, 
			EXAMPLES__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
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
			case ID__PROP: return getId();
			case ANCHOR__PROP: return getAnchor();
			case DYNAMIC_ANCHOR__PROP: return getDynamicAnchor();
			case COMMENT__PROP: return getComment();
			case DEFINITIONS__PROP: return getDefinitions();
			case TITLE__PROP: return getTitle();
			case DESCRIPTION__PROP: return getDescription();
			case DEFAULT_VALUE__PROP: return getDefaultValue();
			case DEPRECATED__PROP: return isDeprecated();
			case READ_ONLY__PROP: return isReadOnly();
			case WRITE_ONLY__PROP: return isWriteOnly();
			case EXAMPLES__PROP: return getExamples();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case ANCHOR__PROP: internalSetAnchor((String) value); break;
			case DYNAMIC_ANCHOR__PROP: internalSetDynamicAnchor((String) value); break;
			case COMMENT__PROP: internalSetComment((String) value); break;
			case DEFINITIONS__PROP: internalSetDefinitions((java.util.Map<String, com.top_logic.basic.json.schema.model.Schema>) value); break;
			case TITLE__PROP: internalSetTitle((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case DEFAULT_VALUE__PROP: internalSetDefaultValue((String) value); break;
			case DEPRECATED__PROP: internalSetDeprecated((boolean) value); break;
			case READ_ONLY__PROP: internalSetReadOnly((boolean) value); break;
			case WRITE_ONLY__PROP: internalSetWriteOnly((boolean) value); break;
			case EXAMPLES__PROP: internalSetExamples(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.Schema readSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.Schema result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case TrueSchema.TRUE_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.TrueSchema.readTrueSchema(in); break;
			case FalseSchema.FALSE_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.FalseSchema.readFalseSchema(in); break;
			case RefSchema.REF_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.RefSchema.readRefSchema(in); break;
			case DynamicRefSchema.DYNAMIC_REF_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.DynamicRefSchema.readDynamicRefSchema(in); break;
			case NullSchema.NULL_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.NullSchema.readNullSchema(in); break;
			case BooleanSchema.BOOLEAN_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.BooleanSchema.readBooleanSchema(in); break;
			case StringSchema.STRING_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.StringSchema.readStringSchema(in); break;
			case NumericSchema.NUMERIC_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.NumericSchema.readNumericSchema(in); break;
			case ArraySchema.ARRAY_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.ArraySchema.readArraySchema(in); break;
			case ObjectSchema.OBJECT_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.ObjectSchema.readObjectSchema(in); break;
			case EnumSchema.ENUM_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.EnumSchema.readEnumSchema(in); break;
			case ConstSchema.CONST_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.ConstSchema.readConstSchema(in); break;
			case AllOfSchema.ALL_OF_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.AllOfSchema.readAllOfSchema(in); break;
			case AnyOfSchema.ANY_OF_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.AnyOfSchema.readAnyOfSchema(in); break;
			case OneOfSchema.ONE_OF_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.OneOfSchema.readOneOfSchema(in); break;
			case NotSchema.NOT_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.NotSchema.readNotSchema(in); break;
			case ConditionalSchema.CONDITIONAL_SCHEMA__TYPE: result = com.top_logic.basic.json.schema.model.ConditionalSchema.readConditionalSchema(in); break;
			case JsonSchemaDocument.JSON_SCHEMA_DOCUMENT__TYPE: result = com.top_logic.basic.json.schema.model.JsonSchemaDocument.readJsonSchemaDocument(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.beginArray();
		out.value(jsonType());
		writeContent(out);
		out.endArray();
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasId()) {
			out.name(ID__PROP);
			out.value(getId());
		}
		if (hasAnchor()) {
			out.name(ANCHOR__PROP);
			out.value(getAnchor());
		}
		if (hasDynamicAnchor()) {
			out.name(DYNAMIC_ANCHOR__PROP);
			out.value(getDynamicAnchor());
		}
		if (hasComment()) {
			out.name(COMMENT__PROP);
			out.value(getComment());
		}
		out.name(DEFINITIONS__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.Schema> entry : getDefinitions().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
		if (hasTitle()) {
			out.name(TITLE__PROP);
			out.value(getTitle());
		}
		if (hasDescription()) {
			out.name(DESCRIPTION__PROP);
			out.value(getDescription());
		}
		if (hasDefaultValue()) {
			out.name(DEFAULT_VALUE__PROP);
			out.value(getDefaultValue());
		}
		out.name(DEPRECATED__PROP);
		out.value(isDeprecated());
		out.name(READ_ONLY__PROP);
		out.value(isReadOnly());
		out.name(WRITE_ONLY__PROP);
		out.value(isWriteOnly());
		out.name(EXAMPLES__PROP);
		out.beginArray();
		for (String x : getExamples()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ANCHOR__PROP: setAnchor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DYNAMIC_ANCHOR__PROP: setDynamicAnchor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COMMENT__PROP: setComment(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEFINITIONS__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.Schema.readSchema(in));
				}
				in.endObject();
				setDefinitions(newValue);
				break;
			}
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEFAULT_VALUE__PROP: setDefaultValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEPRECATED__PROP: setDeprecated(in.nextBoolean()); break;
			case READ_ONLY__PROP: setReadOnly(in.nextBoolean()); break;
			case WRITE_ONLY__PROP: setWriteOnly(in.nextBoolean()); break;
			case EXAMPLES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setExamples(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
