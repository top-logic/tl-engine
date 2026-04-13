package com.top_logic.ai.service.tools.model.messages;

/**
 * Reference usage information.
 */
public class ReferenceUsage extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.ReferenceUsage} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.ReferenceUsage create() {
		return new com.top_logic.ai.service.tools.model.messages.ReferenceUsage();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.ReferenceUsage} type in JSON format. */
	public static final String REFERENCE_USAGE__TYPE = "ReferenceUsage";

	/** @see #getOwner() */
	public static final String OWNER__PROP = "owner";

	/** @see #getReference() */
	public static final String REFERENCE__PROP = "reference";

	/** @see #getUsage() */
	public static final String USAGE__PROP = "usage";

	private String _owner = "";

	private String _reference = "";

	private String _usage = "";

	/**
	 * Creates a {@link ReferenceUsage} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.ReferenceUsage#create()
	 */
	protected ReferenceUsage() {
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
	public com.top_logic.ai.service.tools.model.messages.ReferenceUsage setOwner(String value) {
		internalSetOwner(value);
		return this;
	}

	/** Internal setter for {@link #getOwner()} without chain call utility. */
	protected final void internalSetOwner(String value) {
		_owner = value;
	}

	/**
	 * Reference name.
	 */
	public final String getReference() {
		return _reference;
	}

	/**
	 * @see #getReference()
	 */
	public com.top_logic.ai.service.tools.model.messages.ReferenceUsage setReference(String value) {
		internalSetReference(value);
		return this;
	}

	/** Internal setter for {@link #getReference()} without chain call utility. */
	protected final void internalSetReference(String value) {
		_reference = value;
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
	public com.top_logic.ai.service.tools.model.messages.ReferenceUsage setUsage(String value) {
		internalSetUsage(value);
		return this;
	}

	/** Internal setter for {@link #getUsage()} without chain call utility. */
	protected final void internalSetUsage(String value) {
		_usage = value;
	}

	@Override
	public String jsonType() {
		return REFERENCE_USAGE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			OWNER__PROP, 
			REFERENCE__PROP, 
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
			case REFERENCE__PROP: return getReference();
			case USAGE__PROP: return getUsage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case OWNER__PROP: internalSetOwner((String) value); break;
			case REFERENCE__PROP: internalSetReference((String) value); break;
			case USAGE__PROP: internalSetUsage((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.ReferenceUsage readReferenceUsage(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.ReferenceUsage result = new com.top_logic.ai.service.tools.model.messages.ReferenceUsage();
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
		out.name(REFERENCE__PROP);
		out.value(getReference());
		out.name(USAGE__PROP);
		out.value(getUsage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OWNER__PROP: setOwner(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REFERENCE__PROP: setReference(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USAGE__PROP: setUsage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
