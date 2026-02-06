package com.top_logic.basic.json.schema.model;

/**
 * Root JSON Schema document according to JSON Schema 2020-12 specification.
 *
 * <p>
 * Represents a complete JSON Schema that can validate JSON documents.
 * Supports all features from the JSON Schema 2020-12 specification including
 * validation keywords, applicators, composition, metadata, and references.
 * </p>
 */
public class JsonSchema extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.JsonSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.JsonSchema create() {
		return new com.top_logic.basic.json.schema.model.JsonSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.JsonSchema} type in JSON format. */
	public static final String JSON_SCHEMA__TYPE = "JsonSchema";

	/** @see #get$schema() */
	public static final String $SCHEMA__PROP = "$schema";

	/** @see #get$id() */
	public static final String $ID__PROP = "$id";

	/** @see #get$anchor() */
	public static final String $ANCHOR__PROP = "$anchor";

	/** @see #get$dynamicAnchor() */
	public static final String $DYNAMIC_ANCHOR__PROP = "$dynamicAnchor";

	/** @see #get$ref() */
	public static final String $REF__PROP = "$ref";

	/** @see #get$dynamicRef() */
	public static final String $DYNAMIC_REF__PROP = "$dynamicRef";

	/** @see #get$defs() */
	public static final String $DEFS__PROP = "$defs";

	/** @see #get$comment() */
	public static final String $COMMENT__PROP = "$comment";

	/** @see #get$vocabulary() */
	public static final String $VOCABULARY__PROP = "$vocabulary";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getEnum() */
	public static final String ENUM__PROP = "enum";

	/** @see #getConst() */
	public static final String CONST__PROP = "const";

	/** @see #getMultipleOf() */
	public static final String MULTIPLE_OF__PROP = "multipleOf";

	/** @see #getMaximum() */
	public static final String MAXIMUM__PROP = "maximum";

	/** @see #getExclusiveMaximum() */
	public static final String EXCLUSIVE_MAXIMUM__PROP = "exclusiveMaximum";

	/** @see #getMinimum() */
	public static final String MINIMUM__PROP = "minimum";

	/** @see #getExclusiveMinimum() */
	public static final String EXCLUSIVE_MINIMUM__PROP = "exclusiveMinimum";

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

	/** @see #getMaxItems() */
	public static final String MAX_ITEMS__PROP = "maxItems";

	/** @see #getMinItems() */
	public static final String MIN_ITEMS__PROP = "minItems";

	/** @see #isUniqueItems() */
	public static final String UNIQUE_ITEMS__PROP = "uniqueItems";

	/** @see #getMaxContains() */
	public static final String MAX_CONTAINS__PROP = "maxContains";

	/** @see #getMinContains() */
	public static final String MIN_CONTAINS__PROP = "minContains";

	/** @see #getMaxProperties() */
	public static final String MAX_PROPERTIES__PROP = "maxProperties";

	/** @see #getMinProperties() */
	public static final String MIN_PROPERTIES__PROP = "minProperties";

	/** @see #getRequired() */
	public static final String REQUIRED__PROP = "required";

	/** @see #getDependentRequired() */
	public static final String DEPENDENT_REQUIRED__PROP = "dependentRequired";

	/** @see #getProperties() */
	public static final String PROPERTIES__PROP = "properties";

	/** @see #getPatternProperties() */
	public static final String PATTERN_PROPERTIES__PROP = "patternProperties";

	/** @see #getAdditionalProperties() */
	public static final String ADDITIONAL_PROPERTIES__PROP = "additionalProperties";

	/** @see #getPropertyNames() */
	public static final String PROPERTY_NAMES__PROP = "propertyNames";

	/** @see #getPrefixItems() */
	public static final String PREFIX_ITEMS__PROP = "prefixItems";

	/** @see #getItems() */
	public static final String ITEMS__PROP = "items";

	/** @see #getContains() */
	public static final String CONTAINS__PROP = "contains";

	/** @see #getIf() */
	public static final String IF__PROP = "if";

	/** @see #getThen() */
	public static final String THEN__PROP = "then";

	/** @see #getElse() */
	public static final String ELSE__PROP = "else";

	/** @see #getDependentSchemas() */
	public static final String DEPENDENT_SCHEMAS__PROP = "dependentSchemas";

	/** @see #getAllOf() */
	public static final String ALL_OF__PROP = "allOf";

	/** @see #getAnyOf() */
	public static final String ANY_OF__PROP = "anyOf";

	/** @see #getOneOf() */
	public static final String ONE_OF__PROP = "oneOf";

	/** @see #getNot() */
	public static final String NOT__PROP = "not";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getDefault() */
	public static final String DEFAULT__PROP = "default";

	/** @see #isDeprecated() */
	public static final String DEPRECATED__PROP = "deprecated";

	/** @see #isReadOnly() */
	public static final String READ_ONLY__PROP = "readOnly";

	/** @see #isWriteOnly() */
	public static final String WRITE_ONLY__PROP = "writeOnly";

	/** @see #getExamples() */
	public static final String EXAMPLES__PROP = "examples";

	/** @see #isIsBooleanSchema() */
	public static final String IS_BOOLEAN_SCHEMA__PROP = "isBooleanSchema";

	/** @see #isBooleanSchemaValue() */
	public static final String BOOLEAN_SCHEMA_VALUE__PROP = "booleanSchemaValue";

	private String _$schema = "";

	private String _$id = "";

	private String _$anchor = "";

	private String _$dynamicAnchor = "";

	private String _$ref = "";

	private String _$dynamicRef = "";

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> _$defs = new java.util.HashMap<>();

	private String _$comment = "";

	private final java.util.Map<String, Boolean> _$vocabulary = new java.util.HashMap<>();

	private final java.util.List<String> _type = new java.util.ArrayList<>();

	private final java.util.List<String> _enum = new java.util.ArrayList<>();

	private String _const = "";

	private double _multipleOf = 0.0d;

	private double _maximum = 0.0d;

	private double _exclusiveMaximum = 0.0d;

	private double _minimum = 0.0d;

	private double _exclusiveMinimum = 0.0d;

	private int _maxLength = 0;

	private int _minLength = 0;

	private String _pattern = "";

	private String _format = "";

	private String _contentEncoding = "";

	private String _contentMediaType = "";

	private com.top_logic.basic.json.schema.model.JsonSchema _contentSchema = null;

	private int _maxItems = 0;

	private int _minItems = 0;

	private boolean _uniqueItems = false;

	private int _maxContains = 0;

	private int _minContains = 0;

	private int _maxProperties = 0;

	private int _minProperties = 0;

	private final java.util.List<String> _required = new java.util.ArrayList<>();

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray> _dependentRequired = new java.util.HashMap<>();

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> _properties = new java.util.HashMap<>();

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> _patternProperties = new java.util.HashMap<>();

	private com.top_logic.basic.json.schema.model.JsonSchema _additionalProperties = null;

	private com.top_logic.basic.json.schema.model.JsonSchema _propertyNames = null;

	private final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> _prefixItems = new java.util.ArrayList<>();

	private com.top_logic.basic.json.schema.model.JsonSchema _items = null;

	private com.top_logic.basic.json.schema.model.JsonSchema _contains = null;

	private com.top_logic.basic.json.schema.model.JsonSchema _if = null;

	private com.top_logic.basic.json.schema.model.JsonSchema _then = null;

	private com.top_logic.basic.json.schema.model.JsonSchema _else = null;

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> _dependentSchemas = new java.util.HashMap<>();

	private final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> _allOf = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> _anyOf = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> _oneOf = new java.util.ArrayList<>();

	private com.top_logic.basic.json.schema.model.JsonSchema _not = null;

	private String _title = "";

	private String _description = "";

	private String _default = "";

	private boolean _deprecated = false;

	private boolean _readOnly = false;

	private boolean _writeOnly = false;

	private final java.util.List<String> _examples = new java.util.ArrayList<>();

	private boolean _isBooleanSchema = false;

	private boolean _booleanSchemaValue = false;

	/**
	 * Creates a {@link JsonSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.JsonSchema#create()
	 */
	protected JsonSchema() {
		super();
	}

	/**
	 * Dialect identifier (e.g., "https://json-schema.org/draft/2020-12/schema").
	 *
	 * <p>
	 * Identifies which JSON Schema dialect this schema follows.
	 * Should be used in the document root schema object.
	 * </p>
	 */
	public final String get$schema() {
		return _$schema;
	}

	/**
	 * @see #get$schema()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$schema(String value) {
		internalSet$schema(value);
		return this;
	}

	/** Internal setter for {@link #get$schema()} without chain call utility. */
	protected final void internalSet$schema(String value) {
		_$schema = value;
	}

	/**
	 * Canonical URI identifying this schema resource.
	 *
	 * <p>
	 * Establishes the base URI for resolving relative references.
	 * Subschemas with $id create distinct schema resources.
	 * </p>
	 */
	public final String get$id() {
		return _$id;
	}

	/**
	 * @see #get$id()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$id(String value) {
		internalSet$id(value);
		return this;
	}

	/** Internal setter for {@link #get$id()} without chain call utility. */
	protected final void internalSet$id(String value) {
		_$id = value;
	}

	/**
	 * Plain name fragment for referencing this schema.
	 *
	 * <p>
	 * Creates location-independent references that survive schema restructuring.
	 * Value must match XML's NCName pattern.
	 * </p>
	 */
	public final String get$anchor() {
		return _$anchor;
	}

	/**
	 * @see #get$anchor()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$anchor(String value) {
		internalSet$anchor(value);
		return this;
	}

	/** Internal setter for {@link #get$anchor()} without chain call utility. */
	protected final void internalSet$anchor(String value) {
		_$anchor = value;
	}

	/**
	 * Dynamic anchor for runtime-resolved references.
	 *
	 * <p>
	 * Works cooperatively with <code>$dynamicRef</code> for recursive schemas.
	 * </p>
	 */
	public final String get$dynamicAnchor() {
		return _$dynamicAnchor;
	}

	/**
	 * @see #get$dynamicAnchor()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$dynamicAnchor(String value) {
		internalSet$dynamicAnchor(value);
		return this;
	}

	/** Internal setter for {@link #get$dynamicAnchor()} without chain call utility. */
	protected final void internalSet$dynamicAnchor(String value) {
		_$dynamicAnchor = value;
	}

	/**
	 * Reference to another schema by URI.
	 *
	 * <p>
	 * Can appear alongside other keywords in the same schema object.
	 * </p>
	 */
	public final String get$ref() {
		return _$ref;
	}

	/**
	 * @see #get$ref()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$ref(String value) {
		internalSet$ref(value);
		return this;
	}

	/** Internal setter for {@link #get$ref()} without chain call utility. */
	protected final void internalSet$ref(String value) {
		_$ref = value;
	}

	/**
	 * Dynamic reference resolved at runtime.
	 *
	 * <p>
	 * Particularly useful for recursive schemas that reference themselves.
	 * Works with <code>$dynamicAnchor</code>.
	 * </p>
	 */
	public final String get$dynamicRef() {
		return _$dynamicRef;
	}

	/**
	 * @see #get$dynamicRef()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$dynamicRef(String value) {
		internalSet$dynamicRef(value);
		return this;
	}

	/** Internal setter for {@link #get$dynamicRef()} without chain call utility. */
	protected final void internalSet$dynamicRef(String value) {
		_$dynamicRef = value;
	}

	/**
	 * Reusable schema definitions.
	 *
	 * <p>
	 * Reserved location for defining schemas that can be referenced via $ref.
	 * Does not directly affect validation.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> get$defs() {
		return _$defs;
	}

	/**
	 * @see #get$defs()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$defs(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSet$defs(value);
		return this;
	}

	/** Internal setter for {@link #get$defs()} without chain call utility. */
	protected final void internalSet$defs(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property '$defs' cannot be null.");
		_$defs.clear();
		_$defs.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #get$defs()} map.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema put$def(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalPut$def(key, value);
		return this;
	}

	/** Implementation of {@link #put$def(String, com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void  internalPut$def(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		if (_$defs.containsKey(key)) {
			throw new IllegalArgumentException("Property '$defs' already contains a value for key '" + key + "'.");
		}
		_$defs.put(key, value);
	}

	/**
	 * Removes a key from the {@link #get$defs()} map.
	 */
	public final void remove$def(String key) {
		_$defs.remove(key);
	}

	/**
	 * Comment for schema authors.
	 *
	 * <p>
	 * Explanatory text that doesn't affect validation or annotation results.
	 * </p>
	 */
	public final String get$comment() {
		return _$comment;
	}

	/**
	 * @see #get$comment()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$comment(String value) {
		internalSet$comment(value);
		return this;
	}

	/** Internal setter for {@link #get$comment()} without chain call utility. */
	protected final void internalSet$comment(String value) {
		_$comment = value;
	}

	/**
	 * Vocabulary declarations (used in meta-schemas only).
	 *
	 * <p>
	 * Maps vocabulary URIs to boolean indicating if required (true) or optional (false).
	 * Only appears in meta-schema root schemas.
	 * </p>
	 */
	public final java.util.Map<String, Boolean> get$vocabulary() {
		return _$vocabulary;
	}

	/**
	 * @see #get$vocabulary()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema set$vocabulary(java.util.Map<String, Boolean> value) {
		internalSet$vocabulary(value);
		return this;
	}

	/** Internal setter for {@link #get$vocabulary()} without chain call utility. */
	protected final void internalSet$vocabulary(java.util.Map<String, Boolean> value) {
		if (value == null) throw new IllegalArgumentException("Property '$vocabulary' cannot be null.");
		_$vocabulary.clear();
		_$vocabulary.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #get$vocabulary()} map.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema put$vocabulary(String key, boolean value) {
		internalPut$vocabulary(key, value);
		return this;
	}

	/** Implementation of {@link #put$vocabulary(String, boolean)} without chain call utility. */
	protected final void  internalPut$vocabulary(String key, boolean value) {
		if (_$vocabulary.containsKey(key)) {
			throw new IllegalArgumentException("Property '$vocabulary' already contains a value for key '" + key + "'.");
		}
		_$vocabulary.put(key, value);
	}

	/**
	 * Removes a key from the {@link #get$vocabulary()} map.
	 */
	public final void remove$vocabulary(String key) {
		_$vocabulary.remove(key);
	}

	/**
	 * Instance type constraint.
	 *
	 * <p>
	 * Can be a single type or array of types: "null", "boolean", "object", "array",
	 * "number", "string", or "integer".
	 * </p>
	 */
	public final java.util.List<String> getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setType(java.util.List<? extends String> value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(java.util.List<? extends String> value) {
		_type.clear();
		_type.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getType()} list.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema addType(String value) {
		internalAddType(value);
		return this;
	}

	/** Implementation of {@link #addType(String)} without chain call utility. */
	protected final void internalAddType(String value) {
		_type.add(value);
	}

	/**
	 * Removes a value from the {@link #getType()} list.
	 */
	public final void removeType(String value) {
		_type.remove(value);
	}

	/**
	 * Enumeration of allowed values.
	 *
	 * <p>
	 * Instance must equal one of the values in this array.
	 * Values are JSON-serialized strings.
	 * </p>
	 */
	public final java.util.List<String> getEnum() {
		return _enum;
	}

	/**
	 * @see #getEnum()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setEnum(java.util.List<? extends String> value) {
		internalSetEnum(value);
		return this;
	}

	/** Internal setter for {@link #getEnum()} without chain call utility. */
	protected final void internalSetEnum(java.util.List<? extends String> value) {
		_enum.clear();
		_enum.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getEnum()} list.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema addEnum(String value) {
		internalAddEnum(value);
		return this;
	}

	/** Implementation of {@link #addEnum(String)} without chain call utility. */
	protected final void internalAddEnum(String value) {
		_enum.add(value);
	}

	/**
	 * Removes a value from the {@link #getEnum()} list.
	 */
	public final void removeEnum(String value) {
		_enum.remove(value);
	}

	/**
	 * Constant value constraint.
	 *
	 * <p>
	 * Instance must equal exactly this value.
	 * Value is JSON-serialized string.
	 * </p>
	 */
	public final String getConst() {
		return _const;
	}

	/**
	 * @see #getConst()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setConst(String value) {
		internalSetConst(value);
		return this;
	}

	/** Internal setter for {@link #getConst()} without chain call utility. */
	protected final void internalSetConst(String value) {
		_const = value;
	}

	/**
	 * Multiple-of constraint for numeric values.
	 *
	 * <p>
	 * Valid only if division by this value results in an integer.
	 * </p>
	 */
	public final double getMultipleOf() {
		return _multipleOf;
	}

	/**
	 * @see #getMultipleOf()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMultipleOf(double value) {
		internalSetMultipleOf(value);
		return this;
	}

	/** Internal setter for {@link #getMultipleOf()} without chain call utility. */
	protected final void internalSetMultipleOf(double value) {
		_multipleOf = value;
	}

	/**
	 * Maximum value (inclusive).
	 */
	public final double getMaximum() {
		return _maximum;
	}

	/**
	 * @see #getMaximum()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMaximum(double value) {
		internalSetMaximum(value);
		return this;
	}

	/** Internal setter for {@link #getMaximum()} without chain call utility. */
	protected final void internalSetMaximum(double value) {
		_maximum = value;
	}

	/**
	 * Exclusive maximum value.
	 *
	 * <p>
	 * Valid only if strictly less than this value.
	 * </p>
	 */
	public final double getExclusiveMaximum() {
		return _exclusiveMaximum;
	}

	/**
	 * @see #getExclusiveMaximum()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setExclusiveMaximum(double value) {
		internalSetExclusiveMaximum(value);
		return this;
	}

	/** Internal setter for {@link #getExclusiveMaximum()} without chain call utility. */
	protected final void internalSetExclusiveMaximum(double value) {
		_exclusiveMaximum = value;
	}

	/**
	 * Minimum value (inclusive).
	 */
	public final double getMinimum() {
		return _minimum;
	}

	/**
	 * @see #getMinimum()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMinimum(double value) {
		internalSetMinimum(value);
		return this;
	}

	/** Internal setter for {@link #getMinimum()} without chain call utility. */
	protected final void internalSetMinimum(double value) {
		_minimum = value;
	}

	/**
	 * Exclusive minimum value.
	 *
	 * <p>
	 * Valid only if strictly greater than this value.
	 * </p>
	 */
	public final double getExclusiveMinimum() {
		return _exclusiveMinimum;
	}

	/**
	 * @see #getExclusiveMinimum()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setExclusiveMinimum(double value) {
		internalSetExclusiveMinimum(value);
		return this;
	}

	/** Internal setter for {@link #getExclusiveMinimum()} without chain call utility. */
	protected final void internalSetExclusiveMinimum(double value) {
		_exclusiveMinimum = value;
	}

	/**
	 * Maximum string length.
	 *
	 * <p>
	 * String length must be less than or equal to this value.
	 * </p>
	 */
	public final int getMaxLength() {
		return _maxLength;
	}

	/**
	 * @see #getMaxLength()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMaxLength(int value) {
		internalSetMaxLength(value);
		return this;
	}

	/** Internal setter for {@link #getMaxLength()} without chain call utility. */
	protected final void internalSetMaxLength(int value) {
		_maxLength = value;
	}

	/**
	 * Minimum string length.
	 *
	 * <p>
	 * String length must be greater than or equal to this value.
	 * </p>
	 */
	public final int getMinLength() {
		return _minLength;
	}

	/**
	 * @see #getMinLength()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMinLength(int value) {
		internalSetMinLength(value);
		return this;
	}

	/** Internal setter for {@link #getMinLength()} without chain call utility. */
	protected final void internalSetMinLength(int value) {
		_minLength = value;
	}

	/**
	 * Regular expression pattern.
	 *
	 * <p>
	 * String must match this ECMA-262 regular expression.
	 * </p>
	 */
	public final String getPattern() {
		return _pattern;
	}

	/**
	 * @see #getPattern()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setPattern(String value) {
		internalSetPattern(value);
		return this;
	}

	/** Internal setter for {@link #getPattern()} without chain call utility. */
	protected final void internalSetPattern(String value) {
		_pattern = value;
	}

	/**
	 * Format annotation for semantic validation.
	 *
	 * <p>
	 * Provides hints for specific string patterns: date-time, email, hostname,
	 * ipv4, ipv6, uri, uuid, etc.
	 * </p>
	 */
	public final String getFormat() {
		return _format;
	}

	/**
	 * @see #getFormat()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setFormat(String value) {
		internalSetFormat(value);
		return this;
	}

	/** Internal setter for {@link #getFormat()} without chain call utility. */
	protected final void internalSetFormat(String value) {
		_format = value;
	}

	/**
	 * Content encoding for string-encoded data.
	 *
	 * <p>
	 * Specifies encoding like base64, base32, etc.
	 * </p>
	 */
	public final String getContentEncoding() {
		return _contentEncoding;
	}

	/**
	 * @see #getContentEncoding()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setContentEncoding(String value) {
		internalSetContentEncoding(value);
		return this;
	}

	/** Internal setter for {@link #getContentEncoding()} without chain call utility. */
	protected final void internalSetContentEncoding(String value) {
		_contentEncoding = value;
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
	public com.top_logic.basic.json.schema.model.JsonSchema setContentMediaType(String value) {
		internalSetContentMediaType(value);
		return this;
	}

	/** Internal setter for {@link #getContentMediaType()} without chain call utility. */
	protected final void internalSetContentMediaType(String value) {
		_contentMediaType = value;
	}

	/**
	 * Schema validating decoded content.
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getContentSchema() {
		return _contentSchema;
	}

	/**
	 * @see #getContentSchema()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setContentSchema(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetContentSchema(value);
		return this;
	}

	/** Internal setter for {@link #getContentSchema()} without chain call utility. */
	protected final void internalSetContentSchema(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_contentSchema = value;
	}

	/**
	 * Checks, whether {@link #getContentSchema()} has a value.
	 */
	public final boolean hasContentSchema() {
		return _contentSchema != null;
	}

	/**
	 * Maximum array size.
	 *
	 * <p>
	 * Array size cannot exceed this limit.
	 * </p>
	 */
	public final int getMaxItems() {
		return _maxItems;
	}

	/**
	 * @see #getMaxItems()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMaxItems(int value) {
		internalSetMaxItems(value);
		return this;
	}

	/** Internal setter for {@link #getMaxItems()} without chain call utility. */
	protected final void internalSetMaxItems(int value) {
		_maxItems = value;
	}

	/**
	 * Minimum array size.
	 *
	 * <p>
	 * Array size must meet or exceed this limit.
	 * </p>
	 */
	public final int getMinItems() {
		return _minItems;
	}

	/**
	 * @see #getMinItems()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMinItems(int value) {
		internalSetMinItems(value);
		return this;
	}

	/** Internal setter for {@link #getMinItems()} without chain call utility. */
	protected final void internalSetMinItems(int value) {
		_minItems = value;
	}

	/**
	 * Unique items constraint.
	 *
	 * <p>
	 * If true, all array elements must be distinct.
	 * </p>
	 */
	public final boolean isUniqueItems() {
		return _uniqueItems;
	}

	/**
	 * @see #isUniqueItems()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setUniqueItems(boolean value) {
		internalSetUniqueItems(value);
		return this;
	}

	/** Internal setter for {@link #isUniqueItems()} without chain call utility. */
	protected final void internalSetUniqueItems(boolean value) {
		_uniqueItems = value;
	}

	/**
	 * Maximum number of items matching contains schema.
	 */
	public final int getMaxContains() {
		return _maxContains;
	}

	/**
	 * @see #getMaxContains()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMaxContains(int value) {
		internalSetMaxContains(value);
		return this;
	}

	/** Internal setter for {@link #getMaxContains()} without chain call utility. */
	protected final void internalSetMaxContains(int value) {
		_maxContains = value;
	}

	/**
	 * Minimum number of items matching contains schema.
	 */
	public final int getMinContains() {
		return _minContains;
	}

	/**
	 * @see #getMinContains()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMinContains(int value) {
		internalSetMinContains(value);
		return this;
	}

	/** Internal setter for {@link #getMinContains()} without chain call utility. */
	protected final void internalSetMinContains(int value) {
		_minContains = value;
	}

	/**
	 * Maximum number of properties.
	 *
	 * <p>
	 * Object property count upper limit.
	 * </p>
	 */
	public final int getMaxProperties() {
		return _maxProperties;
	}

	/**
	 * @see #getMaxProperties()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMaxProperties(int value) {
		internalSetMaxProperties(value);
		return this;
	}

	/** Internal setter for {@link #getMaxProperties()} without chain call utility. */
	protected final void internalSetMaxProperties(int value) {
		_maxProperties = value;
	}

	/**
	 * Minimum number of properties.
	 *
	 * <p>
	 * Object property count lower limit.
	 * </p>
	 */
	public final int getMinProperties() {
		return _minProperties;
	}

	/**
	 * @see #getMinProperties()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setMinProperties(int value) {
		internalSetMinProperties(value);
		return this;
	}

	/** Internal setter for {@link #getMinProperties()} without chain call utility. */
	protected final void internalSetMinProperties(int value) {
		_minProperties = value;
	}

	/**
	 * Required property names.
	 *
	 * <p>
	 * Every name in this array must be a property in the instance.
	 * </p>
	 */
	public final java.util.List<String> getRequired() {
		return _required;
	}

	/**
	 * @see #getRequired()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setRequired(java.util.List<? extends String> value) {
		internalSetRequired(value);
		return this;
	}

	/** Internal setter for {@link #getRequired()} without chain call utility. */
	protected final void internalSetRequired(java.util.List<? extends String> value) {
		_required.clear();
		_required.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getRequired()} list.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema addRequired(String value) {
		internalAddRequired(value);
		return this;
	}

	/** Implementation of {@link #addRequired(String)} without chain call utility. */
	protected final void internalAddRequired(String value) {
		_required.add(value);
	}

	/**
	 * Removes a value from the {@link #getRequired()} list.
	 */
	public final void removeRequired(String value) {
		_required.remove(value);
	}

	/**
	 * Conditional required properties.
	 *
	 * <p>
	 * Maps property names to arrays of required properties when that property is present.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray> getDependentRequired() {
		return _dependentRequired;
	}

	/**
	 * @see #getDependentRequired()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setDependentRequired(java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray> value) {
		internalSetDependentRequired(value);
		return this;
	}

	/** Internal setter for {@link #getDependentRequired()} without chain call utility. */
	protected final void internalSetDependentRequired(java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray> value) {
		if (value == null) throw new IllegalArgumentException("Property 'dependentRequired' cannot be null.");
		_dependentRequired.clear();
		_dependentRequired.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getDependentRequired()} map.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema putDependentRequired(String key, com.top_logic.basic.json.schema.model.StringArray value) {
		internalPutDependentRequired(key, value);
		return this;
	}

	/** Implementation of {@link #putDependentRequired(String, com.top_logic.basic.json.schema.model.StringArray)} without chain call utility. */
	protected final void  internalPutDependentRequired(String key, com.top_logic.basic.json.schema.model.StringArray value) {
		if (_dependentRequired.containsKey(key)) {
			throw new IllegalArgumentException("Property 'dependentRequired' already contains a value for key '" + key + "'.");
		}
		_dependentRequired.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getDependentRequired()} map.
	 */
	public final void removeDependentRequired(String key) {
		_dependentRequired.remove(key);
	}

	/**
	 * Schema for object properties by name.
	 *
	 * <p>
	 * Maps property names to their schemas.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> getProperties() {
		return _properties;
	}

	/**
	 * @see #getProperties()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSetProperties(value);
		return this;
	}

	/** Internal setter for {@link #getProperties()} without chain call utility. */
	protected final void internalSetProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'properties' cannot be null.");
		_properties.clear();
		_properties.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getProperties()} map.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema putProperty(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalputProperty(key, value);
		return this;
	}

	/** Implementation of {@link #putProperty(String, com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void  internalputProperty(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		if (_properties.containsKey(key)) {
			throw new IllegalArgumentException("Property 'properties' already contains a value for key '" + key + "'.");
		}
		_properties.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getProperties()} map.
	 */
	public final void removePropertie(String key) {
		_properties.remove(key);
	}

	/**
	 * Schema for properties matching regex patterns.
	 *
	 * <p>
	 * Maps ECMA-262 regex patterns to schemas for matching properties.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> getPatternProperties() {
		return _patternProperties;
	}

	/**
	 * @see #getPatternProperties()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setPatternProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSetPatternProperties(value);
		return this;
	}

	/** Internal setter for {@link #getPatternProperties()} without chain call utility. */
	protected final void internalSetPatternProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'patternProperties' cannot be null.");
		_patternProperties.clear();
		_patternProperties.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getPatternProperties()} map.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema putPatternPropertie(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalPutPatternPropertie(key, value);
		return this;
	}

	/** Implementation of {@link #putPatternPropertie(String, com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void  internalPutPatternPropertie(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		if (_patternProperties.containsKey(key)) {
			throw new IllegalArgumentException("Property 'patternProperties' already contains a value for key '" + key + "'.");
		}
		_patternProperties.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getPatternProperties()} map.
	 */
	public final void removePatternPropertie(String key) {
		_patternProperties.remove(key);
	}

	/**
	 * Schema for additional properties not defined in <code>properties</code> or <code>patternProperties</code>.
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getAdditionalProperties() {
		return _additionalProperties;
	}

	/**
	 * @see #getAdditionalProperties()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setAdditionalProperties(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetAdditionalProperties(value);
		return this;
	}

	/** Internal setter for {@link #getAdditionalProperties()} without chain call utility. */
	protected final void internalSetAdditionalProperties(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_additionalProperties = value;
	}

	/**
	 * Checks, whether {@link #getAdditionalProperties()} has a value.
	 */
	public final boolean hasAdditionalProperties() {
		return _additionalProperties != null;
	}

	/**
	 * Schema for property names.
	 *
	 * <p>
	 * All property names must validate against this schema.
	 * </p>
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getPropertyNames() {
		return _propertyNames;
	}

	/**
	 * @see #getPropertyNames()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setPropertyNames(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetPropertyNames(value);
		return this;
	}

	/** Internal setter for {@link #getPropertyNames()} without chain call utility. */
	protected final void internalSetPropertyNames(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_propertyNames = value;
	}

	/**
	 * Checks, whether {@link #getPropertyNames()} has a value.
	 */
	public final boolean hasPropertyNames() {
		return _propertyNames != null;
	}

	/**
	 * Positional schemas for array items.
	 *
	 * <p>
	 * Applies schemas to array elements by position.
	 * Array at index i validated against schema at position i.
	 * </p>
	 */
	public final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> getPrefixItems() {
		return _prefixItems;
	}

	/**
	 * @see #getPrefixItems()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setPrefixItems(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSetPrefixItems(value);
		return this;
	}

	/** Internal setter for {@link #getPrefixItems()} without chain call utility. */
	protected final void internalSetPrefixItems(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'prefixItems' cannot be null.");
		_prefixItems.clear();
		_prefixItems.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getPrefixItems()} list.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema addPrefixItem(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalAddPrefixItem(value);
		return this;
	}

	/** Implementation of {@link #addPrefixItem(com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void internalAddPrefixItem(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_prefixItems.add(value);
	}

	/**
	 * Removes a value from the {@link #getPrefixItems()} list.
	 */
	public final void removePrefixItem(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_prefixItems.remove(value);
	}

	/**
	 * Schema for array items.
	 *
	 * <p>
	 * For arrays without <code>prefixItems</code>: validates all items.
	 * For arrays with <code>prefixItems</code>: validates items beyond <code>prefixItems</code> positions.
	 * </p>
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getItems() {
		return _items;
	}

	/**
	 * @see #getItems()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setItems(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetItems(value);
		return this;
	}

	/** Internal setter for {@link #getItems()} without chain call utility. */
	protected final void internalSetItems(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_items = value;
	}

	/**
	 * Checks, whether {@link #getItems()} has a value.
	 */
	public final boolean hasItems() {
		return _items != null;
	}

	/**
	 * Contains constraint schema.
	 *
	 * <p>
	 * Array must contain at least one item matching this schema.
	 * Works with <code>minContains</code> and <code>maxContains</code>.
	 * </p>
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getContains() {
		return _contains;
	}

	/**
	 * @see #getContains()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setContains(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetContains(value);
		return this;
	}

	/** Internal setter for {@link #getContains()} without chain call utility. */
	protected final void internalSetContains(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_contains = value;
	}

	/**
	 * Checks, whether {@link #getContains()} has a value.
	 */
	public final boolean hasContains() {
		return _contains != null;
	}

	/**
	 * Condition schema for if/then/else logic.
	 *
	 * <p>
	 * If instance validates against this schema, 'then' is applied, otherwise 'else'.
	 * </p>
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getIf() {
		return _if;
	}

	/**
	 * @see #getIf()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setIf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetIf(value);
		return this;
	}

	/** Internal setter for {@link #getIf()} without chain call utility. */
	protected final void internalSetIf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_if = value;
	}

	/**
	 * Checks, whether {@link #getIf()} has a value.
	 */
	public final boolean hasIf() {
		return _if != null;
	}

	/**
	 * Schema applied when 'if' validates successfully.
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getThen() {
		return _then;
	}

	/**
	 * @see #getThen()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setThen(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetThen(value);
		return this;
	}

	/** Internal setter for {@link #getThen()} without chain call utility. */
	protected final void internalSetThen(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_then = value;
	}

	/**
	 * Checks, whether {@link #getThen()} has a value.
	 */
	public final boolean hasThen() {
		return _then != null;
	}

	/**
	 * Schema applied when 'if' validation fails.
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getElse() {
		return _else;
	}

	/**
	 * @see #getElse()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setElse(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetElse(value);
		return this;
	}

	/** Internal setter for {@link #getElse()} without chain call utility. */
	protected final void internalSetElse(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_else = value;
	}

	/**
	 * Checks, whether {@link #getElse()} has a value.
	 */
	public final boolean hasElse() {
		return _else != null;
	}

	/**
	 * Dependent schemas by property.
	 *
	 * <p>
	 * When a property name from this map is present in the instance,
	 * the corresponding schema is applied.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> getDependentSchemas() {
		return _dependentSchemas;
	}

	/**
	 * @see #getDependentSchemas()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setDependentSchemas(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSetDependentSchemas(value);
		return this;
	}

	/** Internal setter for {@link #getDependentSchemas()} without chain call utility. */
	protected final void internalSetDependentSchemas(java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'dependentSchemas' cannot be null.");
		_dependentSchemas.clear();
		_dependentSchemas.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getDependentSchemas()} map.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema putDependentSchema(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalPutDependentSchema(key, value);
		return this;
	}

	/** Implementation of {@link #putDependentSchema(String, com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void  internalPutDependentSchema(String key, com.top_logic.basic.json.schema.model.JsonSchema value) {
		if (_dependentSchemas.containsKey(key)) {
			throw new IllegalArgumentException("Property 'dependentSchemas' already contains a value for key '" + key + "'.");
		}
		_dependentSchemas.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getDependentSchemas()} map.
	 */
	public final void removeDependentSchema(String key) {
		_dependentSchemas.remove(key);
	}

	/**
	 * All-of composition.
	 *
	 * <p>
	 * Instance must validate against ALL schemas in this array.
	 * </p>
	 */
	public final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> getAllOf() {
		return _allOf;
	}

	/**
	 * @see #getAllOf()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setAllOf(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSetAllOf(value);
		return this;
	}

	/** Internal setter for {@link #getAllOf()} without chain call utility. */
	protected final void internalSetAllOf(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'allOf' cannot be null.");
		_allOf.clear();
		_allOf.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAllOf()} list.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema addAllOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalAddAllOf(value);
		return this;
	}

	/** Implementation of {@link #addAllOf(com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void internalAddAllOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_allOf.add(value);
	}

	/**
	 * Removes a value from the {@link #getAllOf()} list.
	 */
	public final void removeAllOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_allOf.remove(value);
	}

	/**
	 * Any-of composition.
	 *
	 * <p>
	 * Instance must validate against AT LEAST ONE schema in this array.
	 * </p>
	 */
	public final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> getAnyOf() {
		return _anyOf;
	}

	/**
	 * @see #getAnyOf()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setAnyOf(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSetAnyOf(value);
		return this;
	}

	/** Internal setter for {@link #getAnyOf()} without chain call utility. */
	protected final void internalSetAnyOf(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'anyOf' cannot be null.");
		_anyOf.clear();
		_anyOf.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAnyOf()} list.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema addAnyOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalAddAnyOf(value);
		return this;
	}

	/** Implementation of {@link #addAnyOf(com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void internalAddAnyOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_anyOf.add(value);
	}

	/**
	 * Removes a value from the {@link #getAnyOf()} list.
	 */
	public final void removeAnyOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_anyOf.remove(value);
	}

	/**
	 * One-of composition.
	 *
	 * <p>
	 * Instance must validate against EXACTLY ONE schema in this array.
	 * </p>
	 */
	public final java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> getOneOf() {
		return _oneOf;
	}

	/**
	 * @see #getOneOf()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setOneOf(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		internalSetOneOf(value);
		return this;
	}

	/** Internal setter for {@link #getOneOf()} without chain call utility. */
	protected final void internalSetOneOf(java.util.List<? extends com.top_logic.basic.json.schema.model.JsonSchema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'oneOf' cannot be null.");
		_oneOf.clear();
		_oneOf.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getOneOf()} list.
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema addOneOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalAddOneOf(value);
		return this;
	}

	/** Implementation of {@link #addOneOf(com.top_logic.basic.json.schema.model.JsonSchema)} without chain call utility. */
	protected final void internalAddOneOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_oneOf.add(value);
	}

	/**
	 * Removes a value from the {@link #getOneOf()} list.
	 */
	public final void removeOneOf(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_oneOf.remove(value);
	}

	/**
	 * Negation.
	 *
	 * <p>
	 * Instance must NOT validate against this schema.
	 * </p>
	 */
	public final com.top_logic.basic.json.schema.model.JsonSchema getNot() {
		return _not;
	}

	/**
	 * @see #getNot()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setNot(com.top_logic.basic.json.schema.model.JsonSchema value) {
		internalSetNot(value);
		return this;
	}

	/** Internal setter for {@link #getNot()} without chain call utility. */
	protected final void internalSetNot(com.top_logic.basic.json.schema.model.JsonSchema value) {
		_not = value;
	}

	/**
	 * Checks, whether {@link #getNot()} has a value.
	 */
	public final boolean hasNot() {
		return _not != null;
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
	public com.top_logic.basic.json.schema.model.JsonSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
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
	public com.top_logic.basic.json.schema.model.JsonSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Default value.
	 *
	 * <p>
	 * Value is JSON-serialized string.
	 * </p>
	 */
	public final String getDefault() {
		return _default;
	}

	/**
	 * @see #getDefault()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setDefault(String value) {
		internalSetDefault(value);
		return this;
	}

	/** Internal setter for {@link #getDefault()} without chain call utility. */
	protected final void internalSetDefault(String value) {
		_default = value;
	}

	/**
	 * Deprecated flag.
	 *
	 * <p>
	 * Marks schema as obsolete.
	 * </p>
	 */
	public final boolean isDeprecated() {
		return _deprecated;
	}

	/**
	 * @see #isDeprecated()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	/** Internal setter for {@link #isDeprecated()} without chain call utility. */
	protected final void internalSetDeprecated(boolean value) {
		_deprecated = value;
	}

	/**
	 * Read-only flag.
	 *
	 * <p>
	 * Access control metadata for applications.
	 * </p>
	 */
	public final boolean isReadOnly() {
		return _readOnly;
	}

	/**
	 * @see #isReadOnly()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	/** Internal setter for {@link #isReadOnly()} without chain call utility. */
	protected final void internalSetReadOnly(boolean value) {
		_readOnly = value;
	}

	/**
	 * Write-only flag.
	 *
	 * <p>
	 * Access control metadata for applications.
	 * </p>
	 */
	public final boolean isWriteOnly() {
		return _writeOnly;
	}

	/**
	 * @see #isWriteOnly()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	/** Internal setter for {@link #isWriteOnly()} without chain call utility. */
	protected final void internalSetWriteOnly(boolean value) {
		_writeOnly = value;
	}

	/**
	 * Example values.
	 *
	 * <p>
	 * Array of sample values demonstrating proper usage.
	 * Values are JSON-serialized strings.
	 * </p>
	 */
	public final java.util.List<String> getExamples() {
		return _examples;
	}

	/**
	 * @see #getExamples()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setExamples(java.util.List<? extends String> value) {
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
	public com.top_logic.basic.json.schema.model.JsonSchema addExample(String value) {
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

	/**
	 * Boolean schema flag.
	 *
	 * <p>
	 * When true: represents a boolean schema (always pass or always fail).
	 * - <code>booleanSchemaValue</code> = true: schema always passes (equivalent to {})
	 * - <code>booleanSchemaValue</code> = false: schema always fails (equivalent to {"not": {}})
	 * When false: this is a regular object schema (default).
	 * </p>
	 */
	public final boolean isIsBooleanSchema() {
		return _isBooleanSchema;
	}

	/**
	 * @see #isIsBooleanSchema()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setIsBooleanSchema(boolean value) {
		internalSetIsBooleanSchema(value);
		return this;
	}

	/** Internal setter for {@link #isIsBooleanSchema()} without chain call utility. */
	protected final void internalSetIsBooleanSchema(boolean value) {
		_isBooleanSchema = value;
	}

	/**
	 * Boolean schema value (true = pass, false = fail).
	 *
	 * <p>
	 * Only meaningful when <code>isBooleanSchema</code> is true.
	 * </p>
	 */
	public final boolean isBooleanSchemaValue() {
		return _booleanSchemaValue;
	}

	/**
	 * @see #isBooleanSchemaValue()
	 */
	public com.top_logic.basic.json.schema.model.JsonSchema setBooleanSchemaValue(boolean value) {
		internalSetBooleanSchemaValue(value);
		return this;
	}

	/** Internal setter for {@link #isBooleanSchemaValue()} without chain call utility. */
	protected final void internalSetBooleanSchemaValue(boolean value) {
		_booleanSchemaValue = value;
	}

	@Override
	public String jsonType() {
		return JSON_SCHEMA__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			$SCHEMA__PROP, 
			$ID__PROP, 
			$ANCHOR__PROP, 
			$DYNAMIC_ANCHOR__PROP, 
			$REF__PROP, 
			$DYNAMIC_REF__PROP, 
			$DEFS__PROP, 
			$COMMENT__PROP, 
			$VOCABULARY__PROP, 
			TYPE__PROP, 
			ENUM__PROP, 
			CONST__PROP, 
			MULTIPLE_OF__PROP, 
			MAXIMUM__PROP, 
			EXCLUSIVE_MAXIMUM__PROP, 
			MINIMUM__PROP, 
			EXCLUSIVE_MINIMUM__PROP, 
			MAX_LENGTH__PROP, 
			MIN_LENGTH__PROP, 
			PATTERN__PROP, 
			FORMAT__PROP, 
			CONTENT_ENCODING__PROP, 
			CONTENT_MEDIA_TYPE__PROP, 
			CONTENT_SCHEMA__PROP, 
			MAX_ITEMS__PROP, 
			MIN_ITEMS__PROP, 
			UNIQUE_ITEMS__PROP, 
			MAX_CONTAINS__PROP, 
			MIN_CONTAINS__PROP, 
			MAX_PROPERTIES__PROP, 
			MIN_PROPERTIES__PROP, 
			REQUIRED__PROP, 
			DEPENDENT_REQUIRED__PROP, 
			PROPERTIES__PROP, 
			PATTERN_PROPERTIES__PROP, 
			ADDITIONAL_PROPERTIES__PROP, 
			PROPERTY_NAMES__PROP, 
			PREFIX_ITEMS__PROP, 
			ITEMS__PROP, 
			CONTAINS__PROP, 
			IF__PROP, 
			THEN__PROP, 
			ELSE__PROP, 
			DEPENDENT_SCHEMAS__PROP, 
			ALL_OF__PROP, 
			ANY_OF__PROP, 
			ONE_OF__PROP, 
			NOT__PROP, 
			TITLE__PROP, 
			DESCRIPTION__PROP, 
			DEFAULT__PROP, 
			DEPRECATED__PROP, 
			READ_ONLY__PROP, 
			WRITE_ONLY__PROP, 
			EXAMPLES__PROP, 
			IS_BOOLEAN_SCHEMA__PROP, 
			BOOLEAN_SCHEMA_VALUE__PROP);
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
			case $SCHEMA__PROP: return get$schema();
			case $ID__PROP: return get$id();
			case $ANCHOR__PROP: return get$anchor();
			case $DYNAMIC_ANCHOR__PROP: return get$dynamicAnchor();
			case $REF__PROP: return get$ref();
			case $DYNAMIC_REF__PROP: return get$dynamicRef();
			case $DEFS__PROP: return get$defs();
			case $COMMENT__PROP: return get$comment();
			case $VOCABULARY__PROP: return get$vocabulary();
			case TYPE__PROP: return getType();
			case ENUM__PROP: return getEnum();
			case CONST__PROP: return getConst();
			case MULTIPLE_OF__PROP: return getMultipleOf();
			case MAXIMUM__PROP: return getMaximum();
			case EXCLUSIVE_MAXIMUM__PROP: return getExclusiveMaximum();
			case MINIMUM__PROP: return getMinimum();
			case EXCLUSIVE_MINIMUM__PROP: return getExclusiveMinimum();
			case MAX_LENGTH__PROP: return getMaxLength();
			case MIN_LENGTH__PROP: return getMinLength();
			case PATTERN__PROP: return getPattern();
			case FORMAT__PROP: return getFormat();
			case CONTENT_ENCODING__PROP: return getContentEncoding();
			case CONTENT_MEDIA_TYPE__PROP: return getContentMediaType();
			case CONTENT_SCHEMA__PROP: return getContentSchema();
			case MAX_ITEMS__PROP: return getMaxItems();
			case MIN_ITEMS__PROP: return getMinItems();
			case UNIQUE_ITEMS__PROP: return isUniqueItems();
			case MAX_CONTAINS__PROP: return getMaxContains();
			case MIN_CONTAINS__PROP: return getMinContains();
			case MAX_PROPERTIES__PROP: return getMaxProperties();
			case MIN_PROPERTIES__PROP: return getMinProperties();
			case REQUIRED__PROP: return getRequired();
			case DEPENDENT_REQUIRED__PROP: return getDependentRequired();
			case PROPERTIES__PROP: return getProperties();
			case PATTERN_PROPERTIES__PROP: return getPatternProperties();
			case ADDITIONAL_PROPERTIES__PROP: return getAdditionalProperties();
			case PROPERTY_NAMES__PROP: return getPropertyNames();
			case PREFIX_ITEMS__PROP: return getPrefixItems();
			case ITEMS__PROP: return getItems();
			case CONTAINS__PROP: return getContains();
			case IF__PROP: return getIf();
			case THEN__PROP: return getThen();
			case ELSE__PROP: return getElse();
			case DEPENDENT_SCHEMAS__PROP: return getDependentSchemas();
			case ALL_OF__PROP: return getAllOf();
			case ANY_OF__PROP: return getAnyOf();
			case ONE_OF__PROP: return getOneOf();
			case NOT__PROP: return getNot();
			case TITLE__PROP: return getTitle();
			case DESCRIPTION__PROP: return getDescription();
			case DEFAULT__PROP: return getDefault();
			case DEPRECATED__PROP: return isDeprecated();
			case READ_ONLY__PROP: return isReadOnly();
			case WRITE_ONLY__PROP: return isWriteOnly();
			case EXAMPLES__PROP: return getExamples();
			case IS_BOOLEAN_SCHEMA__PROP: return isIsBooleanSchema();
			case BOOLEAN_SCHEMA_VALUE__PROP: return isBooleanSchemaValue();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case $SCHEMA__PROP: internalSet$schema((String) value); break;
			case $ID__PROP: internalSet$id((String) value); break;
			case $ANCHOR__PROP: internalSet$anchor((String) value); break;
			case $DYNAMIC_ANCHOR__PROP: internalSet$dynamicAnchor((String) value); break;
			case $REF__PROP: internalSet$ref((String) value); break;
			case $DYNAMIC_REF__PROP: internalSet$dynamicRef((String) value); break;
			case $DEFS__PROP: internalSet$defs((java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema>) value); break;
			case $COMMENT__PROP: internalSet$comment((String) value); break;
			case $VOCABULARY__PROP: internalSet$vocabulary((java.util.Map<String, Boolean>) value); break;
			case TYPE__PROP: internalSetType(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case ENUM__PROP: internalSetEnum(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case CONST__PROP: internalSetConst((String) value); break;
			case MULTIPLE_OF__PROP: internalSetMultipleOf((double) value); break;
			case MAXIMUM__PROP: internalSetMaximum((double) value); break;
			case EXCLUSIVE_MAXIMUM__PROP: internalSetExclusiveMaximum((double) value); break;
			case MINIMUM__PROP: internalSetMinimum((double) value); break;
			case EXCLUSIVE_MINIMUM__PROP: internalSetExclusiveMinimum((double) value); break;
			case MAX_LENGTH__PROP: internalSetMaxLength((int) value); break;
			case MIN_LENGTH__PROP: internalSetMinLength((int) value); break;
			case PATTERN__PROP: internalSetPattern((String) value); break;
			case FORMAT__PROP: internalSetFormat((String) value); break;
			case CONTENT_ENCODING__PROP: internalSetContentEncoding((String) value); break;
			case CONTENT_MEDIA_TYPE__PROP: internalSetContentMediaType((String) value); break;
			case CONTENT_SCHEMA__PROP: internalSetContentSchema((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case MAX_ITEMS__PROP: internalSetMaxItems((int) value); break;
			case MIN_ITEMS__PROP: internalSetMinItems((int) value); break;
			case UNIQUE_ITEMS__PROP: internalSetUniqueItems((boolean) value); break;
			case MAX_CONTAINS__PROP: internalSetMaxContains((int) value); break;
			case MIN_CONTAINS__PROP: internalSetMinContains((int) value); break;
			case MAX_PROPERTIES__PROP: internalSetMaxProperties((int) value); break;
			case MIN_PROPERTIES__PROP: internalSetMinProperties((int) value); break;
			case REQUIRED__PROP: internalSetRequired(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case DEPENDENT_REQUIRED__PROP: internalSetDependentRequired((java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray>) value); break;
			case PROPERTIES__PROP: internalSetProperties((java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema>) value); break;
			case PATTERN_PROPERTIES__PROP: internalSetPatternProperties((java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema>) value); break;
			case ADDITIONAL_PROPERTIES__PROP: internalSetAdditionalProperties((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case PROPERTY_NAMES__PROP: internalSetPropertyNames((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case PREFIX_ITEMS__PROP: internalSetPrefixItems(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.json.schema.model.JsonSchema.class, value)); break;
			case ITEMS__PROP: internalSetItems((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case CONTAINS__PROP: internalSetContains((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case IF__PROP: internalSetIf((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case THEN__PROP: internalSetThen((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case ELSE__PROP: internalSetElse((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case DEPENDENT_SCHEMAS__PROP: internalSetDependentSchemas((java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema>) value); break;
			case ALL_OF__PROP: internalSetAllOf(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.json.schema.model.JsonSchema.class, value)); break;
			case ANY_OF__PROP: internalSetAnyOf(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.json.schema.model.JsonSchema.class, value)); break;
			case ONE_OF__PROP: internalSetOneOf(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.json.schema.model.JsonSchema.class, value)); break;
			case NOT__PROP: internalSetNot((com.top_logic.basic.json.schema.model.JsonSchema) value); break;
			case TITLE__PROP: internalSetTitle((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case DEFAULT__PROP: internalSetDefault((String) value); break;
			case DEPRECATED__PROP: internalSetDeprecated((boolean) value); break;
			case READ_ONLY__PROP: internalSetReadOnly((boolean) value); break;
			case WRITE_ONLY__PROP: internalSetWriteOnly((boolean) value); break;
			case EXAMPLES__PROP: internalSetExamples(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case IS_BOOLEAN_SCHEMA__PROP: internalSetIsBooleanSchema((boolean) value); break;
			case BOOLEAN_SCHEMA_VALUE__PROP: internalSetBooleanSchemaValue((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.JsonSchema readJsonSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.JsonSchema result = new com.top_logic.basic.json.schema.model.JsonSchema();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name($SCHEMA__PROP);
		out.value(get$schema());
		out.name($ID__PROP);
		out.value(get$id());
		out.name($ANCHOR__PROP);
		out.value(get$anchor());
		out.name($DYNAMIC_ANCHOR__PROP);
		out.value(get$dynamicAnchor());
		out.name($REF__PROP);
		out.value(get$ref());
		out.name($DYNAMIC_REF__PROP);
		out.value(get$dynamicRef());
		out.name($DEFS__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.JsonSchema> entry : get$defs().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
		out.name($COMMENT__PROP);
		out.value(get$comment());
		out.name($VOCABULARY__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,Boolean> entry : get$vocabulary().entrySet()) {
			out.name(entry.getKey());
			out.value(entry.getValue());
		}
		out.endObject();
		out.name(TYPE__PROP);
		out.beginArray();
		for (String x : getType()) {
			out.value(x);
		}
		out.endArray();
		out.name(ENUM__PROP);
		out.beginArray();
		for (String x : getEnum()) {
			out.value(x);
		}
		out.endArray();
		out.name(CONST__PROP);
		out.value(getConst());
		out.name(MULTIPLE_OF__PROP);
		out.value(getMultipleOf());
		out.name(MAXIMUM__PROP);
		out.value(getMaximum());
		out.name(EXCLUSIVE_MAXIMUM__PROP);
		out.value(getExclusiveMaximum());
		out.name(MINIMUM__PROP);
		out.value(getMinimum());
		out.name(EXCLUSIVE_MINIMUM__PROP);
		out.value(getExclusiveMinimum());
		out.name(MAX_LENGTH__PROP);
		out.value(getMaxLength());
		out.name(MIN_LENGTH__PROP);
		out.value(getMinLength());
		out.name(PATTERN__PROP);
		out.value(getPattern());
		out.name(FORMAT__PROP);
		out.value(getFormat());
		out.name(CONTENT_ENCODING__PROP);
		out.value(getContentEncoding());
		out.name(CONTENT_MEDIA_TYPE__PROP);
		out.value(getContentMediaType());
		if (hasContentSchema()) {
			out.name(CONTENT_SCHEMA__PROP);
			getContentSchema().writeTo(out);
		}
		out.name(MAX_ITEMS__PROP);
		out.value(getMaxItems());
		out.name(MIN_ITEMS__PROP);
		out.value(getMinItems());
		out.name(UNIQUE_ITEMS__PROP);
		out.value(isUniqueItems());
		out.name(MAX_CONTAINS__PROP);
		out.value(getMaxContains());
		out.name(MIN_CONTAINS__PROP);
		out.value(getMinContains());
		out.name(MAX_PROPERTIES__PROP);
		out.value(getMaxProperties());
		out.name(MIN_PROPERTIES__PROP);
		out.value(getMinProperties());
		out.name(REQUIRED__PROP);
		out.beginArray();
		for (String x : getRequired()) {
			out.value(x);
		}
		out.endArray();
		out.name(DEPENDENT_REQUIRED__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.StringArray> entry : getDependentRequired().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
		out.name(PROPERTIES__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.JsonSchema> entry : getProperties().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
		out.name(PATTERN_PROPERTIES__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.JsonSchema> entry : getPatternProperties().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
		if (hasAdditionalProperties()) {
			out.name(ADDITIONAL_PROPERTIES__PROP);
			getAdditionalProperties().writeTo(out);
		}
		if (hasPropertyNames()) {
			out.name(PROPERTY_NAMES__PROP);
			getPropertyNames().writeTo(out);
		}
		out.name(PREFIX_ITEMS__PROP);
		out.beginArray();
		for (com.top_logic.basic.json.schema.model.JsonSchema x : getPrefixItems()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasItems()) {
			out.name(ITEMS__PROP);
			getItems().writeTo(out);
		}
		if (hasContains()) {
			out.name(CONTAINS__PROP);
			getContains().writeTo(out);
		}
		if (hasIf()) {
			out.name(IF__PROP);
			getIf().writeTo(out);
		}
		if (hasThen()) {
			out.name(THEN__PROP);
			getThen().writeTo(out);
		}
		if (hasElse()) {
			out.name(ELSE__PROP);
			getElse().writeTo(out);
		}
		out.name(DEPENDENT_SCHEMAS__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.JsonSchema> entry : getDependentSchemas().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
		out.name(ALL_OF__PROP);
		out.beginArray();
		for (com.top_logic.basic.json.schema.model.JsonSchema x : getAllOf()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ANY_OF__PROP);
		out.beginArray();
		for (com.top_logic.basic.json.schema.model.JsonSchema x : getAnyOf()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ONE_OF__PROP);
		out.beginArray();
		for (com.top_logic.basic.json.schema.model.JsonSchema x : getOneOf()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasNot()) {
			out.name(NOT__PROP);
			getNot().writeTo(out);
		}
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(DEFAULT__PROP);
		out.value(getDefault());
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
		out.name(IS_BOOLEAN_SCHEMA__PROP);
		out.value(isIsBooleanSchema());
		out.name(BOOLEAN_SCHEMA_VALUE__PROP);
		out.value(isBooleanSchemaValue());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case $SCHEMA__PROP: set$schema(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case $ID__PROP: set$id(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case $ANCHOR__PROP: set$anchor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case $DYNAMIC_ANCHOR__PROP: set$dynamicAnchor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case $REF__PROP: set$ref(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case $DYNAMIC_REF__PROP: set$dynamicRef(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case $DEFS__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endObject();
				set$defs(newValue);
				break;
			}
			case $COMMENT__PROP: set$comment(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case $VOCABULARY__PROP: {
				java.util.Map<String, Boolean> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), in.nextBoolean());
				}
				in.endObject();
				set$vocabulary(newValue);
				break;
			}
			case TYPE__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setType(newValue);
			}
			break;
			case ENUM__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setEnum(newValue);
			}
			break;
			case CONST__PROP: setConst(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MULTIPLE_OF__PROP: setMultipleOf(in.nextDouble()); break;
			case MAXIMUM__PROP: setMaximum(in.nextDouble()); break;
			case EXCLUSIVE_MAXIMUM__PROP: setExclusiveMaximum(in.nextDouble()); break;
			case MINIMUM__PROP: setMinimum(in.nextDouble()); break;
			case EXCLUSIVE_MINIMUM__PROP: setExclusiveMinimum(in.nextDouble()); break;
			case MAX_LENGTH__PROP: setMaxLength(in.nextInt()); break;
			case MIN_LENGTH__PROP: setMinLength(in.nextInt()); break;
			case PATTERN__PROP: setPattern(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FORMAT__PROP: setFormat(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_ENCODING__PROP: setContentEncoding(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_MEDIA_TYPE__PROP: setContentMediaType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_SCHEMA__PROP: setContentSchema(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case MAX_ITEMS__PROP: setMaxItems(in.nextInt()); break;
			case MIN_ITEMS__PROP: setMinItems(in.nextInt()); break;
			case UNIQUE_ITEMS__PROP: setUniqueItems(in.nextBoolean()); break;
			case MAX_CONTAINS__PROP: setMaxContains(in.nextInt()); break;
			case MIN_CONTAINS__PROP: setMinContains(in.nextInt()); break;
			case MAX_PROPERTIES__PROP: setMaxProperties(in.nextInt()); break;
			case MIN_PROPERTIES__PROP: setMinProperties(in.nextInt()); break;
			case REQUIRED__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setRequired(newValue);
			}
			break;
			case DEPENDENT_REQUIRED__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.StringArray.readStringArray(in));
				}
				in.endObject();
				setDependentRequired(newValue);
				break;
			}
			case PROPERTIES__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endObject();
				setProperties(newValue);
				break;
			}
			case PATTERN_PROPERTIES__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endObject();
				setPatternProperties(newValue);
				break;
			}
			case ADDITIONAL_PROPERTIES__PROP: setAdditionalProperties(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case PROPERTY_NAMES__PROP: setPropertyNames(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case PREFIX_ITEMS__PROP: {
				java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endArray();
				setPrefixItems(newValue);
			}
			break;
			case ITEMS__PROP: setItems(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case CONTAINS__PROP: setContains(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case IF__PROP: setIf(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case THEN__PROP: setThen(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case ELSE__PROP: setElse(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case DEPENDENT_SCHEMAS__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endObject();
				setDependentSchemas(newValue);
				break;
			}
			case ALL_OF__PROP: {
				java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endArray();
				setAllOf(newValue);
			}
			break;
			case ANY_OF__PROP: {
				java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endArray();
				setAnyOf(newValue);
			}
			break;
			case ONE_OF__PROP: {
				java.util.List<com.top_logic.basic.json.schema.model.JsonSchema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in));
				}
				in.endArray();
				setOneOf(newValue);
			}
			break;
			case NOT__PROP: setNot(com.top_logic.basic.json.schema.model.JsonSchema.readJsonSchema(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEFAULT__PROP: setDefault(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
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
			case IS_BOOLEAN_SCHEMA__PROP: setIsBooleanSchema(in.nextBoolean()); break;
			case BOOLEAN_SCHEMA_VALUE__PROP: setBooleanSchemaValue(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
