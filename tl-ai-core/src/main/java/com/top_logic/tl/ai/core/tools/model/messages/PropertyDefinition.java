package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Property definition in a configuration schema.
 */
public class PropertyDefinition extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition} type in JSON format. */
	public static final String PROPERTY_DEFINITION__TYPE = "PropertyDefinition";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getTypeSimpleName() */
	public static final String TYPE_SIMPLE_NAME__PROP = "typeSimpleName";

	/** @see #getElementType() */
	public static final String ELEMENT_TYPE__PROP = "elementType";

	/** @see #getElementTypeSimpleName() */
	public static final String ELEMENT_TYPE_SIMPLE_NAME__PROP = "elementTypeSimpleName";

	/** @see #isMandatory() */
	public static final String MANDATORY__PROP = "mandatory";

	/** @see #isNullable() */
	public static final String NULLABLE__PROP = "nullable";

	/** @see #isMultiple() */
	public static final String MULTIPLE__PROP = "multiple";

	/** @see #isOrdered() */
	public static final String ORDERED__PROP = "ordered";

	/** @see #isHasExplicitDefault() */
	public static final String HAS_EXPLICIT_DEFAULT__PROP = "hasExplicitDefault";

	/** @see #getDefaultValue() */
	public static final String DEFAULT_VALUE__PROP = "defaultValue";

	/** @see #getEnumValues() */
	public static final String ENUM_VALUES__PROP = "enumValues";

	/** @see #getNestedProperties() */
	public static final String NESTED_PROPERTIES__PROP = "nestedProperties";

	private String _name = "";

	private String _kind = "";

	private String _type = "";

	private String _typeSimpleName = "";

	private String _elementType = "";

	private String _elementTypeSimpleName = "";

	private boolean _mandatory = false;

	private boolean _nullable = false;

	private boolean _multiple = false;

	private boolean _ordered = false;

	private boolean _hasExplicitDefault = false;

	private String _defaultValue = "";

	private final java.util.List<String> _enumValues = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition> _nestedProperties = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PropertyDefinition} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition#create()
	 */
	protected PropertyDefinition() {
		super();
	}

	/**
	 * Property name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Property kind (plain, item, list, array, map, complex, derived, ref).
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * Property type (fully-qualified class name).
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Simple type name.
	 */
	public final String getTypeSimpleName() {
		return _typeSimpleName;
	}

	/**
	 * @see #getTypeSimpleName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setTypeSimpleName(String value) {
		internalSetTypeSimpleName(value);
		return this;
	}

	/** Internal setter for {@link #getTypeSimpleName()} without chain call utility. */
	protected final void internalSetTypeSimpleName(String value) {
		_typeSimpleName = value;
	}

	/**
	 * Element type for collections (fully-qualified class name).
	 */
	public final String getElementType() {
		return _elementType;
	}

	/**
	 * @see #getElementType()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setElementType(String value) {
		internalSetElementType(value);
		return this;
	}

	/** Internal setter for {@link #getElementType()} without chain call utility. */
	protected final void internalSetElementType(String value) {
		_elementType = value;
	}

	/**
	 * Simple element type name.
	 */
	public final String getElementTypeSimpleName() {
		return _elementTypeSimpleName;
	}

	/**
	 * @see #getElementTypeSimpleName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setElementTypeSimpleName(String value) {
		internalSetElementTypeSimpleName(value);
		return this;
	}

	/** Internal setter for {@link #getElementTypeSimpleName()} without chain call utility. */
	protected final void internalSetElementTypeSimpleName(String value) {
		_elementTypeSimpleName = value;
	}

	/**
	 * Whether the property is mandatory.
	 */
	public final boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * @see #isMandatory()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	/** Internal setter for {@link #isMandatory()} without chain call utility. */
	protected final void internalSetMandatory(boolean value) {
		_mandatory = value;
	}

	/**
	 * Whether the property is nullable.
	 */
	public final boolean isNullable() {
		return _nullable;
	}

	/**
	 * @see #isNullable()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	/** Internal setter for {@link #isNullable()} without chain call utility. */
	protected final void internalSetNullable(boolean value) {
		_nullable = value;
	}

	/**
	 * Whether the property can have multiple values.
	 */
	public final boolean isMultiple() {
		return _multiple;
	}

	/**
	 * @see #isMultiple()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setMultiple(boolean value) {
		internalSetMultiple(value);
		return this;
	}

	/** Internal setter for {@link #isMultiple()} without chain call utility. */
	protected final void internalSetMultiple(boolean value) {
		_multiple = value;
	}

	/**
	 * Whether multiple values are ordered.
	 */
	public final boolean isOrdered() {
		return _ordered;
	}

	/**
	 * @see #isOrdered()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setOrdered(boolean value) {
		internalSetOrdered(value);
		return this;
	}

	/** Internal setter for {@link #isOrdered()} without chain call utility. */
	protected final void internalSetOrdered(boolean value) {
		_ordered = value;
	}

	/**
	 * Whether the property has an explicit default value.
	 */
	public final boolean isHasExplicitDefault() {
		return _hasExplicitDefault;
	}

	/**
	 * @see #isHasExplicitDefault()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setHasExplicitDefault(boolean value) {
		internalSetHasExplicitDefault(value);
		return this;
	}

	/** Internal setter for {@link #isHasExplicitDefault()} without chain call utility. */
	protected final void internalSetHasExplicitDefault(boolean value) {
		_hasExplicitDefault = value;
	}

	/**
	 * Default value (serialized as string).
	 */
	public final String getDefaultValue() {
		return _defaultValue;
	}

	/**
	 * @see #getDefaultValue()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	/** Internal setter for {@link #getDefaultValue()} without chain call utility. */
	protected final void internalSetDefaultValue(String value) {
		_defaultValue = value;
	}

	/**
	 * Enum values (for enum properties).
	 */
	public final java.util.List<String> getEnumValues() {
		return _enumValues;
	}

	/**
	 * @see #getEnumValues()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setEnumValues(java.util.List<? extends String> value) {
		internalSetEnumValues(value);
		return this;
	}

	/** Internal setter for {@link #getEnumValues()} without chain call utility. */
	protected final void internalSetEnumValues(java.util.List<? extends String> value) {
		_enumValues.clear();
		_enumValues.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getEnumValues()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition addEnumValue(String value) {
		internalAddEnumValue(value);
		return this;
	}

	/** Implementation of {@link #addEnumValue(String)} without chain call utility. */
	protected final void internalAddEnumValue(String value) {
		_enumValues.add(value);
	}

	/**
	 * Removes a value from the {@link #getEnumValues()} list.
	 */
	public final void removeEnumValue(String value) {
		_enumValues.remove(value);
	}

	/**
	 * Nested property definitions (for ITEM/COMPLEX types).
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition> getNestedProperties() {
		return _nestedProperties;
	}

	/**
	 * @see #getNestedProperties()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition setNestedProperties(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition> value) {
		internalSetNestedProperties(value);
		return this;
	}

	/** Internal setter for {@link #getNestedProperties()} without chain call utility. */
	protected final void internalSetNestedProperties(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition> value) {
		if (value == null) throw new IllegalArgumentException("Property 'nestedProperties' cannot be null.");
		_nestedProperties.clear();
		_nestedProperties.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getNestedProperties()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition addNestedProperty(com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition value) {
		internalAddNestedProperty(value);
		return this;
	}

	/** Implementation of {@link #addNestedProperty(com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition)} without chain call utility. */
	protected final void internalAddNestedProperty(com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition value) {
		_nestedProperties.add(value);
	}

	/**
	 * Removes a value from the {@link #getNestedProperties()} list.
	 */
	public final void removeNestedProperty(com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition value) {
		_nestedProperties.remove(value);
	}

	@Override
	public String jsonType() {
		return PROPERTY_DEFINITION__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			KIND__PROP, 
			TYPE__PROP, 
			TYPE_SIMPLE_NAME__PROP, 
			ELEMENT_TYPE__PROP, 
			ELEMENT_TYPE_SIMPLE_NAME__PROP, 
			MANDATORY__PROP, 
			NULLABLE__PROP, 
			MULTIPLE__PROP, 
			ORDERED__PROP, 
			HAS_EXPLICIT_DEFAULT__PROP, 
			DEFAULT_VALUE__PROP, 
			ENUM_VALUES__PROP, 
			NESTED_PROPERTIES__PROP);
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
			case NAME__PROP: return getName();
			case KIND__PROP: return getKind();
			case TYPE__PROP: return getType();
			case TYPE_SIMPLE_NAME__PROP: return getTypeSimpleName();
			case ELEMENT_TYPE__PROP: return getElementType();
			case ELEMENT_TYPE_SIMPLE_NAME__PROP: return getElementTypeSimpleName();
			case MANDATORY__PROP: return isMandatory();
			case NULLABLE__PROP: return isNullable();
			case MULTIPLE__PROP: return isMultiple();
			case ORDERED__PROP: return isOrdered();
			case HAS_EXPLICIT_DEFAULT__PROP: return isHasExplicitDefault();
			case DEFAULT_VALUE__PROP: return getDefaultValue();
			case ENUM_VALUES__PROP: return getEnumValues();
			case NESTED_PROPERTIES__PROP: return getNestedProperties();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case KIND__PROP: internalSetKind((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case TYPE_SIMPLE_NAME__PROP: internalSetTypeSimpleName((String) value); break;
			case ELEMENT_TYPE__PROP: internalSetElementType((String) value); break;
			case ELEMENT_TYPE_SIMPLE_NAME__PROP: internalSetElementTypeSimpleName((String) value); break;
			case MANDATORY__PROP: internalSetMandatory((boolean) value); break;
			case NULLABLE__PROP: internalSetNullable((boolean) value); break;
			case MULTIPLE__PROP: internalSetMultiple((boolean) value); break;
			case ORDERED__PROP: internalSetOrdered((boolean) value); break;
			case HAS_EXPLICIT_DEFAULT__PROP: internalSetHasExplicitDefault((boolean) value); break;
			case DEFAULT_VALUE__PROP: internalSetDefaultValue((String) value); break;
			case ENUM_VALUES__PROP: internalSetEnumValues(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case NESTED_PROPERTIES__PROP: internalSetNestedProperties(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition readPropertyDefinition(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition result = new com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition();
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
		out.name(NAME__PROP);
		out.value(getName());
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(TYPE_SIMPLE_NAME__PROP);
		out.value(getTypeSimpleName());
		out.name(ELEMENT_TYPE__PROP);
		out.value(getElementType());
		out.name(ELEMENT_TYPE_SIMPLE_NAME__PROP);
		out.value(getElementTypeSimpleName());
		out.name(MANDATORY__PROP);
		out.value(isMandatory());
		out.name(NULLABLE__PROP);
		out.value(isNullable());
		out.name(MULTIPLE__PROP);
		out.value(isMultiple());
		out.name(ORDERED__PROP);
		out.value(isOrdered());
		out.name(HAS_EXPLICIT_DEFAULT__PROP);
		out.value(isHasExplicitDefault());
		out.name(DEFAULT_VALUE__PROP);
		out.value(getDefaultValue());
		out.name(ENUM_VALUES__PROP);
		out.beginArray();
		for (String x : getEnumValues()) {
			out.value(x);
		}
		out.endArray();
		out.name(NESTED_PROPERTIES__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition x : getNestedProperties()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE_SIMPLE_NAME__PROP: setTypeSimpleName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ELEMENT_TYPE__PROP: setElementType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ELEMENT_TYPE_SIMPLE_NAME__PROP: setElementTypeSimpleName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MANDATORY__PROP: setMandatory(in.nextBoolean()); break;
			case NULLABLE__PROP: setNullable(in.nextBoolean()); break;
			case MULTIPLE__PROP: setMultiple(in.nextBoolean()); break;
			case ORDERED__PROP: setOrdered(in.nextBoolean()); break;
			case HAS_EXPLICIT_DEFAULT__PROP: setHasExplicitDefault(in.nextBoolean()); break;
			case DEFAULT_VALUE__PROP: setDefaultValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ENUM_VALUES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setEnumValues(newValue);
			}
			break;
			case NESTED_PROPERTIES__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.PropertyDefinition.readPropertyDefinition(in));
				}
				in.endArray();
				setNestedProperties(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
