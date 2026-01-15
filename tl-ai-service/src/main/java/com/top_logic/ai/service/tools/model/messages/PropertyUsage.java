package com.top_logic.ai.service.tools.model.messages;

/**
 * Property usage information.
 */
public class PropertyUsage extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.PropertyUsage} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.PropertyUsage create() {
		return new com.top_logic.ai.service.tools.model.messages.PropertyUsage();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.PropertyUsage} type in JSON format. */
	public static final String PROPERTY_USAGE__TYPE = "PropertyUsage";

	/** @see #getOwner() */
	public static final String OWNER__PROP = "owner";

	/** @see #getProperty() */
	public static final String PROPERTY__PROP = "property";

	/** @see #getUsage() */
	public static final String USAGE__PROP = "usage";

	private String _owner = "";

	private String _property = "";

	private String _usage = "";

	/**
	 * Creates a {@link PropertyUsage} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.PropertyUsage#create()
	 */
	protected PropertyUsage() {
		super();
	}

	/**
	 * Qualified name of the owner type.
	 */
	public final String getOwner() {
		return _owner;
	}

	/**
	 * @see #getOwner()
	 */
	public com.top_logic.ai.service.tools.model.messages.PropertyUsage setOwner(String value) {
		internalSetOwner(value);
		return this;
	}

	/** Internal setter for {@link #getOwner()} without chain call utility. */
	protected final void internalSetOwner(String value) {
		_owner = value;
	}

	/**
	 * Property name.
	 */
	public final String getProperty() {
		return _property;
	}

	/**
	 * @see #getProperty()
	 */
	public com.top_logic.ai.service.tools.model.messages.PropertyUsage setProperty(String value) {
		internalSetProperty(value);
		return this;
	}

	/** Internal setter for {@link #getProperty()} without chain call utility. */
	protected final void internalSetProperty(String value) {
		_property = value;
	}

	/**
	 * Usage description.
	 */
	public final String getUsage() {
		return _usage;
	}

	/**
	 * @see #getUsage()
	 */
	public com.top_logic.ai.service.tools.model.messages.PropertyUsage setUsage(String value) {
		internalSetUsage(value);
		return this;
	}

	/** Internal setter for {@link #getUsage()} without chain call utility. */
	protected final void internalSetUsage(String value) {
		_usage = value;
	}

	@Override
	public String jsonType() {
		return PROPERTY_USAGE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			OWNER__PROP, 
			PROPERTY__PROP, 
			USAGE__PROP);
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
			case OWNER__PROP: return getOwner();
			case PROPERTY__PROP: return getProperty();
			case USAGE__PROP: return getUsage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case OWNER__PROP: internalSetOwner((String) value); break;
			case PROPERTY__PROP: internalSetProperty((String) value); break;
			case USAGE__PROP: internalSetUsage((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.PropertyUsage readPropertyUsage(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.PropertyUsage result = new com.top_logic.ai.service.tools.model.messages.PropertyUsage();
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
		out.name(OWNER__PROP);
		out.value(getOwner());
		out.name(PROPERTY__PROP);
		out.value(getProperty());
		out.name(USAGE__PROP);
		out.value(getUsage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OWNER__PROP: setOwner(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROPERTY__PROP: setProperty(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USAGE__PROP: setUsage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
