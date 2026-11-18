package com.top_logic.ai.core.tools.model.messages;

/**
 * Detailed instance information.
 */
public class InstanceResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.core.tools.model.messages.InstanceResponse} instance.
	 */
	public static com.top_logic.ai.core.tools.model.messages.InstanceResponse create() {
		return new com.top_logic.ai.core.tools.model.messages.InstanceResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.core.tools.model.messages.InstanceResponse} type in JSON format. */
	public static final String INSTANCE_RESPONSE__TYPE = "InstanceResponse";

	/** @see #getTid() */
	public static final String TID__PROP = "_tid";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getAttributes() */
	public static final String ATTRIBUTES__PROP = "attributes";

	/** @see #getMetadata() */
	public static final String METADATA__PROP = "metadata";

	private String _tid = "";

	private com.top_logic.ai.core.tools.model.messages.InstanceTypeInfo _type = null;

	private Object _attributes = null;

	private com.top_logic.ai.core.tools.model.messages.InstanceMetadata _metadata = null;

	/**
	 * Creates a {@link InstanceResponse} instance.
	 *
	 * @see com.top_logic.ai.core.tools.model.messages.InstanceResponse#create()
	 */
	protected InstanceResponse() {
		super();
	}

	/**
	 * Instance TID.
	 */
	public final String getTid() {
		return _tid;
	}

	/**
	 * @see #getTid()
	 */
	public com.top_logic.ai.core.tools.model.messages.InstanceResponse setTid(String value) {
		internalSetTid(value);
		return this;
	}

	/** Internal setter for {@link #getTid()} without chain call utility. */
	protected final void internalSetTid(String value) {
		_tid = value;
	}

	/**
	 * Type information.
	 */
	public final com.top_logic.ai.core.tools.model.messages.InstanceTypeInfo getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.core.tools.model.messages.InstanceResponse setType(com.top_logic.ai.core.tools.model.messages.InstanceTypeInfo value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(com.top_logic.ai.core.tools.model.messages.InstanceTypeInfo value) {
		_type = value;
	}

	/**
	 * Checks, whether {@link #getType()} has a value.
	 */
	public final boolean hasType() {
		return _type != null;
	}

	/**
	 * Object from attribute names to values, holding only the attributes the acting account may read.
	 *
	 * Collections are arrays, dates ISO 8601 texts, and a reference to another object is that
	 * object's identity - <code>_tid</code>, <code>_type</code> and <code>_display</code> - or
	 * absent when the acting account may not read it.
	 */
	public final Object getAttributes() {
		return _attributes;
	}

	/**
	 * @see #getAttributes()
	 */
	public com.top_logic.ai.core.tools.model.messages.InstanceResponse setAttributes(Object value) {
		internalSetAttributes(value);
		return this;
	}

	/** Internal setter for {@link #getAttributes()} without chain call utility. */
	protected final void internalSetAttributes(Object value) {
		_attributes = value;
	}

	/**
	 * Checks, whether {@link #getAttributes()} has a value.
	 */
	public final boolean hasAttributes() {
		return _attributes != null;
	}

	/**
	 * Instance metadata.
	 */
	public final com.top_logic.ai.core.tools.model.messages.InstanceMetadata getMetadata() {
		return _metadata;
	}

	/**
	 * @see #getMetadata()
	 */
	public com.top_logic.ai.core.tools.model.messages.InstanceResponse setMetadata(com.top_logic.ai.core.tools.model.messages.InstanceMetadata value) {
		internalSetMetadata(value);
		return this;
	}

	/** Internal setter for {@link #getMetadata()} without chain call utility. */
	protected final void internalSetMetadata(com.top_logic.ai.core.tools.model.messages.InstanceMetadata value) {
		_metadata = value;
	}

	/**
	 * Checks, whether {@link #getMetadata()} has a value.
	 */
	public final boolean hasMetadata() {
		return _metadata != null;
	}

	@Override
	public String jsonType() {
		return INSTANCE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TID__PROP, 
			TYPE__PROP, 
			ATTRIBUTES__PROP, 
			METADATA__PROP);
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
			case TID__PROP: return getTid();
			case TYPE__PROP: return getType();
			case ATTRIBUTES__PROP: return getAttributes();
			case METADATA__PROP: return getMetadata();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TID__PROP: internalSetTid((String) value); break;
			case TYPE__PROP: internalSetType((com.top_logic.ai.core.tools.model.messages.InstanceTypeInfo) value); break;
			case ATTRIBUTES__PROP: internalSetAttributes((Object) value); break;
			case METADATA__PROP: internalSetMetadata((com.top_logic.ai.core.tools.model.messages.InstanceMetadata) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.core.tools.model.messages.InstanceResponse readInstanceResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.core.tools.model.messages.InstanceResponse result = new com.top_logic.ai.core.tools.model.messages.InstanceResponse();
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
		out.name(TID__PROP);
		out.value(getTid());
		if (hasType()) {
			out.name(TYPE__PROP);
			getType().writeTo(out);
		}
		if (hasAttributes()) {
			out.name(ATTRIBUTES__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getAttributes());
		}
		if (hasMetadata()) {
			out.name(METADATA__PROP);
			getMetadata().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TID__PROP: setTid(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(com.top_logic.ai.core.tools.model.messages.InstanceTypeInfo.readInstanceTypeInfo(in)); break;
			case ATTRIBUTES__PROP: setAttributes(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case METADATA__PROP: setMetadata(com.top_logic.ai.core.tools.model.messages.InstanceMetadata.readInstanceMetadata(in)); break;
			default: super.readField(in, field);
		}
	}

}
