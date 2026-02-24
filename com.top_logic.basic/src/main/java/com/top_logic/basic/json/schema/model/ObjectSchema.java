package com.top_logic.basic.json.schema.model;

/**
 * Schema for object validation.
 *
 * <p>
 * Validates object instances with property count, required properties,
 * and property schemas.
 * </p>
 */
public class ObjectSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.ObjectSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.ObjectSchema create() {
		return new com.top_logic.basic.json.schema.model.ObjectSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.ObjectSchema} type in JSON format. */
	public static final String OBJECT_SCHEMA__TYPE = "ObjectSchema";

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

	/** @see #getDependentSchemas() */
	public static final String DEPENDENT_SCHEMAS__PROP = "dependentSchemas";

	private Integer _maxProperties = null;

	private Integer _minProperties = null;

	private final java.util.List<String> _required = new java.util.ArrayList<>();

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray> _dependentRequired = new java.util.LinkedHashMap<>();

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> _properties = new java.util.LinkedHashMap<>();

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> _patternProperties = new java.util.LinkedHashMap<>();

	private com.top_logic.basic.json.schema.model.Schema _additionalProperties = null;

	private com.top_logic.basic.json.schema.model.Schema _propertyNames = null;

	private final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> _dependentSchemas = new java.util.LinkedHashMap<>();

	/**
	 * Creates a {@link ObjectSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.ObjectSchema#create()
	 */
	protected ObjectSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.OBJECT_SCHEMA;
	}

	/**
	 * Maximum number of properties.
	 */
	public final Integer getMaxProperties() {
		return _maxProperties;
	}

	/**
	 * @see #getMaxProperties()
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema setMaxProperties(Integer value) {
		internalSetMaxProperties(value);
		return this;
	}

	/** Internal setter for {@link #getMaxProperties()} without chain call utility. */
	protected final void internalSetMaxProperties(Integer value) {
		_maxProperties = value;
	}

	/**
	 * Checks, whether {@link #getMaxProperties()} has a value.
	 */
	public final boolean hasMaxProperties() {
		return _maxProperties != null;
	}

	/**
	 * Minimum number of properties.
	 */
	public final Integer getMinProperties() {
		return _minProperties;
	}

	/**
	 * @see #getMinProperties()
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema setMinProperties(Integer value) {
		internalSetMinProperties(value);
		return this;
	}

	/** Internal setter for {@link #getMinProperties()} without chain call utility. */
	protected final void internalSetMinProperties(Integer value) {
		_minProperties = value;
	}

	/**
	 * Checks, whether {@link #getMinProperties()} has a value.
	 */
	public final boolean hasMinProperties() {
		return _minProperties != null;
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
	public com.top_logic.basic.json.schema.model.ObjectSchema setRequired(java.util.List<? extends String> value) {
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
	public com.top_logic.basic.json.schema.model.ObjectSchema addRequired(String value) {
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
	public com.top_logic.basic.json.schema.model.ObjectSchema setDependentRequired(java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray> value) {
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
	public com.top_logic.basic.json.schema.model.ObjectSchema putDependentRequired(String key, com.top_logic.basic.json.schema.model.StringArray value) {
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
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> getProperties() {
		return _properties;
	}

	/**
	 * @see #getProperties()
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema setProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetProperties(value);
		return this;
	}

	/** Internal setter for {@link #getProperties()} without chain call utility. */
	protected final void internalSetProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'properties' cannot be null.");
		_properties.clear();
		_properties.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getProperties()} map.
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema putProperty(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutProperty(key, value);
		return this;
	}

	/** Implementation of {@link #putProperty(String, com.top_logic.basic.json.schema.model.Schema)} without chain call utility. */
	protected final void  internalPutProperty(String key, com.top_logic.basic.json.schema.model.Schema value) {
		if (_properties.containsKey(key)) {
			throw new IllegalArgumentException("Property 'properties' already contains a value for key '" + key + "'.");
		}
		_properties.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getProperties()} map.
	 */
	public final void removeProperty(String key) {
		_properties.remove(key);
	}

	/**
	 * Schema for properties matching regex patterns.
	 *
	 * <p>
	 * Maps ECMA-262 regex patterns to schemas for matching properties.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> getPatternProperties() {
		return _patternProperties;
	}

	/**
	 * @see #getPatternProperties()
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema setPatternProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetPatternProperties(value);
		return this;
	}

	/** Internal setter for {@link #getPatternProperties()} without chain call utility. */
	protected final void internalSetPatternProperties(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'patternProperties' cannot be null.");
		_patternProperties.clear();
		_patternProperties.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getPatternProperties()} map.
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema putPatternProperty(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutPatternProperty(key, value);
		return this;
	}

	/** Implementation of {@link #putPatternProperty(String, com.top_logic.basic.json.schema.model.Schema)} without chain call utility. */
	protected final void  internalPutPatternProperty(String key, com.top_logic.basic.json.schema.model.Schema value) {
		if (_patternProperties.containsKey(key)) {
			throw new IllegalArgumentException("Property 'patternProperties' already contains a value for key '" + key + "'.");
		}
		_patternProperties.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getPatternProperties()} map.
	 */
	public final void removePatternProperty(String key) {
		_patternProperties.remove(key);
	}

	/**
	 * Schema for additional properties not in <code>properties</code> or <code>patternProperties</code>.
	 */
	public final com.top_logic.basic.json.schema.model.Schema getAdditionalProperties() {
		return _additionalProperties;
	}

	/**
	 * @see #getAdditionalProperties()
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema setAdditionalProperties(com.top_logic.basic.json.schema.model.Schema value) {
		internalSetAdditionalProperties(value);
		return this;
	}

	/** Internal setter for {@link #getAdditionalProperties()} without chain call utility. */
	protected final void internalSetAdditionalProperties(com.top_logic.basic.json.schema.model.Schema value) {
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
	public final com.top_logic.basic.json.schema.model.Schema getPropertyNames() {
		return _propertyNames;
	}

	/**
	 * @see #getPropertyNames()
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema setPropertyNames(com.top_logic.basic.json.schema.model.Schema value) {
		internalSetPropertyNames(value);
		return this;
	}

	/** Internal setter for {@link #getPropertyNames()} without chain call utility. */
	protected final void internalSetPropertyNames(com.top_logic.basic.json.schema.model.Schema value) {
		_propertyNames = value;
	}

	/**
	 * Checks, whether {@link #getPropertyNames()} has a value.
	 */
	public final boolean hasPropertyNames() {
		return _propertyNames != null;
	}

	/**
	 * Dependent schemas by property.
	 *
	 * <p>
	 * When a property from this map is present, the corresponding schema is applied.
	 * </p>
	 */
	public final java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> getDependentSchemas() {
		return _dependentSchemas;
	}

	/**
	 * @see #getDependentSchemas()
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema setDependentSchemas(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDependentSchemas(value);
		return this;
	}

	/** Internal setter for {@link #getDependentSchemas()} without chain call utility. */
	protected final void internalSetDependentSchemas(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'dependentSchemas' cannot be null.");
		_dependentSchemas.clear();
		_dependentSchemas.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getDependentSchemas()} map.
	 */
	public com.top_logic.basic.json.schema.model.ObjectSchema putDependentSchema(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDependentSchema(key, value);
		return this;
	}

	/** Implementation of {@link #putDependentSchema(String, com.top_logic.basic.json.schema.model.Schema)} without chain call utility. */
	protected final void  internalPutDependentSchema(String key, com.top_logic.basic.json.schema.model.Schema value) {
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

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ObjectSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return OBJECT_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MAX_PROPERTIES__PROP, 
			MIN_PROPERTIES__PROP, 
			REQUIRED__PROP, 
			DEPENDENT_REQUIRED__PROP, 
			PROPERTIES__PROP, 
			PATTERN_PROPERTIES__PROP, 
			ADDITIONAL_PROPERTIES__PROP, 
			PROPERTY_NAMES__PROP, 
			DEPENDENT_SCHEMAS__PROP);
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
			case MAX_PROPERTIES__PROP: return getMaxProperties();
			case MIN_PROPERTIES__PROP: return getMinProperties();
			case REQUIRED__PROP: return getRequired();
			case DEPENDENT_REQUIRED__PROP: return getDependentRequired();
			case PROPERTIES__PROP: return getProperties();
			case PATTERN_PROPERTIES__PROP: return getPatternProperties();
			case ADDITIONAL_PROPERTIES__PROP: return getAdditionalProperties();
			case PROPERTY_NAMES__PROP: return getPropertyNames();
			case DEPENDENT_SCHEMAS__PROP: return getDependentSchemas();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MAX_PROPERTIES__PROP: internalSetMaxProperties((Integer) value); break;
			case MIN_PROPERTIES__PROP: internalSetMinProperties((Integer) value); break;
			case REQUIRED__PROP: internalSetRequired(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case DEPENDENT_REQUIRED__PROP: internalSetDependentRequired((java.util.Map<String, com.top_logic.basic.json.schema.model.StringArray>) value); break;
			case PROPERTIES__PROP: internalSetProperties((java.util.Map<String, com.top_logic.basic.json.schema.model.Schema>) value); break;
			case PATTERN_PROPERTIES__PROP: internalSetPatternProperties((java.util.Map<String, com.top_logic.basic.json.schema.model.Schema>) value); break;
			case ADDITIONAL_PROPERTIES__PROP: internalSetAdditionalProperties((com.top_logic.basic.json.schema.model.Schema) value); break;
			case PROPERTY_NAMES__PROP: internalSetPropertyNames((com.top_logic.basic.json.schema.model.Schema) value); break;
			case DEPENDENT_SCHEMAS__PROP: internalSetDependentSchemas((java.util.Map<String, com.top_logic.basic.json.schema.model.Schema>) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.ObjectSchema readObjectSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.ObjectSchema result = new com.top_logic.basic.json.schema.model.ObjectSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasMaxProperties()) {
			out.name(MAX_PROPERTIES__PROP);
			out.value(getMaxProperties());
		}
		if (hasMinProperties()) {
			out.name(MIN_PROPERTIES__PROP);
			out.value(getMinProperties());
		}
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
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.Schema> entry : getProperties().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
		out.name(PATTERN_PROPERTIES__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.Schema> entry : getPatternProperties().entrySet()) {
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
		out.name(DEPENDENT_SCHEMAS__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,com.top_logic.basic.json.schema.model.Schema> entry : getDependentSchemas().entrySet()) {
			out.name(entry.getKey());
			entry.getValue().writeTo(out);
		}
		out.endObject();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
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
				java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.Schema.readSchema(in));
				}
				in.endObject();
				setProperties(newValue);
				break;
			}
			case PATTERN_PROPERTIES__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.Schema.readSchema(in));
				}
				in.endObject();
				setPatternProperties(newValue);
				break;
			}
			case ADDITIONAL_PROPERTIES__PROP: setAdditionalProperties(com.top_logic.basic.json.schema.model.Schema.readSchema(in)); break;
			case PROPERTY_NAMES__PROP: setPropertyNames(com.top_logic.basic.json.schema.model.Schema.readSchema(in)); break;
			case DEPENDENT_SCHEMAS__PROP: {
				java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), com.top_logic.basic.json.schema.model.Schema.readSchema(in));
				}
				in.endObject();
				setDependentSchemas(newValue);
				break;
			}
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
