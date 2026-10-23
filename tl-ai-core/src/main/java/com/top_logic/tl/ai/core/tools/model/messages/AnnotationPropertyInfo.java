package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Information about a polymorphic configuration property that requires choosing an implementation.
 */
public class AnnotationPropertyInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo} type in JSON format. */
	public static final String ANNOTATION_PROPERTY_INFO__TYPE = "AnnotationPropertyInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #isRequired() */
	public static final String REQUIRED__PROP = "required";

	/** @see #getImplementations() */
	public static final String IMPLEMENTATIONS__PROP = "implementations";

	private String _name = "";

	private String _description = "";

	private boolean _required = false;

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption> _implementations = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AnnotationPropertyInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo#create()
	 */
	protected AnnotationPropertyInfo() {
		super();
	}

	/**
	 * Property name as declared in the configuration interface.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Human-readable description of the property.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Whether this property is mandatory.
	 */
	public final boolean isRequired() {
		return _required;
	}

	/**
	 * @see #isRequired()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo setRequired(boolean value) {
		internalSetRequired(value);
		return this;
	}

	/** Internal setter for {@link #isRequired()} without chain call utility. */
	protected final void internalSetRequired(boolean value) {
		_required = value;
	}

	/**
	 * Available concrete implementations the agent can choose from.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption> getImplementations() {
		return _implementations;
	}

	/**
	 * @see #getImplementations()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo setImplementations(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption> value) {
		internalSetImplementations(value);
		return this;
	}

	/** Internal setter for {@link #getImplementations()} without chain call utility. */
	protected final void internalSetImplementations(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption> value) {
		if (value == null) throw new IllegalArgumentException("Property 'implementations' cannot be null.");
		_implementations.clear();
		_implementations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getImplementations()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo addImplementation(com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption value) {
		internalAddImplementation(value);
		return this;
	}

	/** Implementation of {@link #addImplementation(com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption)} without chain call utility. */
	protected final void internalAddImplementation(com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption value) {
		_implementations.add(value);
	}

	/**
	 * Removes a value from the {@link #getImplementations()} list.
	 */
	public final void removeImplementation(com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption value) {
		_implementations.remove(value);
	}

	@Override
	public String jsonType() {
		return ANNOTATION_PROPERTY_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			DESCRIPTION__PROP, 
			REQUIRED__PROP, 
			IMPLEMENTATIONS__PROP);
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
			case DESCRIPTION__PROP: return getDescription();
			case REQUIRED__PROP: return isRequired();
			case IMPLEMENTATIONS__PROP: return getImplementations();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case REQUIRED__PROP: internalSetRequired((boolean) value); break;
			case IMPLEMENTATIONS__PROP: internalSetImplementations(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo readAnnotationPropertyInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo result = new com.top_logic.tl.ai.core.tools.model.messages.AnnotationPropertyInfo();
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
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(REQUIRED__PROP);
		out.value(isRequired());
		out.name(IMPLEMENTATIONS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption x : getImplementations()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REQUIRED__PROP: setRequired(in.nextBoolean()); break;
			case IMPLEMENTATIONS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption.readImplementationOption(in));
				}
				in.endArray();
				setImplementations(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
