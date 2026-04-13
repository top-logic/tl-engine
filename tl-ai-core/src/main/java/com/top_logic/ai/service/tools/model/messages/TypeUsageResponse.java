package com.top_logic.ai.service.tools.model.messages;

/**
 * Type usage analysis response.
 */
public class TypeUsageResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.TypeUsageResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.TypeUsageResponse create() {
		return new com.top_logic.ai.service.tools.model.messages.TypeUsageResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.TypeUsageResponse} type in JSON format. */
	public static final String TYPE_USAGE_RESPONSE__TYPE = "TypeUsageResponse";

	/** @see #getProperties() */
	public static final String PROPERTIES__PROP = "properties";

	/** @see #getReferences() */
	public static final String REFERENCES__PROP = "references";

	/** @see #getSubclasses() */
	public static final String SUBCLASSES__PROP = "subclasses";

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.PropertyUsage> _properties = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.ReferenceUsage> _references = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.SubclassUsage> _subclasses = new java.util.ArrayList<>();

	/**
	 * Creates a {@link TypeUsageResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.TypeUsageResponse#create()
	 */
	protected TypeUsageResponse() {
		super();
	}

	/**
	 * Properties that use this type as value type.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.PropertyUsage> getProperties() {
		return _properties;
	}

	/**
	 * @see #getProperties()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeUsageResponse setProperties(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.PropertyUsage> value) {
		internalSetProperties(value);
		return this;
	}

	/** Internal setter for {@link #getProperties()} without chain call utility. */
	protected final void internalSetProperties(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.PropertyUsage> value) {
		if (value == null) throw new IllegalArgumentException("Property 'properties' cannot be null.");
		_properties.clear();
		_properties.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getProperties()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeUsageResponse addProperty(com.top_logic.ai.service.tools.model.messages.PropertyUsage value) {
		internalAddProperty(value);
		return this;
	}

	/** Implementation of {@link #addProperty(com.top_logic.ai.service.tools.model.messages.PropertyUsage)} without chain call utility. */
	protected final void internalAddProperty(com.top_logic.ai.service.tools.model.messages.PropertyUsage value) {
		_properties.add(value);
	}

	/**
	 * Removes a value from the {@link #getProperties()} list.
	 */
	public final void removeProperty(com.top_logic.ai.service.tools.model.messages.PropertyUsage value) {
		_properties.remove(value);
	}

	/**
	 * References that point to this type.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.ReferenceUsage> getReferences() {
		return _references;
	}

	/**
	 * @see #getReferences()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeUsageResponse setReferences(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.ReferenceUsage> value) {
		internalSetReferences(value);
		return this;
	}

	/** Internal setter for {@link #getReferences()} without chain call utility. */
	protected final void internalSetReferences(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.ReferenceUsage> value) {
		if (value == null) throw new IllegalArgumentException("Property 'references' cannot be null.");
		_references.clear();
		_references.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getReferences()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeUsageResponse addReference(com.top_logic.ai.service.tools.model.messages.ReferenceUsage value) {
		internalAddReference(value);
		return this;
	}

	/** Implementation of {@link #addReference(com.top_logic.ai.service.tools.model.messages.ReferenceUsage)} without chain call utility. */
	protected final void internalAddReference(com.top_logic.ai.service.tools.model.messages.ReferenceUsage value) {
		_references.add(value);
	}

	/**
	 * Removes a value from the {@link #getReferences()} list.
	 */
	public final void removeReference(com.top_logic.ai.service.tools.model.messages.ReferenceUsage value) {
		_references.remove(value);
	}

	/**
	 * Classes that extend this type.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.SubclassUsage> getSubclasses() {
		return _subclasses;
	}

	/**
	 * @see #getSubclasses()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeUsageResponse setSubclasses(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.SubclassUsage> value) {
		internalSetSubclasses(value);
		return this;
	}

	/** Internal setter for {@link #getSubclasses()} without chain call utility. */
	protected final void internalSetSubclasses(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.SubclassUsage> value) {
		if (value == null) throw new IllegalArgumentException("Property 'subclasses' cannot be null.");
		_subclasses.clear();
		_subclasses.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSubclasses()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeUsageResponse addSubclass(com.top_logic.ai.service.tools.model.messages.SubclassUsage value) {
		internalAddSubclass(value);
		return this;
	}

	/** Implementation of {@link #addSubclass(com.top_logic.ai.service.tools.model.messages.SubclassUsage)} without chain call utility. */
	protected final void internalAddSubclass(com.top_logic.ai.service.tools.model.messages.SubclassUsage value) {
		_subclasses.add(value);
	}

	/**
	 * Removes a value from the {@link #getSubclasses()} list.
	 */
	public final void removeSubclass(com.top_logic.ai.service.tools.model.messages.SubclassUsage value) {
		_subclasses.remove(value);
	}

	@Override
	public String jsonType() {
		return TYPE_USAGE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			PROPERTIES__PROP, 
			REFERENCES__PROP, 
			SUBCLASSES__PROP);
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
			case PROPERTIES__PROP: return getProperties();
			case REFERENCES__PROP: return getReferences();
			case SUBCLASSES__PROP: return getSubclasses();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case PROPERTIES__PROP: internalSetProperties(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.PropertyUsage.class, value)); break;
			case REFERENCES__PROP: internalSetReferences(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.ReferenceUsage.class, value)); break;
			case SUBCLASSES__PROP: internalSetSubclasses(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.SubclassUsage.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.TypeUsageResponse readTypeUsageResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.TypeUsageResponse result = new com.top_logic.ai.service.tools.model.messages.TypeUsageResponse();
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
		out.name(PROPERTIES__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.PropertyUsage x : getProperties()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(REFERENCES__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.ReferenceUsage x : getReferences()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(SUBCLASSES__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.SubclassUsage x : getSubclasses()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case PROPERTIES__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.PropertyUsage> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.PropertyUsage.readPropertyUsage(in));
				}
				in.endArray();
				setProperties(newValue);
			}
			break;
			case REFERENCES__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.ReferenceUsage> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.ReferenceUsage.readReferenceUsage(in));
				}
				in.endArray();
				setReferences(newValue);
			}
			break;
			case SUBCLASSES__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.SubclassUsage> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.SubclassUsage.readSubclassUsage(in));
				}
				in.endArray();
				setSubclasses(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
