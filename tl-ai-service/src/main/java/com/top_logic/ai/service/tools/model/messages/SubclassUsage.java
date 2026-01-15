package com.top_logic.ai.service.tools.model.messages;

/**
 * Subclass usage information.
 */
public class SubclassUsage extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.SubclassUsage} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.SubclassUsage create() {
		return new com.top_logic.ai.service.tools.model.messages.SubclassUsage();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.SubclassUsage} type in JSON format. */
	public static final String SUBCLASS_USAGE__TYPE = "SubclassUsage";

	/** @see #getSubclass() */
	public static final String SUBCLASS__PROP = "subclass";

	/** @see #getUsage() */
	public static final String USAGE__PROP = "usage";

	private String _subclass = "";

	private String _usage = "";

	/**
	 * Creates a {@link SubclassUsage} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.SubclassUsage#create()
	 */
	protected SubclassUsage() {
		super();
	}

	/**
	 * Qualified name of the subclass.
	 */
	public final String getSubclass() {
		return _subclass;
	}

	/**
	 * @see #getSubclass()
	 */
	public com.top_logic.ai.service.tools.model.messages.SubclassUsage setSubclass(String value) {
		internalSetSubclass(value);
		return this;
	}

	/** Internal setter for {@link #getSubclass()} without chain call utility. */
	protected final void internalSetSubclass(String value) {
		_subclass = value;
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
	public com.top_logic.ai.service.tools.model.messages.SubclassUsage setUsage(String value) {
		internalSetUsage(value);
		return this;
	}

	/** Internal setter for {@link #getUsage()} without chain call utility. */
	protected final void internalSetUsage(String value) {
		_usage = value;
	}

	@Override
	public String jsonType() {
		return SUBCLASS_USAGE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUBCLASS__PROP, 
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
			case SUBCLASS__PROP: return getSubclass();
			case USAGE__PROP: return getUsage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUBCLASS__PROP: internalSetSubclass((String) value); break;
			case USAGE__PROP: internalSetUsage((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.SubclassUsage readSubclassUsage(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.SubclassUsage result = new com.top_logic.ai.service.tools.model.messages.SubclassUsage();
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
		out.name(SUBCLASS__PROP);
		out.value(getSubclass());
		out.name(USAGE__PROP);
		out.value(getUsage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUBCLASS__PROP: setSubclass(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USAGE__PROP: setUsage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
