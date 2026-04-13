package com.top_logic.ai.service.tools.model.messages;

/**
 * Annotation definition schema response.
 */
public class AnnotationDefinitionResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse create() {
		return new com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse} type in JSON format. */
	public static final String ANNOTATION_DEFINITION_RESPONSE__TYPE = "AnnotationDefinitionResponse";

	/** @see #getConfigInterface() */
	public static final String CONFIG_INTERFACE__PROP = "configInterface";

	/** @see #getTagName() */
	public static final String TAG_NAME__PROP = "tagName";

	/** @see #isAbstract() */
	public static final String ABSTRACT__PROP = "abstract";

	/** @see #getSuperInterfaces() */
	public static final String SUPER_INTERFACES__PROP = "superInterfaces";

	/** @see #getProperties() */
	public static final String PROPERTIES__PROP = "properties";

	private String _configInterface = "";

	private String _tagName = "";

	private boolean _abstract = false;

	private final java.util.List<String> _superInterfaces = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.PropertyDefinition> _properties = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AnnotationDefinitionResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse#create()
	 */
	protected AnnotationDefinitionResponse() {
		super();
	}

	/**
	 * Configuration interface class name.
	 */
	public final String getConfigInterface() {
		return _configInterface;
	}

	/**
	 * @see #getConfigInterface()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse setConfigInterface(String value) {
		internalSetConfigInterface(value);
		return this;
	}

	/** Internal setter for {@link #getConfigInterface()} without chain call utility. */
	protected final void internalSetConfigInterface(String value) {
		_configInterface = value;
	}

	/**
	 * Annotation tag name.
	 */
	public final String getTagName() {
		return _tagName;
	}

	/**
	 * @see #getTagName()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse setTagName(String value) {
		internalSetTagName(value);
		return this;
	}

	/** Internal setter for {@link #getTagName()} without chain call utility. */
	protected final void internalSetTagName(String value) {
		_tagName = value;
	}

	/**
	 * Whether the annotation is abstract.
	 */
	public final boolean isAbstract() {
		return _abstract;
	}

	/**
	 * @see #isAbstract()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse setAbstract(boolean value) {
		internalSetAbstract(value);
		return this;
	}

	/** Internal setter for {@link #isAbstract()} without chain call utility. */
	protected final void internalSetAbstract(boolean value) {
		_abstract = value;
	}

	/**
	 * List of super interface class names.
	 */
	public final java.util.List<String> getSuperInterfaces() {
		return _superInterfaces;
	}

	/**
	 * @see #getSuperInterfaces()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse setSuperInterfaces(java.util.List<? extends String> value) {
		internalSetSuperInterfaces(value);
		return this;
	}

	/** Internal setter for {@link #getSuperInterfaces()} without chain call utility. */
	protected final void internalSetSuperInterfaces(java.util.List<? extends String> value) {
		_superInterfaces.clear();
		_superInterfaces.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSuperInterfaces()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse addSuperInterface(String value) {
		internalAddSuperInterface(value);
		return this;
	}

	/** Implementation of {@link #addSuperInterface(String)} without chain call utility. */
	protected final void internalAddSuperInterface(String value) {
		_superInterfaces.add(value);
	}

	/**
	 * Removes a value from the {@link #getSuperInterfaces()} list.
	 */
	public final void removeSuperInterface(String value) {
		_superInterfaces.remove(value);
	}

	/**
	 * List of property definitions.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.PropertyDefinition> getProperties() {
		return _properties;
	}

	/**
	 * @see #getProperties()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse setProperties(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.PropertyDefinition> value) {
		internalSetProperties(value);
		return this;
	}

	/** Internal setter for {@link #getProperties()} without chain call utility. */
	protected final void internalSetProperties(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.PropertyDefinition> value) {
		if (value == null) throw new IllegalArgumentException("Property 'properties' cannot be null.");
		_properties.clear();
		_properties.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getProperties()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse addProperty(com.top_logic.ai.service.tools.model.messages.PropertyDefinition value) {
		internalAddProperty(value);
		return this;
	}

	/** Implementation of {@link #addProperty(com.top_logic.ai.service.tools.model.messages.PropertyDefinition)} without chain call utility. */
	protected final void internalAddProperty(com.top_logic.ai.service.tools.model.messages.PropertyDefinition value) {
		_properties.add(value);
	}

	/**
	 * Removes a value from the {@link #getProperties()} list.
	 */
	public final void removeProperty(com.top_logic.ai.service.tools.model.messages.PropertyDefinition value) {
		_properties.remove(value);
	}

	@Override
	public String jsonType() {
		return ANNOTATION_DEFINITION_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONFIG_INTERFACE__PROP, 
			TAG_NAME__PROP, 
			ABSTRACT__PROP, 
			SUPER_INTERFACES__PROP, 
			PROPERTIES__PROP);
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
			case CONFIG_INTERFACE__PROP: return getConfigInterface();
			case TAG_NAME__PROP: return getTagName();
			case ABSTRACT__PROP: return isAbstract();
			case SUPER_INTERFACES__PROP: return getSuperInterfaces();
			case PROPERTIES__PROP: return getProperties();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONFIG_INTERFACE__PROP: internalSetConfigInterface((String) value); break;
			case TAG_NAME__PROP: internalSetTagName((String) value); break;
			case ABSTRACT__PROP: internalSetAbstract((boolean) value); break;
			case SUPER_INTERFACES__PROP: internalSetSuperInterfaces(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case PROPERTIES__PROP: internalSetProperties(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.PropertyDefinition.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse readAnnotationDefinitionResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse result = new com.top_logic.ai.service.tools.model.messages.AnnotationDefinitionResponse();
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
		out.name(CONFIG_INTERFACE__PROP);
		out.value(getConfigInterface());
		out.name(TAG_NAME__PROP);
		out.value(getTagName());
		out.name(ABSTRACT__PROP);
		out.value(isAbstract());
		out.name(SUPER_INTERFACES__PROP);
		out.beginArray();
		for (String x : getSuperInterfaces()) {
			out.value(x);
		}
		out.endArray();
		out.name(PROPERTIES__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.PropertyDefinition x : getProperties()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONFIG_INTERFACE__PROP: setConfigInterface(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TAG_NAME__PROP: setTagName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ABSTRACT__PROP: setAbstract(in.nextBoolean()); break;
			case SUPER_INTERFACES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setSuperInterfaces(newValue);
			}
			break;
			case PROPERTIES__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.PropertyDefinition> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.PropertyDefinition.readPropertyDefinition(in));
				}
				in.endArray();
				setProperties(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
